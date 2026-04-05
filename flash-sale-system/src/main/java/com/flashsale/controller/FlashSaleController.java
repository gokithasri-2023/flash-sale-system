package com.flashsale.controller;

import com.flashsale.model.Product;
import com.flashsale.service.FlashSaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/flash-sale")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    public FlashSaleController(FlashSaleService flashSaleService) {
        this.flashSaleService = flashSaleService;
    }

    // POST /api/flash-sale/order?productId=1&userId=user1&quantity=2
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestParam Long productId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "1") int quantity) {

        Map<String, Object> result = flashSaleService.placeOrder(productId, userId, quantity);
        return ResponseEntity.ok(result);
    }

    // GET /api/flash-sale/product/1
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(flashSaleService.getProduct(id));
    }

    // POST /api/flash-sale/product  — add a product (admin use)
    @PostMapping("/product")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        return ResponseEntity.ok(flashSaleService.addProduct(product));
    }
}