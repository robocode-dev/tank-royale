package net.robocode2.server.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.json_schema.states.BulletState;
import net.robocode2.model.Bullet;
import net.robocode2.model.Turn;

public final class BulletsToBulletStatesMapper {

	public static List<BulletState> map(Turn turn, int botId) {
		Set<Bullet> bullets = turn.getBullets(botId);
		List<BulletState> bulletStates = new ArrayList<BulletState>();
		for (Bullet bullet : bullets) {
			bulletStates.add(BulletToBulletStateMapper.map(bullet));
		}
		return bulletStates;
	}
}