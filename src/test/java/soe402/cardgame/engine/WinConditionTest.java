package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.cards.CardRegistry;
import soe402.cardgame.effect.CompositeEffect;
import soe402.cardgame.effect.DamageEffect;
import soe402.cardgame.effect.HealEffect;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.Spell;
import soe402.cardgame.model.StatMod;
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

    private GameEngine gameWhereAliceKillsHerself() {
        Spell combo = new Spell("Combo", 1,
                new CompositeEffect(new DamageEffect(30), new HealEffect(8)));
        GameEngine engine = TestGame.startGame(TestGame.deck(combo),
                TestGame.deck(CardRegistry.wisp()));
        assertTrue(engine.playCard(0, engine.getPlayers().get(0)).success());
        return engine;
    }

    @Test
    @DisplayName("a composite effect stops as soon as the game is over")
    void aCompositeStopsWhenTheGameEnds() {
        GameEngine engine = gameWhereAliceKillsHerself();
        Player alice = engine.getPlayers().get(0);

        assertTrue(engine.isFinished());
        assertEquals("Bob", engine.getWinner().orElseThrow().getName());
        assertEquals(0, alice.getHealth(),
                "the healing half of the composite must not run once the game is over");
    }

    @Test
    @DisplayName("healing, drawing and summoning are ignored after the game ends")
    void stateChangesAreIgnoredAfterTheEnd() {
        GameEngine engine = gameWhereAliceKillsHerself();
        Player alice = engine.getPlayers().get(0);
        Player bob = engine.getPlayers().get(1);
        int handBefore = alice.getHand().size();
        int boardBefore = alice.getBoard().size();

        engine.heal(alice, 10);
        engine.drawCards(alice, 1);
        engine.summon(alice, new Creature("Late Arrival", 1, 1, 1));
        engine.attachModifier(new Creature("Dummy", 1, 1, 1), new StatMod(3, 0, "+3 attack"), 1);
        engine.dealDamage(bob, 5, null);

        assertEquals(0, alice.getHealth(), "a dead hero cannot be healed after the end");
        assertEquals(handBefore, alice.getHand().size(), "no cards are drawn after the end");
        assertEquals(boardBefore, alice.getBoard().size(), "nothing is summoned after the end");
        assertEquals(30, bob.getHealth(), "the winner cannot be damaged after the end");
    }
}
