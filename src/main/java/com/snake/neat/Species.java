package com.snake.neat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Species Class to specify a group of genomes that are similar, so they can be
 * evolved and evaluated together so their is no unfair advantage to genomes that are from other species which didn't had time to evolve.
 */
public class Species {
    private static final Random random = new Random();

    private final List<Genome> members = new ArrayList<>();
    private Genome representative;
    private double bestFitness = Double.NEGATIVE_INFINITY;
    private int staleness = 0;
    private double totalAdjusted = 0;

    public Species(Genome representative) {
        this.representative = representative;
        members.add(representative);
    }

    public boolean isCompatible(Genome genome) {
        return Genome.distance(representative, genome) < NeatConfig.COMPATIBILITY_THRESHOLD;
    }

    public void add(Genome genome) {
        members.add(genome);
    }

    public void reset() {
        if (members.isEmpty()) {
            return;
        }

        representative = randomMember();
        members.clear();
    }

    public void adjustFitness() {
        double sum = 0;
        for (Genome g : members) {
            sum += g.fitness;
        }
        totalAdjusted = sum / members.size();

        // Sorts in decreasing order
        members.sort(Comparator.comparingDouble((Genome g) -> g.fitness).reversed());
        if (members.get(0).fitness > bestFitness) {
            bestFitness = members.get(0).fitness;
            staleness = 0;
        } else {
            staleness++;
        }
    }

    public void cull() {
        int keep = Math.max(1, (int) Math.ceil(members.size() * NeatConfig.SURVIVAL_RATE));
        members.subList(keep, members.size()).clear();
    }

    public Genome breed() {
        Genome child;
        if (members.size() > 1 && random.nextDouble() < NeatConfig.CROSSOVER_RATE) {
            child = Genome.crossOver(randomMember(), randomMember());
        } else {
            child = randomMember().copy();
        }
        child.mutate();
        return child;
    }

    public Genome getBestGenome() {
        return members.get(0);
    }

    public List<Genome> getMembers() {
        return members;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public int getStaleness() {
        return staleness;
    }

    public double getTotalAdjusted() {
        return totalAdjusted;
    }

    private Genome randomMember() {
        return members.get(random.nextInt(members.size()));
    }

}