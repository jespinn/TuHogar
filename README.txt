TuHogar - Marketplace de alojamientos

Proyecto de Ingeniería de Software I, Universidad El Bosque.
Módulos: M01 Usuarios, M04 Reservas y M08 Reportes.
Backend: Java 17 y Spring Boot 3.5. Frontend: Angular 18. Base de datos: PostgreSQL 17 en Docker.

Requisitos

Docker Desktop abierto.
Para ejecutar el código sin contenedores también se necesita JDK 17, Maven 3.9 y Node 20.

Claves

El código no trae contraseñas ni secretos.
Antes de arrancar se crea en esta carpeta un archivo llamado .env con dos líneas:
DB_PASSWORD=una-clave-para-la-base
JWT_SECRETO=un-secreto-de-al-menos-32-caracteres
El archivo .env.example sirve de plantilla. El .env no se sube al repositorio.

Arrancar con Docker

En esta carpeta ejecutar:
docker compose up -d --build
Abrir http://localhost:4200 en el navegador.
La primera vez tarda unos minutos. La API queda en http://localhost:8080/api.
Para apagar sin borrar los datos:
docker compose down

Arrancar sin contenedores para el código

docker compose up -d db
cd backend
mvn spring-boot:run
En otra terminal:
cd frontend
npm install
npm start

Pruebas

Con la base encendida:
cd backend
mvn test

Usuarios de prueba

admin@alojamientos.co con Admin1234 (administrador)
carlos.anfitrion@correo.co con Clave1234 (anfitrión)
laura.anfitriona@correo.co con Clave1234 (anfitrión)
maria.huesped@correo.co con Clave1234 (huésped)
pedro.huesped@correo.co con Clave1234 (huésped)

Las contraseñas nuevas deben tener mínimo 8 caracteres con mayúsculas, minúsculas, números y símbolos, por ejemplo Clave123!
