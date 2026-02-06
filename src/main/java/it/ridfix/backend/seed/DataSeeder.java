package it.ridfix.backend.seed;

import it.ridfix.backend.entities.Brand;
import it.ridfix.backend.entities.Category;
import it.ridfix.backend.entities.User;
import it.ridfix.backend.entities.enums.Role;
import it.ridfix.backend.entities.product.Accessory;
import it.ridfix.backend.entities.product.SparePart;
import it.ridfix.backend.repositories.BrandRepository;
import it.ridfix.backend.repositories.CategoryRepository;
import it.ridfix.backend.repositories.ProductRepository;
import it.ridfix.backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final ProductRepository products;

    public DataSeeder(UserRepository users,
                      PasswordEncoder encoder,
                      CategoryRepository categories,
                      BrandRepository brands,
                      ProductRepository products) {
        this.users = users;
        this.encoder = encoder;
        this.categories = categories;
        this.brands = brands;
        this.products = products;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedCatalog();
        seedProducts();
    }

    private void seedUsers() {
        if (!users.existsByEmailIgnoreCase("admin@ridfix.local")) {
            User admin = new User("admin@ridfix.local", encoder.encode("Admin1234!"), "Admin", "Ridfix", Role.ADMIN);
            users.save(admin);
            log.info("Seeded admin: admin@ridfix.local");
        }
        if (!users.existsByEmailIgnoreCase("staff@ridfix.local")) {
            User staff = new User("staff@ridfix.local", encoder.encode("Staff1234!"), "Staff", "Ridfix", Role.STAFF);
            users.save(staff);
        }
    }

    private void seedCatalog() {
        if (categories.count() == 0) {
            // Nomi sincronizzati con CATEGORIES nel frontend
            categories.save(new Category("Motore"));
            categories.save(new Category("Carrozzeria"));
            categories.save(new Category("Accessori"));
            categories.save(new Category("Detersivi"));
            categories.save(new Category("Utensili"));
            categories.save(new Category("Elettrici"));
            log.info("Seeded categories matching frontend");
        }
        if (brands.count() == 0) {
            // Nomi sincronizzati con BRANDS nel frontend
            brands.save(new Brand("Polini"));
            brands.save(new Brand("Malossi"));
            brands.save(new Brand("Piaggio"));
            brands.save(new Brand("Yamaha"));
            brands.save(new Brand("Aprilia"));
            log.info("Seeded brands matching frontend");
        }
    }

    private void seedProducts() {
        if (products.count() > 0) return;

        Category motore = categories.findByNameIgnoreCase("Motore").orElseThrow();
        Category carrozzeria = categories.findByNameIgnoreCase("Carrozzeria").orElseThrow();
        Category accessori = categories.findByNameIgnoreCase("Accessori").orElseThrow();

        Brand malossi = brands.findByNameIgnoreCase("Malossi").orElseThrow();
        Brand polini = brands.findByNameIgnoreCase("Polini").orElseThrow();
        Brand piaggio = brands.findByNameIgnoreCase("Piaggio").orElseThrow();
        Brand yamaha = brands.findByNameIgnoreCase("Yamaha").orElseThrow();

        // Esempi SPARE PART (Ricambi)
        products.save(new SparePart(
                "Cilindro 70cc Malossi MHR",
                "Cilindro ad alte prestazioni in alluminio per motori Piaggio.",
                BigDecimal.valueOf(185.00),
                10, motore, malossi,
                "MAL-70-CIL", "Piaggio Zip SP / NRG"
        ));

        products.save(new SparePart(
                "Variatore Polini Hi-Speed",
                "Kit variatore per migliorare l'accelerazione.",
                BigDecimal.valueOf(65.00),
                20, motore, polini,
                "POL-VAR-66", "Yamaha Aerox / Booster"
        ));

        products.save(new SparePart(
                "Carena Anteriore Nera",
                "Scudo anteriore di ricambio, colore nero opaco.",
                BigDecimal.valueOf(45.50),
                5, carrozzeria, piaggio,
                "PIA-CAR-01", "Vespa Primavera / Sprint"
        ));

        // Esempi ACCESSORY (Accessori)
        products.save(new Accessory(
                "Casco Integrale Yamaha Racing",
                "Casco protettivo omologato con livrea ufficiale.",
                BigDecimal.valueOf(129.00),
                12, accessori, yamaha,
                "Policarbonato", "Blu/Bianco"
        ));

        log.info("Products seeded and aligned with Ridfix frontend");
    }
}