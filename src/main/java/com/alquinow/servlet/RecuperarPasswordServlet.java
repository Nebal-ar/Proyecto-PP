package com.alquinow.servlet;

import com.alquinow.dao.UsuarioDAO;
import com.alquinow.util.EmailUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Servlet que maneja la recuperación de contraseña por correo electrónico:
 * 1) enviar_codigo: genera un código de 6 dígitos, lo guarda en BD y lo envía al mail.
 * 2) verificar_codigo: comprueba que el código ingresado coincida con el enviado.
 * 3) cambiar_contrasena: valida el código y actualiza la contraseña en la BD.
 */
@WebServlet("/recuperar-password")
public class RecuperarPasswordServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String accion = req.getParameter("accion");
        String mail = req.getParameter("mail");

        if (mail == null || mail.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"ok\": false, \"error\": \"Por favor, ingresá tu correo electrónico.\"}");
            return;
        }
        mail = mail.trim();

        try {
            if ("enviar_codigo".equals(accion)) {
                if (!usuarioDAO.existeMail(mail)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"No encontramos ninguna cuenta registrada con ese correo.\"}");
                    return;
                }

                Random rnd = new Random();
                String codigoGenerado = String.format("%06d", rnd.nextInt(999999));

                boolean guardado = usuarioDAO.guardarCodigoRecuperacion(mail, codigoGenerado);
                if (!guardado) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"ok\": false, \"error\": \"No se pudo generar el código de recuperación.\"}");
                    return;
                }

                EmailUtil.enviarCodigoRecuperacion(mail, codigoGenerado);
                out.print("{\"ok\": true, \"mensaje\": \"Te enviamos un código de 6 dígitos a " + escapar(mail) + ".\"}");
                return;
            }

            if ("verificar_codigo".equals(accion)) {
                String codigo = req.getParameter("codigo");
                if (codigo == null || !codigo.trim().matches("\\d{6}")) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"Ingresá el código de 6 números que te llegó al correo.\"}");
                    return;
                }

                boolean valido = usuarioDAO.validarCodigoRecuperacion(mail, codigo.trim());
                if (valido) {
                    out.print("{\"ok\": true, \"mensaje\": \"Código verificado correctamente. Ahora elegí tu nueva contraseña.\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"El código ingresado es incorrecto. Verificá tu correo e intentá de nuevo.\"}");
                }
                return;
            }

            if ("cambiar_contrasena".equals(accion)) {
                String codigo = req.getParameter("codigo");
                String nuevaPass = req.getParameter("nueva_contrasena");
                String confirmarPass = req.getParameter("confirmar_contrasena");

                if (codigo == null || !codigo.trim().matches("\\d{6}")) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"El código de seguridad es inválido.\"}");
                    return;
                }

                if (nuevaPass == null || !nuevaPass.matches("^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$")) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un carácter especial.\"}");
                    return;
                }

                if (confirmarPass != null && !nuevaPass.equals(confirmarPass)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"Las contraseñas no coinciden.\"}");
                    return;
                }

                boolean cambiada = usuarioDAO.restablecerContrasenaConCodigo(mail, codigo.trim(), nuevaPass);
                if (cambiada) {
                    out.print("{\"ok\": true, \"mensaje\": \"¡Contraseña actualizada con éxito!\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"ok\": false, \"error\": \"El código de verificación expiró o es incorrecto.\"}");
                }
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"ok\": false, \"error\": \"Acción no reconocida.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"ok\": false, \"error\": \"Error en el servidor al procesar la solicitud.\"}");
        }
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
