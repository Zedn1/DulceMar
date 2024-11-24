package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MetodoPago extends AppCompatActivity {

    private EditText nombrePago, correoPago, RUT, nTarjeta;
    private AutoCompleteTextView direccionPago; // Cambiado a AutoCompleteTextView
    private Button confirmarPago;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metodo_pago);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        nombrePago = findViewById(R.id.NombrePago);
        correoPago = findViewById(R.id.CorreoPago);
        direccionPago = findViewById(R.id.DireccionPago); // Cambiado a AutoCompleteTextView
        confirmarPago = findViewById(R.id.ConfirmarPago);
        RUT = findViewById(R.id.PagoRUT);
        nTarjeta = findViewById(R.id.PagoNTarjeta);

        FirebaseUser  user = mAuth.getCurrentUser ();

        if (user == null) {
            // Redirigir al usuario a la pantalla de inicio de sesión
            Intent intent = new Intent(MetodoPago.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finaliza la actividad actual
            return; // Salir del método
        }

        // Configurar el botón de confirmar pago
        confirmarPago.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarPago();
            }
        });

        // Configurar el listener para el AutoCompleteTextView
        direccionPago.setOnItemClickListener((parent, view, position, id) -> {
            String selectedAddress = (String) parent.getItemAtPosition(position);
            // Aquí puedes hacer algo con la dirección seleccionada si es necesario
        });

        // Agregar un TextWatcher para buscar direcciones
        direccionPago.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) { // Solo buscar si hay más de 2 caracteres
                    buscarDirecciones(s.toString());
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void buscarDirecciones(String query) {
        new Thread(() -> {
            try {
                String urlString = "https://nominatim.openstreetmap.org/search?q=" + URLEncoder.encode(query, "UTF-8") + "&format=json&addressdetails=1&limit=5";

                // Crear un TrustManager que no valide los certificados
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                        }
                };

                // Instalar el TrustManager que confía en todos los certificados
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(sc.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier((hostname, session) -> true) // Aceptar todos los nombres de host
                        .build();

                Request request = new Request.Builder().url(urlString).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    Log.d("API Response", responseData); // Log para verificar la respuesta

                    Gson gson = new Gson();
                    JsonArray jsonArray = gson.fromJson(responseData, JsonArray.class);

                    List<String> addressList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                        String address = jsonObject.get("display_name").getAsString();
                        addressList.add(address);
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MetodoPago.this, android.R.layout.simple_dropdown_item_1line, addressList);
                        direccionPago.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                Log.e("BuscarDirecciones", "Error al buscar direcciones", e);
            }
        }).start();
    }

    private void confirmarPago() {
        FirebaseUser  user = mAuth.getCurrentUser ();
        String nombre = nombrePago.getText().toString().trim();
        String correo = correoPago.getText().toString().trim();
        String usuario = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Usuario Anónimo";
        String direccion = direccionPago.getText().toString().trim();
        String pagoRUT = RUT.getText().toString().trim();
        String pagoTarjeta = nTarjeta.getText().toString().trim();

        // Verificar que los campos no estén vacíos
        if (nombre.isEmpty() || correo.isEmpty() || direccion.isEmpty() || pagoRUT.isEmpty() || pagoTarjeta.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Geocodificar la dirección para obtener coordenadas
        geocodificarDireccion(direccion);
    }


    private void geocodificarDireccion(String direccion) {
        new Thread(() -> {
            try {
                String urlString = "https://nominatim.openstreetmap.org/search?q=" + URLEncoder.encode(direccion, "UTF-8") + "&format=json&addressdetails=1&limit=1";

                // Crear un TrustManager que no valide los certificados
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                        }
                };

                // Instalar el TrustManager que confía en todos los certificados
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(sc.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier((hostname, session) -> true) // Aceptar todos los nombres de host
                        .build();

                Request request = new Request.Builder().url(urlString).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String responseData = response.body().string();
                    Log.d("API Response", responseData); // Log para verificar la respuesta

                    Gson gson = new Gson();
                    JsonArray jsonArray = gson.fromJson(responseData, JsonArray.class);

                    if (jsonArray.size() > 0) {
                        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                        double latitud = jsonObject.get("lat").getAsDouble();
                        double longitud = jsonObject.get("lon").getAsDouble();

                        // Guardar dirección y coordenadas en Firestore
                        guardarDatosEnFirestore(direccion, latitud, longitud);
                    } else {
                        runOnUiThread(() -> Toast.makeText(MetodoPago.this, "No se encontraron coordenadas para la dirección", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                Log.e("Geocodificación", "Error al geocodificar la dirección", e);
            }
        }).start();
    }

    private void guardarDatosEnFirestore(String direccion, double latitud, double longitud) {
        FirebaseUser  user = mAuth.getCurrentUser ();
        String nombre = nombrePago.getText().toString().trim();
        String correo = correoPago.getText().toString().trim();
        String usuario = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Usuario Anónimo";
        String pagoRUT = RUT.getText().toString().trim();
        String pagoTarjeta = nTarjeta.getText().toString().trim();

        int precioTotal = CarritoClase.getInstancia().calcularTotal(); // Cambiado a int

        Map<String, Object> datosPago = new HashMap<>();
        datosPago.put("direccion", direccion);
        datosPago.put("latitud", latitud);
        datosPago.put("longitud", longitud);
        datosPago.put("estado", "En proceso");
        datosPago.put("nombre", nombre);
        datosPago.put("usuario", usuario);
        datosPago.put("correo", correo);
        datosPago.put("pedidos", CarritoClase.getInstancia().getProductos());

        db.collection("pedido")
                .add(datosPago)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MetodoPago.this, "Pago confirmado", Toast.LENGTH_SHORT).show();
                    CarritoClase.getInstancia().vaciarCarrito();
                    Intent intent = new Intent(MetodoPago.this, factura.class);
                    intent.putExtra("nombre", nombre);
                    intent.putExtra("precioTotal", precioTotal); // Pasar como int
                    intent.putExtra("direccion", direccion);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MetodoPago.this, "Error al confirmar el pago", Toast.LENGTH_SHORT).show();
                });
    }
}