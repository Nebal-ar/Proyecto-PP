package com.alquinow.modelo;

public class PropiedadImagen {
    private int idImagen;
    private int idPropiedadFk;
    private int idPropiedad; // Alternativa por compatibilidad de mapeo
    private String rutaArchivo;
    private boolean esPrincipal;

    public PropiedadImagen() {}

    public int getIdImagen() { return idImagen; }
    public void setIdImagen(int idImagen) { this.idImagen = idImagen; }

    public int getIdPropiedadFk() { return idPropiedadFk; }
    public void setIdPropiedadFk(int idPropiedadFk) { this.idPropiedadFk = idPropiedadFk; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public boolean isEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(boolean esPrincipal) { this.esPrincipal = esPrincipal; }
}