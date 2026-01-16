package com.coderzoe.service;

import com.coderzoe.common.util.JsonUtil;
import com.coderzoe.model.database.SystemDO;
import com.coderzoe.repository.SystemRepository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * 系统表的service
 *
 * @author yinhuasheng
 * @date 2024/8/19 14:19
 */
@Service
@Setter(onMethod = @__(@Autowired))
public class SystemService {
    private SystemRepository systemRepository;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public void saveOrUpdate(String key, Object value) {
        Optional<SystemDO> optionalSystem = systemRepository.findByKey(key);

        String valueStr;
        if (value instanceof String) {
            valueStr = (String) value;
        } else {
            valueStr = JsonUtil.toJsonString(value);
        }
        if (optionalSystem.isEmpty()) {
            systemRepository.save(new SystemDO(key, valueStr, new Date(), new Date()));
        } else {
            SystemDO system = optionalSystem.get();
            system.setValue(valueStr);
            system.setUpdateTime(new Date());
            systemRepository.save(system);
        }
    }

    public <T> T getByKey(String key, Class<T> tClass) {
        Optional<SystemDO> system = systemRepository.findByKey(key);
        if (system.isPresent()) {
            if (tClass == String.class) {
                return tClass.cast(system.get().getValue());
            } else {
                return JsonUtil.parseObject(system.get().getValue(), tClass);
            }
        }
        return null;
    }
}
