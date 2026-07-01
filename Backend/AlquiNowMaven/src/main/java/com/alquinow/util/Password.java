package com.alquinow.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para hashear y verificar contraseñas usando BCrypt.
 *
 * Necesita el .jar "jbcrypt-0.4.jar" en la carpeta /lib.
 *
 * ¿Por qué BCrypt? Nunca guardamos la contraseña en texto plano.
 * Guardamos un "hash" irreversible. Al hacer login, comparamos el hash
 * de lo que escribió el usuario contra el hash guardado.
 */
public class Password {

    /** Genera el hash de una contraseña en texto plano. */
    public static String hashear(String textoPlano) {
        return BCrypt.hashpw(textoPlano, BCrypt.gensalt(12));
    }

    /** Verifica si una contraseña en texto plano coincide con un hash. */
    public static boolean verificar(String textoPlano, String hashGuardado) {
        if (hashGuardado == null || hashGuardado.isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(textoPlano, hashGuardado);
    }
}
