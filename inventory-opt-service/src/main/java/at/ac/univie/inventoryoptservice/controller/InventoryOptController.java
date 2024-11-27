package at.ac.univie.inventoryoptservice.controller;

import at.ac.univie.inventoryoptservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
@Slf4j
public class InventoryOptController {

    @Autowired
    private IInventoryService inventoryService;

    @GetMapping
    public ResponseEntity<?> testGetInventoryAllocation() {
        return inventoryService.fetchInventoryAllocation();
    }
}
