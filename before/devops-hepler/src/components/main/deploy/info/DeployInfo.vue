<template>
  <el-container>
    <el-header>
      <el-form inline>
        <el-form-item label="环境">
          <el-select
              v-model="filterEnv"
              filterable
              clearable
              placeholder="请选择环境"
              style="width: 200px;">
            <el-option v-for="item in environmentOption" :key="item.label" :value="item.value"
                       :label="item.label"
                       :disabled="item.disabled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目">
          <el-select
              v-model="filterProject"
              clearable
              filterable
              placeholder="请选择项目"
              style="width: 200px;">
            <el-option
                v-for="item in projectOption"
                :key="item.label"
                :value="item.value"
                :disabled="item.disabled">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :loading="refreshLoading" @click="getDeployInfoRefresh">
            <el-icon>
              <refresh/>
            </el-icon>
          </el-button>
        </el-form-item>
      </el-form>
    </el-header>
    <el-main style="margin-top: 2%">
      <el-table :data="data" stripe max-height="500" :table-layout="'fixed'">
        <el-table-column type="index" label="#"></el-table-column>
        <el-table-column label="环境" align="center" header-align="center">
          <template #default="scope">
            {{ environmentOption!.find(p => p.value == scope.row.envName)!.label }}
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="项目名" align="center" header-align="center">
        </el-table-column>
        <el-table-column prop="branch" label="分支" align="center" header-align="center">
        </el-table-column>
        <el-table-column prop="tag" label="Tag" align="center" header-align="center">
        </el-table-column>
        <el-table-column label="时间" align="center" header-align="center">
          <template #default="scope">
            {{ timeShow(scope.row.date) }}
          </template>
        </el-table-column>
      </el-table>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import {onMounted, ref, watch} from "vue";
import {getEnvironment, getProjectInfo} from "@/apis/projectApi";
import {projectWeightSort} from "@/components/main/deploy/common/ts/projectWeight";
import {ElLoading, ElMessage} from "element-plus";
import {deployDetailList} from "@/apis/commonDeployApi";
import type ProjectDeployInfoResponse from "@/@types/deploy/common/projectDeployInfoResponse";
import debounce from "@/utils/debounce";
import timeShow from "../../../../utils/time";
import {Refresh} from "@element-plus/icons-vue";


const filterEnv = ref<string>("");
const filterProject = ref<string>("");


interface ProjectOption {
  label: string;
  value: string;
  disabled?: boolean;
}

const projectOption = ref<ProjectOption[]>([]);
onMounted(async () => {
  const result = await getProjectInfo();
  if (result.success) {
    const optionList = result.data.map(p => ({label: p.projectName, value: p.projectName, disabled: p.disabled}))
    projectWeightSort(optionList);
    projectOption.value = optionList;
  } else {
    ElMessage.error(result.msg);
  }
})

const environmentOption = ref<ProjectOption[]>([]);
onMounted(async () => {
  const result = await getEnvironment();
  if (result.success) {
    environmentOption.value = result.data.map(p => (
        {
          label: p.envDescription,
          value: p.envName,
          disabled: false
        }))
  } else {
    ElMessage.error(result.msg);
  }
})


onMounted(() => {
  getDeployInfo()
})

const data = ref<ProjectDeployInfoResponse[]>();

const refreshLoading = ref<boolean>(false);

async function getDeployInfo() {
  if(!filterEnv.value && ! filterProject.value){
    ElMessage.warning("请选择环境或项目");
    data.value = [];
    return;
  }
  const loading = ElLoading.service({
    lock: true,
    text: 'Loading',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  console.log("yhs-test:",filterEnv.value,filterProject.value)
  const result = await deployDetailList(filterEnv.value, filterProject.value);
  loading.close();
  if (result.success) {
    data.value = result.data;
  } else {
    ElMessage.error(result.msg);
  }
}

async function getDeployInfoRefresh() {
  refreshLoading.value = true
  await getDeployInfo();
  refreshLoading.value = false
  ElMessage.success("刷新成功")
}

const debouncedGetDeployList = debounce(getDeployInfo, 500);

watch([filterEnv, filterProject], debouncedGetDeployList, {deep: true});
</script>