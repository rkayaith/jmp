package com.troggo.jmp.screens;

import com.badlogic.gdx.Screen;

public interface SteppableScreen extends Screen {
    // called just before physics simulation is stepped
    void step(float delta);
}
