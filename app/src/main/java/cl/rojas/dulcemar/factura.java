package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class factura extends AppCompatActivity {

    private TextView facturaID, facturaNombre, facturaPrecioSinIVA, facturaIVA, facturaPrecioTotal, facturaDireccion;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factura);

        db = FirebaseFirestore.getInstance();
        Button btnPedidos = findViewById(R.id.pedidos);

        // Inicializar TextViews
        facturaID = findViewById(R.id.FacturaID);
        facturaNombre = findViewById(R.id.FacturaNombre);
        facturaPrecioSinIVA = findViewById(R.id.FacturaPrecioSinIVA);
        facturaIVA = findViewById(R.id.FacturaIVA);
        facturaPrecioTotal = findViewById(R.id.FacturaPrecioTotal);
        facturaDireccion = findViewById(R.id.FacturaDireccion);

        // Obtener los datos del Intent
        String nombre = getIntent().getStringExtra("nombre");
        int precioTotal = getIntent().getIntExtra("precioTotal", 0); // Cambiado a int
        String direccion = getIntent().getStringExtra("direccion");

        // Calcular IVA y precio sin IVA
        int precioSinIVA = precioTotal * 100 / 119; // Precio sin IVA en centavos
        int iva = precioTotal - precioSinIVA; // IVA en centavos

        // Generar ID única para la factura
        String idFactura = UUID.randomUUID().toString();

        btnPedidos.setOnClickListener(view -> startActivity(new Intent(factura.this, pedidosCliente.class)));

        // Mostrar datos en la UI
        facturaID.setText(idFactura);
        facturaNombre.setText(nombre);
        facturaPrecioSinIVA.setText(String.format("Precio sin IVA: %d CLP", precioSinIVA)); // Muestra el precio sin IVA
        facturaIVA.setText(String.format("IVA: %d CLP", iva)); // Muestra el IVA
        facturaPrecioTotal.setText(String.format("Precio Total: %d CLP", precioTotal)); // Muestra el precio total
        facturaDireccion.setText(direccion);

        // Guardar la factura en Firestore
        guardarFactura(idFactura, nombre, precioSinIVA, iva, precioTotal, direccion);
    }

    private void guardarFactura(String idFactura, String nombre, int precioSinIVA, int iva, int precioTotal, String direccion) {
        // Crear un mapa para los datos de la factura
        Map<String, Object> datosFactura = new HashMap<>();
        datosFactura.put("DireccionAEnviar", direccion);
        datosFactura.put("IVA", iva);
        datosFactura.put("PrecioTotal", precioTotal);
        datosFactura.put("idfactura", idFactura);
        datosFactura.put("nombre", nombre);
        datosFactura.put("preciosinIVA", precioSinIVA);

        // Guardar en Firestore
        db.collection("factura")
                .add(datosFactura)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(factura.this, "Factura guardada con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(factura.this, "Error al guardar la factura", Toast.LENGTH_SHORT).show();
                });
    }
}