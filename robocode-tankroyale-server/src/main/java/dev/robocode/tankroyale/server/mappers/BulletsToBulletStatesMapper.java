package dev.robocode.tankroyale.server.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dev.robocode.tankroyale.server.model.Bullet;
import dev.robocode.tankroyale.schema.BulletState;

public final class BulletsToBulletStatesMapper {

	private BulletsToBulletStatesMapper() {}

	public static List<BulletState> map(Set<Bullet> bullets) {
		List<BulletState> bulletStates = new ArrayList<>();
		for (Bullet bullet : bullets) {
			bulletStates.add(BulletToBulletStateMapper.map(bullet));
		}
		return bulletStates;
	}
}