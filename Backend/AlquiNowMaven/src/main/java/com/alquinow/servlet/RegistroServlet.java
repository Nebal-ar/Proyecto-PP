package com.alquinow.servlet;

import com.alquinow.dao.UsuarioDAO;
import com.alquinow.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet que maneja el registro de nuevos usuarios.
 * Recibe los datos del formulario (registro.html) por POST.
 *
 * NOTA: si usás Tomcat 9 o anterior, cambiá los imports "jakarta.servlet.*"
 * por "javax.servlet.*". Tomcat 10+ usa jakarta.
 */
@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String mail = req.getParameter("mail");
        String pass = req.getParameter("contrasena");
        String dni = req.getParameter("dni");
        String tel = req.getParameter("tel");
        String rol = req.getParameter("rol"); // "comprador" o "vendedor"

        // Validación mínima en el servidor
        if (mail == null || mail.isBlank() || pass == null || pass.isBlank()) {
            req.setAttribute("error", "El mail y la contraseña son obligatorios.");
            req.getRequestDispatcher("/registro.html").forward(req, resp);
            return;
        }

        try {
            if (usuarioDAO.existeMail(mail)) {
                req.setAttribute("error", "Ese mail ya está registrado.");
                req.getRequestDispatcher("/registro.html").forward(req, resp);
                return;
            }

            Usuario u = new Usuario();
            u.setMail(mail);
            u.setContrasena(pass); // el DAO la hashea
            u.setDni(dni);
            u.setTel(tel);

            int id = usuarioDAO.registrar(u, rol);
            if (id > 0) {
                // Registro OK -> mandamos al login
                resp.sendRedirect(req.getContextPath() + "/login.html?registrado=1");
            } else {
                req.setAttribute("error", "No se pudo registrar. Intentá de nuevo.");
                req.getRequestDispatcher("/registro.html").forward(req, resp);
            }

        } catch (Exception e) {
            throw new ServletException("Error al registrar usuario", e);
        }
    }
}
