package com.flashsale.service;

import com.flashsale.model.Order;
import com.flashsale.model.Product;
import com.flashsale.repository.OrderRepository;
import com.flashsale.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FlashSaleService {

    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    // In-memory atomic stock counter per product — thread-safe
    private final ConcurrentHashMap<Long, AtomicInteger> stockMap
            = new ConcurrentHashMap<>();

    public FlashSaleService(ProductRepository productRepo,
                            OrderRepository orderRepo) {
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    @Transactional
    public Map<String, Object> placeOrder(Long productId,
                                          String userId, int quantity) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found: " + productId));

        // 1. Check sale active flag
        if (!product.isSaleActive()) {
            return fail("Flash sale is not active for this product.");
        }

        // 2. Check sale time window
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(product.getSaleStart())
                || now.isAfter(product.getSaleEnd())) {
            return fail("Flash sale window has ended or not started yet.");
        }

        // 3. Get or create AtomicInteger for this product's stock
        stockMap.putIfAbsent(productId,
                new AtomicInteger(product.getStock()));
        AtomicInteger atomicStock = stockMap.get(productId);

        // 4. Atomically deduct stock — thread-safe, no overselling
        int remaining = atomicStock.addAndGet(-quantity);
        if (remaining < 0) {
            atomicStock.addAndGet(quantity); // rollback
            return fail("Insufficient stock. Only "
                    + (remaining + quantity) + " unit(s) left.");
        }

        // 5. Persist stock and save order
        product.setStock(remaining);
        productRepo.save(product);

        double totalPrice = product.getSalePrice() * quantity;
        Order order = new Order(productId, userId, quantity, totalPrice);
        orderRepo.save(order);

        return Map.of(
                "status", "SUCCESS",
                "orderId", order.getId(),
                "product", product.getName(),
                "quantity", quantity,
                "pricePaid", totalPrice,
                "stockLeft", remaining
        );
    }

    public Product getProduct(Long productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public Product addProduct(Product product) {
        Product saved = productRepo.save(product);
        // Register fresh stock in the atomic map
        stockMap.put(saved.getId(),
                new AtomicInteger(product.getStock()));
        return saved;
    }

    // Called in @BeforeEach to clear stock map between tests
    public void clearStockCache() {
        stockMap.clear();
    }

    private Map<String, Object> fail(String message) {
        return Map.of("status", "FAILED", "reason", message);
    }
}