package it.ridfix.backend.repositories;

import it.ridfix.backend.entities.Address;
import it.ridfix.backend.entities.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUserIdOrderByTypeAsc(UUID userId);

    Optional<Address> findByIdAndUserId(UUID id, UUID userId);

    // ✅ Reset “default” solo per quell’utente e per quel tipo (SHIPPING/BILLING)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Address a
           SET a.isDefault = false
           WHERE a.user.id = :userId
             AND a.type = :type
             AND a.isDefault = true
           """)
    int resetDefaultsForUserAndType(@Param("userId") UUID userId, @Param("type") AddressType type);
}
