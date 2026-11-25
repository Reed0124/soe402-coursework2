package soe402.cardgame.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import soe402.cardgame.cards.DeckFactory;
import soe402.cardgame.effect.HealEffect;
import soe402.cardgame.engine.ActionResult;
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
 * Console entry point for the scripted demo and local two-player matches.
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
    private static final long DEFAULT_SEED = 2026L;

    private Main() {
    }

    /**
     * @param args use {@code [seed]} for the demo or {@code --play [seed]} for local play
     */
    public static void main(String[] args) {
        boolean manual = args.length > 0 && args[0].equals("--play");
        int seedIndex = manual ? 1 : 0;
        if (args.length > seedIndex + 1) {
            printUsage();
            return;
        }

        long seed = DEFAULT_SEED;
        if (args.length > seedIndex) {
            try {
                seed = Long.parseLong(args[seedIndex]);
            } catch (NumberFormatException invalidSeed) {
                printUsage();
                return;
            }
        }

        GameEngine engine = newGame(seed, new GameLog(System.out));
        if (manual) {
            System.out.println("SOE402 card game engine - local two-player game (seed " + seed + ")");
            try (Scanner input = new Scanner(System.in)) {
                playLocally(engine, input);
            }
            return;
        }
        System.out.println("SOE402 card game engine - demo (seed " + seed + ")");
        System.out.println();
        run(engine);
        printResult(engine);
    }

    private static void printUsage() {
        System.out.println("Usage: java -cp target/classes soe402.cardgame.app.Main [seed]");
        System.out.println("   or: java -cp target/classes soe402.cardgame.app.Main --play [seed]");
    }

    private static void printResult(GameEngine engine) {
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

    private static void playLocally(GameEngine engine, Scanner input) {
        System.out.println("Share the keyboard and take turns. Type help for commands.");
        engine.startGame();
        printState(engine);

        while (!engine.isFinished()) {
            System.out.print(engine.getCurrentPlayer().getName() + " > ");
            System.out.flush();
            if (!input.hasNextLine()) {
                return;
            }
            String line = input.nextLine().strip();
            if (line.isEmpty()) {
                continue;
            }
            String[] words = line.toLowerCase(Locale.ROOT).split("\\s+");
            if (words.length > 1 && !words[0].equals("play") && !words[0].equals("attack")) {
                System.out.println("Unexpected arguments. Type help for examples.");
                continue;
            }
            switch (words[0]) {
                case "help" -> printCommands();
                case "state" -> printState(engine);
                case "quit" -> {
                    System.out.println("Game closed.");
                    return;
                }
                case "end" -> {
                    if (!passKeyboard(input, engine.getOpponent())) {
                        return;
                    }
                    showActionResult(engine, engine.endTurn());
                }
                case "play", "attack" -> handleCardOrAttack(engine, words);
                default -> System.out.println("Unknown command. Type help for examples.");
            }
        }
        printResult(engine);
    }

    private static boolean passKeyboard(Scanner input, Player nextPlayer) {
        // Move the previous hand off screen before the next player's draw is logged.
        System.out.print(System.lineSeparator().repeat(35));
        System.out.println("Pass the keyboard to " + nextPlayer.getName() + ". Press Enter when ready.");
        if (!input.hasNextLine()) {
            return false;
        }
        input.nextLine();
        return true;
    }

    private static void handleCardOrAttack(GameEngine engine, String[] words) {
        boolean playing = words[0].equals("play");
        if (words.length < (playing ? 2 : 3) || words.length > 4) {
            System.out.println("Missing or extra arguments. Type help for examples.");
            return;
        }

        int index;
        GameCharacter target;
        try {
            index = displayIndex(words[1]);
            target = selectTarget(engine, words);
        } catch (IllegalArgumentException invalidInput) {
            System.out.println("Invalid command: " + invalidInput.getMessage());
            return;
        }

        ActionResult result = playing ? engine.playCard(index, target) : engine.attack(index, target);
        showActionResult(engine, result);
    }

    // Console lists are numbered from 1, while the engine uses zero-based indexes.
    private static int displayIndex(String value) {
        try {
            int number = Integer.parseInt(value);
            if (number > 0) {
                return number - 1;
            }
        } catch (NumberFormatException ignored) {
            // Report the same usage error for text and out-of-range numbers.
        }
        throw new IllegalArgumentException("Use a positive whole number for the card or creature.");
    }

    private static GameCharacter selectTarget(GameEngine engine, String[] words) {
        if (words.length == 2) {
            return null;
        }
        Player active = engine.getCurrentPlayer();
        Player opponent = engine.getOpponent();
        if (words.length == 3) {
            return switch (words[2]) {
                case "self" -> active;
                case "enemy" -> opponent;
                default -> throw new IllegalArgumentException("Use self, enemy, ally N, or enemy N.");
            };
        }
        List<Creature> board = switch (words[2]) {
            case "ally" -> active.getBoard();
            case "enemy" -> opponent.getBoard();
            default -> throw new IllegalArgumentException("Use ally N or enemy N for a creature.");
        };
        int targetIndex = displayIndex(words[3]);
        if (targetIndex >= board.size() || board.get(targetIndex).isDead()) {
            throw new IllegalArgumentException("Choose a living creature shown on that board.");
        }
        return board.get(targetIndex);
    }

    private static void showActionResult(GameEngine engine, ActionResult result) {
        if (result.isDenied()) {
            System.out.println("Action denied: " + result.reason());
        } else if (!engine.isFinished()) {
            printState(engine);
        }
    }

    private static void printState(GameEngine engine) {
        Player active = engine.getCurrentPlayer();
        System.out.printf("%nTurn %d | %s to act | %s phase%n",
                engine.getTurnNumber(), active.getName(), engine.getPhase());
        printBoard("You", active, true);
        printBoard("Opponent", engine.getOpponent(), false);
        System.out.println("Your hand:");
        List<Card> hand = active.getHand();
        if (hand.isEmpty()) {
            System.out.println("  (empty)");
        }
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            String target = card.getTargetRequirement().name().toLowerCase(Locale.ROOT).replace('_', ' ');
            System.out.printf("  %d. %s (%d mana): %s [target: %s]%n",
                    i + 1, card.getName(), card.getManaCost(), card.describe(), target);
        }
    }

    private static void printBoard(String label, Player player, boolean showReadiness) {
        System.out.printf("%s (%s) | Health %d/%d | Mana %d/%d | Deck %d | Hand %d | Discard %d%n",
                label, player.getName(), player.getHealth(), player.getMaxHealth(),
                player.getMana(), player.getMaxMana(), player.getDeck().size(),
                player.getHand().size(), player.getDiscardPile().size());
        System.out.println("  Board:");
        List<Creature> board = player.getBoard();
        if (board.isEmpty()) {
            System.out.println("    (empty)");
        }
        for (int i = 0; i < board.size(); i++) {
            Creature creature = board.get(i);
            String status = showReadiness ? (creature.canAttack() ? " [ready]" : " [cannot attack]") : "";
            System.out.printf("    %d. %s (%d attack, %d/%d health)%s%n", i + 1,
                    creature.getName(), creature.getAttack(), creature.getHealth(), creature.getMaxHealth(), status);
        }
    }

    private static void printCommands() {
        System.out.print("""
                state                 Show both boards and your hand
                play 1                Play hand card 1 with no target
                play 1 self           Play hand card 1 on your hero
                play 1 enemy          Play hand card 1 on the opponent's hero
                play 1 ally 2         Play hand card 1 on your creature 2
                play 1 enemy 2        Play hand card 1 on the opponent's creature 2
                attack 1 enemy        Attack the opponent's hero with your creature 1
                attack 1 enemy 2      Attack the opponent's creature 2 with your creature 1
                end                   End your turn and pass the keyboard
                help                  Show this command list
                quit                  Close the game
                """);
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
