package com.alquinow.servlet;

import com.alquinow.dao.PropiedadDAO;
import com.alquinow.modelo.Propiedad;
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
import java.util.List;

@WebServlet("/propiedades")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  
    maxFileSize = 1024 * 1024 * 50,       
    maxRequestSize = 1024 * 1024 * 100    
)
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

        HttpSession sesion = req.getSession(false);
        Usuario u = (sesion == null) ? null : (Usuario) sesion.getAttribute("usuario");
        
        if (u == null || !u.isVendedor()) {
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

            String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            Part partComprobante = req.getPart("comprobante");
            String nombreComprobante = null;
            if (partComprobante != null && partComprobante.getSize() > 0) {
                String fileName = Paths.get(partComprobante.getSubmittedFileName()).getFileName().toString();
                nombreComprobante = System.currentTimeMillis() + "_comp_" + fileName;
                partComprobante.write(uploadPath + File.separator + nombreComprobante);
            }

            Part partFoto = req.getPart("foto_verificacion");
            String nombreFoto = null;
            if (partFoto != null && partFoto.getSize() > 0) {
                String fileName = Paths.get(partFoto.getSubmittedFileName()).getFileName().toString();
                nombreFoto = System.currentTimeMillis() + "_foto_" + fileName;
                partFoto.write(uploadPath + File.separator + nombreFoto);
            }

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
            p.setPiso(req.getParameter("piso"));
            p.setDescripcion(req.getParameter("descripcion"));
            p.setDiasCancelacionSinPenalizacion(parseEntero(req.getParameter("dias_cancelacion_sin_penalizacion")));
            
            // SE AGREGA LA CAPTURA DEL PORCENTAJE DE SEÑA
            Integer sena = parseEntero(req.getParameter("porcentaje_sena"));
            p.setPorcentajeSena(sena != null ? sena : 30);
            
            String precio = req.getParameter("precio_por_noche");
            if (precio != null && !precio.isBlank()) {
                p.setPrecioPorNoche(new BigDecimal(precio));
            }
            
            p.setComprobanteTitularidad(nombreComprobante);
            p.setFotoVerificacion(nombreFoto);
            
            String pisoTexto = req.getParameter("piso");
            boolean yaExiste = propiedadDAO.existePropiedad(p.getCalle(), p.getAltura(), p.getCiudad(), pisoTexto);

            if (yaExiste) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
                out.print("{\"ok\":false, \"mensaje\":\"Error: Ya existe una propiedad en esa dirección exacta.\"}");
                return; 
            }
            
            int id = propiedadDAO.crear(p);
            out.print("{\"ok\":" + (id > 0) + ",\"id\":" + id + "}");

        } catch (Exception e) {
            e.printStackTrace(); 
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Error al subir archivos: Verificá que la imagen no sea muy pesada\"}");
        }
    }

    private Integer parseEntero(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.valueOf(s.trim()); } 
        catch (NumberFormatException e) { return null; }
    }

    private boolean tieneAlgo(String s) {
        return s != null && !s.isBlank();
    }

    private String listaAJson(List<Propiedad> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(propiedadAJson(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private String propiedadAJson(Propiedad p) {
        String estadoV = p.getEstadoVerificacion() != null ? p.getEstadoVerificacion() : "pendiente";
        
        return "{"
            + "\"idPropiedad\":" + p.getIdPropiedad() + ","
            + "\"idVendedor\":" + p.getIdVendedorFk() + ","
            + "\"calle\":\"" + escapar(p.getCalle()) + "\","
            + "\"altura\":" + p.getAltura() + ","
            + "\"ciudad\":\"" + escapar(p.getCiudad()) + "\","
            + "\"provincia\":\"" + escapar(p.getProvincia()) + "\","
            + "\"pais\":\"" + escapar(p.getPais()) + "\","
            + "\"precioPorNoche\":" + p.getPrecioPorNoche() + ","
            + "\"metrosCuadrados\":" + p.getMetrosCuadrados() + ","
            + "\"cantPersonas\":" + p.getCantPersonas() + ","
            + "\"piso\":\"" + escapar(p.getPiso()) + "\","
            + "\"descripcion\":\"" + escapar(p.getDescripcion()) + "\","
            + "\"promedioEstrellas\":" + (p.getPromedioEstrellas() != null ? p.getPromedioEstrellas() : 0.0) + ","
            + "\"estadoVerificacion\":\"" + escapar(estadoV) + "\","
            + "\"motivoRechazo\":\"" + escapar(p.getMotivoRechazo()) + "\","
            + "\"porcentajeSena\":" + p.getPorcentajeSena()  // SE AGREGA AL JSON
            + "}";
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}