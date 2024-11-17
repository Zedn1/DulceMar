package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class pedidosCliente extends AppCompatActivity {

    private ListView listaPedidosCliente;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Pedido> listaPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_cliente);

        listaPedidosCliente = findViewById(R.id.listaPedidosCliente);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listaPedidos = new ArrayList<>();

        Button irAInicio = (Button) findViewById(R.id.btnIrAInicioPedidosAct);

        irAInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(pedidosCliente.this, MenuPrincipal.class));
            }
        });

        FirebaseUser  user = mAuth.getCurrentUser ();
        if (user != null) {
            cargarPedidos(user.getDisplayName());
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarPedidos(String usuario) {
        db.collection("pedido")
                .whereEqualTo("usuario", usuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombre = document.getString("nombre");
                            String direccion = document.getString("direccion");
                            String estado = document.getString("estado");

                            // Cambia esto para manejar una lista de productos
                            List<Map<String, Object>> productosList = (List<Map<String, Object>>) document.get("pedidos");
                            Map<String, Integer> productos = new HashMap<>();

                            // Convertir la lista a un mapa
                            if (productosList != null) {
                                for (Map<String, Object> producto : productosList) {
                                    Integer cantidad = ((Long) producto.get("cantidad")).intValue(); // Asegúrate de que la cantidad sea un Long en Firestore
                                    Map<String, Object> productoData = (Map<String, Object>) producto.get("producto");
                                    String nombreProducto = (String) productoData.get("nombre");

                                    // Agregar el producto y su cantidad al mapa
                                    productos.put(nombreProducto, cantidad);
                                }
                            }

                            Pedido pedido = new Pedido(nombre, direccion, estado, productos);
                            listaPedidos.add(pedido);
                        }
                        mostrarPedidos();
                    } else {
                        Toast.makeText(pedidosCliente.this, "Error al cargar los pedidos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarPedidos() {
        PedidoAdapter adapter = new PedidoAdapter(this, listaPedidos);
        listaPedidosCliente.setAdapter(adapter);
    }
}