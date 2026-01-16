<template>
  <div>
    <el-space>
      <devops-icon :width="50" :height="50"></devops-icon>
      <span class="devops-helper devops">Devops</span>
      <span class="devops-helper helper">Helper</span>
    </el-space>
    <el-menu :default-active="activeMenu">
      <router-link :to="{ name: 'IndexPage' }">
        <el-menu-item index="IndexPage">
          <el-space size="large">
            <active-home-icon :width="iconWidth" :height="iconHeight" v-if="activeMenu === 'IndexPage'">
            </active-home-icon>
            <home-icon :width="iconWidth" :height="iconHeight" v-else></home-icon>
            <span>首页</span>
          </el-space>
        </el-menu-item>
      </router-link>
      <el-sub-menu index="3">
        <template #title>
          <el-space size="large">
            <deploy-icon :width="iconWidth" :height="iconHeight">
            </deploy-icon>
            <span>打包部署</span>
          </el-space>
        </template>
        <router-link :to="{ name: 'CommonDeploy' }">
          <el-menu-item index="CommonDeploy">
            <el-space size="large">
              <active-gitlab-icon :width="iconWidth" :height="iconHeight"
                                  v-if="activeMenu === 'CommonDeploy'">
              </active-gitlab-icon>
              <gitlab-icon :width="iconWidth" :height="iconHeight" v-else></gitlab-icon>
              <span> 通用部署</span>
            </el-space>
          </el-menu-item>
        </router-link>
        <router-link :to="{ name: 'DeployInfo' }">
          <el-menu-item index="DeployInfo">
            <el-space size="large">
              <active-k8s-icon :width="iconWidth" :height="iconHeight" v-if="activeMenu === 'DeployInfo'">
              </active-k8s-icon>
              <k8s-icon :width="iconWidth" :height="iconHeight" v-else>
              </k8s-icon>
              <span> 部署详情</span>
            </el-space>
          </el-menu-item>
        </router-link>
        <router-link :to="{ name: 'DeveloperTransfer' }">
          <el-menu-item index="DeveloperTransfer">
            <el-space size="large">
              <active-jenkins-icon :width="iconWidth" :height="iconHeight"
                                   v-if="activeMenu === 'DeveloperTransfer'">
              </active-jenkins-icon>
              <jenkins-icon :width="iconWidth" :height="iconHeight" v-else></jenkins-icon>
              <span> 开发转测</span>
            </el-space>
          </el-menu-item>
        </router-link>
      </el-sub-menu>
      <el-sub-menu index="Transfer">
        <template #title>
          <el-space size="large">
            <transfer-icon :width="iconWidth" :height="iconHeight"></transfer-icon>
            <span>转测相关</span>
          </el-space>
        </template>
        <router-link :to="{name: 'TransferRequirement'}">
          <el-menu-item index="TransferRequirement">
            <el-space size="large">
              <active-requirement-icon :width="iconWidth" :height="iconHeight"
                                       v-if="activeMenu === 'TransferRequirement'">
              </active-requirement-icon>
              <requirement-icon :width="iconWidth" :height="iconHeight" v-else></requirement-icon>
              <span>需求审查</span>
            </el-space>
          </el-menu-item>
        </router-link>
        <router-link :to="{name: 'TransferBug'}">
          <el-menu-item index="TransferBug">
            <el-space size="large">
              <active-bug-icon :width="iconWidth" :height="iconHeight"
                               v-if="activeMenu === 'TransferBug'">
              </active-bug-icon>
              <bug-icon :width="iconWidth" :height="iconHeight" v-else></bug-icon>
              <span> Bug审查</span>
            </el-space>
          </el-menu-item>
        </router-link>
        <router-link :to="{name: 'TransferArchive'}">
          <el-menu-item index="TransferArchive">
            <el-space size="large">
              <active-archive-icon :width="iconWidth" :height="iconHeight"
                                   v-if="activeMenu === 'TransferArchive'">
              </active-archive-icon>
              <archive-icon :width="iconWidth" :height="iconHeight" v-else></archive-icon>
              <span>转测归档</span>
            </el-space>
          </el-menu-item>
        </router-link>
      </el-sub-menu>
      <router-link :to="{ name: 'EnvManage' }">
        <el-menu-item index="EnvManage">
          <el-space size="large">
            <env-icon :width="iconWidth" :height="iconHeight" v-if="activeMenu === 'EnvManage'">
            </env-icon>
            <in-active-env-icon :width="iconWidth" :height="iconHeight" v-else></in-active-env-icon>
            <span>环境管理</span>
          </el-space>
        </el-menu-item>
      </router-link>
      <router-link :to="{ name: 'ServerProxy' }">
        <el-menu-item index="ServerProxy">
          <el-space size="large">
            <active-proxy-icon :width="iconWidth" :height="iconHeight" v-if="activeMenu === 'ServerProxy'">
            </active-proxy-icon>
            <proxy-icon :width="iconWidth" :height="iconHeight" v-else></proxy-icon>
            <span>服务代理</span>
          </el-space>
        </el-menu-item>
      </router-link>
      <router-link :to="{ name: 'InspectionLog' }">
        <el-menu-item index="InspectionLog">
          <el-space size="large">
            <active-log-icon :width="iconWidth" :height="iconHeight" v-if="activeMenu === 'InspectionLog'">
            </active-log-icon>
            <log-icon :width="iconWidth" :height="iconHeight" v-else></log-icon>
            <span>审计日志</span>
          </el-space>
        </el-menu-item>
      </router-link>
    </el-menu>
  </div>
