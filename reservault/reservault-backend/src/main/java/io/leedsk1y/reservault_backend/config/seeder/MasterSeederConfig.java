package io.leedsk1y.reservault_backend.config.seeder;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MasterSeederConfig {
    private final HotelSeederConfig hotelSeederConfig;
    private final ManagerSeederConfig managerSeederConfig;
    private final OfferSeederConfig offerSeederConfig;

    public MasterSeederConfig(
            HotelSeederConfig hotelSeederConfig,
            ManagerSeederConfig managerSeederConfig,
            OfferSeederConfig offerSeederConfig
    ) {
        this.hotelSeederConfig = hotelSeederConfig;
        this.managerSeederConfig = managerSeederConfig;
        this.offerSeederConfig = offerSeederConfig;
    }

    @Bean
    public ApplicationRunner runAllSeeders() {
        return args -> {
            hotelSeederConfig.runHotelSeeder();
            managerSeederConfig.runManagerSeeder();
            offerSeederConfig.runOfferSeeder();
        };
    }
}
