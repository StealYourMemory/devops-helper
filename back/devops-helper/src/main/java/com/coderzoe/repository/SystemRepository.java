package com.coderzoe.repository;

import com.coderzoe.model.database.SystemDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author yinhuasheng
 * @date 2024/8/19 14:20
 */
public interface SystemRepository extends JpaRepository<SystemDO, Long> {
    /**
     * 根据key字段查找
     *
     * @param key key
     * @return 返回记录
     */
    Optional<SystemDO> findByKey(String key);
}
