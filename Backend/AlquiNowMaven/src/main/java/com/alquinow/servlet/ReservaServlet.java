package com.alquinow.servlet;

import com.alquinow.dao.PropiedadDAO;
import com.alquinow.dao.ReservaDAO;
import com.alquinow.modelo.Propiedad;
import com.alquinow.modelo.Reserva;
import com.alquinow.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servlet para reservas. Requiere sesión de comprador.
 *
 *  GET  /reservas            -> lista las reservas del comprador logueado (JSON)
 *  POST /reservas            -> crea una reserva
 *  POST /reservas?accion=cancelar&id=3
 */
@WebServlet("/reservas")
public class ReservaServlet extends HttpServlet {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final PropiedadDAO propiedadDAO = new PropiedadDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        Usuario u = usuarioEnSesion(req);
        if (u == null) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Iniciá sesión para ver tus reservas.\"}");
            return;
        }

        try {
            List<Reserva> reservas = reservaDAO.listarPorComprador(u.getIdUsuario());
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < reservas.size(); i++) {
                Reserva r = reservas.get(i);
                sb.append("{")
                  .append("\"id\":").append(r.getIdReserva()).append(",")
                  .append("\"idPropiedad\":").append(r.getIdPropiedadFk()).append(",")
                  .append("\"ciudad\":\"").append(safe(r.getCiudadPropiedad())).append("\",")
                  .append("\"fechaInicio\":\"").append(r.getFechaInicio()).append("\",")
                  .append("\"fechaFinal\":\"").append(r.getFechaFinal()).append("\",")
                  .append("\"estado\":\"").append(safe(r.getEstado())).append("\",")
                  .append("\"montoTotal\":").append(r.getMontoTotal())
                  .append("}");
                if (i < reservas.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            out.print(sb.toString());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + safe(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        Usuario u = usuarioEnSesion(req);
        if (u == null || !"comprador".equals(u.getRol())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Iniciá sesión como comprador para reservar.\"}");
            return;
        }

        try {
            String accion = req.getParameter("accion");
            if ("cancelar".equals(accion)) {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean ok = reservaDAO.actualizarEstado(id, "cancelada");
                out.print("{\"ok\":" + ok + "}");
                return;
            }

            int idPropiedad = Integer.parseInt(req.getParameter("idPropiedad"));
            LocalDate inicio = LocalDate.parse(req.getParameter("fechaInicio"));
            LocalDate fin = LocalDate.parse(req.getParameter("fechaFinal"));

            if (!fin.isAfter(inicio)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"La fecha final debe ser posterior a la inicial.\"}");
                return;
            }

            // Calculamos el monto total: noches * precio por noche
            Propiedad p = propiedadDAO.buscarPorId(idPropiedad);
            if (p == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"La propiedad no existe.\"}");
                return;
            }
            long noches = ChronoUnit.DAYS.between(inicio, fin);
            BigDecimal monto = p.getPrecioPorNoche()
                    .multiply(BigDecimal.valueOf(noches));

            Reserva r = new Reserva();
            r.setIdCompradorFk(u.getIdUsuario());
            r.setIdPropiedadFk(idPropiedad);
            r.setFechaInicio(Date.valueOf(inicio));
            r.setFechaFinal(Date.valueOf(fin));
            r.setEstado("pendiente");
            r.setMontoTotal(monto);

            // Fecha límite de cancelación según la política de la propiedad
            Integer diasCancel = p.getDiasCancelacionSinPenalizacion();
            if (diasCancel != null && diasCancel > 0) {
                r.setDiasCancelacionAplicados(diasCancel);
                r.setFechaLimiteCancelacion(
                        Date.valueOf(inicio.minusDays(diasCancel)));
            }

            int id = reservaDAO.crear(r);
            out.print("{\"ok\":" + (id > 0) + ",\"id\":" + id
                    + ",\"noches\":" + noches + ",\"monto\":" + monto + "}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + safe(e.getMessage()) + "\"}");
        }
    }

    private Usuario usuarioEnSesion(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return (s == null) ? null : (Usuario) s.getAttribute("usuario");
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
