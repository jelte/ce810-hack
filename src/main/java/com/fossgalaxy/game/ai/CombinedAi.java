package com.fossgalaxy.game.ai;

import com.fossgalaxy.game.orders.AttackOrder;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.entity.HexagonTile;
import com.fossgalaxy.games.tbs.order.MoveOrder;
import com.fossgalaxy.games.tbs.parameters.EntityType;
import com.fossgalaxy.object.annotations.ObjectDef;
import org.codetome.hexameter.core.api.CubeCoordinate;
import org.codetome.hexameter.core.api.Hexagon;
import rts.PlayerAction;
import rts.ai.abstraction.AbstractionLayerAI;
import rts.ai.core.AI;
import rts.ai.core.ParameterSpecification;

import java.util.*;
import java.util.stream.Collectors;

public class CombinedAi extends AbstractionLayerAI{

    Random r = new Random();

    private final EntityType workerType;
    private final EntityType unitType;
    private final EntityType baseType;
    private final EntityType towerType;
    private final EntityType prodType;
    private final EntityType tankType;

    public final int tankLimit = 15, workerLimit = 3;

    // Strategy implemented by this class:
    // If we have more than 1 "Worker": send the extra workers to attack to the nearest enemy unit
    // If we have a base: train workers non-stop
    // If we have a worker: do this if needed: build base, harvest resources
    @ObjectDef("CombinedAi")
    public CombinedAi(EntityType baseType, EntityType towerType, EntityType workerType, EntityType unitType, EntityType prodType, EntityType tankType) {
        this.baseType = baseType;
        this.workerType = workerType;
        this.unitType = unitType;
        this.towerType = towerType;
        this.prodType = prodType;
        this.tankType = tankType;
    }

    @ObjectDef("CombinedAiP")
    public CombinedAi(EntityType baseType, EntityType towerType, EntityType workerType, EntityType prodType, EntityType tankType) {
        this(baseType,towerType, workerType, workerType,prodType, tankType);
    }

    public void reset() {
        super.reset();
    }


    public AI clone() {
        return new CombinedAi(baseType,towerType,workerType, unitType,prodType, tankType);
    }

    public PlayerAction getAction(int player, GameState rgs) {
        reset();
        PlayerAction pa = new PlayerAction();


        // behavior of bases:
        Collection<Entity> entities = rgs.getOwnedEntities(player);
        Map<EntityType, List<Entity>> map = entities.stream().collect(Collectors.groupingBy(Entity::getType));

        List<Entity> bases = map.getOrDefault(baseType, Collections.emptyList());
        List<Entity> workers = map.getOrDefault(workerType, Collections.emptyList());
        List<Entity> towers = map.getOrDefault(towerType,Collections.emptyList());
        List<Entity> tanks = map.getOrDefault(tankType,Collections.emptyList());

        //we can change this amount of workers by team.
        for (Entity base : bases) {
            baseBehavior(base, player, workers, rgs, tanks);

        }
        //in this case we would put the actual base in the prod type
        List<Entity> prods = map.getOrDefault(prodType, Collections.emptyList());
        for (Entity prod : prods) {
            prodBehavior(prod, player, rgs);
        }

        //this should be the tanks.
        if (tanks != null) {
            for (Entity tank : tanks) {
                unitBehavior(tank, player, rgs);
            }
        }

        ArrayList<Entity> e = new ArrayList<>();
        e.addAll(towers);
        e.add(bases.get(0));
        workersBehavior(workers, e, rgs);

        return translateActions(player, rgs);
    }

    private boolean canAfford(EntityType type, GameState s, int playerID) {

        Map<String, Integer> costs = type.getCosts();
        for (String costType : costs.keySet()) {
            if (s.getResource(playerID, costType) < costs.get(costType)) {
                return false;
            }
        }

        return true;
    }

    private void prodBehavior(Entity prod, int player, GameState rgs) {
        if (canAfford(workerType, rgs, player)) {
            train(prod, workerType, rgs);
        }
    }


