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
    private Double promedioEstrellas;

    private BigDecimal precioPorNoche;
    private Integer metrosCuadrados;
    private Integer cantPersonas;
    private String piso;

    private String descripcion;
    private Integer diasCancelacionSinPenalizacion;
    
    // --- NUEVAS VARIABLES: SEGURIDAD Y VERIFICACIÓN ANTI-FRAUDE ---
    private String estadoVerificacion;
    private String comprobanteTitularidad;
    private String fotoVerificacion;
    private String motivoRechazo;

    // Configuraciones para la seña
    private int porcentajeSena;
    private int horasLimitePago;

    public Propiedad() {
    }

    public int getIdPropiedad() { return idPropiedad; }
    public void setIdPropiedad(int idPropiedad) { this.idPropiedad = idPropiedad; }

    public int getIdVendedorFk() { return idVendedorFk; }
    public void setIdVendedorFk(int idVendedorFk) { this.idVendedorFk = idVendedorFk; }

    public Integer getEstadiaMinima() { return estadiaMinima; }
    public void setEstadiaMinima(Integer estadiaMinima) { this.estadiaMinima = estadiaMinima; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public Integer getAltura() { return altura; }
    public void setAltura(Integer altura) { this.altura = altura; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    public Integer getMetrosCuadrados() { return metrosCuadrados; }
    public void setMetrosCuadrados(Integer metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    public Integer getCantPersonas() { return cantPersonas; }
    public void setCantPersonas(Integer cantPersonas) { this.cantPersonas = cantPersonas; }

    public String getPiso() { return piso; }
    public void setPiso(String piso) { this.piso = piso; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getDiasCancelacionSinPenalizacion() { return diasCancelacionSinPenalizacion; }
    public void setDiasCancelacionSinPenalizacion(Integer dias) { this.diasCancelacionSinPenalizacion = dias; }

    public int getPorcentajeSena() { return porcentajeSena; }
    public void setPorcentajeSena(int porcentajeSena) { this.porcentajeSena = porcentajeSena; }

    public int getHorasLimitePago() { return horasLimitePago; }
    public void setHorasLimitePago(int horasLimitePago) { this.horasLimitePago = horasLimitePago; }

    public Double getPromedioEstrellas() { return promedioEstrellas; }
    public void setPromedioEstrellas(Double promedioEstrellas) { this.promedioEstrellas = promedioEstrellas; }

    // --- GETTERS Y SETTERS: VERIFICACIÓN ---
    public String getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(String estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }

    public String getComprobanteTitularidad() { return comprobanteTitularidad; }
    public void setComprobanteTitularidad(String comprobanteTitularidad) { this.comprobanteTitularidad = comprobanteTitularidad; }

    public String getFotoVerificacion() { return fotoVerificacion; }
    public void setFotoVerificacion(String fotoVerificacion) { this.fotoVerificacion = fotoVerificacion; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}