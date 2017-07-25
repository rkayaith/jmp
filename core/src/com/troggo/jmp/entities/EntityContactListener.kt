package com.troggo.jmp.entities

import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold

class EntityContactListener : ContactListener {
    override fun beginContact(contact: Contact) = onContact(contact, true)
    override fun endContact(contact: Contact) = onContact(contact, false)
    override fun preSolve(contact: Contact, manifold: Manifold) = Unit
    override fun postSolve(contact: Contact, impulse: ContactImpulse) = Unit

    private fun onContact(contact: Contact, begin: Boolean) {
        val objA = contact.fixtureA.body.userData
        val objB = contact.fixtureB.body.userData

        if (objA is Entity && objB is Entity) {
            if (begin) {
                objA.beginContact(objB)
                objB.beginContact(objA)
            } else {
                objA.endContact(objB)
                objB.endContact(objA)
            }
        }
    }
}

