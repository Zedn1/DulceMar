package cl.rojas.dulcemar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

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
    private Polyline currentPolyline; // Variable para almacenar la polilínea actual

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

                        // Obtener la ubicación del pedido y dibujar la ruta
                        if (markerPedido != null) {
                            GeoPoint origen = new GeoPoint(locationRepartidor.getLatitude(), locationRepartidor.getLongitude());
                            GeoPoint destino = markerPedido.getPosition();
                            obtenerRuta(origen, destino); // Llama al método para obtener la ruta
                        }
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

    private List<GeoPoint> decodePoly(String encoded) {
        List<GeoPoint> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result >> 1) ^ -(result & 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result >> 1) ^ -(result & 1));
            lng += dlng;

            GeoPoint p = new GeoPoint((double) (lat / 1E5), (double) (lng / 1E5));
            poly.add(p);
        }
        return poly;
    }

    private void obtenerRuta(GeoPoint origen, GeoPoint destino) {
        String url = "http://router.project-osrm.org/route/v1/driving/" +
                origen.getLongitude() + "," + origen.getLatitude() + ";" +
                destino.getLongitude() + "," + destino.getLatitude() +
                "?overview=full";

        Log.d("OSRM URL", url); // Imprime la URL

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("OSRM Response", response); // Imprime la respuesta completa
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray routes = jsonResponse.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            String geometry = route.getString("geometry"); // Obtén la geometría como String

                            // Decodifica la polilínea
                            List<GeoPoint> routePoints = decodePoly(geometry);

                            // Elimina la polilínea anterior si existe
                            if (currentPolyline != null) {
                                mapView.getOverlays().remove(currentPolyline);
                            }

                            // Dibuja la nueva línea de la ruta
                            currentPolyline = new Polyline();
                            currentPolyline.setPoints(routePoints);
                            currentPolyline.setColor(Color.RED);
                            currentPolyline.setWidth(10.0f);
                            mapView.getOverlays().add(currentPolyline);
                            mapView.invalidate();
                        }
                    } catch (JSONException e) {
                        Log.e("OSRM", "Error al analizar la respuesta JSON", e);
                    }
                },
                error -> Log.e("OSRM", "Error en la solicitud de ruta", error)
        );

        queue.add(stringRequest);
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

                            // Obtener la ubicación del pedido y dibujar la ruta
                            if (markerPedido != null) {
                                GeoPoint origen = new GeoPoint(lat, lon);
                                GeoPoint destino = markerPedido.getPosition();
                                obtenerRuta(origen, destino); // Llama al método para obtener la ruta
                            }
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
                            String direccion = documentSnapshot.getString("direccion"); // Obtener la dirección
                            String nombreCliente = documentSnapshot.getString("nombre"); // Obtener el nombre del cliente
                            String nombreRepartidor = documentSnapshot.getString("repartidorNombre"); // Obtener el nombre del repartidor
                            String estado = documentSnapshot.getString("estado"); // Obtener el estado del pedido

                            // Marca la ubicación del pedido en el mapa
                            markerPedido = new Marker(mapView);
                            markerPedido.setPosition(new GeoPoint(lat, lon));
                            markerPedido.setTitle("Ubicación del Pedido");
                            mapView.getOverlays().add(markerPedido);
                            mapView.invalidate();

                            // Centrar el mapa en la ubicación del pedido
                            mapView.getController().setCenter(new GeoPoint(lat, lon));

                            // Actualizar los TextView
                            actualizarInformacionEntrega(nombreCliente, direccion, nombreRepartidor, estado);
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

    private void actualizarInformacionEntrega(String nombreCliente, String direccion, String nombreRepartidor, String estado) {
        // Obtener los TextViews
        TextView mapaNombre = findViewById(R.id.MapaNombre);
        TextView mapaDireccion = findViewById(R.id.MapaDireccion);

        // Verificar el tipo de usuario y actualizar los TextViews
        if ("Repartidor".equals(userType)) {
            // El repartidor siempre ve el nombre del cliente
            mapaNombre.setText(nombreCliente); // Nombre del cliente para el repartidor
        } else {
            // Para el cliente, verificar el estado del pedido
            if ("Envio Aceptado".equals(estado)) {
                // Si el pedido está aceptado, mostrar el nombre del repartidor
                if (nombreRepartidor != null && !nombreRepartidor.isEmpty()) {
                    mapaNombre.setText(nombreRepartidor); // Nombre del repartidor para el cliente
                } else {
                    mapaNombre.setText("Ningún repartidor ha aceptado el pedido"); // Mensaje si no hay repartidor
                }
            } else {
                mapaNombre.setText("El pedido aún no ha sido aceptado"); // Mensaje si el pedido no está aceptado
            }
        }
        mapaDireccion.setText(direccion); // Dirección del pedido
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