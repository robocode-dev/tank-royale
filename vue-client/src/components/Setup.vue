<template>
  <div class="setup">
    <b-container>

      <b-row class="mt-3">
        <b-col>
          <b-input-group size="sm" prepend="Server URL">
            <b-input placeholder="ws://server:port" v-model="serverUrl" />
            <b-input-group-append>
              <b-btn @click="onConnect" v-show="!isConnected">Connect</b-btn>
              <b-btn @click="onDisconnect" v-show="isConnected" variant="warning">Disconnect</b-btn>
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
              <b-button size="lg" variant="secondary" style="width: 100%; text-align: center" @click="onStartGameClicked"
                :disabled="!isGameStartValid()">Start Game</b-button>
            </b-col>
          </b-row>
        </div> <!-- v=show="isGameTypeSelected" -->
      </div> <!-- v-show="isConnected" -->
    </b-container>

  </div>
</template>

<script>
  import state from '../store/store.js'
  import ReconnectingWebSocket from 'reconnectingwebsocket'

  export default {
    name: 'setup',
    data() {
      return {
        serverUrl: null,
        clientKey: null,

        gameSetup: null,
        selectedBots: [],

        server: 'localhost',
        port: 50000,

        socket: null,
        connectionStatus: 'not connected',
        isConnected: false,

        serverHandshake: null, // from server

        gameTypeOptions: [],

        rules: {
          arenaMinSize: 400,
          arenaMaxSize: 5000,
          minGunCoolingRate: 0.1,
          maxGunCoolingRate: 3.0
        },

        availableBots: []
      }
    },
    mounted() {
      const server = this.$route.query.server
      if (server) {
        this.server = server
      }
      const port = this.$route.query.port
      if (port) {
        this.port = port
      }
      this.serverUrl = 'ws://' + this.server + ':' + this.port
    },
    methods: {
      onConnect() {
        var socket = this.socket
        if (socket) {
          socket.open()
          return
        }

        // Store the server URL
        state.setServerUrl(this.serverUrl)

        socket = new ReconnectingWebSocket(this.serverUrl)
        this.socket = socket

        const vm = this

        socket.onopen = function (event) {
          console.log('ws connected to: ' + event.target.url)

          vm.isConnected = true
          vm.connectionStatus = 'connected'
        }
        socket.onclose = function (event) {
          console.log('ws closed: ' + event.target.url)

          vm.isConnected = false
          vm.connectionStatus = 'not connected'
        }
        socket.onerror = function (event) {
          console.log('ws error: ' + event.data)

          vm.connectionStatus = 'error: ' + event.data
        }
        socket.onmessage = function (event) {
          console.log('ws message: ' + event.data)

          const message = JSON.parse(event.data)

          switch (message.type) {
            case 'serverHandshake':
              vm.onServerHandshake(message)
              break
            case 'botListUpdate':
              vm.onBotListUpdate(message)
              break
          }
        }
      },
      onDisconnect() {
        this.socket.close()

        this.gameSetup = null
        this.selectedBots = []
        this.gameTypeOptions = []
      },
      sendControllerHandshake() {
        console.log('<-controllerHandshake')

        this.socket.send(JSON.stringify(
          {
            clientKey: this.clientKey,
            type: 'controllerHandshake',
            name: 'Robocode 2 Game Controller',
            version: '0.1.0',
            author: 'Flemming N. Larsen <fnl@users.sourceforge.net>'
          }
        ))
      },
      onServerHandshake(serverHandshake) {
        console.log('->serverHandshake')

        this.serverHandshake = serverHandshake

        const gameTypeOptions = []

        if (serverHandshake) {
          this.clientKey = serverHandshake.clientKey

          this.sendControllerHandshake()

          const games = serverHandshake.games
          if (games) {
            gameTypeOptions.push({ 'value': null, 'text': '-- select --' })
            games.forEach(element => {
              const gameType = element.gameType
              gameTypeOptions.push({ 'value': gameType, 'text': gameType })
            })
          }
        }
        this.gameTypeOptions = gameTypeOptions
      },
      onBotListUpdate(botListUpdate) {
        console.log('->botListUpdate')

        const bots = botListUpdate.bots
        for (var i = 0; i < bots.length; i++) {
          const bot = bots[i]
          bot.displayText = `${bot.name} ${bot.version} (${bot.host}:${bot.port})`
        }
        this.availableBots = bots
        this.availableBots.sort(this.compareBots)
      },
      isGameTypeSelected() {
        return this.gameSetup != null
      },
      onGameTypeChanged(event) {
        var foundGameSetup = this.serverHandshake.games.find(gameSetup => gameSetup.gameType === event.target.value)
        if (!foundGameSetup) {
          foundGameSetup = null
        }
        this.gameSetup = foundGameSetup
      },
      onAvailableBotClicked(bot) {
        this.selectedBots.push(bot)
        this.selectedBots.sort(this.compareBots)
        this.removeItem(this.availableBots, bot)
      },
      onSelectedBotClicked(bot) {
        this.availableBots.push(bot)
        this.availableBots.sort(this.compareBots)
        this.removeItem(this.selectedBots, bot)
      },
      onAllAvailableBotsClicked() {
        this.selectedBots = this.selectedBots.concat(this.availableBots).sort(this.compareBots)
        this.availableBots = []
      },
      onAllSelectedBotsClicked() {
        this.availableBots = this.availableBots.concat(this.selectedBots)
        this.availableBots.sort(this.compareBots)
        this.selectedBots = []
      },
      removeItem(array, item) {
        for (var i = 0; i < array.length; i++) {
          if (array[i] === item) {
            array.splice(i, 1)
            return
          }
        }
      },
      compareBots(a, b) {
        if (a.displayText < b.displayText) return -1
        if (a.displayText > b.displayText) return 1
        return 0
      },
      isGameStartValid() {
        const selectedBotsCount = this.selectedBots.length
        const gameSetup = this.gameSetup
        return this.isConnected &&
          this.isGameTypeSelected() &&
          (selectedBotsCount >= gameSetup.minNumberOfParticipants) &&
          ((selectedBotsCount <= gameSetup.maxNumberOfParticipants) || gameSetup.maxNumberOfParticipants == null)
      },
      onStartGameClicked() {
        console.log('Goto arena')

        state.setGameSetup(this.gameSetup)
        state.setSelectedBots(this.selectedBots)

        this.$router.push('/arena')
      }
    }
  }
</script>

<style lang="scss">
  @import "../styles/_variables.scss";
  @import "../../node_modules/bootstrap/scss/bootstrap.scss";
  @import "../styles/_bootswatch.scss";
</style>