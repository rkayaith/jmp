package com.troggo.jmp.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.troggo.jmp.Jmp

abstract class Entity(
    protected val game: Jmp,
    val texture: Texture = Texture("missing_texture.png"),
    width: Float? = null,
    height: Float? = null,
    weight: Float = 0f,
    friction: Float = 0f,
    damping: Float = 0f,
    type: BodyDef.BodyType = BodyDef.BodyType.DynamicBody,
    x: Float = 0f,
    y: Float = 0f
) {

    // maintain texture aspect ratio if only one of width or height is given
    val dimensions = (texture.width.toFloat() / texture.height).let { ar -> Dimensions(
        width = width ?: height?.times(ar) ?: texture.width.toFloat(),
        height = height ?: width?.div(ar) ?: texture.height.toFloat()
    )}

    val body: Body

    init {
        val bodyDef = BodyDef().apply {
            this.type = type
            position.set(x, y)
            linearDamping = damping
            fixedRotation = true
        }

        val box = PolygonShape().apply {
            setAsBox(dimensions.width / 2, dimensions.height / 2)
        }

        val fixture = FixtureDef().apply {
            shape = box
            density = weight / (dimensions.height * dimensions.width)
            this.friction = friction
        }

        body = game.world.createBody(bodyDef).apply {
            createFixture(fixture)
            userData = this@Entity
        }

        box.dispose()
    }

    // entity is being disposed. use this to dispose textures and other assets.
    open fun dispose() {
        texture.dispose()
        game.world.destroyBody(body)
    }

    // frame is being rendered. use this to draw textures.
    open fun render() {
        game.batch.draw(this@Entity)
    }

    // physics world step. use this to update physics bodies and apply forces.
    open fun step() { }

    // begin contact with another entity. both entities have beginContact called.
    open fun beginContact(entity: Entity) { }

    // end contact with another entity. both entities have endContact called.
    open fun endContact(entity: Entity) { }
}

// extensions
data class Dimensions(val width: Float, val height: Float)

fun Batch.draw(entity: Entity, flipX: Boolean = false, flipY: Boolean = false) = with (entity) {
    // render texture at body's position, scaled to its size
    val (x, y) = with (body.position) { Pair(x, y) }
    val (w, h) = dimensions
    draw(texture, x - w / 2, y - h / 2, w, h, 0, 0, texture.width, texture.height, flipX, flipY)
}
