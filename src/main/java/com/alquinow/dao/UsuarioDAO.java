package com.alquinow.dao;

import com.alquinow.modelo.Usuario;
import com.alquinow.util.Conexion;
import com.alquinow.util.Password;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Acceso a datos para Usuario, Comprador y Vendedor.
 * Maneja registro, verificación por correo, autenticación y edición de perfil.
 */
public class UsuarioDAO {

    private static volatile boolean esquemaVerificado = false;

    private void asegurarEsquema(Connection con) {
        if (esquemaVerificado) return;
        synchronized (UsuarioDAO.class) {
            if (esquemaVerificado) return;
            try (Statement st = con.createStatement()) {
                st.executeUpdate("ALTER TABLE Usuario ADD COLUMN foto_perfil VARCHAR(255) DEFAULT NULL");
            } catch (SQLException ignored) {
                // La columna ya existe
            }
            try (Statement st = con.createStatement()) {
                st.executeUpdate("ALTER TABLE Usuario ADD COLUMN es_admin TINYINT(1) DEFAULT 0");
            } catch (SQLException ignored) {
                // La columna ya existe
            }
            esquemaVerificado = true;
        }
    }

    public int registrar(Usuario u, String rol) throws SQLException {
        String sqlUsuario =
            "INSERT INTO Usuario (nombre, contrasena, dni, mail, tel, es_vendedor, es_huesped, cuenta_activa, codigo_verificacion, foto_perfil) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = Conexion.getConexion();
            asegurarEsquema(con);
            con.setAutoCommit(false); 

            int idGenerado;
            try (PreparedStatement ps = con.prepareStatement(
                    sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, u.getNombre());
                ps.setString(2, Password.hashear(u.getContrasena()));
                ps.setString(3, u.getDni());
                ps.setString(4, u.getMail());
                ps.setString(5, u.getTel());
                ps.setBoolean(6, "vendedor".equalsIgnoreCase(rol));
                ps.setBoolean(7, true); // Todos son huéspedes por defecto
                ps.setBoolean(8, false); // Nace inactiva hasta que verifique el código
                ps.setString(9, u.getCodigoVerificacion()); // Guardamos el código de 6 dígitos
                ps.setString(10, u.getFotoPerfil()); // Guardamos la foto de perfil (puede ser null)

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

            // TODOS son compradores por defecto (para poder guardar favoritos y alquilar)
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Comprador (ID_usuario) VALUES (?)")) {
                ps.setInt(1, idGenerado);
                ps.executeUpdate();
            }

            // SI ADEMÁS eligió ser vendedor, lo agregamos también a su respectiva tabla
            if ("vendedor".equalsIgnoreCase(rol)) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Vendedor (ID_usuario, verificado) VALUES (?, FALSE)")) {
                    ps.setInt(1, idGenerado);
                    ps.executeUpdate();
                }
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

