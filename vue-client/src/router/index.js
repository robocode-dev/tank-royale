import Vue from 'vue'
import Router from 'vue-router'
import Setup from '@/components/Setup'
import Arena from '@/components/Arena'

import '../styles/styles.css'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: {
        name: 'Arena'
      }
    },
    {
      path: '/setup',
      name: 'Setup',
      component: Setup
    },
    {
      path: '/arena',
      name: 'Arena',
      component: Arena
    }
  ]
})
