package com.snake;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Game class to handle the game logic.
 * @see Snake
 * @see Direction
 */
public class Game {
    enum CollisionType {
        FAZE, COLLIDE
    }

    private static final Random random = new Random();
    private static final CollisionType COLLISION_TYPE = CollisionType.COLLIDE;
    private static final int SNAKE_LENGTH = 5;
    private final int width;
    private final int height;
    private boolean isPaused;
    private int steps = 0;

    private Snake snake;

    private Direction direction = Direction.LEFT;

    // List<int[]> targets;
    int[] target = null;

    private int score = 0;

    public Game(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and Height must be positive");
        }
        this.width = width;
        this.height = height;
        initSnake();
        initTarget();
    }

    public int getSteps() {
        return steps;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    // spawn snake
    public Snake initSnake(int length, int x, int y) {

        if (length < 2 || length > width || x < 0 || y < 0 || x > width - length || y >= height) {
            throw new IllegalArgumentException(
                    "Snake must fit inside the game board and contain at least two segments");
        }

        snake = new Snake(length).spawn(x, y);
        // System.out.println("Snake Spawned at " + x + " " + y);
        return snake;
    }

    public Snake initSnake() {
        return initSnake(SNAKE_LENGTH, width / 2, height / 2);
    }

    public void update() {
        if (isPaused) {
            return;
        }

        snake.move(direction);
        steps++;

        checkCollision();

        if (!snake.isAlive()) {
            return;
        }

        if (target == null) {
            initTarget();
        }

        // double[] state = getState();

        // System.out.println("--------------------------------");
        // System.out.printf("Danger: %.0f, %.0f, %.0f, %.0f\n", state[0], state[1], state[2], state[3]);
        // System.out.printf("Food: %.0f, %.0f, %.0f, %.0f\n", state[4], state[5], state[6], state[7]);
        // System.out.printf("Direction:  %.0f, %.0f, %.0f, %.0f\n", state[8], state[9], state[10], state[11]);
        // System.out.printf("Raycast: %.4f, %.4f, %.4f\n", state[12], state[13], state[14]);
    }

    public void setDirection(Direction dir) {
        this.direction = dir;
    }

    public Snake getSnake() {
        return snake;
    }

    public int[] getTarget() {
        return target;
    }

    public int getScore() {
        return score;
    }

    public void reset() {
        score = 0;
        isPaused = false;
        direction = Direction.LEFT;
        initSnake();
        initTarget();
    }

    public boolean isGameOver() {
        return snake != null && !snake.isAlive();
    }

    // Network Input Format:
    //
    // 0 -> is Danger UP
    // 1 -> is Danger Down
    // 2 -> is Danger Left
    // 3 -> is Danger Right
    //
    // 4 -> is Target Up
    // 5 -> is Target Down
    // 6 -> is Target Left
    // 7 -> is Target Right
    //
    // 8 -> is Current Direction Up
    // 9 -> is Current Direction Down
    // 10 -> is Current Direction Left
    // 11 -> is Current Direction Right
    //
    // 12 -> Raycast Danger distance Up
    // 13 -> Raycast Danger distance Down
    // 14 -> Raycast Danger distance Left
    // 15 -> Raycast Danger distance Right
    //
    // Network output Format:
    //
    // Output Node 1: Up
    // Output Node 2: Down
    // Output Node 3: Left
    // Output Node 4: Right
    public double[] getState() {
        double[] state = new double[16];

        int[][] body = snake.getBody();
        int headX = body[0][0];
        int headY = body[0][1];
        Direction dir = snake.getCurrentDir();

        if (COLLISION_TYPE == CollisionType.COLLIDE) {
            if (headY == 0 && dir != Direction.DOWN) { // Danger Up
                state[0] = 1;
            }
            if (headY == height - 1 && dir != Direction.UP) { // Danger Down
                state[1] = 1;
            }
            if (headX == 0 && dir != Direction.RIGHT) { // Danger Left
                state[2] = 1;
            }
            if (headX == width - 1 && dir != Direction.LEFT) { // Danger Right
                state[3] = 1;
            }
        }

        boolean[][] snakeMatrix = new boolean[width][height];
        for (int i = 1; i < snake.getLength(); i++) {
            snakeMatrix[body[i][0]][body[i][1]] = true;

            int dx = body[i][0] - headX;
            int dy = body[i][1] - headY;

            if (dx == 0 && dy == -1 && dir != Direction.DOWN) { // Danger Up
                state[0] = 1;
            }
            if (dx == 0 && dy == 1 && dir != Direction.UP) { // Danger Down
                state[1] = 1;
            }
            if (dx == -1 && dy == 0 && dir != Direction.RIGHT) { // Danger Left
                state[2] = 1;
            }
            if (dx == 1 && dy == 0 && dir != Direction.LEFT) { // Danger Right
                state[3] = 1;
            }
        }

        if (target != null) {
            int dx = target[0] - headX, dy = target[1] - headY;

            if (dy <= -1) { // Target Up
                state[4] = 1;
            }
            if (dy >= 1) { // Target Down
                state[5] = 1;
            }
            if (dx <= -1) { // Target Left
                state[6] = 1;
            }
            if (dx >= 1) { // Target Right
                state[7] = 1;
            }
        }

        switch (dir) {
            case UP -> {
                state[8] = 1;
            }
            case DOWN -> {
                state[9] = 1;
            }
            case LEFT -> {
                state[10] = 1;
            }
            case RIGHT -> {
                state[11] = 1;
            }
        }

        double[] rayCastResult = performRayCast(snakeMatrix);
        state[12] = rayCastResult[0]; // Up
        state[13] = rayCastResult[1]; // Down
        state[14] = rayCastResult[2]; // Left
        state[15] = rayCastResult[3]; // Right

        return state;
    }

    // Spawns target using either random attempt or by filtering empty cells
    private int[] initTarget() {
        Set<Integer> occupied = new HashSet<>();

        for (int[] part : snake.getBody()) {
            occupied.add(part[0] * height + part[1]);
        }

        List<int[]> emptyCells = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!occupied.contains(x * height + y)) {
                    emptyCells.add(new int[] { x, y });
                }
            }
        }

        if (emptyCells.isEmpty()) {
            target = null;
            return target;
        }

        target = emptyCells.get(random.nextInt(emptyCells.size()));
        // System.out.println("Target set at " + target[0] + " " + target[1]);
        return target;
    }

    private void checkCollision() {
        int[][] body = snake.getBody();
        int headX = body[0][0];
        int headY = body[0][1];

        for (int i = 0; i < body.length; i++) {
            int x = body[i][0];
            int y = body[i][1];

            // Check Collision with body
            if (x == headX && y == headY && i != 0) {
                snake.kill();
                return;
            }

            // Check if snake collides with walls
            if (COLLISION_TYPE == CollisionType.FAZE) {
                if (x < 0) {
                    body[i][0] = width - 1;
                } else if (x > width - 1) {
                    body[i][0] = 0;
                } else if (y < 0) {
                    body[i][1] = height - 1;
                } else if (y > height - 1) {
                    body[i][1] = 0;
                }
            } else if (COLLISION_TYPE == CollisionType.COLLIDE) {
                if (x < 0 || y < 0 || x > width - 1 || y > height - 1) {
                    snake.kill();
                    return;
                }
            }
        }

        // Check Collision with target
        if (target != null) {
            headX = body[0][0];
            headY = body[0][1];
            if (headX == target[0] && headY == target[1]) {
                snake.grow();
                target = null;
                score++;
            }
        }
    }

    private double rayCast(int[][] body, Direction dir, boolean[][] snakeMatrix) {
        int headX = body[0][0];
        int headY = body[0][1];

        int maxDistance = switch (dir) {
            case UP -> headY;
            case DOWN -> height - headY - 1;
            case LEFT -> headX;
            case RIGHT -> width - headX - 1;
        };

        if (maxDistance == 0) {
            return 0;
        }

        for (int distance = 1; distance <= maxDistance; distance++) {
            int checkX = headX;
            int checkY = headY;

            switch (dir) {
                case UP -> {
                    checkY -= distance;
                }
                case DOWN -> {
                    checkY += distance;
                }
                case LEFT -> {
                    checkX -= distance;
                }
                case RIGHT -> {
                    checkX += distance;
                }
            }

            if (checkX >= width || checkY >= height || checkX < 0 || checkY < 0) {
                return 0.0;
            }

            if (snakeMatrix[checkX][checkY]) {
                return (double) (distance - 1) / Math.max(width, height); // Normalize
            }
        }

        return 1.0;
    }

    private double[] performRayCast(boolean[][] snakeMatrix) {
        int[][] body = snake.getBody();

        double distanceUp = rayCast(body, Direction.UP, snakeMatrix);
        double distanceDown = rayCast(body, Direction.DOWN, snakeMatrix);
        double distanceLeft = rayCast(body, Direction.LEFT, snakeMatrix);
        double distanceRight = rayCast(body, Direction.RIGHT, snakeMatrix);

        return new double[] { distanceUp, distanceDown, distanceLeft, distanceRight };
    }

}