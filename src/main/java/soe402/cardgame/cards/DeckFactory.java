package soe402.cardgame.cards;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import soe402.cardgame.model.Card;
import soe402.cardgame.model.Deck;

/**
 * Builds the twenty card deck used by both players in the demo game.
 *
 * <p>The deck is assembled from {@link CardRegistry} factories, so every copy is a distinct
 * object, and then shuffled with a caller supplied {@link Random}. Passing a seeded random
 * makes the whole demo reproducible, which is what the integration test relies on.</p>
 */
public final class DeckFactory {

    private DeckFactory() {
    }

    /**
     * @param random the random source used to shuffle the deck
     * @return a shuffled twenty card deck
     */
    public static Deck standardDeck(Random random) {
        List<Card> cards = new ArrayList<>();
        addCopies(cards, 2, CardRegistry::wisp);
        addCopies(cards, 3, CardRegistry::riverCrocolisk);
        addCopies(cards, 2, CardRegistry::chillwindYeti);
        addCopies(cards, 1, CardRegistry::boulderfistOgre);
        addCopies(cards, 2, CardRegistry::fireball);
        addCopies(cards, 2, CardRegistry::frostbolt);
        addCopies(cards, 2, CardRegistry::arcaneIntellect);
        addCopies(cards, 2, CardRegistry::healingTouch);
        addCopies(cards, 1, CardRegistry::flamestrike);
        addCopies(cards, 2, CardRegistry::blessingOfMight);
        addCopies(cards, 1, CardRegistry::fortitude);
        return new Deck(cards, random);
    }

    /**
     * @param seed the seed for the shuffle
     * @return a shuffled twenty card deck that is reproducible for a given seed
     */
    public static Deck standardDeck(long seed) {
        return standardDeck(new Random(seed));
    }

    private static void addCopies(List<Card> cards, int count, Supplier<Card> factory) {
        for (int i = 0; i < count; i++) {
            cards.add(factory.get());
        }
    }
}
