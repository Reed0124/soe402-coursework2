package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.model.Player;
import soe402.cardgame.support.TestGame;

@DisplayName("GameEngine: starting a game and running turns")
class GameEngineTurnTest {

    private GameEngine game() {
        return TestGame.startGame(
                TestGame.copies(15, CardRegistry::wisp),
                TestGame.copies(15, CardRegistry::wisp));
    }

    @Test
    @DisplayName("the first player starts with one mana and has already drawn")
    void firstTurnIsSetUp() {
        GameEngine engine = game();

        assertEquals(1, engine.getTurnNumber());
        assertEquals(TestGame.FIRST, engine.getCurrentPlayer().getName());
        assertEquals(1, engine.getCurrentPlayer().getMana());
        assertEquals(1, engine.getCurrentPlayer().getHand().size());
        assertEquals(Phase.MAIN, engine.getPhase());
        assertEquals(0, engine.getOpponent().getHand().size());
    }

    @Test
    @DisplayName("starting the same game twice is a programming error")
    void startingTwiceThrows() {
        GameEngine engine = game();
        assertThrows(IllegalStateException.class, engine::startGame);
    }

    @Test
    @DisplayName("turns alternate between the two players")
    void turnsAlternate() {
        GameEngine engine = game();

        engine.endTurn();
        assertEquals(2, engine.getTurnNumber());
        assertEquals(TestGame.SECOND, engine.getCurrentPlayer().getName());
        assertEquals(1, engine.getCurrentPlayer().getHand().size());

        engine.endTurn();
        assertEquals(3, engine.getTurnNumber());
        assertEquals(TestGame.FIRST, engine.getCurrentPlayer().getName());
    }

    @Test
    @DisplayName("mana only grows on your own turns")
    void manaGrowsOnYourOwnTurns() {
        GameEngine engine = game();
        Player alice = engine.getPlayers().get(0);
        Player bob = engine.getPlayers().get(1);

        assertEquals(1, alice.getMaxMana());
        assertEquals(0, bob.getMaxMana());

        engine.endTurn();
        assertEquals(1, alice.getMaxMana(), "the opponent's turn must not raise your mana");
        assertEquals(1, bob.getMaxMana());

        engine.endTurn();
        assertEquals(2, alice.getMaxMana());
        assertEquals(2, alice.getMana());
    }

    @Test
    @DisplayName("the mana pool stops growing at ten")
    void manaIsCappedAtTen() {
        GameEngine engine = game();
        Player alice = engine.getPlayers().get(0);

        for (int i = 0; i < 24; i++) {
            engine.endTurn();
        }
        assertEquals(10, alice.getMaxMana());
    }

    @Test
    @DisplayName("a game needs two distinct players")
    void theSamePlayerCannotPlayItself() {
        Player only = TestGame.player("Solo", TestGame.copies(5, CardRegistry::wisp));
        assertThrows(IllegalArgumentException.class, () -> new GameEngine(only, only));
    }

    @Test
    @DisplayName("every turn is logged")
    void turnsAreLogged() {
        GameEngine engine = game();
        assertTrue(engine.getLog().text().contains("Game started"));
        assertTrue(engine.getLog().text().contains("Turn 1"));
    }
}
