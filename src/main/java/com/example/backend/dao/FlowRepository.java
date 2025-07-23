package com.example.backend.dao;

import com.example.backend.entities.Flow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface FlowRepository extends JpaRepository<Flow,Long> {
    @Query("SELECT f FROM Flow f WHERE f.status = :status AND f.flow_type.id IN :flowTypeIds")
    List<Flow> findFlowsByStatusAndTypeId(@Param("status") Long status, @Param("flowTypeIds") List<Integer> flowTypeIds);

    @Query("SELECT f FROM Flow f " +
            "WHERE f.flow_type.id = 6 " +
            "AND f.proc.id IN (" +
            "    SELECT e.proc.id " +
            "    FROM EtlprocFlow e " +
            "    WHERE e.flow.id = :idFlow" +
            ")")
    List<Flow> findFlowsByFlowTypeAndProcInSubquery(@Param("idFlow") Long idFlow);
}
