package com.coderzoe.common;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.common.global.KeepAliveHandler;
import org.apache.sshd.server.global.NoMoreSessionsHandler;

import java.util.Arrays;

/**
 * 常量
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:22
 */
public class Constants {
    /**
     * 各服务cookie/token 存入Cache 避免每次查库
     */
    public static final String GITLAB_COOKIE_KEY = "gitlab_cookie_key";
    public static final String JENKINS_COOKIE_KEY = "jenkins_cookie_key";
    public static final String DEVOPS_TOKEN_KEY = "devops_token_key";
    public static final String SEAFILE_COOKIE_KEY = "seafile_cookie_key";
    /**
     * jenkins打包历史存入cache 避免频繁过快的查询
     */
    public static final String JENKINS_PACKAGE_HISTORY_KEY = "JENKINS_PACKAGE_HISTORY_KEY";
    public static final String JENKINS_PACKAGE_LAST_TIME_KEY = "JENKINS_PACKAGE_LAST_TIME_KEY";


    /**
     * jenkins历史记录缓存时间 单位ms
     */
    public static final long JENKINS_HISTORY_CACHE_TIME = 5 * 1000;

    /**
     * 通过devops-helper打包的前缀
     */
    public static final String DEVOPS_HELPER_TAG_PREFIX = "dh-";

    /**
     * release分支名称
     */
    public static final String RELEASE_BRANCH = "release";

    /**
     * 等待最大打包时长 3min
     */
    public static final long WAIT_BUILD_MAX_TIME = 3 * 60 * 1000;

    /**
     * 等待部署最大时长 3min
     */
    public static final long WAIT_DEPLOY_MAX_TIME = 3 * 60 * 1000;

    public static final String IMAGE_TAG_SPLIT = ":";


    /*
     ==============  转测相关常量 ==============
     */

    /**
     * 特性下建议挂的最小用户故事个数
     */
    public static final int DEVOPS_LEVEL2_CHILDREN_MIN_SIZE = 5;

    /**
     * devops 成员信息 存入cache的key
     */
    public static final String DEVOPS_COMPANY_MEMBER_KEY = "DEVOPS_COMPANY_MEMBER_KEY";

    /**
     * devops查询成员数 目前支持最大是1000，最大接口就报错了
     */
    public static final int DEVOPS_COMPANY_MEMBER_MAX_SIZE = 1000;


    public static final String ARCHIVE_YAML_DIR = "yaml";
    public static final String ARCHIVE_SHELL_DIR = "shell";
    public static final String ARCHIVE_REQUIREMENT_FILE = "合入需求.txt";


    public static final String SQL_FILE = ".sql";
    public static final String YAML_FILE = ".yaml";
    public static final String YML_FILE = ".yml";
    public static final String SHELL_FILE = ".sh";
    public static final String TXT_FILE = ".txt";
    public static final String UPDATE_INFO_FILE_NAME = "更新说明";

    public static final String BASE_DIR_START_CHAR = "/";

    public static SshClient CLIENT = SshClient.setUpDefaultClient();

    static {
        CLIENT.start();
        CLIENT.setGlobalRequestHandlers(Arrays.asList(KeepAliveHandler.INSTANCE, NoMoreSessionsHandler.INSTANCE));
    }
}
