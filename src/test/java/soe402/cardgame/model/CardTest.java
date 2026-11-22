package soe402.cardgame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import soe402.cardgame.effect.DamageEffect;

@DisplayName("Card: construction and metadata")
class CardTest {

    @Nested
    @DisplayName("invalid input is rejected")
    class InvalidInput {

        @Test
        void blankNameIsRejected() {
            assertThrows(IllegalArgumentException.class, () -> new Creature("  ", 1, 1, 1));
            assertThrows(IllegalArgumentException.class, () -> new Creature(null, 1, 1, 1));
        }

        @Test
        void negativeManaCostIsRejected() {
            assertThrows(IllegalArgumentException.class, () -> new Creature("Ghost", -1, 1, 1));
        }

        @Test
        void negativeAttackIsRejected() {
            assertThrows(IllegalArgumentException.class, () -> new Creature("Ghost", 1, -1, 1));
        }

        @Test
        void nonPositiveHealthIsRejected() {
            assertThrows(IllegalArgumentException.class, () -> new Creature("Ghost", 1, 1, 0));
        }

        @Test
        void zeroCostIsAllowed() {
            Creature wisp = new Creature("Wisp", 0, 1, 1);
            assertEquals(0, wisp.getManaCost());
        }
    }

    @Nested
    @DisplayName("metadata")
    class Metadata {

        @Test
        void creatureExposesItsValues() {
            Creature yeti = new Creature("Chillwind Yeti", 4, 4, 5);
            assertEquals("Chillwind Yeti", yeti.getName());
            assertEquals(4, yeti.getManaCost());
            assertEquals(CardType.CREATURE, yeti.getType());
            assertTrue(yeti.describe().contains("4/5"));
        }

        @Test
        void spellTakesItsDescriptionFromTheEffect() {
            Spell fireball = new Spell("Fireball", 4, new DamageEffect(6));
            assertEquals(CardType.SPELL, fireball.getType());
            assertEquals("deals 6 damage", fireball.describe());
        }

        @Test
        void modifierReportsItsStatChange() {
            Modifier blessing = new Modifier("Blessing of Might", 1, new StatMod(3, 0, "+3 attack"), 2);
            assertEquals(CardType.MODIFIER, blessing.getType());
            assertTrue(blessing.describe().contains("+3 attack"));
            assertTrue(blessing.describe().contains("2 turns"));
        }
    }
}
