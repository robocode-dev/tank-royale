import { TickEventForObserver } from "@/schemas/Events";
import { BotInfo } from "@/schemas/Comm";
import GameSetup from "@/schemas/GameSetup";

export default {
  saveGameSetup(gameSetup: GameSetup | null) {
    sessionStorage.setItem("gameSetup", JSON.stringify(gameSetup));
  },
  loadGameSetup(): GameSetup | null {
    const gameSetup = sessionStorage.getItem("gameSetup");
    if (gameSetup) {
      return JSON.parse(gameSetup);
    }
    return null;
  },
  saveSelectedBots(selectedBots: BotInfo[]) {
    sessionStorage.setItem("selectedBots", JSON.stringify(selectedBots));
  },
  loadSelectedBots(): BotInfo[] {
    const selectedBots = sessionStorage.getItem("selectedBots");
    if (selectedBots) {
      return JSON.parse(selectedBots);
    }
    return [];
  },
};
