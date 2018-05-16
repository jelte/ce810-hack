package com.fossgalaxy.game.actions;

import com.fossgalaxy.game.orders.AutoGenerateOrder;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.order.GenerateOrder;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.parameters.ResourceType;
import com.fossgalaxy.games.tbs.actions.GeneratorAction;
import com.fossgalaxy.object.annotations.ObjectDef;
import org.codetome.hexameter.core.api.CubeCoordinate;

/**
 * Created by jm17913 on 16/05/2018.
 */
public class AutoGenerate extends GeneratorAction {
    private final ResourceType resourceType;
    private final int quantityPerTurn;

    @ObjectDef("AutoGenerate")
    public AutoGenerate(ResourceType type, int quantityPerTurn) {
        super(type, quantityPerTurn);
        this.resourceType = type;
        this.quantityPerTurn = quantityPerTurn;
    }

    @Override
    public boolean canAutomate() {
        return true;
    }

    public Order generateOrder(CubeCoordinate co, GameState s) {
        return new AutoGenerateOrder(this.resourceType, this.quantityPerTurn);
    }

}
