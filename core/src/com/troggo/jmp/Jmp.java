package com.troggo.jmp;

import com.troggo.jmp.entities.Entity;
import com.troggo.jmp.entities.EntityContactListener;
import com.troggo.jmp.entities.Ground;
import com.troggo.jmp.entities.Wall;
import com.troggo.jmp.screens.SteppableScreen;
import com.troggo.jmp.screens.GameScreen;
import com.troggo.jmp.screens.StartScreen;
import com.troggo.jmp.utils.TouchInput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Jmp extends com.badlogic.gdx.Game {

    public enum Screen {
        START, GAME
    }

    public  static final float WORLD_WIDTH = 20f;               // m
    private static final float WORLD_GRAVITY = 25f;             // m/s^2
    private static final float WORLD_TIME_STEP = 1/300f;        // s
    private static final float MAX_STEP_DELTA = 0.25f;          // s
    private static final float GAME_OVER_SUSPEND_TIME = 0.5f;   // s
    private static final float WALL_OFFSET = 0.25f;             // m
    public  static final float FONT_CAMERA_WIDTH = 400;         // px
    private static final Color BG_COLOR = new Color(0x1c3333ff);

    private Box2DDebugRenderer debugRenderer;

    private Preferences store;
    private World world;
    private final Array<Body> bodies = new Array<>();
    private float worldDelta = 0;   // how far behind the world is from current time
    private float suspendDelta = 0; // how long to suspend the game for
    private boolean suspendTapRequired = false;
    private Runnable suspendCb = null;
    private int highScore;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private OrthographicCamera fontCamera;
    private BitmapFont fontH1;
    private BitmapFont fontH2;
    private BitmapFont fontH3;
    private InputMultiplexer input;
    private Ground ground;
    private Wall wall1;
    private Wall wall2;

    public void create() {
        store = Gdx.app.getPreferences("JMP.STORE");
        highScore = store.getInteger("highScore", 0);

        Box2D.init();
        world = new World(new Vector2(0, -WORLD_GRAVITY), true);
        world.setContactListener(new EntityContactListener());
        batch = new SpriteBatch();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_WIDTH * h / w);
        fontCamera = new OrthographicCamera();
        fontCamera.setToOrtho(false, FONT_CAMERA_WIDTH, FONT_CAMERA_WIDTH * h / w);

        fontH1 = generateFont("04B_30__.TTF", 32);
        fontH2 = generateFont("04B_30__.TTF", 15);
        fontH3 = generateFont("VCR_OSD_MONO_1.001.ttf", 21);

        input = new InputMultiplexer();
        Gdx.input.setInputProcessor(input);
        input.addProcessor(new TouchInput(() -> {
            if (suspendTapRequired && suspendDelta == 0) {
                unsuspend();
                return true;
            }
            return false;
        }));

        debugRenderer = new Box2DDebugRenderer();

        ground = new Ground(this, WORLD_WIDTH);
        wall1 = new Wall(this, -WALL_OFFSET);
        wall2 = new Wall(this, WORLD_WIDTH + WALL_OFFSET);

        setScreen(Screen.START);
    }

    @Override
    public void dispose() {
        batch.dispose();
        fontH1.dispose();
        fontH2.dispose();
        ground.dispose();
        wall1.dispose();
        wall2.dispose();
        super.dispose();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getRawDeltaTime();
        // cap max time we step so we don't overload slow devices
        step(Math.min(delta, MAX_STEP_DELTA));
        render(delta, false);
    }

    private void render(float delta, boolean debug) {
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, BG_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        fontCamera.update();
        batch.setProjectionMatrix(camera.combined);

        world.getBodies(bodies);

        batch.begin();
        // render all entities in world
        for (Body body : bodies) {
            for (Fixture fixture : body.getFixtureList()) {
                Object obj = fixture.getUserData();
                if (obj instanceof Entity) {
                    ((Entity) obj).render(delta);
                }
            }
        }
        super.render();
        batch.end();

        if (debug) {
            // draw box2D bodies
            debugRenderer.render(world, camera.combined);
        }
    }

    private void step(float delta) {
        worldDelta += delta;
        // catch physics world up to current time
        while (worldDelta > WORLD_TIME_STEP) {
            worldDelta -= WORLD_TIME_STEP;

            if (suspended()) {
                wait(WORLD_TIME_STEP);
            } else {
                // step all entities in world
                world.getBodies(bodies);
                for (Body body : bodies) {
                    for (Fixture fixture : body.getFixtureList()) {
                        Object obj = fixture.getUserData();
                        if (obj instanceof Entity) {
                            ((Entity) obj).step(WORLD_TIME_STEP);
                        }
                    }
                }

                if (screen instanceof SteppableScreen) {
                    ((SteppableScreen) screen).step(WORLD_TIME_STEP);
                }

                // we use constant time steps to keep physics consistent
                world.step(WORLD_TIME_STEP, 6, 2);
            }
        }
    }

    private void wait(float delta) {
        if (suspendDelta > delta) {
            suspendDelta -= delta;
        } else if (suspendDelta > 0) {
            if (suspendTapRequired) {
                suspendDelta = 0;
            } else {
                unsuspend();
            }
        }
    }

    private boolean suspended() {
        return suspendTapRequired || suspendDelta > 0;
    }

    private void unsuspend() {
        suspendDelta = 0;
        suspendTapRequired = false;
        if (suspendCb != null) {
            suspendCb.run();
            suspendCb = null;
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_WIDTH * height / width);
        super.resize(width, height);
    }

    public void setScreen(Screen screen) {
        switch (screen) {
            case START: super.setScreen(new StartScreen(this)); return;
            case GAME: super.setScreen(new GameScreen(this, highScore)); return;
            default: throw new IllegalArgumentException("Invalid screen");
        }
    }

    public void suspend(float t, boolean tapRequired, Runnable func) {
        // stop world from updating for t seconds
        // renders will still occur
        if (suspendCb != null) {
            throw new IllegalStateException("Multiple concurrent suspends not implemented.");
        }
        suspendDelta = t;
        suspendTapRequired = tapRequired;
        suspendCb = func;
    }

    public void gameOver(int score) {
        if (score > highScore) {
            highScore = score;
            store.putInteger("highScore", highScore);
            store.flush();
        }
        suspend(GAME_OVER_SUSPEND_TIME, true, () -> {
            if (screen != null) {
                screen.dispose();
            }
            screen = null;
            camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
            setScreen(Screen.GAME);
        });
    }

    public GlyphLayout write(BitmapFont font, CharSequence str, float x, float y, int align) {
        GlyphLayout l = new GlyphLayout(font, str, font.getColor(), FONT_CAMERA_WIDTH, align, true);
        return write(font, l, x, y);
    }

    public GlyphLayout write(BitmapFont font, GlyphLayout layout, float x, float y) {
        // convert co-ords from game camera to font camera
        float scale = FONT_CAMERA_WIDTH / WORLD_WIDTH;
        x *= scale;
        y *= scale;

        batch.setProjectionMatrix(fontCamera.combined);
        font.draw(batch, layout, x, y);
        batch.setProjectionMatrix(camera.combined);
        return layout;
    }

    private BitmapFont generateFont(String file, int size) {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(file));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.color = Color.WHITE;
        param.mono = true;
        param.kerning = false;
        param.size = size;
        BitmapFont font = gen.generateFont(param);
        gen.dispose();
        return font;
    }

    // getters
    public float getPixelWidth() {
        return WORLD_WIDTH / Gdx.graphics.getWidth();
    }

    public World getWorld() {
        return world;
    }

    public Ground getGround() {
        return ground;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFontH1() {
        return fontH1;
    }

    public BitmapFont getFontH2() {
        return fontH2;
    }

    public BitmapFont getFontH3() {
        return fontH3;
    }

    public InputMultiplexer getInput() {
        return input;
    }
}
