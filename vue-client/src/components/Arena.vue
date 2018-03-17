<template>
  <div class="arena">
    <b-container>
      <canvas id="canvas" width="800" height="600"/>
    </b-container>
  </div>
</template>

<script>
  const sharedData = require('./shared-data.js')

  export default {
    name: 'arena',
    data () {
      return {
        ctrl: sharedData.controller,
        observer: sharedData.observer
      }
    },
    mounted () {
      const connection = new WebSocket(sharedData.serverUrl)
      this.observer.connection = connection

      connection.onmessage = function (event) {
        console.log('ws message: ' + event.data)
      }

      this.ctrl.connection.send(
        JSON.stringify({
          type: 'startGame',
          gameSetup: this.ctrl.gameSetup,
          botAddresses: this.ctrl.selectedBots
        })
      )
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