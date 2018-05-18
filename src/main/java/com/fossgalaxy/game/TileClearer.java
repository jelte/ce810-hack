package com.fossgalaxy.game;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.parameters.ResourceType;
import com.fossgalaxy.games.tbs.parameters.TerrainType;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TileClearer {

    public static void clearTiles(Entity host, GameState state)
    {
        try {
            List<CubeCoordinate> grounds = new ArrayList<>();
            List<CubeCoordinate> occuppied = new ArrayList<>();

            List<Entity> closest = new ArrayList<>();

            for (Entity entity : state.getOwnedEntities(host.getOwner())) {
                if (entity.getType().getName().toLowerCase().endsWith("_base") ||
                        (entity.getType().equals(host.getType()) && !entity.equals(host) && state.getCalc().isVisible(state.cube2hex(host.getPos()), state.cube2hex(entity.getPos())))) {
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
            TerrainType hostTerrainType = state.getSettings().getTerrainType((host.getOwner() == 0 ? "blue" : "red") + "_tile");

            // Only process closest 2
            for (int i = 0; i < 2; i++) {
                Entity entity = closest.get(i);
                state.getCalc().drawLine(state.cube2hex(host.getPos()), state.cube2hex(entity.getPos())).forEach((tile) -> {
                    if (state.getTerrainAt(tile.getCubeCoordinate()) == null) return;
                    // Get all ground & contestable tiles
                    if (state.getTerrainAt(tile.getCubeCoordinate()).equals(hostTerrainType) && !grounds.contains(tile.getCubeCoordinate())) {
                        grounds.add(tile.getCubeCoordinate());
                        occuppied.add(tile.getCubeCoordinate());
                    }
                });
            }
            // add tiles in between
            for (int i = 0; i < occuppied.size() - 1; i++) {
                for (int j = i + 1; j < occuppied.size(); j++) {
                    try {
                        if (state.getCalc().isVisible(state.cube2hex(occuppied.get(i)), state.cube2hex(occuppied.get(j)))) {
                            state.getCalc().drawLine(state.cube2hex(occuppied.get(i)), state.cube2hex(occuppied.get(j))).forEach((tile) -> {
                                // Get all ground & contestable tiles
                                if (state.getTerrainAt(tile.getCubeCoordinate()).equals(hostTerrainType) && !grounds.contains(tile.getCubeCoordinate())) {
                                    grounds.add(tile.getCubeCoordinate());
                                }
                            });
                        }
                    } catch (NoSuchElementException e) {
                    }
                }
            }

            ResourceType tiles = state.getSettings().getResourceType("tiles_"+(host.getOwner() == 0?"blue":"red"));
            // assign terrains
            for (int m = 0; m < grounds.size(); m++) {
                if (
                        state.getEntityAt(grounds.get(m)) == null
                                || host.getPos().equals(grounds.get(m))
                                || !(
                                    state.getEntityAt(grounds.get(m)).getType().getName().endsWith("Tower")
                                    || state.getEntityAt(grounds.get(m)).getType().getName().endsWith("base")
                                )
                ) {
                    state.setTerrainAt(grounds.get(m), walkable);
                    state.setResource(0, tiles, state.getResource(0, tiles) - 1);
                    state.setResource(1, tiles, state.getResource(1, tiles) - 1);
                }
            }
        } catch (NoSuchElementException e) {}
    }
}
