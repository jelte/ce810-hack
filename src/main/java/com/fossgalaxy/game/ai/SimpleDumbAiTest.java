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


    private final int workerTowerDistance;
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
        this.workerTowerDistance = 5;
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
        /*List<Entity> prods = map.getOrDefault(prodType, Collections.emptyList());
        for (Entity prod : prods) {
            prodBehavior(prod, player, rgs);
        }

        /*this should be the tanks.
        List<Entity> units = map.get(unitType);
        if (units != null) {
            for (Entity unit : units) {
                unitBehavior(unit, player, rgs);
            }
        }*/

        workersBehavior(workers,towers,bases.get(0), rgs);

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

        if (workers.size() < 3) {
            if (canAfford(workerType, pgs, player)) {
                train(base, workerType, pgs);
            }else{

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

    public void workersBehavior(List<Entity> workers, List<Entity> towers, Entity base, GameState gs) {


        LinkedList<Entity> freeWorkers = new LinkedList<>();
        freeWorkers.addAll(workers);

        if (workers.isEmpty()) return;

        // every worker
        Iterator<Entity> freeItr = freeWorkers.iterator();
        while (freeItr.hasNext()) {
            Entity worker = freeItr.next();
            //get a perimeter from the worker position
            gs.getCalc().calculateRingFrom(gs.cube2hex(worker.getPos()),workerTowerDistance).forEach((tile) -> {
                boolean insideTowerRange = false;

                //if there is a tower inside the worker perimeter

                for (Entity towerEntity: towers)
                {
                    if(towerEntity.getPos().equals(tile.getCubeCoordinate()))
                    {
                           moveAway(worker,towerEntity.getPos(),gs);
                           freeItr.remove();
                           insideTowerRange = true;
                    }
                }
                if(towers.isEmpty()){
                    moveAway(worker,base.getPos(),gs);
                    freeItr.remove();
                }else {
                    if (!insideTowerRange) {
                        buildTower(worker, towers, gs);
                        freeItr.remove();
                    }
                }

            });
        }


    }

    public void buildTower(Entity worker,List<Entity> towers ,GameState gs){
        //just build tower
        final boolean[] building = {false};
        gs.getCalc().calculateRingFrom(gs.cube2hex(worker.getPos()),1).forEach((tile) -> {

            if(!building[0] && gs.getTerrainAt(tile.getCubeCoordinate()).equals(gs.getSettings().getTerrainType("walkable"))){
                build(worker,towerType,tile.getCubeCoordinate());
                building[0] = true;
            }
        });

        //buildIfNotAlreadyBuilding(worker, towerType, gs);
    }

    /*/TODO Get a distance from tiles, workers are not moving, game ends after building initial towers.
    public void buildTower(Entity worker,List<Entity> towers ,GameState gs){
        //we've pre-calculated number of bases, using that to save a little time...
        List<Vec2d> blues = null;
        List<Vec2d> reds = null;
        List<Vec2d> whites = null; // written as "walkable"
        //before building a tower check distance to towers
        //look for white
        //if in territory find walkable
        //calmovrange

        //gs.getCalc().calculateRingFrom(gs.cube2hex(worker.getPos()),workerTowerDistance);
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
        buildIfNotAlreadyBuilding(worker, towerType, gs);
    }*/


    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();

        //parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
}



/*
*
* Movement?
*
*  gs.getCalc().calculateRingFrom(gs.cube2hex(worker.getPos()),workerTowerDistance).forEach((tile) -> {
                boolean notAvailable = false;

                //TODO Enemy tower Check
                //is the enemy check necessary?

                //if there is a tower inside the worker perimeter, add it to my list of towers from worker
                for (Entity towerEntity: towers)
                {   //this will only add 1 tower for each time the cycle repeats.
                    //can I equal cube coordinates? or do they work with different depths?
                    if(towerEntity.getPos().equals(tile.getCubeCoordinate()))
                    {
                            //The worker has a tower close by by the range of 5.
                            towersFromWorker.add(towerEntity);
                            notAvailable = true;
                    }else{
                        notAvailable = false;
                    }
                }
                //TODO if tile is red or blue  make notAvailable true

                //discard coordinates that are either blue-red or have a tower in them.
                if(!notAvailable)
                {
                    coords.add(tile.getCubeCoordinate());

                }

            });

            //******* END of forEach tiles from worker Position

            //NOW coords should have what remains in our range, which should be "walkable" tile coordinates.
            //if they are nothing left,  the worker should:
            //move away from any tower from our range. OR move away from a colored tile
            if(coords.isEmpty()){
                //if its empty, then there are definitely towers inside our range or
                // we are on colored tiles. from another player
                //FOR NOW lets move away from the first tower found.
                moveAway(worker, towersFromWorker.get(0).getPos(),gs);

                //move away and towards a close walkable?
            }else{
                //if our walkable tile coordinates are inside any of the tower ranges, then we should:
                //see if they make a triangle??
                //see distance to that coordinate
                // for each of our towers inside range or to all other towers inside the players control



*/