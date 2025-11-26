// src/main/java/com/cognitive/banking/repository/BranchRepository.java
package com.cognitive.banking.repository;

import com.cognitive.banking.domain.entity.Branch;
import com.cognitive.banking.domain.enums.BranchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByBranchCode(String branchCode);
    List<Branch> findByBranchType(BranchType branchType);
    List<Branch> findByCity(String city);
}