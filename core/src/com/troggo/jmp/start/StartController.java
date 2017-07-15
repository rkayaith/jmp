package com.troggo.jmp.start;

import com.badlogic.gdx.InputAdapter;

class StartController extends InputAdapter {

    private final Start screen;

    StartController(Start screen) {
        this.screen = screen;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        screen.startGame();
        return true;
    }
}
