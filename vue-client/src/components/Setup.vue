<template>
  <div class="setup">
    <b-container>

      <b-row class="mt-3">
        <b-col>
          <b-input-group size="sm">
            <b-input-group-addon>Server URL</b-input-group-addon>
            <b-input placeholder="ws://server:port" v-model="serverUrl" />
            <b-input-group-button slot="right">
              <b-btn @click="onConnect" v-show="!isConnected()">Connect</b-btn>
              <b-btn variant="warning" @click="onDisconnect" v-show="isConnected()">Disconnect</b-btn>
            </b-input-group-button>
          </b-input-group>
          <label style="width: 100%; text-align: right">Status: {{ connectionStatus }}</label>
        </b-col>
      </b-row>

      <div v-if="isConnected()">
        <b-row class="mt-0">
          <b-col sm="12"><label>Game Type</label></b-col>
          <b-col sm="4"><b-form-select size="sm" :options="gameTypeOptions" @change.native="onGameTypeChanged" /></b-col>
        </b-row>

        <div v-if="isGameTypeSelected()">
          <b-row class="mt-3">
            <b-col sm="12"><label>Arena size</label></b-col>
            <b-col sm="4">
              <b-input-group size="sm">
                <b-input-group-addon>width</b-input-group-addon>
                <b-input type="number" v-model="shared.gameType.arenaWidth" :disabled="shared.gameType.isArenaWidthLocked" :min="rules.arenaMinSize"
                  :max="rules.arenaMaxSize" step="100" />
              </b-input-group>
            </b-col>
            <b-col sm="4">
              <b-input-group size="sm">
                <b-input-group-addon>height</b-input-group-addon>
                <b-input type="number" v-model="shared.gameType.arenaHeight" :disabled="shared.gameType.isArenaHeightLocked" :min="rules.arenaMinSize"
                  :max="rules.arenaMaxSize" step="100" />
              </b-input-group>
            </b-col>
          </b-row>

          <b-row class="mt-4">
            <b-col sm="3"><label>Min. number of participants</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="shared.gameType.minNumberOfParticipants" :disabled="shared.gameType.isMinNumberOfParticipantsLocked"
                :min="1" />
            </b-col>
            <b-col sm="3"><label>Max. number of participants</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="shared.gameType.maxNumberOfParticipants" :disabled="shared.gameType.isMaxNumberOfParticipantsLocked"
                :min="1" />
            </b-col>
          </b-row>

          <b-row class="mt-2">
            <b-col sm="3"><label>Number of rounds</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="shared.gameType.numberOfRounds" :disabled="shared.gameType.isNumberOfRoundsLocked" :min="1"/>
            </b-col>
            <b-col sm="3"><label>Inactivity turns</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="shared.gameType.inactivityTurns" :disabled="shared.gameType.isInactivityTurnsLocked" :min="1" step="50" />
            </b-col>
          </b-row>

          <b-row class="mt-2">
            <b-col sm="3"><label>Ready timeout (ms)</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="shared.gameType.delayedObserverTurns" :disabled="shared.gameType.delayedObserverTurnsLocked"
                :min="1" />
            </b-col>
            <b-col sm="3"><label>Turn timeout (ms)</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="shared.gameType.turnTimeout" :disabled="shared.gameType.turnTimeoutLocked" :min="1" />
            </b-col>
          </b-row>

          <b-row class="mt-2">
            <b-col sm="3"><label>Gun cooling rate</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="shared.gameType.gunCoolingRate" :disabled="shared.gameType.isGunCoolingRateLocked" :min="rules.minGunCoolingRate"
                :max="rules.maxGunCoolingRate" step="0.1" />
            </b-col>
            <b-col sm="3"><label>Delayed observer turns</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="shared.gameType.delayedObserverTurns" :disabled="shared.gameType.delayedObserverTurnsLocked"
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
                <b-list-group-item button v-for="bot in shared.selectedBots" :key="bot.key" @click="onSelectedBotClicked(bot)">{{bot.displayText}}</b-list-group-item>
              </b-list-group>
            </b-card>
          </b-card-group>

          <b-row class="mt-3">
            <b-col sm="12"><b-button size="lg" variant="secondary" style="width: 100%; text-align: center" @click="onStartGameClicked" :disabled="!isGameStartValid()">Start Game</b-button></b-col>
          </b-row>
        </div> <!-- v=show="isGameTypeSelected" -->
      </div> <!-- v-show="isConnected()"" -->
    </b-container>

  </div>
</template>

