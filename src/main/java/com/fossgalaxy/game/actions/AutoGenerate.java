package com.fossgalaxy.game.actions;

import com.fossgalaxy.games.tbs.order.GenerateOrder;
import com.fossgalaxy.games.tbs.parameters.ResourceType;
import com.fossgalaxy.games.tbs.actions.GeneratorAction;
import com.fossgalaxy.object.annotations.ObjectDef;

/**
 * Created by jm17913 on 16/05/2018.
 */
public class AutoGenerate extends GeneratorAction {

    @ObjectDef("AutoGenerate")
    public AutoGenerate(ResourceType type, int quantityPerTurn) {
        super(type, quantityPerTurn);
    }

    @Override
    public boolean canAutomate() {
        return true;
    }

}
