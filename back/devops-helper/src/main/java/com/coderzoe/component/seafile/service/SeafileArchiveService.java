package com.coderzoe.component.seafile.service;

import com.coderzoe.common.Constants;
import com.coderzoe.common.Result;
import com.coderzoe.common.enums.ArchiveFileType;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.component.devops.model.DevopsSprintOptionResponse;
import com.coderzoe.component.devops.model.DevopsWorkIssueResponse;
import com.coderzoe.component.devops.service.DevopsService;
import com.coderzoe.component.seafile.config.SeafileConfig;
import com.coderzoe.component.seafile.model.ArchiveNameMatch;
import com.coderzoe.component.seafile.model.FileTree;
import com.coderzoe.config.ProjectConfig;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coderzoe.component.seafile.service.SeafileTransferService.getDirDay;
import static com.coderzoe.component.seafile.service.SeafileTransferService.getFileVersion;

/**
 * seafile归档
 *
 * @author yinhuasheng
 * @date 2024/8/20 11:18
 */
@Service
@Setter(onMethod_ = {@Autowired})
public class SeafileArchiveService {
    private SeafileService seafileService;
    private ProjectConfig projectConfig;
    private DevopsService devopsService;
    private SeafileConfig seafileConfig;


    public void archive(String path) {
        initDir(path);
        List<FileTree> treeList = seafileService.deepGetDirInfo(path);
        //treeList是每天的目录，这里按每天目录时间排序，排序是正序，即由小到大的时间
        List<FileTree> sortedDirList = treeList.stream().filter(p -> !p.isLeaf())
                .sorted(Comparator.comparingInt(p -> getDirDay(p.getName())))
                .toList();
        if (seafileConfig.isArchive(ArchiveFileType.SQL)) {
            archiveSql(sortedDirList);
        }
        if (seafileConfig.isArchive(ArchiveFileType.YAML)) {
            archiveYaml(sortedDirList);
        }
        if (seafileConfig.isArchive(ArchiveFileType.SHELL)) {
            archiveShell(sortedDirList);
        }
        if (seafileConfig.isArchive(ArchiveFileType.REQUIREMENT)) {
            archiveRequirement(path);
        }
    }


    /**
     * 归档sql 逻辑是使用最新版的sql
     */
    private void archiveSql(List<FileTree> sortedDirList) {
        List<FileTree> notFinalList = sortedDirList.stream()
                .filter(p -> !p.isLeaf())
                .filter(p -> !seafileConfig.getArchiveBasePath().equals(p.getName()))
                .toList();
        Map<String, FileTree> sqlMap = new HashMap<>(4);
        //遍历的是每一天的目录
        for (FileTree fileTree : notFinalList) {
            // 过滤拿到一天内所有的sql文件
            List<FileTree> sqlFile = getFileList(fileTree, Constants.SQL_FILE);
            //可能一天里面存在多版本sql问题 排个序
            sqlFile.sort(Comparator.comparingInt(o -> getFileVersion(o.getName())));
            for (FileTree file : sqlFile) {
                //因为是按时间正序和version正序排序的，所有一旦有就替换，认为是新的最新版
                seafileConfig.getArchiveSqlNames()
                        .stream()
                        .filter(p -> p.matches(file.getName()))
                        .findFirst()
                        .ifPresent(sql -> sqlMap.put(sql.getName(), file));
            }
        }
        //写入归档
        FileTree finalDir = sortedDirList.stream()
                .filter(p -> !p.isLeaf())
                .filter(p -> seafileConfig.getArchiveBasePath().equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new CommonException("未找到" + seafileConfig.getArchiveBasePath() + "目录"));
        sqlMap.forEach((key, value) -> {
            //读取文件内容
            Result<String> result = seafileService.getFileContent(value.getParentDirectory() + value.getName());
            //上传
            seafileService.uploadFile(finalDir.getParentDirectory() + finalDir.getName(), result.getData().getBytes(), value.getName());
        });
    }

