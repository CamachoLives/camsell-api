-- Configuracion inicial de la plataforma. Todo lo de aqui es administrable
-- despues desde la consola de parametros; son valores de arranque, no fijos.

INSERT INTO parameter_group
    (code, name, description, data_type, scope, multi_valued, filterable, required_for_listing, show_in_specs, sort_order, active, created_at, updated_at)
VALUES
    ('CONDITION', 'Estado', 'Condicion general del vehiculo', 'OPTION', 'LISTING', FALSE, TRUE, TRUE, TRUE, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BODY_TYPE', 'Carroceria', 'Tipo de carroceria', 'OPTION', 'LISTING', FALSE, TRUE, TRUE, TRUE, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FUEL_TYPE', 'Combustible', 'Tipo de combustible o propulsion', 'OPTION', 'LISTING', FALSE, TRUE, TRUE, TRUE, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TRANSMISSION', 'Transmision', 'Caja de cambios', 'OPTION', 'LISTING', FALSE, TRUE, TRUE, TRUE, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DRIVETRAIN', 'Traccion', 'Sistema de traccion', 'OPTION', 'LISTING', FALSE, TRUE, FALSE, TRUE, 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('COLOR', 'Color', 'Color exterior', 'OPTION', 'LISTING', FALSE, TRUE, TRUE, TRUE, 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'Ciudad', 'Ciudad donde se encuentra el vehiculo', 'OPTION', 'GENERAL', FALSE, TRUE, FALSE, TRUE, 70, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'Equipamiento', 'Accesorios y asistencias del vehiculo', 'OPTION', 'LISTING', TRUE, TRUE, FALSE, TRUE, 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DOORS', 'Puertas', 'Numero de puertas', 'NUMBER', 'LISTING', FALSE, TRUE, FALSE, TRUE, 90, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ENGINE_DISPLACEMENT', 'Cilindrada', 'Cilindrada del motor en centimetros cubicos', 'NUMBER', 'LISTING', FALSE, FALSE, FALSE, TRUE, 100, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HORSEPOWER', 'Potencia', 'Potencia en caballos de fuerza', 'NUMBER', 'LISTING', FALSE, FALSE, FALSE, TRUE, 110, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('WARRANTY', 'Garantia vigente', 'Indica si el vehiculo conserva garantia de fabrica', 'BOOLEAN', 'LISTING', FALSE, TRUE, FALSE, TRUE, 120, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SINGLE_OWNER', 'Unico dueno', 'El vehiculo ha tenido un solo propietario', 'BOOLEAN', 'LISTING', FALSE, TRUE, FALSE, TRUE, 130, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PLATE_LAST_DIGIT', 'Ultimo digito de placa', 'Util para pico y placa', 'TEXT', 'LISTING', FALSE, FALSE, FALSE, TRUE, 140, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('NEWS_CATEGORY', 'Categoria editorial', 'Secciones de la zona de noticias', 'OPTION', 'NEWS', FALSE, FALSE, FALSE, FALSE, 200, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO parameter_option (group_code, code, label, icon_name, color_hex, sort_order, active, created_at, updated_at) VALUES
    ('CONDITION', 'NEW', 'Nuevo', 'sparkles', NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CONDITION', 'USED', 'Usado', 'car', NULL, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CONDITION', 'CERTIFIED', 'Usado certificado', 'shield-check', NULL, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('BODY_TYPE', 'SEDAN', 'Sedan', NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BODY_TYPE', 'HATCHBACK', 'Hatchback', NULL, NULL, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BODY_TYPE', 'SUV', 'SUV', NULL, NULL, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BODY_TYPE', 'PICKUP', 'Pickup', NULL, NULL, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BODY_TYPE', 'VAN', 'Van', NULL, NULL, 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BODY_TYPE', 'COUPE', 'Coupe', NULL, NULL, 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BODY_TYPE', 'CONVERTIBLE', 'Convertible', NULL, NULL, 70, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BODY_TYPE', 'WAGON', 'Station wagon', NULL, NULL, 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('FUEL_TYPE', 'GASOLINE', 'Gasolina', 'fuel', NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FUEL_TYPE', 'DIESEL', 'Diesel', 'fuel', NULL, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FUEL_TYPE', 'HYBRID', 'Hibrido', 'leaf', NULL, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FUEL_TYPE', 'ELECTRIC', 'Electrico', 'zap', NULL, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FUEL_TYPE', 'GAS', 'Gas natural', 'flame', NULL, 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('TRANSMISSION', 'MANUAL', 'Manual', NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TRANSMISSION', 'AUTOMATIC', 'Automatica', NULL, NULL, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TRANSMISSION', 'CVT', 'CVT', NULL, NULL, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TRANSMISSION', 'DUAL_CLUTCH', 'Doble embrague', NULL, NULL, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('DRIVETRAIN', 'FWD', 'Delantera', NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DRIVETRAIN', 'RWD', 'Trasera', NULL, NULL, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DRIVETRAIN', 'AWD', 'Integral AWD', NULL, NULL, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DRIVETRAIN', '4WD', '4x4', NULL, NULL, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('COLOR', 'WHITE', 'Blanco', NULL, '#f8fafc', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('COLOR', 'BLACK', 'Negro', NULL, '#111827', 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('COLOR', 'SILVER', 'Plata', NULL, '#cbd5e1', 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('COLOR', 'GRAY', 'Gris', NULL, '#6b7280', 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('COLOR', 'RED', 'Rojo', NULL, '#dc2626', 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('COLOR', 'BLUE', 'Azul', NULL, '#2563eb', 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('COLOR', 'GREEN', 'Verde', NULL, '#16a34a', 70, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('COLOR', 'BEIGE', 'Beige', NULL, '#d6c7a1', 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('CITY', 'BOGOTA', 'Bogota D.C.', NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'MEDELLIN', 'Medellin', NULL, NULL, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'CALI', 'Cali', NULL, NULL, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'BARRANQUILLA', 'Barranquilla', NULL, NULL, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'CARTAGENA', 'Cartagena', NULL, NULL, 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'BUCARAMANGA', 'Bucaramanga', NULL, NULL, 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'PEREIRA', 'Pereira', NULL, NULL, 70, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'MANIZALES', 'Manizales', NULL, NULL, 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'CUCUTA', 'Cucuta', NULL, NULL, 90, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITY', 'IBAGUE', 'Ibague', NULL, NULL, 100, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('FEATURE', 'AIR_CONDITIONING', 'Aire acondicionado', 'wind', NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'LEATHER_SEATS', 'Sillas en cuero', 'armchair', NULL, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'SUNROOF', 'Techo solar', 'sun', NULL, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'REAR_CAMERA', 'Camara de reversa', 'camera', NULL, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'PARKING_SENSORS', 'Sensores de parqueo', 'radar', NULL, 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'CRUISE_CONTROL', 'Control de velocidad', 'gauge', NULL, 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'APPLE_CARPLAY', 'Apple CarPlay', 'smartphone', NULL, 70, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'ANDROID_AUTO', 'Android Auto', 'smartphone', NULL, 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'KEYLESS_ENTRY', 'Encendido sin llave', 'key-round', NULL, 90, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'BLIND_SPOT', 'Alerta de punto ciego', 'eye', NULL, 100, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'LANE_ASSIST', 'Asistente de carril', 'route', NULL, 110, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FEATURE', 'HEATED_SEATS', 'Sillas calefaccionadas', 'thermometer', NULL, 120, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('NEWS_CATEGORY', 'LAUNCHES', 'Lanzamientos', 'rocket', NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('NEWS_CATEGORY', 'TEST_DRIVE', 'Pruebas de manejo', 'steering-wheel', NULL, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('NEWS_CATEGORY', 'BUYING_GUIDE', 'Guias de compra', 'book-open', NULL, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('NEWS_CATEGORY', 'MARKET', 'Mercado', 'trending-up', NULL, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('NEWS_CATEGORY', 'ELECTRIC', 'Electromovilidad', 'zap', NULL, 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('NEWS_CATEGORY', 'MAINTENANCE', 'Mantenimiento', 'wrench', NULL, 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO brand (name, slug, country_of_origin, logo_url, active, created_at, updated_at) VALUES
    ('Toyota', 'toyota', 'Japon', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Mazda', 'mazda', 'Japon', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Renault', 'renault', 'Francia', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Chevrolet', 'chevrolet', 'Estados Unidos', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Kia', 'kia', 'Corea del Sur', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Hyundai', 'hyundai', 'Corea del Sur', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Volkswagen', 'volkswagen', 'Alemania', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Ford', 'ford', 'Estados Unidos', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BMW', 'bmw', 'Alemania', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BYD', 'byd', 'China', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO vehicle_model (brand_id, name, slug, body_type_code, active, created_at, updated_at)
SELECT b.id, m.name, m.slug, m.body_type_code, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM brand b
JOIN (
    SELECT 'Toyota' AS brand, 'Corolla' AS name, 'toyota-corolla' AS slug, 'SEDAN' AS body_type_code
    UNION ALL SELECT 'Toyota', 'Corolla Cross', 'toyota-corolla-cross', 'SUV'
    UNION ALL SELECT 'Toyota', 'Hilux', 'toyota-hilux', 'PICKUP'
    UNION ALL SELECT 'Toyota', 'Prado', 'toyota-prado', 'SUV'
    UNION ALL SELECT 'Mazda', 'Mazda 3', 'mazda-mazda-3', 'SEDAN'
    UNION ALL SELECT 'Mazda', 'CX-30', 'mazda-cx-30', 'SUV'
    UNION ALL SELECT 'Mazda', 'CX-5', 'mazda-cx-5', 'SUV'
    UNION ALL SELECT 'Renault', 'Duster', 'renault-duster', 'SUV'
    UNION ALL SELECT 'Renault', 'Logan', 'renault-logan', 'SEDAN'
    UNION ALL SELECT 'Renault', 'Sandero', 'renault-sandero', 'HATCHBACK'
    UNION ALL SELECT 'Chevrolet', 'Onix', 'chevrolet-onix', 'SEDAN'
    UNION ALL SELECT 'Chevrolet', 'Tracker', 'chevrolet-tracker', 'SUV'
    UNION ALL SELECT 'Chevrolet', 'Captiva', 'chevrolet-captiva', 'SUV'
    UNION ALL SELECT 'Kia', 'Picanto', 'kia-picanto', 'HATCHBACK'
    UNION ALL SELECT 'Kia', 'Sportage', 'kia-sportage', 'SUV'
    UNION ALL SELECT 'Kia', 'Seltos', 'kia-seltos', 'SUV'
    UNION ALL SELECT 'Hyundai', 'Tucson', 'hyundai-tucson', 'SUV'
    UNION ALL SELECT 'Hyundai', 'Accent', 'hyundai-accent', 'SEDAN'
    UNION ALL SELECT 'Hyundai', 'Creta', 'hyundai-creta', 'SUV'
    UNION ALL SELECT 'Volkswagen', 'T-Cross', 'volkswagen-t-cross', 'SUV'
    UNION ALL SELECT 'Volkswagen', 'Virtus', 'volkswagen-virtus', 'SEDAN'
    UNION ALL SELECT 'Ford', 'Escape', 'ford-escape', 'SUV'
    UNION ALL SELECT 'Ford', 'Ranger', 'ford-ranger', 'PICKUP'
    UNION ALL SELECT 'BMW', 'Serie 3', 'bmw-serie-3', 'SEDAN'
    UNION ALL SELECT 'BMW', 'X1', 'bmw-x1', 'SUV'
    UNION ALL SELECT 'BYD', 'Dolphin', 'byd-dolphin', 'HATCHBACK'
    UNION ALL SELECT 'BYD', 'Song Plus', 'byd-song-plus', 'SUV'
) m ON m.brand = b.name;
