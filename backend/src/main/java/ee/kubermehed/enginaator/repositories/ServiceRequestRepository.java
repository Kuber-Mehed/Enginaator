package ee.kubermehed.enginaator.repositories;

import ee.kubermehed.enginaator.models.GuestRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRequestRepository extends JpaRepository<GuestRequest, UUID> {
    public List<GuestRequest> findAllByOrderByCreatedAtDesc();
}