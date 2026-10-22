package com.alquinow.servlet;

import com.alquinow.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/session")
public class SessionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        // Pedimos la sesión, pero le pasamos "false" para que NO cree una nueva si no existe
        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("usuario") != null) {
            Usuario u = (Usuario) session.getAttribute("usuario");
            
            String nombre = u.getNombre() != null ? u.getNombre() : "";
            String fotoPerfil = u.getFotoPerfil() != null ? u.getFotoPerfil() : "";
            String mail = u.getMail() != null ? u.getMail() : "";
            String dni = u.getDni() != null ? u.getDni() : "";
            String tel = u.getTel() != null ? u.getTel() : "";

            out.print("{"
                + "\"conectado\": true, "
                + "\"esAdmin\": " + u.isAdmin() + ", "
                + "\"esVendedor\": " + u.isVendedor() + ", "
                + "\"esHuesped\": " + u.isHuesped() + ", "
                + "\"nombre\": \"" + escapar(nombre) + "\", "
                + "\"fotoPerfil\": \"" + escapar(fotoPerfil) + "\", "
                + "\"usuario\": {"
                    + "\"idUsuario\": " + u.getIdUsuario() + ", "
                    + "\"nombre\": \"" + escapar(nombre) + "\", "
                    + "\"mail\": \"" + escapar(mail) + "\", "
                    + "\"dni\": \"" + escapar(dni) + "\", "
                    + "\"tel\": \"" + escapar(tel) + "\", "
                    + "\"fotoPerfil\": \"" + escapar(fotoPerfil) + "\""
                + "}"
                + "}");
        } else {
            out.print("{\"conectado\": false}");
        }
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
