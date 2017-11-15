<template>
  <div class="controller">
    Server URL: <input type="url" :value=serverUrl><button style="margin-left: 10px" @click="onConnect">Connect</button>
  </div>
</template>

<script>
export default {
  name: 'controller',
  data() {
    return {
      server: 'localhost',
      port: 50000
    }
  },
  mounted() {
      let server = this.$route.query.server;
      if (server) {
        this.server = server;
      }
      let port = this.$route.query.port;
      if (port) {
        this.port = port;
      }
  },
  methods: {
    onConnect() {
    }
  },
  computed: {
    serverUrl() {
      return 'ws://' + this.server + ':' + this.port;
    }
  }
}
</script>