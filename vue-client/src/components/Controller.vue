<template>
  <div class="controller">
    <div width="100%">
      Server URL: <input type="url" v-model=serverUrl>
      <span style="padding-left: 10px"/>
      <button @click="connect" v-show="!isConnected()">Connect</button>
      <button @click="disconnect" v-show="isConnected()">Disconnect</button>
      <span style="padding-left: 10px"/>Status: {{ connectionStatus }}
    </div>
  </div>
</template>

<script>
export default {
  name: "controller",
  data() {
    return {
      server: "localhost",
      port: 50000,
      serverUrl: null,
      socket: null,
      connectionStatus: "not connected"
    };
  },
  mounted() {
    const server = this.$route.query.server;
    if (server) {
      this.server = server;
    }
    const port = this.$route.query.port;
    if (port) {
      this.port = port;
    }
    this.serverUrl = "ws://" + this.server + ":" + this.port;
  },
  methods: {
    isConnected() {
      var s = this.socket;
      return s != null && s.readyState === WebSocket.OPEN;
    },
    connect() {
      const vm = this;

      const ws = new WebSocket(vm.serverUrl);

      vm.socket = ws;

      ws.onopen = function(event) {
        console.log("ws connected to: " + event.target.url);

        vm.connectionStatus = "connected";

        ws.send(
          JSON.stringify({
            type: "controller-handshake",
            name: "Test",
            version: "0.0.1",
            author: "fnl"
          })
        );
      };

      ws.onerror = function(event) {
        console.log("ws error: " + event.data);

        vm.connectionStatus = "error: " + event.data;
      };

      ws.onclose = function(event) {
        console.log("ws closed: " + event.target.url);

        vm.connectionStatus = "not connected";
      };

      ws.onmessage = function(event) {
        console.log("ws message: " + event.data);
      };
    },
    disconnect() {
      if (this.isConnected) {
        this.socket.close();
      }
    }
  }
};
</script>