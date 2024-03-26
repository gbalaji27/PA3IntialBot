package BaluBot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import CustomUnitClasses.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

public class BaluBot extends AbstractionLayerAI {
    private UnitTypeTable unitTypeTable;
    private Player player;
    private GameState game;
    private PhysicalGameState board;
    private List<Unit> units, _units;

    private UnitType WORKER, LIGHT, HEAVY, RANGED, BASE, BARRACKS;
    private List<Unit> bases, barracks, workers, light, heavy, ranged;
    private List<Unit> _bases, _barracks, _workers, _light, _heavy, _ranged;
    private List<Unit> resources;

    List<Unit> builders = new ArrayList<>();
    List<Unit> harvesters = new ArrayList<>();
    List<Unit> defenders = new ArrayList<>();

    private class Base {
        public Base() {
            bases.forEach(base -> {
                if (base.isIdle(game))
                    assignTask(base);
            });
        }

        private void assignTask(Unit base) {
            boolean isBarracksBuilding = builders.size() > 0
                    && builders.get(0).getUnitActions(game).get(0).getType() == UnitAction.TYPE_PRODUCE;
            List<Unit> enemiesWithinHalfBoard = findUnitsWithin(_units, base,
                    (int) Math.floor(Math.sqrt(board.getWidth() * board.getHeight()) / 2));
            boolean shouldTrain = (barracks.size() == 0
                    && !isBarracksBuilding && workers.size() > 2
                    && enemiesWithinHalfBoard.size() == 0)
                            ? player.getResources() >= BARRACKS.cost + WORKER.cost
                            : player.getResources() >= WORKER.cost;

            if (shouldTrain && defenders.size() == 0) {
                
                train(base, WORKER);
                return;
            }
        }
    }

    private class Barracks {
        public Barracks() {
            barracks.forEach(barrack -> {
                if (barrack.isIdle(game))
                    assignTask(barrack);
            });
        }

        private void assignTask(Unit barrack) {
            if (player.getResources() >= RANGED.cost) {
                train(barrack, RANGED);
                return;
            }
            if (player.getResources() >= LIGHT.cost) {
                train(barrack, LIGHT);
                return;
            }
        }
    }

    private class Workers {
        public Workers() {
            builders.removeIf(builder -> !workers.contains(builder));
            harvesters.removeIf(harvester -> !workers.contains(harvester));
            defenders.removeIf(defender -> !workers.contains(defender));

            if (barracks.size() > 0)
                builders.clear();
            if (resources.size() == 0)
                harvesters.clear();

            workers.forEach(worker -> {
                if (worker.isIdle(game))
                    assignTask(worker);
            });
        }

