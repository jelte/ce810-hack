package com.fossgalaxy.game.actions;

import com.fossgalaxy.game.orders.ExpandOrder;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.actions.AbstractAction;
import com.fossgalaxy.games.tbs.order.GenerateOrder;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.parameters.ResourceType;
import com.fossgalaxy.games.tbs.parameters.TerrainType;
import com.fossgalaxy.object.annotations.ObjectDef;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.awt.*;

public class ExpandAction extends AbstractAction {

    private final String toString;

    @ObjectDef(".Expand")
    public ExpandAction() {
        toString = "Expand";
    }

    @Override
    public boolean canAutomate() {
        return true;
    }

    @Override
    public Order generateOrder(CubeCoordinate co, GameState s) {
        return new ExpandOrder();
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
    public String toString() {
        return toString;
    }
}
