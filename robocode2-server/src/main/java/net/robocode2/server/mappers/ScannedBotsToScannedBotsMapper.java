package net.robocode2.server.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.model.ScannedBot;

public class ScannedBotsToScannedBotsMapper {

	public static List<net.robocode2.json_schema.ScannedBot> map(Set<ScannedBot> scannedBots) {
		List<net.robocode2.json_schema.ScannedBot> schemaScannedBots = new ArrayList<>();
		for (ScannedBot scannedBot : scannedBots) {
			schemaScannedBots.add(map(scannedBot));
		}
		return schemaScannedBots;
	}

	private static net.robocode2.json_schema.ScannedBot map(ScannedBot scannedBot) {
		net.robocode2.json_schema.ScannedBot schemaScannedBot = new net.robocode2.json_schema.ScannedBot();
		schemaScannedBot.setId(scannedBot.getId());
		schemaScannedBot.setEnergy(scannedBot.getEnergy());
		schemaScannedBot.setPosition(TypesMapper.map(scannedBot.getPosition()));
		schemaScannedBot.setDirection(scannedBot.getDirection());
		schemaScannedBot.setSpeed(scannedBot.getSpeed());
		return schemaScannedBot;
	}
}