package com.troggo.jmp.utils

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.troggo.jmp.Jmp.FONT_CAMERA_WIDTH

fun GlyphLayout.getHeight(cam: Camera) = height * cam.viewportWidth / FONT_CAMERA_WIDTH
fun GlyphLayout.getWidth(cam: Camera) = width * cam.viewportWidth / FONT_CAMERA_WIDTH
