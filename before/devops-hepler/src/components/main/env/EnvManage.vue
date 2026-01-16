<template>
  <el-container>
    <el-header>
      <el-button type="primary" @click="addEnvFlag=true">
        添加新环境
      </el-button>
      <el-button icon="refresh" @click="getEnvRefresh" :loading="refreshLoading"></el-button>
    </el-header>
    <el-main>
      <el-table :data="envList" stripe max-height="600">
        <el-table-column type="expand">
          <template #default="scope">
            <div style="margin-left: 5%">
              <h3>服务器信息:</h3>
              <div v-for="(item,index) in scope.row.serverSet" :key="index">
                <el-descriptions :column="10">
                  <el-descriptions-item
                      label="IP"
                      width="180px"
                  >
                    {{ item.ip }}
                  </el-descriptions-item>
                  <el-descriptions-item
                      label="端口"
                      width="180px"
                  >
                    {{ item.port }}
                  </el-descriptions-item>
                  <el-descriptions-item
                      label="用户名"
                      width="180px"
                  >
                    {{ item.userName }}
                  </el-descriptions-item>
                  <el-descriptions-item
                      width="180px"
                      label="密码"
                  >
                    {{ decodeFromBase64(item.password) }}
                  </el-descriptions-item>
                  <el-descriptions-item>
                    <el-button type="danger"
                               text
                               size="small"
                               @click="handleDeleteServer(scope.row.envName,item.ip,item.port)">
                      删除
                    </el-button>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column type="index" label="#"></el-table-column>
        <el-table-column prop="envName" label="环境标识" align="center" header-align="center"></el-table-column>
        <el-table-column prop="envDescription" label="环境名称" align="center" header-align="center"></el-table-column>
        <el-table-column prop="envType" label="环境类型" align="center" header-align="center"></el-table-column>
        <el-table-column label="操作" align="center" header-align="center">
          <template #default="scope">
            <el-button text type="danger"
                       @click="handleDeleteEnv(scope.row.envName)">删除环境
            </el-button>
            |
            <el-button text type="primary" @click="()=>{addServerFlag = true;serverAddReq.envName = scope.row.envName}">
              添加服务
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-main>
  </el-container>

  <el-dialog v-model="addEnvFlag" title="添加环境" width="40%">
    <el-form>
      <el-container>
        <el-main>
          <el-form :model="envAddReq" label-width="100px" :rules="addEnvRules" ref="envAddRuleFormRef">
            <el-form-item label="环境标识" prop="envName">
              <el-input v-model="envAddReq.envName" style="width: 160px;"></el-input>
            </el-form-item>
            <el-form-item label="环境名称" prop="envDescription">
              <el-input v-model="envAddReq.envDescription" style="width: 160px;"></el-input>
            </el-form-item>
            <el-form-item label="环境类型" prop="envType">
              <el-radio-group v-model="envAddReq.envType">
                <el-tooltip content="Rebirth1.0，更新yaml就会自动拉取镜像，如新预发布环境">
                  <el-radio value="REBIRTH_V1">Rebirth1.0</el-radio>
                </el-tooltip>
                <el-tooltip content="需要先使用docker pull拉下来镜像，如城轨环境">
                  <el-radio value="NEED_DOCKER_PULL">NEED_DOCKER_PULL</el-radio>
                </el-tooltip>
                <el-tooltip
                    content="Rebirth2.0 需要先在一个环境拉取镜像并SAVE，再传到部署环境LOAD镜像，且部署环境使用的是nerdctl而非docker，如开发环境">
                  <el-radio value="REBIRTH_V2">Rebirth2.0</el-radio>
                </el-tooltip>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="服务器：">
              <div>
                <p v-for="(item,index) in envAddReq.serverSet" :key="index">
                  <span style="margin-right: 15px">IP：{{ item.ip }}</span>
                  <span style="margin-right: 15px">端口：{{ item.port }}</span>
                  <span style="margin-right: 15px">用户名：{{ item.userName }}</span>
                  <span style="margin-right: 15px">密码：{{ decodeFromBase64(item.password) }}</span>
                  <el-button type="danger" text @click="envAddReq.serverSet?.splice(index,1)">删除</el-button>
                </p>
              </div>
            </el-form-item>
            <el-button type="primary" style="margin-left: 5%" @click="tmpAddServerFlag = true">添加服务器</el-button>
            <el-form-item>
              <el-button type="primary" style="margin-left: 85%" @click="handleAddEnv(envAddRuleFormRef)">提交
              </el-button>
            </el-form-item>
          </el-form>
        </el-main>
      </el-container>
    </el-form>
  </el-dialog>

  <el-dialog v-model="addServerFlag" title="添加服务" width="20%" @close="ruleFormRef?.resetFields()">
    <el-container>
      <el-main>
        <el-form label-width="100px" :model="serverAddReq" :rules="rules" ref="ruleFormRef">
          <el-form-item label="IP：" prop="serverInfo.ip">
            <el-input style="width: 160px;" v-model="serverAddReq.serverInfo.ip" clearable></el-input>
          </el-form-item>
          <el-form-item label="端口：" prop="serverInfo.port">
            <el-input style="width: 160px;" v-model="serverAddReq.serverInfo.port" clearable></el-input>
          </el-form-item>
          <el-form-item label="用户名：" prop="serverInfo.userName">
            <el-input style="width: 160px;" v-model="serverAddReq.serverInfo.userName" clearable></el-input>
          </el-form-item>
          <el-form-item label="密码：" prop="serverInfo.password">
            <el-input style="width: 160px;" v-model="serverAddReq.serverInfo.password" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary"
                       @click="handleAddServer(ruleFormRef)"
                       style="margin-left: 50%">提交
            </el-button>
          </el-form-item>
        </el-form>
      </el-main>
    </el-container>
  </el-dialog>


  <el-dialog v-model="tmpAddServerFlag" title="添加服务" width="20%" @close="ruleFormRef?.resetFields()">
    <el-container>
      <el-main>
        <el-form label-width="100px" :model="serverAddReq" :rules="rules" ref="ruleFormRef">
          <el-form-item label="IP：" prop="serverInfo.ip">
            <el-input style="width: 160px;" v-model="serverAddReq.serverInfo.ip" clearable></el-input>
          </el-form-item>
          <el-form-item label="端口：" prop="serverInfo.port">
            <el-input style="width: 160px;" v-model="serverAddReq.serverInfo.port" clearable></el-input>
          </el-form-item>
          <el-form-item label="用户名：" prop="serverInfo.userName">
            <el-input style="width: 160px;" v-model="serverAddReq.serverInfo.userName" clearable></el-input>
          </el-form-item>
          <el-form-item label="密码：" prop="serverInfo.password">
            <el-input style="width: 160px;" v-model="serverAddReq.serverInfo.password" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary"
                       @click="tmpAddServer(ruleFormRef)"
                       style="margin-left: 50%">提交
            </el-button>
          </el-form-item>
        </el-form>
      </el-main>
    </el-container>
  </el-dialog>


