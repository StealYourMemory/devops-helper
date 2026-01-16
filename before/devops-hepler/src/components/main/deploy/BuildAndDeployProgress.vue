<template>
  <div style="width: 90%">
    <el-card>
      <el-collapse v-model="activeNames">
        <el-collapse-item name="build" title="打包进度" v-if="progress?.buildProgress">
          <el-space wrap v-if="progress?.buildProgress?.success" direction="vertical">
            <!--进度-->
            <el-space v-if="progress.buildProgress.data.status === 'Pending'">
              <el-tooltip>
                <template #content>
                  已用时间：{{ takeTimeShow(progress.buildProgress.data.tookTime) }}<br/>
                  预计还需：{{ takeTimeShow(progress.buildProgress.data.remainingTime) }}<br/>
                </template>
                <el-progress type="circle"
                             :percentage="progressShow(progress.buildProgress.data)"
                             :width="80"
                             style="text-align: center">
                </el-progress>
              </el-tooltip>
            </el-space>
            <el-space v-else>
              <el-progress type="circle"
                           :percentage="100"
                           :status="progress.buildProgress.data.status ==='Success'? 'success':'exception' "
                           :width="80"
                           style="text-align: center">
              </el-progress>
            </el-space>

            <el-space>
              <!--id-->
              <el-tooltip content="详情" placement="bottom" effect="light">
                <el-button text @click="showDialog(progress.buildProgress.data)">
                  <detail-icon :width="18" :height="18"></detail-icon>
                  <span style="font-weight: bold; color: #303133;margin-left: 5px"> {{
                      `${progress.buildProgress.data.id}`
                    }}</span>
                </el-button>
              </el-tooltip>
              <!--分支-->
              <el-space :size="5">
                <branch-icon :width="18" :height="18"></branch-icon>
                <span style="font-weight: bold">{{ `${progress.buildProgress.data.branch}` }}</span>
              </el-space>
              <!--tag-->
              <el-space :size="5">
                <tag-icon :width="18" :height="18"></tag-icon>
                <span style="font-weight: bold">{{ `${progress.buildProgress.data.tag}` }}</span>
              </el-space>
              <el-space :size="5" v-if="progress.buildProgress.data.status !== 'Pending'">
                <el-tooltip content="耗时" placement="bottom" effect="light">
                  <TookTimeIcon :width="18" :height="18"></TookTimeIcon>
                </el-tooltip>
                <span> {{ takeTimeShow(progress.buildProgress.data.tookTime) }}</span>
              </el-space>
            </el-space>
          </el-space>
          <el-space wrap v-if="progress?.buildProgress?.success === false" direction="vertical">
            <el-text type="danger">失败：{{ progress?.buildProgress?.msg }}</el-text>
          </el-space>
        </el-collapse-item>
        <el-collapse-item name="deploy" title="部署进度" v-if="progress?.deployProgress">
          <el-scrollbar v-if="progress?.deployProgress">
            <div class="scrollbar-flex-content" v-for="(item,index) in progress?.deployProgress" :key="index">
              <pre>{{ progress?.deployProgress[index] }}</pre>
            </div>
          </el-scrollbar>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-dialog v-model="dialogShow"
               center
               draggable
               destroy-on-close
               style="border-radius: 15px" width="70%"
    >
      <template #header>
        <div>
          <el-icon
              v-if="dialogStatus === 'Success'"
              color="#67C23A"
              size="28"
              style="vertical-align: middle;">
            <success-filled/>
          </el-icon>
          <el-icon
              v-if="dialogStatus === 'Failed'"
              color="#F56C6C"
              size="28"
              style="vertical-align: middle;">
            <circle-close-filled/>
          </el-icon>
          <el-icon
              v-if="dialogStatus === 'Aborted'"
              color="#909399"
              size="28"
              style="vertical-align: middle;">
            <circle-close-filled/>
          </el-icon>
          <el-icon
              v-if="dialogStatus === 'Pending'"
              color="#409EFF"
              size="28"
              style="vertical-align: middle;">
            <loading/>
          </el-icon>
          <span style="font-weight: bold; vertical-align: middle; margin-left: 10px">控制台输出</span>
        </div>
      </template>
      <build-detail :content="dialogShowContent"
                    :style=" {
        height: 600,
        width: 400,
        borderRadius: 15,
        padding: 0
      }"></build-detail>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import type BuildAndDeployProgressResponse from "@/@types/deploy/common/buildAndDeployProgressResponse";
import {ref} from "vue";
import TagIcon from "@/assets/icons/TagIcon.vue";
import DetailIcon from "@/assets/icons/DetailIcon.vue";
import BranchIcon from "@/assets/icons/BranchIcon.vue";
import TookTimeIcon from "@/assets/icons/TookTimeIcon.vue";
import type BuildHistoryResponse from "@/@types/deploy/common/buildHistoryResponse";
import {getBuildDetail} from "@/apis/projectApi";
import {ElMessage} from "element-plus";
import BuildDetail from "@/components/main/deploy/BuildDetail.vue";
import {CircleCloseFilled, Loading, SuccessFilled} from "@element-plus/icons-vue";

interface Props {
  progress: BuildAndDeployProgressResponse | null
}

withDefaults(defineProps<Props>(), {
  progress: null
})

const activeNames = ref<string[]>(['build', 'deploy'])


const dialogShow = ref<boolean>(false);
const dialogShowContent = ref<string>("");
const dialogStatus = ref<'Success' | 'Failed' | 'Pending' | 'Aborted'>("Success");

async function showDialog(item: BuildHistoryResponse) {
  dialogStatus.value = item.status
  const result = await getBuildDetail(item.projectName, item.id);
  if (result.success) {
    dialogShowContent.value = result.data;
    dialogShow.value = true;
  } else {
    ElMessage.error(result.msg);
  }
}

function takeTimeShow(time: number | null | undefined): string {
  if (time == null) {
    return '--';
  }
  const minutes = Math.floor(time / 60);
  const seconds = time - minutes * 60;
  let result = "";
  if (minutes > 0) {
    result += `${minutes}分`
  }
  result += `${seconds}秒`
  return result;
}


function progressShow(item: BuildHistoryResponse): number {
  return Math.floor(item.tookTime / (item.tookTime + item.remainingTime) * 100);
}
</script>
<style>
.scrollbar-flex-content {
  display: flex;
}
</style>