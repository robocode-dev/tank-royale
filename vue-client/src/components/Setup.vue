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
                <b-input type="number" v-model="selectedGameType.arenaWidth" :disabled="selectedGameType.isArenaWidthLocked" :min="rules.arenaMinSize"
                  :max="rules.arenaMaxSize" step="100" />
              </b-input-group>
            </b-col>
            <b-col sm="4">
              <b-input-group size="sm">
                <b-input-group-addon>height</b-input-group-addon>
                <b-input type="number" v-model="selectedGameType.arenaHeight" :disabled="selectedGameType.isArenaHeightLocked" :min="rules.arenaMinSize"
                  :max="rules.arenaMaxSize" step="100" />
              </b-input-group>
            </b-col>
          </b-row>

          <b-row class="mt-4">
            <b-col sm="3"><label>Min. number of participants</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="selectedGameType.minNumberOfParticipants" :disabled="selectedGameType.isMinNumberOfParticipantsLocked"
                :min="1" />
            </b-col>
            <b-col sm="3"><label>Max. number of participants</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="selectedGameType.maxNumberOfParticipants" :disabled="selectedGameType.isMaxNumberOfParticipantsLocked"
                :min="1" />
            </b-col>
          </b-row>

          <b-row class="mt-2">
            <b-col sm="3"><label>Number of rounds</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="selectedGameType.numberOfRounds" :disabled="selectedGameType.isNumberOfRoundsLocked" :min="1"/>
            </b-col>
            <b-col sm="3"><label>Inactivity turns</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="selectedGameType.inactivityTurns" :disabled="selectedGameType.isInactivityTurnsLocked" :min="1" step="50" />
            </b-col>
          </b-row>

          <b-row class="mt-2">
            <b-col sm="3"><label>Ready timeout (ms)</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="selectedGameType.delayedObserverTurns" :disabled="selectedGameType.delayedObserverTurnsLocked"
                :min="1" />
            </b-col>
            <b-col sm="3"><label>Turn timeout (ms)</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="selectedGameType.turnTimeout" :disabled="selectedGameType.turnTimeoutLocked" :min="1" />
            </b-col>
          </b-row>

          <b-row class="mt-2">
            <b-col sm="3"><label>Gun cooling rate</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="selectedGameType.gunCoolingRate" :disabled="selectedGameType.isGunCoolingRateLocked" :min="rules.minGunCoolingRate"
                :max="rules.maxGunCoolingRate" step="0.1" />
            </b-col>
            <b-col sm="3"><label>Delayed observer turns</label></b-col>
            <b-col sm="2">
              <b-input size="sm" type="number" v-model="selectedGameType.delayedObserverTurns" :disabled="selectedGameType.delayedObserverTurnsLocked"
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
            <b-col sm="12"><b-button size="lg" variant="secondary" style="width: 100%; text-align: center" @click="onStartGameClicked" :disabled="!isGameStartValid()">Start Game</b-button></b-col>
          </b-row>
        </div> <!-- v=show="isGameTypeSelected" -->
      </div> <!-- v-show="isConnected()"" -->
    </b-container>

  </div>
</template>

<script>
  export default {
    name: 'setup',
    data () {
      return {
        server: 'localhost',
        port: 50000,
        serverUrl: null,
        connection: null,
        connectionStatus: 'not connected',

        serverHandshake: null, // from server

        gameTypeOptions: null,
        selectedGameType: null,

        rules: {
          arenaMinSize: 400,
          arenaMaxSize: 5000,
          minGunCoolingRate: 0.1,
          maxGunCoolingRate: 3.0
        },

        availableBots: [],
        selectedBots: []
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
        var c = this.connection
        return c != null && c.readyState === WebSocket.OPEN
      },
      onConnect () {
        const vm = this

        const connection = new WebSocket(vm.serverUrl)

        vm.connection = connection

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
          this.connection.close()
        }
        this.connection = null
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

        var gameTypeOptions = []

        if (serverHandshake) {
          const games = serverHandshake.games
          if (games) {
            gameTypeOptions.push({ 'value': null, 'text': '-- select --' })
            games.forEach(element => {
              var gameType = element['gameType']
              gameTypeOptions.push({ 'value': gameType, 'text': gameType })
            })
          }
        }
        this.gameTypeOptions = gameTypeOptions
      },
      onBotListUpdate (botListUpdate) {
        console.log('->botListUpdate')

        var bots = botListUpdate.bots
        for (var i = 0; i < bots.length; i++) {
          var bot = bots[i]
          bot.displayText = `${bot.name} ${bot.version} (${bot.host}:${bot.port})`
        }
        this.availableBots = bots
        this.availableBots.sort(this.compareBots)
      },
      isGameTypeSelected () {
        return this.selectedGameType != null
      },
      onGameTypeChanged (event) {
        var foundGame = this.serverHandshake.games.find(selectedGameType => selectedGameType.gameType === event.target.value)
        if (!foundGame) {
          foundGame = null
        }
        this.selectedGameType = foundGame
      },
      onAvailableBotClicked (bot) {
        this.selectedBots.push(bot)
        this.selectedBots.sort(this.compareBots)
        this.removeItem(this.availableBots, bot)
      },
      onSelectedBotClicked (bot) {
        this.availableBots.push(bot)
        this.availableBots.sort(this.compareBots)
        this.removeItem(this.selectedBots, bot)
      },
      onAllAvailableBotsClicked () {
        this.selectedBots = this.selectedBots.concat(this.availableBots)
        this.selectedBots.sort(this.compareBots)
        this.availableBots = []
      },
      onAllSelectedBotsClicked () {
        this.availableBots = this.availableBots.concat(this.selectedBots)
        this.availableBots.sort(this.compareBots)
        this.selectedBots = []
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
        return this.isConnected() &&
          this.isGameTypeSelected() &&
          (this.selectedBots.length >= this.selectedGameType.minNumberOfParticipants) &&
          (this.selectedBots.length <= this.selectedGameType.maxNumberOfParticipants)
      }
    }
  }
</script>

<style lang="scss">
  @import "../styles/_variables.scss";
  @import "../../node_modules/bootstrap/scss/bootstrap.scss";
  @import "../styles/_bootswatch.scss";
</style>