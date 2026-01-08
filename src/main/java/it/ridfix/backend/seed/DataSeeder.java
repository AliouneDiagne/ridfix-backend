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
            log.info("Seeded admin: admin@ridfix.local / Admin1234!");
        }
        if (!users.existsByEmailIgnoreCase("staff@ridfix.local")) {
            User staff = new User("staff@ridfix.local", encoder.encode("Staff1234!"), "Staff", "Ridfix", Role.STAFF);
            users.save(staff);
            log.info("Seeded staff: staff@ridfix.local / Staff1234!");
        }
    }

    private void seedCatalog() {
        if (categories.count() == 0) {
            categories.save(new Category("Screens"));
            categories.save(new Category("Batteries"));
            categories.save(new Category("Cases"));
            log.info("Seeded categories");
        }
        if (brands.count() == 0) {
            brands.save(new Brand("Apple"));
            brands.save(new Brand("Samsung"));
            brands.save(new Brand("Xiaomi"));
            log.info("Seeded brands");
        }
    }

    private void seedProducts() {
        if (products.count() > 0) return;

        Category screens = categories.findByNameIgnoreCase("Screens").orElseGet(() -> categories.save(new Category("Screens")));
        Category batteries = categories.findByNameIgnoreCase("Batteries").orElseGet(() -> categories.save(new Category("Batteries")));
        Category cases = categories.findByNameIgnoreCase("Cases").orElseGet(() -> categories.save(new Category("Cases")));

        Brand apple = brands.findByNameIgnoreCase("Apple").orElseGet(() -> brands.save(new Brand("Apple")));
        Brand samsung = brands.findByNameIgnoreCase("Samsung").orElseGet(() -> brands.save(new Brand("Samsung")));

        products.save(new SparePart(
                "iPhone 13 Screen OEM",
                "Original quality replacement screen for iPhone 13.",
                BigDecimal.valueOf(129.90),
                25,
                screens,
                apple,
                "OEM-IP13-SCR",
                "iPhone 13 / 13 Pro"
        ));

        products.save(new SparePart(
                "Galaxy S22 Battery",
                "High quality replacement battery for Samsung Galaxy S22.",
                BigDecimal.valueOf(49.90),
                40,
                batteries,
                samsung,
                "BAT-S22",
                "Galaxy S22"
        ));

        products.save(new Accessory(
                "Shockproof Case",
                "Shockproof phone case with reinforced corners.",
                BigDecimal.valueOf(19.90),
                100,
                cases,
                apple,
                "TPU",
                "Black"
        ));

        log.info("Seeded sample products");
    }
}
