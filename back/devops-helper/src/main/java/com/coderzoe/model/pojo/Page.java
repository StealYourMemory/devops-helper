package com.coderzoe.model.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/19 14:19
 */
@Data
public class Page<T> {
    private Boolean paging;
    private Integer page;
    private Integer size;
    private Long total;
    private String sortBy;
    private String order;
    private List<T> records;
}
