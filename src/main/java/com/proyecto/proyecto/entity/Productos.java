package com.proyecto.proyecto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Products")
@NoArgsConstructor
@Getter
@Setter
public class Productos implements Serializable {
    @Serial
    private static final long serialVersionUID =1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    @Size(max=1600)
    private String descripcion;
    private String precio;
    private String descuento;
    private Boolean estado;
    private String categoria;

    @Size(max=600)
    private String imagen;

    @ElementCollection
    @CollectionTable(name = "product_images",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 600)
    private List<String> imagenes = new ArrayList<>();
}