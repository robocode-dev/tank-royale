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
        ]
      },
      {
        text: 'Installation',
        items: [
          { text: 'Overview', link: '/articles/installation' },
          { text: 'Installing Robocode', link: '/articles/installing-robocode' },
          { text: 'Running the GUI', link: '/articles/running-the-gui' },
          { text: 'Installing Sample Bots', link: '/articles/installing-sample-bots' },
          { text: 'Installing Sounds', link: '/articles/installing-sounds' },
        ]
      },
      {
        text: 'GUI',
        items: [
          { text: 'Overview', link: '/articles/gui' },
          { text: 'Setting up and Starting a Battle', link: '/articles/gui-battle-setup' },
          { text: 'Viewing Battles and Bot State', link: '/articles/gui-battle-view' },
          { text: 'Recording and Replaying Battles', link: '/articles/gui-recording-and-replay' },
          { text: 'Configuring the GUI', link: '/articles/gui-configuration' },
        ]
      },
      {
        text: 'Configuration',
        items: [
          { text: 'User Data & Config Files', link: '/articles/user-data-config' },
          { text: 'User Data Locations', link: '/articles/user-data-locations' },
          { text: 'Configuration Files', link: '/articles/configuration-files' },
          { text: 'Backing Up and Resetting', link: '/articles/backing-up-and-resetting' },
        ]
      },
      {
        text: 'Tutorial',
        items: [
          { text: 'Getting Started', link: '/tutorial/getting-started' },
          { text: 'My First Bot', link: '/tutorial/my-first-bot' },
          { text: 'My First Bot for .NET', link: '/tutorial/dotnet/my-first-bot-for-dotnet' },
          { text: 'My First Bot for JVM', link: '/tutorial/jvm/my-first-bot-for-jvm' },
          { text: 'My First Bot for Python', link: '/tutorial/python/my-first-bot-for-python' },
          { text: 'My First Bot for TypeScript', link: '/tutorial/typescript/my-first-bot-for-typescript' },
          { text: 'Beyond the Basics', link: '/tutorial/beyond-the-basics' },
        ]
      },
      {
        text: 'Reference',
        items: [
          { text: 'APIs', link: '/api/apis' },
          { text: 'Battle Runner', link: '/api/battle-runner' },
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
