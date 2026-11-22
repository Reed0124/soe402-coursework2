package soe402.cardgame.cards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.model.Card;
import soe402.cardgame.model.Deck;

@DisplayName("Card pool and deck building")
class CardRegistryTest {

    @Test
    @DisplayName("every factory call returns a new object")
    void factoriesReturnFreshInstances() {
        assertNotSame(CardRegistry.chillwindYeti(), CardRegistry.chillwindYeti());
        assertNotSame(CardRegistry.fireball(), CardRegistry.fireball());
        assertNotSame(CardRegistry.blessingOfMight(), CardRegistry.blessingOfMight());
    }

    @Test
    @DisplayName("the standard deck has exactly twenty cards")
    void standardDeckHasTwentyCards() {
        Deck deck = DeckFactory.standardDeck(1L);
        assertEquals(20, deck.size());
    }

    @Test
    @DisplayName("the same seed produces the same deck order")
    void seededDecksAreReproducible() {
        List<Card> first = DeckFactory.standardDeck(99L).remaining();
        List<Card> second = DeckFactory.standardDeck(99L).remaining();

        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).getName(), second.get(i).getName());
        }
    }

    @Test
    @DisplayName("cards in a deck are independent objects")
    void deckCopiesAreIndependent() {
        List<Card> remaining = DeckFactory.standardDeck(5L).remaining();
        long distinct = remaining.stream().distinct().count();
        assertEquals(remaining.size(), distinct, "no card object may appear twice in a deck");
    }

    @Test
    @DisplayName("cards expose a description for the interface")
    void cardsHaveDescriptions() {
        assertTrue(CardRegistry.flamestrike().describe().contains("all enemy creatures"));
        assertTrue(CardRegistry.frostbolt().describe().contains("damage"));
    }
}