        private void assignTask(Unit worker) {
            Unit base = findClosest(bases, worker);
            Unit enemyBase = findClosest(_bases, worker);
            Unit enemy = findClosest(_units, worker);
            Unit resource = findClosest(resources, worker);

            boolean isHarvester = harvesters.contains(worker);
            boolean isBuilder = builders.contains(worker);
            boolean isDefender = defenders.contains(worker);

            if (enemy == null)
                return;

            boolean shouldPrioritizeAttack = (distance(worker,
                    enemy) <= (!isHarvester ? (worker.getAttackRange() + 3) : worker.getAttackRange())
                    || (enemyBase == null && !isHarvester)) || base == null;

            if (shouldPrioritizeAttack) {
                harvesters.removeIf(harvester -> harvester == worker);
                builders.removeIf(builder -> builder == worker);
                attack(worker, enemy);
                return;
            }

            if (worker.getResources() > 0) {
                harvest(worker, resource, base);
                return;
            }

            boolean isBarracksBuilding = builders.size() > 0
                    && builders.get(0).getUnitActions(game).get(0).getType() == UnitAction.TYPE_PRODUCE;
            List<Unit> nearbyResources = findUnitsWithin(resources, base,
                    (int) Math.floor(Math.sqrt(board.getWidth() * board.getHeight()) / 2));
            int harvestersNeeded = (int) Math.ceil(findAdjacentCells(nearbyResources).size() / 2);
            if (harvestersNeeded == 1 && !isBarracksBuilding) {
                harvestersNeeded = 2;
            }

            Unit enemyWithinHalfOfMap = findClosestWithin(_units, worker,
                    (int) Math.floor(Math.sqrt(board.getWidth() * board.getHeight()) / 2));
            boolean canBuildBarracks = (player.getResources() >= BARRACKS.cost + WORKER.cost && enemyBase != null
                    && builders.size() == 0 && !isBarracksBuilding
                    && harvesters.size() == harvestersNeeded && (!isHarvester || workers.size() >= 2)) || isBuilder;
            boolean shouldBuildBarracks = barracks.size() == 0 && enemyWithinHalfOfMap == null;

            if (canBuildBarracks && shouldBuildBarracks) {

                int buildX = base.getX();
                int buildY = base.getY();
                buildX += (enemyBase.getX() > base.getX()) ? 1 : -1;
                buildY += (enemyBase.getY() > base.getY()) ? -2 : 2;
                buildX = Math.max(0, Math.min(buildX, board.getWidth() - 1));
                buildY = Math.max(0, Math.min(buildY, board.getHeight() - 1));

                double minDist = Double.MAX_VALUE;
                Unit closestWorker = null;
                for (Unit u : workers) {
                    double d = distance(u.getX(), u.getY(), buildX, buildY);
                    if (d < minDist) {
                        minDist = d;
                        closestWorker = u;
                    }
                }

                if (closestWorker == worker) {
                    if (!isBuilder)
                        builders.add(worker);
                    harvesters.removeIf(harvester -> harvester == worker);
                    defenders.removeIf(defender -> defender == worker);

                    build(worker, BARRACKS, buildX, buildY);
                    return;
                }
            }

            boolean canHarvest = resource != null || worker.getResources() > 0;
            boolean shouldHarvest = harvesters.size() < harvestersNeeded;

            if ((canHarvest && shouldHarvest) || isHarvester) {
                if (!isHarvester)
                    harvesters.add(worker);
                defenders.removeIf(defender -> defender == worker);

                harvest(worker, resource, base);
                return;
            }

            harvesters.removeIf(harvester -> harvester == worker);
            builders.removeIf(builder -> builder == worker);
            if (!isDefender)
                defenders.add(worker);

            attack(worker, enemy);
        }
    }

    private class Lights {
        public Lights() {
            light.forEach(light -> {
                if (light.isIdle(game))
                    assignTask(light);
            });
        }

        private void assignTask(Unit light) {
            attackWithMarch(light);
        }
    }

    private class Heavies {
        public Heavies() {
            heavy.forEach(heavy -> {
                if (heavy.isIdle(game))
                    assignTask(heavy);
            });
        }

        private void assignTask(Unit heavy) {
            attackWithMarch(heavy);
        }
    }

    private class Ranged {
        public Ranged() {
            ranged.forEach(ranged -> {
                if (ranged.isIdle(game))
                    assignTask(ranged);
            });
        }

