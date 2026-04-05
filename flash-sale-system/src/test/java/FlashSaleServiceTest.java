package com.flashsale;

import com.flashsale.model.Product;
import com.flashsale.service.FlashSaleService;
import com.flashsale.repository.ProductRepository;
import com.flashsale.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FlashSaleServiceTest {

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        flashSaleService.clearStockCache(); // clear AtomicInteger map
    }

    @Test
    void testSuccessfulOrder() {
        Product p = flashSaleService.addProduct(new Product(
                "Test TV", 50000.0, 25000.0, 5,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusHours(2)));
        Map<String, Object> result =
                flashSaleService.placeOrder(p.getId(), "user1", 1);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals(25000.0, result.get("pricePaid"));
    }

    @Test
    void testOutOfStock() {
        Product p = flashSaleService.addProduct(new Product(
                "Test TV", 50000.0, 25000.0, 5,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusHours(2)));
        flashSaleService.placeOrder(p.getId(), "user1", 5);
        Map<String, Object> result =
                flashSaleService.placeOrder(p.getId(), "user2", 1);
        assertEquals("FAILED", result.get("status"));
        assertTrue(result.get("reason").toString()
                .contains("Insufficient stock"));
    }

    @Test
    void testInactiveSale() {
        Product p = flashSaleService.addProduct(new Product(
                "Expired Laptop", 80000.0, 40000.0, 10,
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().minusHours(1)));
        Map<String, Object> result =
                flashSaleService.placeOrder(p.getId(), "user1", 1);
        assertEquals("FAILED", result.get("status"));
        assertTrue(result.get("reason").toString().contains("window"));
    }

    @Test
    void testCorrectSalePriceApplied() {
        Product p = flashSaleService.addProduct(new Product(
                "Test TV", 50000.0, 25000.0, 5,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusHours(2)));
        Map<String, Object> result =
                flashSaleService.placeOrder(p.getId(), "user1", 2);
        assertEquals("SUCCESS", result.get("status"));
        assertEquals(50000.0, result.get("pricePaid"));
    }

    @Test
    void testConcurrentHighDemand() throws InterruptedException {
        Product p = flashSaleService.addProduct(new Product(
                "Flash iPhone", 100000.0, 60000.0, 5,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusHours(1)));
        Long productId = p.getId();

        int totalUsers = 10;
        ExecutorService executor =
                Executors.newFixedThreadPool(totalUsers);
        CyclicBarrier barrier = new CyclicBarrier(totalUsers);
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        for (int i = 0; i < totalUsers; i++) {
            String userId = "user-" + i;
            futures.add(executor.submit(() -> {
                try { barrier.await(); }
                catch (Exception e) {
                    Thread.currentThread().interrupt(); }
                return flashSaleService.placeOrder(productId, userId, 1);
            }));
        }

        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);

        long successCount = futures.stream()
                .map(f -> {
                    try { return f.get(); }
                    catch (Exception e) {
                        return Map.of("status", "ERROR"); }
                })
                .filter(r -> "SUCCESS".equals(r.get("status")))
                .count();

        assertEquals(5, successCount,
                "Only 5 orders should succeed — no overselling!");
    }

    @Test
    void testProductNotFound() {
        assertThrows(RuntimeException.class, () ->
                flashSaleService.placeOrder(9999L, "user1", 1));
    }
}