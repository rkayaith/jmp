package com.troggo.jmp.screens.game;

import com.troggo.jmp.Jmp;
import com.troggo.jmp.actors.Guy;
import com.troggo.jmp.screens.SteppableScreen;

public class Game implements SteppableScreen {
    private final Jmp game;

    private final Guy guy;

    public Game(final Jmp _game) {
        game = _game;
        guy = new Guy(game);
        game.getInput().addProcessor(guy.getController());
    }

    @Override
    public void render(float delta) {
        guy.render();
    }

    @Override
    public void step(float delta) {
        guy.step();
    }

    @Override
    public void dispose() {
        guy.dispose();
        game.getInput().removeProcessor(guy.getController());
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
