package com.camsell.bootstrap;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingAttribute;
import com.camsell.catalog.domain.model.ListingImage;
import com.camsell.catalog.domain.model.ListingStatus;
import com.camsell.catalog.domain.model.Money;
import com.camsell.catalog.domain.model.Offer;
import com.camsell.catalog.domain.model.OfferStatus;
import com.camsell.catalog.domain.port.out.FavoriteRepositoryPort;
import com.camsell.catalog.domain.port.out.ListingRepositoryPort;
import com.camsell.catalog.domain.port.out.OfferRepositoryPort;
import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.model.UserRole;
import com.camsell.identity.domain.port.out.PasswordHasherPort;
import com.camsell.identity.domain.port.out.UserRepositoryPort;
import com.camsell.news.domain.model.Article;
import com.camsell.news.domain.model.ArticleStatus;
import com.camsell.news.domain.port.out.ArticleRepositoryPort;
import com.camsell.parameters.domain.model.Brand;
import com.camsell.parameters.domain.model.ParameterGroups;
import com.camsell.parameters.domain.model.VehicleModel;
import com.camsell.parameters.domain.port.out.BrandRepositoryPort;
import com.camsell.shared.domain.util.Slugs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Carga un conjunto de datos de demostracion la primera vez que arranca la
 * aplicacion. Se activa con {@code camsell.demo-data.enabled=true} y nunca
 * sobrescribe informacion existente.
 */
