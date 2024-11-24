package cl.rojas.dulcemar;

public class Repartidor {
    private double latitudRepartidor;
    private double longitudRepartidor;

    public Repartidor() {
    }

    public Repartidor(double latitudRepartidor, double longitudRepartidor) {
        this.latitudRepartidor = latitudRepartidor;
        this.longitudRepartidor = longitudRepartidor;
    }

    public double getLatitudRepartidor() {
        return latitudRepartidor;
    }

    public void setLatitudRepartidor(double latitudRepartidor) {
        this.latitudRepartidor = latitudRepartidor;
    }

    public double getLongitudRepartidor() {
        return longitudRepartidor;
    }

    public void setLongitudRepartidor(double longitudRepartidor) {
        this.longitudRepartidor = longitudRepartidor;
    }
}