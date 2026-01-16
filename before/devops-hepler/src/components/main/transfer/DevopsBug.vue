<template>
  <el-container>
    <el-header>
      <devops-filter-form v-model:sprint-model="selectedSprint"
                          v-model:status-model="selectedStatus"
                          v-model:user-model="selectedUser"
                          @send-mail="bugSendMail"
                          @user-fetch-suggestion="userFetchSuggestion">
      </devops-filter-form>
    </el-header>
    <el-container style="margin-top: 1%">
      <el-main>
        <el-table :data="bugList"
                  max-height="660"
                  :row-class-name="tableRowClassName">
          <el-table-column label="内容" header-align="center">
            <template #default="scope">
              [{{ scope.row.key }}]{{ scope.row.subject }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" header-align="center" align="center">
            <template #default="scope">
              <span :class="getStatusClass(scope.row.stateName)">{{ scope.row.stateName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="责任人" width="120" header-align="center" align="center">
            <template #default="scope">
              {{ scope.row.assigneeName }}
            </template>
          </el-table-column>
          <el-table-column label="审查结果" header-align="center">
            <template #default="scope">
              <template v-for="(item,index) in scope.row.suggest" :key="index">
                <el-tooltip content="删除审查建议" effect="light">
                  <el-button icon="warning-filled" type="warning" text
                             @click="deleteSuggest(scope.row,index)"></el-button>
                </el-tooltip>
                <span>{{ item }}<br/></span>
              </template>
            </template>
          </el-table-column>
        </el-table>

      </el-main>

    </el-container>
  </el-container>
</template>

<script setup lang="ts">

import {ref, watch} from "vue";
import {getBugList, sendMail} from "@/apis/devopsApi";
import {ElLoading, ElMessage, ElMessageBox} from "element-plus";
import type DevopsWorkIssueWrapper from "@/@types/transfer/devopsWorkIssueWrapper";
import DevopsFilterForm from "@/components/main/transfer/DevopsFilterForm.vue";
import type {WorkIssueStatusType} from "@/@types/transfer/workIssueStatus";
import deleteSuggest from "@/components/main/transfer/ts/deleteSuggest";
import tableRowClassName from "@/components/main/transfer/ts/rowClass";

const selectedSprint = ref<string>('');
const selectedUser = ref<string>('');
const selectedStatus = ref<WorkIssueStatusType>('ALL');

const bugList = ref<DevopsWorkIssueWrapper[]>([]);

let bugListMeta: DevopsWorkIssueWrapper[];
watch(selectedSprint, getRequestAndBugList)

watch([selectedStatus, selectedUser], filter)

async function getRequestAndBugList() {
  if (selectedSprint.value) {
    getBugList(selectedSprint.value)
        .then(res => {
          if (res.success) {
            bugListMeta = res.data;
            filter();
          } else {
            ElMessage.error(res.msg)
          }
        }).catch((error) => ElMessage.error(error))
  }
}

function filter() {
  if (selectedStatus.value) {
    switch (selectedStatus.value) {
      case "ALL":
        bugList.value = bugListMeta;
        break;
      case "SUCCESS":
        bugList.value = bugListMeta.filter(p => p.success);
        break;
      case "FAIL":
        bugList.value = bugListMeta.filter(p => !p.success);
        break;
    }
  }
  if (selectedUser.value) {
    bugList.value = bugList.value.filter(p => p.assigneeName.includes(selectedUser.value))
  }
}

function userFetchSuggestion(queryString: string, callback: any) {
  const nameSet = new Set(bugListMeta.map(p => p.assigneeName));
  const nameArray = [...nameSet].map(p => ({value: p}))
  const result = queryString ? nameArray.filter(p => p.value.includes(queryString)) : nameArray;
  callback(result);
}

async function bugSendMail() {
  const failList = bugList.value?.filter(p => !p.success);
  if (failList == null || failList.length === 0) {
    ElMessage.warning("没有可通知的内容");
    return;
  }
  ElMessageBox.confirm(
      '确认发送邮件?',
      {
        confirmButtonText: '是',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    const loading = ElLoading.service({
      lock: true,
      text: 'Loading',
      background: 'rgba(0, 0, 0, 0.7)',
    })
    const result = await sendMail(failList);
    loading.close();
    if (result.success) {
      ElMessage.success("发送成功");
    } else {
      ElMessage.error("发送失败" + result.msg);
    }
  })
}

function getStatusClass(status: string) {
  switch (status) {
    case '未开始':
      return 'status_span unfinished'
    case '实现中':
      return 'status_span implementing'
    case '测试验证':
      return 'status_span implementing'
    case '已完成':
      return 'status_span finished'
    case '已拒绝':
      return 'status_span refused'
    case '阻塞':
      return 'status_span block'
    default:
      return 'status_span'
  }
}

</script>

<style>
.el-table .warning-row {
  --el-table-tr-bg-color: var(--el-color-warning-light-9);
}

.status_span {
  color: white;
  display: inline-block;
  width: 80px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 20px;
}

.unfinished {
  background-color: #bfbfbf;
}

.implementing {
  background-color: #2587f6;
}

.finished {
  background-color: #17a82a;
}

.refused {
  background-color: #F56C6C;
}

.block {
  background-color: crimson;
}
</style>