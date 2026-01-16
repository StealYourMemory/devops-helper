<template>
  <div>
    <el-tree :props="props"
             :load="loadNode"
             lazy>
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
        <div style="margin-left: auto;margin-right: 10%;display: flex; ">
          <div v-if="data.isLeaf" style="margin-right: 60px;">
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
  </div>

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
</template>
<script setup lang="ts">
import {onMounted, ref} from "vue";
import {getDirectory, getFile, getRootDirectory} from "@/apis/seafileApi";
import {ElLoading, ElMessage} from "element-plus";
import type Node from 'element-plus/es/components/tree/src/model/node'
import {Clock, Folder, FolderOpened} from "@element-plus/icons-vue";
import YamlIcon from "@/assets/icons/YamlIcon.vue";
import SqlIcon from "@/assets/icons/SqlIcon.vue";
import ShellIcon from "@/assets/icons/ShellIcon.vue";
import TxtIcon from "@/assets/icons/TxtIcon.vue";
import SeafileFileContent from "@/components/main/deploy/SeafileFileContent.vue";
import type FileTree from "@/@types/deploy/transfer/fileTree";
import UnknownFileIcon from "@/assets/icons/UnknownFileIcon.vue";
import timeShow from "../../../utils/time";
import formatBytes from "../../../utils/byteFormatter";

const props = {
  label: 'name',
  children: 'children',
  isLeaf: 'isLeaf'
}

const baseDirectory = ref<FileTree[] | null>(null);

const iconSize = 28;


let BASE_SEAFILE_DIRECTORY = "";


onMounted(async () => {
  const rootDirResult = await getRootDirectory()
  if (rootDirResult.success) {
    BASE_SEAFILE_DIRECTORY = rootDirResult.data;
  } else {
    ElMessage.error(rootDirResult.msg);
  }
  const result = await getDirectory(BASE_SEAFILE_DIRECTORY);
  if (result.success) {
    baseDirectory.value = result.data;
  } else {
    ElMessage.error(result.msg);
  }
})


const loadNode = async (node: Node, resolve: (data: FileTree[]) => void) => {
  const rootDirResult = await getRootDirectory()
  if (rootDirResult.success) {
    BASE_SEAFILE_DIRECTORY = rootDirResult.data;
  } else {
    ElMessage.error(rootDirResult.msg);
  }
  getDirectory(getPath(node))
      .then(result => {
        if (result.success && result.data) {
          resolve(result.data);
        }
      });
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
    return BASE_SEAFILE_DIRECTORY + result;
  }
}

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
</script>

<style>
.tree-node {
  position: relative; /* 设置相对定位，作为绝对定位子元素的参考 */
  padding-right: 120px; /* 确保有足够空间放置时间显示 */
}

.time-display {
  position: absolute; /* 绝对定位 */
  right: 100px; /* 距离父元素的右边缘100px */
  top: 0; /* 顶部对齐 */
}
</style>