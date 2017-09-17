package com.troggo.jmp.screens;

import com.troggo.jmp.Jmp;
import com.troggo.jmp.Jmp.Screen;
import com.troggo.jmp.utils.TouchInput;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

import static com.troggo.jmp.utils.GlyphLayoutKt.getHeight;

public class StartScreen implements SteppableScreen {
    private final Jmp game;

    private final InputAdapter controller = new TouchInput(() -> {
        startGame();
        return true;
    });

    public StartScreen(final Jmp _game) {
        game = _game;
        game.getInput().addProcessor(controller);
    }

    @Override
    public void dispose() {
        game.getInput().removeProcessor(controller);
    }

    @Override
    public void render(float delta) {
        float y = game.getCamera().viewportHeight / 2 + 1.5f;
        Camera cam = game.getCamera();
        GlyphLayout tap = game.write(game.getFontH1(), "Tap To Start", 0, y, Align.center);
        y -= getHeight(tap, cam) * 1.85;
        GlyphLayout hold = game.write(game.getFontH3(), "HOLD LEFT/RIGHT TO MOVE", 0, y, Align.center);
        y -= getHeight(hold, cam) * 1.3;
        game.write(game.getFontH3(), "TAP TO JUMP", 0, y, Align.center);
    }

    private void startGame() {
        game.setScreen(Screen.GAME);
        dispose();
    }

    @Override
    public void step(float delta) { }

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