<script>
  const sharedData = require('./shared-data.js')

  export default {
    name: 'setup',
    data () {
      return {
        shared: sharedData,

        server: 'localhost',
        port: 50000,
        serverUrl: null,
        connectionStatus: 'not connected',

        serverHandshake: null, // from server

        gameTypeOptions: null,

        rules: {
          arenaMinSize: 400,
          arenaMaxSize: 5000,
          minGunCoolingRate: 0.1,
          maxGunCoolingRate: 3.0
        },

        availableBots: []
      }
    },
    mounted () {
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
      isConnected () {
        const c = this.shared.connection
        return c != null && c.readyState === WebSocket.OPEN
      },
      onConnect () {
        const vm = this

        const connection = new WebSocket(vm.serverUrl)

        this.shared.connection = connection

        connection.onopen = function (event) {
          console.log('ws connected to: ' + event.target.url)

          vm.connectionStatus = 'connected'

          vm.sendControllerHandshake(connection)
        }
        connection.onerror = function (event) {
          console.log('ws error: ' + event.data)

          vm.connectionStatus = 'error: ' + event.data
        }
        connection.onclose = function (event) {
          console.log('ws closed: ' + event.target.url)

          vm.connectionStatus = 'not connected'
        }
        connection.onmessage = function (event) {
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
      onDisconnect () {
        if (this.isConnected) {
          this.shared.connection.close()
        }
        this.shared.connection = null
        this.shared.gameType = null
        this.shared.selectedBots = []
        this.gameTypeOptions = null
      },
      sendControllerHandshake (connection) {
        console.log('<-controllerHandshake')

        connection.send(
          JSON.stringify({
            type: 'controllerHandshake',
            name: 'Robocode 2 Game Controller',
            version: '0.1.0',
            author: 'Flemming N. Larsen <fnl@users.sourceforge.net>'
          })
        )
      },
      onServerHandshake (serverHandshake) {
        console.log('->serverHandshake')

        this.serverHandshake = serverHandshake

        const gameTypeOptions = []

        if (serverHandshake) {
          const games = serverHandshake.games
          if (games) {
            gameTypeOptions.push({ 'value': null, 'text': '-- select --' })
            games.forEach(element => {
              const gameType = element['gameType']
              gameTypeOptions.push({ 'value': gameType, 'text': gameType })
            })
          }
        }
        this.gameTypeOptions = gameTypeOptions
      },
      onBotListUpdate (botListUpdate) {
        console.log('->botListUpdate')

        const bots = botListUpdate.bots
        for (var i = 0; i < bots.length; i++) {
          const bot = bots[i]
          bot.displayText = `${bot.name} ${bot.version} (${bot.host}:${bot.port})`
        }
        this.availableBots = bots
        this.availableBots.sort(this.compareBots)
      },
      isGameTypeSelected () {
        return this.shared.gameType != null
      },
      onGameTypeChanged (event) {
        var foundGame = this.serverHandshake.games.find(it => it.gameType === event.target.value)
        if (!foundGame) {
          foundGame = null
        }
        this.shared.gameType = foundGame
      },
      onAvailableBotClicked (bot) {
        this.shared.selectedBots.push(bot).sort(this.compareBots)
        this.removeItem(this.availableBots, bot)
      },
      onSelectedBotClicked (bot) {
        this.availableBots.push(bot)
        this.availableBots.sort(this.compareBots)
        this.removeItem(this.shared.selectedBots, bot)
      },
      onAllAvailableBotsClicked () {
        this.shared.selectedBots = this.shared.selectedBots.concat(this.availableBots).sort(this.compareBots)
        this.availableBots = []
      },
      onAllSelectedBotsClicked () {
        this.availableBots = this.availableBots.concat(this.shared.selectedBots)
        this.availableBots.sort(this.compareBots)
        this.shared.selectedBots = []
      },
      removeItem (array, item) {
        for (var i = 0; i < array.length; i++) {
          if (array[i] === item) {
            array.splice(i, 1)
            return
          }
        }
      },
      compareBots (a, b) {
        if (a.displayText < b.displayText) return -1
        if (a.displayText > b.displayText) return 1
        return 0
      },
      onStartGameClicked () {
        console.log('Start Game')
        this.$router.push('/arena')
      },
      isGameStartValid () {
        const selectedBotsCount = this.shared.selectedBots.length
        const gameType = this.shared.gameType
        return this.isConnected() &&
          this.isGameTypeSelected() &&
          (selectedBotsCount >= gameType.minNumberOfParticipants) &&
          (selectedBotsCount <= gameType.maxNumberOfParticipants)
      }
    }
  }
</script>

<style lang="scss">
  @import "../styles/_variables.scss";
  @import "../../node_modules/bootstrap/scss/bootstrap.scss";
  @import "../styles/_bootswatch.scss";
</style>