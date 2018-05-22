package com.fossgalaxy.game;

import com.fossgalaxy.games.tbs.GameDef;
import com.fossgalaxy.games.tbs.GameState;
import com.fossgalaxy.games.tbs.ai.AIFactory;
import com.fossgalaxy.games.tbs.ai.Controller;
import com.fossgalaxy.games.tbs.io.SettingsIO;
import com.fossgalaxy.games.tbs.metrics.AppEvolver;
import com.fossgalaxy.games.tbs.metrics.GameMetrics;
import com.fossgalaxy.games.tbs.metrics.TurnMetrics;
import com.fossgalaxy.games.tbs.metrics.parameters.EntityProp;
import com.fossgalaxy.games.tbs.parameters.GameSettings;
import io.jenetics.IntegerGene;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

public class AppMetrics extends AppEvolver {

    public AppMetrics(SettingsIO io, GameSettings common, AIFactory ai, String mapFile) {
        super(io, common, ai, mapFile);
    }

    public static void main(String[] args) {

        SettingsIO io = new SettingsIO();
        GameDef def = io.loadGameDef("game.json");
        AIFactory ai = new AIFactory(io, def.getEvalFileName(), def.getRuleFileName(), def.getAiFileName());

        //we need to know the settings from the file
        GameSettings settings = io.loadSettings(def);

        //build the evolver using our base game
        AppEvolver evo = new AppMetrics(io, settings, ai, "map_balance.json");

        /*
        START PARAMETERS
         */

        //tweak the red knight's defence between 0 and 10 in steps of 1
        evo.addParameter(new EntityProp("Blue_base", "health", 1, 10, 1));

        //tweak the blue archer's attack between 0 and 10 in steps of 2
        evo.addParameter(new EntityProp("Red_Tank", "atkMelee", 1, 5, 1));

        /*
        END PARAMETERS
         */


        //we run the GA to find the 'best'
        GameSettings best = evo.evolve(evo::evaluate);

        //print out the settings the GA picked
        System.out.println(best.getEntityType("Blue_Base").getProperty("health"));
        System.out.println(best.getEntityType("Red_Tank").getProperty("atkMelee"));

        //then dump everything we tried to a CSV file...
        dumpToFile(evo, "logs/summary-"+System.currentTimeMillis()+".csv");
    }

    /**
     * GA limits
     *
     * @return a list of termination criteria for the GA.
     */
    public List<Predicate<? super EvolutionResult<IntegerGene, Double>>> getLimits() {
        return Arrays.asList(
                Limits.byExecutionTime(Duration.ofMinutes(120)),
                Limits.byFixedGeneration(100)
        );
    }


    /**
     * A simple fitness function.
     *
     * This plays lots of games, then calculates the results.
     * To run a game use the runGame utility method.
     *
     * @param settings the parameters we're considering
     * @return the fitness for this object
     */
    public Double evaluate(GameSettings settings) {
        LOG.info("Starting evaluation");
        GameState start = map.buildState(settings);

        Map<String, Integer> winRatesOne = new HashMap<>();
        Map<String, Integer> winRatesTwo = new HashMap<>();
        int draws = 0;

        String stageAgent = "dumbBlue"; // the GA might be better here, but we don't have that kind of time...
        List<String> oppAgents = Arrays.asList("dumbRed", "uct", "combRed", "tunedAiRed");

        boolean[] stageFirst = {true, false};

        //repeat (for consistency)
        for (int i=0; i<10; i++) {

            //play all of the oppAgents against our test agent
            for (String agent : oppAgents) {

                //play as both first and second agent
                for (boolean first : stageFirst) {

                    //figure out p1 and p2
                    Controller p1 = first ? ai.buildAI(stageAgent, settings) : ai.buildAI(agent, settings);
                    Controller p2 = first ? ai.buildAI(agent, settings) : ai.buildAI(stageAgent, settings);

                    Controller[] controllers = new Controller[]{
                            p1,
                            p2
                    };

                    GameState state = new GameState(start);
                    GameMetrics metrics = AppEvolver.runGame(state, settings, controllers);

                    //metrics tells us stuff about the game.
                    Integer winner = metrics.getWinner();
                    if (winner != null) {

                        //if someone won, keep track of the win rates
                        if (first && winner == 1) {
                            winRatesOne.put(agent, winRatesOne.getOrDefault(agent, 0) + 1);
                        } else if (!first && winner == 0) {
                            winRatesTwo.put(agent, winRatesTwo.getOrDefault(agent, 0) + 1);
                        }
                    } else {
                        draws++;
                    }
                }
            }

        }

        //this is a metric aimed at trying to reward the GA for balanced games, and punish it ones where one of the
        //players seems to have an advantage...

        double score = draws * 0.5; //half a point for drawing
        for (String agent : oppAgents) {

            int winsWhenFirst = winRatesOne.getOrDefault(agent, 0);
            int winsWhenSecond = winRatesTwo.getOrDefault(agent, 0);

            //punish games that have different results when we're first and second
            int delta = -Math.abs(winsWhenFirst - winsWhenSecond);

            if (delta == 0) {
                //if the results were perfectly matched, award points for each game won
                score += winsWhenFirst + winsWhenSecond;
            } else {
                //if the results where not, then punish the agent for each difference
                score -= delta;
            }
        }

        fitnessScores.put(settings, score);

        return score;
    }


