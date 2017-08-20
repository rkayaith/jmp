package com.troggo.jmp.screens.game;

import com.troggo.jmp.Jmp;
import com.troggo.jmp.entities.Box;
import com.troggo.jmp.entities.Guy;
import com.troggo.jmp.screens.SteppableScreen;

import com.badlogic.gdx.utils.Array;

import static com.troggo.jmp.entities.BoxKt.BOX_HEIGHT;

public class Game implements SteppableScreen {

    private static final float BOX_SPAWN_RATE = 1.5f;   // s

    private final Jmp game;

    private final Guy guy;
    private final Array<Box> boxes = new Array<Box>();

    private float boxSpawnDelta = BOX_SPAWN_RATE;

    public Game(final Jmp _game) {
        game = _game;
        guy = new Guy(game);
        game.getInput().addProcessor(guy.getController());
    }

    @Override
    public void render(float delta) { }

    @Override
    public void step(float delta) {
        boxSpawnDelta += delta;
        if (boxSpawnDelta > BOX_SPAWN_RATE) {
            boxSpawnDelta -= BOX_SPAWN_RATE;
            // make some boxes square and some half width rectangles
            float width = Math.random() > 0.6 ? BOX_HEIGHT : BOX_HEIGHT / 2;
            float leftLim = width / 2;
            float rightLim = game.getCamera().viewportWidth - width / 2;
            float x = (float)Math.random() * (rightLim - leftLim) + leftLim;
            // align x position nicely
            x = leftLim + Math.round((x - leftLim) / (BOX_HEIGHT / 2)) * BOX_HEIGHT / 2;
            // limit number of boxes
            // TODO: clean up off screen boxes instead
            if (boxes.size >= 30) {
                for (Box box : boxes) {
                    box.dispose();
                }
                boxes.clear();
            }

            boxes.add(new Box(game, width, x, game.getCamera().viewportHeight));
        }
    }

    @Override
    public void dispose() {
        guy.dispose();
        game.getInput().removeProcessor(guy.getController());
        for (Box box : boxes) {
            box.dispose();
        }
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
