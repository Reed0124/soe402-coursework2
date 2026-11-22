package soe402.cardgame.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * A player's draw pile.
 *
 * <p>The order is decided when the deck is built, which is what makes the engine testable:
 * production code passes a {@link Random} and gets a shuffled deck, while tests pass
 * {@code shuffle = false} and get an exactly known draw order. No card ever calls
 * {@code new Random()} itself, so a seeded game replays identically.</p>
 */
public final class Deck {

    private final Deque<Card> cards = new ArrayDeque<>();

    /**
     * Creates a shuffled deck using a fresh, unseeded random source.
     *
     * @param cards the cards in their pre-shuffle order
     */
    public Deck(List<Card> cards) {
        this(cards, new Random());
    }

    /**
     * @param cards   the cards in their pre-shuffle order
     * @param shuffle whether to shuffle the cards
     */
    public Deck(List<Card> cards, boolean shuffle) {
        this(cards, shuffle ? new Random() : null);
    }

    /**
     * @param cards  the cards in their pre-shuffle order
     * @param random the random source used for shuffling, or {@code null} to keep the order
     */
    public Deck(List<Card> cards, Random random) {
        Objects.requireNonNull(cards, "cards must not be null");
        List<Card> copy = new ArrayList<>(cards);
        for (Card card : copy) {
            Objects.requireNonNull(card, "a deck must not contain null cards");
        }
        if (random != null) {
            Collections.shuffle(copy, random);
        }
        this.cards.addAll(copy);
    }

    /**
     * Draws the top card.
     *
     * @return the card, or empty when the deck is exhausted
     */
    public Optional<Card> draw() {
        return cards.isEmpty() ? Optional.empty() : Optional.of(cards.pollFirst());
    }

    /**
     * @return the number of cards still in the deck
     */
    public int size() {
        return cards.size();
    }

    /**
     * @return {@code true} when there is nothing left to draw
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * @return an immutable snapshot of the remaining cards, top card first
     */
    public List<Card> remaining() {
        return List.copyOf(cards);
    }
}
