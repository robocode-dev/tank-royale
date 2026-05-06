import DefaultTheme from 'vitepress/theme'
import './custom.css'
import ThemeImage from './components/ThemeImage.vue'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('ThemeImage', ThemeImage)
  }
}
