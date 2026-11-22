package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Player;
import soe402.cardgame.support.TestGame;

@DisplayName("GameEngine: modifier cards")
class ModifierTest {

    private GameEngine gameWithBlessingInHand() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(CardRegistry.wisp(), CardRegistry.blessingOfMight(), CardRegistry.wisp()),
                TestGame.deck(CardRegistry.wisp(), CardRegistry.wisp()));
        engine.playCard(0, null);   // Alice plays a wisp on turn 1
        engine.endTurn();           // Bob's turn 2
        engine.playCard(0, null);   // Bob plays a wisp
        engine.endTurn();           // Alice's turn 3, the blessing was just drawn
        return engine;
    }

    @Test
    @DisplayName("a modifier raises the creature's attack immediately")
    void modifierBuffsAttack() {
        GameEngine engine = gameWithBlessingInHand();
        Player alice = engine.getPlayers().get(0);
        Creature wisp = alice.getBoard().get(0);

        ActionResult result = engine.playCard(0, wisp);

        assertTrue(result.success(), result.reason());
        assertEquals(4, wisp.getAttack(), "1 base attack plus 3 from the blessing");
        assertEquals(1, alice.getDiscardPile().size(), "the card itself is discarded");
    }

    @Test
    @DisplayName("a two turn modifier is still active on the next turn and gone after that")
    void modifierExpiresAfterItsDuration() {
        GameEngine engine = gameWithBlessingInHand();
        Player alice = engine.getPlayers().get(0);
        Creature wisp = alice.getBoard().get(0);
        engine.playCard(0, wisp);

        TestGame.endRound(engine);
        assertEquals(5, engine.getTurnNumber());
        assertEquals(4, wisp.getAttack(), "still active on the second turn");

        TestGame.endRound(engine);
        assertEquals(7, engine.getTurnNumber());
        assertEquals(1, wisp.getAttack(), "expired after two turns");
        assertTrue(wisp.getModifiers().isEmpty());
    }

    @Test
    @DisplayName("a modifier cannot be attached to an enemy creature")
    void modifierCannotTargetAnEnemy() {
        GameEngine engine = gameWithBlessingInHand();
        Player bob = engine.getPlayers().get(1);
        Creature enemyWisp = bob.getBoard().get(0);

        ActionResult result = engine.playCard(0, enemyWisp);

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("Invalid target"), result.reason());
        assertEquals(1, enemyWisp.getAttack(), "the enemy creature is unchanged");
        assertEquals(1, engine.getCurrentPlayer().getHand().size(), "the card stays in hand");
    }

    @Test
    @DisplayName("a modifier is logged")
    void modifierIsLogged() {
        GameEngine engine = gameWithBlessingInHand();
        engine.playCard(0, engine.getPlayers().get(0).getBoard().get(0));

        assertTrue(engine.getLog().text().contains("gains +3 attack"));
    }
}
