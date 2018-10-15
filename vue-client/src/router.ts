import Vue from "vue";
import Router from "vue-router";
import Setup from "@/views/Setup.vue";
import Arena from "@/views/Arena.vue";

import "./styles/styles.css";

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: "/",
      redirect: {
        name: "Setup",
      },
    },
    {
      path: "/setup",
      name: "Setup",
      component: Setup,
    },
    {
      path: "/arena",
      name: "Arena",
      component: Arena,
    },
  ],
});
