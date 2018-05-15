package com.fossgalaxy.game.orders;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.parameters.TerrainType;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.util.ArrayList;
import java.util.List;

public class ExpandOrder implements Order {

    private final int quantityPerTurn;

    public ExpandOrder(int quantityPerTurn) {
        this.quantityPerTurn = quantityPerTurn;
    }

    @Override
    public void doOrder(Entity host, GameState state) {
        List<CubeCoordinate> grounds = new ArrayList<>();
        List<CubeCoordinate> occuppied = new ArrayList<>();

        List<Entity> closest = new ArrayList();

        for (Entity entity : state.getOwnedEntities(host.getOwner())) {
            if (entity.getType().equals(host.getType()) && !entity.equals(host) && state.getCalc().isVisible(state.cube2hex(host.getPos()), state.cube2hex(entity.getPos()))) {
                closest.add(entity);
            }
        }
        if (closest.size() < 2) {
            return;
        }
        // sort by distance
        closest.sort((a, b) -> (
            state.getDistance(host.getPos(), a.getPos()) < state.getDistance(host.getPos(), b.getPos()) ? -1 : 1
        ));
        TerrainType walkable = state.getSettings().getTerrainType("walkable");
        TerrainType hostTerrainType = state.getSettings().getTerrainType(host.getType().getName().substring(0, host.getType().getName().indexOf("_") -1));

        // Only process closest 2
        for (Entity entity : closest.subList(0, 1)) {
            state.getCalc().drawLine(state.cube2hex(host.getPos()), state.cube2hex(entity.getPos())).forEach((tile) -> {
                // Get all ground & contestable tiles
                if (state.getTerrainAt(tile.getCubeCoordinate()).equals(walkable) && !grounds.contains(tile.getCubeCoordinate())) {
                    grounds.add(tile.getCubeCoordinate());
                }

                // Get all occupied tiles
                if (state.getTerrainAt(tile.getCubeCoordinate()).equals(hostTerrainType) && !occuppied.contains(tile.getCubeCoordinate())) {
                    occuppied.add(tile.getCubeCoordinate());
                }
            });
        }
        // add tiles in between
        for (int i = 0; i < occuppied.size() - 1; i++) {
            for (int j = i + 1; j < occuppied.size(); j++) {
                state.getCalc().drawLine(state.cube2hex(occuppied.get(i)), state.cube2hex(occuppied.get(j))).forEach((tile) -> {
                    // Get all ground & contestable tiles
                    if (state.getTerrainAt(tile.getCubeCoordinate()).equals(walkable) && !grounds.contains(tile.getCubeCoordinate())) {
                        grounds.add(tile.getCubeCoordinate());
                    }
                });
            }
        }
        // assign terrains
        for (int m = 0; m < quantityPerTurn; m++) {
            state.setTerrainAt(grounds.get(m), hostTerrainType);
        }
    }
}
