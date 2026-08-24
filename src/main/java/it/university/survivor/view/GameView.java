package it.university.survivor.view;

import it.university.survivor.controller.RunState;
import it.university.survivor.model.Enemy;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Health;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.Projectile;
import it.university.survivor.model.enemy.EnemyType;
import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveProgression;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

public final class GameView {

    private static final double PLAYER_MARKER_RADIUS = 8.0;
    private static final double PROJECTILE_MARKER_RADIUS = 3.0;

    private static final double HUD_MARGIN = 16.0;
    private static final double HEART_X = HUD_MARGIN;
    private static final double HEART_Y = HUD_MARGIN;
    private static final double HEART_WIDTH = 38.0;
    private static final double HEART_HEIGHT = 34.0;
    private static final double HEART_OUTLINE_WIDTH = 2.0;
    private static final double HEART_TOOLTIP_GAP = 10.0;
    private static final double HEART_TOOLTIP_WIDTH = 112.0;
    private static final double HEART_TOOLTIP_HEIGHT = 24.0;
    private static final double HEART_TOOLTIP_PADDING = 6.0;

    private static final double XP_BAR_X = HUD_MARGIN;
    private static final double XP_BAR_WIDTH = 220.0;
    private static final double XP_BAR_HEIGHT = 14.0;
    private static final double XP_BAR_BOTTOM_MARGIN = 18.0;
    private static final double XP_BAR_INSET = 2.0;
    private static final double XP_LABEL_GAP = 6.0;

    private static final int TOTAL_WAVES = WaveProgression.MAX_WAVES;
    private static final double WAVE_FONT_SIZE = 18.0;
    private static final double RESULT_FONT_SIZE = 52.0;

    private static final double BOSS_BAR_WIDTH = 360.0;
    private static final double BOSS_BAR_HEIGHT = 16.0;
    private static final double BOSS_BAR_Y = 52.0;
    private static final double BOSS_LABEL_GAP = 5.0;
    private static final double BOSS_BAR_INSET = 2.0;

    private static final Color HEART_EMPTY_COLOR = Color.rgb(62, 24, 32);
    private static final Color HEART_FILL_COLOR = Color.rgb(220, 38, 58);
    private static final Color HEART_OUTLINE_COLOR = Color.rgb(32, 10, 16);
    private static final Color XP_BACKGROUND_COLOR = Color.rgb(24, 30, 40);
    private static final Color XP_FILL_COLOR = Color.rgb(45, 132, 220);
    private static final Color HUD_OUTLINE_COLOR = Color.rgb(12, 16, 22);
    private static final Color RESULT_OVERLAY_COLOR = Color.rgb(0, 0, 0, 0.58);
    private static final Color BOSS_BAR_BACKGROUND_COLOR = Color.rgb(52, 18, 24);
    private static final Color BOSS_BAR_FILL_COLOR = Color.rgb(190, 30, 55);
    private static final Color BOSS_BAR_OUTLINE_COLOR = Color.rgb(235, 190, 70);

    private final Canvas canvas;
    private final Pane root;
    private final ExperienceProgression experienceProgression;
    private boolean heartHovered;

    public GameView(double width, double height) {
        this(width, height, new ExperienceProgression());
    }

    public GameView(
            double width,
            double height,
            ExperienceProgression experienceProgression
    ) {
        if (!Double.isFinite(width) || width <= 0.0) {
            throw new IllegalArgumentException("Width must be finite and greater than zero");
        }
        if (!Double.isFinite(height) || height <= 0.0) {
            throw new IllegalArgumentException("Height must be finite and greater than zero");
        }

        this.experienceProgression = Objects.requireNonNull(
                experienceProgression,
                "Experience progression must not be null"
        );
        canvas = new Canvas(width, height);
        canvas.setOnMouseMoved(event -> heartHovered = isInsideHeart(
                event.getX(),
                event.getY()
        ));
        canvas.setOnMouseExited(event -> heartHovered = false);

        root = new Pane();
        root.getChildren().add(canvas);
        root.setMinSize(width, height);
        root.setPrefSize(width, height);
        root.setMaxSize(width, height);
    }

