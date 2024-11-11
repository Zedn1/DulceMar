package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MetodoPago extends AppCompatActivity {

    private EditText nombrePago, correoPago, callePago, nCasaPago, RUT, nTarjeta;
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
        callePago = findViewById(R.id.CallePago);
        nCasaPago = findViewById(R.id.NCasaPago);
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
    }

    private void confirmarPago() {
        FirebaseUser user = mAuth.getCurrentUser();
        String nombre = nombrePago.getText().toString().trim();
        String correo = correoPago.getText().toString().trim();
        String usuario = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Usuario Anónimo";
        String calle = callePago.getText().toString().trim();
        String numeroCasa = nCasaPago.getText().toString().trim();
        String pagoRUT = RUT.getText().toString().trim();
        String pagoTarjeta = nTarjeta.getText().toString().trim();

        // Verificar que los campos no estén vacíos
        if (nombre.isEmpty() || correo.isEmpty() || calle.isEmpty() || numeroCasa.isEmpty() || pagoRUT.isEmpty() || pagoTarjeta.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double precioTotal = CarritoClase.getInstancia().calcularTotal();

        // Crear un mapa para los datos
        Map<String, Object> datosPago = new HashMap<>();
        datosPago.put("direccion", calle + " " + numeroCasa);
        datosPago.put("estado", "En proceso");
        datosPago.put("nombre", nombre); // Guardar el nombre del usuario autenticado
        datosPago.put("usuario", usuario);
        datosPago.put("correo", correo);
        datosPago.put("pedidos", CarritoClase.getInstancia().getProductos()); // Obtener productos del carrito

        // Guardar en Firestore
        db.collection("pedido")
                .add(datosPago)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MetodoPago.this, "Pago confirmado", Toast.LENGTH_SHORT).show();
                    // Ir a la actividad de factura
                    Intent intent = new Intent(MetodoPago.this, factura.class);
                    intent.putExtra("nombre", nombre);
                    intent.putExtra("precioTotal", precioTotal); // Pasar el precio total
                    intent.putExtra("direccion", calle + " " + numeroCasa); // Pasar la dirección
                    startActivity(intent);
                    finish(); // Finaliza la actividad actual
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MetodoPago.this, "Error al confirmar el pago", Toast.LENGTH_SHORT).show();
                });
    }
}