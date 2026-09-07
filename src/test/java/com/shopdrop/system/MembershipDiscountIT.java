package com.shopdrop.system;

import com.shopdrop.system.pages.AccountPage;
import com.shopdrop.system.pages.CartPage;
import com.shopdrop.system.pages.CheckoutPage;
import com.shopdrop.system.pages.LoginPage;
import com.shopdrop.system.pages.ProductDetailPage;
import com.shopdrop.system.pages.ProductsPage;
import com.shopdrop.system.pages.RegisterPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the membership discount actually changes what a customer pays, through the UI.
 * Uses a freshly registered account rather than the shared demo@shopdrop.com fixture, so it
 * doesn't leave the shared account's membership toggled on for other tests.
 */
class MembershipDiscountIT extends BaseSystemTest {

    @Test
    void joiningMembership_changesTheDiscountedTotalForTheSameOrder() {
        String email = "membership.test." + System.currentTimeMillis() + "@shopdrop.com";
        new RegisterPage(driver).open(baseUrl).register("Membership Tester", email, "password123", "password123");
        new LoginPage(driver).loginAs(email, "password123");

        // First purchase as a non-member.
        new ProductsPage(driver).open(baseUrl).openProductByName("Office Chair");
        new ProductDetailPage(driver).addToCart(1);
        new CartPage(driver).open(baseUrl).goToCheckout();
        CheckoutPage checkoutPage = new CheckoutPage(driver);
        String totalAsNonMember = checkoutPage.getTotalText();
        checkoutPage.placeOrder();

        // Join membership from the account page.
        AccountPage accountPage = new AccountPage(driver).open(baseUrl);
        assertTrue(accountPage.getMembershipStatus().contains("Not a member"));
        accountPage.toggleMembership();
        accountPage = new AccountPage(driver).open(baseUrl);
        assertTrue(accountPage.getMembershipStatus().contains("Active"));

        // Second purchase of the same item, now as a member.
        new ProductsPage(driver).open(baseUrl).openProductByName("Office Chair");
        new ProductDetailPage(driver).addToCart(1);
        new CartPage(driver).open(baseUrl).goToCheckout();
        checkoutPage = new CheckoutPage(driver);
        String totalAsMember = checkoutPage.getTotalText();

        assertFalse(totalAsMember.equals(totalAsNonMember),
                "Expected the membership discount to change the total; both were: " + totalAsNonMember);
    }
}