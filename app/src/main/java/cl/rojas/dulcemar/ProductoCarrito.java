package cl.rojas.dulcemar;

import java.io.Serializable;

public class ProductoCarrito implements Serializable {
    private Producto producto;
    private int cantidad;

    public ProductoCarrito(Producto producto) {
        this.producto = producto;
        this.cantidad = 1; // Inicialmente, la cantidad es 1
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void incrementarCantidad() {
        cantidad++;
    }

    public void decrementarCantidad() {
        if (cantidad > 0) {
            cantidad--;
        }
    }

    @Override
    public String toString() {
        return producto.getNombre() + " - $" + producto.getPrecio() + " (Cantidad: " + cantidad + ")";
    }
}