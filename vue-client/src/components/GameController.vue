<template>
  <div class="controller">
    <div width="100%">
      Server URL: <input type="url" v-model="serverUrl">
      <span style="padding-left: 10px"/>
      <button @click="connect" v-show="!isConnected()">Connect</button>
      <button @click="disconnect" v-show="isConnected()">Disconnect</button>
      <span style="padding-left: 10px;"/>Status: {{ connectionStatus }}
    </div>
    <div width="100%" style="margin-top: 20px;">
      Game Types:
      <select v-model="selectedGameType" @change="onGameTypeChanged">
        <option v-for="gameType in gameTypes" :key="gameType">{{ gameType }}</option>
      </select>
    </div>
    <div width="100%" style="margin-top: 20px;">
      Arena size:
      <input type="number" v-model="arenaWidth" style="width: 60px">
      &nbsp;x&nbsp;
      <input type="number" v-model="arenaHeight" style="width: 60px">
    </div>
  </div>
</template>

<script>
export default {
  name: "game-controller",
  data() {
    return {
      server: "localhost",
      port: 50000,
      serverUrl: null,
      connection: null,
      connectionStatus: "not connected",
      gameTypes: null,
      selectedGameType: null,
      arenaWidth: null,
      arenaHeight: null,
    };
  },
  computed: {
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
      var c = this.connection;
      return c != null && c.readyState == WebSocket.OPEN;
    },
    connect() {
      const vm = this;

      const connection = new WebSocket(vm.serverUrl);

      vm.connection = connection;

      connection.onopen = function(event) {
        console.log("ws connected to: " + event.target.url);

        vm.connectionStatus = "connected";

        vm.sendControllerHandshake(connection);
      };

      connection.onerror = function(event) {
        console.log("ws error: " + event.data);

        vm.connectionStatus = "error: " + event.data;
      };

      connection.onclose = function(event) {
        console.log("ws closed: " + event.target.url);

        vm.connectionStatus = "not connected";
      };

      connection.onmessage = function(event) {
        console.log("ws message: " + event.data);

        const message = JSON.parse(event.data);
        switch (message.type) {
          case "server-handshake":
            vm.handleServerHandshake(message);
            break;
        }
      };
    },
    disconnect() {
      if (this.isConnected) {
        this.connection.close();
      }
      this.connection = null;
      this.gameTypes = null;
    },
    sendControllerHandshake(connection) {
      connection.send(
        JSON.stringify({
          type: "controller-handshake",
          name: "Robocode 2 Game Controller",
          version: "0.1.0",
          author: "Flemming N. Larsen <fnl@users.sourceforge.net>"
        })
      );
    },
    handleServerHandshake(serverHandshake) {
      console.log("->server-handshake");

      var gameTypes = new Array();

      if (serverHandshake) {
        const games = serverHandshake.games;
        if (games) {
          games.forEach(element => {
            gameTypes.push(element["game-type"]);
          });
        }
      }
      this.gameTypes = gameTypes;
    },
    onGameTypeChanged() {
      console.log("selected game type: " + this.selectedGameType);
    }
  }
};
</script>