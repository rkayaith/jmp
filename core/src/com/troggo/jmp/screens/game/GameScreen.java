package com.troggo.jmp.screens.game;

import com.troggo.jmp.Jmp;
import com.troggo.jmp.entities.Box;
import com.troggo.jmp.entities.Guy;
import com.troggo.jmp.screens.SteppableScreen;
import com.troggo.jmp.utils.Pool;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.troggo.jmp.Jmp.WORLD_WIDTH;
import static com.troggo.jmp.entities.BoxKt.BOX_FALL_SPEED;
import static com.troggo.jmp.entities.BoxKt.BOX_HEIGHT;
import static com.troggo.jmp.utils.CameraKt.getTop;

public class GameScreen implements SteppableScreen {

    private static final float BOX_SPAWN_DISTANCE = 8;  // m

    private final Jmp game;

    private final Guy guy;
    private final Pool<Box> squareBoxes = new Pool<Box>() {
        @NotNull
        @Override
        protected Box create(@NotNull Pool<Box> pool) {
            return new Box(game, pool, 0, 0, BOX_HEIGHT);
        }
    };

    private final Pool<Box> rectBoxes = new Pool<Box>() {
        @NotNull
        @Override
        protected Box create(@NotNull Pool<Box> pool) {
            return new Box(game, pool, 0, 0, BOX_HEIGHT / 2);
        }
    };

    private float guyMaxY = 0;
    private float boxSpawnDelta = 0;
    private float boxSpawnY = 0;

    public GameScreen(final Jmp _game) {
        game = _game;
        guy = new Guy(game);
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
        float guyY = guy.getPosition().y;
        float camY = game.getCamera().position.y;
        if (guyY > camY) {
            game.getCamera().translate(0, guyY - camY);
        }
    }

    @Override
    public void step(float delta) {
        // end game if Guy is dead
        guyMaxY = Math.max(guyMaxY, guy.getPosition().y);
        if (guy.isDead()) {
            game.gameOver(guyMaxY);
            return;
        }

        // spawn box
        boxSpawnDelta += delta;
        float boxY = boxSpawnY - boxSpawnDelta * BOX_FALL_SPEED;
        if (getTop(game.getCamera()) >= boxY + BOX_SPAWN_DISTANCE) {
            spawnBox(boxY + BOX_SPAWN_DISTANCE);
        }
    }

    private void spawnBox(float y) {
        // make some boxes square and some half width rectangles
        Box box = Math.random() > 0.6 ? squareBoxes.obtain() : rectBoxes.obtain();
        float width = box.getDimensions().getWidth();
        float leftLim = width / 2;
        float rightLim = WORLD_WIDTH - width / 2;
        float x = (float)Math.random() * (rightLim - leftLim) + leftLim;
        // align x position nicely
        x = leftLim + Math.round((x - leftLim) / (BOX_HEIGHT / 2)) * BOX_HEIGHT / 2;

        box.setPosition(new Vector2(x, y + BOX_HEIGHT));    // create box off the screen
        boxSpawnY = y;
        boxSpawnDelta = 0;
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
}
