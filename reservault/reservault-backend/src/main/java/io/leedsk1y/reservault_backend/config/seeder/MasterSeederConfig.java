package io.leedsk1y.reservault_backend.config.seeder;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MasterSeederConfig {
    private final RoleSeederConfig roleSeederConfig;
    private final AdminSeederConfig adminSeederConfig;
    private final ManagerSeederConfig managerSeederConfig;
    private final HotelSeederConfig hotelSeederConfig;
    private final OfferSeederConfig offerSeederConfig;

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

    @Bean
    public ApplicationRunner runAllSeeders() {
        return args -> {
            roleSeederConfig.seedRoles().run(args);
            adminSeederConfig.seedAdminUser().run(args);

            try {
                managerSeederConfig.seedManager();
                hotelSeederConfig.seedHotels();
                offerSeederConfig.seedOffers();
            } catch (Exception e) {
                throw new RuntimeException("Error seeding manager, hotels or offers", e);
            }
        };
    }
}
