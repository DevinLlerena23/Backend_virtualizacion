package com.proyecto.proyecto.repository;


import com.proyecto.proyecto.entity.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface Usuariosrepository extends JpaRepository<Usuarios,Integer> {
    Optional<Usuarios> findByCorreo(String correo);



}
