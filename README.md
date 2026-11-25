# TuHogar: módulos Usuarios, Reportes y Reservas

Proyecto académico: backend Spring Boot 3 (Java 17) + frontend Angular 18.
Módulos: M01 Usuarios, M08 Reportes (solo Administrador) y M04 Reservas.

```
backend/    API REST (Spring Boot, JPA, Spring Security + JWT)
frontend/   Angular 18 (componentes standalone, formularios reactivos, Bootstrap)
docker-compose.yml   PostgreSQL 17 (db), backend y frontend
.env.example         plantilla de claves (copiar como .env)
```
La documentación del proyecto (diagramas, análisis y pruebas) está fuera del código, en la carpeta `TuHogar_Documentacion`.

## Requisitos
- JDK 17 y Maven 3.9 (o abrir `backend` en IntelliJ, que trae Maven)
- Node 20 y npm
- Docker Desktop (forma principal, PostgreSQL 17). Sin Docker se puede usar H2 (perfil `demo`).

## Claves (RNF-13)
El código no trae contraseñas ni secretos. Copie `.env.example` como `.env` y ponga sus valores:
`DB_PASSWORD` (clave de PostgreSQL) y `JWT_SECRETO` (32 caracteres o más). El `.env` no se sube al repositorio.
Si el backend arranca sin `JWT_SECRETO`, usa una clave aleatoria y las sesiones se pierden al reiniciarlo.

## Levantar todo con Docker (PostgreSQL 17, backend y frontend)
```bash
docker compose up -d --build
```
Abra http://localhost:4200 (API en http://localhost:8080/api).

## Levantar con PostgreSQL en Docker y el código en local

Terminal 1, base de datos y backend (puerto 8080). Use la misma clave del `.env`:
```bash
docker compose up -d db
cd backend
set DB_PASSWORD=su-clave        (PowerShell: $env:DB_PASSWORD="su-clave")
mvn spring-boot:run
```
Terminal 2, frontend (puerto 4200):
```bash
cd frontend
npm install
npm start
```
Abra http://localhost:4200.
La base queda en el puerto **5433** del PC, para no chocar con un PostgreSQL instalado en Windows (que usa 5432).
La primera vez, el contenedor ejecuta `schema.sql` y `data.sql`. Para empezar de cero: `docker compose down -v` y otra vez `docker compose up -d db` (los scripts no se vuelven a ejecutar sobre un volumen existente).
Al iniciar, el backend cifra con BCrypt las contraseñas de ejemplo (`DatosIniciales.java`).
En PowerShell, si `npm` está bloqueado, use `npm.cmd install` y `npm.cmd start`.

## Alternativa sin Docker: H2 en memoria (perfil `demo`)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```
El frontend se levanta igual. Con H2 los datos se reinician cada vez que se apaga el backend.

## Pruebas
```bash
cd backend
mvn test
```
44 pruebas (usan H2, perfil `demo`): Usuarios 19, Reservas 18 y Reportes 7.

Contraseñas nuevas (registro y recuperación): mínimo 8 caracteres con mayúsculas, minúsculas, números y símbolos, por ejemplo `Clave123!`.

## Credenciales de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@alojamientos.co | Admin1234 |
| Anfitrión | carlos.anfitrion@correo.co | Clave1234 |
| Anfitrión | laura.anfitriona@correo.co | Clave1234 |
| Huésped | maria.huesped@correo.co | Clave1234 |
| Huésped | pedro.huesped@correo.co | Clave1234 |

Datos de ejemplo: 12 publicaciones ACTIVAS en 6 ciudades (con política de cancelación flexible, moderada o estricta), 2 en BORRADOR y 1 BLOQUEADA (estas 3 no se ofrecen para reservar), y 6 reservas en distintos estados (confirmada, completada, pendiente de aprobación, pendiente de pago, rechazada y cancelada).
Tarjeta de prueba: cualquier número de 16 dígitos se aprueba, salvo los que terminan en `0000`, que se rechazan.

