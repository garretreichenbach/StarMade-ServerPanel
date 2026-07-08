import { defineConfig } from 'tsup';

export default defineConfig({
  entry: ['src/index.ts'],
  format: ['esm'],
  platform: 'node',
  target: 'node22',
  outDir: 'dist',
  clean: true,
  sourcemap: true,
  // Bundle the workspace `shared` package (its entry points at TS source) into
  // the output so `node dist/index.js` needs no on-the-fly TS transpilation.
  noExternal: ['@ssp/shared'],
});
