package com.snake.neat;

/**
 * Configuration for the NEAT algorithm
 */
public class NeatConfig {
    public static final int POPULATION_SIZE = 200;
    public static final int MAX_GENERATIONS = 1000;

    public static final int INPUT_NODES = 7;
    public static final int OUTPUT_NODES = 3;
    public static final int BIAS_NODES = 2;
    public static final int MAX_NODES = INPUT_NODES + BIAS_NODES + OUTPUT_NODES + 50;

    public static final double WEIGHT_MUTATION_RATE = 0.8;
    public static final double WEIGHT_REPLACEMENT_RATE = 0.1;
    public static final double WEIGHT_JITTER = 0.1;
    public static final double NODE_MUTATION_RATE = 0.03;
    public static final double CONNECTION_MUTATION_RATE = 0.05;
    public static final int MAX_CONNECTION_MUTATION_ATTEMPTS = 30;
    public static final double CONNECTION_DISABLE_RATE = 0.005;
    public static final double CONNECTION_ENABLE_RATE = 0.01;
    public static final double DISABLE_INHERIT_RATE = 0.75;

    public static final double SCORE_WEIGHT = 300; // 200

    public static final double C1 = 1.0;
    public static final double C2 = 1.0;
    public static final double C3 = 0.4; // 0.4
    public static final double COMPATIBILITY_THRESHOLD = 2.5; // 3.0
    public static final double SURVIVAL_RATE = 0.3; // 0.3
    public static final double CROSSOVER_RATE = 0.75;
    public static final int STALE_LIMIT = 15; // 15

    public static final int MAX_STEPS_WITHOUT_FOOD = 400; // 500

    private NeatConfig() {
    }
}