package com.alquinow.servlet;

import com.alquinow.dao.UsuarioDAO;
import com.alquinow.modelo.Usuario;
import com.alquinow.util.Password;

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
import java.nio.file.Paths;

/**
 * Servlet para consultar y editar los datos del perfil del usuario logueado.
 */
@WebServlet("/perfil")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 20
)
public class PerfilServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"conectado\": false, \"error\": \"Debes iniciar sesión para ver tu perfil.\"}");
            return;
        }

        Usuario uSesion = (Usuario) session.getAttribute("usuario");
        try {
            Usuario uFresca = usuarioDAO.buscarPorId(uSesion.getIdUsuario());
            if (uFresca != null) {
                session.setAttribute("usuario", uFresca);
                uSesion = uFresca;
            }
            out.print(usuarioAJson(uSesion));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error al obtener los datos del perfil.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"ok\": false, \"error\": \"Debes iniciar sesión para editar tu perfil.\"}");
            return;
        }

        Usuario uSesion = (Usuario) session.getAttribute("usuario");

        try {
            Usuario uActual = usuarioDAO.buscarPorId(uSesion.getIdUsuario());
            if (uActual == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"ok\": false, \"error\": \"Usuario no encontrado.\"}");
                return;
            }

            String nombre = req.getParameter("nombre");
            String tel = req.getParameter("tel");
            String dni = req.getParameter("dni");
            String mail = req.getParameter("mail");
            String eliminarFoto = req.getParameter("eliminar_foto");

            // 1. Validar nombre
            if (nombre == null || nombre.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"ok\": false, \"error\": \"El nombre de usuario no puede estar vacío.\"}");
                return;
            }

            // 2. Validar teléfono (+549 seguido de 10 dígitos)
            if (tel == null || !tel.trim().matches("\\+549\\d{10}")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"ok\": false, \"error\": \"El teléfono debe empezar con +549 seguido de 10 números (ej: +5492914567890).\"}");
                return;
            }

            // 3. Validar DNI (8 dígitos)
            if (dni == null || !dni.trim().matches("\\d{8}")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"ok\": false, \"error\": \"El DNI debe contener exactamente 8 números.\"}");
                return;
            }

            // 4. Validar Mail (si lo envían)
            if (mail != null && !mail.isBlank()) {
                mail = mail.trim();
                if (usuarioDAO.existeMailOtroUsuario(mail, uActual.getIdUsuario())) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"Ese correo electrónico ya está en uso por otra cuenta.\"}");
                    return;
                }
                uActual.setMail(mail);
            }

            // 5. Procesar cambio de contraseña opcional
            String passActual = req.getParameter("contrasena_actual");
            String passNueva = req.getParameter("nueva_contrasena");
            String passConfirmar = req.getParameter("confirmar_contrasena");

            if (passNueva != null && !passNueva.isBlank()) {
                if (passActual == null || passActual.isBlank()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"Ingresá tu contraseña actual para poder cambiarla.\"}");
                    return;
                }
                if (!Password.verificar(passActual, uActual.getContrasena())) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"La contraseña actual que ingresaste es incorrecta.\"}");
                    return;
                }
                if (!passNueva.matches("^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$")) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"La nueva contraseña debe tener al menos 8 caracteres, una mayúscula y un carácter especial.\"}");
                    return;
                }
                if (passConfirmar != null && !passNueva.equals(passConfirmar)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"Las contraseñas nuevas no coinciden.\"}");
                    return;
                }
                usuarioDAO.actualizarContrasena(uActual.getIdUsuario(), passNueva);
                uActual.setContrasena(Password.hashear(passNueva));
            }

            // 6. Procesar foto de perfil
            String contentType = req.getContentType();
            if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
                Part partFoto = req.getPart("foto_perfil");
                if (partFoto != null && partFoto.getSize() > 0 && partFoto.getSubmittedFileName() != null && !partFoto.getSubmittedFileName().isBlank()) {
                    String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                    File uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }
                    String fileName = Paths.get(partFoto.getSubmittedFileName()).getFileName().toString();
                    String safeFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                    String nuevaFoto = System.currentTimeMillis() + "_perfil_" + safeFileName;
                    partFoto.write(uploadPath + File.separator + nuevaFoto);
                    uActual.setFotoPerfil(nuevaFoto);
                } else if ("true".equalsIgnoreCase(eliminarFoto)) {
                    uActual.setFotoPerfil(null);
                }
            } else if ("true".equalsIgnoreCase(eliminarFoto)) {
                uActual.setFotoPerfil(null);
            }

            // 7. Actualizar datos básicos en el objeto y la BD
            uActual.setNombre(nombre.trim());
            uActual.setTel(tel.trim());
            uActual.setDni(dni.trim());

            boolean actualizado = usuarioDAO.actualizarPerfil(uActual);
            if (actualizado) {
                session.setAttribute("usuario", uActual);
                out.print("{\"ok\": true, \"mensaje\": \"¡Tu perfil se actualizó correctamente!\", \"perfil\": " + usuarioAJson(uActual) + "}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"ok\": false, \"error\": \"No se pudieron guardar los cambios.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"ok\": false, \"error\": \"Error al actualizar el perfil: verificá que la imagen no sea demasiado pesada.\"}");
        }
    }

    private String usuarioAJson(Usuario u) {
        String nombre = u.getNombre() != null ? u.getNombre() : "";
        String mail = u.getMail() != null ? u.getMail() : "";
        String dni = u.getDni() != null ? u.getDni() : "";
        String tel = u.getTel() != null ? u.getTel() : "";
        String foto = u.getFotoPerfil() != null ? u.getFotoPerfil() : "";

        return "{"
            + "\"conectado\": true,"
            + "\"idUsuario\": " + u.getIdUsuario() + ","
            + "\"nombre\": \"" + escapar(nombre) + "\","
            + "\"mail\": \"" + escapar(mail) + "\","
            + "\"dni\": \"" + escapar(dni) + "\","
            + "\"tel\": \"" + escapar(tel) + "\","
            + "\"fotoPerfil\": \"" + escapar(foto) + "\","
            + "\"esVendedor\": " + u.isVendedor() + ","
            + "\"esHuesped\": " + u.isHuesped()
            + "}";
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
