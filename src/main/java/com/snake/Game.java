package com.snake;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private static final Random random = new Random();
    private static final String COLLISION_TYPE = "FAZE";
    private int[][] GAME_MATRIX;
    private final int SNAKE_LENGTH = 5;

    private Snake snake;
    private Direction dir = Direction.LEFT;
    // List<int[]> targets;
    int[] target = null;
    private int score = 0;

    public Game(int width, int height) {
        GAME_MATRIX = new int[width][height];
    }

    // spawn snake
    public Snake initSnake(int length, int x, int y) {
        if (length == 0) {
            length = SNAKE_LENGTH;
        }
        snake = new Snake(length).spawn(x, y);
        System.out.println("Snake Spawned at " + x + " " + y);
        return snake;
    }

    public Snake initSnake() {
        return initSnake(SNAKE_LENGTH, GAME_MATRIX.length / 2, GAME_MATRIX[0].length / 2);
    }

    public void update() {
        checkCollision();

        if (!snake.isAlive()) {
            return;
        }

        snake.move(dir);

        if (target == null) {
            target = initTarget();
        }
    }

    public void setDirection(Direction dir) {
        this.dir = dir;
    }

    @Override
    // Temp method to see snake in console
    public String toString() {
        for (int i = 0; i < GAME_MATRIX.length; i++) {
            for (int j = 0; j < GAME_MATRIX[i].length; j++) {
                System.out.print(" " + GAME_MATRIX[i][j] + " ");
            }
            System.out.println();
        }
        return "";
    }

    public Snake getSnake() {
        return snake;
    }

    public int[] getTarget() {
        return target;
    }

    // Spawns target using either random attempt or by filtering empty cells
    private int[] initTarget() {
        if (snake.length() > (int) (0.90 * GAME_MATRIX.length * GAME_MATRIX[0].length)) {
            List<int[]> emptyCells = new ArrayList<>();

            for (int i = 0; i < GAME_MATRIX.length; i++) {
                for (int j = 0; j < GAME_MATRIX[i].length; j++) {
                    if (GAME_MATRIX[i][j] == 0) {
                        emptyCells.add(new int[] { i, j });
                    }
                }
            }

            if (emptyCells.size() == 0) {
                target = null;
            } else {
                target = emptyCells.get(random.nextInt(emptyCells.size()));
            }
        } else {
            int x = random.nextInt(GAME_MATRIX.length);
            int y = random.nextInt(GAME_MATRIX[0].length);

            for (int[] part : snake.getBody()) {
                if (part[0] == x && part[1] == y) {
                    target = initTarget();
                }
                target = new int[] { x, y };
            }
        }

        System.out.println("Target set at " + target[0] + " " + target[1]);
        return target;
    }

    private void checkCollision() {
        int[][] body = snake.getBody();
        int headX = body[0][0];
        int headY = body[0][1];
        int tailX = body[body.length - 1][0];
        int tailY = body[body.length - 1][1];

        // Check Collision with target
        if (target != null) {
            if (headX == target[0] && headY == target[1]) {
                snake.grow(tailX, tailY);
                target = null;
                score++;
            }
        }

        for (int i = 0; i < body.length; i++) {
            int x = body[i][0];
            int y = body[i][1];

            // Check Collision with body
            if (x == headX && y == headY && i != 0) {
                snake.kill();
            }

            // Check if snake collides with walls
            if (COLLISION_TYPE.equals("FAZE")) {
                if (x < 0) {
                    body[i][0] = GAME_MATRIX.length - 1;
                } else if (x > GAME_MATRIX.length - 1) {
                    body[i][0] = 0;
                } else if (y < 0) {
                    body[i][1] = GAME_MATRIX[0].length - 1;
                } else if (y > GAME_MATRIX[0].length - 1) {
                    body[i][1] = 0;
                }
            } else if (COLLISION_TYPE.equals("COLLIDE")) {
                if (x < 0 || y < 0 || x > GAME_MATRIX.length - 1 || y > GAME_MATRIX[0].length - 1) {
                    snake.kill();
                    return;
                }
            }
        }
    }

    public int getScore() {
        return score;
    }

    public void reset() {
        score = 0;
        dir = Direction.LEFT;
        target = null;
        initSnake();
    }

    public boolean isGameOVer() {
        return snake != null && !snake.isAlive();
    }
}
