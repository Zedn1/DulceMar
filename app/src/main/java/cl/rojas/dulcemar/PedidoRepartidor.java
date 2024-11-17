package cl.rojas.dulcemar;

import java.util.Map;

public class PedidoRepartidor {
    private String id; // Agrega este campo
    private String nombre;
    private String direccion;
    private String estado;
    private Map<String, Integer> productos;

    public PedidoRepartidor() {
    }

    public PedidoRepartidor(String id, String nombre, String direccion, String estado, Map<String, Integer> productos) {
        this.id = id; // Inicializa el ID
        this.nombre = nombre;
        this.direccion = direccion;
        this.estado = estado;
        this.productos = productos;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Map<String, Integer> getProductos() {
        return productos;
    }
}