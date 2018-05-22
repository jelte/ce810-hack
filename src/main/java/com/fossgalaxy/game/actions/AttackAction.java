package com.fossgalaxy.game.actions;

import com.fossgalaxy.game.orders.AttackOrder;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.object.annotations.ObjectDef;
import org.codetome.hexameter.core.api.CubeCoordinate;

public class AttackAction extends com.fossgalaxy.games.tbs.actions.MeleeAttackAction {
    @ObjectDef("AttackA")
    public AttackAction() {
        super();
    }

    public Order generateOrder(CubeCoordinate co, GameState s) {
        Entity e = s.getEntityAt(co);
        return e == null ? null : new AttackOrder(e.getID());
    }
}