</template>
<script setup lang="ts">
import {onMounted, ref} from "vue";
import {getEnvironment} from "@/apis/projectApi";
import {ElLoading, ElMessage, ElMessageBox, type FormInstance} from "element-plus";
import {decodeFromBase64, encodeToBase64} from "@/utils/base64";
import {addEnv, addServer, deleteEnv, deleteServer} from "@/apis/k8sEnv";
import {envAddFormCheck, serverAddFormCheck} from "@/components/main/env/addServerFormCheck";
// 导入默认接口
import type K8sEnvAndServerInfo from "@/@types/deploy/common/k8sEnvAndServerInfo";

import type {K8sServerInfo} from "@/@types/deploy/common/k8sEnvAndServerInfo";


// 导入具名接口
import type AddServerRequest from "@/@types/deploy/common/addServerRequest";
import sleep from "@/utils/sleeep";


const envList = ref<K8sEnvAndServerInfo[]>();

const addEnvFlag = ref<boolean>(false)
const addServerFlag = ref<boolean>(false)
const tmpAddServerFlag = ref<boolean>(false)

const envAddReq = ref<K8sEnvAndServerInfo>({
  envName: "",
  envDescription: "",
  envType: '',
  serverSet: []
});


const serverAddReq = ref<AddServerRequest>({
  envName: "",
  serverInfo: {
    ip: '',
    port: 22,
    userName: 'root',
    password: ''
  }
});
const rules = ref(serverAddFormCheck)

