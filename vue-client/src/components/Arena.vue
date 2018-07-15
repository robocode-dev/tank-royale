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

        ctrl: sharedData.controller,
        observer: sharedData.observer
      }
    },
    mounted () {
      var socket = new ReconnectingWebSocket(this.shared.serverUrl)
      this.socket = socket

      var vm = this

      socket.onmessage = function (event) {
        console.log('ws message: ' + event.data)

        const message = JSON.parse(event.data)
        switch (message.type) {
        }
      }

      socket.onopen = function (event) {
        vm.socket.send(JSON.stringify(
          {
            clientKey: sharedData.clientKey,
            type: 'startGame',
            gameSetup: vm.ctrl.gameSetup,
            botAddresses: vm.ctrl.selectedBots
          }
        ))
        console.info("Start game")
      }
    },
    methods: {
    }
  }
</script>

<style lang="scss">
  @import "../styles/_variables.scss";
  @import "../../node_modules/bootstrap/scss/bootstrap.scss";
  @import "../styles/_bootswatch.scss";
</style>