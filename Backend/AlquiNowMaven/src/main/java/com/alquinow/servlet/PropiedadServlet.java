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
import java.math.BigDecimal;
import java.util.List;

/**
 * Servlet REST-like para propiedades. Devuelve JSON para que el frontend
 * HTML5 (con fetch/JS) lo consuma.
 *
 *  GET  /propiedades                 -> lista todas (o filtra por querystring)
 *  GET  /propiedades?id=5            -> una propiedad
 *  GET  /propiedades?ciudad=...&precioMax=...&personasMin=...
 *  POST /propiedades                 -> crea (requiere sesión de vendedor)
 *  POST /propiedades?accion=eliminar&id=5
 *
 * Para no depender de librerías externas, el JSON se arma a mano con un
 * pequeño helper. En un proyecto real usarías Jackson o Gson.
 */
@WebServlet("/propiedades")
public class PropiedadServlet extends HttpServlet {

    private final PropiedadDAO propiedadDAO = new PropiedadDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idParam = req.getParameter("id");
            if (idParam != null) {
                Propiedad p = propiedadDAO.buscarPorId(Integer.parseInt(idParam));
                out.print(p == null ? "null" : propiedadAJson(p));
                return;
            }

            // ¿Hay filtros?
            String ciudad = req.getParameter("ciudad");
            String provincia = req.getParameter("provincia");
            Integer precioMax = parseEntero(req.getParameter("precioMax"));
            Integer personasMin = parseEntero(req.getParameter("personasMin"));

            List<Propiedad> lista;
            if (tieneAlgo(ciudad) || tieneAlgo(provincia)
                    || precioMax != null || personasMin != null) {
                lista = propiedadDAO.buscar(ciudad, provincia, precioMax, personasMin);
            } else {
                lista = propiedadDAO.listarTodas();
            }

            out.print(listaAJson(lista));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapar(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // Solo vendedores logueados pueden crear/eliminar
        HttpSession sesion = req.getSession(false);
        Usuario u = (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");
        if (u == null || !"vendedor".equals(u.getRol())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Necesitás iniciar sesión como vendedor.\"}");
            return;
        }

        try {
            String accion = req.getParameter("accion");

            if ("eliminar".equals(accion)) {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean ok = propiedadDAO.eliminar(id);
                out.print("{\"ok\":" + ok + "}");
                return;
            }

            // Crear propiedad
            Propiedad p = new Propiedad();
            p.setIdVendedorFk(u.getIdUsuario());
            p.setCalle(req.getParameter("calle"));
            p.setAltura(parseEntero(req.getParameter("altura")));
            p.setCodigoPostal(req.getParameter("codigo_postal"));
            p.setCiudad(req.getParameter("ciudad"));
            p.setProvincia(req.getParameter("provincia"));
            p.setPais(req.getParameter("pais"));
            p.setEstadiaMinima(parseEntero(req.getParameter("estadia_minima")));
            p.setMetrosCuadrados(parseEntero(req.getParameter("metros_cuadrados")));
            p.setCantPersonas(parseEntero(req.getParameter("cant_personas")));
            p.setPiso(parseEntero(req.getParameter("piso")));
            p.setDescripcion(req.getParameter("descripcion"));
            p.setDiasCancelacionSinPenalizacion(
                    parseEntero(req.getParameter("dias_cancelacion_sin_penalizacion")));
            p.setDisponibilidadInmediata(
                    "true".equals(req.getParameter("disponibilidad_inmediata"))
                    || "on".equals(req.getParameter("disponibilidad_inmediata")));

            String precio = req.getParameter("precio_por_noche");
            if (precio != null && !precio.isBlank()) {
                p.setPrecioPorNoche(new BigDecimal(precio));
            }

            int id = propiedadDAO.crear(p);
            out.print("{\"ok\":" + (id > 0) + ",\"id\":" + id + "}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapar(e.getMessage()) + "\"}");
        }
    }

    // ---------- Helpers de parseo ----------

    private Integer parseEntero(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean tieneAlgo(String s) {
        return s != null && !s.isBlank();
    }

    // ---------- Helpers de JSON (armado manual) ----------

    private String listaAJson(List<Propiedad> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(propiedadAJson(lista.get(i)));
            if (i < lista.size() - 1) {
                sb.append(",");
            }
        }
        return sb.append("]").toString();
    }

    private String propiedadAJson(Propiedad p) {
        return "{"
            + "\"id\":" + p.getIdPropiedad() + ","
            + "\"idVendedor\":" + p.getIdVendedorFk() + ","
            + "\"calle\":\"" + escapar(p.getCalle()) + "\","
            + "\"altura\":" + p.getAltura() + ","
            + "\"ciudad\":\"" + escapar(p.getCiudad()) + "\","
            + "\"provincia\":\"" + escapar(p.getProvincia()) + "\","
            + "\"pais\":\"" + escapar(p.getPais()) + "\","
            + "\"precioPorNoche\":" + p.getPrecioPorNoche() + ","
            + "\"metrosCuadrados\":" + p.getMetrosCuadrados() + ","
            + "\"cantPersonas\":" + p.getCantPersonas() + ","
            + "\"piso\":" + p.getPiso() + ","
            + "\"descripcion\":\"" + escapar(p.getDescripcion()) + "\","
            + "\"disponibilidadInmediata\":" + p.isDisponibilidadInmediata()
            + "}";
    }

    /** Escapa comillas y saltos de línea para no romper el JSON. */
    private String escapar(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
