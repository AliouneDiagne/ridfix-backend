package it.ridfix.backend.repositories;

import it.ridfix.backend.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserIdOrderByTypeAsc(UUID userId);
    Optional<Address> findByIdAndUserId(UUID id, UUID userId);
}
