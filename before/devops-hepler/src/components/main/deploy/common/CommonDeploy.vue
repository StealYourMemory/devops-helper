<template>
  <el-container>
    <el-aside width="35%">
      <common-deploy-form>
      </common-deploy-form>
      <el-container style="margin-top: 15%">
        <build-and-deploy-progress v-if="progress" :progress="progress"></build-and-deploy-progress>
      </el-container>
    </el-aside>
    <el-main>
      <build-history></build-history>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import CommonDeployForm from "@/components/main/deploy/common/CommonDeployForm.vue";
import BuildHistory from "@/components/main/deploy/BuildHistory.vue";
import {ref} from "vue";
import {
  BUILD_AND_DEPLOY_REQUEST_KEY,
  BUILD_ONLY_REQUEST_KEY,
  BuildAndDeployName,
  BuildOnlyName,
  DEPLOY_ONLY_REQUEST_KEY,
  DEPLOY_TAB_ACTIVE_KEY,
  DeployOnlyName,
  type DeployTabActiveType
} from "@/utils/Constants";
import SessionStorageUtil from "@/utils/SessionStorage";
import {tabAndDeployRequestInfoGetInterval} from "@/config/config";
import LocalStorageUtil from "@/utils/LocalStorage";
import BuildAndDeployProgress from "@/components/main/deploy/BuildAndDeployProgress.vue";
import type BuildAndDeployProgressResponse from "@/@types/deploy/common/buildAndDeployProgressResponse";
import {getBuildAndDeployProgress} from "@/apis/commonDeployApi";
import {ElMessage} from "element-plus";

//这里的数据key都得是从localstorage中查询
//定时轮询localstorage 一旦里面有数据就发请求展示数据 没有数据则不展示
const tabActiveName = ref<DeployTabActiveType>(SessionStorageUtil.get(DEPLOY_TAB_ACTIVE_KEY) as DeployTabActiveType || BuildAndDeployName)

const buildAndDeployRequestId = ref<string | null>(LocalStorageUtil.get(BUILD_AND_DEPLOY_REQUEST_KEY));
const buildOnlyRequestId = ref<string | null>(LocalStorageUtil.get(BUILD_ONLY_REQUEST_KEY));
const deployOnlyRequestId = ref<string | null>(LocalStorageUtil.get(DEPLOY_ONLY_REQUEST_KEY));

setInterval(() => {
  tabActiveName.value = SessionStorageUtil.get(DEPLOY_TAB_ACTIVE_KEY) as DeployTabActiveType || BuildAndDeployName;
  let requestId;
  let key;
  switch (tabActiveName.value) {
    case BuildAndDeployName:
      buildAndDeployRequestId.value = LocalStorageUtil.get(BUILD_AND_DEPLOY_REQUEST_KEY);
      requestId = buildAndDeployRequestId.value;
      key = BUILD_AND_DEPLOY_REQUEST_KEY;
      break;
    case BuildOnlyName:
      buildOnlyRequestId.value = LocalStorageUtil.get(BUILD_ONLY_REQUEST_KEY);
      requestId = buildOnlyRequestId.value;
      key = BUILD_ONLY_REQUEST_KEY;
      break;
    case DeployOnlyName:
      deployOnlyRequestId.value = LocalStorageUtil.get(DEPLOY_ONLY_REQUEST_KEY);
      requestId = deployOnlyRequestId.value;
      key = DEPLOY_ONLY_REQUEST_KEY;
      break;
  }
  //这里拿到信息后，如果存在requestId 就去请求相应的数据
  handleBuildAndDeployDetail(requestId, key);
}, tabAndDeployRequestInfoGetInterval)


const progress = ref<BuildAndDeployProgressResponse | null>(null);

async function handleBuildAndDeployDetail(requestId: string | null, key: string) {
  if (requestId != null) {
    const result = await getBuildAndDeployProgress(requestId);
    if (result.success) {
      progress.value = result.data;
      if (result.data == null) {
        LocalStorageUtil.remove(key);
      }
    } else {
      ElMessage.error(result.msg);
    }
  }
}


// progress.value = {
//   requestId: "d9cf210c-9046-47dd-9d36-6f4c5e6e124e",
//   buildProgress: {
//     success: true,
//     data: {
//       id: 473,
//       projectName: 'slb',
//       branch: "0430_bugfix_yhs",
//       tag: "dh-20240511-v3",
//       status: "Pending",
//       tookTime: 20,
//       remainingTime: 46,
//       date: 1715461080000
//     }
//   },
//   deployProgress: ["uca-network-slb-deployment-6795957fb8-2vcj9                   1/1     Running             0          3m34s\nuca-network-slb-deployment-6795957fb8-8j4kj                   1/1     Running             0          4m8s\nuca-network-slb-deployment-6795957fb8-lv7q7                   1/1     Terminating         0          3m5s\nuca-network-slb-deployment-6cc97f6d4c-4lgjm                   0/1     ContainerCreating   0          6s\nuca-network-slb-deployment-6cc97f6d4c-qmjz5                   1/1     Running             0          32s\n"]
// }
</script>