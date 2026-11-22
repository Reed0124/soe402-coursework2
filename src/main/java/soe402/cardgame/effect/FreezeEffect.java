package soe402.cardgame.effect;

import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Effect;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.GameContext;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.TargetRequirement;

/**
 * Prevents a creature from attacking for a number of turns.
 *
 * <p>Freezing a hero is legal but has no effect, so a card that combines damage and freeze
 * (such as a frostbolt) can be aimed at any character exactly like a plain damage spell.</p>
 */
public class FreezeEffect implements Effect {

    private final int turns;

    /**
     * @param turns number of turns the creature cannot attack, at least one
     */
    public FreezeEffect(int turns) {
        if (turns < 1) {
            throw new IllegalArgumentException("Freeze must last at least one turn, but was " + turns);
        }
        this.turns = turns;
    }

    public int getTurns() {
        return turns;
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ANY_CHARACTER;
    }

    @Override
    public void resolve(GameContext context, Player caster, GameCharacter target) {
        if (target instanceof Creature creature && !creature.isDead()) {
            creature.freeze(turns);
            context.log(creature.getDisplayName() + " is frozen for " + turns + " turn(s)");
        }
    }

    @Override
    public String describe() {
        return "freezes the target for " + turns + " turn(s)";
    }
}
