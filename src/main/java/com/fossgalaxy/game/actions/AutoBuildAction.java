package com.fossgalaxy.game.actions;

import com.fossgalaxy.game.orders.AutoBuildOrder;
import com.fossgalaxy.game.orders.ExpandOrder;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.actions.AbstractAction;
import com.fossgalaxy.games.tbs.actions.BuildAction;
import com.fossgalaxy.games.tbs.actions.MinesAction;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.parameters.EntityType;
import com.fossgalaxy.games.tbs.parameters.ResourceType;
import com.fossgalaxy.object.annotations.ObjectDef;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.awt.*;

/**
 * Created by py17334 on 15/05/2018.
 */
public class AutoBuildAction extends AbstractAction {

    private final EntityType type;
    private final String toString;

    @ObjectDef("AutoBuild")
    public AutoBuildAction(EntityType type) {
        this.type = type;
        toString = String.format("AutoBuildAction %s", type.getName());
    }

    @Override
    public boolean canAutomate() {
        return true;
    }

    @Override
    public Order generateOrder(CubeCoordinate co, GameState s) {
        return new AutoBuildOrder(type, co);
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
