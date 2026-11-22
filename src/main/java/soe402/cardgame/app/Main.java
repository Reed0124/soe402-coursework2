package soe402.cardgame.app;

import java.util.ArrayList;
import java.util.List;

import soe402.cardgame.cards.DeckFactory;
import soe402.cardgame.effect.HealEffect;
import soe402.cardgame.engine.GameConfig;
import soe402.cardgame.engine.GameEngine;
import soe402.cardgame.log.GameLog;
import soe402.cardgame.model.Card;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.Spell;
import soe402.cardgame.model.TargetRequirement;

/**
 * A scripted demonstration of the engine.
 *
 * <p>Two players with seeded decks play against each other using a deliberately simple policy:
 * play every card that can legally be played, then attack the enemy hero with every creature
 * that is able to. The point is not to be a good AI but to exercise every part of the engine -
 * turns, mana, combat, spells, modifiers, fatigue and the win condition - and print a readable
 * log of the result.</p>
 *
 * <p>Because the decks are seeded, the same seed always produces the same game.</p>
 */
public final class Main {

    private static final int MAX_TURNS = 60;

    private Main() {
    }

    /**
     * @param args optionally a single seed for the shuffle
     */
    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 2026L;
        GameEngine engine = newGame(seed, new GameLog(System.out));

        System.out.println("SOE402 card game engine - demo (seed " + seed + ")");
        System.out.println();

        run(engine);

        System.out.println();
        System.out.println("=== Result ===");
        engine.getWinner().ifPresentOrElse(
                winner -> System.out.println("Winner: " + winner.getName()),
                () -> System.out.println("No winner - the turn limit was reached"));
        for (Player player : engine.getPlayers()) {
            System.out.println(player);
        }
        System.out.println("Turns played: " + engine.getTurnNumber());
    }

    /**
     * Creates a game with two seeded decks.
     *
     * @param seed the seed used for both decks
     * @param log  the log the game writes to
     * @return a game that has not been started yet
     */
    public static GameEngine newGame(long seed, GameLog log) {
        Player first = new Player("Alice", DeckFactory.standardDeck(seed));
        Player second = new Player("Bob", DeckFactory.standardDeck(seed + 1));
        return new GameEngine(first, second, GameConfig.standard(), log);
    }

    /**
     * Plays a whole game with the simple policy described above.
     *
     * @param engine the game to play, not yet started
     */
    public static void run(GameEngine engine) {
        engine.startGame();
        while (!engine.isFinished() && engine.getTurnNumber() <= MAX_TURNS) {
            takeTurn(engine);
        }
    }

    private static void takeTurn(GameEngine engine) {
        boolean acted = true;
        while (acted && !engine.isFinished()) {
            acted = playSomething(engine) || attackSomething(engine);
        }
        if (!engine.isFinished()) {
            engine.endTurn();
        }
    }

    private static boolean playSomething(GameEngine engine) {
        Player active = engine.getCurrentPlayer();
        for (int index = 0; index < active.getHand().size(); index++) {
            Card card = active.getHand().get(index);
            if (isWastedHeal(active, card)) {
                continue;
            }
            for (GameCharacter target : candidateTargets(engine, active, card)) {
                if (engine.playCard(index, target).success()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The demo policy is intentionally simple, but it avoids the one obviously pointless play:
     * a healing spell when everything on its side is already at full health.
     */
    private static boolean isWastedHeal(Player active, Card card) {
        if (!(card instanceof Spell spell) || !(spell.getEffect() instanceof HealEffect)) {
            return false;
        }
        if (active.getHealth() < active.getMaxHealth()) {
            return false;
        }
        return active.getBoard().stream().noneMatch(creature -> creature.getHealth() < creature.getMaxHealth());
    }

    private static boolean attackSomething(GameEngine engine) {
        Player active = engine.getCurrentPlayer();
        Player enemy = engine.getOpponent();
        List<Creature> board = active.getBoard();
        for (int index = 0; index < board.size(); index++) {
            if (board.get(index).canAttack() && engine.attack(index, enemy).success()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lists the targets worth trying for a card, most useful first. Friendly cards look at the
     * caster's own side first, everything else looks at the enemy first, and {@code null} is
     * always tried first so that cards without a target are never blocked.
     */
    private static List<GameCharacter> candidateTargets(GameEngine engine, Player active, Card card) {
        List<GameCharacter> friends = new ArrayList<>(active.getBoard());
        friends.add(active);
        List<GameCharacter> enemies = new ArrayList<>(engine.opponentOf(active).getBoard());
        enemies.add(engine.getOpponent());

        List<GameCharacter> targets = new ArrayList<>();
        targets.add(null);
        TargetRequirement requirement = card.getTargetRequirement();
        if (requirement == TargetRequirement.FRIENDLY_CHARACTER
                || requirement == TargetRequirement.FRIENDLY_CREATURE) {
            targets.addAll(friends);
            targets.addAll(enemies);
        } else {
            targets.addAll(enemies);
            targets.addAll(friends);
        }
        return targets;
    }
}
