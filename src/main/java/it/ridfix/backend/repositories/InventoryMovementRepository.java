package it.ridfix.backend.repositories;

import it.ridfix.backend.entities.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {
    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(UUID productId);
}
