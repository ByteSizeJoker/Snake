package com.snake.neat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.snake.Direction;

/**
 * Converts a genome into a actual Neural Network.
 */
public class NeuralNetwork {
    private final List<NodeGene> nodeGenes;
    private final Map<Integer, NodeGene> nodeMap;
    private final List<ConnectionGene> connectionGenes;
    private final Map<Integer, Double> nodeValues = new HashMap<>();
    private final Map<Integer, List<ConnectionGene>> incomingConnections = new HashMap<>();
    private final List<ConnectionGene> enabledConnections = new ArrayList<>();
    private List<Integer> order;

    public NeuralNetwork(Genome genome) {
        this.nodeGenes = genome.getNodeGenes();
        this.connectionGenes = genome.getConnectionGenes();

        nodeMap = new HashMap<>();
        for (NodeGene node : nodeGenes) {
            nodeMap.put(node.id(), node);
        }
    }

    public void buildNetwork() {
        enabledConnections.clear();
        incomingConnections.clear();
        nodeValues.clear();
        // For each Node in nodeGene, initialize its value to 0.0 and
        // add it to incomingConnection List
        for (NodeGene node : nodeGenes) {
            nodeValues.put(node.id(), 0.0);
            incomingConnections.put(node.id(), new ArrayList<>());
        }

        // For each connection in connectionGene, if it is enabled, add
        // it to enabledConnection List
        for (ConnectionGene c : connectionGenes) {
            if (c.isEnabled()) {
                enabledConnections.add(c);
                incomingConnections.get(c.outputID()).add(c);
            }
        }

        // Sort nodes in topological order based on their in-degree
        order = topologicalSort();
    }

    public double[] evaluate(double[] inputs) {
        for (NodeGene node : nodeGenes) {
            if (node.type() == NodeType.INPUT) {
                nodeValues.put(node.id(), inputs[node.id()]);
            } else if (node.type() == NodeType.BIAS) {
                nodeValues.put(node.id(), 1.0);
            }
        }

        if (order == null) {
            throw new IllegalStateException("Network has not been build");
        }

        for (int id : order) {
            NodeType type = nodeMap.get(id).type();
            if (type == NodeType.HIDDEN || type == NodeType.OUTPUT) {
                double sum = 0;
                for (ConnectionGene c : incomingConnections.get(id)) {
                    sum += nodeValues.get(c.inputID()) * c.weight();
                }
                nodeValues.put(id, sigmoid(sum));
            }
        }

        List<Double> outputs = new ArrayList<>(NeatConfig.OUTPUT_NODES);
        for (int out : new TreeSet<>(nodeValues.keySet())) {
            if (nodeMap.get(out).type() == NodeType.OUTPUT) {
                outputs.add(nodeValues.get(out));
            }
        }

        return outputs.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public Direction predict(Direction currentDir, double[] input) {
        if (input == null) {
            throw new NullPointerException("input cannot be null");
        } else if (input.length != NeatConfig.INPUT_NODES) {
            throw new IllegalArgumentException("Invalid input size");
        }

        double[] predictions = evaluate(input);

        if (predictions.length != NeatConfig.OUTPUT_NODES) {
            throw new IllegalStateException(
                    "Encountered unexpected number of predictions. Expected 3, but got " + predictions.length);
        }

        int move = 0;
        for (int i = 0; i < predictions.length; i++) {
            if (predictions[i] > predictions[move]) {
                move = i;
            }
        }

        return switch (move) {
            case 0 -> currentDir.turn(Direction.LEFT);
            case 1 -> currentDir.turn(Direction.RIGHT);
            default -> currentDir;
        };
    }

    private static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    // Kash's Algorithm BFS - Topological Sort algorithm for a 
    // Directed Acyclic Graph (DAG) - https://en.wikipedia.org/wiki/Directed_acyclic_graph
    private List<Integer> topologicalSort() {

        // Number of connections coming into a node
        Map<Integer, Integer> inDegree = new HashMap<>();

        // Initialize map with all nodes having 0 incoming connections
        for (NodeGene n : nodeGenes) {
            inDegree.put(n.id(), 0);
        }

        // For each Connection c in enabledConnections, increment
        // inComing connections for c.outputID - the Node's ID
        for (ConnectionGene c : enabledConnections) {
            Integer count = inDegree.get(c.outputID());
            inDegree.replace(c.outputID(), count == null ? 1 : ++count);
        }

        // Deque - pronounced "deck" - a double-ended queue
        // is a linear java Collection which allows insertion
        // is removal of elements from both ends.
        Deque<Integer> queue = new ArrayDeque<>();
        for (NodeGene node : nodeGenes) {
            if (inDegree.get(node.id()) == 0) {
                queue.add(node.id());
            }
        }

        // Initialize a list for sorted result
        List<Integer> result = new ArrayList<>();

        // while queue is not empty, poll the first element
        // save it as id and add it to result - as queue's first element
        // will always have inDegree of 0 then iterate through all 
        // connections, if the connection's inputID is equal to the id
        // then reduce the inDegree of the connection's outputID by 1
        // as the input node is removed/sorted. if the outputID's inDegree
        // becomes 0 then add it to the queue and repeat
        while (!queue.isEmpty()) {
            int id = queue.poll();
            result.add(id);

            for (ConnectionGene c : enabledConnections) {
                if (c.inputID() == id) {
                    Integer count = inDegree.get(c.outputID());
                    inDegree.replace(c.outputID(), --count);
                    if (count == 0) {
                        queue.add(c.outputID());
                    }
                }
            }
        }
        return result;
    }
}