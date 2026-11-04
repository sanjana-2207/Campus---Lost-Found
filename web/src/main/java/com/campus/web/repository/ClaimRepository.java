package com.campus.web.repository;

import com.campus.web.model.Claim;
import com.campus.web.model.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByClaimantIdOrderBySubmittedAtDesc(Long claimantId);

    @Query("select c from Claim c where c.item.reporter.id = :reporterId "
            + "order by c.submittedAt desc")
    List<Claim> findForReporter(@Param("reporterId") Long reporterId);

    List<Claim> findAllByOrderBySubmittedAtDesc();

    List<Claim> findByItemIdAndStatus(Long itemId, ClaimStatus status);

    List<Claim> findByItemIdAndStatusAndIdNot(Long itemId, ClaimStatus status, Long id);

    boolean existsByItemIdAndClaimantIdAndStatusIn(Long itemId, Long claimantId,
                                                   Collection<ClaimStatus> statuses);

    long countByStatus(ClaimStatus status);
}
