-- datos de ejemplo
INSERT INTO rol (nombre) VALUES ('HUESPED');
INSERT INTO rol (nombre) VALUES ('ANFITRION');
INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR');

INSERT INTO usuario (nombre, correo, contrasena_hash, tipo_documento, numero_documento, acepta_tratamiento_datos, estado, fecha_registro) VALUES
('Ana Administradora', 'admin@alojamientos.co', 'PENDIENTE', 'CC', '1000000001', TRUE, 'ACTIVA', CURRENT_TIMESTAMP),
('Carlos Ramírez', 'carlos.anfitrion@correo.co', 'PENDIENTE', 'CC', '1000000002', TRUE, 'ACTIVA', CURRENT_TIMESTAMP),
('Laura Gómez', 'laura.anfitriona@correo.co', 'PENDIENTE', 'CC', '1000000003', TRUE, 'ACTIVA', CURRENT_TIMESTAMP),
('María Torres', 'maria.huesped@correo.co', 'PENDIENTE', 'CC', '1000000004', TRUE, 'ACTIVA', CURRENT_TIMESTAMP),
('Pedro Díaz', 'pedro.huesped@correo.co', 'PENDIENTE', 'CE', '1000000005', TRUE, 'ACTIVA', CURRENT_TIMESTAMP);

INSERT INTO usuario_rol (id_usuario, id_rol)
SELECT u.id_usuario, r.id_rol FROM usuario u, rol r
WHERE (u.correo = 'admin@alojamientos.co' AND r.nombre = 'ADMINISTRADOR')
   OR (u.correo IN ('carlos.anfitrion@correo.co', 'laura.anfitriona@correo.co') AND r.nombre = 'ANFITRION')
   OR (u.correo IN ('maria.huesped@correo.co', 'pedro.huesped@correo.co') AND r.nombre = 'HUESPED');

