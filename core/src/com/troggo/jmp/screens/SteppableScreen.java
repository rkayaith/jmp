package com.troggo.jmp.screens;

import com.troggo.jmp.utils.BatchQueue;

import com.badlogic.gdx.Screen;

public interface SteppableScreen extends Screen {
    // called just before physics simulation is stepped
    void step(float delta);
}
