package com.coderzoe.component.k8s.model;

import com.coderzoe.common.enums.EnvironmentTypeEnum;
import com.coderzoe.component.k8s.model.database.K8sEnvironmentDO;
import com.coderzoe.component.k8s.model.database.K8sServerDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yinhuasheng
 * @date 2024/8/19 15:37
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class K8sEnvAndServerInfo {
    private String envName;
    private String envDescription;
    private EnvironmentTypeEnum envType;
    private Set<K8sServerInfo> serverSet;

    public K8sEnvAndServerInfo(K8sEnvironmentDO env, List<K8sServerDO> serverList) {
        this.envName = env.getName();
        this.envDescription = env.getDescription();
        this.envType = env.getType();
        this.serverSet = serverList.stream().map(K8sServerInfo::new).collect(Collectors.toSet());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class K8sServerInfo {
        private String ip;
        private Integer port;
        private String userName;
        private String password;

        public K8sServerInfo(K8sServerDO server) {
            this.ip = server.getIp();
            this.port = server.getPort();
            this.userName = server.getUserName();
            this.password = server.getPassword();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            K8sServerInfo that = (K8sServerInfo) o;
            return Objects.equals(ip, that.ip) && Objects.equals(port, that.port);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ip, port);
        }
    }
}
