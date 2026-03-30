package com.ttn.ck.apn.repository;

import com.ttn.ck.apn.model.AiSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiSettingsRepository extends JpaRepository<AiSettings, Long> {
    
    default AiSettings getSingleton() {
        return findById(1L).orElse(null);
    }
}
