package soe402.cardgame.effect;

import java.util.List;

import soe402.cardgame.model.Creature;
import soe402.cardgame.model.Effect;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.GameContext;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.TargetRequirement;

/**
 * Deals damage to every enemy creature. Heroes are untouched.
 *
 * <p>This is the only effect that needs to look at the board rather than at a single target,
 * which is why {@link GameContext} exposes {@link GameContext#creaturesOwnedBy(Player)}. The
 * list is a snapshot, so creatures that die during resolution do not disturb the iteration.</p>
 */
public class AreaDamageEffect implements Effect {

    private final int amount;

    /**
     * @param amount damage dealt to each enemy creature, at least one
     */
    public AreaDamageEffect(int amount) {
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
        return TargetRequirement.NONE;
    }

    @Override
    public void resolve(GameContext context, Player caster, GameCharacter target) {
        Player opponent = context.opponentOf(caster);
        List<Creature> enemies = context.creaturesOwnedBy(opponent);
        context.log(caster.getName() + " deals " + amount + " damage to all enemy creatures");
        for (Creature enemy : enemies) {
            context.dealDamage(enemy, amount, caster);
        }
    }

    @Override
    public String describe() {
        return "deals " + amount + " damage to all enemy creatures";
    }
}
