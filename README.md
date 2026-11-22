# SOE402 Group 7 — Turn-Based Card Game Engine

A Java 17 implementation of the card game engine described in the Phase 1 specification,
together with a JUnit 5 test suite and a scripted demonstration game.

## Building and running

The project is a standard Maven project, so it can be opened directly in IntelliJ IDEA
(*File → Open →* select `pom.xml`) or built from the command line:

```
mvn clean test        # compiles and runs all 114 tests
mvn clean package     # builds target/cardgame-1.0.0.jar
```

To watch a full game in the console:

```
mvn -q compile
java -cp target/classes soe402.cardgame.app.Main
java -cp target/classes soe402.cardgame.app.Main 42   # a different shuffle
```

The demo is deterministic: the same seed always produces the same game, which is also what the
end-to-end test relies on.

## Project layout

| Package | Responsibility |
|---|---|
| `soe402.cardgame.model` | Domain model: `Card`, `Creature`, `Spell`, `Modifier`, `Player`, `Deck`, the `GameCharacter` and `Effect` interfaces, and the `GameContext` port. |
| `soe402.cardgame.effect` | The `Effect` implementations: damage, heal, draw, freeze, area damage and the composite. |
| `soe402.cardgame.engine` | `GameEngine`, the `TurnManager` phase machine, `ActionValidator`, `GameConfig` and `ActionResult`. |
| `soe402.cardgame.event` | `GameEvent` (a sealed hierarchy of records), `EventBus` and the listener interface. |
| `soe402.cardgame.log` | `GameLog`, which renders events into readable lines. |
| `soe402.cardgame.cards` | `CardRegistry` (card pool) and `DeckFactory` (deck construction). |
| `soe402.cardgame.app` | `Main`, the console demonstration. |

## Design decisions

**Cards are definitions; creatures in play are instances.** `Card` holds the immutable name,
mana cost and type. A `Creature` additionally holds its mutable combat state. `CardRegistry`
returns a new object on every call, so two copies of a card never share state.

**Spell behaviour is a Strategy, not a subclass per card.** A `Spell` holds an `Effect`.
Adding "Fireball" means instantiating `new Spell("Fireball", 4, new DamageEffect(6))`; the
engine, the validator and the log are untouched. `CompositeEffect` chains effects, which is how
a frostbolt is built from damage plus freeze without duplicating either.

**One abstraction for targets.** `GameCharacter` is implemented by both `Player` and `Creature`,
so a spell can be aimed at either without a special case. `TargetRequirement` plus `Targets`
turn target rules into data, and `Card.getTargetRequirement()` lets a user interface highlight
legal choices before the player commits.

**The engine is a facade over a phase machine and a validator.** `GameEngine` exposes exactly
three actions — play a card, attack, end the turn. Each one asks `ActionValidator` first and
only then mutates state, so a rejected action cannot leave the game half-changed. `TurnManager`
owns the `START → DRAW → MAIN → END` sequence.

**The log is an observer, not a set of print statements.** The engine publishes a `GameEvent`
for every state change; `GameLog` subscribes and formats. The same seam would carry a GUI or a
replay recorder without touching the engine.

**Cards reach the game through a narrow port.** `GameContext` exposes damage, healing, drawing,
summoning and modifier attachment — and nothing else. The model package therefore has no
dependency on the engine package, and effects can be tested against a stub.

## Rules implemented

* Each player starts with 30 health and a 20-card deck; the hand limit is 7 and an overdraw
  burns the drawn card.
* Mana starts at 1, grows by one per turn up to 10, is refilled at the start of the turn and is
  spent by the card's mana cost.
* A creature may attack from the turn after it was played, at most once per turn. A defender
  that survives the attack strikes back; a defender that dies does not.
* Drawing from an empty deck deals fatigue damage of 1, then 2, then 3, and so on.
* The game ends as soon as a hero reaches zero health; every later action is rejected.

Two values are not fixed by the specification and are defined here: the board holds at most
seven creatures, and fatigue damage escalates by one per empty draw. Both live in `GameConfig`
and in `Player.increaseFatigue()`.

## Testing

The suite has 114 tests across 15 classes and is organised to mirror the main packages:

| Test class | Focus |
|---|---|
| `CardTest`, `CreatureTest`, `DeckTest`, `PlayerTest` | Model behaviour, construction input validation, combat state, zones. |
| `TurnManagerTest` | Phase order and the main-phase guard. |
| `GameEngineTurnTest` | Game start, turn alternation, mana growth and the mana cap. |
| `PlayCardTest` | Legal and illegal plays, mana, board limit, targeting, discard. |
| `CombatTest` | Summoning sickness, one attack per turn, retaliation, death. |
| `ModifierTest` | Buff application, duration, illegal target. |
| `FatigueTest` | Empty deck damage, escalation, lethal fatigue. |
| `WinConditionTest` | Zero health, winner, rejected actions after the end. |
| `EffectTest` | Each effect against a stub `GameContext`, including target rules. |
| `GameLogTest` | Event rendering and log collection. |
| `CardRegistryTest`, `FullGameTest` | Card pool, deck building, reproducible end-to-end games. |

Coverage is measured with JaCoCo (`mvn test` writes `target/site/jacoco/index.html`):
88.6% of lines and 79.4% of branches across the whole project.

Tests never depend on a random shuffle: `Deck` accepts a shuffle flag and the test fixtures
pass `false`, so the draw order is exactly the order written in the test.

## Repository conventions

The repository root is this folder: `pom.xml`, `src` and `README.md` at the top level.

* `main` is kept green. CI (`.github/workflows/build.yml`) runs `mvn -B verify` on every push
  and pull request and publishes the JaCoCo report as a build artifact.
* Work happens on a branch per module — `feature/model`, `feature/effects`, `feature/engine`,
  `feature/tests` — merged through a pull request reviewed by one other member.
* Commit messages start with the module they touch, for example
  `engine: reject attacks after the game ends`.
* The UML sources live in `docs/uml`. A change that alters a class or a relationship must
  update them in the same pull request, so the diagram never drifts from the code.
* Build output (`target/`) and IDE files are ignored; `mvn clean` is therefore safe to run.