-- publicaciones activas
INSERT INTO publicacion (id_anfitrion, titulo, descripcion, ciudad, direccion, tipo_alojamiento, capacidad, precio_noche, tarifa_limpieza, reglas, modo_reserva, estado)
SELECT u.id_usuario, p.titulo, p.descripcion, p.ciudad, p.direccion, p.tipo, p.capacidad, p.precio, p.limpieza, p.reglas, p.modo, p.estado
FROM usuario u JOIN (
    SELECT 'carlos.anfitrion@correo.co' AS correo, 'Apartamento moderno en Chapinero' AS titulo, 'Apartamento luminoso cerca de restaurantes y transporte.' AS descripcion, 'Bogotá' AS ciudad, 'Calle 60 #7-20' AS direccion, 'Apartamento' AS tipo, 4 AS capacidad, 180000 AS precio, 30000 AS limpieza, 'No fumar. No fiestas.' AS reglas, 'INMEDIATA' AS modo, 'ACTIVA' AS estado
    UNION ALL SELECT 'carlos.anfitrion@correo.co', 'Habitación privada en La Candelaria', 'Habitación cómoda en el centro histórico.', 'Bogotá', 'Carrera 3 #12-15', 'Habitación', 2, 90000, 15000, 'Silencio después de las 10 pm.', 'CON_APROBACION', 'ACTIVA'
    UNION ALL SELECT 'carlos.anfitrion@correo.co', 'Casa familiar en El Poblado', 'Casa amplia con terraza y zona BBQ.', 'Medellín', 'Calle 10 #35-40', 'Casa', 8, 420000, 60000, 'Mascotas bajo consulta.', 'CON_APROBACION', 'ACTIVA'
    UNION ALL SELECT 'carlos.anfitrion@correo.co', 'Apartamento con vista en Laureles', 'Apartamento tranquilo con balcón.', 'Medellín', 'Circular 4 #70-12', 'Apartamento', 3, 150000, 25000, 'No fumar.', 'INMEDIATA', 'ACTIVA'
    UNION ALL SELECT 'carlos.anfitrion@correo.co', 'Hotel boutique Ciudad Amurallada', 'Habitación de hotel con desayuno incluido.', 'Cartagena', 'Calle del Arsenal #8-10', 'Hotel', 2, 380000, 0, 'Check-in desde las 3 pm.', 'INMEDIATA', 'ACTIVA'
    UNION ALL SELECT 'carlos.anfitrion@correo.co', 'Apartamento frente al mar', 'Apartamento en Bocagrande con vista al mar.', 'Cartagena', 'Av. San Martín #5-30', 'Apartamento', 5, 350000, 50000, 'No fiestas.', 'INMEDIATA', 'ACTIVA'
    UNION ALL SELECT 'laura.anfitriona@correo.co', 'Casa colonial en San Antonio', 'Casa tradicional con patio interior.', 'Cali', 'Carrera 5 #2-18', 'Casa', 6, 260000, 40000, 'No fumar dentro de la casa.', 'CON_APROBACION', 'ACTIVA'
    UNION ALL SELECT 'laura.anfitriona@correo.co', 'Habitación económica en Granada', 'Habitación sencilla cerca de la zona de restaurantes.', 'Cali', 'Av. 9N #15-20', 'Habitación', 2, 60000, 10000, 'Máximo 2 personas.', 'INMEDIATA', 'ACTIVA'
    UNION ALL SELECT 'laura.anfitriona@correo.co', 'Cabaña cerca de la playa', 'Cabaña con hamacas a 5 minutos del mar.', 'Santa Marta', 'Vía Taganga km 2', 'Cabaña', 4, 230000, 35000, 'Mascotas permitidas.', 'INMEDIATA', 'ACTIVA'
    UNION ALL SELECT 'laura.anfitriona@correo.co', 'Hotel familiar en El Rodadero', 'Hotel con piscina y desayuno.', 'Santa Marta', 'Carrera 2 #8-45', 'Hotel', 4, 300000, 0, 'Niños bienvenidos.', 'INMEDIATA', 'ACTIVA'
    UNION ALL SELECT 'laura.anfitriona@correo.co', 'Finca campestre en Girón', 'Finca con piscina y zona verde.', 'Bucaramanga', 'Vereda El Palmar', 'Finca', 10, 550000, 80000, 'Se permiten eventos pequeños.', 'CON_APROBACION', 'ACTIVA'
    UNION ALL SELECT 'laura.anfitriona@correo.co', 'Apartamento ejecutivo en Cabecera', 'Apartamento amoblado ideal para viajes de trabajo.', 'Bucaramanga', 'Calle 52 #31-10', 'Apartamento', 2, 130000, 20000, 'No fumar.', 'INMEDIATA', 'ACTIVA'
    UNION ALL SELECT 'carlos.anfitrion@correo.co', 'Casa en construcción (borrador)', 'Aún sin terminar de describir.', 'Bogotá', 'Calle 100 #15-20', 'Casa', 6, 200000, 30000, NULL, 'INMEDIATA', 'BORRADOR'
    UNION ALL SELECT 'laura.anfitriona@correo.co', 'Cabaña sin fotos (borrador)', 'Pendiente de agregar imágenes.', 'Cali', 'Km 18 vía al mar', 'Cabaña', 4, 170000, 20000, NULL, 'CON_APROBACION', 'BORRADOR'
    UNION ALL SELECT 'carlos.anfitrion@correo.co', 'Apartamento bloqueado por el administrador', 'Publicación bloqueada por incumplir las normas.', 'Medellín', 'Calle 33 #80-10', 'Apartamento', 3, 140000, 20000, 'No aplica.', 'INMEDIATA', 'BLOQUEADA'
) p ON p.correo = u.correo;

-- imágenes
INSERT INTO imagen_publicacion (id_publicacion, url, orden) SELECT id_publicacion, 'https://picsum.photos/seed/aloj' || id_publicacion || 'a/800/500', 1 FROM publicacion;
INSERT INTO imagen_publicacion (id_publicacion, url, orden) SELECT id_publicacion, 'https://picsum.photos/seed/aloj' || id_publicacion || 'b/800/500', 2 FROM publicacion;

-- servicios adicionales
INSERT INTO servicio_adicional (id_publicacion, nombre, costo) SELECT id_publicacion, 'WiFi', 0 FROM publicacion;
INSERT INTO servicio_adicional (id_publicacion, nombre, costo) SELECT id_publicacion, 'Desayuno', 15000 FROM publicacion WHERE MOD(id_publicacion, 2) = 0;
INSERT INTO servicio_adicional (id_publicacion, nombre, costo) SELECT id_publicacion, 'Parqueadero', 10000 FROM publicacion WHERE MOD(id_publicacion, 3) = 0;
INSERT INTO servicio_adicional (id_publicacion, nombre, costo) SELECT id_publicacion, 'Aire acondicionado', 20000 FROM publicacion WHERE ciudad IN ('Cartagena', 'Santa Marta', 'Cali');

