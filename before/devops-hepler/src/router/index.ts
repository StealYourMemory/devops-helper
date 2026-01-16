import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router'
import NotFound from "@/views/NotFound.vue";


const routes: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'Index',
        component: () => import('@/views/IndexPage.vue'),
        meta: {
            breadcrumb: '首页'
        },
        children: [
            {
                path: '',
                name: 'IndexPage',
                component: () => import('@/components/main/index/IndexPage.vue'),
                meta: {
                    breadcrumb: '首页'
                },
            },
            {
                path: 'deploy/common',
                name: 'CommonDeploy',
                component: () => import('@/components/main/deploy/common/CommonDeploy.vue'),
                meta: {
                    breadcrumb: '通用部署'
                },
            },
            {
                path: 'deploy/info',
                name: 'DeployInfo',
                component: () => import('@/components/main/deploy/info/DeployInfo.vue'),
                meta: {
                    breadcrumb: '部署详情'
                },
            },
            {
                path: 'deploy/transfer',
                name: 'DeveloperTransfer',
                component: () => import('@/components/main/deploy/transfer/DeveloperTransfer.vue'),
                meta: {
                    breadcrumb: '开发转测'
                },
            },
            {
                path: 'deploy/tester',
                name: 'TesterDeploy',
                component: () => import('@/components/main/deploy/tester/TesterDeploy.vue'),
                meta: {
                    breadcrumb: '测试部署'
                },
            },
            {
                path: 'transfer/requirement',
                name: 'TransferRequirement',
                component: () => import('@/components/main/transfer/DevopsRequirement.vue'),
                meta: {
                    breadcrumb: '需求审查'
                },
            },
            {
                path: 'transfer/bug',
                name: 'TransferBug',
                component: () => import('@/components/main/transfer/DevopsBug.vue'),
                meta: {
                    breadcrumb: 'Bug审查'
                },
            },
            {
                path: 'transfer/archive',
                name: 'TransferArchive',
                component: () => import('@/components/main/transfer/ArchivePage.vue'),
                meta: {
                    breadcrumb: '转测归档'
                },
            },
            {
                path: 'env',
                name: "EnvManage",
                component: () => import('@/components/main/env/EnvManage.vue'),
                meta: {
                    breadcrumb: '环境管理'
                },
            },
            {
                path: 'proxy',
                name: "ServerProxy",
                component: () => import('@/components/main/proxy/ProxyPage.vue'),
                meta: {
                    breadcrumb: '服务代理'
                },
            },
            {
                path: 'log',
                name: "InspectionLog",
                component: () => import('@/components/main/log/InspectionLog.vue'),
                meta: {
                    breadcrumb: '审计日志'
                },
            },
            {
                path: '/:pathMatch(.*)*',
                name: 'NotFound',
                component: NotFound
            }
        ]
    }
]


const router = createRouter({
    history: createWebHistory('/devops-helper/'),
    routes: routes
})

export default router
