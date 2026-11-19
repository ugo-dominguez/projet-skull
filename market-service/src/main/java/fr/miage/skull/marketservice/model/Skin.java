package fr.miage.skull.marketservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "skins")
@Getter @Setter @NoArgsConstructor
public class Skin {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "skin_name", nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "flower_image", nullable = false)
    private String flowerImage;

    @Column(name = "skull_image", nullable = false)
    private String skullImage;
}