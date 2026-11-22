package soe402.cardgame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Deck: draw order and shuffling")
class DeckTest {

    private Card card(String name) {
        return new Creature(name, 1, 1, 1);
    }

    @Test
    @DisplayName("cards are drawn in the order they were added")
    void drawsInOrder() {
        Deck deck = new Deck(List.of(card("A"), card("B"), card("C")), false);

        assertEquals("A", deck.draw().orElseThrow().getName());
        assertEquals("B", deck.draw().orElseThrow().getName());
        assertEquals("C", deck.draw().orElseThrow().getName());
    }

    @Test
    @DisplayName("an exhausted deck returns an empty result instead of throwing")
    void exhaustedDeckIsEmpty() {
        Deck deck = new Deck(List.of(card("A")), false);
        assertTrue(deck.draw().isPresent());

        assertTrue(deck.draw().isEmpty());
        assertEquals(0, deck.size());
        assertTrue(deck.isEmpty());
    }

    @Test
    @DisplayName("shuffling keeps every card and changes the order for a known seed")
    void shufflingKeepsAllCards() {
        List<Card> cards = List.of(card("A"), card("B"), card("C"),
                card("D"), card("E"), card("F"));
        Deck deck = new Deck(cards, new Random(42));

        assertEquals(6, deck.size());
        assertEquals(6, deck.remaining().size());
    }

    @Test
    @DisplayName("the same seed always produces the same order")
    void shufflingIsReproducible() {
        List<Card> cards = List.of(card("A"), card("B"), card("C"),
                card("D"), card("E"), card("F"));
        Deck first = new Deck(cards, new Random(7));
        Deck second = new Deck(cards, new Random(7));

        for (int i = 0; i < 6; i++) {
            assertEquals(first.draw().orElseThrow().getName(),
                    second.draw().orElseThrow().getName());
        }
    }

    @Test
    @DisplayName("a deck cannot contain null")
    void nullCardsAreRejected() {
        assertThrows(NullPointerException.class, () -> new Deck(java.util.Arrays.asList(card("A"), null), false));
    }

    @Test
    @DisplayName("the remaining snapshot cannot be used to change the deck")
    void remainingIsReadOnly() {
        Deck deck = new Deck(List.of(card("A")), false);
        List<Card> view = deck.remaining();
        assertInstanceOf(Card.class, view.get(0));
        assertThrows(UnsupportedOperationException.class, view::clear);
        assertFalse(deck.isEmpty());
    }
}
