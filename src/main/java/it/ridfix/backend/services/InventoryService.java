package it.ridfix.backend.services;

import it.ridfix.backend.entities.InventoryMovement;
import it.ridfix.backend.entities.Order;
import it.ridfix.backend.entities.enums.InventoryMovementType;
import it.ridfix.backend.entities.product.Product;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.repositories.InventoryMovementRepository;
import it.ridfix.backend.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InventoryService {

    private final ProductRepository products;
    private final InventoryMovementRepository movements;

    public InventoryService(ProductRepository products, InventoryMovementRepository movements) {
        this.products = products;
        this.movements = movements;
    }

    /**
     * Deve essere chiamato dentro una transazione (createOrder lo è già).
     * Usa pessimistic lock via products.findForUpdate.
     */
    public Product decrementStockForOrder(UUID productId, int qty, Order order) {
        Product product = products.findForUpdate(productId)
                .orElseThrow(() -> new ApiExceptions.NotFound("Product not found: " + productId));

        if (!product.isActive()) {
            throw new ApiExceptions.BadRequest("Product not available: " + product.getName());
        }
        if (qty <= 0) {
            throw new ApiExceptions.BadRequest("Quantity must be >= 1");
        }
        if (product.getStockQty() < qty) {
            throw new ApiExceptions.BadRequest("Not enough stock for " + product.getName());
        }

        product.setStockQty(product.getStockQty() - qty);

        movements.save(new InventoryMovement(
                product,
                InventoryMovementType.OUT,
                qty,
                "Order created",
                order
        ));

        return product;
    }
}
