export default {
  saveServerUrl(serverUrl) {
    sessionStorage.setItem('serverUrl', serverUrl);
  },
  loadServerUrl() {
    return sessionStorage.getItem('serverUrl');
  },
  saveGameSetup(gameSetup) {
    sessionStorage.setItem('gameSetup', JSON.stringify(gameSetup));
  },
  loadGameSetup() {
    return JSON.parse(sessionStorage.getItem('gameSetup'));
  },
  saveSelectedBots(selectedBots) {
    sessionStorage.setItem('selectedBots', JSON.stringify(selectedBots));
  },
  loadSelectedBots() {
    return JSON.parse(sessionStorage.getItem('selectedBots'));
  },
  saveloadIsRunning(loadIsRunning) {
    sessionStorage.setItem('isRunning', loadIsRunning);
  },
  loadIsRunning() {
    var value = sessionStorage.getItem('isRunning');
    return value == null ? false : value === 'true';
  },
  saveloadIsPaused(loadIsPaused) {
    sessionStorage.setItem('isPaused', loadIsPaused);
  },
  loadIsPaused() {
    var value = sessionStorage.getItem('isPaused');
    return value == null ? false : value === 'true';
  },
  saveTickEvent(tickEvent) {
    sessionStorage.setItem('tickEvent', JSON.stringify(tickEvent));
  },
  loadTickEvent() {
    return JSON.parse(sessionStorage.getItem('tickEvent'));
  }
};
