<template>
  <div class="controller">
    <b-container>
      <b-row class="mt-3">
        <b-col>
          <b-input-group>
            <b-input-group-addon>Server URL</b-input-group-addon>
            <b-input placeholder="ws://server:port" v-model="serverUrl"/>
            <b-input-group-button slot="right">
              <b-btn @click="connect" v-show="!isConnected()">Connect</b-btn>
              <b-btn variant="warning" @click="disconnect" v-show="isConnected()">Disconnect</b-btn>
            </b-input-group-button>
          </b-input-group>
          <label>Status: {{ connectionStatus }}</label>
        </b-col>
      </b-row>

      <b-row class="mt-3" v-show="isConnected()">
        <b-col>
          <b-dd text="Game Types">
            <b-dd-item @click="onGameTypeSelected(gameType)" v-for="gameType in gameTypes" :key="gameType">{{ gameType }}</b-dd-item>
          </b-dd>
        </b-col>
      </b-row>

      <b-row class="mt-3" v-show="isConnected()">
        <b-col sm="2">
          <label>Arena size</label>
        </b-col>
        <b-col sm="4">
          <b-input-group>
            <b-input-group-addon>width</b-input-group-addon>
            <b-input type="number" v-model="game.arenaWidth" :disabled="game.isArenaWidthLocked" :min="rules.arenaMinSize" :max="rules.arenaMaxSize" step="100"/>
          </b-input-group>
        </b-col>
        <b-col sm="4">
          <b-input-group>
            <b-input-group-addon>height</b-input-group-addon>
            <b-input type="number" v-model="game.arenaHeight" :disabled="game.isArenaHeightLocked" :min="rules.arenaMinSize" :max="rules.arenaMaxSize" step="100"/>
          </b-input-group>
        </b-col>
      </b-row>

      <b-row class="mt-4" v-show="isConnected()">
        <b-col sm="3">
          <label>Min. number of participants</label>
        </b-col>
        <b-col sm="2">
          <b-input type="number" v-model="game.minNumberOfParticipants" :disabled="game.isMinNumberOfParticipantsLocked" :min="1"/>
        </b-col>
        <b-col sm="3">
          <label>Max. number of participants</label>
        </b-col>
        <b-col sm="2">
          <b-input type="number" v-model="game.maxNumberOfParticipants" :disabled="game.isMaxNumberOfParticipantsLocked" :min="1"/>
        </b-col>
      </b-row>

      <b-row class="mt-2" v-show="isConnected()">
       <b-col sm="3">
          <label>Number of rounds</label>
        </b-col>
        <b-col sm="2">
          <b-input type="number" v-model="game.numberOfRounds" :disabled="game.isNumberOfRoundsLocked" :min="1"/>
        </b-col>
       <b-col sm="3">
          <label>Inactivity turns</label>
        </b-col>
        <b-col sm="2">
          <b-input type="number" v-model="game.inactivityTurns" :disabled="game.isInactivityTurnsLocked" :min="1" step="50"/>
        </b-col>
      </b-row>

      <b-row class="mt-2" v-show="isConnected()">
       <b-col sm="3">
          <label>Ready timeout (ms)</label>
        </b-col>
        <b-col sm="2">
          <b-input type="number" v-model="game.delayedObserverTurns" :disabled="game.delayedObserverTurnsLocked" :min="1"/>
        </b-col>
       <b-col sm="3">
          <label>Turn timeout (ms)</label>
        </b-col>
        <b-col sm="2">
          <b-input type="number" v-model="game.turnTimeout" :disabled="game.turnTimeoutLocked" :min="1"/>
        </b-col>
      </b-row>

      <b-row class="mt-2" v-show="isConnected()">
       <b-col sm="3">
          <label>Gun cooling rate</label>
        </b-col>
        <b-col sm="2">
          <b-input type="number" v-model="game.gunCoolingRate" :disabled="game.isGunCoolingRateLocked" :min="rules.minGunCoolingRate" :max="rules.maxGunCoolingRate" step="0.1"/>
        </b-col>
       <b-col sm="3">
          <label>Delayed observer turns</label>
        </b-col>
        <b-col sm="2">
          <b-input type="number" v-model="game.delayedObserverTurns" :disabled="game.delayedObserverTurnsLocked" :min="1"/>
        </b-col>
      </b-row>
    </b-container>

  </div>
</template>

<script>
export default {
  name: 'game-controller',
  data () {
    return {
      server: 'localhost',
      port: 50000,
      serverUrl: null,
      connection: null,
      connectionStatus: 'not connected',

      serverHandshake: null, // from server
      gameTypes: null,

      game: {}, // selected game

      rules: {
        arenaMinSize: 400,
        arenaMaxSize: 5000,
        minGunCoolingRate: 0.1,
        maxGunCoolingRate: 3.0
      }
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
    connect () {
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
            vm.handleServerHandshake(message)
            break
        }
      }
    },
    disconnect () {
      if (this.isConnected) {
        this.connection.close()
      }
      this.connection = null
      this.gameTypes = null
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
    handleServerHandshake (serverHandshake) {
      console.log('->serverHandshake')

      this.serverHandshake = serverHandshake

      var gameTypes = []

      if (serverHandshake) {
        const games = serverHandshake.games
        if (games) {
          games.forEach(element => {
            gameTypes.push(element['gameType'])
          })
        }
      }
      this.gameTypes = gameTypes
    },
    onGameTypeSelected (gameType) {
      var game = this.serverHandshake.games.find(game => game.gameType === gameType)
      if (!game) {
        game = {}
      }
      this.game = game
    }
  }
}
</script>

<style lang="scss">
@import "../styles/_variables.scss";
@import "../../node_modules/bootstrap/scss/bootstrap.scss";
@import "../styles/_bootswatch.scss";
</style>