import Vue from 'vue'
import Router from 'vue-router'
import GameController from '@/components/GameController'

import '../styles/styles.css'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'GameController',
      component: GameController
    }
  ]
})
