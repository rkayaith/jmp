package com.troggo.jmp;

import com.troggo.jmp.screens.SteppableScreen;
import com.troggo.jmp.screens.game.Game;
import com.troggo.jmp.screens.start.Start;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Jmp extends com.badlogic.gdx.Game {

    public enum Screen {
        START, GAME
    }

    private static final float WORLD_WIDTH = 20f;   // m
    private static final float WORLD_GRAVITY = 25f; // m/s^2
    private static final float WORLD_TIME_STEP = 1/300f;
    private static final float MAX_STEP_DELTA = 0.25f;

    private Box2DDebugRenderer debugRenderer;

    private World world;
    private float worldDelta = 0;   // how far behind the world is from current time
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private BitmapFont font;
    private InputMultiplexer input;

    public void create() {
        Box2D.init();
        world = new World(new Vector2(0, -WORLD_GRAVITY), true);
        batch = new SpriteBatch();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_WIDTH * h / w);

        // TODO: fix font
        font = new BitmapFont();    // Arial
        font.getData().setScale(0.2f);

        input = new InputMultiplexer();
        Gdx.input.setInputProcessor(input);

        debugRenderer = new Box2DDebugRenderer();

        createGround();
        setScreen(Screen.START);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        super.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        super.render();
        debugRenderer.render(world, camera.combined);

        // cap max time we step so we don't overload slow devices
        step(Math.min(Gdx.graphics.getRawDeltaTime(), MAX_STEP_DELTA));
    }

    private void step(float delta) {
        worldDelta += delta;
        // catch physics world up to current time
        while (worldDelta > WORLD_TIME_STEP) {
            // we use constant time steps to keep physics consistent
            if (screen instanceof SteppableScreen) {
                ((SteppableScreen)screen).step(WORLD_TIME_STEP);
            }
            world.step(WORLD_TIME_STEP, 6, 2);
            worldDelta -= WORLD_TIME_STEP;
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_WIDTH * height / width);
        super.resize(width, height);
    }

    public boolean setScreen(Screen screen) {
        switch (screen) {
            case START: super.setScreen(new Start(this)); return true;
            case GAME: super.setScreen(new Game(this)); return true;
            default: return false;
        }
    }

    private void createGround() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(new Vector2(0, 1));
        PolygonShape box = new PolygonShape();
        box.setAsBox(WORLD_WIDTH, 2);

        Body ground = world.createBody(bodyDef);
        ground.createFixture(box, 0);
        box.dispose();
    }

    // getters
    public World getWorld() {
        return world;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    public InputMultiplexer getInput() {
        return input;
    }
}
