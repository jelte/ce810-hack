package com.fossgalaxy.game;

import com.fossgalaxy.games.tbs.App;
import com.fossgalaxy.games.tbs.io.SettingsIO;

public class Game {
	public static void main(String[] args) {

        App.run(SettingsIO.buildWithExtras("com.fossgalaxy.game"), "game.json", "game-map.json", "Player", "Player","Player");

    }

}