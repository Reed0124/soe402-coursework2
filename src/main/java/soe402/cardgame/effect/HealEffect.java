package soe402.cardgame.effect;

import soe402.cardgame.model.Effect;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.GameContext;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.TargetRequirement;

/**
 * Restores health to a friendly character, never above its maximum.
 */
public class HealEffect implements Effect {

    private final int amount;

    /**
     * @param amount health restored, at least one
     */
    public HealEffect(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Healing must be at least one, but was " + amount);
        }
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.FRIENDLY_CHARACTER;
    }

    @Override
    public void resolve(GameContext context, Player caster, GameCharacter target) {
        context.heal(target, amount);
    }

    @Override
    public String describe() {
        return "restores " + amount + " health";
    }
}
