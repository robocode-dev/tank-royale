package dev.robocode.tankroyale.rumble

object JournalFixtures {
    fun participant(name: String, rank: Int, score: Int) = JournalParticipant(
        name = name, version = "1.0", sourceHash = "sha256:$name", rank = rank, totalScore = score, survival = 100, lastSurvivorBonus = 10,
        bulletDamage = 200, bulletKillBonus = 20, ramDamage = 5, ramKillBonus = 0, firstPlaces = 3, secondPlaces = 2, thirdPlaces = 0,
    )

    fun record(id: String, behaviorVersion: Int = 7, winner: Int = 4800) = RankedRecord(
        battleId = id, gameType = "1v1", timestamp = "2026-11-13T10:00:00Z",
        client = JournalClient("client-01", "1.4.0"), engine = JournalEngine(behaviorVersion, "1.4.0", "1.4.0"),
        botsRepoCommit = "08940d5", rounds = 5, participants = listOf(participant("Raven", 1, winner), participant("Corners", 2, 2100)),
    )
}