        private List<Point> calculateRetreatPositions(Unit ranged, List<Unit> enemies) {
            List<Point> retreats = new ArrayList<>();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0)
                        continue;
                    int newX = ranged.getX() + dx;
                    int newY = ranged.getY() + dy;
                    if (isValidRetreat(newX, newY) && isMovingAwayFromEnemies(newX, newY, ranged, enemies)) {
                        retreats.add(new Point(newX, newY));
                    }
                }
            }
            return retreats;
        }

        private void assignTask(Unit ranged) {
            List<Unit> enemiesWithinAttackRange = findUnitsWithin(_units, ranged, ranged.getAttackRange());
            List<Unit> enemiesWithinReducedAttackRange = findUnitsWithin(_units, ranged, ranged.getAttackRange() - 1);

            List<Unit> enemiesAtMaxAttackRange = new ArrayList<>();
            for (Unit enemy : enemiesWithinAttackRange) {
                if (!enemiesWithinReducedAttackRange.contains(enemy)) {
                    enemiesAtMaxAttackRange.add(enemy);
                }
            }
            if (!enemiesWithinReducedAttackRange.isEmpty()) {
                retreatOrAttack(ranged, enemiesWithinReducedAttackRange, enemiesWithinAttackRange);
            } else if (!enemiesAtMaxAttackRange.isEmpty()) {
                attack(ranged, findClosest(enemiesAtMaxAttackRange, ranged));
            } else {
                attackWithMarch(ranged);
            }
        }

        private void retreatOrAttack(Unit ranged, List<Unit> enemiesWithinReducedAttackRange,
                List<Unit> enemiesWithinAttackRange) {
            List<Point> possibleRetreats = calculateRetreatPositions(ranged, enemiesWithinAttackRange);
            Point bestRetreat = chooseBestRetreat(possibleRetreats, enemiesWithinAttackRange);
            if (bestRetreat != null) {
                move(ranged, bestRetreat.x, bestRetreat.y);
            } else {
                Unit target = findClosest(enemiesWithinAttackRange, ranged);
                if (target != null) {
                    attack(ranged, target);
                } else {
                    attackWithMarch(ranged);
                }
            }
        }

        private boolean isValidRetreat(int x, int y) {
            return x >= 0 && x < board.getWidth() && y >= 0 && y < board.getHeight() && game.free(x, y);
        }

        private boolean isMovingAwayFromEnemies(int newX, int newY, Unit ranged, List<Unit> enemies) {
            return enemies.stream().allMatch(enemy -> distance(newX, newY, enemy.getX(),
                    enemy.getY()) > distance(ranged.getX(), ranged.getY(), enemy.getX(), enemy.getY()));
        }

        private Point chooseBestRetreat(List<Point> possibleRetreats, List<Unit> enemies) {
            return possibleRetreats.stream()
                    .max(Comparator.comparingDouble(retreat -> enemies.stream()
                            .mapToDouble(enemy -> distance(retreat.x, retreat.y, enemy.getX(), enemy.getY()))
                            .min().orElse(Double.MAX_VALUE)))
                    .orElse(null);
        }
    }

    private void attackWithMarch(Unit unit) {
        List<Unit> enemiesInCloseRange = findUnitsWithin(_units, unit, unit.getAttackRange());
        if (enemiesInCloseRange.size() > 0) {
            attack(unit, findClosest(enemiesInCloseRange, unit));
            return;
        }

        Unit enemyBase = findClosest(_bases, unit);
        Unit enemyBarracks = findClosest(_barracks, unit);

        int distanceToBase = enemyBase != null ? distance(unit, enemyBase) : Integer.MAX_VALUE;
        int distanceToBarracks = enemyBarracks != null ? distance(unit, enemyBarracks) : Integer.MAX_VALUE;

        if (distanceToBase < distanceToBarracks) {
            attack(unit, enemyBase);
            return;
        } else if (distanceToBarracks < distanceToBase) {
            attack(unit, enemyBarracks);
            return;
        } else {
            attack(unit, findClosest(_units, unit));
        }
    }

    private List<Unit> findUnitsWithin(List<Unit> units, Unit reference, int distance) {
        return units.stream().filter(u -> distance(u, reference) <= distance).collect(Collectors.toList());
    }

    private Unit findClosest(List<Unit> units, Unit reference) {
        return units.stream().min(Comparator.comparingInt(u -> distance(u, reference))).orElse(null);
    }

    private Unit findClosestWithin(List<Unit> units, Unit reference, int distance) {
        return findUnitsWithin(units, reference, distance).stream()
                .min(Comparator.comparingInt(u -> distance(u, reference)))
                .orElse(null);
    }

    public Set<String> findAdjacentCells(List<Unit> units) {
        int[][] DIRECTIONS = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        Set<String> cells = new HashSet<>();

        for (Unit unit : units) {
            for (int[] direction : DIRECTIONS) {
                int nx = unit.getX() + direction[0];
                int ny = unit.getY() + direction[1];

                if (isWithinBoard(nx, ny)) {
                    String cellId = nx + "," + ny;
                    cells.add(cellId);
                }
            }
        }

        return cells;
    }

    private boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < board.getWidth() && y >= 0 && y < board.getHeight();
    }

    private int distance(Unit u1, Unit u2) {
        return Math.abs(u1.getX() - u2.getX()) + Math.abs(u1.getY() - u2.getY());
    }

    private double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    @Override
    public PlayerAction getAction(int player, GameState game) {
        setActionState(player, game);
        new Base();
        new Barracks();
        new Workers();
        new Lights();
        new Heavies();
        new Ranged();
        return translateActions(player, game);
    }