    public void baseBehavior(Entity base, int player,List<Entity> workers, GameState pgs, List<Entity> tanks) {
        //check removed - we don't care...
        if (workers.size() < workerLimit) {
            if (canAfford(workerType, pgs, player)) {
                train(base, workerType, pgs);
            }
        }
        if(tanks.size() < tankLimit){
            if (canAfford(tankType, pgs, player)) {
                train(base, tankType, pgs);
            }
        }
    }

    /**
     * Travel to nearest enemy if we can hit it
     *
     * @param unit the worker unit to move
     * @param p the current player
     * @param pgs the game state
     */

    public void unitBehavior(Entity unit, int p, GameState pgs) {
        Entity closestEnemy = null;
        double closestDistance = Double.MAX_VALUE;


        CubeCoordinate myPos = unit.getPos();

        Collection<Entity> allEntities = pgs.getEntities();
        for (Entity entity : allEntities) {
            if (entity.getOwner() == p) {
                continue;
            }

            CubeCoordinate theirPos = entity.getPos();

            double dist = pgs.getDistance(myPos, theirPos);
            if (closestDistance > dist) {
                closestEnemy = entity;
                closestDistance = dist;
            }

        }

        if (closestEnemy != null) {
            attack(unit, closestEnemy, pgs);
            double dist = (double)pgs.getDistance(unit.getPos(), closestEnemy.getPos());
            if(dist > 1.0D) {
                this.moveTowards(unit, closestEnemy.getPos(), pgs);
            } else {
                this.actions.put(unit.getID(), new AttackOrder(closestEnemy.getID()));
            }
        }

    }

    public Entity getClosestUnfriendly(Entity us, GameState gs, double maxRange) {
        Entity closestEnemy = null;
        double closestDistance = maxRange+1;

        CubeCoordinate myPos = us.getPos();

        Collection<Entity> allEntities = gs.getOwnedEntities((us.getOwner()+1)%2);
        for (Entity entity : allEntities) {
            if (!entity.getType().getName().toLowerCase().endsWith("tank")) {
                continue;
            }
            CubeCoordinate theirPos = entity.getPos();

            double dist = gs.getDistance(myPos, theirPos);
            if (closestDistance > dist) {
                closestEnemy = entity;
                closestDistance = dist;
            }
        }

        return closestEnemy;
    }

    public void workersBehavior(List<Entity> workers, List<Entity> towers, GameState gs) {
        LinkedList<Entity> freeWorkers = new LinkedList<>();
        freeWorkers.addAll(workers);

        if (workers.isEmpty()) return;

        for (Entity worker: workers) {
            workerBehavior(worker, towers, gs);
        }
    }

    private void workerBehavior(Entity worker, List<Entity> towers, GameState gs) {
        // Move away from tanks
        /*Entity closest = getClosestUnfriendly(worker, gs, unitType.getProperty("attackRange"));
        if (closest != null) {
            moveAway(worker, closest.getPos(), gs);
            return;
        }*/

        if (towers.size() == 1) {
            if (gs.getDistance(worker.getPos(), towers.get(0).getPos()) >= 3+new Random().nextInt(3)) {
                if (buildTower(worker, gs)) {
                    return;
                }
            }
            moveAway(gs, worker, towers.get(0).getPos());
            return;
        }

        towers.sort((a, b) -> ( gs.getDistance(worker.getPos(), a.getPos()) - gs.getDistance(worker.getPos(), b.getPos())));
        if (gs.getDistance(worker.getPos(), towers.get(0).getPos()) >= 3+new Random().nextInt(3) && buildTower(worker, gs)) {
            return;
        }
        Set<Hexagon<HexagonTile>> s1 = gs.getCalc().calculateMovementRangeFrom(gs.cube2hex(towers.get(0).getPos()), 6);
        Set<Hexagon<HexagonTile>> s2 = gs.getCalc().calculateMovementRangeFrom(gs.cube2hex(towers.get(1).getPos()), 6);
        List<Hexagon<HexagonTile>> tiles = new ArrayList<>();
        List<Hexagon<HexagonTile>> topTiles = new ArrayList<>();
        for (Hexagon<HexagonTile> t : s1) {
            if (gs.getDistance(towers.get(0).getPos(), t.getCubeCoordinate()) >= 5) {
                if (gs.getTerrainAt(t.getCubeCoordinate()).getName().toLowerCase().endsWith("walkable") && s2.contains(t) && !tiles.contains(t)) {
                    tiles.add(t);
                }
            }
        }
        if (tiles.size() == 0) {
            return;
        }
        tiles.sort((a, b) -> (
                gs.getDistance(towers.get(1).getPos(), b.getCubeCoordinate()) - gs.getDistance(towers.get(1).getPos(), a.getCubeCoordinate())));
        double topDistance = gs.getDistance(towers.get(1).getPos(), tiles.get(0).getCubeCoordinate());
        for (Hexagon<HexagonTile> t : tiles) {
            if (gs.getDistance(towers.get(1).getPos(),t.getCubeCoordinate()) == topDistance) {
                topTiles.add(t);
            }
        }
        System.out.println(topTiles.size());
        moveTowards(worker, topTiles.get(new Random().nextInt(topTiles.size())).getCubeCoordinate(), gs);
    }

