package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.effect.HealEffect;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.Spell;
import soe402.cardgame.support.TestGame;

@DisplayName("GameEngine: playing cards")
class PlayCardTest {

    @Test
    @DisplayName("a creature is summoned onto the board and its mana is spent")
    void playingACreature() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(TestGame.creature("Bear", 1, 2, 2)),
                TestGame.deck(CardRegistry.wisp()));
        Player alice = engine.getPlayers().get(0);

        ActionResult result = engine.playCard(0, null);

        assertTrue(result.success(), result.reason());
        assertEquals(1, alice.getBoard().size());
        assertEquals("Bear", alice.getBoard().get(0).getName());
        assertEquals(0, alice.getMana());
        assertEquals(0, alice.getHand().size());
        assertTrue(alice.getDiscardPile().isEmpty(), "a creature does not go to the discard pile");
    }

    @Test
    @DisplayName("a spell resolves, is discarded and does not stay on the board")
    void playingASpell() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(TestGame.damageSpell("Bolt", 1, 3)),
                TestGame.deck(CardRegistry.wisp()));
        Player alice = engine.getPlayers().get(0);
        Player bob = engine.getPlayers().get(1);

        ActionResult result = engine.playCard(0, bob);

        assertTrue(result.success(), result.reason());
        assertEquals(27, bob.getHealth());
        assertEquals(1, alice.getDiscardPile().size());
        assertEquals(0, alice.getBoard().size());
        assertEquals(0, alice.getHand().size());
    }

    @Test
    @DisplayName("an unaffordable card is rejected and nothing changes")
    void notEnoughMana() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(CardRegistry.fireball(), CardRegistry.wisp()),
                TestGame.deck(CardRegistry.wisp()));
        Player alice = engine.getPlayers().get(0);
        Player bob = engine.getPlayers().get(1);

        ActionResult result = engine.playCard(0, bob);

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("Not enough mana"), result.reason());
        assertEquals(1, alice.getHand().size(), "the card stays in hand");
        assertEquals(1, alice.getMana(), "no mana is spent");
        assertEquals(0, alice.getBoard().size());
        assertTrue(alice.getDiscardPile().isEmpty());
        assertEquals(30, bob.getHealth());
    }

    @Test
    @DisplayName("an index outside the hand is rejected")
    void invalidHandIndex() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(CardRegistry.wisp()),
                TestGame.deck(CardRegistry.wisp()));

        assertTrue(engine.playCard(3, null).isDenied());
        assertTrue(engine.playCard(-1, null).isDenied());
        assertEquals(1, engine.getCurrentPlayer().getHand().size());
    }

    @Test
    @DisplayName("a full board rejects further creatures")
    void fullBoard() {
        GameEngine engine = TestGame.startGame(
                TestGame.copies(15, CardRegistry::wisp),
                TestGame.copies(15, CardRegistry::wisp));
        Player alice = engine.getPlayers().get(0);

        while (alice.getBoard().size() < 7) {
            assertTrue(engine.playCard(0, null).success());
            engine.endTurn();
            engine.endTurn();
        }

        ActionResult result = engine.playCard(0, null);
        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("board is full"), result.reason());
        assertEquals(7, alice.getBoard().size());
        assertEquals(1, alice.getHand().size(), "the rejected card stays in hand");
    }

    @Test
    @DisplayName("a target that violates the card's requirement is rejected")
    void invalidTarget() {
        Spell heal = new Spell("Healing Light", 1, new HealEffect(3));
        GameEngine engine = TestGame.startGame(
                TestGame.deck(heal),
                TestGame.deck(CardRegistry.wisp()));
        Player bob = engine.getPlayers().get(1);

        ActionResult result = engine.playCard(0, bob);

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("Invalid target"), result.reason());
        assertEquals(1, engine.getCurrentPlayer().getHand().size());
    }

    @Test
    @DisplayName("a card that needs a target cannot be played without one")
    void missingTarget() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(TestGame.damageSpell("Bolt", 1, 3)),
                TestGame.deck(CardRegistry.wisp()));

        ActionResult result = engine.playCard(0, null);

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("Invalid target"), result.reason());
    }

    @Test
    @DisplayName("healing a damaged hero restores exactly the missing health")
    void healingRestoresHealth() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(new Spell("Healing Light", 1, new HealEffect(5))),
                TestGame.deck(CardRegistry.wisp()));
        Player alice = engine.getPlayers().get(0);
        alice.takeDamage(12);

        assertEquals(18, alice.getHealth());
        assertTrue(engine.playCard(0, alice).success());
        assertEquals(23, alice.getHealth());
        assertTrue(engine.getLog().text().contains("is healed for 5"));
    }
}
