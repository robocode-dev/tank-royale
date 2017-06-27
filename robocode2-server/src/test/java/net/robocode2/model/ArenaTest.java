package net.robocode2.model;

import static org.junit.Assert.assertNull;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import org.junit.Test;

public class ArenaTest {

	@Test
	public void getSize() {
		assertNull(new Arena(null).getSize());

		Arena arena = new Arena(new Size(10, 20));
		assertReflectionEquals(new Size(10, 20), arena.getSize());

		arena = new Arena(new Size(77, 99));
		assertReflectionEquals(new Size(77, 99), arena.getSize());
	}
}
