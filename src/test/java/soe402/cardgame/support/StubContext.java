package soe402.cardgame.support;

import java.util.ArrayList;
import java.util.List;

import soe402.cardgame.model.Creature;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.GameContext;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.StatMod;

/**
 * A stand-in {@link GameContext} that records what a card asked for.
 *
 * <p>Using a stub instead of a real game is what makes effect tests small and precise: a test
 * of {@code DamageEffect} should not have to build a deck, start a turn and draw a card in
 * order to check that six damage means six damage.</p>
 */
public final class StubContext implements GameContext {

    public final List<String> messages = new ArrayList<>();
    public final List<DamageCall> damages = new ArrayList<>();
    public final List<HealCall> heals = new ArrayList<>();
    public final List<DrawCall> draws = new ArrayList<>();
    public final List<Creature> summoned = new ArrayList<>();
    public final List<String> modifiers = new ArrayList<>();

    private Player opponent;
    private boolean finished;

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public void dealDamage(GameCharacter target, int amount, GameCharacter source) {
        damages.add(new DamageCall(target, amount, source));
        target.takeDamage(amount);
    }

    @Override
    public void heal(GameCharacter target, int amount) {
        heals.add(new HealCall(target, amount));
        target.heal(amount);
    }

    @Override
    public void drawCards(Player player, int amount) {
        draws.add(new DrawCall(player, amount));
    }

    @Override
    public void summon(Player controller, Creature creature) {
        summoned.add(creature);
        controller.addToBoard(creature);
    }

    @Override
    public void attachModifier(Creature target, StatMod statMod, int duration) {
        modifiers.add(statMod.label() + " for " + duration);
        target.addModifier(statMod, duration);
    }

    @Override
    public Player opponentOf(Player player) {
        if (opponent == null) {
            throw new IllegalStateException("The stub has no opponent configured");
        }
        return opponent;
    }

    @Override
    public List<Creature> creaturesOwnedBy(Player player) {
        return player.getBoard();
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void log(String message) {
        messages.add(message);
    }

    public record DamageCall(GameCharacter target, int amount, GameCharacter source) {
    }

    public record HealCall(GameCharacter target, int amount) {
    }

    public record DrawCall(Player player, int amount) {
    }
}
