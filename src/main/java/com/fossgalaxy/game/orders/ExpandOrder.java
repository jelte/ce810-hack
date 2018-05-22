package com.fossgalaxy.game.orders;

import com.fossgalaxy.game.TileClearer;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.order.Order;
import com.fossgalaxy.games.tbs.parameters.ResourceType;
import com.fossgalaxy.games.tbs.parameters.TerrainType;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ExpandOrder implements Order {

    public ExpandOrder() {
    }

    @Override
    public void doOrder(Entity host, GameState state) {
        TileClearer.connect(host, state, host.getProperty("expandRate", 100));
    }
}
