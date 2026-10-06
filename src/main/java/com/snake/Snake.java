package com.snake;

import java.util.Arrays;

/**
 * Snake Class to create a snake which is it own movement and is independent of it surrounding 
 */
public class Snake {
    private int[][] body;
    private boolean isAlive = true;

    private Direction currentDir = Direction.LEFT;

    // Constructor to create instance of snake with given length
    public Snake(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        body = new int[length][2];
    }

    // Spawn the snake
    public Snake spawn(int x, int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Spawn coordinates must be positive");
        }

        for (int i = 0; i < body.length; i++) {
            body[i][0] = x + i;
            body[i][1] = y;
        }
        return this;
    }

    public int getLength() {
        return body.length;
    }

    // Move the snake in set direction
    public void move(Direction dir) {
        if (!isAlive) {
            return;
        }

        if (dir == null) {
            dir = currentDir;
            System.out.println("Encountered null direction. Using current direction: " + currentDir);
        }

        if (dir != currentDir.opposite()) {
            currentDir = dir;
        }

        for (int i = body.length - 1; i > 0; i--) {
            body[i][0] = body[i - 1][0];
            body[i][1] = body[i - 1][1];
        }

        switch (currentDir) {
            case UP -> body[0][1] -= 1;
            case DOWN -> body[0][1] += 1;
            case LEFT -> body[0][0] -= 1;
            case RIGHT -> body[0][0] += 1;
        }
    }

    public void kill() {
        isAlive = false;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int[][] getBody() {
        return body;
    }

    // duplicates the tails to grow snake
    public void grow() {
        int[][] newSnake = Arrays.copyOf(body, body.length + 1);

        newSnake[newSnake.length - 1] = new int[2];
        newSnake[newSnake.length - 1][0] = body[body.length - 1][0];
        newSnake[newSnake.length - 1][1] = body[body.length - 1][1];

        body = newSnake;
    }

    public Direction getCurrentDir() {
        return currentDir;
    }
}
