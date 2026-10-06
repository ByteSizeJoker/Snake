package com.snake.neat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Defines the network's topology
 * Holds a list of nodes and connections between nodes
 */
public class Genome {
    private static final Random random = new Random();

    public static Genome crossOver(Genome p1, Genome p2) {
        Genome fitter = Genome.getFitter(p1, p2);
        Genome other = fitter == p1 ? p2 : p1;

        Genome child = new Genome();
        child.nodeGenes.addAll(fitter.nodeGenes);

        Map<Integer, ConnectionGene> otherGene = new HashMap<>();
        for (ConnectionGene o : other.connectionGenes) {
            otherGene.put(o.globalID(), o);
        }

        for (ConnectionGene c : fitter.connectionGenes) {
            ConnectionGene pick = c;
            ConnectionGene o = otherGene.get(c.globalID());
            if (o != null) {
                if (random.nextDouble() < 0.5) {
                    pick = o.clone();
                }
                if (!c.isEnabled() || !o.isEnabled()) {
                    pick = random.nextDouble() < NeatConfig.DISABLE_INHERIT_RATE ? pick.disable() : pick.enable();
                }
            }
            child.connectionGenes.add(pick);
        }

        return child;
    }

    public static double distance(Genome a, Genome b) {
        Map<Integer, ConnectionGene> mapA = new HashMap<>();
        Map<Integer, ConnectionGene> mapB = new HashMap<>();
        for (ConnectionGene c : a.connectionGenes) {
            mapA.put(c.globalID(), c);
        }
        for (ConnectionGene c : b.connectionGenes) {
            mapB.put(c.globalID(), c);
        }

        int maxA = mapA.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
        int maxB = mapB.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);

        // Set of global IDs of all Connections form a and b
        Set<Integer> ids = new HashSet<>(mapA.keySet());
        ids.addAll(mapB.keySet());

        int excess = 0;
        int disjoint = 0;
        int matching = 0;
        double weightDiff = 0;

        for (int id : ids) {
            ConnectionGene ga = mapA.get(id);
            ConnectionGene gb = mapB.get(id);

            if (ga != null && gb != null) {
                matching++;
                weightDiff += Math.abs(ga.weight() - gb.weight());
            } else {
                int limit = ga != null ? maxB : maxA;
                if (id > limit) {
                    excess++;
                } else {
                    disjoint++;
                }
            }
        }

