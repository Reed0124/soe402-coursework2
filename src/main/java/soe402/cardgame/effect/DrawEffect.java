package soe402.cardgame.effect;

import soe402.cardgame.model.Effect;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.GameContext;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.TargetRequirement;

/**
 * Draws cards for the caster. It needs no target, which is why
 * {@link TargetRequirement#NONE} exists.
 */
public class DrawEffect implements Effect {

    private final int amount;

    /**
     * @param amount number of cards to draw, at least one
     */
    public DrawEffect(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Draw amount must be at least one, but was " + amount);
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
        context.drawCards(caster, amount);
    }

    @Override
    public String describe() {
        return amount == 1 ? "draws a card" : "draws " + amount + " cards";
    }
}
