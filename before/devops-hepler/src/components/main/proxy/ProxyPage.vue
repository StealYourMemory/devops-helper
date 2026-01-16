<template>
  <el-header>
    <el-form v-model="filter" inline>
      <el-form-item label="源IP">
        <el-input v-model="filter.srcHost" clearable></el-input>
      </el-form-item>
      <el-form-item label="代理IP">
        <el-input v-model="filter.proxyHost" clearable></el-input>
      </el-form-item>
      <el-form-item label="创建时间">
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

      <el-form-item>
        <el-button :loading="refreshLoading" @click="getRecordRefresh">
          <el-icon>
            <refresh/>
          </el-icon>
        </el-button>
      </el-form-item>

      <el-form-item>
        <el-button type="danger" @click="addProxyFlag = true">
          <el-icon>
            <promotion/>
          </el-icon>
          <span>创建代理</span>
        </el-button>
      </el-form-item>
    </el-form>

  </el-header>
  <el-main style="margin-top: 2%">
    <el-table :data="pageInfo.records" stripe max-height="500">
      <el-table-column type="index" label="#" width="60px"></el-table-column>
      <el-table-column label="环境" align="center" header-align="center" width="120px">
        <template #default="scope">
          {{ envShow(scope.row.envName) }}
        </template>
      </el-table-column>
      <el-table-column prop="srcHost" label="源IP" align="center" header-align="center">
      </el-table-column>
      <el-table-column prop="srcPort" label="源端口" width="120" align="center" header-align="center">
      </el-table-column>
      <el-table-column prop="proxyHost" label="代理IP" align="center" header-align="center">
      </el-table-column>
      <el-table-column prop="proxyPort" label="代理端口" width="120" align="center" header-align="center">
      </el-table-column>
      <el-table-column prop="description" label="用途" align="center" header-align="center">
      </el-table-column>
      <el-table-column label="创建时间" align="center" header-align="center" >
        <template #default="scope">
          {{ scope.row.createTime == null ? "--" : timeShow(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="删除" align="center" header-align="center" width="200">
        <template #default="scope">
          <el-button icon="delete" type="danger" size="small" @click="handleDeleteProxy(scope.row.id)"></el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-main>
  <el-footer>
    <el-footer class="footer-right">
      <el-pagination v-model:current-page="pageInfo.page"
                     v-model:page-size="pageInfo.size"
                     :page-sizes="[10,20,50,100]"
                     layout="total,prev, pager, next,sizes"
                     :total="pageInfo.total"
                     @size-change="getProxyRecordByFilterAndPage"
                     @current-change="getProxyRecordByFilterAndPage"/>
    </el-footer>
  </el-footer>

  <el-dialog v-model="addProxyFlag" title="添加代理" width="20%" @close="ruleFormRef?.resetFields()">
    <el-container>
      <el-main>
        <el-tooltip>
          <template #content>
            使用k8s的service做代理，主要使用场景是k8s服务器可以访问通的服务，但本地办公网访问不通<br/>
            如我们在多出口联调的时候，创建的虚墙，k8s服务器可以连上，但本地无法连接，有时本地调试比部署到服务器方便些，就可以将这个虚墙代理出来<br/>
            源IP和源端口是为了要代理服务的IP和端口，如虚墙IP+830端口，代理服务器是某个环境的k8s服务器，代理端口是代理出的端口<br/>
            一旦创建代理成功，就可以使用<strong>代理服务器IP+代理端口</strong>来访问要代理的服务<br/>
            <strong>注：请保证代理端口在代理服务器上未被占用，如果代理不通，请确保代理服务器与源服务之间是通的</strong><br/>
            <strong>注：用完的代理要及时删除！！！避免端口长期占用</strong><br/>
          </template>
          <el-icon size="large">
            <question-filled></question-filled>
          </el-icon>
        </el-tooltip>
        <el-form label-width="120px" :model="addProxyReq" :rules="rules" ref="ruleFormRef">
          <el-form-item label="环境" prop="envName">
            <el-select
                v-model="addProxyReq.envName"
                filterable
                clearable
                placeholder="请选择环境"
                style="width: 200px">
              <el-option v-for="item in envOptions" :key="item.label" :value="item.value"
                         :label="item.label"
                         :disabled="item.disabled"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="源IP：" prop="srcHost">
            <el-input style="width: 200px;" v-model="addProxyReq.srcHost" clearable
                      placeholder="输入要代理的IP"></el-input>
          </el-form-item>
          <el-form-item label="源端口：" prop="srcPort">
            <el-input style="width: 200px;" v-model="addProxyReq.srcPort" clearable
                      placeholder="输入要代理的端口"></el-input>
          </el-form-item>
          <el-form-item label="代理服务器：" prop="proxyHost">
            <el-select
                v-model="addProxyReq.proxyHost"
                filterable
                clearable
                placeholder="请选择代理服务器"
                style="width: 200px">
              <el-option v-for="item in serverOptions" :key="item.label" :value="item.value"
                         :label="item.label"
                         :disabled="item.disabled"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="代理端口：" prop="proxyPort">
            <el-input style="width: 200px;" v-model="addProxyReq.proxyPort" clearable></el-input>
          </el-form-item>
          <el-form-item label="用途：" prop="description">
            <el-input style="width: 200px;" v-model="addProxyReq.description" clearable></el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary"
                       @click="handleProxyAdd(ruleFormRef)"
                       style="margin-left: 50%">提交
            </el-button>
          </el-form-item>
        </el-form>
      </el-main>
    </el-container>
  </el-dialog>
</template>
<script setup lang="ts">
import timeShow from "@/utils/time";
import {onMounted, ref, watch} from "vue";
import type Page from "@/@types/page";
import type {AddProxyRequest, K8sProxyRecord, ProxyRecordFilter} from "@/@types/proxy/proxy";
import {ElLoading, ElMessage, ElMessageBox, type FormInstance} from "element-plus";
import {addProxy, deleteProxy, getProxyRecord} from "@/apis/proxyApi";
import debounce from "@/utils/debounce";
import {Promotion, QuestionFilled, Refresh} from "@element-plus/icons-vue";
import {getEnvironment} from "@/apis/projectApi";
import type K8sEnvAndServerInfo from "@/@types/deploy/common/k8sEnvAndServerInfo";
import {proxyAddFormCheck} from "@/components/main/proxy/addProxyFormCheck";
import sleep from "@/utils/sleeep";


//环境信息
interface EnvOption {
  label: string;
  value: string;
  disabled?: boolean;
}


const refreshLoading = ref<boolean>(false);

const envOptions = ref<EnvOption[]>([]);
const environment = ref<K8sEnvAndServerInfo[]>([]);
onMounted(async () => {
  const result = await getEnvironment();
  if (result.success) {
    environment.value = result.data
    envOptions.value = result.data.map(p => (
        {
          label: p.envDescription,
          value: p.envName,
          disabled: p.serverSet == null || p.serverSet.length == 0
        }))
  } else {
    ElMessage.error(result.msg);
  }
})


const filter = ref<ProxyRecordFilter>({
  envName: "",
  proxyHost: "",
  srcHost: ""
});


const pageInfo = ref<Page<K8sProxyRecord>>({
  paging: true,
  page: 1,
  total: 0,
  size: 10
})

onMounted(() => {
  getProxyRecordByFilterAndPage()
})

async function getRecordRefresh() {
  refreshLoading.value = true
  await sleep(500);
  await getProxyRecordByFilterAndPage();
  refreshLoading.value = false
  ElMessage.success("刷新成功")
}

const debouncedGetProxyRecordByFilterAndPage = debounce(getProxyRecordByFilterAndPage, 300);

watch(filter, debouncedGetProxyRecordByFilterAndPage, {deep: true});


async function getProxyRecordByFilterAndPage() {
  const result = await getProxyRecord({...filter.value, ...pageInfo.value});
  if (result.success) {
    const {paging, page, size, total, records} = result.data as Page<K8sProxyRecord>;
    pageInfo.value = {paging, page, size, total, records};
  } else {
    ElMessage.error(result.msg);
  }
}

function envShow(envName: string) {
  const env = environment.value.find(p => p.envName === envName)
  return env!.envDescription;
}

function handleDeleteProxy(id: number) {
  ElMessageBox.confirm(
      '确认删除当前代理?',
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
    const result = await deleteProxy(id)
    loading.close()
    if (result.success) {
      await getProxyRecordByFilterAndPage();
      ElMessage.success("删除成功")
    } else {
      ElMessage.error(result.msg);
    }
  }).catch(() => {
    //do nothing
  })
}


const addProxyFlag = ref<boolean>(false)

const addProxyReq = ref<AddProxyRequest>({
  envName: "",
  srcHost: "",
  proxyHost: ""
})
const ruleFormRef = ref<FormInstance>()


const rules = ref(proxyAddFormCheck)

async function handleProxyAdd(formEl: FormInstance | undefined) {
  if (!formEl) return

  await formEl.validate(async (valid, fields) => {
    if (valid) {
      //密码转base64再提交
      const loading = ElLoading.service({
        lock: true,
        text: 'Loading',
        background: 'rgba(0, 0, 0, 0.7)',
      })
      const result = await addProxy(addProxyReq.value)
      loading.close();
      if (result.success) {
        await getProxyRecordByFilterAndPage();
        ruleFormRef!.value!.resetFields()
        addProxyFlag.value = false;
        ElMessage.success("添加成功")
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}


const serverOptions = ref<EnvOption[]>([]);
watch(() => addProxyReq.value.envName, () => {
  if (addProxyReq.value.envName) {
    const env = environment.value.find(p => p.envName === addProxyReq.value.envName);
    if (env != null) {
      serverOptions.value = env.serverSet!.map(p => (
          {
            label: p.ip,
            value: p.ip,
          }
      ))
    }
    addProxyReq.value.proxyHost = "";
  }
})

</script>

<style scoped>
.footer-right {
  display: flex;
  justify-content: flex-end;
  padding-right: 20px; /* 可根据需要调整 */
}
</style>