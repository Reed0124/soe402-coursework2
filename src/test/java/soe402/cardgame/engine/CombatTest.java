package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Player;
import soe402.cardgame.support.TestGame;

@DisplayName("GameEngine: combat")
class CombatTest {

    private GameEngine gameWithTwoCreatures() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(TestGame.creature("Bear", 1, 2, 2), CardRegistry.wisp()),
                TestGame.deck(TestGame.creature("Wall", 1, 1, 4), CardRegistry.wisp()));
        engine.playCard(0, null);   // Alice plays the Bear
        engine.endTurn();           // Bob's turn
        engine.playCard(0, null);   // Bob plays the Wall
        engine.endTurn();           // back to Alice
        return engine;
    }

    @Test
    @DisplayName("a creature cannot attack on the turn it was played")
    void summoningSickness() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(TestGame.creature("Bear", 1, 2, 2)),
                TestGame.deck(CardRegistry.wisp()));
        engine.playCard(0, null);

        ActionResult result = engine.attack(0, engine.getOpponent());

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("cannot attack on the turn it was played"), result.reason());
        assertEquals(30, engine.getOpponent().getHealth());
    }

    @Test
    @DisplayName("a creature can attack the enemy hero from the next turn")
    void attackingTheEnemyHero() {
        GameEngine engine = gameWithTwoCreatures();
        Player bob = engine.getPlayers().get(1);

        ActionResult result = engine.attack(0, bob);

        assertTrue(result.success(), result.reason());
        assertEquals(28, bob.getHealth());
    }

    @Test
    @DisplayName("a creature can only attack once per turn")
    void onlyOneAttackPerTurn() {
        GameEngine engine = gameWithTwoCreatures();
        Player bob = engine.getPlayers().get(1);

        assertTrue(engine.attack(0, bob).success());
        ActionResult second = engine.attack(0, bob);

        assertTrue(second.isDenied());
        assertTrue(second.reason().contains("already attacked"), second.reason());
        assertEquals(28, bob.getHealth(), "the rejected attack must not deal damage");
    }

    @Test
    @DisplayName("a defender that survives strikes back")
    void retaliation() {
        GameEngine engine = gameWithTwoCreatures();
        Player alice = engine.getPlayers().get(0);
        Player bob = engine.getPlayers().get(1);
        Creature bear = alice.getBoard().get(0);
        Creature wall = bob.getBoard().get(0);

        ActionResult result = engine.attack(0, wall);

        assertTrue(result.success(), result.reason());
        assertEquals(2, wall.getHealth(), "the wall took the bear's two damage");
        assertEquals(1, bear.getHealth(), "the wall hit back for one");
        assertTrue(wall.getHealth() > 0 && bear.getHealth() > 0, "both survive");
    }

    @Test
    @DisplayName("a defender that dies does not strike back")
    void noRetaliationFromADeadDefender() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(TestGame.creature("Bear", 1, 2, 2)),
                TestGame.deck(CardRegistry.wisp()));
        engine.playCard(0, null);
        engine.endTurn();
        engine.playCard(0, null);   // Bob's 1/1 wisp
        engine.endTurn();

        Player alice = engine.getPlayers().get(0);
        Player bob = engine.getPlayers().get(1);
        Creature bear = alice.getBoard().get(0);

        ActionResult result = engine.attack(0, bob.getBoard().get(0));

        assertTrue(result.success(), result.reason());
        assertTrue(bob.getBoard().isEmpty(), "the dead creature leaves the board");
        assertEquals(2, bear.getHealth(), "a dead defender does not retaliate");
        assertEquals(1, bob.getDiscardPile().size(), "the creature goes to the discard pile");
    }

    @Test
    @DisplayName("attacking your own creatures is rejected")
    void cannotAttackYourOwnCreature() {
        GameEngine engine = gameWithTwoCreatures();
        Player alice = engine.getPlayers().get(0);

        ActionResult result = engine.attack(0, alice.getBoard().get(0));

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("your own character"), result.reason());
        assertEquals(2, alice.getBoard().get(0).getHealth(), "no damage was dealt");
    }

    @Test
    @DisplayName("attacking with an empty board is rejected")
    void cannotAttackWithoutCreatures() {
        GameEngine engine = TestGame.startGame(
                TestGame.deck(CardRegistry.wisp()),
                TestGame.deck(CardRegistry.wisp()));

        ActionResult result = engine.attack(0, engine.getOpponent());

        assertTrue(result.isDenied());
        assertTrue(result.reason().contains("no creature at board index 0"), result.reason());
    }

    @Test
    @DisplayName("attacks are written to the log")
    void attacksAreLogged() {
        GameEngine engine = gameWithTwoCreatures();
        engine.attack(0, engine.getPlayers().get(1));

        assertTrue(engine.getLog().text().contains("Bear attacks Bob"));
        assertTrue(engine.getLog().text().contains("Bob takes 2 damage from Bear"));
    }
}
