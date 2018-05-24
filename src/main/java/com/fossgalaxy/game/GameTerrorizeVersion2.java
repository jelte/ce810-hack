package com.fossgalaxy.game;

import com.fossgalaxy.games.tbs.App;
import com.fossgalaxy.games.tbs.io.SettingsIO;

public class GameTerrorizeVersion2 {
	public static void main(String[] args) {
        App.run(SettingsIO.buildWithExtras("com.fossgalaxy.game"), "game-version2.json", "game-map.json", "Player", "combRed","Player");
    }
}