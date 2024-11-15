package at.ac.univie.inventorymgmtservice.service;

public interface IInventoryService {

    void handleIncomingTargetStockUpdateMessage(String message);

    void handleIncomingCurrentStockUpdateMessage(String message);

}
