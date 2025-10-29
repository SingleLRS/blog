/*
 * Remove Google Fonts import from semantic-ui-css to avoid blocked requests in some regions.
 */
const fs = require('fs');
const path = require('path');

const targets = [
  path.join(__dirname, '..', 'node_modules', 'semantic-ui-css', 'semantic.min.css'),
  path.join(__dirname, '..', 'node_modules', 'semantic-ui-css', 'semantic.css'),
];

function stripGoogleFontsImport(filePath) {
  if (!fs.existsSync(filePath)) {
    return;
  }
  try {
    const css = fs.readFileSync(filePath, 'utf8');
    // Remove any @import that contains fonts.googleapis
    const patched = css.replace(/@import[^;]*fonts\.googleapis[^;]*;\s*/gi, '');
    if (patched !== css) {
      fs.writeFileSync(filePath, patched, 'utf8');
      console.log(`[patch-semantic-css] Patched: ${path.relative(process.cwd(), filePath)}`);
    } else {
      console.log(`[patch-semantic-css] No google fonts import found in: ${path.relative(process.cwd(), filePath)}`);
    }
  } catch (e) {
    console.warn(`[patch-semantic-css] Failed to patch ${filePath}:`, e.message);
  }
}

targets.forEach(stripGoogleFontsImport);


