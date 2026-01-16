package com.coderzoe.component.seafile.service;

import com.coderzoe.common.Constants;
import com.coderzoe.common.Result;
import com.coderzoe.common.enums.BuildAndDeployTypeEnum;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.common.util.DateUtil;
import com.coderzoe.common.util.JsonUtil;
import com.coderzoe.component.seafile.config.SeafileConfig;
import com.coderzoe.component.seafile.model.FileTree;
import com.coderzoe.config.ProjectConfig;
import com.coderzoe.model.database.OperationLogDO;
import com.coderzoe.model.request.BuildAndDeployDTO;
import com.coderzoe.model.request.BuildAndTransferRequest;
import com.coderzoe.repository.OperationLogRepository;
import com.coderzoe.service.DeployService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 转测时seafile打包上传功能
 *
 * @author yinhuasheng
 * @date 2024/8/20 11:19
 */
@Service
@Setter(onMethod = @__(@Autowired))
public class SeafileTransferService {
    private SeafileConfig seafileConfig;
    private SeafileService seafileService;
    private DeployService deployService;
    private ProjectConfig projectConfig;
    private OperationLogRepository operationLogRepository;

    /**
     * 打包上传
     *
     * @param request   请求参数
     * @param userName  用户名
     * @param requestId 请求id
     */
    public void buildAndTransfer(BuildAndTransferRequest request, String userName, String requestId) {
        //先打包再更新seafile
        BuildAndDeployDTO buildAndDeploy = BuildAndDeployDTO.builder()
                .projectName(request.getProjectName())
                .branch(request.getBranch())
                .tag(request.getTag())
                .requestId(requestId)
                .user(userName)
                .type(BuildAndDeployTypeEnum.BUILD_AND_TRANSFER)
                .build();
        try {
            //jenkins打包
            boolean buildAndDeployResult = deployService.buildAndDeploy(buildAndDeploy);
            //更新日志
            updateRequestByRequestId(request, requestId);
            //只有打包成功，才更新文件信息
            if (buildAndDeployResult) {
                /*
                 * 逻辑如下
                 * 1. yaml
                 *  先拿到最新项目的yaml，以此为模板
                 *  更改模板中的tag信息，上传到今天的文件夹下
                 *  如果文件夹不存在则自己创建，否则上传到指定文件夹内
                 * 2. 更新说明
                 *  先拿到今天目录下的更新说明，然后将信息尾追加到里面
                 *  如果不存在就新建，然后上传
                 */
                //拿到本次迭代下的所有目录和文件信息
                List<FileTree> fileTreeList = getSubFileByThisSprint();
                String uploadPath = createUploadPath(fileTreeList);
                //处理上传yaml
                uploadYaml(request, fileTreeList, uploadPath);
                //处理更新说明 这里由于yaml已经上传成功，所以更新说明的目录必然存在
                if (request.getUpdateInfo() != null && !request.getUpdateInfo().isEmpty()) {
                    handleUpdateInfo(request.getUpdateInfo(), uploadPath.split("yaml")[0]);
                }
                //更新日志
                updateResultByRequestId("更新seafile成功", requestId, true);
            }
        } catch (Exception e) {
            updateResultByRequestId("更新seafile失败" + e.getMessage(), requestId, false);
        }
    }

    /**
     * 获得本次迭代下的所有文件/目录信息
     */
    private List<FileTree> getSubFileByThisSprint() {
        Result<List<FileTree>> baseDirResult = seafileService.getDirInfo(seafileConfig.getBasePath());
        if (baseDirResult.isSuccess()) {
            //拿到当前迭代的目录
            FileTree sprintDir = baseDirResult.getData()
                    .stream()
                    .filter(p -> p.getName().contains(getSprintLabel()))
                    .findFirst()
                    .orElseThrow(() -> new CommonException("无法在根目录[" + seafileConfig.getBasePath() + "]下找到当前迭代[" + getSprintLabel() + "]" + "的目录"));
            return seafileService.deepGetDirInfo(seafileConfig.getBasePath() + "/" + sprintDir.getName());
        } else {
            throw new CommonException("获取根目录[" + seafileConfig.getBasePath() + "]下子目录失败");
        }
    }

