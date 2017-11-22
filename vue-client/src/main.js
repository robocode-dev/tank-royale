// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import App from './App'
import Vue from 'vue'
import Router from 'vue-router'
import BootstrapVue from 'bootstrap-vue'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

import { Alert } from 'bootstrap-vue/es/components';

Vue.use(Router);
Vue.use(BootstrapVue);
Vue.use(Alert);

Vue.config.productionTip = false

var router = new Router({
  mode: 'history',
  routes: []
});

/* eslint-disable no-new */
var app = new Vue({
  router,
  el: '#app',
  template: '<App/>',
  components: { App }
})
