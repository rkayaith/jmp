package com.troggo.jmp.screens.start;

import com.troggo.jmp.Jmp;
import com.troggo.jmp.Jmp.Screen;
import com.troggo.jmp.screens.SteppableScreen;

import com.badlogic.gdx.InputAdapter;

public class Start implements SteppableScreen {
    private final Jmp game;

    private final InputAdapter controller = new StartController(this);

    public Start(final Jmp _game) {
        game = _game;
        game.getInput().addProcessor(controller);
    }

    @Override
    public void dispose() {
        game.getInput().removeProcessor(controller);
    }

    @Override
    public void render(float delta) {
        game.getBatch().begin();
        game.getFont().draw(game.getBatch(), "Tap to start", 0, 10);
        game.getBatch().end();
    }

    void startGame() {
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