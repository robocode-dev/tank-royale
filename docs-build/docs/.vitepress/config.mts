import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'
import katex from './katex-plugin'

// Detects common Vite hash suffixes (dash or dot) right before the extension
const HASH_BEFORE_EXT_RE = /[-.][A-Za-z0-9_-]{6,}(?=\.[^./]+$)/g;

// Normalizes slashes and removes the hash fragment from the filename while keeping its folder path intact
const stripHashFromFileName = (filePath: string): string => {
  const normalized = filePath.replace(/\\/g, '/');
  const slashIndex = normalized.lastIndexOf('/');
  const dir = slashIndex >= 0 ? normalized.slice(0, slashIndex + 1) : '';
  const file = normalized.slice(slashIndex + 1);
  return `${dir}${file.replace(HASH_BEFORE_EXT_RE, '')}`;
};

// Adds a deterministic numeric suffix when multiple files collapse to the same hash-free name
const appendNumericSuffix = (filePath: string, suffix: number): string => {
  const slashIndex = filePath.lastIndexOf('/');
  const dir = slashIndex >= 0 ? filePath.slice(0, slashIndex + 1) : '';
  const file = filePath.slice(slashIndex + 1);
  const extIndex = file.lastIndexOf('.');
  const base = extIndex >= 0 ? file.slice(0, extIndex) : file;
  const ext = extIndex >= 0 ? file.slice(extIndex) : '';
  return `${dir}${base}-${suffix}${ext}`;
};

// Rollup plugin that rewrites emitted filenames (and internal references) to be stable, hash-free versions
const stableFileNamesPlugin = () => ({
  name: 'stable-file-names',
  generateBundle(_options, bundle) {
    const items = Object.keys(bundle)
      .map(fileName => {
        const cleanName = stripHashFromFileName(fileName);
        return { fileName, cleanName, alreadyClean: fileName === cleanName };
      })
      .sort((a, b) => {
        if (a.cleanName === b.cleanName) {
          if (a.alreadyClean !== b.alreadyClean) {
            return a.alreadyClean ? -1 : 1;
          }
          return a.fileName.localeCompare(b.fileName);
        }
        return a.cleanName.localeCompare(b.cleanName);
      });

    const counters = new Map<string, number>();
    const renames = new Map<string, string>();

    for (const item of items) {
      const currentCount = counters.get(item.cleanName) ?? 0;
      counters.set(item.cleanName, currentCount + 1);

      let targetName = item.cleanName;
      if (currentCount > 0) {
        targetName = appendNumericSuffix(item.cleanName, currentCount);
      }

      if (item.fileName !== targetName) {
        renames.set(item.fileName, targetName);
      }
    }

    if (!renames.size) {
      return;
    }

    const replacementPairs = Array.from(renames.entries());

    for (const entry of Object.values(bundle)) {
      if (entry.type === 'chunk') {
        let code = entry.code;
        for (const [from, to] of replacementPairs) {
          code = code.split(from).join(to);
        }
        entry.code = code;
      } else if (entry.type === 'asset' && typeof entry.source === 'string') {
        let source = entry.source;
        for (const [from, to] of replacementPairs) {
          source = source.split(from).join(to);
        }
        entry.source = source;
      }
    }

    for (const [from, to] of replacementPairs) {
      const entry = bundle[from];
      if (!entry) {
        continue;
      }
      entry.fileName = to;
      bundle[to] = entry;
      delete bundle[from];
    }
  }
});

export default withMermaid(defineConfig({
  lang: 'en-US',
  title: 'Robocode Tank Royale Docs',
  description: 'Documentation for the programming game Robocode Tank Royale.',
  appearance: 'dark',
  ignoreDeadLinks: true,

  base: '/',
  outDir: './.vitepress/dist',
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

  vite: {
    build: {
      rollupOptions: {
        output: {
          chunkFileNames: 'assets/chunks/[name].js'
        }
      }
    },
    plugins: [stableFileNamesPlugin()]
  }
}));
