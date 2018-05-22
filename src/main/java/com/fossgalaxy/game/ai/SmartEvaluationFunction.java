package com.fossgalaxy.game.ai;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.parameters.EntityType;
import com.fossgalaxy.object.annotations.ObjectDef;
import com.fossgalaxy.object.annotations.ObjectDefStatic;
import rts.ai.evaluation.EvaluationFunction;

import java.util.Collection;
import java.util.Iterator;

public class SmartEvaluationFunction extends EvaluationFunction {
    private final float resourceBonus;
    private final float unitBonus;

    @ObjectDef("Smart")
    public SmartEvaluationFunction(float resourceBonus, float unitBonus) {
        this.resourceBonus = resourceBonus;
        this.unitBonus = unitBonus;
    }

    @ObjectDefStatic("SmartDefault")
    public static SmartEvaluationFunction getDefault() {
        return new SmartEvaluationFunction(20.0F, 40.0F);
    }

    public float evaluate(int maxplayer, int minplayer, GameState gs) {
        float s1 = this.base_score(maxplayer, gs);
        float s2 = this.base_score(minplayer, gs);
        return s1 + s2 == 0.0F ? 0.5F : 2.0F * s1 / (s1 + s2) - 1.0F;
    }

    public float base_score(int player, GameState gs) {
        float score = 0.0F;

        Entity base = null;
        int workerCount = 0;
        int tankCounter = 0;
        for (Entity e : gs.getOwnedEntities(player)) {
            if (e.getType().getName().toLowerCase().endsWith("base")) {
                base = e;
            }
            if (e.getType().getName().toLowerCase().endsWith("builder")) {
                workerCount++;
            }
            if (e.getType().getName().toLowerCase().endsWith("tank")) {
                tankCounter++;
            }
        }

        float tiles = (float) gs.getResource(player, player == 0 ? "tiles_blue" : "tiles_red");
        float otherTiles = (float) gs.getResource((player+1)%2, player == 0 ? "tiles_red" : "tiles_blue");
        score = base == null ? 0 : ((workerCount > 2 ? -100000 : workerCount * 5000) + (tankCounter * 4000) + base.getHealth() * 10000) + (tiles / (tiles+otherTiles))*1000 + (gs.getResource(player, "energy")/(gs.getResource(player, "energy")+gs.getResource((player+1)%2, "energy"))) * 1000;

        return score;
    }

    public float upperBound(GameState gs) {
        return 1.0F;
    }
}
