package com.fossgalaxy.game.orders;

import com.fossgalaxy.game.actions.WrapperAction;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.ui.GameAction;

public class WrapperOrder implements Order {

    private final Order actualOrder;

    public WrapperOrder(Order actualOrder) {
        this.actualOrder = actualOrder;
    }

    @Override
    public void doOrder(Entity host, GameState state) {
        actualOrder.doOrder(host, state);

        for (Entity entity : state.getOwnedEntities(host.getOwner())) {
           for (GameAction action : entity.getType().getAvailableActions()) {
               if (action != null && action.canAutomate() && action instanceof WrapperAction) {
                   action.generateOrder(host.getPos(), state).doOrder(entity, state);
               }
           }
        }
    }
}
