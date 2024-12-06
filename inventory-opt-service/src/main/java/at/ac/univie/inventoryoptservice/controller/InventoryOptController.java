package at.ac.univie.inventoryoptservice.controller;

import at.ac.univie.inventoryoptservice.config.OptimizationConfig;
import at.ac.univie.inventoryoptservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@Slf4j
public class InventoryOptController {

    @Autowired
    private IInventoryService inventoryService;

    @GetMapping("/inventories")
    public ResponseEntity<?> testGetInventoryAllocation() {
        return inventoryService.fetchInventoryAllocation();
    }

    @PutMapping("/config")
    public ResponseEntity<?> updateOptimizationConfig(@RequestBody OptimizationConfig config) {
        return inventoryService.updateOptimizationConfig(config);
    }

    @GetMapping("/config")
    public ResponseEntity<?> getOptimizationConfig() {
        return inventoryService.getOptimizationConfig();
    }

    @PostMapping("/optimize")
    public ResponseEntity<?> processOptimizationRequest(@RequestBody String payload) {
        return inventoryService.processOptimizationRequest(payload);
    }
}
