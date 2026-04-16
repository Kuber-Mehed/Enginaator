package ee.kubermehed.enginaator.repositories;

import ee.kubermehed.enginaator.models.InventoryItem;
import ee.kubermehed.enginaator.models.Reconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReconciliationRepository extends JpaRepository<Reconciliation, UUID> {

}