package com.alquinow.dao;

import com.alquinow.modelo.Reserva;
import com.alquinow.util.Conexion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Reserva.
 */
public class ReservaDAO {

    /**
     * Crea una reserva nueva, bloquea los días en Disponibilidad y devuelve su
     * ID generado.
     */
    public int crear(Reserva r) throws SQLException {
        // MODIFICACIÓN: Agregamos monto_pagado y porcentaje_sena_aplicado
        String sqlReserva
                = "INSERT INTO Reserva "
                + "(ID_comprador_fk, ID_propiedad_fk, fecha_inicio, fecha_final, "
                + " estado, monto_total, fecha_reserva, dias_cancelacion_aplicados, "
                + " fecha_limite_cancelacion, fecha_limite_pago, monto_pagado, porcentaje_sena_aplicado) "
                + "VALUES (?, ?, ?, ?, ?, ?, CURDATE(), ?, ?, ?, ?, ?)";

        String sqlDisponibilidad
                = "INSERT INTO Disponibilidad (ID_propiedad_fk, fecha, estado) VALUES (?, ?, 'Ocupado')";

        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // Arranca la transacción

            int idGenerado = -1;

            try (PreparedStatement ps = con.prepareStatement(sqlReserva, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, r.getIdCompradorFk());
                ps.setInt(2, r.getIdPropiedadFk());
                ps.setDate(3, r.getFechaInicio());
                ps.setDate(4, r.getFechaFinal());
                ps.setString(5, r.getEstado() == null ? "pendiente_sena" : r.getEstado()); 
                ps.setBigDecimal(6, r.getMontoTotal());

                if (r.getDiasCancelacionAplicados() == null) {
                    ps.setNull(7, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(7, r.getDiasCancelacionAplicados());
                }
                ps.setDate(8, r.getFechaLimiteCancelacion());
                ps.setTimestamp(9, r.getFechaLimitePago());
                
                // Nuevos campos financieros
                ps.setBigDecimal(10, r.getMontoPagado() == null ? BigDecimal.ZERO : r.getMontoPagado());
                ps.setInt(11, r.getPorcentajeSenaAplicado());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    } else {
                        con.rollback();
                        return -1;
                    }
                }
            }

            // Insertar las fechas bloqueadas en la tabla Disponibilidad
            try (PreparedStatement psDisp = con.prepareStatement(sqlDisponibilidad)) {
                LocalDate inicio = r.getFechaInicio().toLocalDate();
                LocalDate fin = r.getFechaFinal().toLocalDate();

                for (LocalDate fecha = inicio; fecha.isBefore(fin); fecha = fecha.plusDays(1)) {
                    psDisp.setInt(1, r.getIdPropiedadFk());
                    psDisp.setDate(2, java.sql.Date.valueOf(fecha));
                    psDisp.addBatch(); 
                }
                psDisp.executeBatch(); 
            }

