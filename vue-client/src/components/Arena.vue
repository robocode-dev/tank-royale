<template>
  <div class="arena">
    <b-container>
      <canvas id="canvas" width="800" height="600"/>
    </b-container>
  </div>
</template>

<script>
  export default {
    name: 'arena',
    data () {
      return {
      }
    },
    mounted () {
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