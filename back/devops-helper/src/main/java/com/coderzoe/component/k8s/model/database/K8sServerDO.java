package com.coderzoe.component.k8s.model.database;

import com.coderzoe.component.k8s.model.K8sEnvAndServerInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * k8s服务器信息
 *
 * @author yinhuasheng
 * @date 2024/8/19 15:40
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "dh_k8s_server")
@NoArgsConstructor
@AllArgsConstructor
public class K8sServerDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 当前服务器所属的环境
     */
    @Column(name = "environment_id")
    private Long environmentId;
    private String ip;
    private Integer port;
    @Column(name = "user_name")
    private String userName;
    private String password;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "update_time")
    private Date updateTime;

    public K8sServerDO(K8sEnvAndServerInfo.K8sServerInfo serverInfo, long environmentId) {
        this.environmentId = environmentId;
        this.ip = serverInfo.getIp();
        this.port = serverInfo.getPort();
        this.userName = serverInfo.getUserName();
        this.password = serverInfo.getPassword();
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}
