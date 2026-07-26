package com.example.backend.inbounds.repository;

import com.example.backend.inbounds.entity.Inbound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InboundRepository extends JpaRepository<Inbound, Long> {

    Page<Inbound> findByStatus(Inbound.Status status, Pageable pageable);

    Optional<Inbound> findByProductId(Long productId);
}
