package Minimarket.GlobalExpress_SpringBoot.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    // WRITE_ONLY: se puede recibir desde el React, pero nunca se devuelve en las respuestas.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "`contraseña`", nullable = false, length = 20)
    private String contrasena;

    @Column(name = "rol", nullable = false, length = 50)
    private String rol;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

}
