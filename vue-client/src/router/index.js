import Vue from 'vue'
import Router from 'vue-router'
import GameController from '@/components/GameController'

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
