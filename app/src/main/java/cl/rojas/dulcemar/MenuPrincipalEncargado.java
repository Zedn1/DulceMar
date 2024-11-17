package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuPrincipalEncargado extends AppCompatActivity {

    private ListView listaPedidosEncargado;
    private FirebaseFirestore db;
    private List<PedidoEncargado> listaPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal_encargado); // Asegúrate de que el nombre del layout sea correcto

        listaPedidosEncargado = findViewById(R.id.listaPedidosEncargado);
        db = FirebaseFirestore.getInstance();
        listaPedidos = new ArrayList<>();



        cargarPedidosEnProceso();
    }

    private void cargarPedidosEnProceso() {
        db.collection("pedido")
                .whereEqualTo("estado", "En proceso")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId(); // Obtén el ID del documento
                            String nombre = document.getString("nombre");
                            String direccion = document.getString("direccion");
                            String estado = document.getString("estado");

                            List<Map<String, Object>> productosList = (List<Map<String, Object>>) document.get("pedidos");
                            Map<String, Integer> productos = new HashMap<>();

                            if (productosList != null) {
                                for (Map<String, Object> producto : productosList) {
                                    Integer cantidad = (( Long) producto.get("cantidad")).intValue();
                                    Map<String, Object> productoData = (Map<String, Object>) producto.get("producto");
                                    String nombreProducto = (String) productoData.get("nombre");
                                    productos.put(nombreProducto, cantidad);
                                }
                            }

                            // Cambia a PedidoEncargado
                            PedidoEncargado pedido = new PedidoEncargado(id, nombre, direccion, estado, productos);
                            listaPedidos.add(pedido);
                        }
                        mostrarPedidos();
                    } else {
                        Toast.makeText(MenuPrincipalEncargado.this, "Error al cargar los pedidos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarPedidos() {
        PedidoEncargadoAdapter adapter = new PedidoEncargadoAdapter(this, listaPedidos);
        listaPedidosEncargado.setAdapter(adapter);
    }
}