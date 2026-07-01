package com.alquinow.dao;

import com.alquinow.modelo.Reserva;
import com.alquinow.util.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Reserva.
 */
public class ReservaDAO {

    /** Crea una reserva nueva y devuelve su ID generado. */
    public int crear(Reserva r) throws SQLException {
        String sql =
            "INSERT INTO Reserva "
            + "(ID_comprador_fk, ID_propiedad_fk, fecha_inicio, fecha_final, "
            + " estado, monto_total, fecha_reserva, dias_cancelacion_aplicados, "
            + " fecha_limite_cancelacion) "
            + "VALUES (?, ?, ?, ?, ?, ?, CURDATE(), ?, ?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getIdCompradorFk());
            ps.setInt(2, r.getIdPropiedadFk());
            ps.setDate(3, r.getFechaInicio());
            ps.setDate(4, r.getFechaFinal());
            ps.setString(5, r.getEstado() == null ? "pendiente" : r.getEstado());
            ps.setBigDecimal(6, r.getMontoTotal());
            if (r.getDiasCancelacionAplicados() == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, r.getDiasCancelacionAplicados());
            }
            ps.setDate(8, r.getFechaLimiteCancelacion());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /** Lista las reservas de un comprador, con la ciudad de la propiedad. */
    public List<Reserva> listarPorComprador(int idComprador) throws SQLException {
        String sql =
            "SELECT r.*, p.ciudad AS ciudad_prop "
            + "FROM Reserva r "
            + "JOIN Propiedad p ON r.ID_propiedad_fk = p.ID_propiedad "
            + "WHERE r.ID_comprador_fk = ? "
            + "ORDER BY r.fecha_reserva DESC";

        List<Reserva> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idComprador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva r = mapear(rs);
                    r.setCiudadPropiedad(rs.getString("ciudad_prop"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    /** Cambia el estado de una reserva (ej: "cancelada", "confirmada"). */
    public boolean actualizarEstado(int idReserva, String nuevoEstado)
            throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Reserva SET estado = ? WHERE ID_reserva = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idReserva);
            return ps.executeUpdate() > 0;
        }
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
        return r;
    }
}
