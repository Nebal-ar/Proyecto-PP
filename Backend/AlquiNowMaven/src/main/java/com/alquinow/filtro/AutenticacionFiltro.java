package com.alquinow.filtro;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filtro que protege las páginas privadas. Si el usuario no tiene sesión
 * iniciada y pide una ruta protegida, lo manda al login.
 *
 * Protege todo lo que esté bajo /privado/*  (poné ahí las páginas que
 * requieran login, como panel-vendedor o mis-reservas).
 */
@WebFilter("/privado/*")
public class AutenticacionFiltro implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession sesion = req.getSession(false);
        boolean logueado = (sesion != null
                && sesion.getAttribute("usuario") != null);

        if (logueado) {
            chain.doFilter(request, response); // dejá pasar
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.html?required=1");
        }
    }
}
