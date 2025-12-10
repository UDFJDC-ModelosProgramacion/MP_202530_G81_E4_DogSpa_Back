-- Inserts de ejemplo para Services, Branches y Products
-- Ejecutar después de `schema.sql` (mismo servidor/base de datos)


-- Asegúrate de usar la misma base de datos creada por `schema.sql`
USE `dogspa`;


START TRANSACTION;


-- Sucursales (branches) - insertar sólo si no existen (por nombre)
INSERT INTO `branch_entity` (`name`, `address`, `phone`, `zone`)
SELECT 'Centro Canino', 'Calle 1 #10-20', '3101112222', 'Centro'
WHERE NOT EXISTS (SELECT 1 FROM `branch_entity` WHERE `name` = 'Centro Canino');


INSERT INTO `branch_entity` (`name`, `address`, `phone`, `zone`)
SELECT 'Norte Pets', 'Carrera 5 #45-30', '3103334444', 'Norte'
WHERE NOT EXISTS (SELECT 1 FROM `branch_entity` WHERE `name` = 'Norte Pets');


INSERT INTO `branch_entity` (`name`, `address`, `phone`, `zone`)
SELECT 'Sur Spa', 'Avenida Sur 100', '3105556666', 'Sur'
WHERE NOT EXISTS (SELECT 1 FROM `branch_entity` WHERE `name` = 'Sur Spa');


-- Servicios (services) - insertar sólo si no existen (por nombre)
INSERT INTO `service_entity` (`name`, `description`, `price`, `duration`)
SELECT 'Baño completo', 'Baño con shampoo y acondicionado, secado y cepillado', 20000.0, 45
WHERE NOT EXISTS (SELECT 1 FROM `service_entity` WHERE `name` = 'Baño completo');


INSERT INTO `service_entity` (`name`, `description`, `price`, `duration`)
SELECT 'Peluquería', 'Corte de pelo profesional según raza', 40000.0, 90
WHERE NOT EXISTS (SELECT 1 FROM `service_entity` WHERE `name` = 'Peluquería');


INSERT INTO `service_entity` (`name`, `description`, `price`, `duration`)
SELECT 'Corte de uñas', 'Corte y limado de uñas', 8000.0, 15
WHERE NOT EXISTS (SELECT 1 FROM `service_entity` WHERE `name` = 'Corte de uñas');


INSERT INTO `service_entity` (`name`, `description`, `price`, `duration`)
SELECT 'Limpieza de oídos', 'Limpieza y revisión de oídos', 10000.0, 20
WHERE NOT EXISTS (SELECT 1 FROM `service_entity` WHERE `name` = 'Limpieza de oídos');


INSERT INTO `service_entity` (`name`, `description`, `price`, `duration`)
SELECT 'Entrenamiento básico', 'Sesión de adiestramiento básico (obediencia)', 50000.0, 60
WHERE NOT EXISTS (SELECT 1 FROM `service_entity` WHERE `name` = 'Entrenamiento básico');


-- Productos (products) - insertar sólo si no existen (por nombre)
INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Shampoo para perros', 'Higiene', 'Shampoo suave pH balanceado para perros', 15000.0, 50
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Shampoo para perros');


INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Cepillo para pelo', 'Accesorios', 'Cepillo de cerdas para pelo medio y largo', 12000.0, 30
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Cepillo para pelo');


INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Collar ajustable', 'Accesorios', 'Collar nylon ajustable con hebilla', 8000.0, 100
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Collar ajustable');


INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Alimento balanceado 5kg', 'Alimentos', 'Alimento completo para perros adultos', 80000.0, 20
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Alimento balanceado 5kg');


INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Juguete de cuerda', 'Juguetes', 'Juguete resistente para morder', 7000.0, 60
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Juguete de cuerda');


INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Medicamento antipulgas', 'Salud', 'Tratamiento antipulgas (1 dosis)', 25000.0, 25
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Medicamento antipulgas');


-- Relacionar services con branches (service_branch)
-- En lugar de asumir ids, buscamos los ids por nombre para crear las relaciones
-- Detección dinámica de nombres de columna en la tabla de relación
-- Para evitar errores si Hibernate generó nombres diferentes, buscamos las columnas
SELECT COLUMN_NAME INTO @col_service
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'service_entity_branches'
    AND COLUMN_NAME IN ('service_entity_id','service_id','services_id','serviceentity_id','service_id')
    LIMIT 1;


