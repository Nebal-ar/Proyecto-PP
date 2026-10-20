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

public class ReservaDAO {

    public int crear(Reserva r) throws SQLException {
        String sqlReserva
                = "INSERT INTO Reserva "
                + "(ID_comprador_fk, ID_propiedad_fk, fecha_inicio, fecha_final, "
                + " estado, monto_total, fecha_reserva, dias_cancelacion_aplicados, "
                + " fecha_limite_cancelacion, fecha_limite_pago, monto_pagado, porcentaje_sena_aplicado, metodo_pago) "
                + "VALUES (?, ?, ?, ?, ?, ?, CURDATE(), ?, ?, ?, ?, ?, ?)";

        String sqlDisponibilidad = "INSERT INTO Disponibilidad (ID_propiedad_fk, fecha, estado) VALUES (?, ?, 'Ocupado')";

        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); 
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
                ps.setBigDecimal(10, r.getMontoPagado() == null ? BigDecimal.ZERO : r.getMontoPagado());
                ps.setInt(11, r.getPorcentajeSenaAplicado());
                ps.setString(12, r.getMetodoPago());

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
            if (con != null) con.rollback(); 
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    public List<Reserva> listarPorComprador(int idComprador) throws SQLException {
        String sql = "SELECT r.*, p.ciudad AS ciudad_prop, p.calle AS calle_prop, p.altura AS altura_prop "
                   + "FROM Reserva r JOIN Propiedad p ON r.ID_propiedad_fk = p.ID_propiedad "
                   + "WHERE r.ID_comprador_fk = ? ORDER BY r.fecha_reserva DESC";

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

    // NUEVO: Trae las reservas que le hicieron al vendedor para que pueda aprobar los pagos
    public List<Reserva> listarPorVendedor(int idVendedor) throws SQLException {
        String sql = "SELECT r.*, p.ciudad AS ciudad_prop, p.calle AS calle_prop, p.altura AS altura_prop, u.mail "
                   + "FROM Reserva r "
                   + "JOIN Propiedad p ON r.ID_propiedad_fk = p.ID_propiedad "
                   + "JOIN usuario u ON r.ID_comprador_fk = u.ID_usuario "
                   + "WHERE p.ID_vendedor_fk = ? ORDER BY r.fecha_reserva DESC";

        List<Reserva> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVendedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva r = mapear(rs);
                    r.setCiudad(rs.getString("ciudad_prop"));
                    r.setCalle(rs.getString("calle_prop"));
                    r.setAltura(rs.getInt("altura_prop"));
                    r.setCorreoComprador(rs.getString("mail")); // Traemos el mail del que reservó
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    public boolean actualizarEstado(int idReserva, String nuevoEstado) throws SQLException {
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Reserva SET estado = ? WHERE ID_reserva = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idReserva);
            return ps.executeUpdate() > 0;
        }
    }
    
    // NUEVO: Guarda el ticket que sube el huésped
    public boolean guardarComprobante(int idReserva, String nombreArchivo) throws SQLException {
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Reserva SET comprobante_pago = ?, estado = 'verificando_pago' WHERE ID_reserva = ?")) {
            ps.setString(1, nombreArchivo);
            ps.setInt(2, idReserva);
            return ps.executeUpdate() > 0;
        }
    }

    public Reserva buscarPorId(int idReserva) {
        Reserva reserva = null;
        String sql = "SELECT * FROM Reserva WHERE ID_reserva = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) reserva = mapear(rs); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reserva;
    }
    
    public boolean cancelarReserva(int idReserva) throws SQLException {
        Reserva reserva = buscarPorId(idReserva);
        if (reserva == null) return false;

        String estadoActual = reserva.getEstado();
        if (estadoActual.startsWith("cancelada") || estadoActual.equals("finalizada")) return false;

        String nuevoEstado;
        boolean senaPagada = reserva.getMontoPagado() != null && reserva.getMontoPagado().compareTo(BigDecimal.ZERO) > 0;

        if (!senaPagada) {
            nuevoEstado = "cancelada";
        } else {
            LocalDate hoy = LocalDate.now();
            LocalDate limiteCancelacionGratuita = reserva.getFechaLimiteCancelacion().toLocalDate();
            if (!hoy.isAfter(limiteCancelacionGratuita)) {
                nuevoEstado = "cancelada_reembolsada";
            } else {
                nuevoEstado = "cancelada_penalizada";
            }
        }

        boolean actualizado = actualizarEstado(idReserva, nuevoEstado);
        if (actualizado) {
            String sqlLiberar = "DELETE FROM Disponibilidad WHERE ID_propiedad_fk = ? AND fecha >= ? AND fecha < ?";
            try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sqlLiberar)) {
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
        String sql = "SELECT r.*, u.mail FROM reserva r JOIN Usuario u ON r.id_comprador_fk = u.ID_usuario WHERE r.fecha_final = CURDATE()";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reserva r = mapear(rs);
                r.setCorreoComprador(rs.getString("mail"));
                reservas.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return reservas;
    }

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
        
        java.sql.Timestamp limitePago = rs.getTimestamp("fecha_limite_pago");
        if (limitePago != null) r.setFechaLimitePago(limitePago);
        
        r.setMontoPagado(rs.getBigDecimal("monto_pagado"));
        r.setPorcentajeSenaAplicado(rs.getInt("porcentaje_sena_aplicado"));
        
        try { r.setMetodoPago(rs.getString("metodo_pago")); } catch (SQLException e) {}
        try { r.setComprobantePago(rs.getString("comprobante_pago")); } catch (SQLException e) {}
        
        return r;
    }
}