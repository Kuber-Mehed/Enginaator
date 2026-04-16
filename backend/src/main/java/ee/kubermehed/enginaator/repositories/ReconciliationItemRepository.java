package ee.kubermehed.enginaator.repositories;

import ee.kubermehed.enginaator.models.ReconciliationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, UUID> {

}