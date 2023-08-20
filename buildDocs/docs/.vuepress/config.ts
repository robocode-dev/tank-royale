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

    colorMode: 'dark',
    colorModeSwitch: false,

    logo: '/Tank-logo.svg',

    sidebar: [
      '/articles/intro',
      '/articles/installation',
      '/articles/gui',
      '/tutorial/my-first-bot',
      '/api/apis',
      '/articles/debug',
      '/articles/anatomy',
      '/articles/coordinates-and-angles',
      '/articles/physics',
      '/articles/scoring',
      '/articles/booter',
      '/articles/tank-royale',
      '/articles/history',
    ],

    contributors: false,
  }),

  plugins: [
    mdEnhancePlugin({
      katex: true,
      mermaid: true,
      footnote: true,
    })
  ],

  markdown: {
    code: {
       lineNumbers: false,
    },
  },
};

export default config;