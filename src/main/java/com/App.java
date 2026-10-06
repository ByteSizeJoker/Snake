package com;

import com.snake.Direction;
import com.snake.Game;
import com.snake.GameConfig;
import com.snake.neat.Genome;
import com.snake.neat.NeuralNetwork;
import com.snake.neat.Population;
import com.snake.view.SnakeView;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;
    private static final long SECOND = (long) 1e9;
    private static final double TPS = 50;

    private static final double CELL_WIDTH = (double) WINDOW_WIDTH / GameConfig.BOARD_COLS;
    private static final double CELL_HEIGHT = (double) WINDOW_HEIGHT / GameConfig.BOARD_ROWS;

    public static void main(String[] args) {
        launch();
    }

    private Game game = new Game(GameConfig.BOARD_COLS, GameConfig.BOARD_ROWS);

    private Scene scene;

    private SnakeView view;

    // timer to delay movement of snake
    private AnimationTimer timer = new AnimationTimer() {
        private long lastTime = System.nanoTime();

        @Override
        public void handle(long now) {
            if (now - lastTime > SECOND / TPS) {
                lastTime = now;
                // tick();
                testBestGenome();
            }
        }
    };
    NeuralNetwork nn;

    @Override
    public void start(Stage stage) {
        scene = new Scene(buildStartScreen(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        view = new SnakeView(CELL_WIDTH, CELL_HEIGHT, GameConfig.BOARD_ROWS, GameConfig.BOARD_COLS);

        stage.setScene(scene);
        stage.setTitle("Snake");
        stage.show();

        Population p = new Population();
        Genome best = p.run();
        nn = new NeuralNetwork(best);
        nn.buildNetwork();
    }

    public void tick() {
        game.update();
        view.render(game.getSnake(), game.getTarget());

        if (game.isGameOver()) {
            timer.stop();
            scene.setRoot(buildGameOverScreen());
        }
    }

    public void testBestGenome() {
        if (game.isPaused()) {
            return;
        }
        System.out.println("Testing best genome...");

        int steps = 0;
        double[] state = game.getState();
        System.out.println("\n--------------------------------");
        System.out.printf("Danger: %.0f, %.0f, %.0f, %.0f\n", state[0], state[1], state[2], state[3]);
        System.out.printf("Food: %.0f, %.0f, %.0f, %.0f\n", state[4], state[5], state[6], state[7]);
        System.out.printf("Direction:  %.0f, %.0f, %.0f, %.0f\n", state[8], state[9], state[10], state[11]);
        System.out.printf("Raycast: %.4f, %.4f, %.4f, %.4f\n", state[12], state[13], state[14], state[15]);

        double[] output = nn.evaluate(state);
        System.out.printf("Up: %.4f, Down: %.4f, Left: %.4f, Right: %.4f\n", output[0], output[1], output[2],
                output[3]);

        Direction dir = nn.predict(game.getSnake().getCurrentDir(), state);
        game.setDirection(dir);
        game.update();
        steps++;

        view.render(game.getSnake(), game.getTarget());

        if (game.isGameOver()) {
            timer.stop();
            scene.setRoot(buildGameOverScreen());
        }

        System.out.printf("Score: %d | Total Steps: %d | Avg Steps: %.2f\n", game.getScore(), steps,
                (double) game.getSteps() / game.getScore());
    }

    private VBox buildGameOverScreen() {
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

    private VBox buildStartScreen() {
        Label title = new Label("SNAKE");
        title.getStyleClass().add("title-label");

        Button startBtn = new Button("Start Game");
        startBtn.getStyleClass().add("menu-button");
        startBtn.setOnAction(e -> startGame());

        // VBox -> Vertical Box. 30 - (Vertical) Spacing between children
        VBox layout = new VBox(30, title, startBtn);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("root");
        return layout;
    }

    private void startGame() {
        game.reset();
        view.clear();

        scene.setRoot(view.getGameRoot());
        setupInput();

        view.render(game.getSnake(), game.getTarget());
        timer.start();
    }

    private void setupInput() {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> game.setDirection(Direction.UP);
                case DOWN -> game.setDirection(Direction.DOWN);
                case LEFT -> game.setDirection(Direction.LEFT);
                case RIGHT -> game.setDirection(Direction.RIGHT);
                case ESCAPE -> game.setPaused(!game.isPaused());
                default -> {
                }
            }
        });
    }
}