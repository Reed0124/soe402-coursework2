package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.model.Player;
import soe402.cardgame.support.TestGame;

@DisplayName("GameEngine: win condition")
class WinConditionTest {

    private GameEngine gameWithBobAtSixHealth() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(TestGame.damageSpell("Bolt", 1, 6)),
                TestGame.deck(CardRegistry.wisp()));
        engine.getPlayers().get(1).takeDamage(24);
        return engine;
    }

    @Test
    @DisplayName("the game is not over while both heroes are alive")
    void gameContinuesWhileBothAlive() {
        GameEngine engine = gameWithBobAtSixHealth();

        assertFalse(engine.isFinished());
        assertTrue(engine.getWinner().isEmpty());
    }

    @Test
    @DisplayName("damage that brings a hero to exactly zero ends the game")
    void exactlyZeroHealthIsLethal() {
        GameEngine engine = gameWithBobAtSixHealth();
        Player bob = engine.getPlayers().get(1);

        assertTrue(engine.playCard(0, bob).success());

        assertEquals(0, bob.getHealth());
        assertTrue(engine.isFinished());
    }

    @Test
    @DisplayName("the surviving player is the winner")
    void theOtherPlayerWins() {
        GameEngine engine = gameWithBobAtSixHealth();
        Player bob = engine.getPlayers().get(1);

        engine.playCard(0, bob);

        assertEquals("Alice", engine.getWinner().orElseThrow().getName());
    }

    @Test
    @DisplayName("the end of the game is written to the log")
    void theEndIsLogged() {
        GameEngine engine = gameWithBobAtSixHealth();
        engine.playCard(0, engine.getPlayers().get(1));

        assertTrue(engine.getLog().text().contains("wins the game"));
    }

    @Test
    @DisplayName("no action is accepted after the game has ended")
    void actionsAreRejectedAfterTheEnd() {
        GameEngine engine = gameWithBobAtSixHealth();
        Player bob = engine.getPlayers().get(1);
        engine.playCard(0, bob);

        assertTrue(engine.playCard(0, bob).isDenied());
        assertTrue(engine.endTurn().isDenied());
        assertTrue(engine.attack(0, bob).isDenied());
        assertTrue(engine.playCard(0, bob).reason().contains("already over"));
    }
}
