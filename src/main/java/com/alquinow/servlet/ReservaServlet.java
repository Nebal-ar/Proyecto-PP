package com.alquinow.servlet;

import com.alquinow.dao.PropiedadDAO;
import com.alquinow.dao.ReservaDAO;
import com.alquinow.modelo.Propiedad;
import com.alquinow.modelo.Reserva;
import com.alquinow.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@WebServlet("/reservas")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  
    maxFileSize = 1024 * 1024 * 10,       
    maxRequestSize = 1024 * 1024 * 50    
)
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
            String vista = req.getParameter("vista");
            List<Reserva> reservas;
            
            // Verificamos si lo pide el panel del huésped o el panel del vendedor
            if ("vendedor".equals(vista) && u.isVendedor()) {
                reservas = reservaDAO.listarPorVendedor(u.getIdUsuario());
            } else {
                reservas = reservaDAO.listarPorComprador(u.getIdUsuario());
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < reservas.size(); i++) {
                Reserva r = reservas.get(i);
                sb.append("{")
                  .append("\"id\":").append(r.getIdReserva()).append(",")
                  .append("\"idPropiedad\":").append(r.getIdPropiedadFk()).append(",")
                  .append("\"calle\":\"").append(safe(r.getCalle())).append("\",")
                  .append("\"altura\":").append(r.getAltura()).append(",")
                  .append("\"ciudad\":\"").append(safe(r.getCiudad())).append("\",")
                  .append("\"fechaInicio\":\"").append(r.getFechaInicio()).append("\",")
                  .append("\"fechaFinal\":\"").append(r.getFechaFinal()).append("\",")
                  .append("\"estado\":\"").append(safe(r.getEstado())).append("\",")
                  .append("\"montoTotal\":").append(r.getMontoTotal()).append(",")
                  .append("\"metodoPago\":\"").append(safe(r.getMetodoPago())).append("\",")
                  .append("\"comprobantePago\":\"").append(safe(r.getComprobantePago())).append("\",")
                  .append("\"correoComprador\":\"").append(safe(r.getCorreoComprador())).append("\",")
                  .append("\"fechaLimitePago\":\"").append(r.getFechaLimitePago()).append("\"")
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
        if (u == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"ok\": false, \"error\": \"Acceso denegado. Debes iniciar sesión.\"}");
            return; 
        }

        try {
            String accion = req.getParameter("accion");
            
            // ACCIONES DEL DUEÑO: Aprobar o Rechazar el pago
            if ("aprobar_pago".equals(accion) && u.isVendedor()) {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean ok = reservaDAO.actualizarEstado(id, "sena_pagada");
                out.print("{\"ok\":" + ok + "}");
                return;
            }
            if ("rechazar_pago".equals(accion) && u.isVendedor()) {
                int id = Integer.parseInt(req.getParameter("id"));
                // Lo devolvemos a pendiente para que el huésped lo suba de nuevo
                boolean ok = reservaDAO.actualizarEstado(id, "pendiente_sena"); 
                out.print("{\"ok\":" + ok + "}");
                return;
            }

            // ACCIONES DEL HUÉSPED:
            if ("cancelar".equals(accion)) {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean ok = reservaDAO.cancelarReserva(id);
                if (ok) {
                    out.print("{\"ok\": true}");
                } else {
                    out.print("{\"ok\": false, \"error\": \"La reserva ya no puede ser cancelada.\"}");
                }
                return;
            }
            
            // Subir comprobante del banco
            if ("subir_comprobante".equals(accion)) {
                int id = Integer.parseInt(req.getParameter("id"));
                Part partComp = req.getPart("comprobante");
                
                if (partComp != null && partComp.getSize() > 0) {
                    String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                    File uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) uploadDir.mkdirs();

                    String fName = Paths.get(partComp.getSubmittedFileName()).getFileName().toString();
                    String nComp = System.currentTimeMillis() + "_ticket_" + fName;
                    partComp.write(uploadPath + File.separator + nComp);
                    
                    boolean ok = reservaDAO.guardarComprobante(id, nComp);
                    out.print("{\"ok\":" + ok + "}");
                } else {
                    out.print("{\"ok\":false, \"error\":\"No se detectó ningún archivo.\"}");
                }
                return;
            }

            // NÚCLEO DE CREACIÓN DE RESERVAS
            int idPropiedad = Integer.parseInt(req.getParameter("idPropiedad"));
            LocalDate inicio = LocalDate.parse(req.getParameter("fechaInicio"));
            LocalDate fin = LocalDate.parse(req.getParameter("fechaFinal"));

            if (!fin.isAfter(inicio)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"La fecha final debe ser posterior a la inicial.\"}");
                return;
            }

            Propiedad p = propiedadDAO.buscarPorId(idPropiedad);
            if (p == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"La propiedad no existe.\"}");
                return;
            }
            
            long noches = ChronoUnit.DAYS.between(inicio, fin);
            BigDecimal monto = p.getPrecioPorNoche().multiply(BigDecimal.valueOf(noches));

            String metodoPagoElegido = req.getParameter("metodoPago");
            if (metodoPagoElegido == null || metodoPagoElegido.isBlank()) {
                metodoPagoElegido = "Transferencia";
            }

            Reserva r = new Reserva();
            r.setIdCompradorFk(u.getIdUsuario());
            r.setIdPropiedadFk(idPropiedad);
            r.setFechaInicio(Date.valueOf(inicio));
            r.setFechaFinal(Date.valueOf(fin));
            r.setMontoTotal(monto);
            r.setMetodoPago(metodoPagoElegido); 
            
            // Si elige tarjeta, simulamos que el pago se aprobó al instante. Sino, queda pendiente.
            if ("Tarjeta".equals(metodoPagoElegido)) {
                r.setEstado("sena_pagada"); 
            } else {
                r.setEstado("pendiente_sena"); 
            }
            
            int porcentajeSena = p.getPorcentajeSena() > 0 ? p.getPorcentajeSena() : 30;
            r.setPorcentajeSenaAplicado(porcentajeSena);
            r.setMontoPagado(BigDecimal.ZERO);

            int horasLimite = p.getHorasLimitePago() > 0 ? p.getHorasLimitePago() : 24;
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
            java.time.LocalDateTime limitePago = ahora.plusHours(horasLimite);
            r.setFechaLimitePago(java.sql.Timestamp.valueOf(limitePago));

            Integer diasCancel = p.getDiasCancelacionSinPenalizacion();
            if (diasCancel != null && diasCancel > 0) {
                r.setDiasCancelacionAplicados(diasCancel);
                r.setFechaLimiteCancelacion(Date.valueOf(inicio.minusDays(diasCancel)));
            } else {
                r.setDiasCancelacionAplicados(5);
                r.setFechaLimiteCancelacion(Date.valueOf(inicio.minusDays(5)));
            }

            int id = reservaDAO.crear(r);
            out.print("{\"ok\":" + (id > 0) + ",\"id\":" + id
                    + ",\"noches\":" + noches + ",\"monto\":" + monto + "}");

        } catch (Exception e) {
            e.printStackTrace();
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