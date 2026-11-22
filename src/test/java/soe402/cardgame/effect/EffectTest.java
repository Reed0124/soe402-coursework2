package soe402.cardgame.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Deck;
import soe402.cardgame.model.Modifier;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.Spell;
import soe402.cardgame.model.StatMod;
import soe402.cardgame.support.StubContext;

@DisplayName("Effects: resolution and target rules")
class EffectTest {

    private StubContext context;
    private Player caster;
    private Player opponent;

    @BeforeEach
    void setUp() {
        context = new StubContext();
        caster = new Player("Alice", new Deck(List.of(), false));
        opponent = new Player("Bob", new Deck(List.of(), false));
        context.setOpponent(opponent);
    }

    @Test
    @DisplayName("damage removes exactly the stated health")
    void damageDealsItsAmount() {
        new DamageEffect(6).resolve(context, caster, opponent);

        assertEquals(24, opponent.getHealth());
        assertEquals(1, context.damages.size());
        assertEquals(6, context.damages.get(0).amount());
        assertEquals(caster, context.damages.get(0).source());
    }

    @Test
    @DisplayName("damage needs a target")
    void damageRequiresATarget() {
        assertFalse(new DamageEffect(6).isValidTarget(caster, null));
        assertTrue(new DamageEffect(6).isValidTarget(caster, opponent));
    }

    @Test
    @DisplayName("a damage effect of zero is a programming error")
    void damageMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new DamageEffect(0));
    }

    @Test
    @DisplayName("healing restores health but never above the maximum")
    void healingIsCapped() {
        opponent.takeDamage(10);

        new HealEffect(4).resolve(context, caster, opponent);
        assertEquals(24, opponent.getHealth());

        new HealEffect(100).resolve(context, caster, opponent);
        assertEquals(30, opponent.getHealth());
    }

    @Test
    @DisplayName("healing only accepts friendly characters")
    void healingNeedsAFriendlyTarget() {
        assertTrue(new HealEffect(3).isValidTarget(caster, caster));
        assertFalse(new HealEffect(3).isValidTarget(caster, opponent));
        assertFalse(new HealEffect(3).isValidTarget(caster, null));
    }

    @Test
    @DisplayName("drawing asks the engine for the right number of cards")
    void drawingUsesTheContext() {
        new DrawEffect(2).resolve(context, caster, null);

        assertEquals(1, context.draws.size());
        assertEquals(caster, context.draws.get(0).player());
        assertEquals(2, context.draws.get(0).amount());
    }

    @Test
    @DisplayName("drawing needs no target at all")
    void drawingNeedsNoTarget() {
        assertTrue(new DrawEffect(1).isValidTarget(caster, null));
    }

    @Test
    @DisplayName("freezing stops a creature from attacking")
    void freezingACreature() {
        Creature bear = new Creature("Bear", 1, 2, 2);
        bear.beginTurn();
        assertTrue(bear.canAttack());

        new FreezeEffect(1).resolve(context, caster, bear);

        assertFalse(bear.canAttack());
        assertTrue(bear.isFrozen());
    }

    @Test
    @DisplayName("freezing a hero is legal but has no effect")
    void freezingAHeroDoesNothing() {
        new FreezeEffect(1).resolve(context, caster, opponent);

        assertEquals(30, opponent.getHealth());
        assertEquals(0, context.messages.size());
    }

    @Test
    @DisplayName("a dead creature is not frozen")
    void deadCreaturesAreNotFrozen() {
        Creature bear = new Creature("Bear", 1, 2, 2);
        bear.takeDamage(5);

        new FreezeEffect(1).resolve(context, caster, bear);

        assertFalse(bear.isFrozen());
    }

    @Test
    @DisplayName("area damage hits every enemy creature and nothing else")
    void areaDamageHitsOnlyEnemies() {
        Creature first = new Creature("A", 1, 1, 3);
        Creature second = new Creature("B", 1, 1, 3);
        Creature own = new Creature("C", 1, 1, 3);
        opponent.addToBoard(first);
        opponent.addToBoard(second);
        caster.addToBoard(own);

        new AreaDamageEffect(2).resolve(context, caster, null);

        assertEquals(1, first.getHealth());
        assertEquals(1, second.getHealth());
        assertEquals(3, own.getHealth(), "the caster's own board is untouched");
        assertEquals(30, opponent.getHealth(), "heroes are not hit by area damage");
    }

    @Test
    @DisplayName("a composite applies each child effect in order")
    void compositeAppliesEveryChild() {
        Creature bear = new Creature("Bear", 1, 2, 5);
        bear.beginTurn();

        new CompositeEffect(new DamageEffect(3), new FreezeEffect(1)).resolve(context, caster, bear);

        assertEquals(2, bear.getHealth());
        assertTrue(bear.isFrozen());
    }

    @Test
    @DisplayName("a composite is only valid when every child accepts the target")
    void compositeCombinesTargetRules() {
        CompositeEffect damageAndHeal = new CompositeEffect(new DamageEffect(3), new HealEffect(3));

        assertFalse(damageAndHeal.isValidTarget(caster, opponent), "heal rejects an enemy");
        assertTrue(damageAndHeal.isValidTarget(caster, caster));
    }

    @Test
    @DisplayName("a composite needs at least one child")
    void compositeNeedsChildren() {
        assertThrows(IllegalArgumentException.class, CompositeEffect::new);
    }

    @Test
    @DisplayName("a spell delegates its target rules to its effect")
    void spellDelegatesToItsEffect() {
        Spell heal = new Spell("Healing Light", 1, new HealEffect(3));
        Spell bolt = new Spell("Bolt", 1, new DamageEffect(3));

        assertFalse(heal.isValidTarget(caster, opponent));
        assertTrue(bolt.isValidTarget(caster, opponent));
    }

    @Test
    @DisplayName("a modifier attaches its stat change to the target")
    void modifierAttachesItsStatChange() {
        Creature bear = new Creature("Bear", 1, 2, 2);
        caster.addToBoard(bear);
        Modifier blessing = new Modifier("Blessing", 1, new StatMod(3, 0, "+3 attack"), 2);

        blessing.play(context, caster, bear);

        assertEquals(5, bear.getAttack());
        assertEquals(1, context.modifiers.size());
        assertEquals("+3 attack for 2", context.modifiers.get(0));
    }
}
