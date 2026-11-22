package soe402.cardgame.effect;

import soe402.cardgame.model.Effect;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.GameContext;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.TargetRequirement;

/**
 * Deals a fixed amount of damage to a single character.
 *
 * <p>Damage is routed through {@link GameContext#dealDamage} rather than applied directly, so
 * death checks, the game log and the win condition all happen in one place.</p>
 */
public class DamageEffect implements Effect {

    private final int amount;

    /**
     * @param amount damage dealt, at least one
     */
    public DamageEffect(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Damage must be at least one, but was " + amount);
        }
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ANY_CHARACTER;
    }

    @Override
    public void resolve(GameContext context, Player caster, GameCharacter target) {
        context.dealDamage(target, amount, caster);
    }

    @Override
    public String describe() {
        return "deals " + amount + " damage";
    }
}
