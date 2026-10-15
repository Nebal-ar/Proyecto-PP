package com.alquinow.servlet;

import com.alquinow.dao.ResenaDAO;
import com.alquinow.dao.ReservaDAO; // Importación nueva
import com.alquinow.modelo.Resena;
import com.alquinow.modelo.Reserva; // Importación nueva
import com.alquinow.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@WebServlet("/resenas")
public class ResenaServlet extends HttpServlet {

    private final ResenaDAO resenaDAO = new ResenaDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO(); // Instanciamos el DAO para buscar las fechas

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Obtener reseñas de una propiedad específica
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String idPropiedadStr = req.getParameter("idPropiedad");
        if (idPropiedadStr != null) {
            int idPropiedad = Integer.parseInt(idPropiedadStr);
            List<Resena> resenas = resenaDAO.obtenerPorPropiedad(idPropiedad);
            
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < resenas.size(); i++) {
                Resena r = resenas.get(i);
                json.append("{")
                    .append("\"id\":").append(r.getId()).append(",")
                    .append("\"nombreUsuario\":\"").append(escape(r.getNombreUsuario())).append("\",")
                    .append("\"calificacion\":").append(r.getCalificacion()).append(",")
                    .append("\"comentario\":\"").append(escape(r.getComentario())).append("\",")
                    .append("\"fecha\":\"").append(r.getFecha().toString()).append("\"")
                    .append("}");
                if (i < resenas.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            
            try (PrintWriter out = resp.getWriter()) {
                out.print(json.toString());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Debes iniciar sesión para dejar una reseña\"}");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        String idPropiedadStr = req.getParameter("idPropiedad");
        String idReservaStr = req.getParameter("idReserva"); // Capturamos el ID del viaje
        String calificacionStr = req.getParameter("calificacion");
        String comentario = req.getParameter("comentario");

        try {
            int idPropiedad = Integer.parseInt(idPropiedadStr);
            int idReserva = Integer.parseInt(idReservaStr);
            int calificacion = Integer.parseInt(calificacionStr);
            
            if (calificacion < 1 || calificacion > 5) {
                out.print("{\"error\":\"La calificación debe estar entre 1 y 5\"}");
                return;
            }

           // 1. Barrera Anti-Spam: ¿Ya hay una reseña para esta reserva o de este usuario en esta propiedad?
            if (resenaDAO.existeResenaParaReserva(idReserva) || resenaDAO.existeResenaDeUsuarioEnPropiedad(usuario.getIdUsuario(), idPropiedad)) {
                out.print("{\"error\":\"Ya publicaste una reseña para esta propiedad anteriormente.\"}");
                return;
            }

            // 2. Barrera de Tiempo: Traemos los datos de la reserva
            // ATENCIÓN: Ajustá "buscarPorId" y "getFechaFin" si los llamaste distinto en tu proyecto
            Reserva reserva = reservaDAO.buscarPorId(idReserva);
            if (reserva == null) {
                out.print("{\"error\":\"No se encontró la reserva indicada.\"}");
                return;
            }

            LocalDate fechaSalida = reserva.getFechaFinal().toLocalDate();
            LocalDate hoy = LocalDate.now();
            long diasDesdeSalida = ChronoUnit.DAYS.between(fechaSalida, hoy);

            if (diasDesdeSalida < 0) {
                out.print("{\"error\":\"No podés reseñar una estadía que todavía no terminó.\"}");
                return;
            } else if (diasDesdeSalida > 60) {
                out.print("{\"error\":\"El plazo máximo de 60 días para reseñar ya expiró.\"}");
                return;
            }

            // 3. Si pasó todos los controles, ensamblamos y guardamos
            Resena resena = new Resena();
            resena.setIdPropiedad(idPropiedad);
            resena.setIdUsuario(usuario.getIdUsuario());
            resena.setIdReservaFk(idReserva); // Atamos la reseña a este viaje específico
            resena.setCalificacion(calificacion);
            resena.setComentario(comentario);

            if (resenaDAO.insertar(resena)) {
                out.print("{\"ok\":true, \"mensaje\":\"Reseña publicada con éxito\"}");
            } else {
                out.print("{\"error\":\"No se pudo publicar la reseña\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"error\":\"Datos inválidos\"}");
        }
    }

    // Helper simple para escapar comillas en JSON
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}