SELECT COLUMN_NAME INTO @col_branch
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'service_entity_branches'
    AND COLUMN_NAME IN ('branches_id','branch_id','branchentity_id','branch_entity_id','branches_id')
    AND COLUMN_NAME <> @col_service
    LIMIT 1;


SET @do = IF(@col_service IS NULL OR @col_branch IS NULL, 0, 1);


-- Centro Canino ofrece todos los servicios
SET @sname = 'Baño completo'; SET @bname = 'Centro Canino';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


SET @sname = 'Peluquería'; SET @bname = 'Centro Canino';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


SET @sname = 'Corte de uñas'; SET @bname = 'Centro Canino';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


SET @sname = 'Limpieza de oídos'; SET @bname = 'Centro Canino';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


SET @sname = 'Entrenamiento básico'; SET @bname = 'Centro Canino';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- Norte Pets ofrece Baño completo, Corte de uñas, Limpieza de oídos
SET @sname = 'Baño completo'; SET @bname = 'Norte Pets';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


SET @sname = 'Corte de uñas'; SET @bname = 'Norte Pets';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


SET @sname = 'Limpieza de oídos'; SET @bname = 'Norte Pets';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- Sur Spa ofrece Baño completo, Peluquería, Corte de uñas
SET @sname = 'Baño completo'; SET @bname = 'Sur Spa';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


SET @sname = 'Peluquería'; SET @bname = 'Sur Spa';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


