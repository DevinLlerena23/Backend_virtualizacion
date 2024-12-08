package com.proyecto.proyecto.repository;

import com.proyecto.proyecto.entity.Productos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductosRepository extends JpaRepository<Productos, Integer> {
    List<Productos> findByEstado(boolean estado);
}