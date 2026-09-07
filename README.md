# ShopDrop

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-3EB489?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3-3EB489?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 3.3">
  <img src="https://img.shields.io/badge/Selenium-4.25-3EB489?style=flat-square&logo=selenium&logoColor=white" alt="Selenium 4.25">
  <img src="https://img.shields.io/badge/JaCoCo-≥80%25%20branch-3EB489?style=flat-square" alt="JaCoCo coverage gate">
</p>

<p align="center">
  <!-- Replace <your-username>/<your-repo> below with your actual GitHub path once pushed -->
  <a href="https://github.com/abubini/shopdrop/actions/workflows/ci.yml">
    <img src="https://github.com/abubini/shopdrop/actions/workflows/ci.yml/badge.svg" alt="CI status">
  </a>
</p>

A small online shop built with **Spring Boot** (Java 17, Thymeleaf, Spring Security, Spring
Data JPA, H2) as the system under test for a complete Software Testing and Validation project:
the application, its unit/integration/system tests, and CI/CD pipelines all live in this repo.

---

## Course information

| | |
|---|---|
| **Institution** | Addis Ababa University — School of Information Technology and Engineering |
| **Course** | Software Testing and Validation |
| **Project** | Final Project — A Complete Testing Effort |
| **Instructor** | Abel Tadesse |

### Group members

| Name | ID |
|---|---|
| Biniyam Girma | ATE/7146/14 |
| Estifanos Yitayew | ATE/9512/14 |
| Yeabsira Addis | ATE/8574/14 |
| Yosef Ashebir | ATE/4638/14 |

---

## Table of contents

