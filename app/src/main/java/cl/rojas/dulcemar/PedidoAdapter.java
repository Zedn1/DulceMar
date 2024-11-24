package cl.rojas.dulcemar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class PedidoAdapter extends ArrayAdapter<Pedido> {
    public PedidoAdapter(Context context, List<Pedido> pedidos) {
        super(context, 0, pedidos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Pedido pedido = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_pedido, parent, false);
        }

        TextView nombreTextView = convertView.findViewById(R.id.nombrePedido);
        TextView direccionTextView = convertView.findViewById(R.id.direccionPedido);
        TextView estadoTextView = convertView.findViewById(R.id.estadoPedido);
        TextView productosTextView = convertView.findViewById(R.id.productosPedido);
        Button irAlMapa = convertView.findViewById(R.id.botonMapaCliente);

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

        irAlMapa.setOnClickListener(view -> {
            // Obtener el contexto
            Context context = getContext();
            // Crear un Intent para iniciar la actividad Mapa
            Intent intent = new Intent(context, Mapa.class);
            // Obtener el ID del pedido o cualquier dato necesario
            String pedidoId = pedido.getId(); // Asegúrate de que tu clase Pedido tenga un método getId()
            // Pasar el ID del pedido al Intent
            intent.putExtra("pedidoId", pedidoId);
            // Iniciar la actividad Mapa
            context.startActivity(intent);
        });

        return convertView;
    }
}