    private FileTree getLatestYamlContent(String projectName, List<FileTree> treeList) {
        //按日期排个倒序
        List<FileTree> dirList = treeList.stream()
                .filter(p -> !p.isLeaf())
                .sorted((p1, p2) -> getDirDay(p2.getName()) - getDirDay(p1.getName()))
                .toList();
        for (FileTree fileTree : dirList) {
            List<FileTree> yamlFileList = getYamlFileList(projectName, fileTree);
            //由于是倒着找，所以一旦找到就作为最新的
            if (!CollectionUtils.isEmpty(yamlFileList)) {
                //如果某天某项目有多条 选择version最大的然后返回
                return yamlFileList.stream()
                        .max(Comparator.comparingInt(p -> getFileVersion(p.getName())))
                        .get();

            }
        }
        throw new CommonException("本次迭代中未找到最新的" + projectName + "yml模板");
    }

    /**
     * 上传yaml
     *
     * @param request      请求体
     * @param fileTreeList 本次迭代的文件树
     * @param uploadPath   上传路径
     */
    private void uploadYaml(BuildAndTransferRequest request, List<FileTree> fileTreeList, String uploadPath) {
        //先读取上一次的yaml内容
        //1. 拿到这个yaml文件
        FileTree yamlFile = getLatestYamlContent(request.getProjectName(), fileTreeList);
        //读取文件内容
        Result<String> latestYamlResult = seafileService.getFileContent(yamlFile.getParentDirectory() + yamlFile.getName());

        if (latestYamlResult.isSuccess()) {
            //将内容替换为本tag
            String newYamlContent = replaceTag(latestYamlResult.getData(), request.getTag());
            String newYamlFileName = projectConfig.get(request.getProjectName()).getJenkinsUrl()
                    + "-"
                    + DateUtil.formatDate(new Date())
                    + "-v"
                    + getFileVersion(request.getTag())
                    + ".yaml";
            //上传文件
            Result<?> result = seafileService.uploadFile(uploadPath, newYamlContent.getBytes(), newYamlFileName);
            //上传yaml成功
            if (!result.isSuccess()) {
                throw new CommonException("上传更新后的yaml失败");
            }
        } else {
            throw new CommonException("读取文件" + yamlFile.getParentDirectory() + yamlFile.getName() + "失败");
        }
    }

    /**
     * 更新说明的处理方案是：先看是否存在更新说明文件，如果存在则读取并尾追加本次内容，删除之前文件，然后上传最新
     * 如果不存在则直接生成本次内容，上传即可
     */
    private void handleUpdateInfo(String updateInfo, String path) {
        //这里不能直接读，因为即使文件不存在也返回的是200，无法判断是否真的存在，只能先读父目录看下里面有没有
        Result<List<FileTree>> dirInfo = seafileService.getDirInfo(path);
        if (!dirInfo.isSuccess()) {
            throw new CommonException("读取文件夹" + path + "内容失败");
        }
        String newContent;
        Optional<FileTree> first = dirInfo.getData().stream().filter(p -> p.isLeaf() && "更新说明.txt".equals(p.getName())).findFirst();
        if (first.isPresent()) {
            //存在 读取源内容
            Result<String> updateInfoTxt = seafileService.getFileContent(path + "更新说明.txt");
            if (updateInfoTxt.isSuccess()) {
                newContent = updateInfoTxt.getData() + "\n" + updateInfo;
                Result<String> result = seafileService.deleteFile(path + "更新说明.txt");
                if (!result.isSuccess()) {
                    throw new CommonException("删除更新说明文件失败，失败原因：" + result.getMsg());
                }
            } else {
                throw new CommonException("读取更新说明文件失败，失败原因：" + updateInfoTxt.getMsg());
            }
        } else {
            newContent = updateInfo;
        }
        //上传
        Result<?> uploadResult = seafileService.uploadFile(path, newContent.getBytes(), "更新说明.txt");
        if (!uploadResult.isSuccess()) {
            throw new CommonException("上传更新说明文件失败，失败原因：" + uploadResult.getMsg());
        }
    }

