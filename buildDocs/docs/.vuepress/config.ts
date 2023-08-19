import { defaultTheme, type UserConfig } from 'vuepress';
import { mdEnhancePlugin } from 'vuepress-plugin-md-enhance'

const config: UserConfig = {
  lang: 'en-US',
  title: 'Robocode Tank Royale Docs',
  description: 'Documentation for the programming game Robocode Tank Royale.',

  base: '/tank-royale/',
  port: 8080,
  dest: 'build/docs',

  theme: defaultTheme({
    logo: '/Tank-logo.svg',
    sidebar: [
      {
        text: 'What is Robocode?',
        link: '/articles/intro',
      },
      {
        text: 'Installation',
        link: '/articles/installation',
      },
      {
        text: 'GUI Application',
        link: '/articles/gui',
      },
      {
        text: 'My First Bot',
        link: '/tutorial/my-first-bot',
      },
      {
        text: 'API Documentation',
        link: '/api/apis',
      },
      {
        text: 'Debugging',
        link: '/articles/debug',
      },
      {
        text: 'Bot Anatomy',
        link: '/articles/anatomy',
      },
      {
        text: 'Coordinates and Angles',
        link: '/articles/coordinates-and-angles',
      },
      {
        text: 'Physics',
        link: '/articles/physics',
      },
      {
        text: 'Scoring',
        link: '/articles/scoring',
      },
      {
        text: 'Booter',
        link: '/articles/booter',
      },
      {
        text: 'Tank Royale vs orig. Robocode',
        link: '/articles/tank-royale',
      },
      {
        text: 'Robocode history',
        link: '/articles/history',
      },
    ],
  }),

  plugins: [
    mdEnhancePlugin({
      tex: true,
      mermaid: true,
      footnote: true,
    })
  ],
};

export default config;