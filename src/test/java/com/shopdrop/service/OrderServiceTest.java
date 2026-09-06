package com.shopdrop.service;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderItem;
import com.shopdrop.model.OrderStatus;
import com.shopdrop.model.Product;
import com.shopdrop.model.User;
import com.shopdrop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests OrderService in isolation using Mockito mocks (test doubles) for OrderRepository and
 * ProductService, so no real database and no real stock mutation is involved.
 *
 * State transition coverage:
 *   Valid:   PLACED->PACKED, PACKED->SHIPPED, SHIPPED->DELIVERED, PLACED->CANCELLED, PACKED->CANCELLED
 *   Invalid: PLACED->SHIPPED (skips a state), DELIVERED->CANCELLED (terminal),
 *            CANCELLED->PACKED (terminal), SHIPPED->CANCELLED (not allowed once shipped)
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, new DiscountService(), new ShippingFeeCalculator(), productService);
        // Test double behaviour: save() just echoes back whatever it was given.
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private User sampleUser(boolean member) {
        User user = new User();
        user.setName("Abel Tadesse");
        user.setEmail("abel@example.com");
        user.setMember(member);
        return user;
    }

    private OrderItem sampleItem() {
        OrderItem item = new OrderItem();
        item.setProductName("Test Product");
        item.setUnitPrice(new BigDecimal("25.00"));
        item.setQuantity(2);
        item.setLineTotal(new BigDecimal("50.00"));
        return item;
    }

    private Order orderWithStatus(OrderStatus status) {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(status);
        return order;
    }

    @Test
    void placeOrder_setsStatusToPlacedAndComputesTotals() {
        Order order = orderService.placeOrder(sampleUser(false), List.of(sampleItem()));
        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertEquals(new BigDecimal("50.00"), order.getSubtotal());
    }

    @Test
    void placeOrder_snapshotsCustomerDetailsFromTheAccount() {
        Order order = orderService.placeOrder(sampleUser(true), List.of(sampleItem()));
        assertEquals("Abel Tadesse", order.getCustomerName());
        assertEquals("abel@example.com", order.getCustomerEmail());
        assertEquals(true, order.isMember());
    }

    @Test
    void placeOrder_withNoItems_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(sampleUser(false), List.of()));
    }

    @Test
    void placeOrder_withNullItems_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(sampleUser(false), null));
    }

    @Test
    void placeOrder_withNullUser_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(null, List.of(sampleItem())));
    }

    @Test
    void placedToPacked_isAllowed() {
        Order order = orderWithStatus(OrderStatus.PLACED);
        Order result = orderService.transition(order, OrderStatus.PACKED);
        assertEquals(OrderStatus.PACKED, result.getStatus());
    }

    @Test
    void packedToShipped_isAllowed() {
        Order order = orderWithStatus(OrderStatus.PACKED);
        Order result = orderService.transition(order, OrderStatus.SHIPPED);
        assertEquals(OrderStatus.SHIPPED, result.getStatus());
    }

    @Test
    void shippedToDelivered_isAllowed() {
        Order order = orderWithStatus(OrderStatus.SHIPPED);
        Order result = orderService.transition(order, OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, result.getStatus());
    }

    @Test
    void placedToCancelled_isAllowed() {
        Order order = orderWithStatus(OrderStatus.PLACED);
        Order result = orderService.transition(order, OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void packedToCancelled_isAllowed() {
        Order order = orderWithStatus(OrderStatus.PACKED);
        Order result = orderService.transition(order, OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void placedToShipped_isRejected() {
        Order order = orderWithStatus(OrderStatus.PLACED);
        assertThrows(InvalidOrderTransitionException.class,
                () -> orderService.transition(order, OrderStatus.SHIPPED));
    }

    @Test
    void shippedToCancelled_isRejected() {
        Order order = orderWithStatus(OrderStatus.SHIPPED);
        assertThrows(InvalidOrderTransitionException.class,
                () -> orderService.transition(order, OrderStatus.CANCELLED));
    }

    @Test
    void deliveredIsTerminal() {
        Order order = orderWithStatus(OrderStatus.DELIVERED);
        assertThrows(InvalidOrderTransitionException.class,
                () -> orderService.transition(order, OrderStatus.CANCELLED));
    }

    @Test
    void cancelledIsTerminal() {
        Order order = orderWithStatus(OrderStatus.CANCELLED);
        assertThrows(InvalidOrderTransitionException.class,
                () -> orderService.transition(order, OrderStatus.PACKED));
    }

    @Test
    void advance_movesThroughFullLifecycleOneStepAtATime() {
        Order order = orderWithStatus(OrderStatus.PLACED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.advance(1L);
        assertEquals(OrderStatus.PACKED, order.getStatus());

        orderService.advance(1L);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        orderService.advance(1L);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void advance_onDeliveredOrder_throwsException() {
        Order order = orderWithStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(InvalidOrderTransitionException.class, () -> orderService.advance(1L));
    }

    @Test
    void transition_onUnknownOrder_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> orderService.transition(99L, OrderStatus.PACKED));
    }

    @Test
    void findById_onUnknownOrder_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> orderService.findById(99L));
    }

    @Test
    void cancel_delegatesToTransitionWithCancelledStatus() {
        Order order = orderWithStatus(OrderStatus.PLACED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.cancel(1L);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void findByUser_delegatesToRepository() {
        User user = sampleUser(false);
        Order order = orderWithStatus(OrderStatus.PLACED);
        when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(order));

        List<Order> orders = orderService.findByUser(user);
        assertEquals(1, orders.size());
    }

    @Test
    void placeOrder_withProductReference_decreasesStockForThatProduct() {
        Product product = new Product();
        product.setId(10L);
        product.setStockQuantity(5);

        OrderItem item = sampleItem();
        item.setProduct(product);

        orderService.placeOrder(sampleUser(false), List.of(item));

        verify(productService).decreaseStock(product, item.getQuantity());
    }

    @Test
    void placeOrder_withoutProductReference_doesNotTouchStock() {
        orderService.placeOrder(sampleUser(false), List.of(sampleItem()));
        verifyNoInteractions(productService);
    }

    @Test
    void cancellingAPlacedOrder_restoresStockForEachItemWithAProduct() {
        Product product = new Product();
        product.setId(10L);
        product.setStockQuantity(2);

        OrderItem item = sampleItem();
        item.setProduct(product);

        Order order = orderWithStatus(OrderStatus.PLACED);
        order.addItem(item);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancel(1L);

        verify(productService).increaseStock(product, item.getQuantity());
    }

    @Test
    void advancingAnOrder_doesNotTouchStock() {
        Order order = orderWithStatus(OrderStatus.PLACED);
        order.addItem(sampleItem());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.advance(1L);

        verifyNoInteractions(productService);
    }
}
