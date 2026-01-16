<template>
  <div v-if="deployProjectStore.projectName">
    <div style="display: flex; justify-content: flex-end;margin-bottom: 2%;">
      <el-space size="small">
        <el-input v-model="search"
                  placeholder="请输入筛选内容"
                  prefix-icon="search"
                  @change="buildHistoryGet"
                  clearable/>
        <el-button icon="refresh" @click="buildHistoryGetAndLoading" :loading="refreshLoading"></el-button>
        <el-tooltip content="仅支持按分支或tag筛选" placement="bottom" effect="light">
          <el-icon color="#E6A23C">
            <warning/>
          </el-icon>
        </el-tooltip>
      </el-space>
    </div>

    <div style="display: flex; justify-content: flex-end;">
      <el-scrollbar height="760">
        <el-timeline style="margin-top: 1%">
          <el-timeline-item v-for="item in buildHistoryList"
                            :key="item.id"
                            :timestamp="timeShow(item.date,false)"
                            placement="top"
                            :icon="iconShow(item.status)"
                            :color="colorShow(item.status)">
            <el-card>
              <el-space :size="20" wrap>
                <!--id-->
                <el-tooltip content="详情" placement="bottom" effect="light">
                  <el-button text @click="showDialog(item)">
                    <detail-icon :width="18" :height="18"></detail-icon>
                    <span style="font-weight: bold; color: #303133;margin-left: 5px"> {{ `${item.id}` }}</span>
                  </el-button>
                </el-tooltip>
                <!--分支-->
                <el-space :size="5">
                  <branch-icon :width="18" :height="18"></branch-icon>
                  <span style="font-weight: bold">{{ `${item.branch}` }}</span>
                </el-space>
                <!--tag-->
                <el-space :size="5">
                  <tag-icon :width="18" :height="18"></tag-icon>
                  <span style="font-weight: bold">{{ `${item.tag}` }}</span>
                </el-space>
                <!-- 耗时-->
                <el-space :size="5">
                  <el-tooltip content="耗时" placement="bottom" effect="light">
                    <TookTimeIcon :width="18" :height="18"></TookTimeIcon>
                  </el-tooltip>
                  <span> {{takeTimeShow(item.tookTime)}}</span>
                </el-space>
                <el-space :size="5" v-if="item.status === 'Pending'">
                  <el-tooltip content="预计剩余" placement="bottom" effect="light">
                    <TookTimeIcon :width="18" :height="18"></TookTimeIcon>
                  </el-tooltip>
                  <span> {{takeTimeShow(item.remainingTime)}}</span>
                </el-space>
              </el-space>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-scrollbar>
    </div>

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

import type BuildHistoryResponse from "@/@types/deploy/common/buildHistoryResponse";
import {onMounted, ref, watch} from "vue";
import {getBuildDetail, getBuildHistory} from "@/apis/projectApi";
import {ElMessage} from "element-plus";
import useDeployProjectStore from "@/utils/pinia/useDeployProjectStore";
import BranchIcon from "@/assets/icons/BranchIcon.vue";
import TagIcon from "@/assets/icons/TagIcon.vue";
import DetailIcon from "@/assets/icons/DetailIcon.vue";
import BuildDetail from "@/components/main/deploy/BuildDetail.vue";
import {CircleCloseFilled, Loading, SuccessFilled, Warning} from "@element-plus/icons-vue";
import TookTimeIcon from "@/assets/icons/TookTimeIcon.vue";
import sleep from "@/utils/sleeep";
import timeShow from "@/utils/time";

const deployProjectStore = useDeployProjectStore();
const search = ref<string>('');
const buildHistoryList = ref<BuildHistoryResponse[]>([])

const refreshLoading = ref<boolean>(false);

async function buildHistoryGetAndLoading(){
  refreshLoading.value = true;
  //延迟0.5s 要个loading效果
  await sleep(500);
  await buildHistoryGet();
  refreshLoading.value = false;
}

async function buildHistoryGet() {
  if (deployProjectStore.projectName) {
    const result = await getBuildHistory(deployProjectStore.projectName, search.value)
    if (result.success) {
      buildHistoryList.value = result.data;
    } else {
      ElMessage.error(result.msg);
    }
  }
}

onMounted(() => {
  buildHistoryGet();
})
watch(() => deployProjectStore.projectName, buildHistoryGet)




function iconShow(status: 'Success' | 'Failed' | 'Pending' | 'Aborted'): string {
  switch (status) {
    case "Success":
      return 'circleCheck';
    case "Failed":
      return 'circleClose';
    case 'Pending':
      return 'loading';
    case 'Aborted':
      return 'circleClose';
  }
}

function colorShow(status: 'Success' | 'Failed' | 'Pending' | 'Aborted'): string {
  switch (status) {
    case "Success":
      return '#67C23A';
    case "Failed":
      return '#F56C6C'
    case "Pending":
      return '#409EFF';
    case "Aborted":
      return '#909399'
  }
}

function takeTimeShow(time:number|null|undefined):string{
  if(time == null){
    return '--';
  }
  const minutes = Math.floor(time/60);
  const seconds = time-minutes*60;
  let result = "";
  if(minutes > 0){
    result += `${minutes}分`
  }
  result += `${seconds}秒`
  return result;
}

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

</script>
<style>

.el-dialog__header {
  padding-bottom: 10px;
}

.el-dialog--center {
  text-align: left;
}
</style>