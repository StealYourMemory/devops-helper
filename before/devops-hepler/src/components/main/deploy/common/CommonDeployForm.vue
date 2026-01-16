<template>
    <el-container>
        <el-tabs type="card" v-model="tabActiveName" @tab-click="handleTabClick">
            <el-tab-pane label="打包+部署" :name="BuildAndDeployName">
                <build-and-deploy></build-and-deploy>
            </el-tab-pane>
            <el-tab-pane label="仅打包" :name="BuildOnlyName">
                <build-only></build-only>
            </el-tab-pane>
            <el-tab-pane label="仅部署" :name="DeployOnlyName">
                <deploy-only></deploy-only>
            </el-tab-pane>
        </el-tabs>
    </el-container>
</template>

<script setup lang="ts">
import BuildAndDeploy from './BuildAndDeploy.vue';
import BuildOnly from './BuildOnly.vue';
import DeployOnly from './DeployOnly.vue';
import {
  BuildAndDeployName,
  BuildOnlyName,
  DeployOnlyName,
} from "@/utils/Constants";
import tabActiveName, {handleTabClick} from "@/components/main/deploy/common/ts/deployTab";


</script>