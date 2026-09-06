# ShopDrop

A small online shop built with **Spring Boot** (Java 17, Thymeleaf, Spring Security, Spring
Data JPA, H2) for the Software Testing and Validation final project.

> **Assumption on the admin login**: the brief said "admin email and password is
> (admin.shopdrop.com, pass: admin123)" — since that isn't a valid email address, it's seeded
> as **admin@shopdrop.com / admin123**. Change it in `DataLoader` if you'd rather use something
> else.

---

## 1. What's in the app

**Accounts & roles.** Anyone can register (`USER` role). A seeded `ADMIN` account manages the
store. Admins are staff, not shoppers: they land on a dashboard after login, don't see a Cart
link, and are blocked (403) from `/cart` and `/checkout` even if they type the URL directly.

**Catalog.** Products belong to a real `Category` entity. The catalog page supports filtering
by category and a text search by name.

**Inventory.** Stock is tracked per product. Adding to cart or updating a cart line is capped
at the product's current stock; placing an order decreases stock for real; cancelling an order
puts it back.

**Reviews.** A customer can leave a 1–5 star rating and a comment on a product, but only once,
and only for a product from an order that's actually reached `DELIVERED`. The product page
shows the average rating and the review list.

**Admin CRUD.** `/admin/products` — create, edit, delete catalog items.

**Admin dashboard.** `/admin` — total products/users/orders, a per-status order breakdown, and
delivered revenue.

**Order ownership.** A regular user can only view/cancel their own orders; an admin can see and
manage all of them.

### Seeded accounts

| Role  | Email               | Password   |
|-------|---------------------|------------|
| Admin | admin@shopdrop.com  | admin123   |
| User  | demo@shopdrop.com   | demo1234   |

---

## 2. Running the application

```bash
mvn spring-boot:run
```

Open **http://localhost:8080** — it redirects to the product catalog, which is public.
Logging in as the admin lands you on `/admin`; logging in as a regular user (or your own new
registration) lands you on `/account`.

Stop it with `Ctrl+C`. Data is in-memory H2 and reseeds fresh every time you start it.

---

## 3. Running the tests

The suite is split by speed/dependency, matching the test pyramid:

```bash
mvn test      # unit tests + MockMvc integration tests + JaCoCo coverage check
mvn verify    # the above, PLUS the Selenium system tests (needs Chrome installed)
```

- **Unit tests** — `src/test/java/com/shopdrop/service/` (8 classes). Each tests one class in
  isolation with Mockito test doubles standing in for its repository/collaborator
  dependencies. Picked up automatically by Surefire (`*Test.java`).
- **Integration tests** — `src/test/java/com/shopdrop/integration/` (5 classes). Boot the real
  Spring context and drive it through MockMvc: registration + login (including the
  role-based redirect), product CRUD through to the rendered catalog, cart + checkout
  persisting a real order and decrementing stock, review submission end to end, and the
  authorization rules for `/admin/**`, `/cart`, `/checkout` and `/account`. Also run by
  Surefire.
- **System tests (Selenium)** — `src/test/java/com/shopdrop/system/` (6 test classes, named
  `*IT.java` so Maven **Failsafe** picks them up on `mvn verify`, plus a shared
  `BaseSystemTest` base class). Each test boots the full application on a random port and
  drives **headless Chrome** against it. Most use the Page Objects in `system/pages/`;
  `ReviewSubmissionRawLocatorsIT` deliberately skips the Page Objects and calls
  `driver.findElement(By...)` directly, as a second style to compare against — see the
  comment at the top of that file. Selenium 4.6+'s built-in Selenium Manager downloads the
  matching chromedriver automatically; you only need Chrome itself installed.

Run one specific test class:

```bash
mvn test -Dtest=DiscountServiceTest
mvn verify -Dit.test=ShoppingJourneyIT
```

JaCoCo's report lands at `target/site/jacoco/index.html` after `mvn test`; open it in a
browser. The build **fails if branch coverage of the `service` package drops below 80%**,
since that's where the core business logic lives (pricing, discounts, the order state
machine, stock adjustment, review eligibility, registration/product validation, dashboard
aggregation).

Surefire/Failsafe's raw XML reports (used by CI/Jenkins) land in `target/surefire-reports`
and `target/failsafe-reports`.

---

## 4. Running it through GitHub Actions

Nothing to set up — `.github/workflows/ci.yml` runs automatically on every push or pull
request to `main`. It runs `mvn test`, then `mvn verify` (GitHub's `ubuntu-latest` runners
ship Chrome already), then uploads the JaCoCo report and JUnit XML results as downloadable
build artifacts from the Actions tab.

To see it locally before pushing, just run the same two commands yourself:

```bash
mvn test
mvn verify
```

---

## 5. Running it through Jenkins

The repo includes a `Jenkinsfile` plus a Docker Compose file to run Jenkins itself locally.

```bash
cd jenkins
docker compose up -d
```

