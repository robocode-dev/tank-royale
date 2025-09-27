import { viteBundler } from '@vuepress/bundler-vite'
import { defaultTheme } from '@vuepress/theme-default'
import { defineUserConfig } from 'vuepress'
import { mdEnhancePlugin } from 'vuepress-plugin-md-enhance'

export default defineUserConfig({
  lang: 'en-US',
  title: 'Robocode Tank Royale Docs',
  description: 'Documentation for the programming game Robocode Tank Royale.',

  base: '/tank-royale/',
  port: 8080,
  dest: 'build/docs',

  head: [['link', { rel: 'icon', href: '/favicon.ico' }]],

  bundler: viteBundler({
    viteOptions: {
      build: {
        rollupOptions: {
          output: {
            // Use stable, non-hashed filenames so links remain the same across builds
            entryFileNames: 'assets/[name].js',
            chunkFileNames: 'assets/[name].js',
            assetFileNames: (chunkInfo) => {
              // Preserve original extension and folder
              const ext = chunkInfo.name && chunkInfo.name.includes('.')
                ? chunkInfo.name.substring(chunkInfo.name.lastIndexOf('.'))
                : '[extname]'
              return `assets/[name]${ext}`
            },
          },
        },
      },
    },
  }),

  theme: defaultTheme({

    logo: '/Tank-logo.svg',

    colorMode: 'dark',
    colorModeSwitch: false,

    sidebar: [
      '/articles/intro',
      '/articles/installation',
      '/articles/gui',
      '/tutorial/getting-started',
      '/tutorial/my-first-bot',
      '/api/apis',
      '/tutorial/beyond-the-basics',
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
});
