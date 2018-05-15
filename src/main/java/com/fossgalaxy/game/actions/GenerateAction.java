package com.fossgalaxy.game.actions;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.actions.AbstractAction;
import com.fossgalaxy.games.tbs.order.GenerateOrder;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.parameters.ResourceType;
import com.fossgalaxy.object.annotations.ObjectDef;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.awt.*;

public class GenerateAction extends AbstractAction {
    private final ResourceType resourceType;
    private final int quantityPerTurn;
    private final String toString;

    @ObjectDef("Generate")
    public GenerateAction(ResourceType resourceType, int quantityPerTurn) {
        this.resourceType = resourceType;
        this.quantityPerTurn = quantityPerTurn;
        toString = String.format("Generate %d %s", quantityPerTurn, resourceType.getName());
    }

    @Override
    public Order generateOrder(CubeCoordinate co, GameState gameState) {
        return new GenerateOrder(resourceType, quantityPerTurn);
    }

    @Override
    public Color getHintColour() {
        return HINT_MINE_COLOUR;
    }

    @Override
    public Color getBorderColour() {
        return HINT_MINE_COLOUR;
    }

    @Override
    public boolean canAutomate() {
        return true;
    }




    @Override
    public String toString() {
        return toString;
    }


}
