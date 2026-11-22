package soe402.cardgame.cards;

import soe402.cardgame.effect.AreaDamageEffect;
import soe402.cardgame.effect.CompositeEffect;
import soe402.cardgame.effect.DamageEffect;
import soe402.cardgame.effect.DrawEffect;
import soe402.cardgame.effect.FreezeEffect;
import soe402.cardgame.effect.HealEffect;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Modifier;
import soe402.cardgame.model.Spell;
import soe402.cardgame.model.StatMod;

/**
 * The card pool.
 *
 * <p>Every method returns a <em>new</em> card object. That is deliberate: deck building calls
 * these factories once per physical copy, so two copies of a yeti are two independent objects
 * and damaging one never damages the other. There is no shared static card instance in the
 * whole project.</p>
 *
 * <p>Adding a card means adding one factory method; no existing class needs to change.</p>
 */
public final class CardRegistry {

    private CardRegistry() {
    }

    // ---------------------------------------------------------------- creatures

    public static Creature wisp() {
        return new Creature("Wisp", 0, 1, 1);
    }

    public static Creature riverCrocolisk() {
        return new Creature("River Crocolisk", 2, 2, 3);
    }

    public static Creature chillwindYeti() {
        return new Creature("Chillwind Yeti", 4, 4, 5);
    }

    public static Creature boulderfistOgre() {
        return new Creature("Boulderfist Ogre", 6, 6, 7);
    }

    // ---------------------------------------------------------------- spells

    public static Spell fireball() {
        return new Spell("Fireball", 4, new DamageEffect(6));
    }

    public static Spell frostbolt() {
        return new Spell("Frostbolt", 2, new CompositeEffect(new DamageEffect(3), new FreezeEffect(1)));
    }

    public static Spell arcaneIntellect() {
        return new Spell("Arcane Intellect", 3, new DrawEffect(2));
    }

    public static Spell healingTouch() {
        return new Spell("Healing Touch", 3, new HealEffect(8));
    }

    public static Spell flamestrike() {
        return new Spell("Flamestrike", 7, new AreaDamageEffect(4));
    }

    // ---------------------------------------------------------------- modifiers

    public static Modifier blessingOfMight() {
        return new Modifier("Blessing of Might", 1, new StatMod(3, 0, "+3 attack"), 2);
    }

    public static Modifier fortitude() {
        return new Modifier("Fortitude", 1, new StatMod(0, 3, "+3 health"), 2);
    }
}
