package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Modifier;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.StatMod;
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

    @Test
    @DisplayName("a creature dies when the health buff that kept it alive expires")
    void aCreatureDiesWhenItsHealthBuffExpires() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(CardRegistry.wisp(), CardRegistry.fortitude()),
                TestGame.deck(CardRegistry.wisp()));
        Player alice = engine.getPlayers().get(0);
        engine.playCard(0, null);   // turn 1: the wisp
        engine.endTurn();           // Bob's turn 2
        engine.endTurn();           // Alice's turn 3, the fortitude was just drawn

        Creature wisp = alice.getBoard().get(0);
        assertTrue(engine.playCard(0, wisp).success(), "the wisp becomes 1/4");
        wisp.takeDamage(3);
        assertEquals(1, wisp.getHealth());
        assertEquals(1, alice.getBoard().size());

        engine.endTurn();           // one turn of the buff remains
        engine.endTurn();           // Bob's turn 4
        engine.endTurn();           // the buff expires at the end of turn 5

        assertTrue(wisp.isDead(), "losing the buff leaves the wisp below zero health");
        assertTrue(alice.getBoard().isEmpty(),
                "a creature that died from a stat change must leave the board");
        assertEquals(2, alice.getDiscardPile().size(),
                "both the modifier and the dead creature are in the discard pile");
        assertTrue(engine.getLog().text().contains("Wisp dies"));
    }

    @Test
    @DisplayName("a negative health modifier kills a creature immediately")
    void aNegativeHealthModifierCanKillOnAttach() {
        Modifier curse = new Modifier("Curse of Weakness", 1, new StatMod(0, -3, "-3 health"), 2);
        GameEngine engine = TestGame.startGame(
                TestGame.deck(CardRegistry.wisp(), curse),
                TestGame.deck(CardRegistry.wisp()));
        Player alice = engine.getPlayers().get(0);
        engine.playCard(0, null);
        engine.endTurn();
        engine.endTurn();

        Creature wisp = alice.getBoard().get(0);
        assertTrue(engine.playCard(0, wisp).success());

        assertTrue(wisp.isDead(), "1 health minus 3 is below zero");
        assertTrue(alice.getBoard().isEmpty(), "the creature is removed straight away");
        assertTrue(engine.getLog().text().contains("Wisp dies"));
    }
}
