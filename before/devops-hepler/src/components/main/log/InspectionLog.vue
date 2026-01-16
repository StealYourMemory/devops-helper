<template>
  <el-container>
    <el-header v-if="filter">
      <el-form v-model="filter" inline>
        <el-form-item label="用户名">
          <el-input v-model="filter.user" clearable></el-input>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="filter.operation" clearable></el-input>
        </el-form-item>
        <el-form-item label="是否成功">
          <el-select v-model="filter.success" clearable placeholder="是否成功">
            <el-option :value="true" label="是"></el-option>
            <el-option :value="false" label="否"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-space>
            <el-date-picker v-model="filter.startTime"
                            type="datetime"
                            placeholder="开始"
                            value-format='x'
                            clearable>
            </el-date-picker>
            <el-date-picker v-model="filter.endTime"
                            type="datetime"
                            placeholder="结束"
                            value-format='x'
                            clearable>
            </el-date-picker>
          </el-space>
        </el-form-item>
      </el-form>
    </el-header>
    <el-main style="margin-top: 2%">
      <el-table :data="pageInfo.records" stripe max-height="500">
        <el-table-column type="index" label="#"></el-table-column>
        <el-table-column prop="user" label="用户" width="100" align="center" header-align="center">
        </el-table-column>
        <el-table-column prop="operation" label="操作类型" width="140" align="center" header-align="center">
        </el-table-column>
        <el-table-column prop="requestDetail" label="请求详情" align="center" header-align="center"
                         :show-overflow-tooltip="{'effect':'light','placement':'bottom'}"
                         tooltip-effect='light'>
        </el-table-column>
        <el-table-column label="结果" align="center" header-align="center">
          <template #default="scope">
            {{ scope.row.success == null ? "--":scope.row.success ? "执行成功" : "执行失败" }}
          </template>
        </el-table-column>
        <el-table-column prop="resultDetail" label="结果详情" align="center" header-align="center"
                         :show-overflow-tooltip="{'effect':'light','placement':'bottom'}"
                         tooltip-effect='light'>
        </el-table-column>
        <el-table-column label="开始时间" align="center" header-align="center" width="200">
          <template #default="scope">
            {{ timeShow(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" align="center" header-align="center" width="200">
          <template #default="scope">
            {{ scope.row.endTime == null? "--":timeShow(scope.row.endTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-main>
    <el-footer class="footer-right">
      <el-pagination v-model:current-page="pageInfo.page"
                     v-model:page-size="pageInfo.size"
                     :page-sizes="[10,20,50,100]"
                     layout="total,prev, pager, next,sizes"
                     :total="pageInfo.total"
                     @size-change="getLogByFilterAndPage"
                     @current-change="getLogByFilterAndPage"/>
    </el-footer>
  </el-container>

</template>
<script setup lang="ts">

import {onMounted, ref, watch} from "vue";
import type {LogFilter, OperationLog} from "@/@types/operationLog";
import type Page from "@/@types/page";
import {getLog} from "@/apis/operationLogApi";
import {ElMessage} from "element-plus";
import timeShow from "../../../utils/time";
import debounce from "@/utils/debounce";


const filter = ref<LogFilter>({
  user: "",
  operation: "",
});

const pageInfo = ref<Page<OperationLog>>({
  paging: true,
  page: 1,
  total:0,
  size: 10
})

onMounted(() => {
  getLogByFilterAndPage();
})

const debouncedGetLogByFilterAndPage = debounce(getLogByFilterAndPage, 300);

watch(filter, debouncedGetLogByFilterAndPage, {deep: true});

async function getLogByFilterAndPage() {
  const result = await getLog({...filter.value, ...pageInfo.value});
  if (result.success) {
    const {paging, page, size, total, records} = result.data as Page<OperationLog>;
    pageInfo.value = {paging, page, size, total, records};
  } else {
    ElMessage.error(result.msg);
  }
}

</script>

<style scoped>
.el-select {
  --el-select-width: 120px;
}

.el-input {
  --el-input-width: 160px;
}

.footer-right {
  display: flex;
  justify-content: flex-end;
  padding-right: 20px; /* 可根据需要调整 */
}
</style>