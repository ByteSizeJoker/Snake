package com.snake.neat;

/**
 * Stores information about a node
 * @param id
 * @param type
 */
public record NodeGene(int id, NodeType type) {
    public NodeGene clone() {
        return new NodeGene(id, type);
    }
}