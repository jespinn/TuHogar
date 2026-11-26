// E2E del módulo Usuarios: registro, inicio de sesión y rutas protegidas
const { test, expect } = require('@playwright/test');

const unico = Date.now().toString().slice(-8); // correo y documento distintos en cada corrida

async function iniciarSesion(page, correo, contrasena) {
  await page.goto('/login');
  await page.locator('#correo').fill(correo);
  await page.locator('#contrasena').fill(contrasena);
  await page.locator('form button[type=submit]').click();
}

test('RF-01: registro con contraseña débil se rechaza y con contraseña segura se crea la cuenta', async ({ page }) => {
  await page.goto('/registro');
  await page.locator('#nombre').fill('Usuario Prueba');
  await page.locator('#correo').fill(`e2e${unico}@correo.co`);
  await page.locator('#numeroDocumento').fill(`9${unico}`);
  await page.locator('#contrasena').fill('Clave1234');
  await page.locator('#confirmacion').fill('Clave1234');
  await page.locator('#huesped').check();
  await page.locator('#acepta').check();
  await page.getByRole('button', { name: 'Crear cuenta' }).click();
  await expect(page.getByText('Debe tener mayúsculas, minúsculas, números y símbolos')).toBeVisible();

  await page.locator('#contrasena').fill('Clave123!');
  await page.locator('#confirmacion').fill('Clave123!');
  await page.getByRole('button', { name: 'Crear cuenta' }).click();
  await expect(page.getByText('Cuenta creada. Ya puede iniciar sesión.')).toBeVisible();
});

test('RF-02: inicio de sesión con datos incorrectos muestra mensaje genérico', async ({ page }) => {
  await iniciarSesion(page, 'maria.huesped@correo.co', 'Erronea999');
  await expect(page.getByText('Correo o contraseña incorrectos')).toBeVisible();
});

test('RF-02: el huésped inicia sesión, ve su menú y cierra sesión', async ({ page }) => {
  await iniciarSesion(page, 'maria.huesped@correo.co', 'Clave1234');
  await expect(page.getByRole('link', { name: 'Mis reservas' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Reportes' })).toHaveCount(0);
  await page.getByRole('button', { name: 'Salir' }).click();
  await expect(page).toHaveURL(/\/login/);
});

test('RF-02: sin sesión no se puede entrar a Reportes', async ({ page }) => {
  await page.goto('/reportes');
  await expect(page).toHaveURL(/\/login/);
});
