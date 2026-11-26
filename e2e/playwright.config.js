// configuración de las pruebas E2E
// por defecto prueba la app local (docker compose up -d); se puede cambiar con URL_APP
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './pruebas',
  timeout: 90000,
  workers: 1, // en orden, una a la vez
  reporter: [['list'], ['html', { outputFolder: 'reporte', open: 'never' }]],
  use: {
    baseURL: process.env.URL_APP || 'http://localhost:4200',
    screenshot: 'on',
    trace: 'retain-on-failure',
  },
});
