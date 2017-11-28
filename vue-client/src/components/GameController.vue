<template>
  <div class="controller">
    <b-container>
      <b-row class="mt-3">
        <b-input-group>
          <b-input-group-addon>Server URL</b-input-group-addon>
          <b-form-input placeholder="ws://server:port" v-model="serverUrl"/>
          <b-input-group-button slot="right">
            <b-btn @click="connect" v-show="!isConnected()">Connect</b-btn>
            <b-btn variant="danger" @click="disconnect" v-show="isConnected()">Disconnect</b-btn>
          </b-input-group-button>
        </b-input-group>
        <label>Status: {{ connectionStatus }}</label>
      </b-row>

      <b-row class="mt-2" v-show2="isConnected()">
        <b-dropdown text="Game Types" v-model="selectedGameType" @change="onGameTypeChanged" :disabled="!isConnected">
          <b-dropdown-item v-for="gameType in gameTypes" :key="gameType">{{ gameType }}</b-dropdown-item>
        </b-dropdown>

        <label class="ml-5 col-1.5 col-form-label">Arena size:</label>
        <b-input-group class="col-2">
          <b-input-group-addon>width</b-input-group-addon>
          <b-form-input type="number" placeholder="800" v-model="game['arena-width']" :disabled="game['is-arena-width-fixed']" :min="rules.arenaMinSize" :max="rules.arenaMaxSize" step="100"/>
        </b-input-group>
        <b-input-group class="col-2">
          <b-input-group-addon>height</b-input-group-addon>
          <b-form-input type="number" placeholder="600" v-model="game['arena-height']" :disabled="game['is-arena-height-fixed']" :min="rules.arenaMinSize" :max="rules.arenaMaxSize" step="100"/>
        </b-input-group>
      </b-row>

      <b-row class="mt-2" v-show2="isConnected()">
        <b-col>
          <b-form-group label="Min. number of participants"/>
            <b-input type="number" v-model="game['min-number-of-participants']" :disabled="game['is-min-number-of-participants-fixed']" :min="1"/>
          </b-form-group>
          <b-form-group label="Max. number of participants"/>
            <b-input class="col-4" type="number" v-model="game['max-number-of-participants']" :disabled="game['is-max-number-of-participants-fixed']" :min="1"/>
          </b-form-group>
        </b-col>
        <b-col>
        </b-col>
        <b-col>
        </b-col>
        <b-col>
        </b-col>
      </b-row>

    </b-container>

    <div v-show2="isConnected()">

      <div style="width: 100%; margin-top: 20px; overflow: hidden">
        <div style="width: 25%; float: left;">
          <div style="width: 100%">Min. number of participants</div>
          <div style="width: 100%"><input type="number" style="width: 60px" v-model="game['min-number-of-participants']" :disabled="game['is-min-number-of-participants-fixed']" :min="1"></div>
        </div>
        <div style="width: 25%; float: left;">
          <div style="width: 100%">Number of rounds</div>
          <div style="width: 100%"><input type="number" style="width: 60px" v-model="game['number-of-rounds']" :disabled="game['is-number-of-rounds-fixed']" :min="1"></div>
        </div>
        <div style="width: 25%; float: left;">
          <div style="width: 100%">Inactivity turns</div>
          <div style="width: 100%"><input type="number" style="width: 60px" v-model="game['inactivity-turns']" :disabled="game['is-inactivity-turns-fixed']" :min="1" step="50"></div>
        </div>
        <div style="width: 25%; float: left;">
          <div style="width: 100%">Delayed observer turns</div>
          <div style="width: 100%"><input type="number" style="width: 60px" v-model="game['delayed-observer-turns']" :disabled="game['is-delayed-observer-turns-fixed']" :min="1"></div>
        </div>
      </div>
      <div style="width: 100%; margin-top: 20px; overflow: hidden">
        <div style="width: 25%; float: left;">
          <div style="width: 100%">Max. number of participants</div>
          <div style="width: 100%"><input type="number" style="width: 60px" v-model="game['max-number-of-participants']" :disabled="game['is-max-number-of-participants-fixed']" :min="1"></div>
        </div>
        <div style="width: 25%; float: left;">
          <div style="width: 100%">Ready timeout (ms)</div>
          <div style="width: 100%"><input type="number" style="width: 60px" v-model="game['ready-timeout']" :disabled="game['is-ready-timeout-fixed']" :min="1"></div>
        </div>
        <div style="width: 25%; float: left;">
          <div style="width: 100%">Turn timeout (ms)</div>
          <div style="width: 100%"><input type="number" style="width: 60px" v-model="game['turn-timeout']" :disabled="game['is-turn-timeout-fixed']" :min="1"></div>
        </div>
        <div style="width: 25%; float: left;">
          <div style="width: 100%">Gun cooling rate</div>
          <div style="width: 100%"><input type="number" style="width: 60px" v-model="game['gun-cooling-rate']" :disabled="game['is-gun-cooling-rate-fixed']" :min="rules.minGunCoolingRate" :max="rules.maxGunCoolingRate" step="0.1"></div>
        </div>
      </div>
    </div>
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
      selectedGameType: null,

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
          case 'server-handshake':
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
      console.log('<-controller-handshake')

      connection.send(
        JSON.stringify({
          type: 'controller-handshake',
          name: 'Robocode 2 Game Controller',
          version: '0.1.0',
          author: 'Flemming N. Larsen <fnl@users.sourceforge.net>'
        })
      )
    },
    handleServerHandshake (serverHandshake) {
      console.log('->server-handshake')

      this.serverHandshake = serverHandshake

      var gameTypes = []

      if (serverHandshake) {
        const games = serverHandshake.games
        if (games) {
          games.forEach(element => {
            gameTypes.push(element['game-type'])
          })
        }
      }
      this.gameTypes = gameTypes
    },
    onGameTypeChanged () {
      this.game = this.serverHandshake.games.find(
        game => game['game-type'] === this.selectedGameType
      )
    }
  }
}
</script>

<style lang="scss">
@import "../styles/_variables.scss";
@import "../../node_modules/bootstrap/scss/bootstrap.scss";
@import "../styles/_bootswatch.scss";
</style>