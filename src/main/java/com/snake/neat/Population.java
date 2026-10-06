package com.snake.neat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import com.snake.Direction;
import com.snake.Game;
import com.snake.GameConfig;

/**
 * Main population class, which handles POPULATION_SIZE number of genomes.
 */
public class Population {

    // 20 * 20 board will need <= 400 steps when snake is
    // max size - 1. But 250 is fine for now
    private List<Genome> genomes;
    private Genome bestGenome = new Genome();
    private final int size = NeatConfig.POPULATION_SIZE;
    private List<Species> speciesList;

    public Population() {
        genomes = new ArrayList<>(size);
        speciesList = new ArrayList<>();
    }

    public void initPopulation() {
        System.out.println("Initializing population...");
        for (int i = 0; i < size; i++) {
            genomes.add(new Genome().build());
        }
    }

    // public static void main(String[] args) {
    //     Population p = new Population();
    //     Genome best = p.run();
    //     NeuralNetwork nn = new NeuralNetwork(best);
    //     nn.buildNetwork();

    //     System.out.println("Testing best genome...");
    //     for (int i = 0; i < 20; i++) {
    //         Game g = new Game(GameConfig.BOARD_COLS, GameConfig.BOARD_ROWS);
    //         int steps = 0;
    //         while (!g.isGameOver()) {
    //             double[] state = g.getState();
    //             Direction dir = nn.predict(g.getSnake().getCurrentDir(), state);
    //             g.setDirection(dir);
    //             g.update();
    //             steps++;
    //         }
    //         System.out.printf("Run %d | Score: %d | Total Steps: %d | Avg Steps: %.2f\n", i + 1,
    //                 g.getScore(), steps, (double) steps / g.getScore());
    //     }
    // }

    public Genome run() {
        long time = System.nanoTime();
        initPopulation();
        bestGenome = genomes.get(0).copy();
        for (int gen = 1; gen <= NeatConfig.MAX_GENERATIONS; gen++) {
            evaluate();
            double sum = 0, min = Double.MAX_VALUE, max = 0;
            for (Genome genome : genomes) {
                sum += genome.fitness;
                min = Math.min(min, genome.fitness);
                max = Math.max(max, genome.fitness);

                if (genome.fitness > bestGenome.fitness) {
                    bestGenome = genome.copy();
                }
            }

            speciate();
            if (gen % 50 == 0 || gen == 1) {
                System.out.printf("Gen %d | max %.0f | avg %.0f | min %.0f | species %d%n",
                        gen, max, sum / genomes.size(), min, speciesList.size());
            }
            reproduce();
        }

        System.out.println("Best genome: " + bestGenome.fitness);
        System.out.println("Time taken: " + (System.nanoTime() - time) / 1e9 + "s");
        return bestGenome;
    }

    private void evaluate() {
        IntStream.range(0, genomes.size()).parallel().forEach(i -> {
            Genome genome = genomes.get(i);
            // for (Genome genome : genomes) {
            NeuralNetwork nn = new NeuralNetwork(genome);
            nn.buildNetwork();
            Game g = new Game(GameConfig.BOARD_COLS, GameConfig.BOARD_ROWS);
            int stepSinceFood = 0;
            int lastScore = 0;
            while (!g.isGameOver() && stepSinceFood < NeatConfig.MAX_STEPS_WITHOUT_FOOD) {
                double[] state = g.getState();
                Direction snakeDir = g.getSnake().getCurrentDir();
                Direction dir = nn.predict(snakeDir, state);
                g.setDirection(dir);
                g.update();

                if (g.getScore() > lastScore) {
                    lastScore = g.getScore();
                    stepSinceFood = 0;
                } else {
                    stepSinceFood++;
                }
                // redundant cond but let it be there for now
                if (stepSinceFood > 10000) {
                    break;
                }
            }
            // penalty = 1;
            genome.fitness = (g.getScore() * NeatConfig.SCORE_WEIGHT + Math.min(g.getSteps(), 1000))
                    / stepSinceFood;
            // genome.fitness = (g.getScore() * NeatConfig.SCORE_WEIGHT
            //         + Math.min(g.getSteps(), 1000)) / penalty;
            // genome.fitness = g.getScore() * NeatConfig.SCORE_WEIGHT //BestYet
            //         + Math.min(g.getSteps(), 1000) * 0.1 * (1 / penalty);

            if (g.isGameOver()) {
                genome.fitness = genome.fitness * 0.90;
            }
        });
    }

    private void speciate() {
        for (Species s : speciesList) {
            s.reset();
        }

        for (Genome g : genomes) {
            boolean selected = false;
            for (Species s : speciesList) {
                if (s.isCompatible(g)) {
                    s.add(g);
                    selected = true;
                    break;
                }
            }
            if (!selected) {
                speciesList.add(new Species(g));
                // System.out.println("New species created: " + speciesList.size() + " species total.");
            }
        }

        speciesList.removeIf(s -> s.getMembers().isEmpty());
    }

    private void reproduce() {
        for (Species s : speciesList) {
            s.adjustFitness();
            s.cull();
        }

        speciesList.sort(Comparator.comparingDouble(Species::getBestFitness).reversed());
        Species top = speciesList.get(0);
        speciesList.removeIf(s -> s != top && s.getStaleness() >= NeatConfig.STALE_LIMIT);

        double total = 0;
        for (Species s : speciesList) {
            total += s.getTotalAdjusted();
        }

        if (total == 0) {
            return; // This should never happen, but just in case.
        }

        List<Genome> next = new ArrayList<>(size);
        for (Species s : speciesList) {
            int quota = (int) Math.round(s.getTotalAdjusted() / total * size);
            if (quota >= 1) {
                next.add(s.getBestGenome().copy());
                quota--;
            }
            for (int i = 0; i < quota; i++) {
                next.add(s.breed());
            }
        }

        while (next.size() < size) {
            Genome child = top.getBestGenome().copy();
            child.mutate();
            next.add(child);
        }

        genomes = new ArrayList<>(next.subList(0, size));
    }

}