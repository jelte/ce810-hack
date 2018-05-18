package com.fossgalaxy.game.actions;

import com.fossgalaxy.game.orders.SelfDestructOrder;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.object.annotations.ObjectDef;
import org.codetome.hexameter.core.api.CubeCoordinate;

public class SelfDestructAction extends com.fossgalaxy.games.tbs.actions.SellAction {
    private final double regen;

    @ObjectDef("SelfDestruct")
    public SelfDestructAction(double regen) {
        super(regen);
        this.regen = regen;
    }

    public Order generateOrder(CubeCoordinate co, GameState s) {
        return new SelfDestructOrder(this.regen);
    }
}
