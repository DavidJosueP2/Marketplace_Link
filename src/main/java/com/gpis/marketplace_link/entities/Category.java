package com.gpis.marketplace_link.entities;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Entity
@Table(name="categories")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

     @OneToMany(mappedBy = "category")
     private List<Publication> publications;

}
