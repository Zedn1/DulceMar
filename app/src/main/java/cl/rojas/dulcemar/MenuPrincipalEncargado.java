package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuPrincipalEncargado extends AppCompatActivity {

    private ListView listaPedidosEncargado;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<PedidoEncargado> listaPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal_encargado); // Asegúrate de que el nombre del layout sea correcto

        listaPedidosEncargado = findViewById(R.id.listaPedidosEncargado);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listaPedidos = new ArrayList<>();
        ImageButton btnOpcionesEncargado = findViewById(R.id.menuOpcionesEncargado);

        btnOpcionesEncargado.setOnClickListener(view -> {
            mostrarMenuOpciones(view);
        });




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

    private void mostrarMenuOpciones(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_opciones_encargado, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.CerrarSesionEncargado) {
                    cerrarSesion();
                    return true;
                } else {
                    return false;
                }
            }
        });
        popupMenu.show();
    }

    private void cerrarSesion(){
        mAuth.signOut();
        finish();
        startActivity(new Intent(MenuPrincipalEncargado.this, MainActivity.class));
    }

}