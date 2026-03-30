package com.ttn.ck.apn.repository;

import com.ttn.ck.apn.model.CustomerMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerMappingRepository extends JpaRepository<CustomerMapping, String> {
}
