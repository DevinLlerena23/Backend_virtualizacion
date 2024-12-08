package com.proyecto.proyecto.controller;

import com.proyecto.proyecto.entity.Usuarios;
import com.proyecto.proyecto.repository.Usuariosrepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
class UsuariosController {
    @Autowired
    private Usuariosrepository repository;

    private static final String JWT_SECRET = "tu_clave_secreta_muy_larga_y_segura_123456789_debe_ser_mas_larga_y_segura_2024";
    private static final long JWT_EXPIRATION = 86400000; // 24 horas

    private final SecretKey key = Keys.hmacShaKeyFor(
            JWT_SECRET.getBytes(StandardCharsets.UTF_8)
    );

    private String generateToken(Usuarios usuario) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(JWT_EXPIRATION)))
                .signWith(key)
                .compact();
    }

    @GetMapping("/usuarios")
    ResponseEntity<List<Usuarios>> all() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/usuarios/verify")
    ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Extraer el token del header (quitar el "Bearer ")
            String token = authHeader.replace("Bearer ", "");

            // Verificar el token y obtener el correo del usuario
            String correo = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            // Buscar el usuario por correo
            return repository.findByCorreo(correo)
                    .map(usuario -> {
                        response.put("id", usuario.getId());
                        response.put("nombre", usuario.getNombre());
                        response.put("correo", usuario.getCorreo());
                        response.put("telefono", usuario.getTelefono());
                        response.put("direccion", usuario.getDireccion());
                        response.put("isAdmin", usuario.getIsAdmin()); // Incluir el estado de admin
                        response.put("token", token); // Incluir el token en la respuesta
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("error", "Usuario no encontrado");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            response.put("error", "Token inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/usuarios/save")
    ResponseEntity<Map<String, Object>> newUsuario(@RequestBody Usuarios newUsuario) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (repository.findByCorreo(newUsuario.getCorreo()).isPresent()) {
                response.put("error", "El correo ya está registrado");
                return ResponseEntity.badRequest().body(response);
            }

            Usuarios savedUsuario = repository.save(newUsuario);
            String token = generateToken(savedUsuario);

            response.put("usuario", savedUsuario);
            response.put("token", token);
            response.put("mensaje", "Usuario registrado exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Error al registrar usuario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/usuarios/{id}")
    ResponseEntity<Map<String, Object>> one(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        return repository.findById(id)
                .map(usuario -> {
                    response.put("usuario", usuario);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("error", "Usuario no encontrado");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    @PutMapping("/usuarios/{id}")
    ResponseEntity<Map<String, Object>> actualizarUsuario(@RequestBody Usuarios newUsuario, @PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            return repository.findById(id)
                    .map(usuario -> {
                        if (newUsuario.getNombre() != null) {
                            usuario.setNombre(newUsuario.getNombre());
                        }
                        if (newUsuario.getCorreo() != null) {
                            usuario.setCorreo(newUsuario.getCorreo());
                        }
                        if (newUsuario.getContrasena() != null) {
                            usuario.setContrasena(newUsuario.getContrasena());
                        }
                        if (newUsuario.getTelefono() != null) {
                            usuario.setTelefono(newUsuario.getTelefono());
                        }
                        if (newUsuario.getDireccion() != null) {
                            usuario.setDireccion(newUsuario.getDireccion());
                        }
                        if (newUsuario.getIsAdmin() != null) { // Agregar verificación para el estado de admin
                            usuario.setIsAdmin(newUsuario.getIsAdmin());
                        }

                        Usuarios updatedUsuario = repository.save(usuario);
                        response.put("usuario", updatedUsuario);
                        response.put("mensaje", "Usuario actualizado exitosamente");
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("error", "Usuario no encontrado");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            response.put("error", "Error al actualizar usuario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();

        try {
            String correo = credentials.get("correo");
            String contraseña = credentials.get("contraseña");

            return repository.findByCorreo(correo)
                    .filter(usuario -> usuario.getContrasena().equals(contraseña))
                    .map(usuario -> {
                        String token = generateToken(usuario);
                        response.put("token", token);
                        response.put("id", usuario.getId());
                        response.put("nombre", usuario.getNombre());
                        response.put("correo", usuario.getCorreo());
                        response.put("telefono", usuario.getTelefono());
                        response.put("direccion", usuario.getDireccion());
                        response.put("isAdmin", usuario.getIsAdmin()); // Incluir el estado de admin
                        response.put("mensaje", "Inicio de sesión exitoso");
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("error", "Credenciales inválidas");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                    });
        } catch (Exception e) {
            response.put("error", "Error en el inicio de sesión");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/usuarios/{id}")
    ResponseEntity<Map<String, Object>> borrarUsuario(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                response.put("mensaje", "Usuario eliminado exitosamente");
                return ResponseEntity.ok(response);
            }
            response.put("error", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("error", "Error al eliminar usuario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}