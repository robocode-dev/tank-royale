import Vue from "vue";
import Router from "vue-router";
import Setup from "@/views/Setup";
import Arena from "@/views/Arena";

import "./styles/styles.css";

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: "/",
      redirect: {
        name: "Setup"
      }
    },
    {
      path: "/setup",
      name: "Setup",
      component: Setup
    },
    {
      path: "/arena",
      name: "Arena",
      component: Arena
    }
  ]
});