</template>
<script setup lang="ts">
import GitlabIcon from '@/assets/icons/inactive/GitlabIcon.vue';
import DeployIcon from '@/assets/icons/inactive/DeployIcon.vue';
import JenkinsIcon from '@/assets/icons/inactive/JenkinsIcon.vue';
import K8sIcon from '@/assets/icons/inactive/K8sIcon.vue';
import HomeIcon from '@/assets/icons/inactive/HomeIcon.vue';
import TransferIcon from '@/assets/icons/inactive/TransferIcon.vue';
import LogIcon from '@/assets/icons/inactive/LogIcon.vue';
import {computed} from 'vue';
import {useRoute} from 'vue-router';
import ActiveGitlabIcon from '@/assets/icons/active/ActiveGitlabIcon.vue';
import ActiveJenkinsIcon from '@/assets/icons/active/ActiveJenkinsIcon.vue';
import ActiveK8sIcon from '@/assets/icons/active/ActiveK8sIcon.vue';
import ActiveLogIcon from '@/assets/icons/active/ActiveLogIcon.vue';
import ActiveHomeIcon from '@/assets/icons/active/ActiveHomeIcon.vue';
import DevopsIcon from "@/assets/icons/inactive/DevopsIcon.vue";
import BugIcon from "@/assets/icons/inactive/BugIcon.vue";
import ActiveBugIcon from "@/assets/icons/active/ActiveBugIcon.vue";
import ActiveRequirementIcon from "@/assets/icons/active/ActiveRequirementIcon.vue";
import RequirementIcon from "@/assets/icons/inactive/RequirementIcon.vue";
import ActiveArchiveIcon from "@/assets/icons/active/ActiveArchiveIcon.vue";
import ArchiveIcon from "@/assets/icons/inactive/ArchiveIcon.vue";
import EnvIcon from "@/assets/icons/active/EnvIcon.vue";
import InActiveEnvIcon from "@/assets/icons/inactive/InActiveEnvIcon.vue";
import ActiveProxyIcon from "@/assets/icons/active/ActiveProxyIcon.vue";
import ProxyIcon from "@/assets/icons/inactive/ProxyIcon.vue";

const iconHeight: number = 24;
const iconWidth: number = 24;

const route = useRoute()

const activeMenu = computed<string>(() => {
  return route.name as string;
})
</script>

<style scoped>
.el-menu a.router-link-active,
.el-menu a.router-link-exact-active,
.el-menu a {
  text-decoration: none;
}

/*
.el-menu {
  background-color: #F5F9FA;
  border-right: none;
}
.el-menu-item{
  background-color: #F5F9FA;
}
*/

.devops-helper {
  font-size: 20px;
  /* 可以根据需要调整字体大小 */
  font-family: Arial, sans-serif;
  /* 使用通用的无衬线字体 */
}

.devops {
  color: #0D2C40;
  /* 深蓝偏黑色 */
}

.helper {
  color: #0081FF;
  /* 浅蓝色 */
}
</style>