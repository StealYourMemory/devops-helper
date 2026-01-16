package com.coderzoe.component.seafile.model;

import com.coderzoe.component.seafile.model.response.DirectoryInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/20 10:39
 */
@Data
@NoArgsConstructor
public class FileTree {
    private String type;
    private String id;
    private String name;
    private Long time;
    private String permission;
    private String parentDirectory;
    private Boolean starred;
    /**
     * 以下属性只有在type是file时才有值
     */
    private Long size;
    private String modifierEmail;
    private String modifierName;
    private String modifierContactEmail;
    /**
     * 是否是叶子节点 文件是叶子节点 目录是非叶子节点
     */
    private Boolean isLeaf;

    /**
     * 子节点
     */
    private List<FileTree> children;

    public FileTree(DirectoryInfo directoryInfo) {
        this.type = directoryInfo.getType();
        this.id = directoryInfo.getId();
        this.name = directoryInfo.getName();
        this.time = directoryInfo.getTime();
        this.permission = directoryInfo.getPermission();
        this.parentDirectory = directoryInfo.getParentDirectory();
        this.starred = directoryInfo.isStarred();
        this.size = directoryInfo.getSize();
        this.modifierEmail = directoryInfo.getModifierEmail();
        this.modifierName = directoryInfo.getModifierName();
        this.modifierContactEmail = directoryInfo.getModifierContactEmail();
        this.isLeaf = isLeaf();
    }

    /**
     * 是否是叶子节点 文件是叶子节点 目录是非叶子节点
     */
    public boolean isLeaf() {
        return "file".equals(type);
    }
}
