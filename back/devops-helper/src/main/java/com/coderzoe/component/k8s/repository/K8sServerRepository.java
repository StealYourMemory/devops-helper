package com.coderzoe.component.k8s.repository;

import com.coderzoe.component.k8s.model.database.K8sServerDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:04
 */
public interface K8sServerRepository extends JpaRepository<K8sServerDO, Long> {
    /**
     * 按环境删除
     *
     * @param environmentId 环境id
     */
    void deleteByEnvironmentId(long environmentId);

    /**
     * 根据ip和端口以及环境查询
     *
     * @param envId 环境id
     * @param ip    ip
     * @param port  端口
     * @return 返回服务器
     */
    Optional<K8sServerDO> findByEnvironmentIdAndIpAndPort(long envId, String ip, int port);
}
