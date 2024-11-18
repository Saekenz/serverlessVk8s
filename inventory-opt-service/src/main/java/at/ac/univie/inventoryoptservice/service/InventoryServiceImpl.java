package at.ac.univie.inventoryoptservice.service;

import at.ac.univie.inventoryoptservice.config.PubSubConfiguration;
import at.ac.univie.inventoryoptservice.model.InventoryAllocationDTO;
import at.ac.univie.inventoryoptservice.model.StockUpdateDTO;
import at.ac.univie.inventoryoptservice.outbound.OutboundConfiguration;
import at.ac.univie.inventoryoptservice.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {
    private final OutboundConfiguration.PubSubOutboundGateway messagingGateway;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PubSubConfiguration pubSubConfiguration;

    @Override
    public ResponseEntity<?> fetchInventoryAllocation() {
        List<InventoryAllocationDTO> invAllocations = inventoryRepository.fetchInventoryWithLocation();
        return ResponseEntity.ok(invAllocations);
    }

    @Override
    public ResponseEntity<?> pubsubTest() {
        try {
            StockUpdateDTO stockUpdateDTO = new StockUpdateDTO(1L, 3L, 33);
            String stockUpdateJson = new ObjectMapper().writeValueAsString(stockUpdateDTO);

            messagingGateway.sendToPubSub(stockUpdateJson, pubSubConfiguration.getTopic());
            return ResponseEntity.ok().body(stockUpdateJson);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
