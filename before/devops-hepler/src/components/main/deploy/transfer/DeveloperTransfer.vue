<template>
  <el-container>
    <el-aside width="30%">
      <el-row>
        <el-col :span="18">
          <build-and-transfer></build-and-transfer>
        </el-col>
      </el-row>
      <el-container style="margin-top: 15%">
        <build-and-deploy-progress v-if="progress" :progress="progress"></build-and-deploy-progress>
      </el-container>
    </el-aside>
    <el-main style="margin-left: 10%">
      <seafile-page></seafile-page>
    </el-main>
  </el-container>
</template>
<script setup lang="ts">
import SeafilePage from "@/components/main/deploy/SeafilePage.vue";
import BuildAndTransfer from "@/components/main/deploy/transfer/BuildAndTransfer.vue";
import BuildAndDeployProgress from "@/components/main/deploy/BuildAndDeployProgress.vue";
import {ref} from "vue";
import type BuildAndDeployProgressResponse from "@/@types/deploy/common/buildAndDeployProgressResponse";
import {tabAndDeployRequestInfoGetInterval} from "@/config/config";
import LocalStorageUtil from "@/utils/LocalStorage";
import {BUILD_AND_TRANSFER_KEY} from "@/utils/Constants";
import {buildAndTransfer} from "@/apis/seafileApi";
import {getBuildAndDeployProgress} from "@/apis/commonDeployApi";
import {ElMessage} from "element-plus";

const progress = ref<BuildAndDeployProgressResponse | null>(null);

setInterval(()=>{
  const requestId = LocalStorageUtil.get(BUILD_AND_TRANSFER_KEY);
  handleBuildAndDeployDetail(requestId,BUILD_AND_TRANSFER_KEY);
},tabAndDeployRequestInfoGetInterval)


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
</script>