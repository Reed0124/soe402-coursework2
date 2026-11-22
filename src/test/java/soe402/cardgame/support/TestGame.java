package soe402.cardgame.support;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import soe402.cardgame.effect.DamageEffect;
import soe402.cardgame.engine.GameConfig;
import soe402.cardgame.engine.GameEngine;
import soe402.cardgame.log.GameLog;
import soe402.cardgame.model.Card;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Deck;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.Spell;

/**
 * Small factory helpers shared by the test classes.
 *
 * <p>Tests always build decks that are <em>not</em> shuffled, so the draw order is exactly the
 * order written in the test. That is the whole reason the production {@link Deck} accepts a
 * shuffle flag: a test that depends on a random shuffle is a test that fails on someone else's
 * machine.</p>
 */
public final class TestGame {

    public static final String FIRST = "Alice";
    public static final String SECOND = "Bob";

    private TestGame() {
    }

    /**
     * @param cards the cards, in draw order
     * @return a mutable list usable as a deck
     */
    public static List<Card> deck(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }

    /**
     * @param count   number of copies
     * @param factory creates each copy, so no two copies are the same object
     * @return a list of independent card instances
     */
    public static List<Card> copies(int count, Supplier<Card> factory) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(factory.get());
        }
        return cards;
    }

    public static Creature creature(String name, int cost, int attack, int health) {
        return new Creature(name, cost, attack, health);
    }

    public static Spell damageSpell(String name, int cost, int damage) {
        return new Spell(name, cost, new DamageEffect(damage));
    }

    public static Player player(String name, List<Card> deck) {
        return new Player(name, new Deck(deck, false));
    }

    /**
     * Starts a game with two unshuffled decks and a silent log.
     *
     * @param firstDeck  cards for the first player, in draw order
     * @param secondDeck cards for the second player, in draw order
     * @return a game that has already started, with the first player in the main phase
     */
    public static GameEngine startGame(List<Card> firstDeck, List<Card> secondDeck) {
        return startGame(new GameLog(), firstDeck, secondDeck);
    }

    public static GameEngine startGame(GameLog log, List<Card> firstDeck, List<Card> secondDeck) {
        GameEngine engine = new GameEngine(player(FIRST, firstDeck), player(SECOND, secondDeck),
                GameConfig.standard(), log);
        engine.startGame();
        return engine;
    }

    /**
     * Ends the current turn and the opponent's turn, returning to the first player.
     *
     * @param engine the running game
     */
    public static void endRound(GameEngine engine) {
        engine.endTurn();
        engine.endTurn();
    }

    /**
     * @param engine the running game
     * @param rounds how many full rounds to skip
     */
    public static void endRounds(GameEngine engine, int rounds) {
        for (int i = 0; i < rounds; i++) {
            endRound(engine);
        }
    }
}
