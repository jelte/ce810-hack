package com.fossgalaxy.game.orders;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.entity.Resource;
import com.fossgalaxy.games.tbs.order.BuildOrder;
import com.fossgalaxy.games.tbs.order.GenerateOrder;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.parameters.EntityType;
import com.fossgalaxy.games.tbs.parameters.ResourceType;
import org.codetome.hexameter.core.api.CubeCoordinate;

public class AutoGenerateOrder implements Order {

    private final ResourceType resourceType;
    private final int quantity;

    public AutoGenerateOrder(ResourceType type, int quantity) {
        this.resourceType = type;
        this.quantity = quantity;
    }

    @Override
    public void doOrder(Entity host, GameState state) {
        int quantity = this.quantity;
        for (Resource resource : state.getResources()) {
            if (state.getTerrainAt(resource.getLocation()).getName().startsWith(host.getOwner() == 0 ? "blue" : "red")) {
                quantity += resource.getAmountPerTurn();
            }
        }


        new GenerateOrder(resourceType, quantity).doOrder(host, state);
    }
}
