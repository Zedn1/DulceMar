package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuPrincipalRepartidor extends AppCompatActivity {

    private ListView listaPedidosRepartidor;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<PedidoRepartidor> listaPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal_repartidor); // Asegúrate de que el nombre del layout sea correcto

        listaPedidosRepartidor = findViewById(R.id.listaPedidosRepartidor);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listaPedidos = new ArrayList<>();
        ImageButton btnOpcionesRepartidor = findViewById(R.id.menuOpcionesRepartidor);


        btnOpcionesRepartidor.setOnClickListener(view -> {
            mostrarMenuOpciones(view);
        });



        cargarPedidosListos();
    }

    private void cargarPedidosListos() {
        // Crear una consulta con múltiples estados
        db.collection("pedido")
                .whereIn("estado", Arrays.asList("Pedido Listo", "Envio Aceptado"))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaPedidos.clear(); // Limpiar la lista antes de agregar nuevos pedidos
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String nombre = document.getString("nombre");
                            String direccion = document.getString("direccion");
                            String estado = document.getString("estado");

                            List<Map<String, Object>> productosList = (List<Map<String, Object>>) document.get("pedidos");
                            Map<String, Integer> productos = new HashMap<>();

                            if (productosList != null) {
                                for (Map<String, Object> producto : productosList) {
                                    Integer cantidad = ((Long) producto.get("cantidad")).intValue();
                                    Map<String, Object> productoData = (Map<String, Object>) producto.get("producto");
                                    String nombreProducto = (String) productoData.get("nombre");
                                    productos.put(nombreProducto, cantidad);
                                }
                            }

                            // Crea un nuevo objeto PedidoRepartidor
                            PedidoRepartidor pedido = new PedidoRepartidor(id, nombre, direccion, estado, productos);
                            listaPedidos.add(pedido);
                        }
                        mostrarPedidos();
                    } else {
                        Toast.makeText(MenuPrincipalRepartidor.this, "Error al cargar los pedidos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarPedidos() {
        PedidoRepartidorAdapter adapter = new PedidoRepartidorAdapter(this, listaPedidos);
        listaPedidosRepartidor.setAdapter(adapter);
    }

    private void mostrarMenuOpciones(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_opciones_repartidor, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.CerrarSesionRepartidor) {
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
        startActivity(new Intent(MenuPrincipalRepartidor.this, MainActivity.class));
    }

}