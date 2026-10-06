package com.snake.neat;

/**
 * Stores information about a connection between two nodes
 * @param inputID
 * @param outputID
 * @param weight
 * @param isEnabled
 * @param globalID
 */
public record ConnectionGene(int inputID, int outputID, double weight, boolean isEnabled, int globalID) {

    public ConnectionGene enable() {
        return new ConnectionGene(inputID, outputID, weight, true, globalID);
    }

    public ConnectionGene disable() {
        return new ConnectionGene(inputID, outputID, weight, false, globalID);
    }

    public ConnectionGene withWeight(double weight) {
        return new ConnectionGene(inputID, outputID, weight, isEnabled, globalID);
    }

    public ConnectionGene clone() {
        return new ConnectionGene(inputID, outputID, weight, isEnabled, globalID);
    }
}