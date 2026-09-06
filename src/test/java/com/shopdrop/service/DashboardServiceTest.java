package com.shopdrop.service;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderStatus;
import com.shopdrop.repository.OrderRepository;
import com.shopdrop.repository.ProductRepository;
import com.shopdrop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/** Tests the dashboard's aggregation logic in isolation with mocked repositories. */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(productRepository, userRepository, orderRepository);
    }

    private Order orderWith(OrderStatus status, String total) {
        Order order = new Order();
        order.setStatus(status);
        order.setTotal(new BigDecimal(total));
        return order;
    }

    @Test
    void computeStats_countsOrdersByStatusAndSumsOnlyDeliveredRevenue() {
        when(orderRepository.findAll()).thenReturn(List.of(
                orderWith(OrderStatus.PLACED, "10.00"),
                orderWith(OrderStatus.DELIVERED, "50.00"),
                orderWith(OrderStatus.DELIVERED, "25.00"),
                orderWith(OrderStatus.CANCELLED, "5.00")
        ));
        when(productRepository.count()).thenReturn(10L);
        when(userRepository.count()).thenReturn(4L);

        DashboardStats stats = dashboardService.computeStats();

        assertEquals(10L, stats.getTotalProducts());
        assertEquals(4L, stats.getTotalUsers());
        assertEquals(4L, stats.getTotalOrders());
        assertEquals(new BigDecimal("75.00"), stats.getTotalRevenue());
        assertEquals(1L, stats.getOrdersByStatus().get(OrderStatus.PLACED));
        assertEquals(2L, stats.getOrdersByStatus().get(OrderStatus.DELIVERED));
        assertEquals(1L, stats.getOrdersByStatus().get(OrderStatus.CANCELLED));
        assertEquals(0L, stats.getOrdersByStatus().get(OrderStatus.PACKED));
        assertEquals(0L, stats.getOrdersByStatus().get(OrderStatus.SHIPPED));
    }

    @Test
    void computeStats_withNoOrders_returnsZeroRevenueAndAllStatusesPresent() {
        when(orderRepository.findAll()).thenReturn(List.of());
        when(productRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);

        DashboardStats stats = dashboardService.computeStats();

        assertEquals(BigDecimal.ZERO, stats.getTotalRevenue());
        assertEquals(0L, stats.getTotalOrders());
        assertEquals(5, stats.getOrdersByStatus().size());
        for (OrderStatus status : OrderStatus.values()) {
            assertEquals(0L, stats.getOrdersByStatus().get(status));
        }
    }
}
