package com.fossgalaxy.game.actions;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.parameters.EntityType;
import com.fossgalaxy.games.tbs.parameters.TerrainType;
import org.codetome.hexameter.core.api.CubeCoordinate;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by py17334 on 18/05/2018.
 */
public class UpgradeAction  extends com.fossgalaxy.games.tbs.actions.UpgradeAction{

    protected EntityType type;

    public UpgradeAction(EntityType first, EntityType second) {
        super(first, second);
    }

}
