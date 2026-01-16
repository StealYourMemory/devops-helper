import type {FormRules} from "element-plus";
import type AddServerRequest from "@/@types/deploy/common/addServerRequest";
import type K8sEnvAndServerInfo from "@/@types/deploy/common/k8sEnvAndServerInfo";



// IP校验函数
const validateIP = (rule:any, value:any, callback:any) => {
    const ipv4Regex = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
    if (!value) {
        return callback(new Error('请输入ip'));
    } else if (!ipv4Regex.test(value)) {
        return callback(new Error('请输入正确的IPv4地址'));
    } else {
        callback();
    }
};

// 端口校验函数
const validatePort = (rule:any, value:any, callback:any) => {
    const port = Number(value);
    if (!value && value !== 0) {
        return callback(new Error('请输入端口'));
    } else if (isNaN(port) || port < 1 || port > 65535 || !Number.isInteger(port)) {
        return callback(new Error('端口号必须在1到65535之间'));
    } else {
        callback();
    }
};

const serverAddFormCheck:FormRules<AddServerRequest> = {
    "serverInfo.ip":{
        required: true,
        validator:validateIP,
        trigger: 'change',
    },
    "serverInfo.port":{
        required: true,
        validator:validatePort,
        trigger: 'change',
    },
    "serverInfo.userName":{
        required: true,
        message: '请输入用户名',
        trigger: 'change',
    },
    "serverInfo.password":{
        required: true,
        message: '请输入密码',
        trigger: 'change',
    }
}


const envAddFormCheck:FormRules<K8sEnvAndServerInfo> = {
    envName:{
        required: true,
        message: '请输入环境标志',
        trigger: 'change',
    },
    envDescription:{
        required: true,
        message: '请输入环境名称',
        trigger: 'change',
    },
    envType:{
        required: true,
        message: '请选择环境类型',
        trigger: 'change',
    }
}


export { serverAddFormCheck ,envAddFormCheck}