<template>
  <div>
    <el-container>
      <el-header>
        <el-form inline>
          <el-form-item label="迭代版本">
            <el-select v-model="selectedVersion"
                       clearable
                       placeholder="请选择迭代版本"
                       style="width: 200px;">
              <el-option v-for="item in versionOptionalList" :key="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-space size="large">
              <el-button type="warning" @click="doArchive">
                <el-icon>
                  <files></files>
                </el-icon>
                <span>生成归档</span>
              </el-button>
              <el-tooltip placement="bottom" effect="light">
                <template #content>
                  会归档本次迭代的<strong>yaml</strong>、<strong>sql</strong>、<strong>shell</strong>和<strong>合入需求</strong><br/>
                  归档内容会放入final-dh目录，如果不存在会新建，<strong>如果存在final-dh会删除之前的内容，所以如果原final-dh存在重要文件，请先备份！！！</strong><br/>
                  合入需求查的是devops的需求列表，需要seafile迭代目录包含yyyy年MM，devops的迭代目录包含yy年MM或者yyyy年MM<br/>
                </template>
                <el-icon color="#E6A23C">
                  <warning/>
                </el-icon>
              </el-tooltip>
            </el-space>
          </el-form-item>
          <el-form-item>
            <el-button icon="refresh" @click="getDeepFileTree"></el-button>
          </el-form-item>
        </el-form>
      </el-header>
      <el-main>
        <el-tree :props="props"
                 :data="deepFileTree">
          <template #default="{node,data}">
            <div>
              <el-icon v-if="!node.expanded && !data.isLeaf" :size="20">
                <folder/>
              </el-icon>
              <el-icon v-if="node.expanded && !data.isLeaf" :size="20">
                <folder-opened/>
              </el-icon>
            </div>
            <div v-if="data.isLeaf">
              <yaml-icon v-if="data.name.endsWith('.yaml') || data.name.endsWith('.yml')"
                         :width="iconSize"
                         :height="iconSize"></yaml-icon>
              <sql-icon v-else-if="data.name.endsWith('.sql')" :width="iconSize"
                        :height="iconSize"></sql-icon>
              <shell-icon v-else-if="data.name.endsWith('.sh')" :width="iconSize"
                          :height="iconSize"></shell-icon>
              <txt-icon v-else-if="data.name.endsWith('.txt')" :width="iconSize"
                        :height="iconSize"></txt-icon>
              <unknown-file-icon v-else :width="iconSize" :height="iconSize"></unknown-file-icon>
            </div>
            <span style="font-size: medium;margin-left: 20px" v-if="!data.isLeaf">{{ data.name }}</span>
            <el-button text v-if="data.isLeaf" size="small" @click="getFileContent(node,data)">
              <span style="font-size: medium">{{ data.name }}</span>
            </el-button>

            <div style="margin-left: auto;margin-right: 20%;display: flex; ">
              <div v-if="data.isLeaf" style="margin-right: 200px;">
                <span style="font-size: small;">{{ formatBytes(data.size) }}</span>
              </div>
              <div>
                <el-icon>
                  <clock/>
                </el-icon>
                <span style="font-size: small;">{{ (timeShow(data.time * 1000)) }}</span>
              </div>
            </div>

          </template>
        </el-tree>
      </el-main>
    </el-container>

    <el-dialog v-model="detailDialogShow"
               center
               draggable
               destroy-on-close
               width="80%">
      <seafile-file-content
          :content="fileContent"
          :file-type="fileType">
      </seafile-file-content>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">

import {onMounted, ref, watch} from "vue";
import {getDeep, getDirectory, getFile, archive, getRootDirectory} from "@/apis/seafileApi";
import {ElLoading, ElMessage, ElMessageBox} from "element-plus";
import type FileTree from "@/@types/deploy/transfer/fileTree";
import TxtIcon from "@/assets/icons/TxtIcon.vue";
import UnknownFileIcon from "@/assets/icons/UnknownFileIcon.vue";
import {Clock, Files, Folder, FolderOpened, Warning} from "@element-plus/icons-vue";
import ShellIcon from "@/assets/icons/ShellIcon.vue";
import YamlIcon from "@/assets/icons/YamlIcon.vue";
import SqlIcon from "@/assets/icons/SqlIcon.vue";
import type Node from "element-plus/es/components/tree/src/model/node";
import SeafileFileContent from "@/components/main/deploy/SeafileFileContent.vue";
import formatBytes from "@/utils/byteFormatter";
import timeShow from "@/utils/time";


const selectedVersion = ref<string>('')
const versionOptionalList = ref<string[]>([]);

const deepFileTree = ref<FileTree[]>([]);
const iconSize = 28;
const props = {
  label: 'name',
  children: 'children',
  isLeaf: 'isLeaf'
}

let BASE_SEAFILE_DIRECTORY = "";
//下拉列表和文件树
onMounted(async () => {
  const rootDirResult = await getRootDirectory()
  if (rootDirResult.success) {
    BASE_SEAFILE_DIRECTORY = rootDirResult.data;
  } else {
    ElMessage.error(rootDirResult.msg);
  }

  const result = await getDirectory(BASE_SEAFILE_DIRECTORY);
  if (result.success) {
    versionOptionalList.value = result.data.map(item => item.name)
  } else {
    ElMessage.error(result.msg);
  }
})

watch(selectedVersion, getDeepFileTree)

async function getDeepFileTree() {
  //不能为空
  if (!selectedVersion.value) {
    deepFileTree.value = [];
  } else {
    const loading = ElLoading.service({
      lock: true,
      text: 'Loading',
      background: 'rgba(0, 0, 0, 0.7)',
    })
    const result = await getDeep(BASE_SEAFILE_DIRECTORY + "/" + selectedVersion.value);
    loading.close();
    if (result.success) {
      deepFileTree.value = result.data
    } else {
      ElMessage.error(result.msg);
    }
  }
}


//查看文件详情
const detailDialogShow = ref<boolean>(false);
const fileContent = ref<string>("");
const fileType = ref<string>("");

async function getFileContent(node: Node, data: FileTree) {
  if (data.size != null && (Number)(data.size) > 10 * 1024 * 1024) {
    ElMessage.warning("文件大于10MB，可能不是文本文件，不能查看");
    return;
  }
  const loading = ElLoading.service({
    lock: true,
    text: 'Loading',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  const result = await getFile(getPath(node));
  loading.close();
  if (result.success) {
    fileType.value = node.label.split(".")[1]
    fileContent.value = result.data;
    detailDialogShow.value = true;
  } else {
    ElMessage.error(result.msg);
  }
}

function getPath(node: Node): string {
  if (node.level === 0) {
    return BASE_SEAFILE_DIRECTORY;
  } else {
    const pathArray: string[] = [];
    while (node && node.label) {
      pathArray.unshift(node.label);
      node = node.parent;
    }
    let result = "";
    pathArray.forEach((item) => {
      result += `/${item}`
    })
    return BASE_SEAFILE_DIRECTORY + "/" + selectedVersion.value + result;
  }
}

//归档
async function doArchive() {
  if (!selectedVersion.value) {
    ElMessage.warning("请先选择要归档的版本");
    return;
  }

  ElMessageBox.confirm(
      '原final-dh目录下的所有内容会删除，确认归档?',
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
    const result = await archive(BASE_SEAFILE_DIRECTORY + "/" + selectedVersion.value);
    loading.close();
    if (result.success) {
      ElMessage.success("归档成功")
    } else {
      ElMessage.error("归档失败:" + result.msg);
    }
  })

}

</script>