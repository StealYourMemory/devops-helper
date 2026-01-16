package com.coderzoe.repository;

import com.coderzoe.model.database.OperationLogDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * @author yinhuasheng
 * @date 2024/8/19 14:21
 */
public interface OperationLogRepository extends JpaRepository<OperationLogDO, Long>, JpaSpecificationExecutor<OperationLogDO> {
    /**
     * 根据requestId查询
     *
     * @param requestId requestId
     * @return 返回记录
     */
    Optional<OperationLogDO> findByRequestId(String requestId);
}