    /**
     * 创建上传路径，正常路径是update_yyyyMMdd下的yaml目录内，但如果不存在则新建
     *
     * @return 返回上传路径
     */
    private String createUploadPath(List<FileTree> fileTreeList) {
        String label = DateUtil.formatDate(new Date());
        Optional<FileTree> fileTreeOptional = fileTreeList.stream()
                .filter(p -> !p.isLeaf())
                .filter(p -> p.getName().contains(label))
                .findFirst();
        //判断yyyyMMdd目录是否存在
        if (fileTreeOptional.isPresent()) {
            List<FileTree> children = fileTreeOptional.get().getChildren();
            children = children == null ? new ArrayList<>() : children;
            Optional<FileTree> firstYamlDir = children.stream().filter(p -> "yaml".equals(p.getName())).findFirst();
            //判断yyyyMMdd下yaml目录是否存在
            if (firstYamlDir.isPresent()) {
                FileTree fileTree = firstYamlDir.get();
                return fileTree.getParentDirectory() + fileTree.getName() + "/";
            } else {
                //创建yaml目录
                String path = fileTreeOptional.get().getParentDirectory() + fileTreeOptional.get().getName() + "/yaml/";
                Result<?> result = seafileService.createDir(path);
                if (result.isSuccess()) {
                    return path;
                }
            }
        } else {
            //创建update_yyyyMMdd目录和yaml目录
            String path = fileTreeList.getFirst().getParentDirectory() + "update_" + label + "/";
            Result<?> result = seafileService.createDir(path);
            if (result.isSuccess()) {
                path += "yaml/";
                result = seafileService.createDir(path);
                if (result.isSuccess()) {
                    return path;
                }
            }
        }
        throw new CommonException("创建上传目录失败");
    }

    /**
     * 可能会有0到多条
     *
     * @param projectName 项目名
     * @param fileTree    某天的目录
     * @return 返回这个目录下 匹配的yaml
     */
    private List<FileTree> getYamlFileList(String projectName, FileTree fileTree) {
        List<FileTree> fileTreeList = new ArrayList<>();
        if (fileTree.isLeaf()) {
            //用jenkins的url作为yaml文件的匹配
            String label = projectConfig.get(projectName).getJenkinsUrl();
            String fileName = fileTree.getName();
            boolean isYaml = fileName.endsWith(Constants.YAML_FILE) || fileName.endsWith(Constants.YML_FILE);
            if (fileName.contains(label) && isYaml) {
                fileTreeList.add(fileTree);
                return fileTreeList;
            }
        } else {
            //是目录的话 遍历子目录继续查找
            if (!CollectionUtils.isEmpty(fileTree.getChildren())) {
                for (FileTree child : fileTree.getChildren()) {
                    List<FileTree> list = getYamlFileList(projectName, child);
                    if (!CollectionUtils.isEmpty(list)) {
                        fileTreeList.addAll(list);
                    }
                }
            }
        }
        return fileTreeList;
    }


    private void updateRequestByRequestId(BuildAndTransferRequest request, String requestId) {
        OperationLogDO operationLog = operationLogRepository.findByRequestId(requestId).orElseThrow(() -> new CommonException("未找到记录"));
        operationLog.setRequestDetail(JsonUtil.toJsonString(request));
        operationLogRepository.save(operationLog);
    }

    private void updateResultByRequestId(String result, String requestId, boolean success) {
        OperationLogDO operationLog = operationLogRepository.findByRequestId(requestId).orElseThrow(() -> new CommonException("未找到记录"));
        operationLog.setSuccess(success);
        String resultDetail = operationLog.getResultDetail();
        resultDetail += result;
        operationLog.setResultDetail(resultDetail);
        operationLog.setEndTime(new Date());
        operationLogRepository.save(operationLog);
    }

    private static String getSprintLabel() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        return year + "年" + (month < 10 ? ("0" + month) : month);
    }

    public static int getFileVersion(String fileName) {
        String regex = "[-_][vV](\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        int number = 0;
        if (matcher.find()) {
            number = Integer.parseInt(matcher.group(1));
        }
        return number;
    }

    private static String replaceTag(String yamlContent, String newTag) {
        String regex = "(harbor-local\\.unicloudsrv\\.com/[^:]+:)[^\\s,]*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(yamlContent);
        return matcher.replaceAll("$1" + newTag);
    }


    public static int getDirDay(String dir) {
        try {
            return Integer.parseInt(dir.split("_")[1]);
        } catch (Exception e) {
            return -1;
        }
    }
}
