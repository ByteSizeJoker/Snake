package com.snake.neat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps track of innovations achieved by the NEAT algorithm.
 */
public class InnovationHistory {

    private record ConnectionKey(int inputID, int outputID) {
    }

    private static class Holder {
        private static final InnovationHistory INSTANCE = new InnovationHistory();
    }

    public static InnovationHistory getInstance() {
        return Holder.INSTANCE;
    }

    ConcurrentMap<ConnectionKey, Integer> connectionInnovationNumbers = new ConcurrentHashMap<>();
    ConcurrentMap<Integer, Integer> nodeInnovationNumbers = new ConcurrentHashMap<>();

    private final AtomicInteger nextNodeID = new AtomicInteger(
            NeatConfig.INPUT_NODES + NeatConfig.BIAS_NODES + NeatConfig.OUTPUT_NODES);
    private final AtomicInteger nextConnectionID = new AtomicInteger();

    public int registerConnectionInnovation(int inputID, int outputID) {
        return connectionInnovationNumbers.computeIfAbsent(new ConnectionKey(inputID, outputID),
                k -> nextConnectionID.getAndIncrement());
    }

    public int registerNodeInnovation(int splitConnectionID) {
        return nodeInnovationNumbers.computeIfAbsent(splitConnectionID, k -> nextNodeID.getAndIncrement());
    }

    public void reset() {
        connectionInnovationNumbers.clear();
        nodeInnovationNumbers.clear();
        nextNodeID.set(NeatConfig.INPUT_NODES + NeatConfig.BIAS_NODES + NeatConfig.OUTPUT_NODES);
        nextConnectionID.set(0);
    }
}
