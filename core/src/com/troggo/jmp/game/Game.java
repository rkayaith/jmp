package com.troggo.jmp.game;

import com.troggo.jmp.Jmp;
import com.troggo.jmp.actors.Guy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class Game implements Screen {
    private final Jmp game;

    private OrthographicCamera camera;

    private Guy guy;

    public Game(final Jmp _game) {
        game = _game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 480, 800);

        guy = new Guy(camera, game.batch);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        guy.render(delta);
    }

    @Override
    public void dispose() {
        guy.dispose();
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
