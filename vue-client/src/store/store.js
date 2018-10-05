export default {
  setServerUrl(serverUrl) {
    sessionStorage.setItem("serverUrl", serverUrl);
  },
  getServerUrl() {
    return sessionStorage.getItem("serverUrl");
  },
  setGameSetup(gameSetup) {
    sessionStorage.setItem("gameSetup", JSON.stringify(gameSetup));
  },
  getGameSetup() {
    return JSON.parse(sessionStorage.getItem("gameSetup"));
  },
  setSelectedBots(selectedBots) {
    sessionStorage.setItem("selectedBots", JSON.stringify(selectedBots));
  },
  getSelectedBots() {
    return JSON.parse(sessionStorage.getItem("selectedBots"));
  },
  setRunning(isRunning) {
    sessionStorage.setItem("isRunning", isRunning);
  },
  isRunning() {
    var value = sessionStorage.getItem("isRunning");
    return value == null ? false : value === "true";
  },
  setPaused(isPaused) {
    sessionStorage.setItem("isPaused", isPaused);
  },
  isPaused() {
    var value = sessionStorage.getItem("isPaused");
    return value == null ? false : value === "true";
  }
};
