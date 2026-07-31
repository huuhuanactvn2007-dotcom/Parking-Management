package com.hotrodoan.repository;

import com.hotrodoan.model.dto.RegularPassSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegularPassSubRepository extends JpaRepository<RegularPassSub, Long>{
}