    /**
     * 归档yaml
     */
    private void archiveYaml(List<FileTree> sortedDirList) {
        List<FileTree> notFinalList = sortedDirList.stream().filter(p -> !p.isLeaf())
                .filter(p -> !seafileConfig.getArchiveBasePath().equals(p.getName()))
                .toList();
        Map<String, FileTree> yamlMap = new HashMap<>(20);

        //这一层遍历的是每天的内容
        for (FileTree fileTree : notFinalList) {
            // 过滤拿到所有yaml的文件 .yaml或.yml
            List<FileTree> yamlFile = getFileList(fileTree, Constants.YAML_FILE);
            yamlFile.addAll(getFileList(fileTree, Constants.YML_FILE));
            //可能一天里面存在多版本yaml问题 排个序
            yamlFile.sort(Comparator.comparingInt(o -> getFileVersion(o.getName())));
            //这一层遍历的是一天内的多个yaml文件
            for (FileTree tree : yamlFile) {
                //找到这个yaml文件所属的项目
                projectConfig.getProjects()
                        .stream()
                        .filter(p -> tree.getName().contains(p.getName()))
                        .findFirst()
                        .ifPresent(project -> yamlMap.put(project.getName(), tree));
            }
        }
        //写入归档
        FileTree finalDir = sortedDirList.stream()
                .filter(p -> !p.isLeaf())
                .filter(p -> seafileConfig.getArchiveBasePath().equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new CommonException("未找到" + seafileConfig.getArchiveBasePath() + "目录"));
        FileTree yamlDir = finalDir.getChildren()
                .stream()
                .filter(p -> !p.isLeaf())
                .filter(p -> Constants.ARCHIVE_YAML_DIR.equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new CommonException("未找到" + seafileConfig.getArchiveBasePath() + "目录下的" + Constants.ARCHIVE_YAML_DIR + "目录"));
        yamlMap.forEach((key, value) -> {
            //读取文件内容
            Result<String> result = seafileService.getFileContent(value.getParentDirectory() + value.getName());
            //上传
            seafileService.uploadFile(yamlDir.getParentDirectory() + yamlDir.getName(), result.getData().getBytes(), value.getName());
        });
    }

    /**
     * 归档shell shell的归档 只会将所有shell提取出来
     */
    private void archiveShell(List<FileTree> sortedDirList) {
        List<FileTree> notFinalList = sortedDirList.stream()
                .filter(p -> !p.isLeaf())
                .filter(p -> !seafileConfig.getArchiveBasePath().equals(p.getName()))
                .toList();
        List<FileTree> shellFileList = new ArrayList<>();

        for (FileTree fileTree : notFinalList) {
            // 过滤拿到所有的shell文件
            //shell文件的逻辑是.sh结尾或者.txt结尾但不包含更新说明字眼的
            shellFileList.addAll(getFileList(fileTree, Constants.SHELL_FILE));
            List<FileTree> txtShellList = getFileList(fileTree, Constants.TXT_FILE).stream().filter(p -> !p.getName().contains(Constants.UPDATE_INFO_FILE_NAME)).toList();
            shellFileList.addAll(txtShellList);
        }
        //写入归档
        FileTree finalDir = sortedDirList.stream()
                .filter(p -> !p.isLeaf())
                .filter(p -> seafileConfig.getArchiveBasePath().equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new CommonException("未找到" + seafileConfig.getArchiveBasePath() + "目录"));
        FileTree shellDir = finalDir.getChildren()
                .stream()
                .filter(p -> !p.isLeaf())
                .filter(p -> Constants.ARCHIVE_SHELL_DIR.equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new CommonException("未找到" + seafileConfig.getArchiveBasePath() + "目录下的" + Constants.ARCHIVE_YAML_DIR + "目录"));
        for (FileTree fileTree : shellFileList) {
            //读取文件内容
            Result<String> result = seafileService.getFileContent(fileTree.getParentDirectory() + fileTree.getName());
            //上传
            seafileService.uploadFile(shellDir.getParentDirectory() + shellDir.getName(), result.getData().getBytes(), fileTree.getName());
        }
    }

