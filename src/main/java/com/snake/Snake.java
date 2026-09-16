package com.snake;

import java.util.Arrays;

public class Snake {
    private int[][] snake;
    private boolean isAlive = true;

    private Direction currentDir = Direction.LEFT;

    // Constructor to create instance of snake with given length
    public Snake(int length) {
        snake = new int[length][2];
    }

    // Spawn the snake
    public Snake spawn(int x, int y) {
        for (int i = 0; i < snake.length; i++) {
            snake[i][0] = x + i;
            snake[i][1] = y;
        }
        return this;
    }

    public int getLength() {
        return snake.length;
    }

    // Move the snake in set direction
    public void move(Direction dir) {
        if (!isAlive)
            return;

        boolean validInput = !(dir == Direction.UP && currentDir == Direction.DOWN
                || dir == Direction.DOWN && currentDir == Direction.UP
                || dir == Direction.LEFT && currentDir == Direction.RIGHT
                || dir == Direction.RIGHT && currentDir == Direction.LEFT);

        if (validInput) {
            currentDir = dir;

        }

        for (int i = snake.length - 1; i > 0; i--) {
            snake[i][0] = snake[i - 1][0];
            snake[i][1] = snake[i - 1][1];
        }

        switch (currentDir) {
            case UP: {
                snake[0][1] -= 1;
                break;
            }
            case DOWN: {
                snake[0][1] += 1;
                break;
            }
            case LEFT: {
                snake[0][0] -= 1;
                break;
            }
            case RIGHT: {
                snake[0][0] += 1;
                break;
            }
        }
    }

    public void kill() {
        isAlive = false;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int[][] getBody() {
        return snake;
    }

    // duplicates the tails to grow snake
    public void grow(int lastX, int lastY) {
        int[][] newSnake = Arrays.copyOf(snake, snake.length + 1);

        newSnake[newSnake.length - 1] = new int[2];
        newSnake[newSnake.length - 1][0] = lastX;
        newSnake[newSnake.length - 1][1] = lastY;

        snake = newSnake;
    }

    public int length() {
        return snake.length;
    }

    public Direction getCurrentDir() {
        return currentDir;
    }
}