-- políticas de cancelación
INSERT INTO politica_cancelacion (tipo, dias_anticipacion, porcentaje_devolucion) VALUES ('FLEXIBLE', 1, 100);
INSERT INTO politica_cancelacion (tipo, dias_anticipacion, porcentaje_devolucion) VALUES ('MODERADA', 5, 50);
INSERT INTO politica_cancelacion (tipo, dias_anticipacion, porcentaje_devolucion) VALUES ('ESTRICTA', 14, 30);
UPDATE publicacion SET id_politica = (SELECT id_politica FROM politica_cancelacion WHERE tipo = 'FLEXIBLE') WHERE MOD(id_publicacion, 3) = 0;
UPDATE publicacion SET id_politica = (SELECT id_politica FROM politica_cancelacion WHERE tipo = 'MODERADA') WHERE MOD(id_publicacion, 3) = 1;
UPDATE publicacion SET id_politica = (SELECT id_politica FROM politica_cancelacion WHERE tipo = 'ESTRICTA') WHERE MOD(id_publicacion, 3) = 2;

-- reservas de ejemplo
INSERT INTO reserva (id_huesped, id_publicacion, fecha_llegada, fecha_salida, numero_huespedes, tarifa_base, cargo_limpieza, cargo_servicio, descuentos, total, clave_idempotencia, estado, fecha_creacion)
SELECT h.id_usuario, p.id_publicacion, r.llegada, r.salida, r.huespedes, r.base, r.limpieza, r.cargo, r.descuento, r.total, r.clave, r.estado, CAST(CURRENT_DATE - r.creada AS TIMESTAMP)
FROM (
    SELECT 'maria.huesped@correo.co' AS correo, 'Apartamento moderno en Chapinero' AS titulo, CURRENT_DATE + 10 AS llegada, CURRENT_DATE + 13 AS salida, 2 AS huespedes, 540000 AS base, 30000 AS limpieza, 54000 AS cargo, 0 AS descuento, 624000 AS total, 'demo-1' AS clave, 'CONFIRMADA' AS estado, 15 AS creada
    UNION ALL SELECT 'maria.huesped@correo.co', 'Casa familiar en El Poblado', CURRENT_DATE + 20, CURRENT_DATE + 22, 4, 840000, 60000, 84000, 0, 984000, 'demo-2', 'PENDIENTE_APROBACION', 5
    UNION ALL SELECT 'pedro.huesped@correo.co', 'Apartamento frente al mar', CURRENT_DATE + 15, CURRENT_DATE + 23, 3, 2800000, 50000, 280000, 280000, 2850000, 'demo-3', 'PENDIENTE_PAGO', 0
    UNION ALL SELECT 'pedro.huesped@correo.co', 'Habitación privada en La Candelaria', CURRENT_DATE + 30, CURRENT_DATE + 32, 2, 180000, 15000, 18000, 0, 213000, 'demo-4', 'RECHAZADA', 8
    UNION ALL SELECT 'maria.huesped@correo.co', 'Cabaña cerca de la playa', CURRENT_DATE - 20, CURRENT_DATE - 17, 2, 690000, 35000, 69000, 0, 804000, 'demo-5', 'COMPLETADA', 35
    UNION ALL SELECT 'pedro.huesped@correo.co', 'Hotel boutique Ciudad Amurallada', CURRENT_DATE + 40, CURRENT_DATE + 42, 2, 760000, 0, 76000, 0, 836000, 'demo-6', 'CANCELADA', 10
) r
JOIN usuario h ON h.correo = r.correo
JOIN publicacion p ON p.titulo = r.titulo;