@SuppressWarnings("unchecked")
public void setActionState(int player, GameState game) {
    this.player = game.getPlayer(player);
    this.game = game;
    board = game.getPhysicalGameState();

    resetUnits();
    for (Unit unit : board.getUnits()) {
        if (unit.getPlayer() == player) {
            addUnitToLists(unit, units, bases, barracks, workers, light, heavy, ranged);
        } else if (unit.getPlayer() >= 0) {
            addUnitToLists(unit, _units, _bases, _barracks, _workers, _light, _heavy, _ranged);
        } else {
            resources.add(unit);
        }
    }
}

@SuppressWarnings("unchecked")
private void addUnitToLists(Unit unit, List<Unit>... lists) {
    lists[0].add(unit);
    switch (unit.getType().name) {
        case "Base"     -> lists[1].add(unit);
        case "Barracks" -> lists[2].add(unit);
        case "Worker"   -> lists[3].add(unit);
        case "Light"    -> lists[4].add(unit);
        case "Heavy"    -> lists[5].add(unit);
        case "Ranged"   -> lists[6].add(unit);
    }
}


    public BaluBot(UnitTypeTable unitTypeTable) {
        this(unitTypeTable, new AStarPathFinding());
    }

    public BaluBot(UnitTypeTable unitTypeTable, PathFinding pf) {
        super(pf);
        this.unitTypeTable = unitTypeTable;
        setUnitTypes();
        resetUnits();
    }

    @Override
    public void reset(UnitTypeTable unitTypeTable) {
        super.reset(unitTypeTable);
    }

    @Override
    public AI clone() {
        return new BaluBot(unitTypeTable, pf);
    }

    private void setUnitTypes() {
        for (UnitType unitType : unitTypeTable.getUnitTypes()) {
            switch (unitType.name) {
                case "Worker":
                    WORKER = unitType;
                    break;
                case "Light":
                    LIGHT = unitType;
                    break;
                case "Heavy":
                    HEAVY = unitType;
                    break;
                case "Ranged":
                    RANGED = unitType;
                    break;
                case "Base":
                    BASE = unitType;
                    break;
                case "Barracks":
                    BARRACKS = unitType;
                    break;
            }
        }
    }

    private void resetUnits() {
        units = new ArrayList<>();
        _units = new ArrayList<>();
        bases = new ArrayList<>();
        barracks = new ArrayList<>();
        workers = new ArrayList<>();
        light = new ArrayList<>();
        heavy = new ArrayList<>();
        ranged = new ArrayList<>();
        _bases = new ArrayList<>();
        _barracks = new ArrayList<>();
        _workers = new ArrayList<>();
        _light = new ArrayList<>();
        _heavy = new ArrayList<>();
        _ranged = new ArrayList<>();
        resources = new ArrayList<>();
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return null;
    }

    private class Point {
        public int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}