@Component
@ConditionalOnProperty(name = "camsell.demo-data.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String DEMO_PASSWORD = "Camsell2026*";

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final BrandRepositoryPort brands;
    private final ListingRepositoryPort listings;
    private final OfferRepositoryPort offers;
    private final FavoriteRepositoryPort favorites;
    private final ArticleRepositoryPort articles;

    /** Secuencia local para que referencias y fechas del seed sean deterministas. */
    private int sequence;

    public DemoDataSeeder(
            UserRepositoryPort users,
            PasswordHasherPort hasher,
            BrandRepositoryPort brands,
            ListingRepositoryPort listings,
            OfferRepositoryPort offers,
            FavoriteRepositoryPort favorites,
            ArticleRepositoryPort articles) {
        this.users = users;
        this.hasher = hasher;
        this.brands = brands;
        this.listings = listings;
        this.offers = offers;
        this.favorites = favorites;
        this.articles = articles;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.existsByEmail("admin@camsell.dev")) {
            log.info("Datos de demostracion ya presentes; no se cargan de nuevo");
            return;
        }

        User admin = createUser("admin@camsell.dev", "Christian Camacho", UserRole.ADMIN,
                "+57 300 000 0001", "BOGOTA");
        User dealer = createUser("ventas@autonorte.co", "AutoNorte Concesionario", UserRole.DEALER,
                "+57 310 555 2244", "MEDELLIN");
        User seller = createUser("laura.gomez@mail.com", "Laura Gomez", UserRole.USER,
                "+57 320 418 7766", "BOGOTA");
        User buyer = createUser("carlos.ruiz@mail.com", "Carlos Ruiz", UserRole.USER,
                "+57 318 902 3311", "CALI");

        Map<String, Brand> brandIndex = index();
        List<Listing> created = new ArrayList<>();

        created.add(publish(dealer, brandIndex, "Toyota", "Corolla Cross", 2024, 8_500,
                "168900000", "BOGOTA", true, true,
                "Toyota Corolla Cross XEI hibrida con un solo propietario, mantenimientos al dia en "
                        + "concesionario autorizado y garantia de fabrica vigente hasta 2027. "
                        + "Llantas nuevas, sin reportes de choque y revision de 150 puntos aprobada.",
                List.of(
                        attr(ParameterGroups.CONDITION, "CERTIFIED"),
                        attr(ParameterGroups.BODY_TYPE, "SUV"),
                        attr(ParameterGroups.FUEL_TYPE, "HYBRID"),
                        attr(ParameterGroups.TRANSMISSION, "CVT"),
                        attr(ParameterGroups.DRIVETRAIN, "FWD"),
                        attr(ParameterGroups.COLOR, "WHITE"),
                        attr("FEATURE", "REAR_CAMERA"),
                        attr("FEATURE", "APPLE_CARPLAY"),
                        attr("FEATURE", "CRUISE_CONTROL"),
                        attr("FEATURE", "BLIND_SPOT"),
                        number("DOORS", 5),
                        number("ENGINE_DISPLACEMENT", 1798),
                        number("HORSEPOWER", 122),
                        flag("WARRANTY", true),
                        flag("SINGLE_OWNER", true))));

        created.add(publish(dealer, brandIndex, "Mazda", "CX-5", 2023, 23_400,
                "154500000", "MEDELLIN", true, true,
                "Mazda CX-5 Grand Touring LX con traccion AWD, interior en cuero negro y techo "
                        + "solar panoramico. Unico dueno, kilometraje certificado y mantenimientos "
                        + "registrados en la red Mazda. Entrega inmediata con transferencia incluida.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "SUV"),
                        attr(ParameterGroups.FUEL_TYPE, "GASOLINE"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.DRIVETRAIN, "AWD"),
                        attr(ParameterGroups.COLOR, "GRAY"),
                        attr("FEATURE", "LEATHER_SEATS"),
                        attr("FEATURE", "SUNROOF"),
                        attr("FEATURE", "PARKING_SENSORS"),
                        attr("FEATURE", "HEATED_SEATS"),
                        number("DOORS", 5),
                        number("ENGINE_DISPLACEMENT", 2500),
                        number("HORSEPOWER", 187),
                        flag("SINGLE_OWNER", true))));

        created.add(publish(seller, brandIndex, "Renault", "Duster", 2021, 61_200,
                "72900000", "BOGOTA", true, false,
                "Renault Duster Intens 4x2 automatica, perfecta para ciudad y carretera. "
                        + "Mantenimiento preventivo recien realizado, llantas al 80% y SOAT vigente "
                        + "hasta diciembre. Se recibe vehiculo de menor valor como parte de pago.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "SUV"),
                        attr(ParameterGroups.FUEL_TYPE, "GASOLINE"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.DRIVETRAIN, "FWD"),
                        attr(ParameterGroups.COLOR, "SILVER"),
                        attr("FEATURE", "AIR_CONDITIONING"),
                        attr("FEATURE", "REAR_CAMERA"),
                        number("DOORS", 5),
                        number("ENGINE_DISPLACEMENT", 1600),
                        number("HORSEPOWER", 115),
                        text("PLATE_LAST_DIGIT", "4"))));

        created.add(publish(dealer, brandIndex, "BYD", "Dolphin", 2025, 1_900,
                "118900000", "BOGOTA", true, true,
                "BYD Dolphin GL 100% electrica, modelo 2025 practicamente nueva con 1.900 km. "
                        + "Autonomia homologada de 405 km, cargador de pared incluido y garantia de "
                        + "bateria por ocho anos. Exenta de pico y placa en Bogota.",
                List.of(
                        attr(ParameterGroups.CONDITION, "CERTIFIED"),
                        attr(ParameterGroups.BODY_TYPE, "HATCHBACK"),
                        attr(ParameterGroups.FUEL_TYPE, "ELECTRIC"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.DRIVETRAIN, "FWD"),
                        attr(ParameterGroups.COLOR, "BLUE"),
                        attr("FEATURE", "APPLE_CARPLAY"),
                        attr("FEATURE", "ANDROID_AUTO"),
                        attr("FEATURE", "KEYLESS_ENTRY"),
                        attr("FEATURE", "LANE_ASSIST"),
                        number("DOORS", 5),
                        number("HORSEPOWER", 95),
                        flag("WARRANTY", true),
                        flag("SINGLE_OWNER", true))));

        created.add(publish(seller, brandIndex, "Chevrolet", "Onix", 2022, 38_700,
                "58400000", "CALI", true, false,
                "Chevrolet Onix Premier turbo automatico, consumo real de 18 km por galon en ciudad. "
                        + "Pantalla de 8 pulgadas con MyLink, camara de reversa y seis airbags. "
                        + "Documentos listos para traspaso inmediato.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "SEDAN"),
                        attr(ParameterGroups.FUEL_TYPE, "GASOLINE"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.COLOR, "RED"),
                        attr("FEATURE", "AIR_CONDITIONING"),
                        attr("FEATURE", "ANDROID_AUTO"),
                        attr("FEATURE", "REAR_CAMERA"),
                        number("DOORS", 4),
                        number("ENGINE_DISPLACEMENT", 1000),
                        number("HORSEPOWER", 116))));

        created.add(publish(dealer, brandIndex, "Toyota", "Hilux", 2022, 74_500,
                "189000000", "BUCARAMANGA", true, true,
                "Toyota Hilux SRV 4x4 diesel automatica, ideal para trabajo pesado y viaje familiar. "
                        + "Mantenimientos en concesionario, platon con forro y barra antivuelco. "
                        + "Revision tecnomecanica vigente y sin multas pendientes.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "PICKUP"),
                        attr(ParameterGroups.FUEL_TYPE, "DIESEL"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.DRIVETRAIN, "4WD"),
                        attr(ParameterGroups.COLOR, "BLACK"),
                        attr("FEATURE", "CRUISE_CONTROL"),
                        attr("FEATURE", "PARKING_SENSORS"),
                        number("DOORS", 4),
                        number("ENGINE_DISPLACEMENT", 2800),
                        number("HORSEPOWER", 204))));

        created.add(publish(seller, brandIndex, "Kia", "Picanto", 2020, 52_300,
                "44900000", "BOGOTA", true, false,
                "Kia Picanto Vibrant mecanico, el citycar mas economico de mantener del mercado. "
                        + "Pintura original, sin choques y con kit de sincronizacion recien cambiado. "
                        + "Perfecto como primer carro o para aplicaciones de transporte.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "HATCHBACK"),
                        attr(ParameterGroups.FUEL_TYPE, "GASOLINE"),
                        attr(ParameterGroups.TRANSMISSION, "MANUAL"),
                        attr(ParameterGroups.COLOR, "WHITE"),
                        attr("FEATURE", "AIR_CONDITIONING"),
                        number("DOORS", 5),
                        number("ENGINE_DISPLACEMENT", 1250),
                        number("HORSEPOWER", 87),
                        text("PLATE_LAST_DIGIT", "7"))));

        created.add(publish(dealer, brandIndex, "BMW", "Serie 3", 2021, 41_000,
                "212000000", "MEDELLIN", true, true,
                "BMW 320i Sport Line con paquete M, tapiceria Sensatec y asistente de parqueo. "
                        + "Historial de servicio completo en BMW Autogermana, dos juegos de llaves y "
                        + "garantia extendida transferible por un ano.",
                List.of(
                        attr(ParameterGroups.CONDITION, "CERTIFIED"),
                        attr(ParameterGroups.BODY_TYPE, "SEDAN"),
                        attr(ParameterGroups.FUEL_TYPE, "GASOLINE"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.DRIVETRAIN, "RWD"),
                        attr(ParameterGroups.COLOR, "BLACK"),
                        attr("FEATURE", "LEATHER_SEATS"),
                        attr("FEATURE", "SUNROOF"),
                        attr("FEATURE", "PARKING_SENSORS"),
                        attr("FEATURE", "BLIND_SPOT"),
                        number("DOORS", 4),
                        number("ENGINE_DISPLACEMENT", 1998),
                        number("HORSEPOWER", 184),
                        flag("WARRANTY", true))));

        created.add(publish(seller, brandIndex, "Hyundai", "Tucson", 2023, 19_800,
                "139500000", "BARRANQUILLA", true, false,
                "Hyundai Tucson Limited con techo panoramico y asistencias de conduccion SmartSense. "
                        + "Unico dueno, garantia de fabrica vigente y mantenimientos prepagados hasta "
                        + "los 50.000 km. Se entrega con polarizado y proteccion ceramica.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "SUV"),
                        attr(ParameterGroups.FUEL_TYPE, "GASOLINE"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.DRIVETRAIN, "AWD"),
                        attr(ParameterGroups.COLOR, "SILVER"),
                        attr("FEATURE", "SUNROOF"),
                        attr("FEATURE", "LANE_ASSIST"),
                        attr("FEATURE", "KEYLESS_ENTRY"),
                        number("DOORS", 5),
                        number("ENGINE_DISPLACEMENT", 2000),
                        number("HORSEPOWER", 156),
                        flag("WARRANTY", true),
                        flag("SINGLE_OWNER", true))));

        created.add(publish(dealer, brandIndex, "Volkswagen", "T-Cross", 2023, 27_600,
                "98700000", "PEREIRA", true, false,
                "Volkswagen T-Cross Highline con motor TSI turbo y cambios por paleta al volante. "
                        + "Interior impecable, llantas nuevas y revision pre entrega de 120 puntos. "
                        + "Financiacion aprobada hasta 84 meses con bancos aliados.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "SUV"),
                        attr(ParameterGroups.FUEL_TYPE, "GASOLINE"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.COLOR, "GRAY"),
                        attr("FEATURE", "APPLE_CARPLAY"),
                        attr("FEATURE", "CRUISE_CONTROL"),
                        number("DOORS", 5),
                        number("ENGINE_DISPLACEMENT", 1400),
                        number("HORSEPOWER", 150))));

        created.add(publish(seller, brandIndex, "Mazda", "Mazda 3", 2019, 78_400,
                "67500000", "BOGOTA", true, false,
                "Mazda 3 Grand Touring sedan, carro de dueno unico con historial de mantenimiento "
                        + "completo. Sin choques registrados, pintura original de fabrica y motor "
                        + "Skyactiv con excelente rendimiento de combustible.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "SEDAN"),
                        attr(ParameterGroups.FUEL_TYPE, "GASOLINE"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.COLOR, "BLUE"),
                        attr("FEATURE", "LEATHER_SEATS"),
                        attr("FEATURE", "REAR_CAMERA"),
                        number("DOORS", 4),
                        number("ENGINE_DISPLACEMENT", 2000),
                        number("HORSEPOWER", 155),
                        flag("SINGLE_OWNER", true))));

        created.add(publish(dealer, brandIndex, "Ford", "Ranger", 2024, 11_200,
                "214900000", "BOGOTA", true, false,
                "Ford Ranger XLT 4x4 diesel modelo 2024, aun con garantia completa de fabrica. "
                        + "Equipada con control de descenso, bloqueo de diferencial trasero y "
                        + "pantalla SYNC 4 de 12 pulgadas. Lista para trabajo y aventura.",
                List.of(
                        attr(ParameterGroups.CONDITION, "CERTIFIED"),
                        attr(ParameterGroups.BODY_TYPE, "PICKUP"),
                        attr(ParameterGroups.FUEL_TYPE, "DIESEL"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.DRIVETRAIN, "4WD"),
                        attr(ParameterGroups.COLOR, "WHITE"),
                        attr("FEATURE", "ANDROID_AUTO"),
                        attr("FEATURE", "PARKING_SENSORS"),
                        number("DOORS", 4),
                        number("ENGINE_DISPLACEMENT", 2000),
                        number("HORSEPOWER", 170),
                        flag("WARRANTY", true))));

        created.add(publish(seller, brandIndex, "Kia", "Seltos", 2022, 44_100,
                "104300000", "MANIZALES", true, false,
                "Kia Seltos EX automatica con camara 360 y seis airbags. Vehiculo familiar de un "
                        + "solo dueno, siempre en garaje, mantenimientos en Kia Autotecnica y sin "
                        + "ningun reporte en el historial de siniestros.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "SUV"),
                        attr(ParameterGroups.FUEL_TYPE, "GASOLINE"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.COLOR, "BEIGE"),
                        attr("FEATURE", "REAR_CAMERA"),
                        attr("FEATURE", "KEYLESS_ENTRY"),
                        number("DOORS", 5),
                        number("ENGINE_DISPLACEMENT", 1600),
                        number("HORSEPOWER", 123),
                        flag("SINGLE_OWNER", true))));

        created.add(publish(dealer, brandIndex, "BYD", "Song Plus", 2024, 14_300,
                "172400000", "CARTAGENA", true, false,
                "BYD Song Plus DM-i hibrida enchufable con 1.100 km de autonomia combinada. "
                        + "Siete anos de garantia en bateria, cargador portatil y mantenimiento "
                        + "programado incluido durante el primer ano.",
                List.of(
                        attr(ParameterGroups.CONDITION, "USED"),
                        attr(ParameterGroups.BODY_TYPE, "SUV"),
                        attr(ParameterGroups.FUEL_TYPE, "HYBRID"),
                        attr(ParameterGroups.TRANSMISSION, "AUTOMATIC"),
                        attr(ParameterGroups.COLOR, "GREEN"),
                        attr("FEATURE", "APPLE_CARPLAY"),
                        attr("FEATURE", "BLIND_SPOT"),
                        attr("FEATURE", "HEATED_SEATS"),
                        number("DOORS", 5),
                        number("HORSEPOWER", 235),
                        flag("WARRANTY", true))));

        created.removeIf(java.util.Objects::isNull);
        seedOffersAndFavorites(created, buyer);
        seedArticles(admin);

        log.info("Datos de demostracion cargados: {} usuarios, {} publicaciones",
                4, created.size());
        log.info("Credenciales demo -> admin@camsell.dev / {} (ADMIN)", DEMO_PASSWORD);
    }

    private void seedOffersAndFavorites(List<Listing> created, User buyer) {
        if (created.size() < 5) {
            return;
        }
        Listing first = created.get(0);
        Listing second = created.get(2);

        offers.save(new Offer(
                null,
                first.id(),
                first.reference(),
                first.title(),
                first.sellerId(),
                buyer.id(),
                buyer.fullName(),
                buyer.phone(),
                new Money(new BigDecimal("160000000"), "COP"),
                "Me interesa mucho el vehiculo. Puedo pagar de contado esta semana si llegamos a un acuerdo.",
                OfferStatus.PENDING,
                null,
                null));

        offers.save(new Offer(
                null,
                second.id(),
                second.reference(),
                second.title(),
                second.sellerId(),
                buyer.id(),
                buyer.fullName(),
                buyer.phone(),
                new Money(new BigDecimal("68000000"), "COP"),
                "Tengo credito preaprobado. Quedo atento para agendar la revision con mi mecanico.",
                OfferStatus.PENDING,
                null,
                null));

        for (int index : new int[] {1, 3, 7}) {
            if (index < created.size()) {
                favorites.add(buyer.id(), created.get(index).id());
            }
        }
    }

    private void seedArticles(User author) {
        publishArticle(author, "LAUNCHES", true,
                "BYD Dolphin 2026 llega a Colombia con 425 km de autonomia",
                "La electrica de acceso de BYD estrena bateria Blade de segunda generacion y recorta "
                        + "el tiempo de carga rapida a 29 minutos.",
                List.of("electricos", "lanzamientos", "byd"),
                """
                BYD confirmo la llegada al pais de la version 2026 del Dolphin, su hatchback electrico \
                de acceso, con una bateria Blade de segunda generacion que eleva la autonomia \
                homologada de 405 a 425 kilometros en ciclo WLTP.

                El cambio mas relevante no esta en la capacidad sino en la gestion termica. El nuevo \
                paquete admite carga rapida de 11 a 80 por ciento en 29 minutos, nueve menos que el \
                modelo saliente, y mantiene la potencia de carga estable incluso con temperatura \
                ambiente por encima de 30 grados, un escenario habitual en la costa colombiana.

                En equipamiento, la version Premium suma asistente de carril, alerta de punto ciego \
                y una pantalla rotativa de 12,8 pulgadas con Apple CarPlay y Android Auto \
                inalambricos. La garantia de bateria se mantiene en ocho anos o 200.000 kilometros, \
                transferible al segundo propietario, un detalle que pesa en el valor de reventa.

                La red de servicio es el punto que mas preguntas genera entre compradores. BYD opera \
                hoy once talleres autorizados en siete ciudades y proyecta llegar a dieciseis antes \
                de terminar el ano, con inventario de repuestos criticos en Bogota y Medellin para \
                reducir los tiempos de espera que afectaron a los primeros propietarios.

                Frente a sus rivales directos, el Dolphin queda en una posicion comoda: ofrece mas \
                autonomia que el Renault Kwid E-Tech y un precio sensiblemente menor que el Volvo \
                EX30, aunque sin el nivel de acabados del sueco. Para quien hace recorridos urbanos \
                diarios y carga en casa, la cuenta de ahorro frente a un gasolina equivalente se \
                paga en poco mas de tres anos.
                """);

        publishArticle(author, "BUYING_GUIDE", true,
                "Como revisar un usado en 2026: la lista que evita sorpresas",
                "Diez verificaciones concretas, en orden, antes de firmar el traspaso de un vehiculo usado.",
                List.of("guias", "usados", "compra"),
                """
                Comprar usado sigue siendo la mejor relacion entre precio y valor del mercado \
                colombiano, pero el margen de error es estrecho. Estas son las verificaciones que \
                de verdad cambian la decision, en el orden en que conviene hacerlas.

                Empiece por los papeles, no por el carro. Pida el certificado de tradicion y \
                libertad del RUNT, confirme que el numero de propietarios coincida con lo que dice \
                el vendedor y revise que no existan embargos, prendas ni limitaciones de dominio. \
                Una prenda vigente puede bloquear el traspaso durante semanas.

                Siga con el historial de siniestros. Las aseguradoras reportan a centrales de \
                informacion y un reporte de perdida parcial no descalifica el vehiculo, pero si \
                cambia el precio justo. Compare el reporte con la inspeccion fisica: si hay un \
                choque reportado y la lamina se ve perfecta, pregunte quien hizo la reparacion.

                En la revision fisica, mida el espesor de pintura con un calibrador en los seis \
                paneles principales. Diferencias mayores a 40 micras entre paneles indican \
                repintado. Revise la alineacion de los vidrios, el desgaste del volante frente al \
                kilometraje declarado y el estado de los pedales, dos indicadores dificiles de \
                falsificar.

                Lleve el vehiculo a un taller que no tenga relacion con el vendedor. Pida \
                diagnostico con escaner, compresion de motor y prueba de frenos en banco. El costo \
                esta entre 180.000 y 350.000 pesos y es la inversion con mejor retorno de todo el \
                proceso.

                Finalmente, negocie con datos. Con el informe del taller en mano, cada hallazgo \
                tiene un costo de reparacion verificable que entra a la conversacion de precio. Un \
                juego de llantas, unas pastillas y una correa de reparticion suman facil tres \
                millones de pesos que no deberia pagar usted.
                """);

        publishArticle(author, "MARKET", false,
                "El mercado de usados crecio 14% y los hibridos lideran la demanda",
                "Informe trimestral del comportamiento de precios por segmento y region.",
                List.of("mercado", "precios", "hibridos"),
                """
                El mercado de vehiculos usados cerro el trimestre con un crecimiento del 14 por \
                ciento frente al mismo periodo del ano anterior, impulsado por el segmento de SUV \
                compactas y por una oferta de hibridos que por primera vez supera el diez por \
                ciento del inventario publicado.

                Los hibridos con menos de cinco anos de uso son hoy la categoria con rotacion mas \
                rapida: 23 dias promedio entre publicacion y venta, frente a 41 dias del total del \
                mercado. La razon es doble, la exencion de pico y placa en varias ciudades y un \
                costo de operacion que resiste bien la volatilidad del precio de la gasolina.

                Por region, Bogota concentra el 38 por ciento de la oferta pero Medellin muestra \
                los precios mas firmes, con una depreciacion anual promedio de 8,4 por ciento \
                contra 11,2 de la capital. En la costa, el factor determinante del precio sigue \
                siendo el estado de la carroceria por efecto de la salinidad.

                Para quien vende, el dato practico es que las publicaciones con mas de ocho \
                fotografias y ficha tecnica completa reciben 2,7 veces mas contactos que las \
                publicaciones incompletas, con independencia del precio. La informacion, no el \
                descuento, es lo que mueve la aguja.
                """);

        publishArticle(author, "TEST_DRIVE", false,
                "Probamos la Toyota Hilux 2025: menos ruido, mismo caracter",
                "Mil kilometros entre Bogota y el Llano para entender que cambio en la pickup mas vendida.",
                List.of("pruebas", "pickup", "toyota"),
                """
                La Hilux lleva tanto tiempo liderando el segmento que cualquier cambio se mide con \
                lupa. La version 2025 no reinventa nada y eso, en este caso, es una decision \
                deliberada: Toyota corrigio lo que molestaba sin tocar lo que funciona.

                El primer cambio se siente en el habitaculo. El aislamiento acustico mejoro de \
                forma notoria y a 100 kilometros por hora el ruido de rodadura bajo lo suficiente \
                para sostener una conversacion sin levantar la voz, algo que la generacion anterior \
                no permitia con comodidad.

                En el Llano, con 600 kilos en el platon y trocha humeda, el bloqueo de diferencial \
                trasero y el control de descenso hicieron el trabajo sin drama. El motor 2.8 diesel \
                entrega los 500 Nm desde las 1.600 vueltas, lo que evita tener que exprimirlo en \
                pendientes con carga.

                El consumo real promedio fue de 11,2 kilometros por galon en carretera y 8,9 en \
                trocha, cifras consistentes con lo declarado. La suspension trasera sigue siendo \
                firme cuando va vacia, el peaje habitual de una pickup de trabajo que mantiene su \
                capacidad de carga sin concesiones.
                """);

        publishArticle(author, "ELECTRIC", false,
                "Cargadores publicos en Colombia: donde estan y cuanto cuestan",
                "Mapa del estado real de la infraestructura de carga y el costo por kilovatio hora.",
                List.of("electricos", "infraestructura", "carga"),
                """
                La infraestructura de carga publica paso de 230 a 412 puntos en dieciocho meses, \
                pero la distribucion sigue siendo el problema. Mas del 60 por ciento de los \
                cargadores rapidos esta en Bogota, Medellin y el corredor que las une.

                El costo por kilovatio hora en carga rapida publica oscila entre 1.100 y 1.800 \
                pesos segun el operador, lo que para un vehiculo de consumo medio equivale a unos \
                180 pesos por kilometro. Cargar en casa en horario nocturno baja ese numero a cerca \
                de 70 pesos, una diferencia que define la economia real del vehiculo electrico.

                Para viajes largos, el corredor Bogota-Medellin ya permite recorrer el trayecto con \
                una sola parada de carga en vehiculos con mas de 350 kilometros de autonomia. La \
                ruta a la costa sigue siendo exigente: el tramo entre Bucaramanga y Santa Marta \
                tiene apenas cuatro cargadores rapidos operativos.

                La recomendacion practica para quien evalua su primer electrico es simple: si tiene \
                donde cargar en casa o en la oficina, la infraestructura publica es un complemento \
                ocasional y no un requisito. Si depende exclusivamente de carga publica, revise la \
                cobertura de su zona antes de decidir.
                """);

        publishArticle(author, "MAINTENANCE", false,
                "Cinco mantenimientos que la gente posterga y salen carisimos",
                "Lo que cuesta hacerlo a tiempo frente a lo que cuesta repararlo despues.",
                List.of("mantenimiento", "costos", "taller"),
                """
                Postergar mantenimiento es la forma mas comun de convertir un gasto pequeno en una \
                reparacion mayor. Estos cinco casos son los que mas aparecen en taller y los que \
                tienen la diferencia de costo mas brutal.

                La correa de reparticion encabeza la lista. Cambiarla a tiempo cuesta entre 800.000 \
                y 1.600.000 pesos segun el motor. Si se rompe en marcha, el dano a valvulas y \
                piston deja una factura que arranca en ocho millones y puede llegar a quince.

                El liquido de frenos es el mas ignorado. Absorbe humedad y pierde punto de \
                ebullicion con el tiempo, lo que se traduce en pedal esponjoso y corrosion interna \
                del sistema. Un cambio cada dos anos cuesta 180.000 pesos; reemplazar bomba, \
                cilindros y caliper corroidos pasa de tres millones.

                El filtro de aire del motor parece menor pero un filtro saturado sube el consumo \
                entre 5 y 8 por ciento y acelera el desgaste de cilindros. Cuesta menos de 100.000 \
                pesos y es la pieza con mejor relacion costo beneficio de toda la lista.

                La alineacion y balanceo fuera de especificacion destruye un juego de llantas en la \
                mitad de su vida util. Un servicio de 120.000 pesos frente a un juego de llantas de \
                dos a cuatro millones, segun el tamano de rin.

                Por ultimo, el refrigerante vencido pierde capacidad anticorrosiva y ataca radiador, \
                bomba de agua y empaque de culata. El cambio programado cuesta cerca de 250.000 \
                pesos; una culata levantada por sobrecalentamiento no baja de cinco millones.
                """);
    }

    private void publishArticle(
            User author,
            String category,
            boolean featured,
            String title,
            String excerpt,
            List<String> tags,
            String body) {
        String slug = Slugs.of(title);
        if (articles.existsBySlug(slug)) {
            return;
        }
        Article article = new Article(
                null,
                slug,
                title,
                excerpt,
                body,
                "https://picsum.photos/seed/" + slug + "/1280/720",
                category,
                tags,
                author.id(),
                author.fullName(),
                ArticleStatus.PUBLISHED,
                featured,
                Article.estimateReadingMinutes(body),
                0L,
                Instant.now().minus(++sequence * 3L, ChronoUnit.DAYS),
                null);
        articles.save(article);
    }

    private Listing publish(
            User seller,
            Map<String, Brand> brandIndex,
            String brandName,
            String modelName,
            int year,
            int mileage,
            String price,
            String cityCode,
            boolean published,
            boolean featured,
            String description,
            List<ListingAttribute> attributes) {

        Brand brand = brandIndex.get(brandName);
        Optional<VehicleModel> model = brand == null
                ? Optional.empty()
                : brand.models().stream().filter(candidate -> candidate.name().equals(modelName)).findFirst();
        if (model.isEmpty()) {
            log.warn("Modelo {} {} no encontrado; se omite del seed", brandName, modelName);
            return null;
        }

        String title = "%s %s %d".formatted(brandName, modelName, year);
        String slug = Slugs.of(brandName, modelName, String.valueOf(year));
        String reference = "CAMS-D%04d".formatted(++sequence);

        List<ListingImage> images = List.of(
                ListingImage.of("https://picsum.photos/seed/" + slug + "-1/1280/860", title, 0),
                ListingImage.of("https://picsum.photos/seed/" + slug + "-2/1280/860", title + " interior", 1),
                ListingImage.of("https://picsum.photos/seed/" + slug + "-3/1280/860", title + " lateral", 2),
                ListingImage.of("https://picsum.photos/seed/" + slug + "-4/1280/860", title + " motor", 3));

        Listing listing = new Listing(
                null,
                reference,
                slug,
                seller.id(),
                seller.fullName(),
                model.get().brandId(),
                brandName,
                model.get().id(),
                modelName,
                title,
                year,
                mileage,
                new Money(new BigDecimal(price), "COP"),
                true,
                description,
                cityCode,
                published ? ListingStatus.PUBLISHED : ListingStatus.DRAFT,
                featured,
                0L,
                published ? Instant.now().minus(sequence * 2L, ChronoUnit.DAYS) : null,
                null,
                images,
                attributes);

        return listings.save(listing);
    }

    private Map<String, Brand> index() {
        return brands.findAllBrands(true, true).stream()
                .collect(java.util.stream.Collectors.toMap(Brand::name, brand -> brand));
    }

    private User createUser(String email, String fullName, UserRole role, String phone, String cityCode) {
        return users.save(new User(
                null,
                email,
                hasher.hash(DEMO_PASSWORD),
                fullName,
                phone,
                cityCode,
                "https://i.pravatar.cc/160?u=" + email,
                role,
                true,
                null));
    }

    private static ListingAttribute attr(String group, String option) {
        return ListingAttribute.option(group, option);
    }

    private static ListingAttribute number(String group, int value) {
        return ListingAttribute.number(group, BigDecimal.valueOf(value));
    }

    private static ListingAttribute text(String group, String value) {
        return ListingAttribute.text(group, value);
    }

    private static ListingAttribute flag(String group, boolean value) {
        return ListingAttribute.flag(group, value);
    }
}
