package com.coderzoe.model.database;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * 日志表
 *
 * @author yinhuasheng
 * @date 2024/8/19 14:14
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dh_operation_log")
public class OperationLogDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String user;
    @Column(name = "request_id")
    private String requestId;
    private String operation;
    @Column(name = "request_detail")
    private String requestDetail;
    private Boolean success;
    @Column(name = "result_detail")
    private String resultDetail;
    @Column(name = "start_time")
    private Date startTime;
    @Column(name = "end_time")
    private Date endTime;
}