    private void archiveRequirement(String path) {
        String[] pathList = path.split("/");
        String newPath = pathList[pathList.length - 1];
        int year = getYear(newPath);
        String month = getMonth(newPath);
        String label1 = (year - 2000) + "年" + month;
        String label2 = year + "年" + month;
        Result<List<DevopsSprintOptionResponse>> result = devopsService.getDevopsSprintOptions();
        if (!result.isSuccess()) {
            throw new CommonException("查询devops迭代异常");
        }
        DevopsSprintOptionResponse sprint = result.getData()
                .stream()
                .filter(p -> p.getName().contains(label1) || p.getName().contains(label2))
                .findFirst()
                .orElseThrow(() -> new CommonException("未找到" + label1 + "或者" + label2 + "的devops迭代"));
        Result<List<DevopsWorkIssueResponse>> workIssueList = devopsService.getRequestListBySprints(Collections.singletonList(sprint.getId()));
        if (!workIssueList.isSuccess()) {
            throw new CommonException("查询devops需求详情异常");
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (DevopsWorkIssueResponse workIssue : workIssueList.getData()) {
            stringBuilder.append("[")
                    .append(workIssue.getKey())
                    .append("]")
                    .append(" ")
                    .append(workIssue.getSubject())
                    .append("\n");
        }
        seafileService.uploadFile(path + "/" + seafileConfig.getArchiveBasePath(), stringBuilder.toString().getBytes(StandardCharsets.UTF_8), Constants.ARCHIVE_REQUIREMENT_FILE);

    }


    private void initDir(String path) {
        List<FileTree> fileTreeList = seafileService.deepGetDirInfo(path);
        Optional<FileTree> finalDir = fileTreeList.stream()
                .filter(p -> !p.isLeaf())
                .filter(p -> seafileConfig.getArchiveBasePath().equals(p.getName()))
                .findFirst();
        //如果存在final-dh目录 直接全删了
        if (finalDir.isPresent()) {
            Boolean deleted = seafileService.deleteDir(path, Collections.singletonList(seafileConfig.getArchiveBasePath()));
            if (!deleted) {
                throw new CommonException("删除目录" + path + "/" + seafileConfig.getArchiveBasePath() + "失败");
            }
        }
        seafileService.createDir(path + "/" + seafileConfig.getArchiveBasePath());
        seafileService.createDir(path + "/" + seafileConfig.getArchiveBasePath() + "/" + Constants.ARCHIVE_YAML_DIR);
        seafileService.createDir(path + "/" + seafileConfig.getArchiveBasePath() + "/" + Constants.ARCHIVE_SHELL_DIR);
    }

    private List<FileTree> getFileList(FileTree fileTree, String suffix) {
        List<FileTree> fileList = new ArrayList<>();

        //过滤掉不参与归档目录 如SLB镜像
        if (ignorePath(fileTree)) {
            return fileList;
        }

        if (fileTree.isLeaf() && fileTree.getName().endsWith(suffix)) {
            fileList.add(fileTree);
        } else if (!fileTree.isLeaf() && fileTree.getChildren() != null) {
            for (FileTree child : fileTree.getChildren()) {
                fileList.addAll(getFileList(child, suffix));
            }
        }
        return fileList;
    }

    /**
     * 不参与归档的目录
     */
    private boolean ignorePath(FileTree fileTree) {
        if (!fileTree.isLeaf() && seafileConfig.getArchiveIgnorePaths() != null) {
            for (ArchiveNameMatch ignorePath : seafileConfig.getArchiveIgnorePaths()) {
                if (ignorePath.matches(fileTree.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int getYear(String path) {
        String regex = "(\\d+)年";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new CommonException("通过path" + path + "无法提取出年");
    }

    private static String getMonth(String path) {
        path = path.split("年")[1];
        return path.substring(0, 2);
    }
}
