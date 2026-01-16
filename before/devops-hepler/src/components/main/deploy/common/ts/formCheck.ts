import type DeployRequest from "@/@types/deploy/common/deployRequest";
import type { FormRules } from "element-plus";


const packageAndDeployRules: FormRules<DeployRequest> = {
    projectName: {
        required: true,
        message: '请选择项目名',
        trigger: 'change'
    },
    branch: {
        required: true,
        message: '请选择分支',
        trigger: 'change'
    },
    environment: {
        required: true,
        message: '请选择部署环境',
        trigger: 'change'
    },
    tag: {
        required: true,
        message: '请输入打包tag',
        trigger: 'change'
    },
}


const packageOnlyRules: FormRules<DeployRequest> = {
    projectName: {
        required: true,
        message: '请选择项目名',
        trigger: 'change'
    },
    branch: {
        required: true,
        message: '请选择分支',
        trigger: 'change'
    },
    tag: {
        required: true,
        message: '请输入打包tag',
        trigger: 'change'
    },
}

const deployOnlyRules: FormRules<DeployRequest> = {
    projectName: {
        required: true,
        message: '请选择项目名',
        trigger: 'change'
    },
    environment: {
        required: true,
        message: '请选择部署环境',
        trigger: 'change'
    },
    tag: {
        required: true,
        message: '请输入打包tag',
        trigger: 'change'
    },
}

export { packageAndDeployRules ,packageOnlyRules,deployOnlyRules};