package com.coderzoe.component.k8s.model.database;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * @author yinhuasheng
 * @date 2024/8/19 15:40
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "dh_k8s_proxy_record")
@NoArgsConstructor
@AllArgsConstructor
public class K8sProxyRecordDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "env_name")
    private String envName;
    @Column(name = "proxy_host")
    private String proxyHost;
    @Column(name = "proxy_port")
    private Integer proxyPort;
    @Column(name = "src_host")
    private String srcHost;
    @Column(name = "src_port")
    private Integer srcPort;

    private String description;
    @Column(name = "create_time")
    private Date createTime;


    public K8sProxyRecordDO(String envName, String proxyHost, int proxyPort, String srcHost, int srcPort, String description) {
        this.envName = envName;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.srcHost = srcHost;
        this.srcPort = srcPort;
        this.description = description;
        this.createTime = new Date();
    }
}