    public Parent getRoot() {
        return root;
    }

    public void render(GameWorld world, Wave currentWave, RunState runState) {
        Objects.requireNonNull(world, "World must not be null");
        Objects.requireNonNull(runState, "Run state must not be null");

        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

        graphics.setFill(Color.rgb(32, 37, 43));
        graphics.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            drawEnemy(graphics, enemy);
        }

        double projectileMarkerDiameter = PROJECTILE_MARKER_RADIUS * 2.0;
        graphics.setFill(Color.GOLD);
        for (Projectile projectile : world.getProjectiles()) {
            Position projectilePosition = projectile.getPosition();
            graphics.fillOval(
                    projectilePosition.x() - PROJECTILE_MARKER_RADIUS,
                    projectilePosition.y() - PROJECTILE_MARKER_RADIUS,
                    projectileMarkerDiameter,
                    projectileMarkerDiameter
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

        drawHealthHud(graphics, player.getHealth());
        drawExperienceHud(graphics);
        drawLifecycleFeedback(graphics, currentWave, runState);
    }

    private void drawLifecycleFeedback(
            GraphicsContext graphics,
            Wave currentWave,
            RunState runState
    ) {
        if (runState == RunState.ACTIVE_WAVE) {
            if (currentWave != null) {
                drawWaveHud(graphics, currentWave);
                drawBossHealthBar(graphics, currentWave);
            }
            return;
        }

        if (runState == RunState.VICTORY || runState == RunState.DEFEAT) {
            drawRunResultOverlay(graphics, runState);
        }
    }

    private void drawWaveHud(GraphicsContext graphics, Wave currentWave) {
        graphics.save();
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font("System", FontWeight.BOLD, WAVE_FONT_SIZE));
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.TOP);
        graphics.fillText(
                "WAVE " + currentWave.getWaveNumber() + " / " + TOTAL_WAVES,
                canvas.getWidth() / 2.0,
                HUD_MARGIN
        );
        graphics.restore();
    }

    private void drawEnemy(GraphicsContext graphics, Enemy enemy) {
        EnemyType enemyType = enemy.getType();
        double radius = enemyType.collisionRadius();
        double diameter = radius * 2.0;
        Position position = enemy.getPosition();

        graphics.save();
        graphics.setFill(enemyFillColor(enemyType));
        graphics.fillOval(
                position.x() - radius,
                position.y() - radius,
                diameter,
                diameter
        );
        graphics.setStroke(enemyOutlineColor(enemyType));
        graphics.setLineWidth(enemyOutlineWidth(enemyType));
        graphics.strokeOval(
                position.x() - radius,
                position.y() - radius,
                diameter,
                diameter
        );
        graphics.restore();
    }

    private void drawBossHealthBar(GraphicsContext graphics, Wave currentWave) {
        if (currentWave.getWaveNumber() != TOTAL_WAVES) {
            return;
        }

        Enemy boss = findLivingBoss(currentWave);
        if (boss == null) {
            return;
        }

        Health health = boss.getHealth();
        double healthRatio = calculateRatio(
                health.getCurrentHealth(),
                health.getMaxHealth()
        );
        double availableWidth = Math.max(0.0, canvas.getWidth() - 2.0 * HUD_MARGIN);
        double barWidth = Math.min(BOSS_BAR_WIDTH, availableWidth);
        double barX = (canvas.getWidth() - barWidth) / 2.0;
        double innerWidth = Math.max(0.0, barWidth - 2.0 * BOSS_BAR_INSET);
        double innerHeight = BOSS_BAR_HEIGHT - 2.0 * BOSS_BAR_INSET;

        graphics.save();
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font("System", FontWeight.BOLD, 14.0));
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.BOTTOM);
        graphics.fillText(
                "BOSS  " + health.getCurrentHealth() + " / " + health.getMaxHealth(),
                canvas.getWidth() / 2.0,
                BOSS_BAR_Y - BOSS_LABEL_GAP
        );

        graphics.setFill(BOSS_BAR_BACKGROUND_COLOR);
        graphics.fillRect(barX, BOSS_BAR_Y, barWidth, BOSS_BAR_HEIGHT);
        graphics.setFill(BOSS_BAR_FILL_COLOR);
        graphics.fillRect(
                barX + BOSS_BAR_INSET,
                BOSS_BAR_Y + BOSS_BAR_INSET,
                innerWidth * healthRatio,
                innerHeight
        );
        graphics.setStroke(BOSS_BAR_OUTLINE_COLOR);
        graphics.setLineWidth(2.0);
        graphics.strokeRect(barX, BOSS_BAR_Y, barWidth, BOSS_BAR_HEIGHT);
        graphics.restore();
    }

    private static Enemy findLivingBoss(Wave wave) {
        for (Enemy enemy : wave.getEnemies()) {
            if (enemy.getType() == EnemyType.BOSS && !enemy.isDead()) {
                return enemy;
            }
        }
        return null;
    }

    private static Color enemyFillColor(EnemyType enemyType) {
        return switch (enemyType) {
            case BASIC -> Color.CRIMSON;
            case FAST -> Color.ORANGERED;
            case TANK -> Color.SEAGREEN;
            case RANGED -> Color.MEDIUMPURPLE;
            case MINIBOSS -> Color.DARKORANGE;
            case BOSS -> Color.DARKMAGENTA;
        };
    }

    private static Color enemyOutlineColor(EnemyType enemyType) {
        return switch (enemyType) {
            case BASIC -> Color.DARKRED;
            case FAST -> Color.GOLD;
            case TANK -> Color.LIGHTGREEN;
            case RANGED -> Color.PLUM;
            case MINIBOSS, BOSS -> Color.GOLD;
        };
    }

    private static double enemyOutlineWidth(EnemyType enemyType) {
        return switch (enemyType) {
            case MINIBOSS -> 2.5;
            case BOSS -> 3.0;
            default -> 1.5;
        };
    }

    private void drawRunResultOverlay(GraphicsContext graphics, RunState runState) {
        graphics.save();
        graphics.setFill(RESULT_OVERLAY_COLOR);
        graphics.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font("System", FontWeight.BOLD, RESULT_FONT_SIZE));
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        graphics.fillText(
                runState == RunState.VICTORY ? "VICTORY" : "DEFEAT",
                canvas.getWidth() / 2.0,
                canvas.getHeight() / 2.0
        );
        graphics.restore();
    }

    private void drawHealthHud(GraphicsContext graphics, Health health) {
        double healthRatio = calculateRatio(
                health.getCurrentHealth(),
                health.getMaxHealth()
        );
        drawHeart(graphics, healthRatio);

        if (!heartHovered) {
            return;
        }

        double tooltipX = HEART_X + HEART_WIDTH + HEART_TOOLTIP_GAP;
        double tooltipY = HEART_Y + (HEART_HEIGHT - HEART_TOOLTIP_HEIGHT) / 2.0;

        graphics.save();
        graphics.setFill(Color.rgb(12, 16, 22, 0.88));
        graphics.fillRoundRect(
                tooltipX,
                tooltipY,
                HEART_TOOLTIP_WIDTH,
                HEART_TOOLTIP_HEIGHT,
                6.0,
                6.0
        );
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font("System", FontWeight.BOLD, 12.0));
        graphics.fillText(
                "HP: " + health.getCurrentHealth() + " / " + health.getMaxHealth(),
                tooltipX + HEART_TOOLTIP_PADDING,
                tooltipY + HEART_TOOLTIP_HEIGHT - HEART_TOOLTIP_PADDING
        );
        graphics.restore();
    }

    private void drawHeart(GraphicsContext graphics, double healthRatio) {
        graphics.save();

        traceHeartPath(graphics);
        graphics.setFill(HEART_EMPTY_COLOR);
        graphics.fill();

        if (healthRatio > 0.0) {
            double fillHeight = HEART_HEIGHT * healthRatio;
            double fillY = HEART_Y + HEART_HEIGHT - fillHeight;

            graphics.save();
            traceHeartPath(graphics);
            graphics.clip();
            graphics.setFill(HEART_FILL_COLOR);
            graphics.fillRect(HEART_X, fillY, HEART_WIDTH, fillHeight);
            graphics.restore();
        }

        traceHeartPath(graphics);
        graphics.setStroke(HEART_OUTLINE_COLOR);
        graphics.setLineWidth(HEART_OUTLINE_WIDTH);
        graphics.stroke();
        graphics.restore();
    }

    private static void traceHeartPath(GraphicsContext graphics) {
        double centerX = HEART_X + HEART_WIDTH / 2.0;
        double bottomY = HEART_Y + HEART_HEIGHT;

        graphics.beginPath();
        graphics.moveTo(centerX, bottomY);
        graphics.bezierCurveTo(
                HEART_X + HEART_WIDTH * 0.10,
                HEART_Y + HEART_HEIGHT * 0.64,
                HEART_X,
                HEART_Y + HEART_HEIGHT * 0.43,
                HEART_X,
                HEART_Y + HEART_HEIGHT * 0.27
        );
        graphics.bezierCurveTo(
                HEART_X,
                HEART_Y + HEART_HEIGHT * 0.04,
                HEART_X + HEART_WIDTH * 0.28,
                HEART_Y,
                centerX,
                HEART_Y + HEART_HEIGHT * 0.22
        );
        graphics.bezierCurveTo(
                HEART_X + HEART_WIDTH * 0.72,
                HEART_Y,
                HEART_X + HEART_WIDTH,
                HEART_Y + HEART_HEIGHT * 0.04,
                HEART_X + HEART_WIDTH,
                HEART_Y + HEART_HEIGHT * 0.27
        );
        graphics.bezierCurveTo(
                HEART_X + HEART_WIDTH,
                HEART_Y + HEART_HEIGHT * 0.43,
                HEART_X + HEART_WIDTH * 0.90,
                HEART_Y + HEART_HEIGHT * 0.64,
                centerX,
                bottomY
        );
        graphics.closePath();
    }

    private void drawExperienceHud(GraphicsContext graphics) {
        double barY = canvas.getHeight() - XP_BAR_BOTTOM_MARGIN - XP_BAR_HEIGHT;
        double experienceRatio = calculateRatio(
                experienceProgression.getCurrentExperience(),
                experienceProgression.getExperienceForNextLevel()
        );

        graphics.save();
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font("System", FontWeight.BOLD, 13.0));
        graphics.fillText(
                "LEVEL " + experienceProgression.getLevel(),
                XP_BAR_X,
                barY - XP_LABEL_GAP
        );

        graphics.setFill(XP_BACKGROUND_COLOR);
        graphics.fillRect(XP_BAR_X, barY, XP_BAR_WIDTH, XP_BAR_HEIGHT);

        double innerWidth = XP_BAR_WIDTH - 2.0 * XP_BAR_INSET;
        double innerHeight = XP_BAR_HEIGHT - 2.0 * XP_BAR_INSET;
        graphics.setFill(XP_FILL_COLOR);
        graphics.fillRect(
                XP_BAR_X + XP_BAR_INSET,
                barY + XP_BAR_INSET,
                innerWidth * experienceRatio,
                innerHeight
        );

        graphics.setStroke(HUD_OUTLINE_COLOR);
        graphics.setLineWidth(2.0);
        graphics.strokeRect(XP_BAR_X, barY, XP_BAR_WIDTH, XP_BAR_HEIGHT);
        graphics.restore();
    }

    private static double calculateRatio(int currentValue, int maximumValue) {
        if (maximumValue <= 0) {
            return 0.0;
        }

        double ratio = (double) currentValue / maximumValue;
        return Math.max(0.0, Math.min(1.0, ratio));
    }

    private static boolean isInsideHeart(double x, double y) {
        return x >= HEART_X && x <= HEART_X + HEART_WIDTH
                && y >= HEART_Y && y <= HEART_Y + HEART_HEIGHT;
    }
}
