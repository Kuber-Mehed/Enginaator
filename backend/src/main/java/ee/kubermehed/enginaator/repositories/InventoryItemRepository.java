package ee.kubermehed.enginaator.repositories;

import ee.kubermehed.enginaator.models.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    List<InventoryItem> findAllByNameIn(Collection<String> names);
}