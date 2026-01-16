<template>
  <div>
    <el-form
        :model="deployRequest"
        label-width="auto"
        ref="ruleFormRef"
        :rules="rules">
      <el-form-item label="项目名" prop="projectName">
        <el-select
            v-model="deployRequest.projectName"
            clearable
            filterable
            placeholder="请选择项目">
          <el-option
              v-for="item in projectOption"
              :key="item.label"
              :value="item.value"
              :disabled="item.disabled">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="环境" prop="environment">
        <el-select
            v-model="deployRequest.environment"
            filterable
            clearable
            placeholder="请选择环境">
          <el-option v-for="item in environmentOption" :key="item.label" :value="item.value"
                     :label="item.label"
                     :disabled="item.disabled"></el-option>
        </el-select>
        <el-tooltip content="当前正运行情况" placement="bottom" effect="light"
                    v-if="showDeployDetail">
          <el-button text @click="envDeployDetail">
            <detail-icon :width="30" :height="30"></detail-icon>
          </el-button>
        </el-tooltip>
      </el-form-item>
      <el-form-item label="Tag"
                    prop="tag">
        <el-autocomplete v-model="deployRequest.tag"
                         clearable
                         :fetch-suggestions="tagGet"
                         :debounce='500'
                         :disabled="deployRequest.projectName == undefined
                             || deployRequest.projectName.length === 0">
          <template #loading>
            <svg class="circular" viewBox="0 0 50 50">
              <circle class="path" cx="25" cy="25" r="20" fill="none"/>
            </svg>
          </template>
          <template #default="{item}">
            {{ item.label }} / {{ item.branch }}
          </template>
        </el-autocomplete>
      </el-form-item>
      <div style="display: flex; justify-content: flex-end; margin-right: 10px;margin-top: 40px">
        <el-button size="large" @click="doDeploy(ruleFormRef)" :loading="doDeployLoading">
          <el-space size="small">
            <deploy-only-icon :width="22" :height="22"></deploy-only-icon>
            <span>仅部署</span>
          </el-space>
        </el-button>
      </div>
    </el-form>
  </div>

  <el-dialog v-model="dialogShow"
             center
             draggable
             destroy-on-close
             style="border-radius: 15px" width="30%"
  >
    <template #header>
      当前环境正运行信息：
    </template>
    <el-descriptions border :column="1">
      <el-descriptions-item>
        <template #label>
          环境
        </template>
        {{ environmentOption!.find(p => p.value == deployRequest.environment)!.label }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          项目名
        </template>
        {{ envDeployResult!.projectName }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          Tag
        </template>
        {{ envDeployResult!.tag }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <div class="cell-item">
            分支
          </div>
        </template>
        {{ envDeployResult!.branch }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          打包时间
        </template>
        {{ timeShow(envDeployResult!.date) }}
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup lang="ts">
import {getProjectInfo, getEnvironment, getBuildHistory} from '@/apis/projectApi';
import type DeployRequest from '@/@types/deploy/common/deployRequest';
import {ElLoading, ElMessage, type FormInstance} from 'element-plus';
import {deployOnlyRules} from './ts/formCheck';
import useDeployProjectStore from "@/utils/pinia/useDeployProjectStore";
import {deployDetail, deployOnly} from "@/apis/commonDeployApi";
import LocalStorageUtil from "@/utils/LocalStorage";
import {DEPLOY_ONLY_REQUEST_KEY, DeployOnlyName} from "@/utils/Constants";
import {projectWeightSort, projectWeightUpdate} from "@/components/main/deploy/common/ts/projectWeight";
import sleep from "@/utils/sleeep";
import {computed, onMounted, onUnmounted, ref, watch} from "vue";
import {registerTabWatch} from "@/components/main/deploy/common/ts/deployTab";
import DeployOnlyIcon from "@/assets/icons/DeployOnlyIcon.vue";
import timeShow from "@/utils/time";
import type BuildHistoryResponse from "@/@types/deploy/common/buildHistoryResponse";
import DetailIcon from "@/assets/icons/DetailIcon.vue";

const deployRequest = ref<DeployRequest>({
  projectName: '',
  environment: '',
  tag: ''
})


interface ProjectOption {
  label: string;
  value: string;
  disabled?: boolean;
}


//form校验
const rules = ref(deployOnlyRules)
const ruleFormRef = ref<FormInstance>()

//========   项目名称  =============
//一般来说项目名不易变更，因此组件加载的时候请求一次项目名即可 不用每次点击select的时候查询
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


// ========= 环境 ==============
//与项目一样 基本无变动 因此只在组件加载的时候查一次
const environmentOption = ref<ProjectOption[]>([]);
onMounted(async () => {
  const result = await getEnvironment();
  if (result.success) {
    environmentOption.value = result.data.map(p => (
        {
          label: p.envDescription,
          value: p.envName,
          disabled: p.serverSet == null || p.serverSet.length == 0
        }))
  } else {
    ElMessage.error(result.msg);
  }
})


//========= tag ==============
function tagGet(queryString: string, callback: any) {
  if (deployRequest.value.projectName != undefined
      && deployRequest.value.projectName.length != 0) {
    const result = getBuildHistory(deployRequest.value.projectName);
    result.then((res) => {
      if (res.success) {
        const tagList = res.data.map(p => ({label: p.tag, value: p.tag, branch: p.branch}))
        //只在未输入任何信息或输入了 但包含这个信息时给出建议
        if (!queryString) {
          callback(tagList);
        } else {
          callback(tagList.filter(p => p.value.includes(queryString)))
        }

      } else {
        ElMessage.error(res.msg);
      }
    }).catch((error) => {
      ElMessage.error(error);
    })
  }
}


// ========= 组件联动监听 =======
const deployProjectStore = useDeployProjectStore();

registerTabWatch((tab) => {
  //一旦tab变了且等于自己的时候，监听并处理这种变化
  if (tab === DeployOnlyName) {
    deployProjectStore.setDeployProject(deployRequest.value);
  }
})

onUnmounted(() => {
  deployProjectStore.clear();
})

watch(() => deployRequest.value.projectName, () => {
  deployRequest.value.tag = '';
  //更新pinia状态
  deployProjectStore.setDeployProject(deployRequest.value);

  //更新所选项目的优先级 做到更人性化的展示最常用的项目放上面
  projectWeightUpdate(deployRequest.value.projectName);
  projectWeightSort(projectOption.value);
})


// ========== 打包和部署 ===========
//key值 存入localstorage 方便刷新后也能查看打包信息
const doDeployLoading = ref<boolean>(false);

async function doDeploy(formEl: FormInstance | undefined) {
  if (!formEl) return
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      //请求打包和部署
      doDeployLoading.value = true;
      await sleep(500);
      const result = await deployOnly(deployRequest.value);
      doDeployLoading.value = false;
      if (result.success) {
        LocalStorageUtil.set(DEPLOY_ONLY_REQUEST_KEY, result.data);
        ElMessage.success("请求部署成功");
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}


const dialogShow = ref<boolean>(false);
const envDeployResult = ref<BuildHistoryResponse>();
const showDeployDetail = computed(() => deployRequest.value.projectName && deployRequest.value.environment)

async function envDeployDetail() {
  const loading = ElLoading.service({
    lock: true,
    text: 'Loading',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  const result = await deployDetail(deployRequest.value.environment!, deployRequest.value.projectName);
  loading.close()
  if (result.success) {
    envDeployResult.value = result.data
    dialogShow.value = true;
  } else {
    ElMessage.error(result.msg);
  }
}

</script>


<style scoped>
.el-form-item {
  margin-top: 30px;
}


.el-select-dropdown__loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
  font-size: 20px;
}

.circular {
  display: inline;
  height: 30px;
  width: 30px;
  animation: loading-rotate 2s linear infinite;
}

.path {
  animation: loading-dash 1.5s ease-in-out infinite;
  stroke-dasharray: 90, 150;
  stroke-dashoffset: 0;
  stroke-width: 2;
  stroke: var(--el-color-primary);
  stroke-linecap: round;
}

.loading-path .dot1 {
  transform: translate(3.75px, 3.75px);
  fill: var(--el-color-primary);
  animation: custom-spin-move 1s infinite linear alternate;
  opacity: 0.3;
}

.loading-path .dot2 {
  transform: translate(calc(100% - 3.75px), 3.75px);
  fill: var(--el-color-primary);
  animation: custom-spin-move 1s infinite linear alternate;
  opacity: 0.3;
  animation-delay: 0.4s;
}

.loading-path .dot3 {
  transform: translate(3.75px, calc(100% - 3.75px));
  fill: var(--el-color-primary);
  animation: custom-spin-move 1s infinite linear alternate;
  opacity: 0.3;
  animation-delay: 1.2s;
}

.loading-path .dot4 {
  transform: translate(calc(100% - 3.75px), calc(100% - 3.75px));
  fill: var(--el-color-primary);
  animation: custom-spin-move 1s infinite linear alternate;
  opacity: 0.3;
  animation-delay: 0.8s;
}

@keyframes loading-rotate {
  to {
    transform: rotate(360deg);
  }
}

@keyframes loading-dash {
  0% {
    stroke-dasharray: 1, 200;
    stroke-dashoffset: 0;
  }

  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -40px;
  }

  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -120px;
  }
}

@keyframes custom-spin-move {
  to {
    opacity: 1;
  }
}
</style>