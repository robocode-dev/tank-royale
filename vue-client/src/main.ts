import Vue from "vue";
import App from "./App.vue";
import BootstrapVue from "bootstrap-vue";
import router from "./router";
import store from "./store";

Vue.config.productionTip = false;

Vue.use(BootstrapVue);

// tslint:disable-next-line
new Vue({
  router,
  store,
  el: "#app",
  template: "<App/>",
  components: { App },
});
