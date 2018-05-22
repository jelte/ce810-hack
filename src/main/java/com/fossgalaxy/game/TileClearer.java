package com.fossgalaxy.game;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.parameters.ResourceType;
import com.fossgalaxy.games.tbs.parameters.TerrainType;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

public class TileClearer {

    public static List<CubeCoordinate> convertedTiles = new ArrayList<>();

    public static void clearTiles(Entity host, GameState state) {
        TerrainType hostTerrainType = state.getSettings().getTerrainType((host.getOwner() == 0 ? "blue" : "red") + "_tile");
        TerrainType walkable = state.getSettings().getTerrainType("walkable");
        ResourceType tiles = state.getSettings().getResourceType("tiles_" + (host.getOwner() == 0 ? "blue" : "red"));

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
                    state.getDistance(host.getPos(), a.getPos()) - state.getDistance(host.getPos(), b.getPos())
            ));

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
                    clearTile(grounds.get(m), state, tiles, walkable);
                }
            }
        } catch (NoSuchElementException e) {
        }
    }

    private static void clearTile(CubeCoordinate coord, GameState state, ResourceType tiles, TerrainType walkable)
    {
        state.setTerrainAt(coord, walkable);
        state.setResource(0, tiles, state.getResource(0, tiles) - 1 < 0 ? 0 : state.getResource(0, tiles) - 1);
        state.setResource(1, tiles, state.getResource(1, tiles) - 1 < 0 ? 0 : state.getResource(1, tiles) - 1);
        //convertedTiles.remove(coord);
    }

    public static void connect(Entity host, GameState state, int quantityPerTurn)
    {
        List<CubeCoordinate> grounds = new ArrayList<>();
        List<CubeCoordinate> occuppied = new ArrayList<>();

        List<Entity> closest = new ArrayList<>();
        try {
            for (Entity entity : state.getOwnedEntities(host.getOwner())) {
                if (state.getCalc().isVisible(state.cube2hex(host.getPos()), state.cube2hex(entity.getPos())) && (entity.getType().getName().toLowerCase().endsWith("_base") ||
                        (entity.getType().equals(host.getType()) && !entity.equals(host)))) {
                    closest.add(entity);
                }
            }

            if (closest.size() < 2) {
                return;
            }
            // sort by distance
            closest.sort((a, b) -> (
                state.getDistance(host.getPos(), a.getPos()) - state.getDistance(host.getPos(), b.getPos())
            ));
            TerrainType walkable = state.getSettings().getTerrainType("walkable");
            TerrainType hostTerrainType = state.getSettings().getTerrainType((host.getOwner() == 0 ? "blue" : "red") + "_tile");

            // Only process closest 2
            for (Entity entity : closest) {
                state.getCalc().drawLine(state.cube2hex(host.getPos()), state.cube2hex(entity.getPos())).forEach((tile) -> {
                    if (state.getTerrainAt(tile.getCubeCoordinate()) == null) return;
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
                    if (state.getCalc().isVisible(state.cube2hex(occuppied.get(i)), state.cube2hex(occuppied.get(j)))) {
                        state.getCalc().drawLine(state.cube2hex(occuppied.get(i)), state.cube2hex(occuppied.get(j))).forEach((tile) -> {
                            // Get all ground & contestable tiles
                            if (state.getTerrainAt(tile.getCubeCoordinate()).equals(walkable) && !grounds.contains(tile.getCubeCoordinate())) {
                                grounds.add(tile.getCubeCoordinate());
                            }
                        });
                    }
                }
            }
            List<CubeCoordinate> addedGround = new ArrayList<>();
            // add tiles in between
            for (int i = 0; i < grounds.size() - 1; i++) {
                for (int j = i + 1; j < grounds.size(); j++) {
                    if (state.getCalc().isVisible(state.cube2hex(grounds.get(i)), state.cube2hex(grounds.get(j)))) {
                        state.getCalc().drawLine(state.cube2hex(grounds.get(i)), state.cube2hex(grounds.get(j))).forEach((tile) -> {
                            // Get all ground & contestable tiles
                            if (state.getTerrainAt(tile.getCubeCoordinate()).equals(walkable) && !addedGround.contains(tile.getCubeCoordinate())&& !grounds.contains(tile.getCubeCoordinate())) {
                                addedGround.add(tile.getCubeCoordinate());
                            }
                        });
                    }
                }
            }
            grounds.addAll(addedGround);
            ResourceType tiles = state.getSettings().getResourceType("tiles_"+(host.getOwner() == 0?"blue":"red"));
            // assign terrains
            for (int m = 0; m < quantityPerTurn && m < grounds.size(); m++) {
                state.setTerrainAt(grounds.get(m), hostTerrainType);
                state.setResource(0, tiles, state.getResource(0, tiles) + 1);
                state.setResource(1, tiles, state.getResource(1, tiles) + 1);
             //   TileClearer.convertedTiles.add(grounds.get(m));
            }
        } catch (NoSuchElementException e) {}
    }
}
