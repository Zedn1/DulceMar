package cl.rojas.dulcemar;

import java.io.Serializable;

public class Producto implements Serializable {
    private String nombre;
    private int precio; // Precio en centavos

    public Producto() {}

    public Producto(String nombre, int precio) {
        this.nombre = nombre;
        this.precio = precio; // Asegúrate de pasar el precio en centavos
    }

    public String getNombre() {
        return nombre;
    }

    public int getPrecio() {
        return precio; // Devuelve el precio como int
    }

    @Override
    public String toString() {
        return nombre + " - $" + (precio / 100.0); // Muestra el precio en formato decimal solo para depuración
    }
}