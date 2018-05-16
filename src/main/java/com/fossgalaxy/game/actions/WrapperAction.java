package com.fossgalaxy.game.actions;

import com.fossgalaxy.game.orders.ExpandOrder;
import com.fossgalaxy.game.orders.WrapperOrder;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.actions.AbstractAction;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.ui.GameAction;
import com.fossgalaxy.object.annotations.ObjectDef;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.awt.*;

public class WrapperAction extends AbstractAction {
    private final GameAction actualAction;

    @ObjectDef("Wrapper")
    public WrapperAction(GameAction actualAction) {
        this.actualAction = actualAction;
        System.out.println(actualAction.getClass());

       //toString = actualAction.toString();
    }

    public boolean isPossible(Entity var1, GameState var2) { return actualAction.isPossible(var1, var2); }
    public boolean isPossible(Entity var1, GameState var2, CubeCoordinate var3) { return actualAction.isPossible(var1, var2, var3); }

    @Override
    public Order generateOrder(CubeCoordinate co, GameState s) { return new WrapperOrder(actualAction.generateOrder(co, s)); }

    @Override
    public boolean canAutomate() { return actualAction.canAutomate(); }

    @Override
    public Color getHintColour() {
        return actualAction.getHintColour();
    }

    @Override
    public Color getBorderColour() {
        return actualAction.getBorderColour();
    }

    @Override
    public String toString() {
        return actualAction.toString();
    }
}