Then:

1. Open http://localhost:8080 and unlock Jenkins with:
   `docker exec shopdrop-jenkins cat /var/jenkins_home/secrets/initialAdminPassword`
2. Install the suggested plugins, plus **Docker Pipeline** and **JUnit**.
3. New Item → Pipeline → name it `shopdrop-ci` → Pipeline script from SCM → point it at this
   repo, script path `Jenkinsfile` → Save → Build Now.

The pipeline (defined in `Jenkinsfile`) runs inside a `maven:3.9-eclipse-temurin-17` Docker
agent: checkout → install Chrome into the agent → `mvn test` (unit + integration + coverage
check) → publish the JaCoCo report as a build artifact → `mvn verify` (Selenium) → package the
jar. JUnit results from both test stages are published so failures show up per-test in the
Jenkins UI. Full details in `jenkins/README.md`.

**To demonstrate a regression** (useful for the test summary report): break an assertion in
one of the unit tests, push it, and show the "Unit & Integration Tests" stage fail with that
specific test named in the JUnit report — then revert and show the pipeline go green again.
The same works on the GitHub Actions side with the `mvn test` step.

---

## 6. Application walkthrough

1. `/products` (public) — browse, filter by category, search by name.
2. `/products/{id}` (public) — detail, stock level, reviews. "Add to cart" needs login as a
   `USER`; a delivered-order review form appears once you're eligible.
3. `/register`, `/login` — create an account or log in.
4. `/cart`, `/checkout` (USER role only) — review the cart, place the order.
5. `/orders/{id}` (owner or admin only) — status timeline, cancel while PLACED/PACKED.
6. `/account` — profile, membership toggle, order history.
7. `/admin` (ADMIN only) — dashboard: counts, orders-by-status, delivered revenue.
8. `/admin/products` (ADMIN only) — create, edit, delete catalog items.
9. `/admin/orders` (ADMIN only) — advance an order PLACED → PACKED → SHIPPED → DELIVERED, or
   cancel it.

---

## 7. Where each testing technique has something to bite into

| Technique | Class | Notes |
|---|---|---|
| Equivalence partitioning / boundary value analysis | `ShippingFeeCalculator`, `CartService` | Shipping fee bands at $20/$100; cart quantity vs. available stock boundary. |
| Decision table | `DiscountService` | 3 spend bands × membership (yes/no) = 6 rules. |
| State transition testing | `OrderService` + `OrderStatus` | `PLACED → PACKED → SHIPPED → DELIVERED`, `CANCELLED` reachable only from `PLACED`/`PACKED`; cancelling also restores stock. |
| More validation logic | `UserService`, `ProductService`, `ReviewService` | Email format, password length, price/stock rules, 1–5 rating bounds, "purchased and delivered, not already reviewed". |
| Aggregation logic | `DashboardService` | Per-status order counts, revenue counted only for DELIVERED orders. |
| Authentication & authorization | `SecurityConfig`, `CustomUserDetailsService`, `OrderController` | Role-based post-login redirect; ADMIN-only routes; USER-only cart/checkout; order ownership check. |
| Test doubles | `*ServiceTest` classes | Mockito mocks stand in for repositories/collaborators. |
| Integration testing | `src/test/java/com/shopdrop/integration/` | MockMvc + real Spring context + real (in-memory) DB. |
| System testing / Page Object pattern | `src/test/java/com/shopdrop/system/` | Selenium driving headless Chrome; one test (`ReviewSubmissionRawLocatorsIT`) uses raw locators instead, as a comparison. |
| Regression & CI | `.github/workflows/ci.yml`, `Jenkinsfile` | Both re-run the whole suite on every push. |

---

## 8. Project layout

```
src/main/java/com/shopdrop/
  model/         User, Role, Category, Product, Order, OrderItem, OrderStatus, Review
  repository/    Spring Data JPA repositories
  security/      SecurityConfig, CustomUserDetailsService
  service/       CartService, DiscountService, ShippingFeeCalculator, OrderService,
                 UserService, ProductService, ReviewService, DashboardService, DashboardStats
  controller/    Product/Cart/Checkout/Order/Account/Auth/AdminProduct/AdminDashboard/Review
                 controllers, GlobalExceptionHandler, StaticPagesController (403 page)
  config/        DataLoader (seeds accounts + catalog)
src/main/resources/
  templates/     Thymeleaf views (Mint Green / White / Slate Gray, light mode)
  static/css/    style.css
src/test/java/com/shopdrop/
  service/       Unit tests (Mockito test doubles) - 8 classes
  integration/   MockMvc integration tests - 5 classes
  system/        Selenium system tests (*IT.java) - 6 classes + BaseSystemTest + system/pages/ (Page Objects)
.github/workflows/ci.yml   GitHub Actions pipeline
Jenkinsfile                 Jenkins declarative pipeline
jenkins/                    Docker Compose setup to run Jenkins locally + instructions
```
