package com.ifx.vm_manager.infrastructure.adapters.output.persistence.repository;

import com.ifx.vm_manager.infrastructure.adapters.output.persistence.entity.VmEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VmR2dbcRepository extends ReactiveCrudRepository<VmEntity, Long> {}
