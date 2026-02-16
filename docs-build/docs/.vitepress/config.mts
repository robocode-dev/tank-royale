import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'
import katex from './katex-plugin'

export default withMermaid(defineConfig({
  lang: 'en-US',
  title: 'Robocode Tank Royale Docs',
  description: 'Documentation for the programming game Robocode Tank Royale.',
  ignoreDeadLinks: true,

  base: '/',
  outDir: './.vitepress/dist',

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
            {text: 'User Data & Config Files', link: '/articles/user-data-config'},
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
          { text: 'Collision Mechanics', link: '/articles/collision-mechanics' },
          { text: 'Scoring', link: '/articles/scoring' },
          { text: 'Team Messages', link: '/articles/team-messages'},
          { text: 'Booter', link: '/articles/booter' },
          { text: 'Tank Royale', link: '/articles/tank-royale' },
          { text: 'History', link: '/articles/history' },
        ]
      },
      {
        text: 'Advanced Topics',
        items: [
          { text: 'Testing & Debugging Guide', link: '/articles/testing-guide' },
          { text: 'Performance Optimization', link: '/articles/performance-optimization' },
          { text: 'Custom Game Setup', link: '/articles/custom-game-setup' },
          { text: 'Team Strategies', link: '/articles/team-strategies' },
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/robocode-dev/tank-royale' }
    ],

    footer: {
      message: 'Released under the Apache License 2.0.',
        copyright: 'Copyright © 2022 Flemming Nørnberg Larsen'
    }
  },

  markdown: {
    config(md) {
      md.use(katex);
    }
  }
}));
