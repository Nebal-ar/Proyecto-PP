package com.alquinow.modelo;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Representa una fila de la tabla Reserva.
 */
public class Reserva {

    private int idReserva;
    private int idCompradorFk;
    private int idPropiedadFk;

    private Date fechaInicio;
    private Date fechaFinal;
    private String estado;
    private BigDecimal montoTotal;
    private Date fechaReserva;
    private Integer diasCancelacionAplicados;
    private Date fechaLimiteCancelacion;

    // Campo auxiliar para mostrar info de la propiedad en listados (no es columna)
    private String ciudadPropiedad;

    public Reserva() {
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public int getIdCompradorFk() {
        return idCompradorFk;
    }

    public void setIdCompradorFk(int idCompradorFk) {
        this.idCompradorFk = idCompradorFk;
    }

    public int getIdPropiedadFk() {
        return idPropiedadFk;
    }

    public void setIdPropiedadFk(int idPropiedadFk) {
        this.idPropiedadFk = idPropiedadFk;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public Date getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(Date fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public Integer getDiasCancelacionAplicados() {
        return diasCancelacionAplicados;
    }

    public void setDiasCancelacionAplicados(Integer dias) {
        this.diasCancelacionAplicados = dias;
    }

    public Date getFechaLimiteCancelacion() {
        return fechaLimiteCancelacion;
    }

    public void setFechaLimiteCancelacion(Date fecha) {
        this.fechaLimiteCancelacion = fecha;
    }

    public String getCiudadPropiedad() {
        return ciudadPropiedad;
    }

    public void setCiudadPropiedad(String ciudadPropiedad) {
        this.ciudadPropiedad = ciudadPropiedad;
    }
}