    private boolean buildTower(Entity worker, GameState gs)
    {
        for (Hexagon<HexagonTile> tile : gs.getCalc().calculateMovementRangeFrom(gs.cube2hex(worker.getPos()), 1)) {
            if (gs.getTerrainAt(tile.getCubeCoordinate()).getName().toLowerCase().endsWith("walkable") && gs.getEntityAt(tile.getCubeCoordinate()) == null) {
                build(worker, towerType, tile.getCubeCoordinate());
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();

        //parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }


    private void moveAway(GameState gameState, Entity entity, CubeCoordinate target) {
        double distance = -1.7976931348623157E308D;
        CubeCoordinate next = null;
        Collection<Hexagon<HexagonTile>> neighbors = gameState.getNeighbors(entity.getPos());
        Iterator var7 = neighbors.iterator();

        List<CubeCoordinate> cubeCoordinates = new ArrayList<>();
        while(var7.hasNext()) {
            Hexagon<HexagonTile> hex = (Hexagon)var7.next();
            if (gameState.getEntityAt(hex.getCubeCoordinate()) == null && gameState.getTerrainAt(hex.getCubeCoordinate()).isPassible(entity)) {
                double myDist = (double)gameState.getDistance(hex.getCubeCoordinate(), target);
                if (myDist > distance) {
                    cubeCoordinates.add(hex.getCubeCoordinate());
                    distance = myDist;
                }
            }
        }

        if (cubeCoordinates.size() == 0) {
            return;
        } else {
            this.actions.put(entity.getID(), new MoveOrder(cubeCoordinates.get(new Random().nextInt(cubeCoordinates.size()))));
        }
    }
    public void moveTowards(GameState gameState, Entity entity, CubeCoordinate target) {
        double distance = 1.7976931348623157E308D;
        CubeCoordinate next = null;
        Collection<Hexagon<HexagonTile>> neighbors = gameState.getNeighbors(entity.getPos());
        Iterator var7 = neighbors.iterator();

        List<CubeCoordinate> cubeCoordinates = new ArrayList<>();
        while(var7.hasNext()) {
            Hexagon<HexagonTile> hex = (Hexagon)var7.next();
            if (gameState.getEntityAt(hex.getCubeCoordinate()) == null && gameState.getTerrainAt(hex.getCubeCoordinate()).isPassible(entity)) {
                HexagonTile gt = (HexagonTile)hex.getSatelliteData().get();
                if (gt.isPassable(entity)) {
                    double myDist = (double)gameState.getDistance(hex.getCubeCoordinate(), target);
                    if (myDist < distance) {
                        cubeCoordinates.add(hex.getCubeCoordinate());
                        distance = myDist;
                    }
                }
            }
        }

        if (cubeCoordinates.size() == 0) {
            return;
        } else {
            this.actions.put(entity.getID(), new MoveOrder(cubeCoordinates.get(new Random().nextInt(cubeCoordinates.size()))));
        }
    }
}
