import { TickEventForObserver } from "@/schemas/Events";
import { BotInfo } from "@/schemas/Comm";
import GameSetup from "@/schemas/GameSetup";

export default {
  saveServerUrl(serverUrl: string) {
    sessionStorage.setItem("serverUrl", serverUrl);
  },
  loadServerUrl(): string | null {
    return sessionStorage.getItem("serverUrl");
  },
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
  saveIsRunning(isRunning: boolean) {
    sessionStorage.setItem("isRunning", "" + isRunning);
  },
  loadIsRunning(): boolean {
    const isRunning = sessionStorage.getItem("isRunning");
    return isRunning == null ? false : isRunning === "true";
  },
  saveIsPaused(isPaused: boolean) {
    sessionStorage.setItem("isPaused", "" + isPaused);
  },
  loadIsPaused(): boolean {
    const isPaused = sessionStorage.getItem("isPaused");
    return isPaused == null ? false : isPaused === "true";
  },
  saveTickEvent(event: TickEventForObserver | null) {
    sessionStorage.setItem("tickEvent", JSON.stringify(event));
  },
  loadTickEvent(): TickEventForObserver | null {
    const event = sessionStorage.getItem("tickEvent");
    if (event) {
      return JSON.parse(event);
    }
    return null;
  },
};