            con.commit(); 
            return idGenerado;

        } catch (SQLException e) {
            if (con != null) {
                con.rollback(); 
            }
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    /**
     * Lista las reservas de un comprador, con la dirección exacta de la propiedad.
     */
    public List<Reserva> listarPorComprador(int idComprador) throws SQLException {
        String sql
                = "SELECT r.*, p.ciudad AS ciudad_prop, p.calle AS calle_prop, p.altura AS altura_prop "
                + "FROM Reserva r "
                + "JOIN Propiedad p ON r.ID_propiedad_fk = p.ID_propiedad "
                + "WHERE r.ID_comprador_fk = ? "
                + "ORDER BY r.fecha_reserva DESC";

        List<Reserva> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idComprador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva r = mapear(rs);
                    r.setCiudad(rs.getString("ciudad_prop"));
                    r.setCalle(rs.getString("calle_prop"));
                    r.setAltura(rs.getInt("altura_prop"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    /**
     * Cambia el estado de una reserva sin validación extra.
     */
    public boolean actualizarEstado(int idReserva, String nuevoEstado)
            throws SQLException {
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Reserva SET estado = ? WHERE ID_reserva = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idReserva);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca una reserva por ID con TODOS sus datos completos.
     */
    public Reserva buscarPorId(int idReserva) {
        Reserva reserva = null;
        String sql = "SELECT * FROM Reserva WHERE ID_reserva = ?";

        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reserva = mapear(rs); // Mapeamos TODOS los datos, incluyendo financieros
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reserva;
    }
    
    /**
     * NÚCLEO DE NEGOCIO: Cancela la reserva aplicando lógica de reembolso y libera el calendario.
     */
    public boolean cancelarReserva(int idReserva) throws SQLException {
        Reserva reserva = buscarPorId(idReserva);
        if (reserva == null) return false;

        String estadoActual = reserva.getEstado();
        
        // Si ya estaba cancelada o finalizada, no permitimos cambiarla
        if (estadoActual.startsWith("cancelada") || estadoActual.equals("finalizada")) {
            return false;
        }

        String nuevoEstado;
        
        // Verificamos si ya puso plata real (si el estado es distinto a pendiente_sena y el monto es > 0)
        boolean senaPagada = reserva.getMontoPagado() != null && reserva.getMontoPagado().compareTo(BigDecimal.ZERO) > 0;

        if (!senaPagada) {
            // Caso 1: Canceló antes de pagar un peso
            nuevoEstado = "cancelada";
        } else {
            // Caso 2 y 3: Ya pagó. Hay que calcular los días cruzando la fecha de hoy.
            LocalDate hoy = LocalDate.now();
            LocalDate limiteCancelacionGratuita = reserva.getFechaLimiteCancelacion().toLocalDate();

            // isAfter() verifica si el día actual ya pasó el límite establecido
            if (!hoy.isAfter(limiteCancelacionGratuita)) {
                // Canceló a tiempo -> Corresponde devolverle el dinero
                nuevoEstado = "cancelada_reembolsada";
            } else {
                // Canceló tarde -> Pierde la seña
                nuevoEstado = "cancelada_penalizada";
            }
        }

        // 1. Cambiamos el estado en la base de datos
        boolean actualizado = actualizarEstado(idReserva, nuevoEstado);

        // 2. Si se canceló correctamente, ¡Liberamos el calendario para que otro pueda alquilar!
        if (actualizado) {
            String sqlLiberar = "DELETE FROM Disponibilidad WHERE ID_propiedad_fk = ? AND fecha >= ? AND fecha < ?";
            try (Connection con = Conexion.getConexion();
                 PreparedStatement ps = con.prepareStatement(sqlLiberar)) {
                ps.setInt(1, reserva.getIdPropiedadFk());
                ps.setDate(2, reserva.getFechaInicio());
                ps.setDate(3, reserva.getFechaFinal());
                ps.executeUpdate();
            }
        }

        return actualizado;
    }

    public List<Reserva> obtenerReservasFinalizadasHoy() {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT r.*, u.mail "
                + "FROM reserva r "
                + "JOIN Usuario u ON r.id_comprador_fk = u.ID_usuario "
                + "WHERE r.fecha_final = CURDATE()";

        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reserva r = mapear(rs);
                r.setCorreoComprador(rs.getString("mail"));
                reservas.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservas;
    }

    /**
     * Helper centralizado para transformar ResultSet en objeto Reserva.
     */
    private Reserva mapear(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("ID_reserva"));
        r.setIdCompradorFk(rs.getInt("ID_comprador_fk"));
        r.setIdPropiedadFk(rs.getInt("ID_propiedad_fk"));
        r.setFechaInicio(rs.getDate("fecha_inicio"));
        r.setFechaFinal(rs.getDate("fecha_final"));
        r.setEstado(rs.getString("estado"));
        r.setMontoTotal(rs.getBigDecimal("monto_total"));
        r.setFechaReserva(rs.getDate("fecha_reserva"));
        r.setDiasCancelacionAplicados(rs.getInt("dias_cancelacion_aplicados"));
        r.setFechaLimiteCancelacion(rs.getDate("fecha_limite_cancelacion"));
        
        // Evitamos errores si la consulta (ej. reservas viejas) trae NULL en la hora límite
        java.sql.Timestamp limitePago = rs.getTimestamp("fecha_limite_pago");
        if (limitePago != null) {
            r.setFechaLimitePago(limitePago);
        }
        
        // Mapeo financiero
        r.setMontoPagado(rs.getBigDecimal("monto_pagado"));
        r.setPorcentajeSenaAplicado(rs.getInt("porcentaje_sena_aplicado"));
        
        return r;
    }
}
