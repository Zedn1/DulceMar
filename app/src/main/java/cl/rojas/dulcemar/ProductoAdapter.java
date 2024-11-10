package cl.rojas.dulcemar;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> productos;
    private Context context;
    private CarritoClase carrito; // Agregar carrito

    public ProductoAdapter(List<Producto> productos, Context context, CarritoClase carrito) {
        this.productos = productos;
        this.context = context;
        this.carrito = carrito; // Inicializar carrito
    }

    @NonNull
    @Override
    public ProductoAdapter.ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_productos, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoAdapter.ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.tvNombre.setText(producto.getNombre());
        holder.tvPrecio.setText(String.valueOf(producto.getPrecio()));

        holder.btnAgregarCarrito.setOnClickListener(view -> {
            carrito.agregarProducto(producto); // Agregar producto al carrito
            Toast.makeText(context, "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio;
        Button btnAgregarCarrito;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            btnAgregarCarrito = itemView.findViewById(R.id.btnAgregarCarrito);
        }
    }
}