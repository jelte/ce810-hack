package com.fossgalaxy.game.orders;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.order.BuildOrder;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.parameters.EntityType;
import org.codetome.hexameter.core.api.CubeCoordinate;

public class AutoBuildOrder extends BuildOrder {

    public AutoBuildOrder(EntityType type, CubeCoordinate cube) {
        super(type, cube);
    }

    @Override
    public void doOrder(Entity host, GameState state) {
        System.out.println("Autobuild order: " + host.getProperty("tick", 0));
        if (host.getProperty("tick", 0) >= 0) {
            host.setHealth(0);
            this.build(host, state);
            state.setTerrainAt(host.getPos(), state.getSettings().getTerrainType((host.getOwner() == 0 ? "blue" : "red") + "_tile"));
        } else {
            host.setProperty("tick", 1);
        }
    }
}
