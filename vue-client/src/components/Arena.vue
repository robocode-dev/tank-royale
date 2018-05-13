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
        gameSetup: sharedData.gameSetup,
        selectedBots: sharedData.selectedBots
      }
    },
    mounted () {
      this.$options.sockets.onmessage = function (event) {
        console.log('ws message: ' + event.data)

        const message = JSON.parse(event.data)
        switch (message.type) {
        }
      }

      this.$socket.sendObj(
        {
          type: 'startGame',
          gameSetup: this.gameSetup,
          botAddresses: this.selectedBots
        }
      )
      console.info("Start game")
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