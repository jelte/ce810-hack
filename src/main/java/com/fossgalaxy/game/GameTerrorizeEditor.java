package com.fossgalaxy.game;

import com.fossgalaxy.games.tbs.editor.Editor;
import com.fossgalaxy.games.tbs.io.SettingsIO;

public class GameTerrorizeEditor {

    public static void main(String[] args) {
        Editor.run(SettingsIO.buildWithExtras("com.fossgalaxy.game"),"game.json", "game-map.json" );
    }
}
