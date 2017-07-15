package com.troggo.jmp.start;

import com.badlogic.gdx.InputAdapter;
import com.troggo.jmp.Jmp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class Start implements Screen {
    private final Jmp game;

    private final OrthographicCamera camera = new OrthographicCamera();
    private final InputAdapter controller = new StartController(this);

    public Start(final Jmp game) {
        this.game = game;
        camera.setToOrtho(false, 480, 800);
        game.input.addProcessor(controller);
    }

    @Override
    public void dispose() {
        game.input.removeProcessor(controller);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.font.draw(game.batch, "Tap to start", 100, 150);
        game.batch.end();
    }

    void startGame() {
        game.setScreen(Jmp.Screen.GAME);
        dispose();
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
