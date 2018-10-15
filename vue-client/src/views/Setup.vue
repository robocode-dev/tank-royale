<template>
  <div class="setup">
    <b-container>

      <b-row class="mt-3">
        <b-col>
          <b-input-group size="sm" prepend="Server URL">
            <b-input placeholder="ws://server:port" v-model="serverUrl" />
            <b-input-group-append>
              <b-btn @click="onConnect" v-show="!isConnected">Connect</b-btn>
              <b-btn @click="onDisconnect" v-show="isConnected" letiant="warning">Disconnect</b-btn>
            </b-input-group-append>
          </b-input-group>
          <label style="width: 100%; text-align: right">Status: {{ connectionStatus }}</label>
        </b-col>
      </b-row>

      <div v-if="isConnected">
        <b-row class="mt-0">
          <b-col sm="12"><label>Game Type</label></b-col>
          <b-col sm="4">
            <b-form-select size="sm" :options="gameTypeOptions" @change.native="onGameTypeChanged" />
          </b-col>
        </b-row>

        <div v-if="isGameTypeSelected()">
          <b-row class="mt-3">
            <b-col sm="12"><label>Arena size</label></b-col>
            <b-col sm="4">
              <b-input-group size="sm" prepend="width">
                <b-input type="number" v-model="gameSetup.arenaWidth" :disabled="gameSetup.isArenaWidthLocked" :min="rules.arenaMinSize"
                  :max="rules.arenaMaxSize" step="100" />
              </b-input-group>
            </b-col>
            <b-col sm="4">
              <b-input-group size="sm" prepend="height">
                <b-input type="number" v-model="gameSetup.arenaHeight" :disabled="gameSetup.isArenaHeightLocked" :min="rules.arenaMinSize"
                  :max="rules.arenaMaxSize" step="100" />
              </b-input-group>
            </b-col>
          </b-row>

          <b-row class="mt-4">
            <b-col sm="3"><label>Min. number of participants</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="gameSetup.minNumberOfParticipants" :disabled="gameSetup.isMinNumberOfParticipantsLocked"
                :min="1" />
            </b-col>
            <b-col sm="3"><label>Max. number of participants</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="gameSetup.maxNumberOfParticipants" :disabled="gameSetup.isMaxNumberOfParticipantsLocked"
                :min="1" />
            </b-col>
          </b-row>

          <b-row class="mt-2">
            <b-col sm="3"><label>Number of rounds</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="gameSetup.numberOfRounds" :disabled="gameSetup.isNumberOfRoundsLocked"
                :min="1" />
            </b-col>
            <b-col sm="3"><label>Inactivity turns</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="gameSetup.inactivityTurns" :disabled="gameSetup.isInactivityTurnsLocked"
                :min="1" step="50" />
            </b-col>
          </b-row>

          <b-row class="mt-2">
            <b-col sm="3"><label>Ready timeout (ms)</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="gameSetup.delayedObserverTurns" :disabled="gameSetup.delayedObserverTurnsLocked"
                :min="1" />
            </b-col>
            <b-col sm="3"><label>Turn timeout (ms)</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="gameSetup.turnTimeout" :disabled="gameSetup.turnTimeoutLocked"
                :min="1" />
            </b-col>
          </b-row>

          <b-row class="mt-2">
            <b-col sm="3"><label>Gun cooling rate</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="gameSetup.gunCoolingRate" :disabled="gameSetup.isGunCoolingRateLocked"
                :min="rules.minGunCoolingRate" :max="rules.maxGunCoolingRate" step="0.1" />
            </b-col>
            <b-col sm="3"><label>Delayed observer turns</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="gameSetup.delayedObserverTurns" :disabled="gameSetup.delayedObserverTurnsLocked"
                :min="1" />
            </b-col>
          </b-row>

          <b-card-group deck class="mt-4">
            <b-card header="Available bots">
              <b-button size="sm" style="width: 100%" @click="onAllAvailableBotsClicked">&gt;&gt;</b-button>
              <b-list-group class="bot-list">
                <b-list-group-item button v-for="bot in availableBots" :key="bot.key" @click="onAvailableBotClicked(bot)">{{bot.displayText}}</b-list-group-item>
              </b-list-group>
            </b-card>

            <b-card header="Selected bots">
              <b-button size="sm" style="width: 100%" @click="onAllSelectedBotsClicked">&lt;&lt;</b-button>
              <b-list-group class="bot-list">
                <b-list-group-item button v-for="bot in selectedBots" :key="bot.key" @click="onSelectedBotClicked(bot)">{{bot.displayText}}</b-list-group-item>
              </b-list-group>
            </b-card>
          </b-card-group>

          <b-row class="mt-3">
            <b-col sm="12">
              <b-button size="lg" letiant="secondary" style="width: 100%; text-align: center" @click="onStartGameClicked"
                :disabled="!isGameStartValid()">Start Game</b-button>
            </b-col>
          </b-row>
        </div> <!-- v=show="isGameTypeSelected" -->
      </div> <!-- v-show="isConnected" -->
    </b-container>

  </div>
</template>

<script src="./Setup.ts"></script>

<style lang="scss">
  @import "../styles/_variables.scss";
  @import "../../node_modules/bootstrap/scss/bootstrap.scss";
  @import "../styles/_bootswatch.scss";
</style>