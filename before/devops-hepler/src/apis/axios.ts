import axios from 'axios';
import baseUrl from '@/config/config';

// 检查是否提供了baseUrl，如果没有提供，使用当前页面的URL构建
const trueBaseUrl = baseUrl || (function() {
    const protocol = window.location.protocol; // "http:" 或 "https:"
    const host = window.location.hostname;
    let port = window.location.port; // 端口可能为空，如果是标准端口（80, 443）

    // 如果使用标准端口，不添加端口号到URL
    if (protocol === 'http:' && port === '80') {
        port = '';
    } else if (protocol === 'https:' && port === '443') {
        port = '';
    }

    const portPart = port ? `:${port}` : '';
    return `${protocol}//${host}${portPart}`;
})();

// 创建axios实例
const api = axios.create({
    baseURL: trueBaseUrl,
});
api.interceptors.response.use(
    response => {
        return response;
    },
    error => {
        // 错误处理，返回FailResult格式
        let errMsg = 'Unknown Error';
        if (error.response) {
            errMsg = error.response.data?.msg || error.response.statusText;
        } else if (error.request) {
            errMsg = 'No response received';
        } else {
            errMsg = error.message;
        }

        //统一异常处理 这里使用resolve来处理异常情况 避免api调用的时候写try catch
        return Promise.resolve({
            data: {
                success: false,
                msg: `网络请求失败: ${errMsg}`
            }
        });
    }
)
export default api;
