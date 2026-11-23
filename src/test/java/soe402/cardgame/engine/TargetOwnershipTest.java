package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.effect.DamageEffect;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Deck;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.Spell;
import soe402.cardgame.support.TestGame;

/**
 * Regression tests for a hole found in review: the engine accepted targets that do not take
 * part in the current game, so an action could damage an unrelated player or a creature that
 * had already left the board.
 */
@DisplayName("GameEngine: targets must belong to this game")
class TargetOwnershipTest {

    private GameEngine gameWithTwoBoards() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(TestGame.creature("Bear", 1, 2, 2), CardRegistry.wisp()),
                TestGame.deck(CardRegistry.wisp()));
        engine.playCard(0, null);   // Alice's bear
        engine.endTurn();           // Bob's turn
        engine.playCard(0, null);   // Bob's wisp
        engine.endTurn();           // Alice's turn again
        return engine;
    }

    @Test
    @DisplayName("a player from another game cannot be attacked")
    void aForeignPlayerCannotBeAttacked() {
        GameEngine engine = gameWithTwoBoards();
        Player bob = engine.getPlayers().get(1);
        Player outsider = new Player("Outsider", new Deck(List.of(), false));

        ActionResult result = engine.attack(0, outsider);

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("not part of this game"), result.reason());
        assertEquals(30, outsider.getHealth(), "the outsider must not be damaged");
        assertEquals(30, bob.getHealth(), "the real opponent must not be damaged either");

        assertTrue(engine.attack(0, bob).success(),
                "a rejected attack must not consume the creature's attack");
        assertEquals(28, bob.getHealth());
    }

    @Test
    @DisplayName("a spell cannot damage a character from another game")
    void aForeignCharacterCannotBeTargetedByASpell() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(new Spell("Bolt", 1, new DamageEffect(3))),
                TestGame.deck(CardRegistry.wisp()));
        Player alice = engine.getPlayers().get(0);
        Player stranger = new Player("Stranger", new Deck(List.of(), false));

        ActionResult result = engine.playCard(0, stranger);

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("not part of this game"), result.reason());
        assertEquals(30, stranger.getHealth());
        assertEquals(1, alice.getHand().size(), "the card stays in hand");
        assertEquals(1, alice.getMana(), "no mana is spent");
    }

    @Test
    @DisplayName("a creature that has already left the board cannot be targeted again")
    void aDeadCreatureIsNoLongerAValidTarget() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(new Spell("Bolt", 1, new DamageEffect(3)),
                        new Spell("Bolt", 1, new DamageEffect(3))),
                TestGame.deck(CardRegistry.wisp()));
        engine.endTurn();           // Bob's turn
        engine.playCard(0, null);   // Bob's wisp
        engine.endTurn();           // Alice's turn

        Player bob = engine.getPlayers().get(1);
        Creature wisp = bob.getBoard().get(0);
        assertTrue(engine.playCard(0, wisp).success(), "the first bolt kills the wisp");
        assertTrue(bob.getBoard().isEmpty());

        ActionResult second = engine.playCard(0, wisp);

        assertTrue(second.isDenied());
        assertTrue(second.reason().contains("not part of this game"), second.reason());
    }

    @Test
    @DisplayName("a creature that never entered the game is not a valid target")
    void aCreatureOutsideTheGameCannotBeAttacked() {
        GameEngine engine = gameWithTwoBoards();
        Player another = new Player("Another", new Deck(List.of(), false));
        Creature foreign = new Creature("Foreign", 1, 1, 1);
        another.addToBoard(foreign);

        ActionResult result = engine.attack(0, foreign);

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("not part of this game"), result.reason());
        assertEquals(1, foreign.getHealth());
    }

    @Test
    @DisplayName("legitimate targets are still accepted")
    void theRulesHaveNotBecomeTooStrict() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(new Spell("Bolt", 1, new DamageEffect(3))),
                TestGame.deck(CardRegistry.wisp()));
        Player alice = engine.getPlayers().get(0);

        ActionResult result = engine.playCard(0, alice);

        assertTrue(result.success(), result.reason());
        assertEquals(27, alice.getHealth(), "any character is a legal target for a bolt");
    }
}
