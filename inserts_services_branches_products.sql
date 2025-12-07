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
-- Relacionar services con branches (service_branch)
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

-- Helper para ejecutar un INSERT dinámico por par (service name, branch name)
-- Usamos QUOTE() para escapar los literales
-- Si no se detectaron las columnas, la consulta ejecutará 'SELECT "SKIP"' y no fallará

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

COMMIT;

-- Fin de inserts (robustos frente a tablas creadas por Hibernate)
