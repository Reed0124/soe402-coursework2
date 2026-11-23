package soe402.cardgame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import soe402.cardgame.support.StubContext;

/**
 * Covers every {@link TargetRequirement} against every kind of character.
 *
 * <p>The matrix is written out explicitly so that a change to {@link Targets} has to be
 * matched by a change of intention here, rather than being silently absorbed.</p>
 */
@DisplayName("Targets: the target rule matrix")
class TargetsTest {

    private Player caster;
    private Player opponent;
    private List<GameCharacter> targets;
    private List<String> targetNames;

    @BeforeEach
    void setUp() {
        StubContext context = new StubContext();
        caster = new Player("Alice", new Deck(List.of(), false));
        opponent = new Player("Bob", new Deck(List.of(), false));
        context.setOpponent(opponent);

        Creature ownCreature = new Creature("Own", 1, 1, 1);
        ownCreature.play(context, caster, null);
        Creature enemyCreature = new Creature("Enemy", 1, 1, 1);
        enemyCreature.play(context, opponent, null);

        targets = new ArrayList<>(List.of(caster, opponent, ownCreature, enemyCreature));
        targetNames = List.of("own hero", "enemy hero", "own creature", "enemy creature");
    }

    @Test
    @DisplayName("every requirement accepts exactly the documented kinds of target")
    void requirementMatrix() {
        Map<TargetRequirement, boolean[]> expected = new EnumMap<>(TargetRequirement.class);
        expected.put(TargetRequirement.NONE, new boolean[]{true, true, true, true});
        expected.put(TargetRequirement.ANY_CHARACTER, new boolean[]{true, true, true, true});
        expected.put(TargetRequirement.ENEMY_CHARACTER, new boolean[]{false, true, false, true});
        expected.put(TargetRequirement.FRIENDLY_CHARACTER, new boolean[]{true, false, true, false});
        expected.put(TargetRequirement.ANY_CREATURE, new boolean[]{false, false, true, true});
        expected.put(TargetRequirement.ENEMY_CREATURE, new boolean[]{false, false, false, true});
        expected.put(TargetRequirement.FRIENDLY_CREATURE, new boolean[]{false, false, true, false});

        assertEquals(TargetRequirement.values().length, expected.size(),
                "every requirement must appear in the matrix");

        for (Map.Entry<TargetRequirement, boolean[]> row : expected.entrySet()) {
            for (int i = 0; i < targets.size(); i++) {
                String message = row.getKey() + " with target " + targetNames.get(i);
                assertEquals(row.getValue()[i],
                        Targets.isValid(row.getKey(), caster, targets.get(i)), message);
            }
        }
    }

    @Test
    @DisplayName("no requirement except NONE accepts a missing target")
    void nullIsOnlyValidWhenNoTargetIsNeeded() {
        assertTrue(Targets.isValid(TargetRequirement.NONE, caster, null));
        for (TargetRequirement requirement : TargetRequirement.values()) {
            if (requirement != TargetRequirement.NONE) {
                assertFalse(Targets.isValid(requirement, caster, null), requirement.name());
            }
        }
    }

    @Test
    @DisplayName("friendly means the caster's own hero or its own creatures")
    void friendliness() {
        assertTrue(Targets.isFriendly(caster, caster));
        assertFalse(Targets.isFriendly(caster, opponent));
        assertTrue(Targets.isFriendly(caster, targets.get(2)));
        assertFalse(Targets.isFriendly(caster, targets.get(3)));
    }
}
