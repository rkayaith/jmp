package com.troggo.jmp.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.Shape
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.Dimensions
import com.troggo.jmp.utils.draw
import com.badlogic.gdx.physics.box2d.Body as Box2DBody

interface Entity {
    // begin contact with another entity. both entities have beginContact called.
    fun beginContact(entity: Entity) = Unit
    // end contact with another entity. both entities have endContact called.
    fun endContact(entity: Entity) = Unit
    // frame is being rendered. use this to draw textures.
    fun render() = Unit
    // physics world step. use this to update physics bodies and apply forces.
    fun step() = Unit
}

abstract class Body(
    protected val game: Jmp,
    val texture: Texture = Texture("missing_texture.png"),

    type: BodyDef.BodyType = BodyDef.BodyType.DynamicBody,
    x: Float = 0f,
    y: Float = 0f,
    damping: Float = 0f,
    gravityScale: Float = 1f,

    // fixture attributes
    width: Float? = null,
    height: Float? = null,
    weight: Float = 0f,
    friction: Float = 0f,
    isSensor: Boolean = false,
    shapeType: Shape.Type = Shape.Type.Polygon
) : Entity {

    // maintain texture aspect ratio if only one of width or height is given
    val dimensions = (texture.width.toFloat() / texture.height).let { ar -> Dimensions(
        width = width ?: height?.times(ar) ?: texture.width.toFloat(),
        height = height ?: width?.div(ar) ?: texture.height.toFloat()
    )}

    protected val body: Box2DBody = game.world.createBody(BodyDef().also {
        it.type = type
        it.position.set(x, y)
        it.linearDamping = damping
        it.gravityScale = gravityScale
        it.fixedRotation = true
    })

    open var position: Vector2
        get() = body.position
        set(vec) = body.setTransform(vec, body.angle)

    init {
        Fixture(dimensions.width, dimensions.height, weight, friction, isSensor, shapeType, isBody = true)
    }

    // body is being disposed. use this to dispose textures and other assets.
    open fun dispose() {
        texture.dispose()
        game.world.destroyBody(body)
    }

    override fun render() {
        game.batch.draw(this)
    }

    open inner class Fixture(
        width: Float,
        height: Float,
        weight: Float = 0f,
        friction: Float = 0f,
        isSensor: Boolean = false,
        shapeType: Shape.Type = Shape.Type.Polygon,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        isBody: Boolean = false
    ) : Entity {
        init {
            val shape = when (shapeType) {
                Shape.Type.Polygon -> PolygonShape().apply {
                    setAsBox(width / 2, height / 2, Vector2(offsetX, offsetY), 0f)
                }
                Shape.Type.Chain -> ChainShape().apply {
                    createLoop(arrayOf(
                        Vector2(offsetX + width / 2, offsetY + height / 2),
                        Vector2(offsetX - width / 2, offsetY + height / 2),
                        Vector2(offsetX - width / 2, offsetY - height / 2),
                        Vector2(offsetX + width / 2, offsetY - height / 2)
                    ))
                }
                else -> throw IllegalArgumentException("Shape type not implemented.")
            }

            val fixture = FixtureDef().also {
                it.shape = shape
                it.density = weight / (dimensions.height * dimensions.width)
                it.friction = friction
                it.isSensor = isSensor
            }

            body.createFixture(fixture).also {
                it.userData = if (isBody) this@Body else this
            }

            shape.dispose()
        }
    }
}
