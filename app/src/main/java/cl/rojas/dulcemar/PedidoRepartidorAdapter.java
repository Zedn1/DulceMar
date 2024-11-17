package cl.rojas.dulcemar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class PedidoRepartidorAdapter extends ArrayAdapter<PedidoRepartidor> {
    private FirebaseFirestore db;

    public PedidoRepartidorAdapter(Context context, List<PedidoRepartidor> pedidos) {
        super(context, 0, pedidos);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PedidoRepartidor pedido = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_repartidores, parent, false);
        }

        TextView nombreTextView = convertView.findViewById(R.id.nombrePedidoRepartidor);
        TextView direccionTextView = convertView.findViewById(R.id.direccionPedidoRepartidor);
        TextView estadoTextView = convertView.findViewById(R.id.estadoPedidoRepartidor);
        TextView productosTextView = convertView.findViewById(R.id.productosPedidoRepartidor);
        ImageButton aprobarButton = convertView.findViewById(R.id.AprobarPedidoRepartidor);
        ImageButton cancelarButton = convertView.findViewById(R.id.CancelarPedidoRepartidor);

        nombreTextView.setText(pedido.getNombre());
        direccionTextView.setText(pedido.getDireccion());
        estadoTextView.setText(pedido.getEstado());

        // Muestra los productos
        StringBuilder productosString = new StringBuilder();
        for (Map.Entry<String, Integer> entry : pedido.getProductos().entrySet()) {
            productosString.append(entry.getKey()).append(" (").append(entry.getValue()).append("), ");
        }
        // Elimina la última coma y espacio
        if (productosString.length() > 0) {
            productosString.setLength(productosString.length() - 2);
        }
        productosTextView.setText(productosString.toString());

        // Configura los botones
        aprobarButton.setOnClickListener(v -> {
            actualizarEstadoPedido(pedido, "Envio Aceptado");
        });

        cancelarButton.setOnClickListener(v -> {
            actualizarEstadoPedido(pedido, "Envio Cancelado");
        });

        return convertView;
    }

    private void actualizarEstadoPedido(PedidoRepartidor pedido, String nuevoEstado) {
        String pedidoId = pedido.getId(); // Asegúrate de que tu clase PedidoRepartidor tenga un método para obtener el ID

        db.collection("pedido").document(pedidoId)
                .update("estado", nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    pedido.setEstado(nuevoEstado); // Actualiza el estado en el objeto
                    notifyDataSetChanged(); // Notifica que los datos han cambiado
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores
                });
    }
}