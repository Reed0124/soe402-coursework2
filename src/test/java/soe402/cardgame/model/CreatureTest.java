package soe402.cardgame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Creature: combat state")
class CreatureTest {

    private Creature bear() {
        return new Creature("Bear", 1, 2, 3);
    }

    @Nested
    @DisplayName("damage and healing")
    class Health {

        @Test
        void damageReducesCurrentHealth() {
            Creature bear = bear();
            bear.takeDamage(2);
            assertEquals(1, bear.getHealth());
            assertFalse(bear.isDead());
        }

        @Test
        void creatureDiesWhenHealthReachesZero() {
            Creature bear = bear();
            bear.takeDamage(3);
            assertEquals(0, bear.getHealth());
            assertTrue(bear.isDead());
        }

        @Test
        void overkillDamageLeavesANegativeHealthValue() {
            Creature bear = bear();
            bear.takeDamage(10);
            assertEquals(-7, bear.getHealth());
            assertTrue(bear.isDead());
        }

        @Test
        void healingIsCappedAtMaximumHealth() {
            Creature bear = bear();
            bear.takeDamage(2);
            bear.heal(50);
            assertEquals(3, bear.getHealth());
        }

        @Test
        void negativeDamageIsRejected() {
            Creature bear = bear();
            assertThrows(IllegalArgumentException.class, () -> bear.takeDamage(-1));
            assertThrows(IllegalArgumentException.class, () -> bear.heal(-1));
        }
    }

    @Nested
    @DisplayName("attacking")
    class Attacking {

        @Test
        void aFreshlyPlayedCreatureCannotAttack() {
            assertFalse(bear().canAttack(), "summoning sickness must block the first turn");
        }

        @Test
        void aCreatureCanAttackFromItsControllersNextTurn() {
            Creature bear = bear();
            bear.beginTurn();
            assertTrue(bear.canAttack());
        }

        @Test
        void aCreatureCannotAttackTwiceInOneTurn() {
            Creature bear = bear();
            bear.beginTurn();
            bear.markAttacked();
            assertFalse(bear.canAttack());
        }

        @Test
        void anAttackFlagIsClearedAtTheStartOfTheNextTurn() {
            Creature bear = bear();
            bear.beginTurn();
            bear.markAttacked();
            bear.beginTurn();
            assertTrue(bear.canAttack());
        }
    }

    @Nested
    @DisplayName("freeze")
    class Frozen {

        @Test
        void aFrozenCreatureMissesExactlyOneTurn() {
            Creature bear = bear();
            bear.beginTurn();
            bear.freeze(1);

            bear.beginTurn();
            assertFalse(bear.canAttack(), "the turn after being frozen must be skipped");
            assertFalse(bear.isFrozen(), "the freeze itself has expired");

            bear.beginTurn();
            assertTrue(bear.canAttack(), "the following turn is normal again");
        }

        @Test
        void freezingRequiresAtLeastOneTurn() {
            Creature bear = bear();
            assertThrows(IllegalArgumentException.class, () -> bear.freeze(0));
        }
    }

    @Nested
    @DisplayName("modifiers")
    class Modifiers {

        @Test
        void anAttackModifierRaisesTheAttackValue() {
            Creature bear = bear();
            bear.addModifier(new StatMod(3, 0, "+3 attack"), 2);
            assertEquals(5, bear.getAttack());
        }

        @Test
        void aHealthModifierRaisesCurrentAndMaximumHealth() {
            Creature bear = bear();
            bear.takeDamage(2);
            bear.addModifier(new StatMod(0, 3, "+3 health"), 2);
            assertEquals(4, bear.getHealth());
            assertEquals(6, bear.getMaxHealth());
        }

        @Test
        void aModifierExpiresAfterItsDuration() {
            Creature bear = bear();
            bear.addModifier(new StatMod(3, 0, "+3 attack"), 2);
            bear.tickModifiers();
            assertEquals(5, bear.getAttack(), "still active after one turn");

            bear.tickModifiers();
            assertEquals(2, bear.getAttack(), "expired after two turns");
            assertTrue(bear.getModifiers().isEmpty());
        }

        @Test
        void attackNeverDropsBelowZero() {
            Creature bear = bear();
            bear.addModifier(new StatMod(-10, 0, "-10 attack"), 1);
            assertEquals(0, bear.getAttack());
        }
    }

    @Test
    @DisplayName("two copies of the same card are independent objects")
    void copiesDoNotShareState() {
        Creature first = new Creature("Yeti", 4, 4, 5);
        Creature second = new Creature("Yeti", 4, 4, 5);
        first.takeDamage(3);
        assertEquals(5, second.getHealth());
    }
}
