<template>
  <div class="arena">
    <b-container>
      <b-row class="mt-3">
        <canvas id="canvas" width="800" height="600"/>
      </b-row>
      <b-row class="mt-1">
        <b-button :pressed.sync="paused" @click="onTogglePause">{{ paused ? "Resume" : "Pause" }}</b-button>
        <b-button @click="onStop">Stop</b-button>
        <b-button @click="onRestart">Restart</b-button>
      </b-row>
    </b-container>
  </div>
</template>

<script>
  const sharedData = require('./shared-data.js')

  class Explosion {
    constructor (pos, size) {
      this.pos = pos
      this.size = size
    }
  }

  export default {
    name: 'arena',
    data () {
      return {
        shared: sharedData,

        canvas: null,
        ctx: null,

        paused: false,

        botStates: [],
        bulletStates: [],
        events: [],
        scanEvents: [],

        lastBotPositions: [],
        explosions: []
      }
    },
    mounted () {
      var ws = this.shared.connection

      ws.onmessage = this.onMessage(event)

      ws.onerror = function (event) {
        console.log('ws error: ' + event.data)
      }

      ws.onopen = function (event) {
        console.log('ws opened: ' + event.data)
      }

      ws.onclose = function (event) {
        console.log('ws closed: ' + event.data)
      }

      ws.send(
        JSON.stringify({
          type: 'startGame',
          gameSetup: this.shared.gameSetup,
          botAddresses: this.shared.selectedBots
        })
      )

      var canvas = document.getElementById('canvas')
      this.canvas = canvas
      var ctx = canvas.getContext('2d')
      this.ctx = ctx

      canvas.width = 400
      canvas.height = 400

      this.clearCanvas()
    },
    methods: {
      onTogglePause () {
        this.paused = !this.paused
      },
      onStop () {
      },
      onRestart () {
      },
      onMessage (event) {
        console.log('#### ' + event)

        var msg = JSON.parse(event.data)

        console.log('ws message: ' + msg)

        switch (msg.type) {
          case 'gameStartedEventForObserver':
            this.onGameStarted(event)
            break
          case 'tickEventForObserver':
            this.onTick(event)
            break
          default:
            console.warn('unhandled message type: ' + msg.type)
        }
      },
      onGameStarted (event) {
        var gameSetup = event.gameSetup
        this.canvas.width = gameSetup.width
        this.canvas.height = gameSetup.height

        this.draw()
      },
      onTick (event) {
        this.botStates = event.botStates || []
        this.bulletStates = event.bulletStates || []
        this.events = event.events || []
        this.scanEvents = []

        this.botStates.array.forEach(bot => {
          this.lastBotPositions[bot.id] = bot.position
        })

        this.events.forEach(event => {
          switch (event.type) {
            case 'botDeathEvent':
              var pos = this.lastBotPosition[event.victimId]
              this.explosions.push(new Explosion(pos, 40))
              break
            case 'bulletHitBotEvent':
              this.explosions.push(new Explosion(event.bullet.position, 15))
              break
            case 'scannedBotEvent':
              this.scanEvents.push(event)
              break
            default:
              console.warn('unhandled tick event: ' + event.type)
          }

          this.draw()

          this.explosions.forEach(explosion => {
            explosion.size -= 5
            if (explosion.size <= 0) {
              this.explosions.remove(explosion)
            }
          })
        })
      },
      draw () {
        this.clearCanvas()
        this.drawBullets()
        this.drawBots()
        this.drawScans()
        this.drawExplosions()
      },
      clearCanvas () {
        var canvas = this.canvas
        var ctx = this.ctx
        ctx.fillStyle = 'black'
        ctx.fillRect(1, 1, canvas.width - 2, canvas.height - 2)

        ctx.lineWidth = 1
        ctx.strokeStyle = 'red'
        ctx.strokeRect(0.5, 0.5, canvas.width - 0.5, canvas.height - 0.5)
      },
      drawBullets () {
        this.bulletStates.forEach(bullet => {
          var pos = bullet.position
          this.drawBullet(pos.x, pos.y, bullet.power)
        })
      },
      drawBots () {
        this.botStates.forEach(bot => {
          var pos = bot.position
          this.drawBot(pos.x, pos.y, bot)
        })
      },
      drawScans () {
        this.scanEvents.forEach(scanEvent => {
          var pos = scanEvent.position
          this.fillCirle(pos.x, pos.y, 36 / 2, 'rgba(255, 255, 0, 1.0')
        })
      },
      drawExplosions () {
        this.explosions.forEach(explosion => {
          var pos = explosion.position
          this.fillCircle(pos.x, pos.y, explosion.size, 'red')
        })
      },
      fillCircle (x, y, r, color) {
        var ctx = this.ctx
        ctx.fillStyle = color
        ctx.beginPath()
        ctx.arc(x, y, r, 0, 2 * Math.PI)
        ctx.fill()
      }
    }
  }
</script>

<style lang="scss">
  @import "../styles/_variables.scss";
  @import "../../node_modules/bootstrap/scss/bootstrap.scss";
  @import "../styles/_bootswatch.scss";
</style>