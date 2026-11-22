package soe402.cardgame.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TurnManager: phase sequence")
class TurnManagerTest {

    @Test
    @DisplayName("phases advance start, draw, main, end and wrap around")
    void phasesAdvanceInOrder() {
        TurnManager turns = new TurnManager();
        turns.beginTurn();

        assertEquals(Phase.START, turns.getPhase());
        turns.advance();
        assertEquals(Phase.DRAW, turns.getPhase());
        turns.advance();
        assertEquals(Phase.MAIN, turns.getPhase());
        turns.advance();
        assertEquals(Phase.END, turns.getPhase());
        turns.advance();
        assertEquals(Phase.START, turns.getPhase());
    }

    @Test
    @DisplayName("only the main phase allows actions")
    void onlyMainPhaseAllowsActions() {
        TurnManager turns = new TurnManager();
        turns.beginTurn();
        assertFalse(turns.isMainPhase());

        turns.advance();
        turns.advance();
        assertTrue(turns.isMainPhase());
    }

    @Test
    @DisplayName("ending the turn jumps straight to the end phase")
    void endingTheTurnJumpsToEnd() {
        TurnManager turns = new TurnManager();
        turns.beginTurn();
        turns.endTurn();
        assertEquals(Phase.END, turns.getPhase());
        assertFalse(turns.isMainPhase());
    }

    @Test
    @DisplayName("the turn counter starts at zero and counts every turn")
    void turnCounterIncrements() {
        TurnManager turns = new TurnManager();
        assertEquals(0, turns.getTurnNumber());

        turns.beginTurn();
        assertEquals(1, turns.getTurnNumber());
        turns.beginTurn();
        assertEquals(2, turns.getTurnNumber());
    }
}
