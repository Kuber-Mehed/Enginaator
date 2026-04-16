package ee.kubermehed.enginaator.repositories;

import ee.kubermehed.enginaator.models.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestItemRepository extends JpaRepository<RequestItem, UUID> {

}