package soe402.cardgame.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.engine.GameEngine;
import soe402.cardgame.log.GameLog;

@DisplayName("End to end: a full game")
class FullGameTest {

    private String play(long seed) {
        GameEngine engine = Main.newGame(seed, new GameLog());
        Main.run(engine);
        return engine.getLog().text();
    }

    @Test
    @DisplayName("a seeded game reaches a winner without intervention")
    void aSeededGameFinishes() {
        GameEngine engine = Main.newGame(2026L, new GameLog());

        Main.run(engine);

        assertTrue(engine.isFinished(), "the demo policy should finish within the turn limit");
        assertTrue(engine.getWinner().isPresent());
        assertTrue(engine.getTurnNumber() <= 60);
    }

    @Test
    @DisplayName("the winner's opponent is dead and the winner is not")
    void theWinnerIsTheSurvivor() {
        GameEngine engine = Main.newGame(2026L, new GameLog());
        Main.run(engine);

        assertTrue(engine.getPlayers().stream().anyMatch(player -> player.isDead()));
        assertFalse(engine.getWinner().orElseThrow().isDead());
    }

    @Test
    @DisplayName("the same seed always replays the same game")
    void gamesAreReproducible() {
        assertEquals(play(7L), play(7L));
    }

    @Test
    @DisplayName("the log records the whole story of the game")
    void theLogCoversTheGame() {
        String log = play(2026L);

        assertTrue(log.contains("Game started"));
        assertTrue(log.contains("draws"));
        assertTrue(log.contains("plays"));
        assertTrue(log.contains("attacks"));
        assertTrue(log.contains("wins the game"));
    }
}
