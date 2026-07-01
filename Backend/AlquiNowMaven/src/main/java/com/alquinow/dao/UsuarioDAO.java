package com.alquinow.dao;

import com.alquinow.modelo.Usuario;
import com.alquinow.util.Conexion;
import com.alquinow.util.Password;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Acceso a datos para Usuario, Comprador y Vendedor.
 * Maneja registro (con hash de contraseña) y autenticación.
 */
public class UsuarioDAO {

    /**
     * Registra un usuario y, según el rol, crea su fila en Comprador o Vendedor.
     * Usa una transacción: si algo falla, se deshace todo.
     *
     * @return el ID generado, o -1 si falló.
     */
    public int registrar(Usuario u, String rol) throws SQLException {
        String sqlUsuario =
            "INSERT INTO Usuario (contrasena, dni, mail, tel) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // arranca la transacción

            int idGenerado;
            try (PreparedStatement ps = con.prepareStatement(
                    sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                // Hasheamos la contraseña ANTES de guardarla
                ps.setString(1, Password.hashear(u.getContrasena()));
                ps.setString(2, u.getDni());
                ps.setString(3, u.getMail());
                ps.setString(4, u.getTel());
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

            // Creamos la fila en la tabla del rol correspondiente
            if ("vendedor".equalsIgnoreCase(rol)) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Vendedor (ID_usuario, verificado) VALUES (?, FALSE)")) {
                    ps.setInt(1, idGenerado);
                    ps.executeUpdate();
                }
            } else { // por defecto, comprador
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Comprador (ID_usuario) VALUES (?)")) {
                    ps.setInt(1, idGenerado);
                    ps.executeUpdate();
                }
            }

            con.commit(); // confirmamos todo
            return idGenerado;

        } catch (SQLException e) {
            if (con != null) {
                con.rollback(); // si algo falló, deshacemos
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
     * Verifica las credenciales. Si el mail existe y la contraseña coincide
     * con el hash guardado, devuelve el Usuario (con su rol). Si no, null.
     */
    public Usuario autenticar(String mail, String passwordPlano) throws SQLException {
        String sql = "SELECT * FROM Usuario WHERE mail = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("contrasena");
                    // Comparamos texto plano contra hash con BCrypt
                    if (Password.verificar(passwordPlano, hash)) {
                        Usuario u = mapear(rs);
                        u.setRol(detectarRol(con, u.getIdUsuario()));
                        return u;
                    }
                }
            }
        }
        return null; // mail inexistente o contraseña incorrecta
    }

    /** Determina si el usuario es comprador o vendedor consultando las tablas. */
    private String detectarRol(Connection con, int idUsuario) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM Vendedor WHERE ID_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "vendedor";
                }
            }
        }
        return "comprador";
    }

    /** Busca un usuario por ID. */
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Usuario WHERE ID_usuario = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = mapear(rs);
                    u.setRol(detectarRol(con, id));
                    return u;
                }
            }
        }
        return null;
    }

    /** Verifica si un mail ya está registrado. */
    public boolean existeMail(String mail) throws SQLException {
        String sql = "SELECT 1 FROM Usuario WHERE mail = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mail);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Convierte una fila del ResultSet en un objeto Usuario. */
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("ID_usuario"));
        u.setContrasena(rs.getString("contrasena"));
        u.setDni(rs.getString("dni"));
        u.setMail(rs.getString("mail"));
        u.setTel(rs.getString("tel"));
        return u;
    }
}