1. [What's in the app](#1-whats-in-the-app)
2. [Tech stack](#2-tech-stack)
3. [Getting started](#3-getting-started)
4. [Seeded accounts](#4-seeded-accounts)
5. [Application walkthrough](#5-application-walkthrough)
6. [Running the tests](#6-running-the-tests)
7. [Continuous integration — GitHub Actions](#7-continuous-integration--github-actions)
8. [Continuous integration — Jenkins](#8-continuous-integration--jenkins)
9. [Where each testing technique lives](#9-where-each-testing-technique-lives)
10. [Project structure](#10-project-structure)
11. [Academic integrity](#11-academic-integrity)

---

## 1. What's in the app

**Accounts & roles.** Anyone can register (`USER` role). A seeded `ADMIN` account manages the
store. Admins are staff, not shoppers: they land on a dashboard after login, never see a Cart
link, and are blocked (HTTP 403) from `/cart` and `/checkout` even if they type the URL
directly — enforced at the security layer, not just hidden in the UI.

**Catalog.** Products belong to a real `Category` entity. The catalog page supports filtering
by category and a case-insensitive text search by name.

**Inventory.** Stock is tracked per product. Adding to cart or updating a cart line is capped
at the product's current stock; placing an order decreases stock for real; cancelling an order
restores it.

**Reviews.** A customer can leave a 1–5 star rating and a comment on a product, but only once,
and only for a product from an order that has actually reached `DELIVERED`. The product page
shows the average rating and the review list.

**Admin CRUD.** `/admin/products` — create, edit, delete catalog items.

**Admin dashboard.** `/admin` — total products/users/orders, a per-status order breakdown, and
delivered revenue.

**Order ownership.** A regular user can only view/cancel their own orders; an admin can see and
manage all of them.

---

## 2. Tech stack

| Layer | Technology |
|---|---|
| Language / runtime | Java 17 |
| Framework | Spring Boot 3.3 (Web MVC, Security, Data JPA) |
| Templating | Thymeleaf + thymeleaf-extras-springsecurity6 |
| Database | H2 (in-memory) |
| Build | Maven |
| Unit / integration testing | JUnit 5, Mockito, Spring `MockMvc`, `spring-security-test` |
| System testing | Selenium 4.25 (headless Chrome), Page Object pattern |
| Coverage | JaCoCo (branch-coverage gate on the `service` package, ≥ 80%) |
| CI | GitHub Actions, Jenkins (declarative pipeline, Docker agent) |

---

## 3. Getting started

### Prerequisites

- JDK 17+
- Maven 3.9+ (or use the wrapper if you add one)
- Google Chrome, if you plan to run the Selenium system tests locally

### Run it

```bash
mvn spring-boot:run
```

Open **http://localhost:8080** — it redirects to the product catalog, which is public.
Logging in as the admin lands you on `/admin`; logging in as a regular user (or a fresh
registration) lands you on `/account`.

Data is in-memory H2 and reseeds fresh every time you start the app.

---

## 4. Seeded accounts

| Role  | Email               | Password   |
|-------|---------------------|------------|
| Admin | admin@shopdrop.com  | admin123   |
| User  | demo@shopdrop.com   | demo1234   |

*(The original brief listed the admin login as `admin.shopdrop.com`, which isn't a valid email
— it's seeded as `admin@shopdrop.com` above. Change it in `DataLoader` if you'd rather use
something else.)*

---

## 5. Application walkthrough

| Route | Access | What's there |
|---|---|---|
| `/products` | Public | Browse, filter by category, search by name |
| `/products/{id}` | Public | Detail, stock level, reviews. "Add to cart" needs a `USER` login |
| `/register`, `/login` | Public | Create an account or log in |
| `/cart`, `/checkout` | `USER` role only | Review the cart, place the order |
| `/orders/{id}` | Owner or admin | Status timeline, cancel while `PLACED`/`PACKED` |
| `/account` | Any logged-in user | Profile, membership toggle, order history |
| `/admin` | `ADMIN` only | Dashboard: counts, orders-by-status, delivered revenue |
| `/admin/products` | `ADMIN` only | Create, edit, delete catalog items |
| `/admin/orders` | `ADMIN` only | Advance an order `PLACED → PACKED → SHIPPED → DELIVERED`, or cancel it |

---

## 6. Running the tests

The suite is split by speed/dependency, matching the test pyramid:

```bash
mvn test      # unit tests + MockMvc integration tests + JaCoCo coverage check
mvn verify    # the above, PLUS the Selenium system tests (needs Chrome installed)
```

Run one specific test class:

```bash
mvn test -Dtest=DiscountServiceTest
mvn verify -Dit.test=ShoppingJourneyIT
```

### Unit tests — `src/test/java/com/shopdrop/service/` (8 classes)

Each tests one class in isolation with Mockito test doubles standing in for its
repository/collaborator dependencies: `CartServiceTest`, `DiscountServiceTest`,
`ShippingFeeCalculatorTest`, `OrderServiceTest`, `ProductServiceTest`, `UserServiceTest`,
`ReviewServiceTest`, `DashboardServiceTest`. Picked up automatically by Surefire (`*Test.java`).

### Integration tests — `src/test/java/com/shopdrop/integration/` (5 classes)

Boot the real Spring context and drive it through `MockMvc`: `AuthenticationIntegrationTest`
(register + login, including the role-based post-login redirect), `ProductCatalogIntegrationTest`
(catalog + category filtering), `OrderPlacementIntegrationTest` (cart → checkout → persisted
order → stock actually decremented), `ReviewIntegrationTest` (review eligibility end to end),
`AdminAuthorizationIntegrationTest` (every `/admin`, `/cart`, `/checkout`, `/account`
authorization rule). Also picked up by Surefire.

### System tests (Selenium) — `src/test/java/com/shopdrop/system/` (14 classes)

Named `*IT.java` so Maven **Failsafe** (not Surefire) picks them up on `mvn verify`. Each test
boots the full application on a random port and drives **headless Chrome** against it.
Selenium 4.6+'s built-in Selenium Manager downloads the matching chromedriver automatically —
you only need Chrome itself installed.

- **Page Object style** (`system/pages/`, 12 page objects) — `RegistrationAndLoginIT`,
  `ShoppingJourneyIT`, `CartManagementIT`, `SearchAndCategoryFilterIT`, `OrderCancellationIT`,
  `MembershipDiscountIT`, `AdminOrderManagementIT`, `AdminProductCrudIT`,
  `AdminRoleRestrictionsIT`.
- **Raw-locator style** (calls `driver.findElement(By...)` directly, no Page Objects) —
  `ReviewSubmissionRawLocatorsIT`, `RawLocatorShoppingAndCartIT`,
  `RawLocatorCheckoutOrderLifecycleIT`, `RawLocatorAccountAndStockLimitIT`,
  `RawLocatorAdminOperationsIT`. Kept deliberately alongside the Page Object tests as a direct
  comparison of the two styles — see the class comment at the top of each raw-locator file.

### Coverage

JaCoCo's report lands at `target/site/jacoco/index.html` after `mvn test`; open it in a
browser. The build **fails if branch coverage of the `service` package drops below 80%**,
since that's where the core business logic lives (pricing, discounts, the order state machine,
stock adjustment, review eligibility, registration/product validation, dashboard aggregation).

Surefire/Failsafe's raw XML reports (used by CI/Jenkins) land in `target/surefire-reports` and
`target/failsafe-reports`.

---

## 7. Continuous integration — GitHub Actions

Nothing to set up — `.github/workflows/ci.yml` runs automatically on every push or pull
request to `main`. It runs `mvn test`, then `mvn verify` (GitHub's `ubuntu-latest` runners
ship Chrome already), then uploads the JaCoCo report and JUnit XML results as downloadable
build artifacts from the Actions tab.

> To make the CI badge at the top of this file live, replace `<your-username>/<your-repo>` in
> both badge lines with your repository's actual path once it's pushed to GitHub.

To see it locally before pushing, just run the same two commands yourself:

```bash
mvn test
mvn verify
```

---

## 8. Continuous integration — Jenkins

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

The pipeline runs inside a `maven:3.9-eclipse-temurin-17` Docker agent: checkout → install
Chrome into the agent → `mvn test` (unit + integration + coverage check) → publish the JaCoCo
report as a build artifact → `mvn verify` (Selenium) → package the jar. JUnit results from
both test stages are published so failures show up per-test in the Jenkins UI. Full details in
`jenkins/README.md`.

**To demonstrate a regression** (useful for the test summary report): break an assertion in
one of the unit tests, push it, and show the "Unit & Integration Tests" stage fail with that
specific test named in the JUnit report — then revert and show the pipeline go green again.
The same works on the GitHub Actions side with the `mvn test` step.

---

## 9. Where each testing technique lives

| Technique | Class | Notes |
|---|---|---|
| Equivalence partitioning / boundary value analysis | `ShippingFeeCalculator`, `CartService` | Shipping-fee bands at $20/$100; cart quantity vs. available stock boundary. |
| Decision table | `DiscountService` | 3 spend bands × membership (yes/no) = 6 rules. |
| State transition testing | `OrderService` + `OrderStatus` | `PLACED → PACKED → SHIPPED → DELIVERED`; `CANCELLED` reachable only from `PLACED`/`PACKED`; cancelling also restores stock. |
| More validation logic | `UserService`, `ProductService`, `ReviewService` | Email format, password length, price/stock rules, 1–5 rating bounds, "purchased and delivered, not already reviewed". |
| Aggregation logic | `DashboardService` | Per-status order counts; revenue counted only for `DELIVERED` orders. |
| Authentication & authorization | `SecurityConfig`, `CustomUserDetailsService`, `OrderController` | Role-based post-login redirect; `ADMIN`-only routes; `USER`-only cart/checkout; order-ownership check. |
| Test doubles | `*ServiceTest` classes | Mockito mocks stand in for repositories/collaborators. |
| Integration testing | `src/test/java/com/shopdrop/integration/` | `MockMvc` + real Spring context + real (in-memory) DB. |
| System testing / Page Object pattern | `src/test/java/com/shopdrop/system/` | Selenium driving headless Chrome, in both Page Object and raw-locator styles. |
| Regression & CI | `.github/workflows/ci.yml`, `Jenkinsfile` | Both re-run the whole suite on every push. |

---

## 10. Project structure

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



---
