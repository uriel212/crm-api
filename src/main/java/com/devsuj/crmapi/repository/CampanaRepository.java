package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.model.Campana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CampanaRepository extends JpaRepository<Campana, Integer>, JpaSpecificationExecutor<Campana> {
}
