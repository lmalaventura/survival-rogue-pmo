package it.university.survivor.view;

import it.university.survivor.model.Enemy;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Health;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.Objects;

public final class GameView {

    private static final double PLAYER_MARKER_RADIUS = 8.0;
    private static final double ENEMY_MARKER_RADIUS = 6.0;

    private final Canvas canvas;
    private final Pane root;

    public GameView(double width, double height) {
        if (!Double.isFinite(width) || width <= 0.0) {
            throw new IllegalArgumentException("Width must be finite and greater than zero");
        }
        if (!Double.isFinite(height) || height <= 0.0) {
            throw new IllegalArgumentException("Height must be finite and greater than zero");
        }

        canvas = new Canvas(width, height);
        root = new Pane();
        root.getChildren().add(canvas);
        root.setMinSize(width, height);
        root.setPrefSize(width, height);
        root.setMaxSize(width, height);
    }

    public Parent getRoot() {
        return root;
    }

    public void render(GameWorld world) {
        Objects.requireNonNull(world, "World must not be null");

        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

        graphics.setFill(Color.rgb(32, 37, 43));
        graphics.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

        double enemyMarkerDiameter = ENEMY_MARKER_RADIUS * 2.0;
        graphics.setFill(Color.CRIMSON);
        for (Enemy enemy : world.getEnemies()) {
            Position enemyPosition = enemy.getPosition();
            graphics.fillOval(
                    enemyPosition.x() - ENEMY_MARKER_RADIUS,
                    enemyPosition.y() - ENEMY_MARKER_RADIUS,
                    enemyMarkerDiameter,
                    enemyMarkerDiameter
            );
        }

        Player player = world.getPlayer();
        Position position = player.getPosition();
        double markerDiameter = PLAYER_MARKER_RADIUS * 2.0;

        graphics.setFill(Color.DODGERBLUE);
        graphics.fillOval(
                position.x() - PLAYER_MARKER_RADIUS,
                position.y() - PLAYER_MARKER_RADIUS,
                markerDiameter,
                markerDiameter
        );

        Health health = player.getHealth();
        graphics.setFill(Color.WHITE);
        graphics.fillText(
                "HP: " + health.getCurrentHealth() + " / " + health.getMaxHealth(),
                12.0,
                22.0
        );
    }
}
