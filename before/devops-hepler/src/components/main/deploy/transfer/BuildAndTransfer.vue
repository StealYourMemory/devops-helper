<template>
  <div>
    <div>
      <h4 style="display: inline-block;margin-right: 5%">打包并更新到seafile</h4>
      <el-tooltip placement="bottom" effect="light">
        <template #content>
          以当前项目的上一个yaml作为模板，只更新tag信息<br/>
          生成的yaml会放到update_yyMMdd文件夹下的yaml文件夹内<br/>
          提交的更新说明内容会尾追加到update_yyMMdd文件夹的《更新说明.txt》文件内<br/>
          如果不存在上述文件夹或文件则会新建
        </template>
        <el-icon color="#E6A23C">
          <warning/>
        </el-icon>
      </el-tooltip>
    </div>
    <el-form
        :model="buildAndTransferRequest"
        label-width="auto"
        ref="ruleFormRef"
        :rules="rules">
      <el-form-item label="项目名" prop="projectName">
        <el-select
            v-model="buildAndTransferRequest.projectName"
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
      <el-form-item label="分支" prop="branch">
        <el-select
            v-model="buildAndTransferRequest.branch"
            clearable
            filterable
            @visible-change="getProjectBranchOptional"
            :disabled="buildAndTransferRequest.projectName == undefined || buildAndTransferRequest.projectName == ''"
            :loading="projectBranchLoading" placeholder="请选择项目分支">
          <el-option v-for="item in projectBranchOptional"
                     :key="item.label"
                     :value="item.value"
                     :disabled="item.disabled">

          </el-option>
          <!-- 自定义加载样式 -->
          <template #loading>
            <el-icon class="is-loading">
              <LoadingIcon></LoadingIcon>
            </el-icon>
          </template>
        </el-select>
      </el-form-item>
      <el-form-item label="Tag"
                    prop="tag">
        <el-autocomplete v-model="buildAndTransferRequest.tag"
                         clearable
                         :fetch-suggestions="tagGet"
                         :debounce='500'
                         :disabled="buildAndTransferRequest.projectName == undefined
                             || buildAndTransferRequest.projectName.length === 0
                             || buildAndTransferRequest.branch == undefined
                             || buildAndTransferRequest.branch.length===0 ">
          <template #loading>
            <svg class="circular" viewBox="0 0 50 50">
              <circle class="path" cx="25" cy="25" r="20" fill="none"/>
            </svg>
          </template>
        </el-autocomplete>
      </el-form-item>
      <el-form-item label="更新说明">
        <el-input v-model="buildAndTransferRequest.updateInfo"
                  type="textarea"
                  placeholder="请输入更新说明"
                  :autosize="{ minRows: 2, maxRows: 4 }"
        ></el-input>
      </el-form-item>
      <div style="display: flex; justify-content: flex-end; margin-right: 10px;margin-top: 40px">
        <el-button size="large" @click="doBuildAndTransfer(ruleFormRef)" :loading="doBuildLoading">
          <el-space size="small">
            <package-only-icon :width="22" :height="22"></package-only-icon>
            <span>打包更新</span>
          </el-space>
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import {getProjectBranch, getProjectInfo, getProjectTag} from '@/apis/projectApi';
import {branchGetInterval} from '@/config/config';
import {ElMessage, type FormInstance} from 'element-plus';
import LoadingIcon from "@/assets/icons/LoadingIcon.vue";
import LocalStorageUtil from "@/utils/LocalStorage";
import {projectWeightSort, projectWeightUpdate} from "@/components/main/deploy/common/ts/projectWeight";
import sleep from "@/utils/sleeep";
import {onMounted, ref, watch} from "vue";
import {packageOnlyRules} from "@/components/main/deploy/common/ts/formCheck";
import PackageOnlyIcon from "@/assets/icons/PackageOnlyIcon.vue";
import {BUILD_AND_TRANSFER_KEY} from "@/utils/Constants";
import {Warning} from "@element-plus/icons-vue";
import type BuildAndTransferRequest from "@/@types/deploy/transfer/buildAndTransferRequest";
import {buildAndTransfer} from "@/apis/seafileApi";

const buildAndTransferRequest = ref<BuildAndTransferRequest>({
  projectName: '',
  branch: '',
  tag: '',
  updateInfo: ''
})


interface ProjectOption {
  label: string;
  value: string;
  disabled?: boolean;
}


//form校验
const rules = ref(packageOnlyRules)
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


// ========== 项目分支  ===============
const projectBranchOptional = ref<ProjectOption[]>([]);
const projectBranchLoading = ref<boolean>(false);
//分支变动会比较大 因此每次点击的时候重新请求 但做个防抖 不请求那么快
let lastGetProjectBranchOptional = 0;

async function getProjectBranchOptional() {
  if (Date.now() - lastGetProjectBranchOptional > branchGetInterval || projectBranchOptional.value.length === 0) {
    projectBranchLoading.value = true;
    const result = await getProjectBranch(buildAndTransferRequest.value.projectName);
    //更新上一次的请求时间
    projectBranchLoading.value = false;
    lastGetProjectBranchOptional = Date.now();
    if (result.success) {
      projectBranchOptional.value = result.data.branches.map(p => ({label: p, value: p}))
    } else {
      ElMessage.error(result.msg);
    }

  }
}


//========= tag ==============
function tagGet(queryString: string, callback: any) {
  if (buildAndTransferRequest.value.projectName != undefined
      && buildAndTransferRequest.value.projectName.length != 0
      && buildAndTransferRequest.value.branch != undefined
      && buildAndTransferRequest.value.branch.length != 0) {
    const result = getProjectTag(buildAndTransferRequest.value.projectName, buildAndTransferRequest.value.branch);
    result.then((res) => {
      if (res.success) {
        const suggestTag = res.data;
        //只在未输入任何信息或输入了 但包含这个信息时给出建议
        if (!queryString || suggestTag.includes(queryString)) {
          callback([{label: res.data, value: res.data}]);
        } else {
          callback([]);
        }
      } else {
        ElMessage.error(res.msg);
      }
    }).catch((error) => {
      ElMessage.error(error);
    })
  }
}

watch(() => buildAndTransferRequest.value.projectName, () => {
  buildAndTransferRequest.value.branch = '';
  buildAndTransferRequest.value.tag = '';
  projectBranchOptional.value = [];
  lastGetProjectBranchOptional = 0 - branchGetInterval;
  //更新所选项目的优先级 做到更人性化的展示最常用的项目放上面
  projectWeightUpdate(buildAndTransferRequest.value.projectName);
  projectWeightSort(projectOption.value);
})


// ========== 打包和部署 ===========
//key值 存入localstorage 方便刷新后也能查看打包信息
const doBuildLoading = ref<boolean>(false);

async function doBuildAndTransfer(formEl: FormInstance | undefined) {
  if (!formEl) return
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      //请求打包和部署
      doBuildLoading.value = true;
      await sleep(500);
      const result = await buildAndTransfer(buildAndTransferRequest.value);
      doBuildLoading.value = false;
      if (result.success) {
        LocalStorageUtil.set(BUILD_AND_TRANSFER_KEY, result.data);
        ElMessage.success("请求打包转测成功");
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      console.log('error submit!', fields)
    }
  })
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