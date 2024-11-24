package cl.rojas.dulcemar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class PedidoRepartidorAdapter extends ArrayAdapter<PedidoRepartidor> {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public PedidoRepartidorAdapter(Context context, List<PedidoRepartidor> pedidos) {
        super(context, 0, pedidos);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Inicializa FirebaseAuth
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
        ImageButton irMapa = convertView.findViewById(R.id.VerMapaRepartidor);

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

        irMapa.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), Mapa.class);
            intent.putExtra("pedidoId", pedido.getId()); // Asegúrate de pasar el ID del pedido
            getContext().startActivity(intent);
        });

        return convertView;
    }

    private void actualizarEstadoPedido(PedidoRepartidor pedido, String nuevoEstado) {
        String pedidoId = pedido.getId(); // Asegúrate de que tu clase PedidoRepartidor tenga un método para obtener el ID

        // Obtener el usuario autenticado
        FirebaseUser  user = mAuth.getCurrentUser ();
        if (user != null) {
            String repartidorId = user.getUid(); // Obtiene el UID del repartidor autenticado

            // Primero, obtén el nombre del repartidor desde la colección "users"
            db.collection("users").document(repartidorId)
                    .get()
                    .addOnSuccessListener(userDocument -> {
                        if (userDocument.exists()) {
                            String repartidorNombre = userDocument.getString("name"); // Obtiene el nombre del repartidor

                            // Actualiza el estado del pedido y guarda el nombre y el ID del repartidor
                            db.collection("pedido").document(pedidoId)
                                    .update("estado", nuevoEstado,
                                            "repartidorNombre", repartidorNombre,
                                            "IdRepartidor", repartidorId) // Agrega el ID del repartidor
                                    .addOnSuccessListener(aVoid -> {
                                        pedido.setEstado(nuevoEstado); // Actualiza el estado en el objeto
                                        notifyDataSetChanged(); // Notifica que los datos han cambiado
                                    })
                                    .addOnFailureListener(e -> {
                                        // Manejo de errores
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Manejo de errores al obtener el nombre del repartidor
                    });
        }
    }
}