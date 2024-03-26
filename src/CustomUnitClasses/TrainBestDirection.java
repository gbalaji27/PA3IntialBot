package CustomUnitClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

import ai.abstraction.AbstractAction;
import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import util.XMLWriter;

public class TrainBestDirection extends AbstractAction {
    UnitType type;
    boolean completed = false;
    Unit unit;

    public TrainBestDirection(Unit u, UnitType a_type) {
        super(u);
        type = a_type;
        unit = u;
    }

    public boolean completed(GameState pgs) {
        return completed;
    }

    public boolean equals(Object o) {
        if (!(o instanceof TrainBestDirection))
            return false;
        TrainBestDirection a = (TrainBestDirection) o;
        return type == a.type;
    }

    public void toxml(XMLWriter w) {
        w.tagWithAttributes("Train", "unitID=\"" + unit.getID() + "\" type=\"" + type.name + "\"");
        w.tag("/Train");
    }

    public UnitAction execute(GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        List<Unit> enemyBase = new ArrayList<>();
        List<Unit> ownBase = new ArrayList<>();

        for (Unit u : pgs.getUnits()) {
            if (u.getType().name == "Base") {
                if (u.getPlayer() == unit.getPlayer()) {
                    ownBase.add(u);
                } else {
                    enemyBase.add(u);
                }
            }
        }

        if (enemyBase.isEmpty() || ownBase.isEmpty()) {
            return null;
        }
        int enemyBaseX = enemyBase.get(0).getX();
        int enemyBaseY = enemyBase.get(0).getY();

        int bestDirection = -1;
        int bestScore = Integer.MIN_VALUE;
        int[][] directions = new int[][] { { 0, -1, UnitAction.DIRECTION_UP }, { 1, 0, UnitAction.DIRECTION_RIGHT },
                { 0, 1, UnitAction.DIRECTION_DOWN }, { -1, 0, UnitAction.DIRECTION_LEFT } };

        for (int[] dir : directions) {
            int newX = unit.getX() + dir[0];
            int newY = unit.getY() + dir[1];
            if (newX >= 0 && newX < pgs.getWidth() && newY >= 0 && newY < pgs.getHeight() && gs.free(newX, newY)) {
                int score = score(newX, newY, type, unit.getPlayer(), pgs, enemyBaseX, enemyBaseY, ownBase.get(0));
                if (score > bestScore || bestDirection == -1) {
                    bestScore = score;
                    bestDirection = dir[2];
                }
            }
        }

        completed = true;

        if (bestDirection != -1) {
            UnitAction ua = new UnitAction(UnitAction.TYPE_PRODUCE, bestDirection, type);
            if (gs.isUnitActionAllowed(unit, ua))
                return ua;
        }

        return null; 
    }

    public int score(int x, int y, UnitType type, int player, PhysicalGameState pgs, int enemyBaseX, int enemyBaseY,
            Unit ownBase) {
        int score = 0;
        int distanceToEnemyBase = Math.abs(enemyBaseX - x) + Math.abs(enemyBaseY - y);
        score -= distanceToEnemyBase;
        int distanceToOwnBase = Math.abs(ownBase.getX() - x) + Math.abs(ownBase.getY() - y);
        score += 2 * distanceToOwnBase; 

        return score;
    }

}
