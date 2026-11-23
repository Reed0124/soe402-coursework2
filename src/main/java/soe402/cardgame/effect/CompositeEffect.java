package soe402.cardgame.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import soe402.cardgame.model.Effect;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.GameContext;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.TargetRequirement;

/**
 * Runs several effects in order and reports a single combined target requirement.
 *
 * <p>The Composite pattern keeps simple effects small: a frostbolt is
 * {@code new CompositeEffect(new DamageEffect(3), new FreezeEffect(1))} instead of a new class
 * that duplicates the damage logic. A target is legal only when every child effect accepts it.</p>
 */
public class CompositeEffect implements Effect {

    private final List<Effect> effects;

    /**
     * @param effects the effects to run, in order; at least one
     */
    public CompositeEffect(Effect... effects) {
        this(List.of(effects));
    }

    /**
     * @param effects the effects to run, in order; at least one
     */
    public CompositeEffect(List<Effect> effects) {
        if (effects == null || effects.isEmpty()) {
            throw new IllegalArgumentException("A composite effect needs at least one child effect");
        }
        this.effects = List.copyOf(new ArrayList<>(effects));
    }

    /**
     * @return the child effects, in resolution order
     */
    public List<Effect> getEffects() {
        return effects;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The requirement reported here is only a hint: it is the first child requirement that
     * is not {@link TargetRequirement#NONE}, while {@link #isValidTarget} requires
     * <em>every</em> child to accept the target. A user interface must therefore call
     * {@code isValidTarget} for each candidate instead of trusting this value alone.</p>
     */
    @Override
    public TargetRequirement getTargetRequirement() {
        return effects.stream()
                .map(Effect::getTargetRequirement)
                .filter(requirement -> requirement != TargetRequirement.NONE)
                .findFirst()
                .orElse(TargetRequirement.NONE);
    }

    @Override
    public boolean isValidTarget(Player caster, GameCharacter target) {
        return effects.stream().allMatch(effect -> effect.isValidTarget(caster, target));
    }

    /**
     * Resolves the children in order and stops as soon as the game is over.
     *
     * <p>Without the check a lethal first effect would be followed by, say, a heal, and a hero
     * who has already lost the game would be restored to positive health while the winner had
     * already been decided.</p>
     */
    @Override
    public void resolve(GameContext context, Player caster, GameCharacter target) {
        for (Effect effect : effects) {
            if (context.isFinished()) {
                return;
            }
            effect.resolve(context, caster, target);
        }
    }

    @Override
    public String describe() {
        return effects.stream().map(Effect::describe).collect(Collectors.joining(", then "));
    }
}
