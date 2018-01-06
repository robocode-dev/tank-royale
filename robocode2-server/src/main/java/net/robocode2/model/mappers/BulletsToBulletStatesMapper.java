package net.robocode2.model.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.json_schema.states.BulletState;
import net.robocode2.model.Bullet;

public final class BulletsToBulletStatesMapper {

	public static List<BulletState> map(Set<Bullet> bullets) {
		List<BulletState> bulletStates = new ArrayList<BulletState>();
		for (Bullet bullet : bullets) {
			bulletStates.add(BulletToBulletStateMapper.map(bullet));
		}
		return bulletStates;
	}
}