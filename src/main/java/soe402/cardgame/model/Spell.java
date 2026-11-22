package soe402.cardgame.model;

import java.util.Objects;

/**
 * A one-off spell. It holds an {@link Effect} and delegates both target validation and
 * resolution to it, so subclasses are not needed for individual spells.
 */
public class Spell extends Card {

    private final Effect effect;

    /**
     * @param name     display name of the spell
     * @param manaCost mana cost, zero or more
     * @param effect   the effect resolved when the spell is played
     */
    public Spell(String name, int manaCost, Effect effect) {
        super(name, manaCost, CardType.SPELL);
        this.effect = Objects.requireNonNull(effect, "effect must not be null");
    }

    public Effect getEffect() {
        return effect;
    }

    @Override
    public void play(GameContext context, Player controller, GameCharacter target) {
        effect.resolve(context, controller, target);
    }

    @Override
    public boolean isValidTarget(Player controller, GameCharacter target) {
        return effect.isValidTarget(controller, target);
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return effect.getTargetRequirement();
    }

    @Override
    public String describe() {
        return effect.describe();
    }
}
