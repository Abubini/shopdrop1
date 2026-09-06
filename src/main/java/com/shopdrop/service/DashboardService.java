package com.shopdrop.service;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderStatus;
import com.shopdrop.repository.OrderRepository;
import com.shopdrop.repository.ProductRepository;
import com.shopdrop.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Aggregates store-wide numbers for the admin dashboard: counts and delivered revenue. */
@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public DashboardService(ProductRepository productRepository, UserRepository userRepository,
                             OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public DashboardStats computeStats() {
        List<Order> orders = orderRepository.findAll();

        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status, 0L);
        }

        BigDecimal revenue = BigDecimal.ZERO;
        for (Order order : orders) {
            byStatus.merge(order.getStatus(), 1L, Long::sum);
            if (order.getStatus() == OrderStatus.DELIVERED) {
                revenue = revenue.add(order.getTotal());
            }
        }

        return new DashboardStats(
                productRepository.count(),
                userRepository.count(),
                orders.size(),
                byStatus,
                revenue
        );
    }
}
