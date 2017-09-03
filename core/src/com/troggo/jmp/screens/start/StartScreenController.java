package com.troggo.jmp.screens.start;

import com.badlogic.gdx.InputAdapter;

class StartScreenController extends InputAdapter {

    private final StartScreen screen;

    StartScreenController(StartScreen _screen) {
        screen = _screen;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        screen.startGame();
        return true;
    }
}
