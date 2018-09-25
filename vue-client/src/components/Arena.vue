<template>
  <div class="arena">
    <b-container>
      <div class="mt-3"></div>
      <canvas id="canvas" width="800" height="600"></canvas>
      <b-row class="mt-2">
        <b-col sm="8">
            <b-btn @click="startGame" v-show="!isGameRunning">Start Game</b-btn>
            <b-btn @click="stopGame" v-show="isGameRunning">Stop Game</b-btn>

            <b-btn @click="pauseGame" v-show="!isGamePaused" :disabled="!isGameRunning">Pause Game</b-btn>
            <b-btn @click="resumeGame" v-show="isGamePaused" :disabled="!isGameRunning">Resume Game</b-btn>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>

<script>
  import ReconnectingWebSocket from 'reconnectingwebsocket'

  class Point {
    constructor(x, y) {
      this.x = x
      this.y = y
    }
  }

  class Explosion {
    constructor(pos, size) {
      this.pos = pos
      this.size = size
    }
  }

  export default {
    name: 'arena',
    data() {
      return {
        canvas: null,
        ctx: null,

        socket: null,
        clientKey: null,

        botStates: [],
        bulletStates: [],
        events: [],
        scanEvents: [],

        lastBotPositions: [],
        explosions: [],

        isGameRunning: false,
        isGamePaused: false
      }
    },
    mounted() {
      this.canvas = document.getElementById("canvas")
      this.ctx = canvas.getContext("2d")

      this.clearCanvas()

      var socket = new ReconnectingWebSocket(this.$store.getters.serverUrl)
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
          case 'gameAbortedEventForObserver':
            vm.onGameAborted(message)
            break
          case 'gameEndedEventForObserver':
            vm.onGameEnded(message)
            break
          case 'gamePausedEventForObserver':
            vm.onGamePaused(message)
            break
          case 'gameResumedEventForObserver':
            vm.onGameResumed(message)
            break
        }
        var canvasDiv = document.getElementById('canvas')

//        vm.startGame()
      }

      socket.onopen = function (event) {
      }
    },
    methods: {
      onServerHandshake(serverHandshake) {
        console.log('->serverHandshake')

        this.clientKey = serverHandshake.clientKey

        this.sendControllerHandshake()
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
        console.info("<-startGame")

        this.socket.send(JSON.stringify(
          {
            clientKey: this.clientKey,
            type: 'startGame',
            gameSetup: this.$store.getters.gameSetup,
            botAddresses: this.$store.getters.selectedBots
          }
        ))
      },
      stopGame() {
        console.info("<-stopGame")

        this.socket.send(JSON.stringify(
          {
            clientKey: this.clientKey,
            type: 'stopGame'
          }
        ))
      },
      pauseGame() {
        console.info("<-pauseGame")

        this.socket.send(JSON.stringify(
          {
            clientKey: this.clientKey,
            type: 'pauseGame'
          }
        ))
      },
      resumeGame() {
        console.info("<-resumeGame")

        this.socket.send(JSON.stringify(
          {
            clientKey: this.clientKey,
            type: 'resumeGame'
          }
        ))
      },
      onGameStarted(gameStartedEvent) {
        this.isGameRunning = true

        console.log('->gameStarted')

        this.botStates = []
        this.bulletStates = []
        this.events = []
        this.scanEvents = []

        this.lastBotPositions = []
        this.explosions = []
      },
      onGameAborted(gameAbortedEvent) {
        this.isGameRunning = false
      },
      onGameEnded(gameEndedEvent) {
        this.isGameRunning = false
      },
      onGamePaused(gamePausedEvent) {
        this.isGamePaused = true
      },
      onGameResumed(gameResumedEvent) {
        this.isGamePaused = false
      },
      onTick(tickEvent) {
        console.log('->tickEvent')

        this.botStates = tickEvent.botStates
        this.bulletStates = tickEvent.bulletStates
        this.events = tickEvent.events
        this.scanEvents = []

        this.botStates.forEach(bot => {
          this.lastBotPositions[bot.id] = bot.position
        })

        this.events.forEach(event => {
          switch (event.type) {

            case "botDeathEvent":
              var explosionPos = this.lastBotPositions[event.victimId]
              this.explosions.push(new Explosion(explosionPos, 40))
              break

            case "bulletHitBotEvent":
              var explosionPos = event.bullet.position
              this.explosions.push(new Explosion(explosionPos, 15))
              break

            case "scannedBotEvent":
              this.scanEvents.push(event)
              break

            default:
              console.error('Unknown event type: ' + event.type)
          }
        })

        this.draw()

        try {
          this.explosions.forEach(explosion => {
            explosion.size -= 5
            if (explosion.size <= 0) {
              this.explosions.splice(this.explosions.indexOf(explosion), 1)
            }
          })
        } catch (err) {
          debugger
        }
      },
      draw() {
        this.clearCanvas()

        this.bulletStates.forEach(bullet => {
          var pos = bullet.position
          this.drawBullet(pos.x, pos.y, bullet.power)
        })

        this.botStates.forEach(bot => {
          var pos = bot.position
          this.drawBot(pos.x, pos.y, bot)
        })

        this.scanEvents.forEach(scanEvent => {
          var pos = scanEvent.position
          this.fillCircle(pos.x, pos.y, 18, 'rgba(255, 255, 0, 1.0)')
        })

        this.explosions.forEach(explosion => {
          var pos = explosion.pos
          this.fillCircle(pos.x, pos.y, explosion.size, 'red')
        })
      },
      clearCanvas() {
        this.ctx.fillStyle = 'black'
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height)
      },
      drawBullet(x, y, power) {
        var size = Math.max(Math.sqrt(5 * power), 1)
        this.fillCircle(x, y, size, 'white')
      },
      drawBot(x, y, bot) {
        this.drawBotBody(x, y, bot.direction)
        this.drawGun(x, y, bot.gunDirection)
        this.drawRadar(x, y, bot.radarDirection)
        this.drawScanField(x, y, bot.radarDirection, bot.radarSweep)
        this.drawLabels(x, y, bot.id, bot.energy)
      },
      drawBotBody(x, y, direction) {
        var ctx = this.ctx
        ctx.save()

        ctx.translate(x, y)
        ctx.rotate(this.toRad(direction))

        ctx.fillStyle = 'blue'
        ctx.beginPath()
        ctx.fillRect(-18, -18 + 1 + 6, 36, 36 - 2 * 7)

        ctx.fillStyle = 'gray'
        ctx.beginPath()
        ctx.fillRect(-18, -18, 36, 6)
        ctx.fillRect(-18, 18 - 6, 36, 6)

        ctx.restore()
      },
      drawGun(x, y, direction) {
        var ctx = this.ctx
        ctx.save()

        ctx.translate(x, y)

        ctx.fillStyle = 'lightgray'
        ctx.beginPath()
        ctx.arc(0, 0, 10, 0, this.toRad(360))
        ctx.fill()

        ctx.beginPath()
        ctx.rotate(this.toRad(direction))
        ctx.rect(10, -2, 14, 4)
        ctx.fill()

        ctx.restore()
      },
      drawRadar(x, y, direction) {
        var ctx = this.ctx
        ctx.save()

        ctx.translate(x, y)
        ctx.rotate(this.toRad(direction))

        ctx.fillStyle = 'red'
        ctx.beginPath()
        ctx.arc(10, 0, 15, 7 * Math.PI / 10, Math.PI * 2 - 7 * Math.PI / 10, false)
        ctx.arc(12, 0, 13, Math.PI * 2 - 7 * Math.PI / 10, 7 * Math.PI / 10, true)
        ctx.fill()

        ctx.beginPath()
        ctx.arc(0, 0, 4, 0, 2 * Math.PI)
        ctx.fill()

        ctx.restore()
      },
      drawScanField(x, y, direction, spreadAngle) {
        var angle = this.toRad(spreadAngle)

        var color = 'rgba(0, 255, 255, 0.5)'

        var ctx = this.ctx
        ctx.save()
        ctx.translate(x, y)
        ctx.rotate(this.toRad(direction))

        if (Math.abs(angle) < 0.0001) {
          ctx.strokeStyle = color
          ctx.lineTo(1200, 0)
          ctx.stroke()
        } else {
          ctx.fillStyle = color
          ctx.beginPath()
          ctx.moveTo(0, 0)
          ctx.arc(0, 0, 1200, 0, angle, (angle < 0))
          ctx.lineTo(0, 0)
          ctx.fill()
        }

        ctx.restore()
      },
      drawLabels(x, y, botId, energy) {
        var ctx = this.ctx
        ctx.save()
        ctx.fillStyle = 'white'
        ctx.font = '10px Arial'

        var idStr = "" + botId
        var energyStr = energy.toFixed(1)
        var idWidth = ctx.measureText(idStr).width
        var energyWidth = ctx.measureText(energyStr).width

        ctx.fillText(idStr, x - idWidth / 2, y + 30 + 10)
        ctx.fillText(energyStr, x - energyWidth / 2, y - 30)

        ctx.restore()
      },
      fillCircle(x, y, r, color) {
        var ctx = this.ctx
        ctx.fillStyle = color
        ctx.beginPath()
        ctx.arc(x, y, r, 0, 2 * Math.PI)
        ctx.fill()
      },
      toRad(degrees) {
        return degrees * Math.PI / 180
      }
    }
  }
</script>

<style lang="scss">
  @import "../styles/_variables.scss";
  @import "../../node_modules/bootstrap/scss/bootstrap.scss";
  @import "../styles/_bootswatch.scss";
</style>