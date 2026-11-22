package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.model.Player;
import soe402.cardgame.support.TestGame;

@DisplayName("GameEngine: fatigue")
class FatigueTest {

    private GameEngine gameWithASingleCardDeck() {
        return TestGame.startGame(
                TestGame.deck(CardRegistry.wisp()),
                TestGame.deck(CardRegistry.wisp(), CardRegistry.wisp()));
    }

    @Test
    @DisplayName("drawing from an empty deck deals one fatigue damage")
    void firstEmptyDrawDealsOneDamage() {
        GameEngine engine = gameWithASingleCardDeck();
        Player alice = engine.getPlayers().get(0);
        assertEquals(1, alice.getHand().size(), "the only card was drawn on turn one");

        TestGame.endRound(engine);

        assertEquals(3, engine.getTurnNumber());
        assertEquals(29, alice.getHealth());
        assertEquals(1, alice.getFatigueCounter());
    }

    @Test
    @DisplayName("fatigue damage grows by one on every empty draw")
    void fatigueEscalates() {
        GameEngine engine = gameWithASingleCardDeck();
        Player alice = engine.getPlayers().get(0);

        TestGame.endRound(engine);
        TestGame.endRound(engine);

        assertEquals(5, engine.getTurnNumber());
        assertEquals(27, alice.getHealth(), "one damage, then two more");
        assertEquals(2, alice.getFatigueCounter());
    }

    @Test
    @DisplayName("fatigue damage can end the game")
    void fatigueCanBeLethal() {
        GameEngine engine = gameWithASingleCardDeck();
        Player alice = engine.getPlayers().get(0);
        alice.takeDamage(29);

        TestGame.endRound(engine);

        assertTrue(engine.isFinished());
        assertEquals("Bob", engine.getWinner().orElseThrow().getName());
    }

    @Test
    @DisplayName("fatigue is described as such in the log")
    void fatigueIsLogged() {
        GameEngine engine = gameWithASingleCardDeck();
        TestGame.endRound(engine);

        assertTrue(engine.getLog().text().contains("fatigue damage"));
    }
}