-- más reservas
INSERT INTO reserva (id_huesped, id_publicacion, fecha_llegada, fecha_salida, numero_huespedes, tarifa_base, cargo_limpieza, cargo_servicio, descuentos, total, clave_idempotencia, estado, fecha_creacion)
SELECT h.id_usuario, p.id_publicacion, r.llegada, r.salida, r.huespedes, r.base, r.limpieza, r.cargo, r.descuento, r.total, r.clave, r.estado, CAST(CURRENT_DATE - r.creada AS TIMESTAMP)
FROM (
    SELECT 'pedro.huesped@correo.co' AS correo, 'Hotel familiar en El Rodadero' AS titulo, CURRENT_DATE - 60 AS llegada, CURRENT_DATE - 57 AS salida, 3 AS huespedes, 900000 AS base, 0 AS limpieza, 90000 AS cargo, 0 AS descuento, 990000 AS total, 'demo-7' AS clave, 'COMPLETADA' AS estado, 75 AS creada
    UNION ALL SELECT 'maria.huesped@correo.co', 'Finca campestre en Girón', CURRENT_DATE - 45, CURRENT_DATE - 43, 6, 1100000, 80000, 110000, 0, 1290000, 'demo-8', 'COMPLETADA', 52
    UNION ALL SELECT 'pedro.huesped@correo.co', 'Apartamento ejecutivo en Cabecera', CURRENT_DATE - 25, CURRENT_DATE - 22, 1, 390000, 20000, 39000, 0, 449000, 'demo-9', 'COMPLETADA', 28
    UNION ALL SELECT 'maria.huesped@correo.co', 'Habitación económica en Granada', CURRENT_DATE - 12, CURRENT_DATE - 10, 1, 120000, 10000, 12000, 0, 142000, 'demo-10', 'COMPLETADA', 20
    UNION ALL SELECT 'pedro.huesped@correo.co', 'Casa familiar en El Poblado', CURRENT_DATE - 8, CURRENT_DATE - 5, 5, 1260000, 60000, 126000, 0, 1446000, 'demo-11', 'COMPLETADA', 12
    UNION ALL SELECT 'maria.huesped@correo.co', 'Apartamento frente al mar', CURRENT_DATE + 50, CURRENT_DATE + 53, 4, 1050000, 50000, 105000, 0, 1205000, 'demo-12', 'CONFIRMADA', 6
    UNION ALL SELECT 'pedro.huesped@correo.co', 'Hotel boutique Ciudad Amurallada', CURRENT_DATE + 60, CURRENT_DATE + 62, 2, 760000, 0, 76000, 0, 836000, 'demo-13', 'CONFIRMADA', 4
    UNION ALL SELECT 'maria.huesped@correo.co', 'Cabaña cerca de la playa', CURRENT_DATE + 45, CURRENT_DATE + 52, 3, 1610000, 35000, 161000, 161000, 1645000, 'demo-14', 'PENDIENTE_PAGO', 2
    UNION ALL SELECT 'pedro.huesped@correo.co', 'Apartamento moderno en Chapinero', CURRENT_DATE + 70, CURRENT_DATE + 72, 2, 360000, 30000, 36000, 0, 426000, 'demo-15', 'CONFIRMADA', 1
    UNION ALL SELECT 'maria.huesped@correo.co', 'Hotel familiar en El Rodadero', CURRENT_DATE + 80, CURRENT_DATE + 82, 2, 600000, 0, 60000, 0, 660000, 'demo-16', 'PENDIENTE_PAGO', 0
    UNION ALL SELECT 'pedro.huesped@correo.co', 'Finca campestre en Girón', CURRENT_DATE + 90, CURRENT_DATE + 92, 8, 1100000, 80000, 110000, 0, 1290000, 'demo-17', 'PENDIENTE_APROBACION', 0
    UNION ALL SELECT 'maria.huesped@correo.co', 'Habitación privada en La Candelaria', CURRENT_DATE + 35, CURRENT_DATE + 36, 1, 90000, 15000, 9000, 0, 114000, 'demo-18', 'RECHAZADA', 3
    UNION ALL SELECT 'pedro.huesped@correo.co', 'Apartamento ejecutivo en Cabecera', CURRENT_DATE + 25, CURRENT_DATE + 27, 2, 260000, 20000, 26000, 0, 306000, 'demo-19', 'CANCELADA', 9
) r
JOIN usuario h ON h.correo = r.correo
JOIN publicacion p ON p.titulo = r.titulo;

-- servicio usado
INSERT INTO reserva_servicio (id_reserva, id_servicio, costo_aplicado)
SELECT r.id_reserva, s.id_servicio, 10000 FROM reserva r, servicio_adicional s
WHERE r.clave_idempotencia = 'demo-5' AND s.id_publicacion = r.id_publicacion AND s.nombre = 'Parqueadero';

-- pagos aprobados
INSERT INTO pago (id_reserva, monto, referencia_pasarela, estado, fecha)
SELECT id_reserva, total, 'SIM-DEMO' || id_reserva || '-4242', 'APROBADO', fecha_creacion
FROM reserva WHERE estado IN ('CONFIRMADA', 'COMPLETADA') OR clave_idempotencia = 'demo-6';

-- cancelación demo-6
INSERT INTO cancelacion (id_reserva, id_usuario, motivo, valor_devolucion, estado_final, fecha)
SELECT id_reserva, id_huesped, 'Cambio de planes', 250800, 'CANCELADA', CURRENT_TIMESTAMP
FROM reserva WHERE clave_idempotencia = 'demo-6';

-- cancelación demo-19
INSERT INTO cancelacion (id_reserva, id_usuario, motivo, valor_devolucion, estado_final, fecha)
SELECT id_reserva, id_huesped, 'Ya no viaja', 0, 'CANCELADA', CURRENT_TIMESTAMP
FROM reserva WHERE clave_idempotencia = 'demo-19';
