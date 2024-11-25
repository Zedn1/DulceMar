package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class busqueda extends AppCompatActivity {

    private static final int REQUEST_CODE_CARRITO = 1;
    private RecyclerView recyclerView;
    private ProductoAdapter adapter;
    private List<Producto> productos;
    private FirebaseFirestore db;
    private CarritoClase carrito; // Agregar carrito

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busqueda);

        carrito = CarritoClase.getInstancia(); // Obtener la instancia del carrito

        recyclerView = findViewById(R.id.recycler_productos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productos = new ArrayList<>();
        adapter = new ProductoAdapter(productos, this, carrito); // Pasar el carrito al adaptador
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        cargarProductos();

        Button btnIrCarrito = findViewById(R.id.btnIrCarrito);
        ImageButton btnOpcionesBusqueda = findViewById(R.id.BotonOpcionesBusqueda);





        btnIrCarrito.setOnClickListener(v -> {
            Intent intent = new Intent(this, carrito.class);
            intent.putExtra("carrito", carrito); // Pasa el carrito actual
            startActivityForResult(intent, REQUEST_CODE_CARRITO); // Usa la constante aquí
        });


        btnOpcionesBusqueda.setOnClickListener(view -> {
            mostrarMenuOpciones(view);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CARRITO && resultCode == RESULT_OK) {
            carrito = (CarritoClase) data.getSerializableExtra("carrito");
        }
    }

    private void cargarProductos() {
        db.collection("producto").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Producto producto = document.toObject(Producto.class);
                    productos.add(producto);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void mostrarMenuOpciones(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_opciones, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.opcionPedidos) {
                    irAPedidos();
                    return true;
                } else {
                    return false;
                }
            }
        });
        popupMenu.show();
    }


    private void irAPedidos() {
        startActivity(new Intent(busqueda.this, pedidosCliente.class));
    }
}