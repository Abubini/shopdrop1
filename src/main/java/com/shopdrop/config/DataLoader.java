package com.shopdrop.config;

import com.shopdrop.model.Category;
import com.shopdrop.model.Product;
import com.shopdrop.model.Role;
import com.shopdrop.model.User;
import com.shopdrop.repository.CategoryRepository;
import com.shopdrop.repository.ProductRepository;
import com.shopdrop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** Seeds a demo admin/user account and the product catalog on startup. */
@Component
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(ProductRepository productRepository, CategoryRepository categoryRepository,
                       UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedCatalog();
    }

    private void seedUsers() {
        if (userRepository.findByEmail("admin@shopdrop.com").isEmpty()) {
            User admin = new User();
            admin.setName("ShopDrop Admin");
            admin.setEmail("admin@shopdrop.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setMember(true);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
        }

        if (userRepository.findByEmail("demo@shopdrop.com").isEmpty()) {
            User demo = new User();
            demo.setName("Demo Shopper");
            demo.setEmail("demo@shopdrop.com");
            demo.setPassword(passwordEncoder.encode("demo1234"));
            demo.setRole(Role.USER);
            demo.setMember(false);
            demo.setCreatedAt(LocalDateTime.now());
            userRepository.save(demo);
        }
    }

    private void seedCatalog() {
        if (productRepository.count() > 0) {
            return;
        }

        Map<String, Category> categories = new HashMap<>();
        for (String name : new String[]{"Electronics", "Stationery", "Home", "Bags", "Furniture"}) {
            Category category = new Category();
            category.setName(name);
            categories.put(name, categoryRepository.save(category));
        }

        productRepository.save(new Product("Wireless Earbuds", "Compact earbuds with a charging case",
                categories.get("Electronics"), new BigDecimal("15.99"), 50, "\uD83C\uDFA7"));
        productRepository.save(new Product("Notebook Set", "Pack of 3 ruled notebooks",
                categories.get("Stationery"), new BigDecimal("8.50"), 100, "\uD83D\uDCD3"));
        productRepository.save(new Product("Ceramic Mug", "350ml mint-glazed mug",
                categories.get("Home"), new BigDecimal("12.00"), 80, "\u2615"));
        productRepository.save(new Product("Desk Lamp", "Adjustable LED desk lamp",
                categories.get("Home"), new BigDecimal("29.99"), 40, "\uD83D\uDCA1"));
        productRepository.save(new Product("Backpack", "Water-resistant daily backpack",
                categories.get("Bags"), new BigDecimal("45.00"), 30, "\uD83C\uDF92"));
        productRepository.save(new Product("Bluetooth Speaker", "Portable speaker with a 12-hour battery",
                categories.get("Electronics"), new BigDecimal("59.99"), 25, "\uD83D\uDD0A"));
        productRepository.save(new Product("Mechanical Keyboard", "Tactile keyboard with mint keycaps",
                categories.get("Electronics"), new BigDecimal("89.00"), 20, "\u2328\uFE0F"));
        productRepository.save(new Product("Office Chair", "Ergonomic mesh office chair",
                categories.get("Furniture"), new BigDecimal("149.00"), 10, "\uD83E\uDE91"));
        productRepository.save(new Product("Standing Desk", "Electric height-adjustable desk",
                categories.get("Furniture"), new BigDecimal("259.00"), 8, "\uD83D\uDDA5\uFE0F"));
        productRepository.save(new Product("Water Bottle", "1L insulated steel bottle",
                categories.get("Home"), new BigDecimal("18.75"), 60, "\uD83D\uDEB0"));
    }
}
