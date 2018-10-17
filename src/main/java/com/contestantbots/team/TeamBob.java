package com.contestantbots.team;

import com.contestantbots.util.*;
import com.scottlogic.hackathon.client.Client;
import com.scottlogic.hackathon.game.Bot;
import com.scottlogic.hackathon.game.Direction;
import com.scottlogic.hackathon.game.GameState;
//import com.scottlogic.hackathon.game.Map;
import com.scottlogic.hackathon.game.Move;
import com.scottlogic.hackathon.game.Player;
import com.scottlogic.hackathon.game.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class TeamBob extends Bot {
    private final GameStateLogger gameStateLogger;

    public TeamBob() {
        super("Example Bot");
        gameStateLogger = new GameStateLogger(getId());
    }

    @Override
    public List<Move> makeMoves(final GameState gameState) {
        gameStateLogger.process(gameState);
        List<Move> moves = new ArrayList<>();
        List<Position> nextPositions = new ArrayList<>();
        Map<Player, Position> assignedPlayerDestinations = new HashMap<>();

        moves.addAll(doExplore(gameState, nextPositions, moves, assignedPlayerDestinations));
        
        

        return moves;
    }
    
    private List<Move> doExplore(final GameState gameState, final List<Position> nextPositions, List<Move> moves, Map<Player, Position> assignedPlayerDestinations) {
        List<Move> exploreMoves = new ArrayList<>();
        moves.addAll(doCollect(gameState, assignedPlayerDestinations, nextPositions));
        exploreMoves.addAll(gameState.getPlayers().stream()
                .filter(player -> isMyPlayer(player))
                .map(player -> doMove(gameState, nextPositions, player))
                .collect(Collectors.toList()));
        
        Set<Position> collectablePositions = gameState.getCollectables().stream()
                .map(collectable -> collectable.getPosition())
                .collect(Collectors.toSet());
        Set<Player> players = gameState.getPlayers().stream()
                .filter(player -> isMyPlayer(player))
                .collect(Collectors.toSet());
        
        List<Route> collectableRoutes = new ArrayList<>();
        for (Position collectablePosition : collectablePositions) {
            for (Player player : players) {
                int distance = gameState.getMap().distance(player.getPosition(), collectablePosition);
                Route route = new Route(player, collectablePosition, distance);
                collectableRoutes.add(route);
            }
        }
        Collections.sort(collectableRoutes);
        
        System.out.println(exploreMoves.size() + " players exploring");
        return exploreMoves;
    }
    
    private Move doMove(final GameState gameState, final List<Position> nextPositions, final Player player) {
        Direction direction;
        do {
            direction = Direction.random();
        } while (!canMove(gameState, nextPositions, player, direction));
        return new MoveImpl(player.getId(), direction);
    }
    

    private boolean isMyPlayer(final Player player) {
        return player.getOwner().equals(getId());
    }
    
    private List<Move> doCollect(final GameState gameState, final Map<Player, Position> assignedPlayerDestinations, final List<Position> nextPositions) {
        List<Move> collectMoves = new ArrayList<>();
        System.out.println(collectMoves.size() + " players collecting");
        return collectMoves;
    }
    
    private boolean canMove(final GameState gameState, final List<Position> nextPositions, final Player player, final Direction direction) {
        Set<Position> outOfBounds = gameState.getOutOfBoundsPositions();
        Position newPosition = gameState.getMap().getNeighbour(player.getPosition(), direction);
        if (!nextPositions.contains(newPosition)
            && !outOfBounds.contains(newPosition)) {
            nextPositions.add(newPosition);
            return true;
        } else {
            return false;
        }
    }
    
    


    /*
     * Run this main as a java application to test and debug your code within your IDE.
     * After each turn, the current state of the game will be printed as an ASCII-art representation in the console.
     * You can study the map before hitting 'Enter' to play the next phase.
     */
    public static void main(String ignored[]) throws Exception {

        final String[] args = new String[]{
                /*
                Pick the map to play on
                -----------------------
                Each successive map is larger, and has more out-of-bounds positions that must be avoided.
                Make sure you only have ONE line uncommented below.
                 */
                "--map",
//                    "VeryEasy",
                    "Easy",
//                    "Medium",
//                    "LargeMedium",
//                    "Hard",

                /*
                Pick your opponent bots to test against
                ---------------------------------------
                Every game needs at least one opponent, and you can pick up to 3 at a time.
                Uncomment the bots you want to face, or specify the same opponent multiple times to face multiple
                instances of the same bot.
                 */
                "--bot",
//                    "Default", // Players move in random directions
                    "Milestone1", // Players just try to stay out of trouble
//                    "Milestone2", // Some players gather collectables, some attack enemy players, and some attack enemy spawn points
//                    "Milestone3", // Strategy dynamically updates based on the current state of the game
//                    "FastExpansion", // Advanced dynamic strategy where players work together

                /*
                Enable debug mode
                -----------------
                This causes all Bots' 'makeMoves()' methods to be invoked from the main thread,
                and prevents them from being disqualified if they take longer than the usual time limit.
                This allows you to run in your IDE debugger and pause on break points without timing out.

                Comment this line out if you want to check that your bot is running fast enough.
                 */
                "--debug",

                // Use this class as the 'main' Bot
                "--className", TeamBob.class.getName()
        };

        Client.main(args);
    }

}