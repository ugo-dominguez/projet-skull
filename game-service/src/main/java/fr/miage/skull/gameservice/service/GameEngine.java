package fr.miage.skull.gameservice.service;

import fr.miage.skull.gameservice.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class GameEngine {
    public void start(Game game) {
        game.setStatus(GameStatus.IN_PROGRESS);

        List<PlayerGame> order = new ArrayList<>(game.getPlayerGames());
        Collections.shuffle(order);
        for (int i = 0; i < order.size(); i++) {
            order.get(i).setSeat(i);
        }

        for (PlayerGame pg : game.getPlayerGames()) {
            for (int i = 0; i < 3; i++) addDisc(pg, DiscType.FLOWER);
            addDisc(pg, DiscType.SKULL);
        }

        Round first = new Round();
        first.setGame(game);
        first.setRoundNumber(1);
        first.setStatus(RoundStatus.PLACING);
        game.getRounds().add(first);
    }

    private void addDisc(PlayerGame pg, DiscType type) {
        Disc d = new Disc();
        d.setPlayerGame(pg);
        d.setType(type);
        d.setStatus(DiscStatus.IN_HAND);
        pg.getDiscs().add(d);
    }
}