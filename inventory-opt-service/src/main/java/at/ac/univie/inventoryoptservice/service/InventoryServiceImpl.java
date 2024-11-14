package at.ac.univie.inventoryoptservice.service;

import at.ac.univie.inventoryoptservice.model.InventoryAllocationDTO;
import at.ac.univie.inventoryoptservice.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl implements IInventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public ResponseEntity<?> fetchInventoryAllocation() {
        List<InventoryAllocationDTO> invAllocations = inventoryRepository.fetchInventoryWithWarehouseAndLocation();
        return ResponseEntity.ok(invAllocations);
    }
}