SET @sname = 'Corte de uñas'; SET @bname = 'Sur Spa';
SET @sql = IF(@do=0, 'SELECT "SKIP"', CONCAT(
    'INSERT INTO `service_entity_branches` (`', @col_service, '`, `', @col_branch, '`) '
    ,'SELECT s.id, b.id FROM `service_entity` s, `branch_entity` b '
    ,'WHERE s.name = ', QUOTE(@sname), ' AND b.name = ', QUOTE(@bname)
    ,' AND NOT EXISTS (SELECT 1 FROM `service_entity_branches` sb WHERE sb.`', @col_service, '` = s.id AND sb.`', @col_branch, '` = b.id)'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ===== Agregados adicionales: más sucursales, servicios, productos y multimedia (imágenes) =====


-- Sucursales adicionales
INSERT INTO `branch_entity` (`name`, `address`, `phone`, `zone`)
SELECT 'Este PetCare', 'Transversal 12 #78-90', '3107778888', 'Este'
WHERE NOT EXISTS (SELECT 1 FROM `branch_entity` WHERE `name` = 'Este PetCare');


INSERT INTO `branch_entity` (`name`, `address`, `phone`, `zone`)
SELECT 'Occidente Pets', 'Calle 140 #20-10', '3109990000', 'Occidente'
WHERE NOT EXISTS (SELECT 1 FROM `branch_entity` WHERE `name` = 'Occidente Pets');


INSERT INTO `branch_entity` (`name`, `address`, `phone`, `zone`)
SELECT 'Veterinaria Central', 'Avenida Principal 200', '3101212121', 'Centro'
WHERE NOT EXISTS (SELECT 1 FROM `branch_entity` WHERE `name` = 'Veterinaria Central');


-- Servicios adicionales
INSERT INTO `service_entity` (`name`, `description`, `price`, `duration`)
SELECT 'Spa deluxe', 'Tratamiento completo: baño, cepillado, acondicionador y fragancia', 60000.0, 120
WHERE NOT EXISTS (SELECT 1 FROM `service_entity` WHERE `name` = 'Spa deluxe');


INSERT INTO `service_entity` (`name`, `description`, `price`, `duration`)
SELECT 'Guardería día', 'Cuidado diurno con juego y supervisión', 30000.0, 480
WHERE NOT EXISTS (SELECT 1 FROM `service_entity` WHERE `name` = 'Guardería día');


INSERT INTO `service_entity` (`name`, `description`, `price`, `duration`)
SELECT 'Baño medicado', 'Baño con producto antiparasitario recetado por el veterinario', 35000.0, 60
WHERE NOT EXISTS (SELECT 1 FROM `service_entity` WHERE `name` = 'Baño medicado');


INSERT INTO `service_entity` (`name`, `description`, `price`, `duration`)
SELECT 'Corte y estilizado', 'Corte estético y estilizado según raza y preferencias', 50000.0, 90
WHERE NOT EXISTS (SELECT 1 FROM `service_entity` WHERE `name` = 'Corte y estilizado');


-- Productos adicionales
INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Acondicionador para perros', 'Higiene', 'Acondicionador nutritivo para pelo suave y brillante', 17000.0, 40
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Acondicionador para perros');


INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Correa retráctil', 'Accesorios', 'Correa retráctil 5m resistente', 35000.0, 25
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Correa retráctil');


INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Snack dental', 'Alimentos', 'Snack masticable para higiene dental', 5000.0, 120
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Snack dental');


INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Bolsa de alimento 10kg', 'Alimentos', 'Alimento completo para perros adultos - 10kg', 150000.0, 10
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Bolsa de alimento 10kg');


INSERT INTO `product_entity` (`name`, `category`, `description`, `price`, `stock`)
SELECT 'Juguete interactivo', 'Juguetes', 'Juguete con dispensador de snacks para entretenimiento', 22000.0, 35
WHERE NOT EXISTS (SELECT 1 FROM `product_entity` WHERE `name` = 'Juguete interactivo');


-- ===== MULTIMEDIA: IMÁGENES PARA BRANCHES =====

INSERT INTO `multimedia_entity` (`type`, `url`, `branch_id`)
SELECT 'IMAGE', 'https://fast.com.co/blog/wp-content/uploads/2024/04/papelmatic-higiene-profesional-limpieza-desinfeccion-clinicas-veterinarias.jpg', b.id
FROM `branch_entity` b
WHERE b.name = 'Centro Canino'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://fast.com.co/blog/wp-content/uploads/2024/04/papelmatic-higiene-profesional-limpieza-desinfeccion-clinicas-veterinarias.jpg' AND m.`branch_id` = b.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `branch_id`)
SELECT 'IMAGE', 'https://cmspreprod.lasalle.edu.co/sites/default/files/styles/original/public/2025-02/5.png.webp?itok=PFai9-cd', b.id
FROM `branch_entity` b
WHERE b.name = 'Norte Pets'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://cmspreprod.lasalle.edu.co/sites/default/files/styles/original/public/2025-02/5.png.webp?itok=PFai9-cd' AND m.`branch_id` = b.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `branch_id`)
SELECT 'IMAGE', 'https://www.plazasatelite.com.co/images/products/mac_jaimerodriguez_1674508380_ELoBM.jpg', b.id
FROM `branch_entity` b
WHERE b.name = 'Sur Spa'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://www.plazasatelite.com.co/images/products/mac_jaimerodriguez_1674508380_ELoBM.jpg' AND m.`branch_id` = b.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `branch_id`)
SELECT 'IMAGE', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSREEeEzc5en9Xh-a5yNvqBO5FVhWp14eqVXg&s', b.id
FROM `branch_entity` b
WHERE b.name = 'Este PetCare'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSREEeEzc5en9Xh-a5yNvqBO5FVhWp14eqVXg&s' AND m.`branch_id` = b.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `branch_id`)
SELECT 'IMAGE', 'https://www.veterinariaoccidente.com/images/clinica-veterinaria-occidente-ribadeo-001.jpg', b.id
FROM `branch_entity` b
WHERE b.name = 'Occidente Pets'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://www.veterinariaoccidente.com/images/clinica-veterinaria-occidente-ribadeo-001.jpg' AND m.`branch_id` = b.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `branch_id`)
SELECT 'IMAGE', 'https://www.abanimalclinicaveterinaria.com/wp-content/uploads/2020/09/home-page-.jpg', b.id
FROM `branch_entity` b
WHERE b.name = 'Veterinaria Central'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://www.abanimalclinicaveterinaria.com/wp-content/uploads/2020/09/home-page-.jpg' AND m.`branch_id` = b.id);


-- ===== MULTIMEDIA: IMÁGENES PARA SERVICES =====

INSERT INTO `multimedia_entity` (`type`, `url`, `service_id`)
SELECT 'IMAGE', 'https://img.freepik.com/fotos-premium/perro-gracioso-bano-zapatilla-cabeza-mascota-toma-ducha-muestra-su-enfoque-suave-lengua_228441-316.jpg?semt=ais_se_enriched&w=740&q=80', s.id
FROM `service_entity` s
WHERE s.name = 'Baño completo'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://img.freepik.com/fotos-premium/perro-gracioso-bano-zapatilla-cabeza-mascota-toma-ducha-muestra-su-enfoque-suave-lengua_228441-316.jpg?semt=ais_se_enriched&w=740&q=80' AND m.`service_id` = s.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `service_id`)
SELECT 'IMAGE', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRrrAtcLTQff8ifBjM6fJEUhf5BPshcmP7Y9w&s', s.id
FROM `service_entity` s
WHERE s.name = 'Peluquería'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRrrAtcLTQff8ifBjM6fJEUhf5BPshcmP7Y9w&s' AND m.`service_id` = s.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `service_id`)
SELECT 'IMAGE', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRcIexSvx1krIEFuhcaqy0fFSmJJGaFVIrA2A&s', s.id
FROM `service_entity` s
WHERE s.name = 'Corte de uñas'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRcIexSvx1krIEFuhcaqy0fFSmJJGaFVIrA2A&s' AND m.`service_id` = s.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `service_id`)
SELECT 'IMAGE', 'https://www.zooplus.es/magazine/wp-content/uploads/2021/01/C%C3%B3mo-limpiar-las-orejas-a-un-perro-1.webp', s.id
FROM `service_entity` s
WHERE s.name = 'Limpieza de oídos'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://www.zooplus.es/magazine/wp-content/uploads/2021/01/C%C3%B3mo-limpiar-las-orejas-a-un-perro-1.webp' AND m.`service_id` = s.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `service_id`)
SELECT 'IMAGE', 'https://grupobravel.es/wp-content/uploads/2023/04/adiestrar-a-un-perro.jpg', s.id
FROM `service_entity` s
WHERE s.name = 'Entrenamiento básico'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://grupobravel.es/wp-content/uploads/2023/04/adiestrar-a-un-perro.jpg' AND m.`service_id` = s.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `service_id`)
SELECT 'IMAGE', 'https://getslucky.co/wp-content/uploads/2021/06/spa-para-tu-perro.jpg', s.id
FROM `service_entity` s
WHERE s.name = 'Spa deluxe'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://getslucky.co/wp-content/uploads/2021/06/spa-para-tu-perro.jpg' AND m.`service_id` = s.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `service_id`)
SELECT 'IMAGE', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ0iXUowBs7K8KuXL3bwj2LYF3vXSsDwtKbfA&s', s.id
FROM `service_entity` s
WHERE s.name = 'Guardería día'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ0iXUowBs7K8KuXL3bwj2LYF3vXSsDwtKbfA&s' AND m.`service_id` = s.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `service_id`)
SELECT 'IMAGE', 'https://purina.com.co/sites/default/files/2024-03/como-banar-un-perro-ar.jpg', s.id
FROM `service_entity` s
WHERE s.name = 'Baño medicado'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://purina.com.co/sites/default/files/2024-03/como-banar-un-perro-ar.jpg' AND m.`service_id` = s.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `service_id`)
SELECT 'IMAGE', 'https://cdn.shopify.com/s/files/1/0552/3750/9303/files/corte_pelo_shih_Tzu_600x600.jpg?v=1736509565', s.id
FROM `service_entity` s
WHERE s.name = 'Corte y estilizado'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://cdn.shopify.com/s/files/1/0552/3750/9303/files/corte_pelo_shih_Tzu_600x600.jpg?v=1736509565' AND m.`service_id` = s.id);


-- ===== MULTIMEDIA: IMÁGENES PARA PRODUCTS =====

INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://media.falabella.com/sodimacCO/509380_1/w=1500,h=1500,fit=pad', p.id
FROM `product_entity` p
WHERE p.name = 'Shampoo para perros'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://media.falabella.com/sodimacCO/509380_1/w=1500,h=1500,fit=pad' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://http2.mlstatic.com/D_NQ_NP_975931-CBT82019535931_012025-O.webp', p.id
FROM `product_entity` p
WHERE p.name = 'Acondicionador para perros'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://http2.mlstatic.com/D_NQ_NP_975931-CBT82019535931_012025-O.webp' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://exitocol.vtexassets.com/arquivos/ids/28790040/collar-para-perro-de-nylon-bandas-reflejantes-verde-lima.jpg?v=638852731555900000', p.id
FROM `product_entity` p
WHERE p.name = 'Collar ajustable'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://exitocol.vtexassets.com/arquivos/ids/28790040/collar-para-perro-de-nylon-bandas-reflejantes-verde-lima.jpg?v=638852731555900000' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://http2.mlstatic.com/D_NQ_NP_914837-MLU78132058630_082024-O.webp', p.id
FROM `product_entity` p
WHERE p.name = 'Alimento balanceado 5kg'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://http2.mlstatic.com/D_NQ_NP_914837-MLU78132058630_082024-O.webp' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://www.tierragro.com/cdn/shop/products/juguete6.jpg?v=1734971632&width=1200', p.id
FROM `product_entity` p
WHERE p.name = 'Juguete de cuerda'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://www.tierragro.com/cdn/shop/products/juguete6.jpg?v=1734971632&width=1200' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://www.agrocampo.com.co/media/catalog/product/cache/d51e0dc10c379a6229d70d752fc46d83/5/1/5151151105110440004-v3-min.jpg', p.id
FROM `product_entity` p
WHERE p.name = 'Medicamento antipulgas'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://www.agrocampo.com.co/media/catalog/product/cache/d51e0dc10c379a6229d70d752fc46d83/5/1/5151151105110440004-v3-min.jpg' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://ceba.com.co/cdn/shop/products/WhatsAppImage2022-06-30at1.04.27PM.jpg?v=1738849300', p.id
FROM `product_entity` p
WHERE p.name = 'Cepillo para pelo'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://ceba.com.co/cdn/shop/products/WhatsAppImage2022-06-30at1.04.27PM.jpg?v=1738849300' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTRxXidqCPaIyi4v6iztduDU2Q7fm0QNebepg&s', p.id
FROM `product_entity` p
WHERE p.name = 'Correa retráctil'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTRxXidqCPaIyi4v6iztduDU2Q7fm0QNebepg&s' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://ceba.com.co/cdn/shop/files/CHUNKY-Delicaprichosperro.jpg?v=1741628071', p.id
FROM `product_entity` p
WHERE p.name = 'Snack dental'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://ceba.com.co/cdn/shop/files/CHUNKY-Delicaprichosperro.jpg?v=1741628071' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRIMlsEQ3cyquD1D-1SKtLmNY8iY3DU4s5y0g&s', p.id
FROM `product_entity` p
WHERE p.name = 'Bolsa de alimento 10kg'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRIMlsEQ3cyquD1D-1SKtLmNY8iY3DU4s5y0g&s' AND m.`product_id` = p.id);


INSERT INTO `multimedia_entity` (`type`, `url`, `product_id`)
SELECT 'IMAGE', 'https://www.tierragro.com/cdn/shop/files/pelotasnakrojo2_1024x1024_8787d808-a95c-415e-8516-6114d70a1589.webp?v=1730558949', p.id
FROM `product_entity` p
WHERE p.name = 'Juguete interactivo'
    AND NOT EXISTS (SELECT 1 FROM `multimedia_entity` m WHERE m.`url` = 'https://www.tierragro.com/cdn/shop/files/pelotasnakrojo2_1024x1024_8787d808-a95c-415e-8516-6114d70a1589.webp?v=1730558949' AND m.`product_id` = p.id);



-- ===== USUARIOS: ADMIN =====
INSERT INTO `person_entity` (`dtype`, `name`, `lastname`, `email`, `password`, `address`, `telephone`, `loyaltypoints`)
SELECT 'AdminEntity', 'Administrador', 'DogSpa', 'admin@gmail.com', 'admin123', 'Sede Central', '3000000000', NULL
WHERE NOT EXISTS (SELECT 1 FROM `person_entity` WHERE `email` = 'admin@gmail.com');


COMMIT;


-- Fin de inserts (robustos frente a tablas creadas por Hibernate)