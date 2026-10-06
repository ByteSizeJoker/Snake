package com.snake.view;

import java.util.ArrayList;
import java.util.List;

import com.snake.Snake;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class SnakeView {

    private static final String[] SHAPE_CLASSES = {
            "round-north", "round-south", "round-east", "round-west",
            "corner-nw", "corner-ne", "corner-se", "corner-sw",
    };

    private static final double TARGET_SCALE = 0.75;

    private static String bodyShape(int[][] body, int i) {
        int currX = body[i][0];
        int currY = body[i][1];

        int dx = normalizeDelta(body[i - 1][0] - currX) + normalizeDelta(body[i + 1][0] - currX);
        int dy = normalizeDelta(body[i - 1][1] - currY) + normalizeDelta(body[i + 1][1] - currY);

        if (Math.abs(dx) != 1 || Math.abs(dy) != 1) {
            return null;
        }
        return (dy > 0 ? "corner-n" : "corner-s") + (dx > 0 ? "w" : "e");
    }

    private static int normalizeDelta(int delta) {
        if (delta > 1)
            return -1;
        if (delta < -1)
            return 1;
        return delta;
    }

    private Pane gameRoot = new Pane();
    private List<Region> snakeParts = new ArrayList<>();
    private Region target = new Region();

    private final double cellWidth;
    private final double cellHeight;
    private final int rows;
    private final int cols;

    public SnakeView(double cellWidth, double cellHeight, int rows, int cols) {
        this.cellHeight = cellHeight;
        this.cellWidth = cellWidth;
        this.rows = rows;
        this.cols = cols;

        target.setPrefWidth(cellWidth * TARGET_SCALE);
        target.setPrefHeight(cellHeight * TARGET_SCALE);
        target.getStyleClass().add("snake-target");

        gameRoot.getStyleClass().add("root");
        gameRoot.getChildren().add(target);
    }

    public Pane getGameRoot() {
        return gameRoot;
    }

    public void clear() {
        snakeParts.forEach(part -> gameRoot.getChildren().remove(part));
        snakeParts.clear();
        target.setVisible(false);
    }

    public void render(Snake snake, int[] targetPos) {
        while (snakeParts.size() < snake.getLength()) {
            Region part = new Region();
            part.setPrefWidth(cellWidth);
            part.setPrefHeight(cellHeight);
            part.getStyleClass().add("snake-part");
            snakeParts.add(part);
            gameRoot.getChildren().add(part);
        }

        int[][] body = snake.getBody();
        for (int i = 0; i < snakeParts.size(); i++) {
            Region part = snakeParts.get(i);

            part.getStyleClass().removeAll(SHAPE_CLASSES);

            part.setLayoutX(body[i][0] * cellWidth);
            part.setLayoutY(body[i][1] * cellHeight);

            String shape = (i > 0 && i < snakeParts.size() - 1) ? bodyShape(body, i) : null;
            if (shape != null) {
                part.getStyleClass().add(shape);
            }

        }

        roundHead(snake);
        roundTail(snake);
        renderTarget(targetPos);
    }

    private void roundHead(Snake snake) {
        Region head = snakeParts.get(0);
        switch (snake.getCurrentDir()) {
            case UP -> head.getStyleClass().add("round-north");
            case DOWN -> head.getStyleClass().add("round-south");
            case RIGHT -> head.getStyleClass().add("round-east");
            case LEFT -> head.getStyleClass().add("round-west");
        }
    }

    private void roundTail(Snake snake) {
        int[][] body = snake.getBody();
        if (body.length < 2)
            return;

        int lastIdx = body.length - 1;
        int tailX = body[lastIdx][0];
        int tailY = body[lastIdx][1];

        int prevIdx = lastIdx - 1;
        while (prevIdx >= 0 && body[prevIdx][0] == tailX && body[prevIdx][1] == tailY) {
            prevIdx--;
        }

        if (prevIdx < 0)
            return;

        int dx = body[prevIdx][0] - tailX;
        int dy = body[prevIdx][1] - tailY;

        if (dx > 1) {
            dx -= cols;
        } else if (dx < -1) {
            dx += cols;
        }

        if (dy > 1) {
            dy -= rows;
        } else if (dy < -1) {
            dy += rows;
        }

        String styleClass;
        if (dx > 0) {
            styleClass = "round-west";
        } else if (dx < 0) {
            styleClass = "round-east";
        } else if (dy > 0) {
            styleClass = "round-north";
        } else {
            styleClass = "round-south";
        }

        for (int k = lastIdx; k > prevIdx; k--) {
            snakeParts.get(k).getStyleClass().add(styleClass);
        }
    }

    private void renderTarget(int[] targetPos) {
        target.setVisible(targetPos != null);
        if (targetPos == null) {
            return;
        }

        target.setLayoutX(targetPos[0] * cellWidth + cellWidth / 2 * (1 - TARGET_SCALE));
        target.setLayoutY(targetPos[1] * cellHeight + cellHeight / 2 * (1 - TARGET_SCALE));
    }
}
