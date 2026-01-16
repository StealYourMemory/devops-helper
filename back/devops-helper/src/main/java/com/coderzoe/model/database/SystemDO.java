package com.coderzoe.model.database;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * 系统表
 *
 * @author yinhuasheng
 * @date 2024/8/19 14:14
 */
@Entity
@Table(name = "dh_system")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SystemDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String key;
    private String value;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "update_time")
    private Date updateTime;

    public SystemDO(String key, String value, Date createTime, Date updateTime) {
        this.key = key;
        this.value = value;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
