<template>
  <div class="arena">
    <b-container>
      <div class="mt-3"></div>
      <canvas id="canvas" width="800" height="600"></canvas>
      <b-row class="mt-2">
        <b-col sm="8">
          <b-btn @click="startGame" v-show="!isRunning">Start Game</b-btn>
          <b-btn @click="stopGame" v-show="isRunning">Stop Game</b-btn>

          <b-btn @click="pauseGame" v-show="!isPaused" :disabled="!isRunning">Pause Game</b-btn>
          <b-btn @click="resumeGame" v-show="isPaused" :disabled="!isRunning">Resume Game</b-btn>
        </b-col>
      </b-row>
    </b-container>
    <b-modal ref="resultsModal" lazy size="lg" hide-footer title="Results for x rounds">
      <b-table style="font-size: 12px;" head-variant="dark" small responsive hover :items="getResults"></b-table>
      <b-btn class="mt-3" block>OK</b-btn>
    </b-modal>
  </div>
</template>

<script src="./Arena.ts"></script>

<style lang="scss">
  @import "@/styles/_variables.scss";
  @import "@/../node_modules/bootstrap/scss/bootstrap.scss";
  @import "@/styles/_bootswatch.scss";
</style>
