package com.fossgalaxy.game.orders;

import com.fossgalaxy.game.TileClearer;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.order.AttackOrderMelee;
import java.util.UUID;

public class AttackOrder extends com.fossgalaxy.games.tbs.order.AttackOrderMelee {

    public AttackOrder(UUID target) {
        super(target);
    }

    public void doOrder(Entity entity, GameState state) {
        Entity target = state.getEntityByID(this.targetID);
        if (target != null && target.getType().getName().endsWith("_Tower")) {
            TileClearer.clearTiles(target, state);
        }
        new AttackOrderMelee(targetID).doOrder(entity, state);
        if (target != null && target.getHealth() <= 0) {
            entity.setPos(target.getPos());
        }
    }

}
