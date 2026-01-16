<template>
  <el-container>
    <el-header>
      <devops-filter-form v-model:sprint-model="selectedSprint"
                          v-model:status-model="selectedStatus"
                          v-model:user-model="selectedUser"
                          @send-mail="requirementSendMail"
                          @user-fetch-suggestion="userFetchSuggestion">
      </devops-filter-form>
    </el-header>
    <el-container style="margin-top: 1%">
      <el-main>
        <el-table :data="requirementList"
                  row-key="key"
                  default-expand-all
                  max-height="660"
                  :row-class-name="tableRowClassName">
          <el-table-column label="级别" header-align="center" align="center" width="160">
            <template #default="scope">
              <span :class="getLevelClass(scope.row.projectIssueTypeName)">{{ scope.row.projectIssueTypeName }}</span>
            </template>
          </el-table-column>
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
import {getRequestList, sendMail} from "@/apis/devopsApi";
import {ElLoading, ElMessage, ElMessageBox} from "element-plus";
import type DevopsWorkIssueWrapper from "@/@types/transfer/devopsWorkIssueWrapper";
import DevopsFilterForm from "@/components/main/transfer/DevopsFilterForm.vue";
import type {WorkIssueStatusType} from "@/@types/transfer/workIssueStatus";
import tableRowClassName from "@/components/main/transfer/ts/rowClass";
import deleteSuggest from "@/components/main/transfer/ts/deleteSuggest";

const selectedSprint = ref<string>('');
const selectedUser = ref<string>('');
const selectedStatus = ref<WorkIssueStatusType>('ALL');

const requirementList = ref<DevopsWorkIssueWrapper[]>([]);

let requirementListMeta: DevopsWorkIssueWrapper[];
watch(selectedSprint, getRequestAndBugList)

watch([selectedStatus, selectedUser], filter)

async function getRequestAndBugList() {
  if (selectedSprint.value) {
    getRequestList(selectedSprint.value)
        .then(res => {
          if (res.success) {
            requirementListMeta = res.data;
            filter();
          } else {
            ElMessage.error(res.msg)
          }
        }).catch((error) => ElMessage.error(error))
  }
}

function filter() {
  switch (selectedStatus.value) {
    case "ALL":
      requirementList.value = requirementListMeta;
      break;
    case "SUCCESS":
      requirementList.value = requirementListMeta.filter(p => isSuccess(p));
      break;
    case "FAIL":
      requirementList.value = requirementListMeta.filter(p => !isSuccess(p));
      break;
    default:
      requirementList.value = requirementListMeta;
      break;
  }
  if (selectedUser.value) {
    requirementList.value = requirementList.value.filter(p => hasName(p))
  }
}

function userFetchSuggestion(queryString: string, callback: any) {
  const nameSet = new Set(getUserNameArray(requirementListMeta));
  const nameArray = [...nameSet].map(p => ({value: p}))
  const result = queryString ? nameArray.filter(p => p.value.includes(queryString)) : nameArray;
  callback(result);
}

function getUserNameArray(list: DevopsWorkIssueWrapper[]) {
  let nameArray = list.map(p => p.assigneeName);
  for (const item of list) {
    if (item.children != null && item.children.length > 1) {
      nameArray = [...nameArray, ...getUserNameArray(item.children)]
    }
  }
  return nameArray;
}

/*
递归遍历子树 全成功才叫成功
 */
function isSuccess(item: DevopsWorkIssueWrapper) {
  if (!item.success) {
    return false;
  } else if (item.children != null && item.children.length > 1) {
    for (let child of item.children) {
      if (!isSuccess(child)) {
        return false
      }
    }
  }
  return true;
}

function hasName(item: DevopsWorkIssueWrapper) {
  if (item.assigneeName.includes(selectedUser.value)) {
    return true;
  } else if (item.children != null && item.children.length > 1) {
    for (let child of item.children) {
      if (hasName(child)) {
        return true;
      }
    }
  }
  return false;
}

function getLevelClass(level: string) {
  switch (level) {
    case '史诗':
      return 'level_span epic';
    case '特性':
      return 'level_span characteristic';
    case '用户故事':
      return 'level_span user_stories';
    case '任务':
      return 'level_span task';
    default:
      return 'level_span';
  }
}

function getStatusClass(status: string) {
  switch (status) {
    case '未开始':
      return 'status_span unfinished'
    case '实现中':
      return 'status_span implementing'
    case '已完成':
      return 'status_span finished'
    case '已拒绝':
      return 'status_span refused'
    default:
      return 'status_span'
  }
}

async function requirementSendMail() {
  const failList = requirementList.value?.filter(p => !isSuccess(p));
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


</script>
<style>

.el-table .warning-row {
  --el-table-tr-bg-color: var(--el-color-warning-light-9);
}

.level_span {
  color: white;
  display: inline-block;
  width: 80px;
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

.epic {
  background-color: #f0249c;
}

.characteristic {
  background-color: #ecc411;
}

.user_stories {
  background-color: #17a72a;
}

.task {
  background-color: #fc7700;
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

</style>