        double avgWeightDiff = matching == 0 ? 0 : weightDiff / matching;
        return NeatConfig.C1 * excess + NeatConfig.C2 * disjoint + NeatConfig.C3 * avgWeightDiff;
    }

    private static Genome getFitter(Genome p1, Genome p2) {
        return p1.fitness > p2.fitness ? p1 : p2;
    }

    private List<NodeGene> nodeGenes;
    private List<ConnectionGene> connectionGenes;

    public double fitness = 0;

    public double adjustedFitness = 0;

    private InnovationHistory history = InnovationHistory.getInstance();

    public Genome() {
        nodeGenes = new ArrayList<>();
        connectionGenes = new ArrayList<>();
    }

    public Genome build() {
        int totalNodes = NeatConfig.INPUT_NODES + NeatConfig.BIAS_NODES + NeatConfig.OUTPUT_NODES;
        int totalConnections = (NeatConfig.INPUT_NODES + NeatConfig.BIAS_NODES) * NeatConfig.OUTPUT_NODES;
        int totalInputAndBiasNodes = NeatConfig.INPUT_NODES + NeatConfig.BIAS_NODES;

        nodeGenes = new ArrayList<>(totalNodes);
        connectionGenes = new ArrayList<>(totalConnections);

        for (int nodeID = 0; nodeID < totalNodes; nodeID++) {
            if (nodeID < NeatConfig.INPUT_NODES) {
                nodeGenes.add(new NodeGene(nodeID, NodeType.INPUT));
            } else if (nodeID < totalInputAndBiasNodes) {
                nodeGenes.add(new NodeGene(nodeID, NodeType.BIAS));
            } else {
                nodeGenes.add(new NodeGene(nodeID, NodeType.OUTPUT));
            }
        }

        for (int i = 0; i < NeatConfig.OUTPUT_NODES; i++) {
            for (int j = 0; j < totalInputAndBiasNodes; j++) {
                addConnection(nodeGenes.get(j), nodeGenes.get(totalInputAndBiasNodes + i));
            }
        }

        return this;
    }

    public Genome copy() {
        Genome copy = new Genome();
        copy.nodeGenes = new ArrayList<>(nodeGenes.size());
        copy.connectionGenes = new ArrayList<>(connectionGenes.size());
        copy.nodeGenes.addAll(nodeGenes);
        copy.connectionGenes.addAll(connectionGenes);
        copy.fitness = fitness;
        copy.adjustedFitness = adjustedFitness;
        return copy;
    }

    public List<NodeGene> getNodeGenes() {
        return nodeGenes;
    }

    public List<ConnectionGene> getConnectionGenes() {
        return connectionGenes;
    }

    public void addConnection(NodeGene from, NodeGene to) {
        int inID = from.id();
        int outID = to.id();
        double weight = random.nextDouble() * 2 - 1;
        int globalID = history.registerConnectionInnovation(inID, outID);

        ConnectionGene connectionGene = new ConnectionGene(inID, outID, weight, true, globalID);
        connectionGenes.add(connectionGene);
    }

    public void addNode() {
        if (nodeGenes.size() >= NeatConfig.MAX_NODES) {
            return;
        }

        List<Integer> enableConnectionIndices = new ArrayList<>();
        for (int i = 0; i < connectionGenes.size(); i++) {
            if (connectionGenes.get(i).isEnabled()) {
                enableConnectionIndices.add(i);
            }
        }

        if (enableConnectionIndices.isEmpty()) {
            return;
        }

        int randomIndex = random.nextInt(enableConnectionIndices.size());
        addNodeAt(enableConnectionIndices.get(randomIndex));
    }

    public void mutateWeights() {
        for (int i = 0; i < connectionGenes.size(); i++) {
            if (random.nextDouble() < NeatConfig.WEIGHT_MUTATION_RATE) {
                ConnectionGene gene = connectionGenes.get(i);
                double newWeight;
                if (random.nextDouble() < NeatConfig.WEIGHT_REPLACEMENT_RATE) {
                    newWeight = random.nextDouble() * 2 - 1;
                } else {
                    newWeight = Math.clamp(gene.weight() + (random.nextGaussian() * NeatConfig.WEIGHT_JITTER), -1.0,
                            1.0);
                }
                connectionGenes.set(i, gene.withWeight(newWeight));
            }
        }
    }

    public void mutate() {
        mutateWeights();
        if (random.nextDouble() < NeatConfig.NODE_MUTATION_RATE) {
            addNode();
        }
        if (random.nextDouble() < NeatConfig.CONNECTION_MUTATION_RATE) {
            addRandomConnection();
        }
        toggleConnection();
    }

    private void addNodeAt(int index) {
        if (index < 0 || index >= connectionGenes.size()) {
            throw new IllegalArgumentException("Invalid index: " + index);
        }

        ConnectionGene old = connectionGenes.get(index);
        if (!old.isEnabled()) {
            throw new IllegalArgumentException("Cannot add node to disabled connection");
        }

        int newNodeId = history.registerNodeInnovation(old.globalID());
        for (NodeGene n : nodeGenes) {
            if (n.id() == newNodeId) {
                return;
            }
        }

        ConnectionGene disabled = old.disable();
        connectionGenes.set(index, disabled);

        int inputId = old.inputID();
        int outputId = old.outputID();

        int conn1GlobalId = history.registerConnectionInnovation(inputId, newNodeId);
        ConnectionGene newConnection1 = new ConnectionGene(inputId, newNodeId, 1, true,
                conn1GlobalId);

        int conn2GlobalId = history.registerConnectionInnovation(newNodeId, outputId);
        ConnectionGene newConnection2 = new ConnectionGene(newNodeId, outputId, old.weight(), true,
                conn2GlobalId);

        nodeGenes.add(new NodeGene(newNodeId, NodeType.HIDDEN));
        connectionGenes.add(newConnection1);
        connectionGenes.add(newConnection2);
    }

    private void addRandomConnection() {
        for (int attempt = 0; attempt < NeatConfig.MAX_CONNECTION_MUTATION_ATTEMPTS; attempt++) {
            NodeGene from = nodeGenes.get(random.nextInt(nodeGenes.size()));
            NodeGene to = nodeGenes.get(random.nextInt(nodeGenes.size()));

            if (from.type() == NodeType.OUTPUT) {
                continue;
            } else if (to.type() == NodeType.INPUT || to.type() == NodeType.BIAS) {
                continue;
            } else if (from.id() == to.id() || connectionExist(from.id(), to.id())) {
                continue;
            } else if (reaches(from.id(), to.id())) {
                continue;
            }

            addConnection(from, to);
            return;

        }
    }

    private boolean connectionExist(int formId, int toId) {
        for (ConnectionGene c : connectionGenes) {
            if (c.inputID() == formId && c.outputID() == toId) {
                return true;
            }
        }
        return false;
    }

    private boolean reaches(int startId, int targetID) {
        Deque<Integer> stack = new ArrayDeque<>();
        Set<Integer> visited = new HashSet<>();
        stack.push(startId);

        while (!stack.isEmpty()) {
            int currentId = stack.pop();
            if (currentId == targetID) {
                return true;
            }
            if (!visited.add(currentId)) {
                continue;
            }
            for (ConnectionGene c : connectionGenes) {
                if (c.inputID() == currentId) {
                    stack.push(c.outputID());
                }
            }
        }

        return false;
    }

    private void toggleConnection() {
        for (int i = 0; i < connectionGenes.size(); i++) {
            ConnectionGene gene = connectionGenes.get(i);
            if (gene.isEnabled() && random.nextDouble() < NeatConfig.CONNECTION_DISABLE_RATE) {
                connectionGenes.set(i, gene.disable());
            } else if (!gene.isEnabled() && random.nextDouble() < NeatConfig.CONNECTION_ENABLE_RATE) {
                connectionGenes.set(i, gene.enable());
            }
        }
    }
}