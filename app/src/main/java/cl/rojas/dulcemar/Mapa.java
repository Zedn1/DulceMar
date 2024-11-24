package cl.rojas.dulcemar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class Mapa extends AppCompatActivity {

    private MapView mapView;
    private Marker markerRepartidor;
    private Marker markerPedido;
    private Location locationRepartidor;
    private String pedidoId; // ID del pedido
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient; // Para obtener la ubicación
    private DocumentReference repartidorRef; // Referencia al documento del repartidor en Firestore
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String userType; // Tipo de usuario (repartidor o cliente)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        setContentView(R.layout.activity_mapa);

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Inicializa FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Obtener el ID del pedido desde el Intent
        pedidoId = getIntent().getStringExtra("pedidoId");

        db = FirebaseFirestore.getInstance(); // Inicializa Firestore
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // Inicializa el cliente de ubicación

        // Verifica el tipo de usuario
        verificarTipoUsuario();

        // Configura la solicitud de ubicación
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(15000); // Intervalo de 15 segundos
        locationRequest.setFastestInterval(5000); // Intervalo más rápido de 5 segundos
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Prioridad alta para obtener la ubicación más precisa

        // Inicializa el callback de ubicación
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        locationRepartidor = location;

                        // Actualiza el marcador del repartidor en el mapa
                        if (markerRepartidor == null) {
                            markerRepartidor = new Marker(mapView);
                            markerRepartidor.setTitle("Repartidor");
                            mapView.getOverlays().add(markerRepartidor);
                        }
                        markerRepartidor.setPosition(new GeoPoint(locationRepartidor.getLatitude(), locationRepartidor.getLongitude()));
                        mapView.invalidate();

                        // Guardar la ubicación en Firestore
                        guardarUbicacionEnFirestore(locationRepartidor);
                    }
                }
            }
        };

        // Verifica y solicita permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            initializeLocation();
        }

        // Iniciar recuperación de coordenadas desde Firestore
        obtenerCoordenadasDesdeFirestore();
    }

    private void verificarTipoUsuario() {
        FirebaseUser  user = mAuth.getCurrentUser ();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    userType = documentSnapshot.getString("userType");
                    if ("Repartidor".equals(userType)) {
                        // Si es repartidor, iniciar actualizaciones de ubicación
                        startLocationUpdates();
                    } else {
                        // Si es cliente, primero obtenemos el ID del repartidor desde Firestore
                        obtenerIdRepartidorDelPedido();
                    }
                }
            });
        }
    }

    private void obtenerIdRepartidorDelPedido() {
        db.collection("pedido").document(pedidoId) // Usar el ID del pedido
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtener el ID del repartidor
                        String idRepartidor = documentSnapshot.getString("IdRepartidor");
                        if (idRepartidor != null) {
                            mostrarUbicacionRepartidor(idRepartidor); // Llama a mostrarUbicacionRepartidor con el ID del repartidor
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener ID del repartidor", e);
                });
    }
    private void initializeLocation() {
        // Establece la ubicación inicial del mapa (por ejemplo, Chillán, Chile)
        mapView.getController().setZoom(15);
        mapView.getController().setCenter(new GeoPoint(-36.6052, -72.1040)); // Coordenadas de Chillán
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void guardarUbicacionEnFirestore(Location location) {
        FirebaseUser  user = mAuth.getCurrentUser ();
        if (user != null) {
            String repartidorId = user.getUid(); // Obtiene el UID del usuario autenticado
            repartidorRef = db.collection("repartidores").document(repartidorId);

            // Verifica si el documento existe antes de actualizar
            repartidorRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Si el documento existe, actualiza la ubicación
                    repartidorRef.update("latitudRepartidor", location.getLatitude(), "longitudRepartidor", location.getLongitude())
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Ubicación del repartidor actualizada correctamente"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar la ubicación del repartidor", e));
                } else {
                    // Si el documento no existe, créalo
                    repartidorRef.set(new Repartidor(location.getLatitude(), location.getLongitude()))
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Documento creado y ubicación del repartidor guardada correctamente"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error al crear el documento del repartidor", e));
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Error al verificar la existencia del documento", e);
            });
        }
    }

    private void mostrarUbicacionRepartidor(String repartidorId) {
        // Obtener la ubicación del repartidor desde Firestore usando el ID del repartidor
        db.collection("repartidores").document(repartidorId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e);
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        double lat = documentSnapshot.getDouble("latitudRepartidor");
                        double lon = documentSnapshot.getDouble("longitudRepartidor");

                        runOnUiThread(() -> {
                            // Marca la ubicación del repartidor en el mapa
                            if (markerRepartidor == null) {
                                markerRepartidor = new Marker(mapView);
                                markerRepartidor.setTitle("Repartidor");
                                mapView.getOverlays().add(markerRepartidor);
                            }
                            markerRepartidor.setPosition(new GeoPoint(lat, lon));
                            mapView.invalidate();
                        });
                    }
                });
    }

    private void obtenerCoordenadasDesdeFirestore() {
        db.collection("pedido").document(pedidoId) // Usar el ID del pedido
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            double lat = documentSnapshot.getDouble("latitud");
                            double lon = documentSnapshot.getDouble("longitud");
                            // Marca la ubicación del pedido en el mapa
                            markerPedido = new Marker(mapView);
                            markerPedido.setPosition(new GeoPoint(lat, lon));
                            markerPedido.setTitle("Ubicación del Pedido");
                            mapView.getOverlays().add(markerPedido);
                            mapView.invalidate();

                            // Centrar el mapa en la ubicación del pedido
                            mapView.getController().setCenter(new GeoPoint(lat, lon));

                        } else {
                            Log.e("Firestore", "El documento no existe");
                            runOnUiThread(() -> Toast.makeText(Mapa.this, "No se encontraron coordenadas en Firestore", Toast.LENGTH_SHORT).show());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener coordenadas", e);
                    runOnUiThread(() -> Toast.makeText(Mapa.this, "Error al obtener coordenadas de Firestore", Toast.LENGTH_SHORT).show());
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener las actualizaciones de ubicación
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}