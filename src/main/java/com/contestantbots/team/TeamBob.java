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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class TeamBob extends Bot {
    private final GameStateLogger gameStateLogger;
    private Set<Position> unseenPositions = new HashSet<>();
    private Set<Position> enemySpawnPointPositions = new HashSet<>();
    

    public TeamBob() {
        super("Example Bot");
        gameStateLogger = new GameStateLogger(getId());
    }
    
    @Override
    public void initialise(final GameState gameState) {
    	 for (int x = 0; x < gameState.getMap().getWidth(); x++) {
             for (int y = 0; y < gameState.getMap().getHeight(); y++) {
                 unseenPositions.add(new Position(x, y));
             }
         }
    }

    @Override
    public List<Move> makeMoves(final GameState gameState) {
        gameStateLogger.process(gameState);
        List<Move> moves = new ArrayList<>();
        List<Position> nextPositions = new ArrayList<>();
        
        Map<Player, Position> assignedPlayerDestinations = new HashMap<>();

        moves.addAll(doExplore(gameState, nextPositions, moves, assignedPlayerDestinations));
        
        updateUnseenLocations(gameState);
        updateEnemySpawnPointLocations(gameState);
        moves.addAll(doAttack(gameState, assignedPlayerDestinations, nextPositions));
        
        

        return moves;
    }
    
    private List<Move> doExplore(final GameState gameState, final List<Position> nextPositions, List<Move> moves, Map<Player, Position> assignedPlayerDestinations) {
        List<Move> exploreMoves = new ArrayList<>();
        moves.addAll(doCollect(gameState, assignedPlayerDestinations, nextPositions));
        exploreMoves.addAll(gameState.getPlayers().stream()
                .filter(player -> isMyPlayer(player))
                .map(player -> doMove(gameState, nextPositions, player))
                .collect(Collectors.toList()));
        
        
        moves.addAll(doExploreUnseen(gameState, assignedPlayerDestinations, nextPositions));
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
        Set<Position> collectablePositions = gameState.getCollectables().stream()
                .map(collectable -> collectable.getPosition())
                .collect(Collectors.toSet());
        Set<Player> players = gameState.getPlayers().stream()
                .filter(player -> isMyPlayer(player))
                .collect(Collectors.toSet());
        
        List<Route> collectableRoutes = generateRoutes(gameState, players, collectablePositions);
        collectMoves.addAll(assignRoutes(gameState, assignedPlayerDestinations, nextPositions, collectableRoutes));
        for (Position collectablePosition : collectablePositions) {
            for (Player player : players) {
                int distance = gameState.getMap().distance(player.getPosition(), collectablePosition);
                Route route = new Route(player, collectablePosition, distance);
                collectableRoutes.add(route);
            }
        }

        for (Route route : collectableRoutes) {
            if (!assignedPlayerDestinations.containsKey(route.getPlayer())
                    && !assignedPlayerDestinations.containsValue(route.getDestination())) {
                Optional<Direction> direction = gameState.getMap().directionsTowards(route.getPlayer().getPosition(), route.getDestination()).findFirst();
                if (direction.isPresent() && canMove(gameState, nextPositions, route.getPlayer(), direction.get())) {
                    collectMoves.add(new MoveImpl(route.getPlayer().getId(), direction.get()));
                    assignedPlayerDestinations.put(route.getPlayer(), route.getDestination());
                }
            }
        }
        
        Collections.sort(collectableRoutes);
        return collectMoves;
    }
    
    private List<Route> generateRoutes(final GameState gameState, Set<Player> players, Set<Position> destinations) {
        List<Route> routes = new ArrayList<>();
        for (Position destination : destinations) {
            for (Player player : players) {
                int distance = gameState.getMap().distance(player.getPosition(), destination);
                Route route = new Route(player, destination, distance);
                routes.add(route);
            }
        }
        return routes;
    }
    
    private List<Move> assignRoutes(final GameState gameState, final Map<Player, Position> assignedPlayerDestinations, final List<Position> nextPositions, List<Route> routes) {
        return routes.stream()
                .filter(route -> !assignedPlayerDestinations.containsKey(route.getPlayer())&& !assignedPlayerDestinations.containsValue(route.getDestination()))
                .map(route -> {
                    Optional<Direction> direction = gameState.getMap().directionsTowards(route.getPlayer().getPosition(), route.getDestination()).findFirst();
                    if (direction.isPresent() && canMove(gameState, nextPositions, route.getPlayer(), direction.get())) {
                        assignedPlayerDestinations.put(route.getPlayer(), route.getDestination());
                        return new MoveImpl(route.getPlayer().getId(), direction.get());
                    }
                    return null;
                })
                .filter(move -> move != null)
                .collect(Collectors.toList());
    }
    
    private void updateUnseenLocations(final GameState gameState) {
        // assume players can 'see' a distance of 5 squares
        int visibleDistance = 10;
        final Set<Position> visiblePositions = gameState.getPlayers()
                .stream()
                .filter(player -> isMyPlayer(player))
                .map(player -> player.getPosition())
                .flatMap(playerPosition -> getSurroundingPositions(gameState, playerPosition, visibleDistance))
                .distinct()
                .collect(Collectors.toSet());

        // remove any positions that can be seen
        unseenPositions.removeIf(position -> visiblePositions.contains(position));
    }
    
    private List<Move> doExploreUnseen(final GameState gameState, final Map<Player, Position> assignedPlayerDestinations, final List<Position> nextPositions) {
        List<Move> exploreMoves = new ArrayList<>();

        Set<Player> players = gameState.getPlayers().stream()
                .filter(player -> isMyPlayer(player))
                .filter(player -> !assignedPlayerDestinations.containsKey(player))
                .collect(Collectors.toSet());

        List<Route> unseenRoutes = generateRoutes(gameState, players, unseenPositions);

        Collections.sort(unseenRoutes);
        exploreMoves.addAll(assignRoutes(gameState, assignedPlayerDestinations, nextPositions, unseenRoutes));

        System.out.println(exploreMoves.size() + " players exploring unseen");
        return exploreMoves;
    }
    
    private void updateEnemySpawnPointLocations(final GameState gameState) {
        enemySpawnPointPositions.addAll(gameState.getSpawnPoints().stream()
                .filter(spawnPoint -> !spawnPoint.getOwner().equals(getId()))
                .map(spawnPoint -> spawnPoint.getPosition())
                .collect(Collectors.toList()));

        enemySpawnPointPositions.removeAll(gameState.getRemovedSpawnPoints().stream()
                .filter(spawnPoint -> !spawnPoint.getOwner().equals(getId()))
                .map(spawnPoint -> spawnPoint.getPosition())
                .collect(Collectors.toList()));
    }
    
    private Stream<Position> getSurroundingPositions(final GameState gameState, final Position position, final int distance) {
        Stream<Position> positions = Arrays.stream(Direction.values())
                .flatMap(direction -> IntStream.rangeClosed(1, distance)
                        .mapToObj(currentDistance -> gameState.getMap().getRelativePosition(position, direction, currentDistance)));

        positions = Stream.concat(Stream.of(position), positions);

        return positions;
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
    
    private List<Move> doAttack(final GameState gameState, final Map<Player, Position> assignedPlayerDestinations,
            final List<Position> nextPositions) {
List<Move> attackMoves = new ArrayList<>();

Set<Player> players = gameState.getPlayers().stream()
.filter(player -> isMyPlayer(player))
.filter(player -> !assignedPlayerDestinations.containsKey(player.getId()))
.collect(Collectors.toSet());
System.out.println(players.size() + " players available to attack");

List<Route> attackRoutes = generateRoutes(gameState, players, enemySpawnPointPositions);

Collections.sort(attackRoutes);
attackMoves.addAll(assignRoutes(gameState, assignedPlayerDestinations, nextPositions, attackRoutes));

System.out.println(attackMoves.size() + " players attacking");
return attackMoves;
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
