package com.alquinow.servlet;

import com.alquinow.dao.UsuarioDAO;
import com.alquinow.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Maneja login (POST /login) y logout (GET /logout).
 * Usa HttpSession: al loguearse, guarda el usuario en sesión.
 */
@WebServlet({"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** Login. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String mail = req.getParameter("mail");
        String pass = req.getParameter("contrasena");

        try {
            Usuario u = usuarioDAO.autenticar(mail, pass);
            if (u != null) {
                // Credenciales correctas: creamos sesión
                HttpSession sesion = req.getSession(true);
                sesion.setAttribute("usuario", u);
                sesion.setAttribute("rol", u.getRol());

                // Redirigimos según el rol
                if ("vendedor".equals(u.getRol())) {
                    resp.sendRedirect(req.getContextPath() + "/panel-vendedor.html");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/propiedades");
                }
            } else {
                req.setAttribute("error", "Mail o contraseña incorrectos.");
                req.getRequestDispatcher("/login.html").forward(req, resp);
            }
        } catch (Exception e) {
            throw new ServletException("Error al iniciar sesión", e);
        }
    }

    /** Logout. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate(); // destruye la sesión
        }
        resp.sendRedirect(req.getContextPath() + "/login.html");
    }
}
