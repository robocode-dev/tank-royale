<template>
  <div class="arena">
    <b-container>
      <canvas id="canvas" width="800" height="600"/>
    </b-container>
  </div>
</template>

<script>
  import ReconnectingWebSocket from 'reconnectingwebsocket'

  const sharedData = require('./shared-data.js')

  export default {
    name: 'arena',
    data () {
      return {
        shared: sharedData,
        socket: null,

        clientKey: null,

        ctrl: sharedData.controller,
        observer: sharedData.observer
      }
    },
    mounted () {
      var socket = new ReconnectingWebSocket(this.shared.serverUrl)
      this.socket = socket

      const vm = this

      socket.onmessage = function (event) {
        console.log('ws message: ' + event.data)

        const message = JSON.parse(event.data)
        switch (message.type) {
          case 'serverHandshake':
            vm.onServerHandshake(message)
            break
          case 'gameStartedEventForObserver':
            vm.onGameStarted(message)
            break
          case 'tickEventForObserver':
            vm.onTick(message)
            break
        }
        var canvasDiv = document.getElementById('canvas')
      }

      socket.onopen = function (event) {
      }
    },
    methods: {
      onServerHandshake(serverHandshake) {
        console.log('->serverHandshake')

        this.clientKey = serverHandshake.clientKey

        this.sendControllerHandshake()

        this.startGame()
      },
      onGameStarted(gameStartedEvent) {
        console.log('->gameStarted')
      },
      onTick(tickEvent) {
        console.log('->tickEvent')
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
      startGame() {
        console.info("Starting game")

        // Start the game
        this.socket.send(JSON.stringify(
          {
            clientKey: this.clientKey,
            type: 'startGame',
            gameSetup: this.ctrl.gameSetup,
            botAddresses: this.ctrl.selectedBots
          }
        ))
      }
    }
  }
</script>

<style lang="scss">
  @import "../styles/_variables.scss";
  @import "../../node_modules/bootstrap/scss/bootstrap.scss";
  @import "../styles/_bootswatch.scss";
</style>