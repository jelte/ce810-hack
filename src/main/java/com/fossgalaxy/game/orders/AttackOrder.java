package com.fossgalaxy.game.orders;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.ai.rules.AttackMeleeRule;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.order.AttackOrderMelee;
import com.fossgalaxy.games.tbs.parameters.TerrainType;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class AttackOrder extends com.fossgalaxy.games.tbs.order.AttackOrderMelee {

    public AttackOrder(UUID target) {
        super(target);
    }

    public void doOrder(Entity entity, GameState state) {
        if (state.getEntityByID(this.targetID) != null && state.getEntityByID(this.targetID).getType().getName().endsWith("_Tower")) {
            clearTiles(state.getEntityByID(this.targetID), state);
        }
        new AttackOrderMelee(targetID).doOrder(entity, state);
    }

    private void clearTiles(Entity host, GameState state)
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

            // assign terrains
            for (int m = 0; m < grounds.size(); m++) {
                state.setTerrainAt(grounds.get(m), walkable);
            }
        } catch (NoSuchElementException e) {}
    }
}