    /**
     * Advanced evaluation function.
     *
     * You only need to deal with this if you want file-based logging.
     *
     * @param settings the current settings object
     * @return the fitness for this settings object
     */
    public Double evaluateWithFiles(GameSettings settings) {

        GameState start = map.buildState(settings);


        //create the file (with a 'random' name for our logging)
        PrintStream out = null;
        try {
            File logFile = new File(String.format("logs/%s.csv", UUID.randomUUID()));
            out = new PrintStream(logFile);
        } catch (FileNotFoundException ex) {
            System.err.println("You didn't create the logs/ directory!");
            System.exit(1);
        }

        //note our paramters, we'll want these later
        int defence = settings.getEntityType("red_knight").getProperty("defRanged");
        int attack = settings.getEntityType("blue_archer").getProperty("atkRanged");


        int[] winCounts = new int[2];
        int[] peiceDifference = new int[2];

        // play 10 games
        for (int i=0; i<10; i++) {

            //if you wanted to play a bunch of different agents, put more for loops here
            Controller[] controllers = new Controller[] {
                    ai.buildAI("RangedRush", settings),
                    ai.buildAI("KnightRush", settings)
            };

            //run the game
            GameState state = new GameState(start);
            GameMetrics metrics = runGame(state, settings, controllers);

            //get the winner
            Integer winner = metrics.getWinner();
            if (winner != null) {
                winCounts[winner]++;
            }

            //an example of more advanced metric - piece difference
            List<String> p1Entities = new ArrayList<>();
            List<String> p2Entities = new ArrayList<>();

            //go though each turn of the game, grabbing the pieces at the end of the turn
            for (int turn=0; turn<metrics.getTurns(); turn++) {
                TurnMetrics turnMetrics = metrics.getMetricsForTurn(turn);

                GameState startState = turnMetrics.getStateAtStart();
                GameState endState = turnMetrics.getStateAtEnd();

                //add the pieces at the end of the turn to lists, for saving to csv
                p1Entities.add( Integer.toString(endState.getOwnedEntities(0).size()) );
                p2Entities.add( Integer.toString(endState.getOwnedEntities(1).size()) );

                //calcuate the piece difference
                int peices = endState.getEntities().size() -  startState.getEntities().size();
                peiceDifference[turnMetrics.getPlayerID()] += peices;
            }

            //if file logging is enabled, log two CSV lines - one for each player (per game)
            if (out != null) {
                out.printf("%d,%d,%d,%d,%s%n", 0, i, attack, defence, String.join(",", p1Entities));
                out.printf("%d,%d,%d,%d,%s%n", 1, i, attack, defence, String.join(",", p2Entities));
            }

        }

        //we're done writing to the file, close it.
        if (out != null) {
            out.close();
        }

        double score = winCounts[1] - winCounts[0];
        fitnessScores.put(settings, score);

        return score;
    }


}