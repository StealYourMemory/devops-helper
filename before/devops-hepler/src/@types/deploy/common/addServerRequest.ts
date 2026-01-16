import type {K8sServerInfo} from "@/@types/deploy/common/k8sEnvAndServerInfo";

export default interface AddServerRequest {
    envName: string,
    serverInfo: K8sServerInfo
}