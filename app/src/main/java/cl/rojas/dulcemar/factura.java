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
        Button btnPedidos = (Button) findViewById(R.id.pedidos);

        // Inicializar TextViews
        facturaID = findViewById(R.id.FacturaID);
        facturaNombre = findViewById(R.id.FacturaNombre);
        facturaPrecioSinIVA = findViewById(R.id.FacturaPrecioSinIVA);
        facturaIVA = findViewById(R.id.FacturaIVA);
        facturaPrecioTotal = findViewById(R.id.FacturaPrecioTotal);
        facturaDireccion = findViewById(R.id.FacturaDireccion);

        // Obtener los datos del Intent
        String nombre = getIntent().getStringExtra("nombre");
        double precioTotal = getIntent().getDoubleExtra("precioTotal", 0.0);
        String direccion = getIntent().getStringExtra("direccion");

        // Calcular IVA y precio sin IVA
        double precioSinIVA = precioTotal / 1.19; // Precio sin IVA
        double iva = precioTotal - precioSinIVA; // IVA

        // Generar ID única para la factura
        String idFactura = UUID.randomUUID().toString();

        btnPedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(factura.this, pedidosCliente.class));
            }
        });

        // Mostrar datos en la UI
        facturaID.setText(idFactura);
        facturaNombre.setText(nombre);
        facturaPrecioSinIVA.setText(String.format("Precio sin IVA: $%.2f", precioSinIVA));
        facturaIVA.setText(String.format("IVA: $%.2f", iva));
        facturaPrecioTotal.setText(String.format("Precio Total: $%.2f", precioTotal));
        facturaDireccion.setText(direccion);

        // Guardar la factura en Firestore
        guardarFactura(idFactura, nombre, precioSinIVA, iva, precioTotal, direccion);
    }

    private void guardarFactura(String idFactura, String nombre, double precioSinIVA, double iva, double precioTotal, String direccion) {
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