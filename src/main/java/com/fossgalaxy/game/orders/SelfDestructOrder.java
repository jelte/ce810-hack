package com.fossgalaxy.game.orders;

import com.fossgalaxy.game.TileClearer;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;

public class SelfDestructOrder extends com.fossgalaxy.games.tbs.order.SellOrder {

    public SelfDestructOrder(double regen) {
        super(regen);
    }

    public void doOrder(Entity entity, GameState state) {
        TileClearer.clearTiles(entity, state);
        super.doOrder(entity, state);
    }
}
