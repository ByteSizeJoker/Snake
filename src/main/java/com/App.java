package com;

import java.util.ArrayList;
import java.util.List;

import com.snake.Direction;
import com.snake.Game;
import com.snake.Snake;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    private final int width = 800;
    private final int height = 800;
    private final int gameWidth = 40;
    private final int gameHeight = 40;

    private Canvas canvas;
    private GraphicsContext gc;
    private Game game = new Game(gameWidth, gameHeight);
    private Scene scene;

    private List<Region> snakeParts = new ArrayList<>();
    private Pane gameRoot;

    private double cellWidth = 20;
    private double cellHeight = 20;

    // timer to delay movement of snake
    AnimationTimer timer = new AnimationTimer() {
        private final long SECOND = (long) 1e9;
        private final int TPS = 8;
        private long lastTime = System.nanoTime();

        @Override
        public void handle(long now) {
            if (now - lastTime > SECOND / TPS) {
                game.update();
                styleSnake();
                // render(gc);
                lastTime = now;

                if (game.isGameOVer()) {
                    timer.stop();
                    scene.setRoot(buildGameOverScreen());
                }
            }
        }
    };

    @Override
    public void start(Stage stage) {
        scene = new Scene(buildStartScreen(), width, height);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Snake");
        stage.show();
    }

    private Pane buildStartScreen() {
        Label title = new Label("SNAKE");
        title.getStyleClass().add("title-label");

        Button startBtn = new Button("Start Game");
        startBtn.getStyleClass().add("menu-button");
        startBtn.setOnAction(e -> startGame());

        VBox layout = new VBox(30, title, startBtn);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("root");
        return layout;
    }

    private Pane buildGameOverScreen() {
        Label title = new Label("GAME OVER");
        title.getStyleClass().add("title-label");

        Label scoreLabel = new Label("Score: " + game.getScore());
        scoreLabel.getStyleClass().add("score-label");

        Button retryBtn = new Button("Retry");
        retryBtn.getStyleClass().add("menu-button");
        retryBtn.setOnAction(e -> startGame());

        VBox layout = new VBox(20, title, scoreLabel, retryBtn);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("root");
        return layout;
    }

    private void startGame() {
        game.reset();
        snakeParts.clear();

        gameRoot = new Pane();
        gameRoot.getStyleClass().add("root");
        scene.setRoot(gameRoot);
        setupInput(scene);

        styleSnake();
        timer.start();
    }

    private void styleSnake() {
        Snake snake = game.getSnake();
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

            if (i == 0 && !part.getStyleClass().contains("snake-head")) {
                part.getStyleClass().add("snake-head");
            } else if (i == snakeParts.size() - 1 && !part.getStyleClass().contains("snake-tail")) {
                part.getStyleClass().add("snake-tail");
            } else if (part.getStyleClass().contains("snake-tail")) {
                part.getStyleClass().remove("snake-tail");
                part.setStyle("-fx-background-radius: 0 0 0 0");
            }

            part.setTranslateX(body[i][0] * cellWidth);
            part.setTranslateY(body[i][1] * cellHeight);
        }

        Region head = snakeParts.get(0);
        Region tail = snakeParts.get(snakeParts.size() - 1);

        switch (snake.getCurrentDir()) {
            case UP -> head.setStyle("-fx-background-radius: 50 50 0 0");
            case DOWN -> head.setStyle("-fx-background-radius: 0 0 50 50");
            case RIGHT -> head.setStyle("-fx-background-radius: 0 50 50 0");
            case LEFT -> head.setStyle("-fx-background-radius: 50 0 0 50");
        }

        int tailX = body[body.length - 1][0];
        int tailY = body[body.length - 1][1];
        int partBeforeTailX = body[body.length - 2][0];
        int partBeforeTailY = body[body.length - 2][1];

        if (tailX == partBeforeTailX && tailY > partBeforeTailY) {
            tail.setStyle("-fx-background-radius: 0 0 50 50");
        } else if (tailX == partBeforeTailX && tailY < partBeforeTailY) {
            tail.setStyle("-fx-background-radius: 50 50 0 0");
        } else if (tailX > partBeforeTailX && tailY == partBeforeTailY) {
            tail.setStyle("-fx-background-radius: 0 50 50 0");
        } else if (tailX < partBeforeTailX && tailY == partBeforeTailY) {
            tail.setStyle("-fx-background-radius: 50 0 0 50");
        }
        if (game.getTarget() != null) {
            int targetX = (int) (game.getTarget()[0] * cellWidth);
            int targetY = (int) (game.getTarget()[1] * cellHeight);

            Region target = new Region();
            target.setPrefWidth(cellWidth);
            target.setPrefHeight(cellHeight);
            target.getStyleClass().add("snake-target");
            target.setLayoutX(targetX);
            target.setLayoutY(targetY);
            gameRoot.getChildren().remove(lastTarget);
            gameRoot.getChildren().add(target);
            lastTarget = target;
        }

    }

    Region lastTarget = null;

    private void setupInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> game.setDirection(Direction.UP);
                case DOWN -> game.setDirection(Direction.DOWN);
                case LEFT -> game.setDirection(Direction.LEFT);
                case RIGHT -> game.setDirection(Direction.RIGHT);
                default -> {
                }
            }
        });
    }
}