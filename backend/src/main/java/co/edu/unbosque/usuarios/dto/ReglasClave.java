package co.edu.unbosque.usuarios.dto;

/** reglas de contraseña segura: mayúscula, minúscula, número y símbolo */
public final class ReglasClave {
    // cada (?=.*x) exige que aparezca al menos un caracter de ese tipo
    public static final String PATRON = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).*$";
    public static final String MENSAJE =
            "La contraseña debe tener mayúsculas, minúsculas, números y símbolos";

    private ReglasClave() {
    }
}
