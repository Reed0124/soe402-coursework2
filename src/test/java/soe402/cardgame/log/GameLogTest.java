package soe402.cardgame.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.engine.GameEngine;
import soe402.cardgame.event.GameEvent;
import soe402.cardgame.support.TestGame;

@DisplayName("GameLog: readable output")
class GameLogTest {

    @Test
    @DisplayName("a damage event is rendered with source and remaining health")
    void rendersDamage() {
        String line = GameLog.render(new GameEvent.DamageDealt("Alice", "Bob", 6, 24));
        assertEquals("Bob takes 6 damage from Alice (24 health left)", line);
    }

    @Test
    @DisplayName("fatigue damage is named as such")
    void rendersFatigue() {
        String line = GameLog.render(new GameEvent.DamageDealt("Fatigue", "Bob", 2, 28));
        assertEquals("Bob takes 2 fatigue damage (28 health left)", line);
    }

    @Test
    @DisplayName("the winner is announced")
    void rendersGameEnd() {
        assertTrue(GameLog.render(new GameEvent.GameEnded("Alice")).contains("Alice wins the game"));
    }

    @Test
    @DisplayName("the log receives events from a real game")
    void collectsEventsFromTheEngine() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(CardRegistry.wisp()),
                TestGame.deck(CardRegistry.wisp()));

        assertTrue(engine.getLog().text().contains("Game started - Alice goes first."));
        assertTrue(engine.getLog().text().contains("Alice draws Wisp"));
    }

    @Test
    @DisplayName("the log can be cleared between games")
    void clearRemovesLines() {
        GameLog log = new GameLog();
        log.log("something");
        assertFalse(log.getLines().isEmpty());

        log.clear();
        assertTrue(log.getLines().isEmpty());
        assertTrue(log.text().isEmpty());
    }

    @Test
    @DisplayName("the log can mirror to a stream while keeping the lines in memory")
    void teesToAStream() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        GameLog log = new GameLog(new PrintStream(bytes, true, StandardCharsets.UTF_8));

        log.log("hello");

        assertEquals("hello", log.getLines().get(0));
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("hello"));
    }
}
