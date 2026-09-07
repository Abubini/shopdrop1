package com.shopdrop.service;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderItem;
import com.shopdrop.model.OrderStatus;
import com.shopdrop.model.User;
import com.shopdrop.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
/**
 * Order placement and the order state machine.
 *
 * Allowed transitions:
 *   PLACED    -> PACKED, CANCELLED
 *   PACKED    -> SHIPPED, CANCELLED
 *   SHIPPED   -> DELIVERED
 *   DELIVERED -> (terminal)
 *   CANCELLED -> (terminal)
 *
 * Placing an order decreases stock for each item that carries a live product reference;
 * cancelling one restores it. Items built without a product reference (e.g. historical data)
 * are skipped for stock purposes rather than failing the order.
 */
@Service
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PLACED, EnumSet.of(OrderStatus.PACKED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PACKED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final DiscountService discountService;
    private final ShippingFeeCalculator shippingFeeCalculator;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository,
                         DiscountService discountService,
                         ShippingFeeCalculator shippingFeeCalculator,
                         ProductService productService) {
        this.orderRepository = orderRepository;
        this.discountService = discountService;
        this.shippingFeeCalculator = shippingFeeCalculator;
        this.productService = productService;
    }

    public Order placeOrder(User user, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with no items");
        }
        if (user == null) {
            throw new IllegalArgumentException("An order must belong to an account");
        }

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(user.getName());
        order.setCustomerEmail(user.getEmail());
        order.setMember(user.isMember());
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            if (item.getProduct() != null) {
                productService.decreaseStock(item.getProduct(), item.getQuantity());
            }
            order.addItem(item);
            subtotal = subtotal.add(item.getLineTotal());
        }

        BigDecimal discount = discountService.calculateDiscountAmount(subtotal, user.isMember());
        BigDecimal shipping = shippingFeeCalculator.calculate(subtotal);
        BigDecimal total = subtotal.subtract(discount).add(shipping);

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setShippingFee(shipping);
        order.setTotal(total);

        return orderRepository.save(order);
    }

    @Transactional
    public Order transition(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return transition(order, newStatus);
    }

    @Transactional
    public Order transition(Order order, OrderStatus newStatus) {
        OrderStatus current = order.getStatus();
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class));

        if (!allowed.contains(newStatus)) {
            throw new InvalidOrderTransitionException(
                    "Cannot transition order from " + current + " to " + newStatus);
        }

        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    productService.increaseStock(item.getProduct(), item.getQuantity());
                }
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public Order advance(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return transition(order, nextStatus(order.getStatus()));
    }

    @Transactional
    public Order cancel(Long orderId) {
        return transition(orderId, OrderStatus.CANCELLED);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    private OrderStatus nextStatus(OrderStatus current) {
        switch (current) {
            case PLACED:
                return OrderStatus.PACKED;
            case PACKED:
                return OrderStatus.SHIPPED;
            case SHIPPED:
                return OrderStatus.DELIVERED;
            default:
                throw new InvalidOrderTransitionException("Order is already in a terminal state: " + current);
        }
    }
}
