package com.alquinow.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    private static final String MI_CORREO = "demiancastro697@gmail.com"; 
    private static final String MI_PASS = "svbkkebyyxhezluf"; 

    private static Session crearSesionSmtp() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MI_CORREO, MI_PASS);
            }
        });
    }

    public static void enviarCodigo(String correoDestino, String codigo) throws Exception {
        Session session = crearSesionSmtp();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MI_CORREO, "Seguridad AlquiNow"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
        message.setSubject("Tu código de verificación de AlquiNow");
        
        String html = "<h2 style='color:#ff5a5f;'>Bienvenido a AlquiNow</h2>"
                    + "<p>Tu código de seguridad para activar la cuenta es:</p>"
                    + "<h1 style='background:#f4f4f4; padding:10px; display:inline-block; border-radius:5px; letter-spacing: 5px;'>" + codigo + "</h1>"
                    + "<p>Si no fuiste vos, ignorá este correo.</p>";
                    
        message.setContent(html, "text/html; charset=utf-8");

        Transport.send(message);
    }

    public static void enviarCodigoRecuperacion(String correoDestino, String codigo) throws Exception {
        Session session = crearSesionSmtp();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MI_CORREO, "Seguridad AlquiNow"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
        message.setSubject("Recuperación de contraseña - AlquiNow");

        String html = "<h2 style='color:#e0603a;'>Recuperación de contraseña — AlquiNow</h2>"
                    + "<p>Recibimos una solicitud para restablecer la contraseña de tu cuenta.</p>"
                    + "<p>Ingresá el siguiente código de verificación de 6 dígitos en la página:</p>"
                    + "<h1 style='background:#f5f3ee; border:1px solid #e2ddd2; color:#1d2433; padding:12px 18px; display:inline-block; border-radius:8px; letter-spacing: 6px;'>" + codigo + "</h1>"
                    + "<p style='color:#6b7280; font-size:0.9rem;'>Si no solicitaste cambiar tu contraseña, podés ignorar este correo.</p>";

        message.setContent(html, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
