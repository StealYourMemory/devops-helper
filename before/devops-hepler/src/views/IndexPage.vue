<template>
    <div>
        <el-container>
            <el-aside width="200px" class="custom-aside">
                <sidebar-menu></sidebar-menu>
            </el-aside>
            <el-container>
                <el-header style="margin-top: 2%">
                  <page-header></page-header>
                </el-header>
                <el-main>
                  <!-- 这里展示的是路由结果，由路由来动态的展示 -->
                  <el-row :justify="'center'" style="margin-top: 10px">
                    <el-col :span="22">
                      <router-view></router-view>
                    </el-col>
                  </el-row>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>
<script setup lang="ts">
import SidebarMenu from '@/components/SidebarMenu.vue';
import PageHeader from "@/views/PageHeader.vue";
</script>

<style scoped>
/*
.custom-aside {
  background-color: #F5F9FA;
  height: 100vh;
  border-right: none;
}

.el-main{
  height: 100%;
  overflow: auto;
  background-color: #F2F4F3;
}
.el-header{
  background-color: #F5F9FA;
}
*/
</style>