    // Método para validar el código y activar la cuenta en MySQL
    public boolean verificarCodigo(String mail, String codigoIngresado) throws SQLException {
        String sqlSelect = "SELECT ID_usuario, codigo_verificacion FROM Usuario WHERE mail = ? AND cuenta_activa = 0";
        String sqlUpdate = "UPDATE Usuario SET cuenta_activa = 1, codigo_verificacion = NULL WHERE ID_usuario = ?";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int idUsuario = -1;
                String codigoBD = null;

                // 1. Buscamos el código asignado a ese correo
                try (PreparedStatement ps = con.prepareStatement(sqlSelect)) {
                    ps.setString(1, mail);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            idUsuario = rs.getInt("ID_usuario");
                            codigoBD = rs.getString("codigo_verificacion");
                        }
                    }
                }

                // 2. Comparamos el código ingresado con el de la base de datos
                if (idUsuario != -1 && codigoBD != null && codigoBD.equals(codigoIngresado)) {
                    // 3. Si coincide, activamos la cuenta
                    try (PreparedStatement psUp = con.prepareStatement(sqlUpdate)) {
                        psUp.setInt(1, idUsuario);
                        psUp.executeUpdate();
                    }
                    con.commit();
                    return true;
                }

                con.rollback();
                return false;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public Usuario autenticar(String mail, String passwordPlano) throws SQLException {
        String sql = "SELECT * FROM Usuario WHERE mail = ?";

        try (Connection con = Conexion.getConexion()) {
            asegurarEsquema(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, mail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Validamos que la cuenta esté activa antes de dejarlo entrar
                        if (!rs.getBoolean("cuenta_activa")) {
                            return null; // Cuenta pendiente de verificación
                        }
                        
                        String hash = rs.getString("contrasena");
                        if (Password.verificar(passwordPlano, hash)) {
                            return mapear(rs);
                        }
                    }
                }
            }
        }
        return null;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Usuario WHERE ID_usuario = ?";
        try (Connection con = Conexion.getConexion()) {
            asegurarEsquema(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapear(rs);
                    }
                }
            }
        }
        return null;
    }

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

    public boolean existeMailOtroUsuario(String mail, int idUsuarioActual) throws SQLException {
        String sql = "SELECT 1 FROM Usuario WHERE mail = ? AND ID_usuario <> ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mail);
            ps.setInt(2, idUsuarioActual);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean actualizarPerfil(Usuario u) throws SQLException {
        String sql = "UPDATE Usuario SET nombre = ?, tel = ?, dni = ?, mail = ?, foto_perfil = ? WHERE ID_usuario = ?";
        try (Connection con = Conexion.getConexion()) {
            asegurarEsquema(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getTel());
                ps.setString(3, u.getDni());
                ps.setString(4, u.getMail());
                ps.setString(5, u.getFotoPerfil());
                ps.setInt(6, u.getIdUsuario());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean actualizarContrasena(int idUsuario, String nuevaContrasenaPlana) throws SQLException {
        String sql = "UPDATE Usuario SET contrasena = ? WHERE ID_usuario = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Password.hashear(nuevaContrasenaPlana));
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean guardarCodigoRecuperacion(String mail, String codigo) throws SQLException {
        String sql = "UPDATE Usuario SET codigo_verificacion = ? WHERE mail = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, mail);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean validarCodigoRecuperacion(String mail, String codigoIngresado) throws SQLException {
        String sql = "SELECT codigo_verificacion FROM Usuario WHERE mail = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String codigoBD = rs.getString("codigo_verificacion");
                    return codigoBD != null && !codigoBD.isBlank() && codigoBD.equals(codigoIngresado);
                }
            }
        }
        return false;
    }

    public boolean restablecerContrasenaConCodigo(String mail, String codigoIngresado, String nuevaContrasenaPlana) throws SQLException {
        String sqlSelect = "SELECT ID_usuario, codigo_verificacion FROM Usuario WHERE mail = ?";
        String sqlUpdate = "UPDATE Usuario SET contrasena = ?, codigo_verificacion = NULL, cuenta_activa = 1 WHERE ID_usuario = ?";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int idUsuario = -1;
                String codigoBD = null;

                try (PreparedStatement ps = con.prepareStatement(sqlSelect)) {
                    ps.setString(1, mail);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            idUsuario = rs.getInt("ID_usuario");
                            codigoBD = rs.getString("codigo_verificacion");
                        }
                    }
                }

                if (idUsuario != -1 && codigoBD != null && !codigoBD.isBlank() && codigoBD.equals(codigoIngresado)) {
                    try (PreparedStatement psUp = con.prepareStatement(sqlUpdate)) {
                        psUp.setString(1, Password.hashear(nuevaContrasenaPlana));
                        psUp.setInt(2, idUsuario);
                        psUp.executeUpdate();
                    }
                    con.commit();
                    return true;
                }

                con.rollback();
                return false;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("ID_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setContrasena(rs.getString("contrasena"));
        u.setDni(rs.getString("dni"));
        u.setMail(rs.getString("mail"));
        u.setTel(rs.getString("tel"));
        u.setEsVendedor(rs.getBoolean("es_vendedor"));
        u.setEsHuesped(rs.getBoolean("es_huesped"));
        u.setCuentaActiva(rs.getBoolean("cuenta_activa"));
        u.setCodigoVerificacion(rs.getString("codigo_verificacion"));

        if (tieneColumna(rs, "foto_perfil")) {
            u.setFotoPerfil(rs.getString("foto_perfil"));
        }
        if (tieneColumna(rs, "es_admin")) {
            u.setEsAdmin(rs.getBoolean("es_admin"));
        }
        return u;
    }

    private boolean tieneColumna(ResultSet rs, String nombreColumna) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            if (nombreColumna.equalsIgnoreCase(meta.getColumnName(i)) || nombreColumna.equalsIgnoreCase(meta.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }
}
