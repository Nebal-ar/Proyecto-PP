package com.alquinow.servlet;

import com.alquinow.dao.PropiedadDAO;
import com.alquinow.modelo.Propiedad;
import com.alquinow.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet del Panel de Administrador para verificar titularidad y autenticidad
 * de las propiedades subidas por los anfitriones (aprobar / dar de alta, rechazar o pausar).
 */
@WebServlet("/admin-propiedades")
public class AdminPropiedadServlet extends HttpServlet {

    private final PropiedadDAO propiedadDAO = new PropiedadDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        Usuario u = (session == null) ? null : (Usuario) session.getAttribute("usuario");

        if (u == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Debes iniciar sesión para acceder al panel de administración.\"}");
            return;
        }

        try {
            List<Propiedad> lista = propiedadDAO.listarTodasParaAdmin();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                sb.append(propiedadAdminAJson(lista.get(i)));
                if (i < lista.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            out.print(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error al cargar las propiedades para verificar.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        Usuario u = (session == null) ? null : (Usuario) session.getAttribute("usuario");

        if (u == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"ok\": false, \"error\": \"Debes iniciar sesión como administrador.\"}");
            return;
        }

        try {
            String accion = req.getParameter("accion");
            int idPropiedad = Integer.parseInt(req.getParameter("id"));

            if ("aprobar".equals(accion)) {
                boolean ok = propiedadDAO.aprobarPropiedad(idPropiedad);
                out.print("{\"ok\": " + ok + ", \"mensaje\": \"Propiedad verificada y dada de alta en el catálogo.\"}");
                return;
            }

            if ("rechazar".equals(accion)) {
                String motivo = req.getParameter("motivo");
                if (motivo == null || motivo.isBlank()) {
                    motivo = "La documentación de titularidad o la foto de verificación no pudo ser validada.";
                }
                boolean ok = propiedadDAO.rechazarPropiedad(idPropiedad, motivo.trim());
                out.print("{\"ok\": " + ok + ", \"mensaje\": \"Propiedad rechazada.\"}");
                return;
            }

            if ("pendiente".equals(accion)) {
                boolean ok = propiedadDAO.ponerPendientePropiedad(idPropiedad);
                out.print("{\"ok\": " + ok + ", \"mensaje\": \"La propiedad volvió a estado pendiente de revisión.\"}");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"ok\": false, \"error\": \"Acción inválida.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"ok\": false, \"error\": \"Error al actualizar el estado de la propiedad.\"}");
        }
    }

    private String propiedadAdminAJson(Propiedad p) {
        String estadoV = (p.getEstadoVerificacion() != null && !p.getEstadoVerificacion().isBlank())
                ? p.getEstadoVerificacion()
                : "pendiente";
        String alias = (p.getAliasCbu() != null && !p.getAliasCbu().isBlank())
                ? p.getAliasCbu()
                : "alquinow.propietario.mp";

        StringBuilder fotosJson = new StringBuilder("[");
        try {
            List<String> fotos = propiedadDAO.obtenerImagenesPorPropiedad(p.getIdPropiedad());
            for (int i = 0; i < fotos.size(); i++) {
                fotosJson.append("\"").append(escapar(fotos.get(i))).append("\"");
                if (i < fotos.size() - 1) fotosJson.append(",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fotosJson.append("]");

        return "{"
            + "\"idPropiedad\":" + p.getIdPropiedad() + ","
            + "\"idVendedor\":" + p.getIdVendedorFk() + ","
            + "\"calle\":\"" + escapar(p.getCalle()) + "\","
            + "\"altura\":" + p.getAltura() + ","
            + "\"ciudad\":\"" + escapar(p.getCiudad()) + "\","
            + "\"provincia\":\"" + escapar(p.getProvincia()) + "\","
            + "\"pais\":\"" + escapar(p.getPais()) + "\","
            + "\"precioPorNoche\":" + (p.getPrecioPorNoche() != null ? p.getPrecioPorNoche() : 0) + ","
            + "\"metrosCuadrados\":" + (p.getMetrosCuadrados() != null ? p.getMetrosCuadrados() : 0) + ","
            + "\"cantPersonas\":" + (p.getCantPersonas() != null ? p.getCantPersonas() : 1) + ","
            + "\"piso\":\"" + escapar(p.getPiso()) + "\","
            + "\"descripcion\":\"" + escapar(p.getDescripcion()) + "\","
            + "\"estadoVerificacion\":\"" + escapar(estadoV) + "\","
            + "\"comprobanteTitularidad\":\"" + escapar(p.getComprobanteTitularidad()) + "\","
            + "\"fotoVerificacion\":\"" + escapar(p.getFotoVerificacion()) + "\","
            + "\"motivoRechazo\":\"" + escapar(p.getMotivoRechazo()) + "\","
            + "\"porcentajeSena\":" + p.getPorcentajeSena() + ","
            + "\"diasCancelacionSinPenalizacion\":" + (p.getDiasCancelacionSinPenalizacion() != null ? p.getDiasCancelacionSinPenalizacion() : 5) + ","
            + "\"aliasCbu\":\"" + escapar(alias) + "\","
            + "\"nombreVendedor\":\"" + escapar(p.getNombreVendedor()) + "\","
            + "\"dniVendedor\":\"" + escapar(p.getDniVendedor()) + "\","
            + "\"mailVendedor\":\"" + escapar(p.getMailVendedor()) + "\","
            + "\"telVendedor\":\"" + escapar(p.getTelVendedor()) + "\","
            + "\"fotoPerfilVendedor\":\"" + escapar(p.getFotoPerfilVendedor()) + "\","
            + "\"imagenes\":" + fotosJson.toString()
            + "}";
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
