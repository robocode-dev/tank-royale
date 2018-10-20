import { BotInfo } from "@/schemas/Comm";

export default {
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
