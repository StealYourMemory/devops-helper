package com.coderzoe.component.k8s.repository;

import com.coderzoe.component.k8s.model.database.K8sEnvironmentDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:03
 */
public interface K8sEnvironmentRepository extends JpaRepository<K8sEnvironmentDO, Long> {
    /**
     * 查询环境信息
     *
     * @param name 环境名
     * @return 返回optional
     */
    Optional<K8sEnvironmentDO> findByName(String name);
}
