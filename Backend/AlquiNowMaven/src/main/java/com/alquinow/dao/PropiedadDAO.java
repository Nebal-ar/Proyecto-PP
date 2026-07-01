package com.alquinow.dao;

import com.alquinow.modelo.Propiedad;
import com.alquinow.util.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Propiedad: alta, baja, modificación,
 * listado y búsqueda con filtros.
 */
public class PropiedadDAO {

    /** Inserta una propiedad nueva y devuelve su ID generado. */
    public int crear(Propiedad p) throws SQLException {
        String sql =
            "INSERT INTO Propiedad "
            + "(ID_vendedor_fk, estadia_minima, calle, altura, codigo_postal, "
            + " ciudad, provincia, pais, precio_por_noche, metros_cuadrados, "
            + " cant_personas, piso, descripcion, disponibilidad_inmediata, "
            + " dias_cancelacion_sin_penalizacion) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getIdVendedorFk());
            setIntOrNull(ps, 2, p.getEstadiaMinima());
            ps.setString(3, p.getCalle());
            setIntOrNull(ps, 4, p.getAltura());
            ps.setString(5, p.getCodigoPostal());
            ps.setString(6, p.getCiudad());
            ps.setString(7, p.getProvincia());
            ps.setString(8, p.getPais() == null ? "Argentina" : p.getPais());
            ps.setBigDecimal(9, p.getPrecioPorNoche());
            setIntOrNull(ps, 10, p.getMetrosCuadrados());
            setIntOrNull(ps, 11, p.getCantPersonas());
            setIntOrNull(ps, 12, p.getPiso());
            ps.setString(13, p.getDescripcion());
            ps.setBoolean(14, p.isDisponibilidadInmediata());
            setIntOrNull(ps, 15, p.getDiasCancelacionSinPenalizacion());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /** Lista todas las propiedades. */
    public List<Propiedad> listarTodas() throws SQLException {
        return ejecutarConsulta("SELECT * FROM Propiedad ORDER BY ID_propiedad DESC",
                new Object[]{});
    }

    /** Lista las propiedades de un vendedor concreto. */
    public List<Propiedad> listarPorVendedor(int idVendedor) throws SQLException {
        return ejecutarConsulta(
                "SELECT * FROM Propiedad WHERE ID_vendedor_fk = ? ORDER BY ID_propiedad DESC",
                new Object[]{idVendedor});
    }

    /** Busca una propiedad por su ID. */
    public Propiedad buscarPorId(int id) throws SQLException {
        List<Propiedad> lista = ejecutarConsulta(
                "SELECT * FROM Propiedad WHERE ID_propiedad = ?",
                new Object[]{id});
        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Búsqueda con filtros opcionales. Cualquier parámetro puede venir null
     * (o <= 0 para los numéricos) y simplemente no se aplica.
     */
    public List<Propiedad> buscar(String ciudad, String provincia,
            Integer precioMax, Integer personasMin) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM Propiedad WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (ciudad != null && !ciudad.isBlank()) {
            sql.append(" AND ciudad LIKE ?");
            params.add("%" + ciudad + "%");
        }
        if (provincia != null && !provincia.isBlank()) {
            sql.append(" AND provincia = ?");
            params.add(provincia);
        }
        if (precioMax != null && precioMax > 0) {
            sql.append(" AND precio_por_noche <= ?");
            params.add(precioMax);
        }
        if (personasMin != null && personasMin > 0) {
            sql.append(" AND cant_personas >= ?");
            params.add(personasMin);
        }
        sql.append(" ORDER BY precio_por_noche ASC");

        return ejecutarConsulta(sql.toString(), params.toArray());
    }

    /** Actualiza una propiedad existente. */
    public boolean actualizar(Propiedad p) throws SQLException {
        String sql =
            "UPDATE Propiedad SET estadia_minima=?, calle=?, altura=?, "
            + "codigo_postal=?, ciudad=?, provincia=?, pais=?, precio_por_noche=?, "
            + "metros_cuadrados=?, cant_personas=?, piso=?, descripcion=?, "
            + "disponibilidad_inmediata=?, dias_cancelacion_sin_penalizacion=? "
            + "WHERE ID_propiedad=?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setIntOrNull(ps, 1, p.getEstadiaMinima());
            ps.setString(2, p.getCalle());
            setIntOrNull(ps, 3, p.getAltura());
            ps.setString(4, p.getCodigoPostal());
            ps.setString(5, p.getCiudad());
            ps.setString(6, p.getProvincia());
            ps.setString(7, p.getPais());
            ps.setBigDecimal(8, p.getPrecioPorNoche());
            setIntOrNull(ps, 9, p.getMetrosCuadrados());
            setIntOrNull(ps, 10, p.getCantPersonas());
            setIntOrNull(ps, 11, p.getPiso());
            ps.setString(12, p.getDescripcion());
            ps.setBoolean(13, p.isDisponibilidadInmediata());
            setIntOrNull(ps, 14, p.getDiasCancelacionSinPenalizacion());
            ps.setInt(15, p.getIdPropiedad());

            return ps.executeUpdate() > 0;
        }
    }

    /** Elimina una propiedad por ID. */
    public boolean eliminar(int id) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM Propiedad WHERE ID_propiedad = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Helpers privados ----------

    /** Ejecuta una consulta SELECT con parámetros y mapea el resultado. */
    private List<Propiedad> ejecutarConsulta(String sql, Object[] params)
            throws SQLException {
        List<Propiedad> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Setea un Integer que puede ser null en un PreparedStatement. */
    private void setIntOrNull(PreparedStatement ps, int idx, Integer valor)
            throws SQLException {
        if (valor == null) {
            ps.setNull(idx, java.sql.Types.INTEGER);
        } else {
            ps.setInt(idx, valor);
        }
    }

    /** Convierte una fila en objeto Propiedad. */
    private Propiedad mapear(ResultSet rs) throws SQLException {
        Propiedad p = new Propiedad();
        p.setIdPropiedad(rs.getInt("ID_propiedad"));
        p.setIdVendedorFk(rs.getInt("ID_vendedor_fk"));
        p.setEstadiaMinima(rs.getInt("estadia_minima"));
        p.setCalle(rs.getString("calle"));
        p.setAltura(rs.getInt("altura"));
        p.setCodigoPostal(rs.getString("codigo_postal"));
        p.setCiudad(rs.getString("ciudad"));
        p.setProvincia(rs.getString("provincia"));
        p.setPais(rs.getString("pais"));
        p.setPrecioPorNoche(rs.getBigDecimal("precio_por_noche"));
        p.setMetrosCuadrados(rs.getInt("metros_cuadrados"));
        p.setCantPersonas(rs.getInt("cant_personas"));
        p.setPiso(rs.getInt("piso"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setDisponibilidadInmediata(rs.getBoolean("disponibilidad_inmediata"));
        p.setDiasCancelacionSinPenalizacion(
                rs.getInt("dias_cancelacion_sin_penalizacion"));
        return p;
    }
}
