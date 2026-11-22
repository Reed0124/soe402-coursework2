package soe402.cardgame.model;

import java.util.Objects;

/**
 * A card that attaches a timed {@link StatMod} to a friendly creature.
 *
 * <p>Modifiers are the "persistent effect" category from the specification. The card itself is
 * discarded immediately after being played; the lasting part is the stat change stored on the
 * creature, which the engine ticks down at the end of its controller's turn.</p>
 */
public class Modifier extends Card {

    private final StatMod statMod;
    private final int duration;

    /**
     * @param name     display name of the card
     * @param manaCost mana cost, zero or more
     * @param statMod  the stat change to attach
     * @param duration number of turns the change lasts, at least one
     */
    public Modifier(String name, int manaCost, StatMod statMod, int duration) {
        super(name, manaCost, CardType.MODIFIER);
        if (duration < 1) {
            throw new IllegalArgumentException("A modifier must last at least one turn");
        }
        this.statMod = Objects.requireNonNull(statMod, "statMod must not be null");
        this.duration = duration;
    }

    public StatMod getStatMod() {
        return statMod;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public void play(GameContext context, Player controller, GameCharacter target) {
        if (!(target instanceof Creature creature)) {
            throw new IllegalArgumentException("A modifier needs a creature target");
        }
        context.attachModifier(creature, statMod, duration);
    }

    @Override
    public boolean isValidTarget(Player controller, GameCharacter target) {
        return target instanceof Creature creature && creature.getOwner() == controller;
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.FRIENDLY_CREATURE;
    }

    @Override
    public String describe() {
        return statMod.label() + " for " + duration + " turns";
    }
}
