// E2E del flujo completo: buscar -> ver detalle -> cotizar -> reservar -> pagar
const { test, expect } = require('@playwright/test');

// fecha dentro del periodo disponible (días 1 a 60), distinta en cada corrida
function fecha(diasDesdeHoy) {
  const d = new Date();
  d.setDate(d.getDate() + diasDesdeHoy);
  return d.toISOString().slice(0, 10);
}

test('Flujo de reserva inmediata: buscar, cotizar, reservar y pagar', async ({ page }) => {
  const desplazamiento = 25 + (Math.floor(Date.now() / 60000) % 30); // entre 25 y 54 días
  const llegada = fecha(desplazamiento);
  const salida = fecha(desplazamiento + 2);

  // iniciar sesión como huésped
  await page.goto('/login');
  await page.locator('#correo').fill('maria.huesped@correo.co');
  await page.locator('#contrasena').fill('Clave1234');
  await page.locator('form button[type=submit]').click();
  await expect(page.getByRole('link', { name: 'Mis reservas' })).toBeVisible();

  // buscar en la portada por ciudad y abrir el alojamiento
  await page.goto('/');
  await page.getByRole('button', { name: 'Medellín' }).click();
  await page.getByText('Apartamento con vista en Laureles').click();
  await expect(page.getByRole('heading', { name: 'Apartamento con vista en Laureles' })).toBeVisible();

  // cotizar
  await page.locator('#llegada').fill(llegada);
  await page.locator('#salida').fill(salida);
  await page.locator('#huespedes').fill('2');
  await page.getByRole('button', { name: 'Cotizar' }).click();
  await expect(page.getByRole('button', { name: 'Reservar' })).toBeVisible();

  // reservar (modo inmediato: pasa a pagar)
  await page.getByRole('button', { name: 'Reservar' }).click();
  await expect(page.getByRole('heading', { name: 'Pagar reserva' })).toBeVisible();

  // pagar con tarjeta de prueba
  await page.locator('#numeroTarjeta').fill('4242424242424242');
  await page.locator('#titular').fill('Maria Torres');
  await page.locator('#codigoSeguridad').fill('123');
  await page.getByRole('button', { name: 'Pagar' }).click();
  await expect(page).toHaveURL(/mis-reservas/);
  await expect(page.getByText('Pago aprobado. Su reserva quedó confirmada.')).toBeVisible();
});
