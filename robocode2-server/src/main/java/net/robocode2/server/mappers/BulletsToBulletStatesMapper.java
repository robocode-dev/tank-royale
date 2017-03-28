package net.robocode2.server.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.json_schema.states.BulletState;
import net.robocode2.model.ImmutableBullet;

public final class BulletsToBulletStatesMapper {

	public static List<BulletState> map(Set<ImmutableBullet> bullets) {
		List<BulletState> bulletStates = new ArrayList<BulletState>();
		for (ImmutableBullet bullet : bullets) {
			bulletStates.add(BulletToBulletStateMapper.map(bullet));
		}
		return bulletStates;
	}
}