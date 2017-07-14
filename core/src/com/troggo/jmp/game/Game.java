package com.troggo.jmp.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.troggo.jmp.Jmp;

public class Game implements Screen {
    private final Jmp game;

    private OrthographicCamera camera;

    private Texture guyTexture;
    private Rectangle guy;

    public Game(final Jmp _game) {
        game = _game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 480, 800);

        guy = new Rectangle();
        guy.width = 20;
        guy.height = 50;
        guyTexture = new Texture(Gdx.files.internal("guy.png"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(guyTexture, guy.x, guy.y);
        game.batch.end();

        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            if (touchPos.x < 240) {
                guy.x -= 200 * delta;
            } else {
                guy.x += 200 * delta;
            }
        }
        if (guy.x < 0) guy.x = 0;
        if (guy.x > 480 - guy.width) guy.x = 480 - guy.width;
    }

    @Override
    public void dispose() {
        guyTexture.dispose();
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
