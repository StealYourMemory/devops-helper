// IP校验函数
import type {FormRules} from "element-plus";
import type {AddProxyRequest} from "@/@types/proxy/proxy";

const validateIP = (rule: any, value: any, callback: any) => {
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
const validatePort = (rule: any, value: any, callback: any) => {
    const port = Number(value);
    if (!value && value !== 0) {
        return callback(new Error('请输入端口'));
    } else if (isNaN(port) || port < 1 || port > 65535 || !Number.isInteger(port)) {
        return callback(new Error('端口号必须在1到65535之间'));
    } else {
        callback();
    }
};


const proxyAddFormCheck: FormRules<AddProxyRequest> = {
    envName: {
        required: true,
        message: "请选择环境",
        trigger: 'change',
    },
    proxyHost: {
        required: true,
        validator: validateIP,
        trigger: 'change',
    },
    srcHost: {
        required: true,
        validator: validateIP,
        trigger: 'change',
    },
    proxyPort: {
        required: true,
        validator: validatePort,
        trigger: 'change',
    },
    srcPort: {
        required: true,
        validator: validatePort,
        trigger: 'change',
    }
}
export {proxyAddFormCheck}
