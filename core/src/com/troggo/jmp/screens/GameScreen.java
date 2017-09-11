package com.troggo.jmp.screens;

import com.troggo.jmp.Jmp;
import com.troggo.jmp.entities.Box;
import com.troggo.jmp.entities.Ground;
import com.troggo.jmp.entities.Guy;
import com.troggo.jmp.screens.SteppableScreen;
import com.troggo.jmp.utils.Pool;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import java.util.List;

import static com.troggo.jmp.Jmp.WORLD_WIDTH;
import static com.troggo.jmp.entities.BoxKt.BOX_FALL_SPEED;
import static com.troggo.jmp.entities.BoxKt.BOX_HEIGHT;
import static com.troggo.jmp.entities.GuyKt.GUY_HEIGHT;
import static com.troggo.jmp.utils.CameraKt.getBottom;
import static com.troggo.jmp.utils.CameraKt.getTop;
import static com.troggo.jmp.utils.GlyphLayoutKt.getHeight;
import static com.troggo.jmp.utils.GlyphLayoutKt.getWidth;

public class GameScreen implements SteppableScreen {

    private static final float BOX_SPAWN_DISTANCE = 8.0f;   // m
    private static final float CAM_CENTER_PERCENT = 0.65f;
    private static final float CAM_MIN_SPEED = 1.0f;        // m/s
    private static final float CAM_MAX_SPEED = 5.0f;        // m/s
    private static final float CAM_MAX_SPEED_HEIGHT = 100;  // m

    private final Jmp game;
    private final int highScore;

    private final Guy guy;
    private final Pool<Box> squareBoxes;
    private final Pool<Box> rectBoxes;
    private final float groundLevel;

    private int score = 0;
    private float guyMaxY = 0;
    private float boxSpawnDelta = 0;
    private float boxSpawnY = 0;

    public GameScreen(final Jmp _game, int _highScore) {
        game = _game;
        highScore = _highScore;

        squareBoxes = new Pool<>(pool -> new Box(game, pool, true, 0, 0));
        rectBoxes = new Pool<>(pool -> new Box(game, pool, false, 0, 0));

        Ground ground = game.getGround();
        groundLevel = ground.getPosition().y + ground.getDimensions().getHeight() / 2 + GUY_HEIGHT / 2;
        guy = new Guy(game, groundLevel);
        game.getInput().addProcessor(guy.getController());
        spawnBox(getTop(game.getCamera()));
    }

    @Override
    public void dispose() {
        guy.dispose();
        game.getInput().removeProcessor(guy.getController());
        List<Box> boxes = squareBoxes.empty();
        boxes.addAll(rectBoxes.empty());
        for (Box box : boxes) {
            box.dispose();
        }
    }

    @Override
    public void render(float delta) {
        // raise camera if Guy gets too high
        float guyY = guy.getPosition().y;
        Camera cam = game.getCamera();
        float camY = getBottom(cam) + cam.viewportHeight * CAM_CENTER_PERCENT;
        if (guyY > camY) {
            game.getCamera().translate(0, guyY - camY);
        }

        // draw scores
        int highScore = Math.max(score, this.highScore);
        BitmapFont font = game.getFontH3();
        GlyphLayout hLabel = new GlyphLayout(font, "HIGH SCORE: ");
        GlyphLayout hValue = new GlyphLayout(font, "" + highScore);
        GlyphLayout sLabel = new GlyphLayout(font, "SCORE: ");
        GlyphLayout sValue = new GlyphLayout(font, "" + score);
        float y;
        float x;
        float width = getWidth(hLabel, cam) + Math.max(getWidth(hValue, cam), getWidth(sValue, cam));
        if (guy.isDead()) {
            y = cam.viewportHeight * 0.6f;
            x = game.getCamera().viewportWidth / 2f - width / 2;
            GlyphLayout rip = game.write(game.getFontH1(), "RIP", 0, y, Align.center);
            y -= getHeight(rip, cam) * 1.2f;
        } else {
            y = cam.viewportHeight - 0.4f;
            x = 0.3f;
        }
        game.write(font, hLabel, x, y);
        game.write(font, hValue, x + width - getWidth(hValue, cam), y);
        y -= getHeight(hLabel, cam) * 1.3f;
        game.write(font, sLabel, x, y);
        game.write(font, sValue, x + width - getWidth(sValue, cam), y);
        if (guy.isDead() && highScore > this.highScore) {
            y -= getHeight(sLabel, cam) * 2;
            game.write(game.getFontH2(), "New High Score!", 0, y, Align.center);
        }
    }

    @Override
    public void step(float delta) {
        guyMaxY = Math.max(guyMaxY, guy.getPosition().y - groundLevel);

        // end game if Guy is dead
        if (guy.isDead()) {
            game.gameOver(score);
            return;
        }

        // spawn box
        boxSpawnDelta += delta;
        float boxY = boxSpawnY - boxSpawnDelta * BOX_FALL_SPEED;
        if (getTop(game.getCamera()) >= boxY + BOX_SPAWN_DISTANCE) {
            spawnBox(boxY + BOX_SPAWN_DISTANCE);
        }

        // camera creep
        float speed = guyMaxY * (CAM_MAX_SPEED - CAM_MIN_SPEED) / CAM_MAX_SPEED_HEIGHT + CAM_MIN_SPEED;
        game.getCamera().translate(0, Math.min(speed, CAM_MAX_SPEED) * delta);
    }

    private void spawnBox(float y) {
        // make some boxes square and some half width rectangles
        Box box = Math.random() > 0.6 ? squareBoxes.obtain() : rectBoxes.obtain();
        float x = generateBoxX(0, WORLD_WIDTH, box.getDimensions().getWidth());
        // create boxes off the screen
        box.setPosition(new Vector2(x, y + BOX_HEIGHT));
        boxSpawnY = y;
        boxSpawnDelta = 0;
    }

    private float generateBoxX(float leftLim, float rightLim, float width) {
        leftLim += width / 2;
        rightLim -= width / 2;
        float x = (float)Math.random() * (rightLim - leftLim) + leftLim;
        // align x position nicely
        return leftLim + Math.round((x - leftLim) / (BOX_HEIGHT / 2)) * BOX_HEIGHT / 2;
    }

    @Override
    public void resize(int width, int height) { }

    @Override
    public void show() { }

    @Override
    public void hide() { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    // getters / setters
    public int getScore() {
        return score;
    }

    public void setScore(int s) {
        score = s;
    }
}