const addEnvRules = ref(envAddFormCheck)

const ruleFormRef = ref<FormInstance>()

const envAddRuleFormRef = ref<FormInstance>()


onMounted(getEnvList)

const refreshLoading = ref<boolean>(false);

async function getEnvRefresh() {
  refreshLoading.value = true
  await sleep(500);
  const result = await getEnvironment();
  refreshLoading.value = false
  if (result.success) {
    envList.value = result.data;
    ElMessage.success("刷新成功")
  } else {
    ElMessage.error(result.msg);
  }
}

async function getEnvList() {
  const result = await getEnvironment();
  if (result.success) {
    envList.value = result.data;
  } else {
    ElMessage.error(result.msg);
  }
}

async function handleAddEnv(formEl: FormInstance | undefined) {
  if (!formEl) return

  await formEl.validate(async (valid, fields) => {
    if (valid) {
      const loading = ElLoading.service({
        lock: true,
        text: 'Loading',
        background: 'rgba(0, 0, 0, 0.7)',
      })
      const result = await addEnv(envAddReq.value)
      loading.close();
      if (result.success) {
        await getEnvList();
        envAddRuleFormRef!.value!.resetFields()
        addEnvFlag.value = false;
        ElMessage.success("添加成功")
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}

async function tmpAddServer(formEl: FormInstance | undefined) {
  if (!formEl) return
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      const request: K8sServerInfo = {
        ip: serverAddReq.value.serverInfo.ip,
        port: serverAddReq.value.serverInfo.port,
        userName: serverAddReq.value.serverInfo.userName,
        password: encodeToBase64(serverAddReq.value.serverInfo.password),
      }
      envAddReq.value.serverSet?.push(request)
      ruleFormRef!.value!.resetFields()
      tmpAddServerFlag.value = false;
    } else {
      console.log('error submit!', fields)
    }
  })
}

async function handleAddServer(formEl: FormInstance | undefined) {
  if (!formEl) return

  await formEl.validate(async (valid, fields) => {
    if (valid) {
      //密码转base64再提交
      const request: AddServerRequest = {
        envName: serverAddReq.value.envName,
        serverInfo: {
          ip: serverAddReq.value.serverInfo.ip,
          port: serverAddReq.value.serverInfo.port,
          userName: serverAddReq.value.serverInfo.userName,
          password: encodeToBase64(serverAddReq.value.serverInfo.password),
        }
      }
      const loading = ElLoading.service({
        lock: true,
        text: 'Loading',
        background: 'rgba(0, 0, 0, 0.7)',
      })
      const result = await addServer(request)
      loading.close();
      if (result.success) {
        await getEnvList();
        ruleFormRef!.value!.resetFields()
        addServerFlag.value = false;
        ElMessage.success("添加成功")
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}


function handleDeleteEnv(envName: string) {
  ElMessageBox.confirm(
      '确认删除当前环境?',
      {
        confirmButtonText: '是',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    const result = await deleteEnv(envName)
    if (result.success) {
      await getEnvList();
      ElMessage.success("删除成功")
    } else {
      ElMessage.error(result.msg);
    }
  }).catch(() => {
    //do nothing
  })
}


function handleDeleteServer(envName: string, ip: string, port: number) {
  ElMessageBox.confirm(
      '确认删除当前服务器?',
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
    const result = await deleteServer({envName: envName, ip: ip, port: port});
    loading.close();
    if (result.success) {
      await getEnvList();
      ElMessage.success("删除成功")
    } else {
      ElMessage.error(result.msg);
    }
  }).catch(() => {
    //do nothing
  })
}
</script>