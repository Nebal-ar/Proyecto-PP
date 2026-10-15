package com.alquinow.dao;

import com.alquinow.modelo.Resena;
import com.alquinow.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ResenaDAO {

    public boolean insertar(Resena resena) {
        String sql = "INSERT INTO resena (ID_propiedad_fk, ID_comprador_fk, id_reserva_fk, puntuacion, comentario) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, resena.getIdPropiedad());
            ps.setInt(2, resena.getIdUsuario());
            ps.setInt(3, resena.getIdReservaFk()); 
            ps.setInt(4, resena.getCalificacion());
            ps.setString(5, resena.getComentario());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Resena> obtenerPorPropiedad(int idPropiedad) {
        List<Resena> resenas = new ArrayList<>();
        
        // Unimos directamente resena con usuario usando los nombres exactos de tu BD
        String sql = "SELECT r.*, u.mail "
                   + "FROM resena r "
                   + "LEFT JOIN usuario u ON r.ID_comprador_fk = u.ID_usuario "
                   + "WHERE r.ID_propiedad_fk = ? "
                   + "ORDER BY r.ID_resena DESC";
                   
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPropiedad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Resena r = new Resena();
                    r.setId(rs.getInt("ID_resena"));
                    r.setIdPropiedad(rs.getInt("ID_propiedad_fk"));
                    r.setIdUsuario(rs.getInt("ID_comprador_fk"));
                    r.setIdReservaFk(rs.getInt("id_reserva_fk"));
                    r.setCalificacion(rs.getInt("puntuacion"));
                    r.setComentario(rs.getString("comentario"));
                    
                    try {
                        Timestamp ts = rs.getTimestamp("fecha");
                        r.setFecha(ts != null ? ts : new Timestamp(System.currentTimeMillis()));
                    } catch (SQLException ex) {
                        r.setFecha(new Timestamp(System.currentTimeMillis()));
                    }
                    
                    String mail = rs.getString("mail");
                    String nombreMostrado = (mail != null && mail.contains("@")) ? mail.split("@")[0] : "Huésped verificado";
                    r.setNombreUsuario(nombreMostrado);
                    
                    resenas.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resenas;
    }
    
    // Chequea si ya se reseñó esa estadía específica
    public boolean existeResenaParaReserva(int idReserva) {
        String sql = "SELECT 1 FROM resena WHERE id_reserva_fk = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Chequea si el mismo usuario ya reseñó esta propiedad antes
    public boolean existeResenaDeUsuarioEnPropiedad(int idUsuario, int idPropiedad) {
        String sql = "SELECT 1 FROM resena WHERE ID_comprador_fk = ? AND ID_propiedad_fk = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idUsuario);
            ps.setInt(2, idPropiedad);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}