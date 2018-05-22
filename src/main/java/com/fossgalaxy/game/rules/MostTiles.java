package com.fossgalaxy.game.rules;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.rules.Rule;
import com.fossgalaxy.object.annotations.ObjectDef;
import org.codetome.hexameter.core.api.CubeCoordinate;

public class MostTiles implements Rule {
    private final int turns;

    @ObjectDef("MostTiles")
    public MostTiles(int turns) {
        this.turns = turns;
    }

    public Integer getWinner(GameState state) {
        if (state.getTime() < turns) {
            return NO_WINNER;
        }

        int blue = 0, red = 0;
        for (int x = 0; x < state.getWidth(); x++) {
            for (int z = 0; z < state.getHeight(); z++) {
                try {
                    blue += state.getTerrainAt(CubeCoordinate.fromCoordinates(x, z)).getName().startsWith("blue") ? 1 : 0;
                    red += state.getTerrainAt(CubeCoordinate.fromCoordinates(x, z)).getName().startsWith("red") ? 1 : 0;
                } catch (Exception e) {}
            }
        }
        int total = state.getWidth() * state.getHeight();
        int totalResources = state.getResource(0, "energy") +state.getResource(1, "energy");

        double scoreBlue = ((double) blue / total) + ((double) state.getResource(0, "energy") / totalResources);
        double scoreRed = ((double) red / total) + ((double) state.getResource(1, "energy") / totalResources);

        System.out.println(scoreBlue + " - " + scoreRed);
        return scoreBlue == scoreRed ? NO_WINNER : (scoreBlue > scoreRed ? 0 : 1);
    }
}
