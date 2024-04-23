package tests;

import ai.PassiveAI;
import ai.RandomBiasedAI;
import ai.abstraction.WorkerDefense;
import ai.core.AI;
import gui.PhysicalGameStatePanel;
import mayariBot.mayari;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import javax.swing.*;

import BaluBot.BaluBot;

import java.util.*;
import java.util.stream.Collectors;

public class Tournament {

    private static final String MAP_PATH = "maps/16x16/basesWorkers16x16.xml"; // Path to the map file
    private static final int MAX_CYCLES = 5000; // Maximum number of cycles before the game ends
    private static final int UPDATE_PERIOD = 10; // Time in milliseconds between game cycles
    private static final int WINDOW_SIZE = 1000; // Size of the game window
    private static final boolean DISPOSE_WINDOW = true; // Close the game window after the game ends
    private static final boolean CHECK_FOR_ADVANTAGE = true; // Check for player 1 advantage
    private static final int MAX_DURATION_PER_MATCHUP = 15000; // Time in milliseconds the tournament can runs
    private static final boolean VISUALIZE = true; // Start the game with GUI
    private static final int SIMULATIONS = 1; // Number of simulations to run

    private static void getTournamentPlayers() {
        // players.add(new PassiveAI(utt));
        // players.add(new RandomAI(utt));
        // players.add(new RandomBiasedAI(utt));
        // players.add(new HeavyRush(utt));
        // players.add(new LightDefense(utt));
        // players.add(new WorkerDefense(utt));
        // players.add(new WorkerRush(utt));
        // players.add(new CoacAI(utt));
        // players.add(new BasicRush(utt));
        // players.add(new mayari(utt));
        // players.add(new ObiBotKenobi(utt));
        // players.add(new DameBot(utt));
        players.add(new BaluBot(utt));
        players.add(new LasyaBot(utt));
        players.add(new HSBot(utt));
    }
    }

    public static void main(String[] args) {
        getTournamentPlayers();
        if (players.size() < 2) {
            System.out.println("There must be at least two players to run a tournament.");
            return;
        }
        runTournament();
    }

    private static void runTournament() {
        Map<String, Map<String, List<Result>>> tournamentResults = new HashMap<>();
        Map<String, Map<String, List<Result>>> tournamentResultsSwitched = new HashMap<>();
        List<Integer> gameCycles = new ArrayList<>();
        Map<String, Long> matchDurations = new HashMap<>();

        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                runMatchesBetweenPlayers(i, j, tournamentResults, tournamentResultsSwitched, gameCycles,
                        matchDurations);
            }
        }
        GameStats.summarizeTournament(tournamentResults, matchDurations, tournamentResultsSwitched);
    }

    private static int runMatch(GameState gs, AI ai1, AI ai2, List<Integer> gameCycles, JFrame window) {
        int winner = -1;
        try {
            long nextUpdateTime = System.currentTimeMillis() + (VISUALIZE ? UPDATE_PERIOD : 0);
            while (!gs.gameover() && gs.getTime() < MAX_CYCLES) {
                if (System.currentTimeMillis() >= nextUpdateTime) {
                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);
                    gs.cycle();
                    if (window != null)
                        window.repaint();
                    nextUpdateTime += (VISUALIZE ? UPDATE_PERIOD : 0);
                } else {
                    Thread.sleep(1);
                }
            }
            winner = gs.winner();
            gameCycles.add(gs.getTime());

            ai1.reset();
            ai2.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return winner;
    }

    private static void runMatchesBetweenPlayers(int playerIndex1, int playerIndex2,
            Map<String, Map<String, List<Result>>> tournamentResults,
            Map<String, Map<String, List<Result>>> tournamentResultsSwitched,
            List<Integer> gameCycles,
            Map<String, Long> matchDurations) {
        String ai1Name = players.get(playerIndex1).getClass().getSimpleName();
        String ai2Name = players.get(playerIndex2).getClass().getSimpleName();
        List<Integer> winners = new ArrayList<>();
        List<Integer> winnersSwitched = new ArrayList<>();

        long start = System.currentTimeMillis();

        for (int simulation = 0; simulation < SIMULATIONS; simulation++) {
            if (System.currentTimeMillis() - start > MAX_DURATION_PER_MATCHUP) {
                break;
            }
            simulateMatch(ai1Name, ai2Name, playerIndex1, playerIndex2, winners, gameCycles, tournamentResults, false);
            if (CHECK_FOR_ADVANTAGE) {
                simulateMatch(ai2Name, ai1Name, playerIndex2, playerIndex1, winnersSwitched, gameCycles,
                        tournamentResultsSwitched, true);
            }
        }

        long duration = System.currentTimeMillis() - start;
        matchDurations.put(ai1Name + " vs " + ai2Name, duration);
        GameStats gameStats = new GameStats(winners, winnersSwitched, gameCycles, duration, ai1Name, ai2Name);
        gameStats.summarizeMatchup();
    }

    private static void simulateMatch(String ai1Name, String ai2Name, int playerIndex1, int playerIndex2,
            List<Integer> winners, List<Integer> gameCycles,
            Map<String, Map<String, List<Result>>> resultsStorage, boolean switched) {
        GameState gs = setupGame();
        JFrame window = VISUALIZE ? setupVisualizer(gs) : null;
        AI player1 = players.get(playerIndex1);
        AI player2 = players.get(playerIndex2);

        int winner = runMatch(gs, player1, player2, gameCycles, window);
        winners.add(winner);
        recordResult(resultsStorage, ai1Name, ai2Name, winner);

        if (window != null && DISPOSE_WINDOW)
            window.dispose();
    }

    private static void recordResult(Map<String, Map<String, List<Result>>> results, String ai1, String ai2,
            int winner) {
        Result result = winner == 0 ? Result.WIN : winner == 1 ? Result.LOSE : Result.DRAW;
        results.computeIfAbsent(ai1, k -> new HashMap<>()).computeIfAbsent(ai2, k -> new ArrayList<>()).add(result);
        Result oppositeResult = (result == Result.WIN) ? Result.LOSE : (result == Result.LOSE ? Result.WIN : result);
        results.computeIfAbsent(ai2, k -> new HashMap<>()).computeIfAbsent(ai1, k -> new ArrayList<>())
                .add(oppositeResult);
    }

    private static GameState setupGame() {
        try {
            PhysicalGameState pgs = PhysicalGameState.load(MAP_PATH, utt);
            return new GameState(pgs, utt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFrame setupVisualizer(GameState gs) {
        JFrame window = PhysicalGameStatePanel.newVisualizer(gs, WINDOW_SIZE, WINDOW_SIZE, false);
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        return window;
    }

    enum Result {
        WIN, LOSE, DRAW
    }

    public static class GameStats {
        private List<Integer> winners;
        private List<Integer> winnersSwitched;
        private List<Integer> cycles;
        private String player1;
        private String player2;
        private long duration;

        public GameStats(List<Integer> winners, List<Integer> winnersSwitched, List<Integer> cycles, long duration,
                String player1, String player2) {
            this.winners = winners;
            this.cycles = cycles;
            this.duration = duration;
            this.player1 = player1;
            this.player2 = player2;
            this.winnersSwitched = winnersSwitched;
        }

        public void summarizeMatchup() {
            System.out.println(player1 + " vs " + player2);
            System.out.println("================================================");
            summarizeResults(winners, winnersSwitched, player1, player2);
            System.out.printf("\t%-25s %dms%n", "Matchup Duration:", duration);
            double averageCycles = cycles.stream().mapToInt(Integer::intValue).average().orElse(0);
            int minCycles = cycles.stream().mapToInt(Integer::intValue).min().orElse(0);
            int maxCycles = cycles.stream().mapToInt(Integer::intValue).max().orElse(0);
            System.out.printf("\t%-25s %.2f%n", "Average game cycles:", averageCycles);
            System.out.printf("\t%-25s %d%n", "Minimum game cycles:", minCycles);
            System.out.printf("\t%-25s %d%n", "Maximum game cycles:", maxCycles);
            System.out.println("================================================\n");
        }

        private void summarizeResults(List<Integer> winnersList, List<Integer> winnersListSwitched, String ai1,
                String ai2) {
            if (winnersListSwitched.size() > 0) {
                summarizeForRole(winnersList, ai1, ai2, ai1 + " as P1");
                summarizeForRole(winnersListSwitched, ai2, ai1, ai2 + " as P1");
                List<Integer> combinedResults = new ArrayList<>(winnersList);
                combinedResults.addAll(winnersListSwitched.stream().map(winner -> {
                    return (winner == 1) ? 0 : (winner == 0) ? 1 : 2;
                }).collect(Collectors.toList()));

                summarizeForRole(combinedResults, ai1, ai2, "Combined Results (" + ai1 + " vs " + ai2 + ")");
            } else {
                summarizeForRole(winnersList, ai1, ai2, ai1 + " as P1");
            }
        }

        private void summarizeForRole(List<Integer> winnersList, String ai1, String ai2, String description) {
            int winsAI1 = 0, winsAI2 = 0, draws = 0;
            for (int winner : winnersList) {
                if (winner == 0)
                    winsAI1++;
                else if (winner == 1)
                    winsAI2++;
                else
                    draws++;
            }

            double totalGames = winnersList.size();
            double winRateAI1 = (winsAI1 / totalGames) * 100;
            double winRateAI2 = (winsAI2 / totalGames) * 100;
            double drawRate = (draws / totalGames) * 100;

            System.out.println(description);
            System.out.println("------------------------------------------------");
            System.out.printf("\t%-25s %d%n", "Total simulations:", winnersList.size());
            System.out.printf("\t%-25s %d (%.2f%%)%n", ai1 + " wins:", winsAI1, winRateAI1);
            System.out.printf("\t%-25s %d (%.2f%%)%n", ai2 + " wins:", winsAI2, winRateAI2);
            System.out.printf("\t%-25s %d (%.2f%%)%n", "Draws:", draws, drawRate);
            System.out.println("------------------------------------------------");
        }

        public static void summarizeTournament(Map<String, Map<String, List<Result>>> tournamentResults,
                Map<String, Long> matchDurations,
                Map<String, Map<String, List<Result>>> tournamentResultsSwitched) {
            System.out.printf("\nTournament Results (%s)\n", MAP_PATH);
            System.out.println("================================================");

            Map<String, Map<String, List<Result>>> combinedResults = combineAndAggregateResults(tournamentResults,
                    tournamentResultsSwitched);

            Map<String, Double> rankings = calculateRankings(combinedResults);
            displayRankings(rankings, combinedResults);
            summarizeTournamentResults(combinedResults, matchDurations);
        }

        private static Map<String, Map<String, List<Result>>> combineAndAggregateResults(
                Map<String, Map<String, List<Result>>> original,
                Map<String, Map<String, List<Result>>> switched) {
            Map<String, Map<String, List<Result>>> combined = new HashMap<>();

            original.forEach((player, opponents) -> combined.put(player, new HashMap<>(opponents)));
            switched.forEach((player, opponents) -> {
                opponents.forEach((opponent, results) -> combined.computeIfAbsent(player, k -> new HashMap<>())
                        .merge(opponent, results, (oldResults, newResults) -> {
                            List<Result> mergedList = new ArrayList<>(oldResults);
                            mergedList.addAll(newResults);
                            return mergedList;
                        }));
            });

            return combined;
        }

        private static Map<String, Double> calculateRankings(Map<String, Map<String, List<Result>>> combinedResults) {
            Map<String, Double> winPercentages = new HashMap<>();

            combinedResults.forEach((bot, opponentsResults) -> {
                int wins = opponentsResults.values().stream()
                        .flatMap(List::stream)
                        .mapToInt(result -> result == Result.WIN ? 1 : 0)
                        .sum();
                int totalMatches = opponentsResults.values().stream()
                        .flatMap(List::stream)
                        .mapToInt(result -> 1) // Count every result as a match
                        .sum();

                double winPercentage = totalMatches > 0 ? (double) wins / totalMatches * 100 : 0;
                winPercentages.put(bot, winPercentage);
            });

            return winPercentages.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new));
        }

        private static void displayRankings(Map<String, Double> rankings,
                Map<String, Map<String, List<Result>>> tournamentResults) {
            int currentRank = 1;
            int botsProcessed = 0;
            double previousWinPercentage = -1.0;

            for (var entry : rankings.entrySet()) {
                if (entry.getValue() != previousWinPercentage) {
                    currentRank = botsProcessed + 1;
                    previousWinPercentage = entry.getValue();
                }

                String botName = entry.getKey();
                double winPercentage = entry.getValue();
                int wins = tournamentResults.get(botName).values().stream()
                        .flatMap(List::stream)
                        .mapToInt(result -> result == Result.WIN ? 1 : 0)
                        .sum();
                int losses = tournamentResults.get(botName).values().stream()
                        .flatMap(List::stream)
                        .mapToInt(result -> result == Result.LOSE ? 1 : 0)
                        .sum();
                int draws = tournamentResults.get(botName).values().stream()
                        .flatMap(List::stream)
                        .mapToInt(result -> result == Result.DRAW ? 1 : 0)
                        .sum();

                System.out.printf("\tRank %d: %-20s %d Wins | %d Losses | %d Draws | %.2f%% Win Rate\n", currentRank,
                        botName, wins, losses, draws, winPercentage);

                botsProcessed++;
            }
            System.out.println("------------------------------------------------");
        }

        private static void summarizeTournamentResults(Map<String, Map<String, List<Result>>> tournamentResults,
                Map<String, Long> matchDurations) {
            tournamentResults.forEach((ai1, opponents) -> {
                final boolean[] isFirstOpponent = { true };

                opponents.forEach((ai2, results) -> {
                    long wins = results.stream().filter(r -> r == Result.WIN).count();
                    long losses = results.stream().filter(r -> r == Result.LOSE).count();
                    long draws = results.stream().filter(r -> r == Result.DRAW).count();

                    if (isFirstOpponent[0]) {
                        System.out.printf("\t%-20s vs %-20s %d Wins | %d Losses | %d Draws\n", ai1, ai2, wins, losses,
                                draws);
                        isFirstOpponent[0] = false;
                    } else {
                        System.out.printf("\t\t\t\t%-20s %d Wins | %d Losses | %d Draws\n", ai2,
                                wins, losses, draws);
                    }
                });
                System.out.println("------------------------------------------------");
            });

            long totalDuration = matchDurations.values().stream().mapToLong(Long::longValue).sum();
            System.out.printf("\tTotal Tournament Duration: %dms\n", totalDuration);
            System.out.println("================================================");
        }
    }

    private static List<AI> players = new ArrayList<>();
    private static UnitTypeTable utt = new UnitTypeTable();
}
