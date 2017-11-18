<template>
  <div id="app">
    <game-controller/>
  </div>
</template>

<script>

import GameController from './components/GameController'

export default {
  name: 'app',
  components: {
    GameController
  }
}
</script>