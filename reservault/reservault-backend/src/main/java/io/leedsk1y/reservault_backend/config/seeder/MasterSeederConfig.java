package io.leedsk1y.reservault_backend.config.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MasterSeederConfig {
    private static final Logger logger = LoggerFactory.getLogger(MasterSeederConfig.class);
    private final RoleSeederConfig roleSeederConfig;
    private final AdminSeederConfig adminSeederConfig;
    private final ManagerSeederConfig managerSeederConfig;
    private final HotelSeederConfig hotelSeederConfig;
    private final OfferSeederConfig offerSeederConfig;

    @Value("${reservault.seeders.enabled:true}")
    private boolean seedersEnabled;

    @Value("${reservault.seeders.roles:true}")
    private boolean seedRoles;

    @Value("${reservault.seeders.admin:true}")
    private boolean seedAdmin;

    @Value("${reservault.seeders.manager:true}")
    private boolean seedManager;

    @Value("${reservault.seeders.hotels:true}")
    private boolean seedHotels;

    @Value("${reservault.seeders.offers:true}")
    private boolean seedOffers;

    public MasterSeederConfig(
            RoleSeederConfig roleSeederConfig,
            AdminSeederConfig adminSeederConfig,
            ManagerSeederConfig managerSeederConfig,
            HotelSeederConfig hotelSeederConfig,
            OfferSeederConfig offerSeederConfig
    ) {
        this.roleSeederConfig = roleSeederConfig;
        this.adminSeederConfig = adminSeederConfig;
        this.managerSeederConfig = managerSeederConfig;
        this.hotelSeederConfig = hotelSeederConfig;
        this.offerSeederConfig = offerSeederConfig;
    }

    /**
     * Runs application seeders on startup based on configuration flags.
     * Executes the following seeders conditionally:
     * - Roles
     * - Admin user
     * - Manager user
     * - Hotels
     * - Offers
     *
     * Controlled by:
     * - `reservault.seeders.enabled`
     * - `reservault.seeders.roles`
     * - `reservault.seeders.admin`
     * - `reservault.seeders.manager`
     * - `reservault.seeders.hotels`
     * - `reservault.seeders.offers`
     *
     * @return ApplicationRunner that triggers seeders during application startup.
     */
    @Bean
    public ApplicationRunner runAllSeeders() {
        return args -> {
            if (!seedersEnabled) {
                return;
            }

            logger.info("Running master seeder...");

            if (seedRoles) {
                logger.info("Seeding roles...");
                roleSeederConfig.seedRoles().run(args);
            }

            if (seedAdmin) {
                logger.info("Seeding admin...");
                adminSeederConfig.seedAdminUser().run(args);
            }

            if (seedManager) {
                logger.info("Seeding manager...");
                managerSeederConfig.seedManager();
            }

            if (seedHotels) {
                logger.info("Seeding hotels...");
                hotelSeederConfig.seedHotels();
            }

            if (seedOffers) {
                logger.info("Seeding offers...");
                offerSeederConfig.seedOffers();
            }
        };
    }
}
