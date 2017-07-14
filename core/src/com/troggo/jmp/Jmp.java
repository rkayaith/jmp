package com.troggo.jmp;

import com.badlogic.gdx.Gdx;
import com.troggo.jmp.start.Start;
import com.troggo.jmp.game.Game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.InputMultiplexer;

public class Jmp extends com.badlogic.gdx.Game {

    public enum Screen {
        START, GAME
    }

    public SpriteBatch batch;
    public BitmapFont font;
    public InputMultiplexer input;

    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();    // Arial
        input = new InputMultiplexer();
        Gdx.input.setInputProcessor(input);

        setScreen(Screen.START);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        super.dispose();
    }

    public boolean setScreen(Screen screen) {
        switch (screen) {
            case START: super.setScreen(new Start(this)); return true;
            case GAME: super.setScreen(new Game(this)); return true;
            default: return false;
        }
    }
}

