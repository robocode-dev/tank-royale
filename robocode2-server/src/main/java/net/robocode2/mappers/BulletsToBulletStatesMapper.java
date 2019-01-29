package net.robocode2.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.schema.events.BulletState;
import net.robocode2.model.Bullet;

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