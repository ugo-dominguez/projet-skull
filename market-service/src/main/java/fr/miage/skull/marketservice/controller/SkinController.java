package fr.miage.skull.marketservice.controller;

import fr.miage.skull.marketservice.model.Skin;
import fr.miage.skull.marketservice.model.UserSkin;
import fr.miage.skull.marketservice.repository.SkinRepository;
import fr.miage.skull.marketservice.repository.UserSkinRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
public class SkinController {
    record CreateSkin(String name, String description, String flowerImage, String skullImage) {}
    record SkinView(UUID id, String name, String description, String flowerImage, String skullImage) {}
    record OwnedSkinView(UUID skinId, String name, boolean equipped) {}

    private final SkinRepository skins;
    private final UserSkinRepository userSkins;

    public SkinController(SkinRepository skins, UserSkinRepository userSkins) {
        this.skins = skins;
        this.userSkins = userSkins;
    }

    private static SkinView toView(Skin s) {
        return new SkinView(s.getId(), s.getName(), s.getDescription(),
                s.getFlowerImage(), s.getSkullImage());
    }

    @PostMapping("/skins")
    public SkinView create(@RequestBody CreateSkin req) {
        if (skins.existsByName(req.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Skin déjà existant");
        }
        Skin s = new Skin();
        s.setName(req.name());
        s.setDescription(req.description());
        s.setFlowerImage(req.flowerImage());
        s.setSkullImage(req.skullImage());
        return toView(skins.save(s));
    }

    @GetMapping("/skins")
    public List<SkinView> list() {
        return skins.findAll().stream().map(SkinController::toView).toList();
    }

    @PostMapping("/users/{userId}/skins/{skinId}")
    public OwnedSkinView buy(@PathVariable UUID userId, @PathVariable UUID skinId) {
        Skin skin = skins.findById(skinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skin inconnu"));
        if (userSkins.findByUserIdAndSkinId(userId, skinId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Skin déjà acheté");
        }
        UserSkin us = new UserSkin();
        us.setUserId(userId);
        us.setSkin(skin);
        us.setEquipped(false);
        userSkins.save(us);
        return new OwnedSkinView(skin.getId(), skin.getName(), false);
    }

    @GetMapping("/users/{userId}/skins")
    @Transactional(readOnly = true)
    public List<OwnedSkinView> owned(@PathVariable UUID userId) {
        return userSkins.findByUserId(userId).stream()
                .map(us -> new OwnedSkinView(us.getSkin().getId(), us.getSkin().getName(), us.isEquipped()))
                .toList();
    }

    @PutMapping("/users/{userId}/skins/{skinId}/equip")
    @Transactional
    public OwnedSkinView equip(@PathVariable UUID userId, @PathVariable UUID skinId) {
        UserSkin target = userSkins.findByUserIdAndSkinId(userId, skinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skin non possédé"));
        userSkins.findByUserIdAndEquippedTrue(userId).ifPresent(cur -> cur.setEquipped(false));
        target.setEquipped(true);
        return new OwnedSkinView(skinId, target.getSkin().getName(), true);
    }
}