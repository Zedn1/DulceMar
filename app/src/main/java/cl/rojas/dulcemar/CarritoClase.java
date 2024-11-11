package cl.rojas.dulcemar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CarritoClase implements Serializable {
    private static CarritoClase instancia;
    private List<ProductoCarrito> productos;

    private CarritoClase() {
        productos = new ArrayList<>();
    }

    public static CarritoClase getInstancia() {
        if (instancia == null) {
            instancia = new CarritoClase();
        }
        return instancia;
    }


    public void agregarProducto(Producto producto) {
        for (ProductoCarrito pc : productos) {
            if (pc.getProducto().getNombre().equals(producto.getNombre())) {
                pc.incrementarCantidad(); // Incrementa la cantidad si ya existe
                return;
            }
        }
        productos.add(new ProductoCarrito(producto)); // Agrega un nuevo producto si no existe
    }

    public void aumentarCantidad(Producto producto) {
        for (ProductoCarrito pc : productos) {
            if (pc.getProducto().getNombre().equals(producto.getNombre())) {
                pc.incrementarCantidad(); // Aumenta la cantidad
                return;
            }
        }
    }

    public void disminuirCantidad(Producto producto) {
        for (ProductoCarrito pc : productos) {
            if (pc.getProducto().getNombre().equals(producto.getNombre())) {
                if (pc.getCantidad() > 1) {
                    pc.decrementarCantidad(); // Disminuye la cantidad si es mayor a 1
                } else {
                    productos.remove(pc); // Elimina el producto si la cantidad es 1
                }
                return;
            }
        }
    }

    public void eliminarProducto(Producto producto) {
        productos.removeIf(pc -> pc.getProducto().getNombre().equals(producto.getNombre()));
    }





    public double calcularTotal() {
        double total = 0;
        for (ProductoCarrito pc : productos) {
            total += pc.getProducto().getPrecio() * pc.getCantidad();
        }
        return total;
    }

    public List<ProductoCarrito> getProductos() {
        return productos;
    }
}