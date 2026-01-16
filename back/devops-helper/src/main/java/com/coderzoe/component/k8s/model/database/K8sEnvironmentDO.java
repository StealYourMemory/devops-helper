package com.coderzoe.component.k8s.model.database;

import com.coderzoe.common.enums.EnvironmentTypeEnum;
import com.coderzoe.component.k8s.model.K8sEnvAndServerInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * k8s环境信息
 *
 * @author yinhuasheng
 * @date 2024/8/19 15:38
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "dh_k8s_environment")
@NoArgsConstructor
@AllArgsConstructor
public class K8sEnvironmentDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private EnvironmentTypeEnum type;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;


    public K8sEnvironmentDO(K8sEnvAndServerInfo envAndServerInfo) {
        this.name = envAndServerInfo.getEnvName();
        this.description = envAndServerInfo.getEnvDescription();
        this.type = envAndServerInfo.getEnvType();
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}
