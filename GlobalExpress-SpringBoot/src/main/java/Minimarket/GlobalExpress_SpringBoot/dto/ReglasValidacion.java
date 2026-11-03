package Minimarket.GlobalExpress_SpringBoot.dto;

public final class ReglasValidacion {

    private ReglasValidacion() {
    }

    /* Nombre de persona: solo letras (con tildes y ñ), espacios, apóstrofe, punto y guion. SIN números. */
    public static final String NOMBRE_PERSONA = "[\\p{L}\\s'.-]+";

    /** Igual que NOMBRE_PERSONA pero puede venir vacío */
    public static final String NOMBRE_PERSONA_OPCIONAL = "[\\p{L}\\s'.-]*";

    /* Nombre o marca de producto: debe ser texto, es decir, contener al menos una letra
     * (la parte "(?=.*\\p{L})"). Admite números junto a las letras (ej. "Coca Cola 1L"),
     * pero no puede ser solo números. */
    public static final String TEXTO_PRODUCTO = "(?=.*\\p{L})[\\p{L}0-9\\s.&'%/-]+";

    /* Nombre de usuario (login): letras sin tildes, números, punto, guion y guion bajo. Sin espacios. */
    public static final String NOMBRE_USUARIO = "[A-Za-z0-9._-]+";

    /* Contraseña: vacía (al editar un usuario, vacía significa "conservar la actual") o de 8 a 12
     * caracteres con una minúscula, una mayúscula, un dígito y un símbolo.
     * Al CREAR, la obligatoriedad la sigue exigiendo UsuarioService.validarContrasena. */
    public static final String CONTRASENA_O_VACIA ="^$|(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,12}";
}
