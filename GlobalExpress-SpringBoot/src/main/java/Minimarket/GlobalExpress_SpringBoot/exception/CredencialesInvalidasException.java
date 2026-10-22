package Minimarket.GlobalExpress_SpringBoot.exception;
//Error cuando las credenciales son incorrectas
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos.");
    }
}
