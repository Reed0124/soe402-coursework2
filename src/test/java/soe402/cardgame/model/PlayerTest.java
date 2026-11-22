package soe402.cardgame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Player: resources, zones and hero state")
class PlayerTest {

    private Player player() {
        return new Player("Alice", new Deck(List.of(), false));
    }

    @Nested
    @DisplayName("mana")
    class Mana {

        @Test
        void thePoolStartsEmptyAndGrowsByOnePerTurn() {
            Player player = player();
            assertEquals(0, player.getMaxMana());

            player.startTurnMana(10);
            assertEquals(1, player.getMaxMana());
            assertEquals(1, player.getMana());

            player.startTurnMana(10);
            assertEquals(2, player.getMaxMana());
            assertEquals(2, player.getMana());
        }

        @Test
        void thePoolNeverExceedsTheCap() {
            Player player = player();
            for (int i = 0; i < 20; i++) {
                player.startTurnMana(10);
            }
            assertEquals(10, player.getMaxMana());
            assertEquals(10, player.getMana());
        }

        @Test
        void spendingManaReducesThePool() {
            Player player = player();
            player.startTurnMana(10);
            player.spendMana(1);
            assertEquals(0, player.getMana());
            assertFalse(player.canAfford(1));
        }

        @Test
        void thePoolIsRefilledAtTheStartOfEachTurn() {
            Player player = player();
            player.startTurnMana(10);
            player.spendMana(1);
            player.startTurnMana(10);
            assertEquals(2, player.getMana());
        }

        @Test
        void spendingMoreThanAvailableIsRejected() {
            Player player = player();
            player.startTurnMana(10);
            assertThrows(IllegalStateException.class, () -> player.spendMana(2));
            assertEquals(1, player.getMana(), "a failed spend must not change the pool");
        }
    }

    @Nested
    @DisplayName("hero")
    class Hero {

        @Test
        void heroStartsAtThirtyHealth() {
            assertEquals(30, player().getHealth());
            assertEquals(30, player().getMaxHealth());
        }

        @Test
        void healingIsCappedAtMaximumHealth() {
            Player player = player();
            player.takeDamage(5);
            player.heal(100);
            assertEquals(30, player.getHealth());
        }

        @Test
        void aHeroAtZeroHealthIsDead() {
            Player player = player();
            player.takeDamage(30);
            assertTrue(player.isDead());
        }
    }

    @Nested
    @DisplayName("zones")
    class Zones {

        @Test
        void handCannotBeModifiedThroughTheSnapshot() {
            Player player = player();
            player.addToHand(new Creature("Wisp", 0, 1, 1));
            List<Card> hand = player.getHand();
            assertEquals(1, hand.size());
            assertThrows(UnsupportedOperationException.class, () -> hand.add(new Creature("X", 1, 1, 1)));
        }

        @Test
        void removingACardFromTheHandShiftsTheRest() {
            Player player = player();
            player.addToHand(new Creature("A", 1, 1, 1));
            player.addToHand(new Creature("B", 1, 1, 1));
            player.removeFromHand(0);
            assertEquals("B", player.getHand().get(0).getName());
        }

        @Test
        void discardPileKeepsCards() {
            Player player = player();
            Card card = new Creature("Wisp", 0, 1, 1);
            player.addToDiscard(card);
            assertEquals(1, player.getDiscardPile().size());
        }

        @Test
        void fatigueCounterIncreasesByOne() {
            Player player = player();
            assertEquals(0, player.getFatigueCounter());
            assertEquals(1, player.increaseFatigue());
            assertEquals(2, player.increaseFatigue());
        }
    }

    @Test
    @DisplayName("a player needs a name and a deck")
    void constructorValidatesArguments() {
        assertThrows(IllegalArgumentException.class, () -> new Player(" ", new Deck(List.of(), false)));
        assertThrows(NullPointerException.class, () -> new Player("Alice", null));
    }
}
