// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import Router from 'vue-router'
import App from './App'

Vue.use(Router);

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
