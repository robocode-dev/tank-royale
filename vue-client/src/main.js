import Vue from "vue";
import App from "./App";
import BootstrapVue from "bootstrap-vue";
import router from "./router";

Vue.config.productionTip = false;

Vue.use(BootstrapVue);

/* eslint-disable no-new */
new Vue({
  router,
  el: "#app",
  template: "<App/>",
  components: { App }
});
