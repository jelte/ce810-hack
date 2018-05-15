# Readme

This is a template game, that pulls in the base game as a dependency. This makes it easier for us to change
the base game without upsetting your code as much

Change the classes "Game" and "GameEditor" to your "GameName" and "GameNameEditor"

Open the pom.xml and change the groupId from "com.fossgalaxy.game" to "com.fossgalaxy.GameName" as well as altering the
artifactId and the name

Also change the game.json file, the other json files to something game specific and the reference to game.json in
Game.java and GameEditor.java

It is best to start devloping with the map editor, and then you can move to testing the game itself.

Games must have a valid map file to run