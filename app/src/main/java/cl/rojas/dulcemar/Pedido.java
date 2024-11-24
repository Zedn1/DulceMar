package cl.rojas.dulcemar;

import java.util.Map;

public class Pedido {
    private String id;
    private String nombre;
    private String direccion;
    private String estado;
    private Map<String, Integer> productos; // Suponiendo que los productos son un mapa de nombre a cantidad

    public Pedido() {
    }

    public Pedido(String id, String nombre, String direccion, String estado, Map<String, Integer> productos) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.estado = estado;
        this.productos = productos;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEstado() {
        return estado;
    }

    public Map<String, Integer> getProductos() {
        return productos;
    }
}