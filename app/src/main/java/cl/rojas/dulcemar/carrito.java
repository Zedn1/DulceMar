package cl.rojas.dulcemar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class carrito extends AppCompatActivity {
    private CarritoClase carritoClase;
    private ListView listViewCarrito;
    private CarritoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        carritoClase = CarritoClase.getInstancia();


        listViewCarrito = findViewById(R.id.listViewCarrito);
        mostrarProductosEnCarrito();
    }
    private void actualizarTotal() {
        TextView tvTotal = findViewById(R.id.tvTotal);
        tvTotal.setText("Precio Total: $" + String.format("%.2f", carritoClase.calcularTotal()));
    }

    private void mostrarProductosEnCarrito() {
        adapter = new CarritoAdapter(this, carritoClase.getProductos());
        listViewCarrito.setAdapter(adapter);
        actualizarTotal(); // Llama a actualizarTotal aquí
    }



    private class CarritoAdapter extends ArrayAdapter<ProductoCarrito> {
        public CarritoAdapter(Context context, List<ProductoCarrito> productos) {
            super(context, 0, productos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_producto_carrito, parent, false);
            }

            ProductoCarrito productoCarrito = getItem(position);
            TextView tvNombreProducto = convertView.findViewById(R.id.tvNombreProducto);
            TextView tvCantidad = convertView.findViewById(R.id.tvCantidad);
            Button btnAumentar = convertView.findViewById(R.id.btnAumentar);
            Button btnDisminuir = convertView.findViewById(R.id.btnDisminuir);
            Button btnEliminar = convertView.findViewById(R.id.btnEliminar);

            tvNombreProducto.setText(productoCarrito.getProducto().getNombre());
            tvCantidad.setText(String.valueOf(productoCarrito.getCantidad()));

            btnAumentar.setOnClickListener(v -> {
                carritoClase.aumentarCantidad(productoCarrito.getProducto());
                notifyDataSetChanged();
                ((carrito) getContext()).actualizarTotal(); // Llama a actualizarTotal
            });

            btnDisminuir.setOnClickListener(v -> {
                carritoClase.disminuirCantidad(productoCarrito.getProducto());
                notifyDataSetChanged();
                ((carrito) getContext()).actualizarTotal(); // Llama a actualizarTotal
            });

            btnEliminar.setOnClickListener(v -> {
                carritoClase.eliminarProducto(productoCarrito.getProducto());
                notifyDataSetChanged();
                ((carrito) getContext()).actualizarTotal(); // Llama a actualizarTotal
            });

            return convertView;
        }
    }
}