## Guion de demo (3 minutos)
1. **Registro y sesión (30 s):** en *Registrarse* use una contraseña distinta en la confirmación y un correo ya usado: cada error sale bajo su campo y el botón queda deshabilitado. Cree una cuenta como Huésped, inicie sesión y vea que el menú solo muestra *Elegir alojamiento*, *Mis reservas* y *Mi perfil*.
2. **Elegir y cotizar (40 s):** en *Elegir alojamiento* abra "Apartamento con vista en Laureles". Lea la política de cancelación. Pruebe una salida anterior a la llegada y más huéspedes que la capacidad (errores bajo el campo), luego elija 7 noches y pulse *Cotizar*: el desglose muestra el cargo de servicio del 10 % y el descuento del 10 %.
3. **Reservar y pagar (40 s):** pulse *Reservar* (modo inmediato: queda pendiente de pago). Pague con una tarjeta que termine en `0000` (se rechaza, la reserva sigue pendiente) y luego con `4242424242424242` (queda CONFIRMADA con su referencia).
4. **Cancelar (30 s):** en *Mis reservas* pulse *Cancelar*, escriba un motivo y confirme. La devolución sigue HU-24: 100 % si faltan 7 días o más, 50 % si faltan de 2 a 6 días y 0 % si faltan menos de 2.
5. **Anfitrión (20 s):** entre como `carlos.anfitrion@correo.co`, abra *Reservas de mis publicaciones* y use *Aprobar* o *Rechazar* sobre la reserva "pendiente aprobación" (solo aparecen en ese estado).
6. **Administrador (20 s):** entre como `admin@alojamientos.co`. Solo él ve *Reportes*: usuarios, publicaciones, reservas por estado, ingresos estimados, filtro por rango de fechas y *Exportar* en PDF o Excel.

## Reglas de reservas
- Solo una reserva CONFIRMADA (pagada) bloquea noches (RF-09). Las pendientes no bloquean; al aprobar o pagar se vuelve a revisar y, si otra se confirmó antes, se rechaza (HU-19 CA3).
- El dashboard filtra por fecha de llegada de la reserva y fecha de registro del usuario. Las publicaciones no tienen fecha en el modelo, así que no cambian con el filtro.

## Endpoints principales (`/api`)

| Método y ruta | Quién | Para qué |
|---|---|---|
| POST `/usuarios/registro` | Visitante | RF-01 registrarse |
| POST `/usuarios/login` | Visitante | RF-02 iniciar sesión (devuelve el token) |
| POST `/usuarios/reactivar` | Visitante | RF-04 reactivar cuenta inactiva |
| POST `/usuarios/recuperar` · `/usuarios/restablecer` | Visitante | RF-03 código y nueva contraseña |
| GET · PUT `/usuarios/perfil` | Con sesión | RF-04 ver y actualizar perfil |
| PUT `/usuarios/perfil/desactivacion` | Huésped, Anfitrión | RF-04 desactivar cuenta |
| GET `/usuarios` · PUT `/usuarios/{id}/desactivar` · `/usuarios/{id}/activar` | Administrador | RF-21 restringir o reactivar |
| GET `/reservas/alojamientos` · `/reservas/alojamientos/{id}` | Todos | Elegir alojamiento |
| POST `/reservas/cotizar` · POST `/reservas` | Huésped | RF-12 cotizar, RF-13 reservar |
| POST `/reservas/{id}/pagar` · PUT `/reservas/{id}/cancelar` · GET `/reservas/{id}/devolucion` | Huésped | RF-15 pagar, RF-17 cancelar |
| GET `/reservas/mias` | Huésped | RF-16 mis reservas |
| GET `/reservas/de-mis-publicaciones` · PUT `/reservas/{id}/aprobar` · `/reservas/{id}/rechazar` | Anfitrión | RF-14, RF-16 |
| GET `/reservas/todas` | Administrador | RF-16 todas las reservas |
| GET `/reservas/{id}/comprobante` | Huésped, Anfitrión | CU-30 comprobante PDF |
| GET `/reportes/indicadores?desde=&hasta=` | Administrador | RF-24 dashboard |
| GET `/reportes/exportar?tipo=&formato=&desde=&hasta=` | Administrador | RF-25 exportar PDF o Excel |

## Notas
- JWT de 30 minutos. Si vence, el interceptor cierra la sesión y pide iniciar sesión de nuevo.
- Las imágenes de ejemplo vienen de `picsum.photos`: necesitan internet.
- Registro de errores (RNF-15): los errores inesperados quedan en consola y en `backend/logs/aplicacion.log` con fecha, hora y `[modulo=usuarios|reportes|reservas]`.
- Si usa un JDK muy nuevo (probado con JDK 26 en Windows) y Tomcat falla con "Unable to establish loopback connection", cree una carpeta corta (por ejemplo `C:\jt`) y arranque con la variable `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:/jt`. Con JDK 17 no hace falta.

## Marca TuHogar
Logo en `frontend/public/logo.svg` (completo) y `logo-icon.svg` (solo el ícono, también es el favicon). Paleta (definida en `frontend/src/styles.css`):

| Color | Código | Uso |
|---|---|---|
| Terracota | `#D9653B` | botones, enlaces, ícono |
| Petróleo | `#1F4E5F` | barra de navegación y títulos |
| Crema | `#FFF6EC` | fondo |
| Ámbar | `#F2B84B` | acentos |
| Gris oscuro | `#2B2B2B` | texto |
