package com.fossgalaxy.game;

import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.entity.Entity;
import com.fossgalaxy.games.tbs.rules.Rule;
import com.fossgalaxy.object.annotations.ObjectDef;

import java.util.Map;
import java.util.stream.Collectors;

public class MostOfEntityForTurns implements Rule {

    private final String winEntity;
    private final int threshold;
    //Hopefully it works with abstract entities, threshold is the Turn threshold
    @ObjectDef("MostOfEntityForTurns")
    public MostOfEntityForTurns(final String winEntity, final int threshold) {
        this.winEntity = winEntity;
        this.threshold = threshold;
    }

    @Override
    public Integer getWinner(final GameState state) {
       Map.Entry<Integer,Long> maxKey = null;
       int key = 0;
        if(state.getTime()>= threshold){
            Map<Integer, Long> counts = state.getEntities().stream()
                    .filter(x -> x.getType().getName().equals(winEntity))
                    .collect(Collectors.groupingBy(Entity::getOwner, Collectors.counting()));
            //simple, i don't know if Map Keys stay the same though...
            //thats the reason why the extra key integer
            for (Map.Entry<Integer, Long> entry : counts.entrySet()) {
                if(maxKey == null || entry.getValue().compareTo(maxKey.getValue())>0){
                    maxKey = entry;
                    key = entry.getKey();
                }
            }
            
            return key;

        }
        return NO_WINNER;
    }





}
