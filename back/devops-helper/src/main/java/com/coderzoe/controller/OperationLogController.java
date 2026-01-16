package com.coderzoe.controller;

import com.coderzoe.common.Result;
import com.coderzoe.common.log.Log;
import com.coderzoe.model.database.OperationLogDO;
import com.coderzoe.model.request.OperationLogFilterRequest;
import com.coderzoe.repository.OperationLogRepository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 日志controller 查询日志
 *
 * @author yinhuasheng
 * @date 2024/8/19 19:24
 */
@RestController
@RequestMapping("/operation-log")
@Setter(onMethod = @__(@Autowired))
public class OperationLogController {
    private OperationLogRepository operationLogRepository;

    @Log
    @PostMapping("/log")
    public Result<OperationLogFilterRequest> getLog(@RequestBody OperationLogFilterRequest request) {
        Pageable pageable;
        //注：为aot的可能，不要用TypedSort 详见 https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html
        Sort sort = Sort.by("id").descending();
        if (request.getPaging()) {
            pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);
        } else {
            pageable = Pageable.unpaged(sort);
        }

        Specification<OperationLogDO> spec = Specification.where(null);

        if (StringUtils.hasLength(request.getUser())) {
            spec = spec.and(Specifications.nameStartsWith(request.getUser()));
        }

        if (StringUtils.hasLength(request.getOperation())) {
            spec = spec.and(Specifications.operationStartsWith(request.getOperation()));
        }

        if (request.getSuccess() != null) {
            spec = spec.and(Specifications.isSuccess(request.getSuccess()));
        }

        if (request.getStartTime() != null || request.getEndTime() != null) {
            spec = spec.and(Specifications.timeBetweenAnd(request.getStartTime(), request.getEndTime()));
        }

        Page<OperationLogDO> page = operationLogRepository.findAll(spec, pageable);
        request.setTotal(page.getTotalElements());
        request.setRecords(page.toList());
        return Result.success(request);
    }


    static class Specifications {
        public static Specification<OperationLogDO> nameStartsWith(String name) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("user"), name + "%");
        }

        public static Specification<OperationLogDO> operationStartsWith(String operation) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("operation"), operation + "%");
        }

        public static Specification<OperationLogDO> isSuccess(boolean success) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("success"), success);
        }

        public static Specification<OperationLogDO> timeBetweenAnd(Long startTime, Long endTime) {
            return (root, query, criteriaBuilder) -> {
                Date effectiveStartTime = startTime != null ? new Date(startTime) : new Date(0);
                Date effectiveEndTime = endTime != null ? new Date(endTime) : new Date();

                return criteriaBuilder.between(root.get("startTime"), effectiveStartTime, effectiveEndTime);
            };
        }
    }
}
