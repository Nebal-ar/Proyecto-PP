package com.alquinow.servlet;

import com.alquinow.dao.UsuarioDAO;
import com.alquinow.modelo.Usuario;
import com.alquinow.util.EmailUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Servlet que maneja el registro de nuevos usuarios.
 * Recibe los datos del formulario (registro.html) por POST (incluyendo foto de perfil).
 */
@WebServlet("/registro")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 20
)
public class RegistroServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String nombre = req.getParameter("nombre");
        String mail = req.getParameter("mail");
        String pass = req.getParameter("contrasena");
        String dni = req.getParameter("dni");
        String tel = req.getParameter("tel");
        String rol = "comprador";

        // 1. Validación de campos básicos (incluyendo nombre)
        if (nombre == null || nombre.isBlank() || mail == null || mail.isBlank() || pass == null || pass.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/registro.html?error=vacios");
            return;
        }

        // 2. Validación estricta de DNI en el servidor
        if (dni == null || !dni.matches("\\d{8}")) {
            resp.sendRedirect(req.getContextPath() + "/registro.html?error=dni");
            return;
        }

        // 3. Validación estricta de Teléfono en el servidor
        if (tel == null || !tel.matches("\\+549\\d{10}")) {
            resp.sendRedirect(req.getContextPath() + "/registro.html?error=tel");
            return;
        }
        
        // 4. Validación estricta de la Contraseña
        if (pass == null || !pass.matches("^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$")) {
            resp.sendRedirect(req.getContextPath() + "/registro.html?error=pass");
            return;
        }
        
        try {
            // 5. Verificamos si el correo ya existe
            if (usuarioDAO.existeMail(mail.trim())) {
                resp.sendRedirect(req.getContextPath() + "/registro.html?error=existe");
                return;
            }

            // 6. Procesamos y validamos la foto de perfil
            String nombreFotoPerfil = null;
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
                    nombreFotoPerfil = System.currentTimeMillis() + "_perfil_" + safeFileName;
                    partFoto.write(uploadPath + File.separator + nombreFotoPerfil);
                }
            }

            if (nombreFotoPerfil == null) {
                resp.sendRedirect(req.getContextPath() + "/registro.html?error=foto");
                return;
            }

            // 7. Generamos el código secreto de 6 dígitos
            Random rnd = new Random();
            int numeroAleatorio = rnd.nextInt(999999);
            String codigoGenerado = String.format("%06d", numeroAleatorio);

            Usuario u = new Usuario();
            u.setNombre(nombre.trim());
            u.setMail(mail.trim());
            u.setContrasena(pass); // el DAO la hashea
            u.setDni(dni.trim());
            u.setTel(tel.trim());
            u.setCodigoVerificacion(codigoGenerado);
            u.setFotoPerfil(nombreFotoPerfil);

            int id = usuarioDAO.registrar(u, rol);
            
            if (id > 0) {
                // 8. Disparamos el correo real a través de tu cuenta de Gmail
                EmailUtil.enviarCodigo(mail.trim(), codigoGenerado);
                
                // 9. Mandamos al usuario a la pantalla de verificación con el mail en la URL
                resp.sendRedirect(req.getContextPath() + "/verificar.html?mail=" + mail.trim());
            } else {
                resp.sendRedirect(req.getContextPath() + "/registro.html?error=db");
            }

        } catch (Exception e) {
            throw new ServletException("Error al registrar usuario", e);
        }
    }
}
