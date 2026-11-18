package fr.miage.skull.playerservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "players")
@Getter @Setter @NoArgsConstructor
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_admin", nullable = false)
    private boolean admin;

    @Column(name = "is_banned", nullable = false)
    private boolean banned;
}