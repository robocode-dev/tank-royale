<template>
  <div id="app">
    <setup/>
  </div>
</template>

<script>

import Setup from './components/Setup'
import Arena from './components/Arena'

export default {
  name: 'app',
  components: {
    Setup,
    Arena
  }
}
</script>