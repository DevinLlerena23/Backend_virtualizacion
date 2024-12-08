package com.proyecto.proyecto.controller;

import com.proyecto.proyecto.entity.Productos;
import com.proyecto.proyecto.repository.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@RestController
public class ProductosController {
    @Autowired
    private ProductosRepository repository;

    // Obtener todos los productos
    @GetMapping("/productos")
    public List<Productos> all() {
        return repository.findAll();
    }

    // Obtener productos lanzados
    @GetMapping("/productos/lanzados")
    public List<Productos> getReleasedProducts() {
        return repository.findByEstado(true);
    }

    // Obtener productos próximos a lanzarse
    @GetMapping("/productos/proximos")
    public List<Productos> getUpcomingProducts() {
        return repository.findByEstado(false);
    }

    // Obtener un producto por su ID
    @GetMapping("/productos/{id}")
    public ResponseEntity<Productos> one(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear un nuevo producto
    @PostMapping("/productos/save")
    public ResponseEntity<Productos> newProducto(@RequestBody Productos newProducto) {
        if (newProducto.getImagenes() == null) {
            newProducto.setImagenes(new ArrayList<>());
        }
        Productos savedProducto = repository.save(newProducto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProducto);
    }

    // Actualizar un producto existente
    @PutMapping("/productos/{id}")
    public ResponseEntity<Productos> replaceProducto(@RequestBody Productos newProducto, @PathVariable Integer id) {
        return repository.findById(id)
                .map(productos -> {
                    productos.setNombre(newProducto.getNombre());
                    productos.setDescripcion(newProducto.getDescripcion());
                    productos.setPrecio(newProducto.getPrecio());
                    productos.setDescuento(newProducto.getDescuento());
                    productos.setImagen(newProducto.getImagen());
                    productos.setImagenes(newProducto.getImagenes());
                    productos.setEstado(newProducto.getEstado());
                    productos.setCategoria(newProducto.getCategoria());
                    return ResponseEntity.ok(repository.save(productos));
                })
                .orElseGet(() -> {
                    newProducto.setId(id);
                    return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(newProducto));
                });
    }

    // Eliminar un producto por su ID
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> borrarProducto(@PathVariable Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Añadir nuevos endpoints para manejar imágenes específicamente
    @PostMapping("/productos/{id}/imagenes")
    public ResponseEntity<Productos> addImagenes(@PathVariable Integer id, @RequestBody List<String> nuevasImagenes) {
        return repository.findById(id)
                .map(productos -> {
                    if (productos.getImagenes() == null) {
                        productos.setImagenes(new ArrayList<>());
                    }
                    productos.getImagenes().addAll(nuevasImagenes);
                    return ResponseEntity.ok(repository.save(productos));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/productos/{id}/imagenes/{index}")
    public ResponseEntity<Productos> removeImagen(@PathVariable Integer id, @PathVariable Integer index) {
        return repository.findById(id)
                .map(productos -> {
                    if (productos.getImagenes() != null &&
                            index >= 0 &&
                            index < productos.getImagenes().size()) {
                        productos.getImagenes().remove(index.intValue());
                        return ResponseEntity.ok(repository.save(productos));
                    }
                    // Devolver un ResponseEntity<Productos> vacío en caso de error
                    return ResponseEntity.badRequest().body(new Productos()); // Cambiado para devolver un nuevo objeto Productos
                })
                .orElse(ResponseEntity.notFound().build());
    }
}