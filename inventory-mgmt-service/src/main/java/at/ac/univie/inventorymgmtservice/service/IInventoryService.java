package at.ac.univie.inventorymgmtservice.service;

public interface IInventoryService {

    void handleIncomingOrderItem(String message);

    void handleIncomingStockOptimization(String message);

}
