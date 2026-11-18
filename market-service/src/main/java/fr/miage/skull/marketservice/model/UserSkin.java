package fr.miage.skull.marketservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_skins",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skin_id"}))
@Getter @Setter @NoArgsConstructor
public class UserSkin {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_id")
    private Skin skin;

    @Column(nullable = false)
    private boolean equipped;
}