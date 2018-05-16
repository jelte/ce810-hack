package com.fossgalaxy.game.actions;

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
public class AutoBuild extends BuildAction {

    @ObjectDef("AutoBuild")
    public AutoBuild(EntityType type) {
        super(type);
    }

    @Override
    public boolean canAutomate() {
        return true;
    }
}
