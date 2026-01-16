package com.coderzoe.component.k8s.repository;

import com.coderzoe.component.k8s.model.database.K8sProxyRecordDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:04
 */
public interface K8sProxyRecordRepository extends JpaRepository<K8sProxyRecordDO, Long>, JpaSpecificationExecutor<K8sProxyRecordDO> {
}
