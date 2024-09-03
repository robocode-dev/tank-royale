package model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import model.factory.BulletFactory

class BulletTest : StringSpec({

    "toMutableBullet() should return a MutableBullet that is a copy of the Bullet instance" {
        val bullet = BulletFactory.createBullet()
        val mutableBullet = bullet.toMutableBullet()

        mutableBullet.id shouldBe bullet.id
        mutableBullet.botId shouldBe bullet.botId
        mutableBullet.power shouldBe bullet.power
        mutableBullet.direction shouldBe bullet.direction
        mutableBullet.color shouldBe bullet.color
        mutableBullet.startPosition shouldBe bullet.startPosition
        mutableBullet.tick shouldBe bullet.tick
    }

    "hashCode() must return the bullet id value" {
        val bullet = BulletFactory.createBullet()

        bullet.hashCode() shouldBe bullet.id.value
    }
/*
    "equals() must return true if the bullet is an IBullet instance and share same id" {
        val bullet = BulletFactory.createBullet()

        // IBullet with same bullet id

        val other = mockk<IBullet>()
        every { other.id } returns bullet.id // same bullet id

        bullet shouldBe other

        // Same IBullet, but with another bullet id

        val another = mockk<IBullet>()
        every { another.id } returns BulletId(bullet.id.value + 1) // different bullet id

        bullet shouldNotBe another

        // Alien type with same bullet id

        val alienType = object {
            var id = bullet.id // same bullet id
        }

        bullet shouldNotBe alienType
    }*/
})
