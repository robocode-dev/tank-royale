import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'
import katex from './katex-plugin'

export default withMermaid(defineConfig({
  lang: 'en-US',
  title: 'Robocode Tank Royale Docs',
  description: 'Documentation for the programming game Robocode Tank Royale.',
  appearance: 'dark',

  base: '/',
  outDir: './build/docs',
  srcExclude: ['api/**'],

  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
    ['link', { rel: 'stylesheet', href: 'https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css' }]
  ],

  themeConfig: {
    logo: '/Tank-logo.svg',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Articles', link: '/articles/intro' },
      { text: 'Tutorial', link: '/tutorial/getting-started' },
      { text: 'API', link: '/api/apis' }
    ],

    sidebar: [
      {
        text: 'Guide',
        items: [
          { text: 'Introduction', link: '/articles/intro' },
          { text: 'Installation', link: '/articles/installation' },
          { text: 'GUI', link: '/articles/gui' },
        ]
      },
      {
        text: 'Tutorial',
        items: [
          { text: 'Getting Started', link: '/tutorial/getting-started' },
          { text: 'My First Bot', link: '/tutorial/my-first-bot' },
          { text: 'Beyond the Basics', link: '/tutorial/beyond-the-basics' },
        ]
      },
      {
        text: 'Reference',
        items: [
          { text: 'APIs', link: '/api/apis' },
          { text: 'Debugging', link: '/articles/debug' },
          { text: 'Anatomy of a Bot', link: '/articles/anatomy' },
          { text: 'Coordinates and Angles', link: '/articles/coordinates-and-angles' },
          { text: 'Physics', link: '/articles/physics' },
          { text: 'Scoring', link: '/articles/scoring' },
          { text: 'Booter', link: '/articles/booter' },
          { text: 'Tank Royale', link: '/articles/tank-royale' },
          { text: 'History', link: '/articles/history' },
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/robocode-dev/tank-royale' }
    ],

    footer: {
      message: 'Released under the Apache License 2.0.',
      copyright: 'Copyright © 2024 Flemming Nørnberg Larsen'
    }
  },

  markdown: {
    config(md) {
      md.use(katex);
    }
  },
}));
