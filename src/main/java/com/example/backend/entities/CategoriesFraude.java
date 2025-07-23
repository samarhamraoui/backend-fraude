package com.example.backend.entities;

import com.example.backend.services.AuditLogListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity(name = "CategoriesFraude")
@Table(name = "categories_fraudes",schema = "tableref")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditLogListener.class)
public class CategoriesFraude {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    @Column(name="nom_categorie")
    private String nomCategorie;
}
