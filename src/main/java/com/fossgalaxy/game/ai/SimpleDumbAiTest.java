package com.fossgalaxy.game.ai;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.entity.Resource;
import com.fossgalaxy.games.tbs.order.BuildOrder;
import com.fossgalaxy.games.tbs.parameters.EntityType;
import com.fossgalaxy.games.tbs.ui.GameAction;
import com.fossgalaxy.object.annotations.ObjectDef;
import com.sun.javafx.geom.Vec2d;
import org.codetome.hexameter.core.api.CubeCoordinate;
import rts.PlayerAction;
import rts.ai.abstraction.AbstractionLayerAI;
import rts.ai.abstraction.ProRushTactics;
import rts.ai.core.AI;
import rts.ai.core.ParameterSpecification;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleDumbAiTest  extends AbstractionLayerAI{

    Random r = new Random();

    private final EntityType workerType;
    private final EntityType unitType;
    private final EntityType baseType;
    private final EntityType towerType;
    private final EntityType prodType;

    // Strategy implemented by this class:
    // If we have more than 1 "Worker": send the extra workers to attack to the nearest enemy unit
    // If we have a base: train workers non-stop
    // If we have a worker: do this if needed: build base, harvest resources
    @ObjectDef("SimpleTact")
    public SimpleDumbAiTest(EntityType baseType, EntityType towerType, EntityType workerType, EntityType unitType, EntityType prodType) {
        this.baseType = baseType;
        this.workerType = workerType;
        this.unitType = unitType;
        this.towerType = towerType;
        this.prodType = prodType;
    }

    @ObjectDef("SimpleTactP")
    public SimpleDumbAiTest(EntityType baseType, EntityType towerType, EntityType workerType, EntityType prodType) {
        this(baseType,towerType, workerType, workerType,prodType);
    }

    public void reset() {
        super.reset();
    }


    public AI clone() {
        return new SimpleDumbAiTest(baseType,towerType,workerType, unitType,prodType);
    }

    public PlayerAction getAction(int player, GameState rgs) {
        PlayerAction pa = new PlayerAction();


        // behavior of bases:
        Collection<Entity> entities = rgs.getOwnedEntities(player);
        Map<EntityType, List<Entity>> map = entities.stream().collect(Collectors.groupingBy(Entity::getType));

        List<Entity> bases = map.getOrDefault(baseType, Collections.emptyList());
        List<Entity> workers = map.getOrDefault(workerType, Collections.emptyList());
        List<Entity> towers = map.getOrDefault(towerType,Collections.emptyList());

        //we can change this amount of workers by team.
        for (Entity base : bases) {
            baseBehavior(base, player, workers, rgs);

        }
        //in this case we would put the actual base in the prod type
        List<Entity> prods = map.getOrDefault(prodType, Collections.emptyList());
        for (Entity prod : prods) {
            prodBehavior(prod, player, rgs);
        }

        //this should be the tanks.
        List<Entity> units = map.get(unitType);
        if (units != null) {
            for (Entity unit : units) {
                unitBehavior(unit, player, rgs);
            }
        }

        workersBehavior(workers, player, bases.size(), prods.size(), rgs);

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


    public void baseBehavior(Entity base, int player,List<Entity> workers, GameState pgs) {
        //check removed - we don't care...
        if (workers.size() < 8) {
            if (canAfford(workerType, pgs, player)) {
                train(base, workerType, pgs);
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
        }

    }

    public Entity getClosestUnfriendly(Entity us, GameState gs, double maxRange) {
        Entity closestEnemy = null;
        double closestDistance = maxRange;


        CubeCoordinate myPos = us.getPos();

        Collection<Entity> allEntities = gs.getEntities();
        for (Entity entity : allEntities) {
            if (entity.getOwner() == us.getOwner()) {
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

    public void workersBehavior(List<Entity> workers, int p, int nbases, int nProd, GameState gs) {


        LinkedList<Entity> freeWorkers = new LinkedList<>();
        freeWorkers.addAll(workers);

        if (workers.isEmpty()) return;

        //if we have free workers and no bases, we should build a base.
        if (nbases==0 && !freeWorkers.isEmpty()) {
            Entity worker = freeWorkers.poll();
            buildIfNotAlreadyBuilding(worker, baseType, gs);
        }

        //build towers forever
        if (!freeWorkers.isEmpty()) {
            Entity worker = freeWorkers.poll();
            //use buildTower function. Not yet implemented
            buildIfNotAlreadyBuilding(worker, towerType, gs);
        }

        // run away
        Iterator<Entity> freeItr = freeWorkers.iterator();
        while (freeItr.hasNext()) {
            Entity worker = freeItr.next();
            Entity enemy = getClosestUnfriendly(worker, gs, 3);
            if (enemy != null) {
                moveAway(worker, enemy.getPos(), gs);
                freeItr.remove();
            }


        }


    }
    //TODO Get a distance from tiles, workers are not moving, game ends after building initial towers.
    public void buildTower(Entity worker,GameState gs){
        //we've pre-calculated number of bases, using that to save a little time...
        List<Vec2d> blues = null;
        List<Vec2d> reds = null;
        List<Vec2d> whites = null;
        for (int x = 0; x < gs.getWidth(); x++) {
            for (int z = 0; z < gs.getHeight(); z++) {
                try {
                    if(gs.getTerrainAt(CubeCoordinate.fromCoordinates(x, z)).getName().startsWith("blue")? true:false){
                        blues.add(new Vec2d(x,z));
                    }
                    if(gs.getTerrainAt(CubeCoordinate.fromCoordinates(x, z)).getName().startsWith("red")? true:false){
                        reds.add(new Vec2d(x,z));
                    }
                } catch (Exception e) {}
            }
        }//TODO see if blues and reds exist, get the closest one, move towards closest white tile?
        if(blues != null){

        }
    }


    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();

        //parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
}
