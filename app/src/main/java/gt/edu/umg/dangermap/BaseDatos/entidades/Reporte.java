package gt.edu.umg.dangermap.BaseDatos.entidades;

public class Reporte {
    private int id; //
    private String tipoIncidente;
    private double latitud;
    private double longitud;
    private String imagenRuta;

    // constructor
    public Reporte(int id, String tipoIncidente, double latitud, double longitud, String imagenRuta) {
        this.id = id; //
        this.tipoIncidente = tipoIncidente;
        this.latitud = latitud;
        this.longitud = longitud;
        this.imagenRuta = imagenRuta;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public String getTipoIncidente() {
        return tipoIncidente;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getImagenRuta() {
        return imagenRuta;
    }
}