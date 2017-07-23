package com.troggo.jmp.entities

interface Entity {
    fun render() {}                     // frame is being rendered. use this to draw textures.
    fun step() {}                       // physics world step. use this to update physics bodies and apply forces.
    fun dispose() {}                    // scene is being disposed. use this to dispose textures and other assets.
    fun beginContact(entity: Entity) {} // begin contact with another entity. both entities have beginContact called.
    fun endContact(entity: Entity) {}   // end contact with another entity. both entities have beginContact called.
}
