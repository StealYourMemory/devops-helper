<template>
  <el-form inline>
    <el-form-item label="迭代版本">
      <el-select
          v-model="selectedSprint"
          filterable
          placeholder="请选择迭代版本"
          :loading='sprintSelectLoading'
          style="width: 180px;">
        <el-option
            v-for="item in sprintOptionals"
            :key="item.label"
            :label="item.label"
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
    <el-form-item label="审查结果">
      <el-select
          v-model="selectedStatus"
          filterable
          placeholder="请选择审查结果"
          style="width: 120px;">
        <el-option
            v-for="item in statusOptionals"
            :key="item.label"
            :label="item.label"
            :value="item.value">
        </el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="责任人">
      <el-autocomplete
          v-model="selectedUser"
          :fetch-suggestions="emitUserFetchSuggestion"
          clearable></el-autocomplete>
    </el-form-item>
    <el-form-item @click="emitSendMail">
      <el-button type="warning">
        <el-icon>
          <message></message>
        </el-icon>
        <span>邮件通知</span>
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {getSprintOptionals} from "@/apis/devopsApi";
import {ElMessage} from "element-plus";
import type {WorkIssueStatusType} from "@/@types/transfer/workIssueStatus";
import {Message} from "@element-plus/icons-vue";
import LoadingIcon from "@/assets/icons/LoadingIcon.vue";

// 声明 emits 事件
const emit = defineEmits(['sendMail','userFetchSuggestion']);

// 定义 emit 事件的方法
const emitSendMail = () => {
  emit('sendMail');
};

const emitUserFetchSuggestion = (queryString:string, callback:any) => {
  emit('userFetchSuggestion', queryString, callback);
};

interface StatusOption {
  label: string;
  value: WorkIssueStatusType
}


const selectedSprint = defineModel<string>('sprintModel', {required: true})
const selectedStatus = defineModel<WorkIssueStatusType>('statusModel', {required: true})
const selectedUser = defineModel<string>('userModel', {required: true})

const statusOptionals = ref<StatusOption[]>([
  {label: "全部", value: 'ALL'},
  {label: "不合格", value: 'FAIL'},
  {label: "合格", value: 'SUCCESS'}
])

interface SprintOption {
  label: string;
  value: string;
  disabled?: boolean;
}

const sprintOptionals = ref<SprintOption[]>([]);
const sprintSelectLoading = ref<boolean>(false);
onMounted(async () => {
  sprintSelectLoading.value = true;
  const result = await getSprintOptionals();
  sprintSelectLoading.value = false;
  if (result.success) {
    sprintOptionals.value = result.data.map(p => ({label: p.name, value: p.id, disabled: false}))
  } else {
    ElMessage.error(result.msg);
  }
})


</script>