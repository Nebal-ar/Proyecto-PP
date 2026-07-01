package com.alquinow.modelo;

import java.math.BigDecimal;

/**
 * Representa una fila de la tabla Propiedad.
 */
public class Propiedad {

    private int idPropiedad;
    private int idVendedorFk;
    private Integer estadiaMinima;

    private String calle;
    private Integer altura;
    private String codigoPostal;
    private String ciudad;
    private String provincia;
    private String pais;

    private BigDecimal precioPorNoche;
    private Integer metrosCuadrados;
    private Integer cantPersonas;
    private Integer piso;

    private String descripcion;
    private boolean disponibilidadInmediata;
    private Integer diasCancelacionSinPenalizacion;

    public Propiedad() {
    }

    public int getIdPropiedad() {
        return idPropiedad;
    }

    public void setIdPropiedad(int idPropiedad) {
        this.idPropiedad = idPropiedad;
    }

    public int getIdVendedorFk() {
        return idVendedorFk;
    }

    public void setIdVendedorFk(int idVendedorFk) {
        this.idVendedorFk = idVendedorFk;
    }

    public Integer getEstadiaMinima() {
        return estadiaMinima;
    }

    public void setEstadiaMinima(Integer estadiaMinima) {
        this.estadiaMinima = estadiaMinima;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public Integer getAltura() {
        return altura;
    }

    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public BigDecimal getPrecioPorNoche() {
        return precioPorNoche;
    }

    public void setPrecioPorNoche(BigDecimal precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
    }

    public Integer getMetrosCuadrados() {
        return metrosCuadrados;
    }

    public void setMetrosCuadrados(Integer metrosCuadrados) {
        this.metrosCuadrados = metrosCuadrados;
    }

    public Integer getCantPersonas() {
        return cantPersonas;
    }

    public void setCantPersonas(Integer cantPersonas) {
        this.cantPersonas = cantPersonas;
    }

    public Integer getPiso() {
        return piso;
    }

    public void setPiso(Integer piso) {
        this.piso = piso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isDisponibilidadInmediata() {
        return disponibilidadInmediata;
    }

    public void setDisponibilidadInmediata(boolean disponibilidadInmediata) {
        this.disponibilidadInmediata = disponibilidadInmediata;
    }

    public Integer getDiasCancelacionSinPenalizacion() {
        return diasCancelacionSinPenalizacion;
    }

    public void setDiasCancelacionSinPenalizacion(Integer dias) {
        this.diasCancelacionSinPenalizacion = dias;
    }
}
