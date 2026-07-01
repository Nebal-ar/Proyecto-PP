package com.alquinow.modelo;

/**
 * Representa una fila de la tabla Usuario.
 * La contraseña que se guarda acá ya viene hasheada con BCrypt.
 */
public class Usuario {

    private int idUsuario;
    private String contrasena; // hash BCrypt
    private String dni;
    private String mail;
    private String tel;

    // Indica el rol: "comprador" o "vendedor" (no es columna, se usa en sesión)
    private String rol;

    public Usuario() {
    }

    public Usuario(int idUsuario, String mail, String tel) {
        this.idUsuario = idUsuario;
        this.mail = mail;
        this.tel = tel;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
