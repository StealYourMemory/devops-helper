import type {Result} from "@/@types/Result";
import api from "@/apis/axios";
import type K8sEnvAndServerInfo from "@/@types/deploy/common/k8sEnvAndServerInfo";
import type AddServerRequest from "@/@types/deploy/common/addServerRequest";
import type DeleteServerRequest from "@/@types/deploy/common/deleteServerRequest";

const prefix: string = '/k8s'

export async function addEnv(request: K8sEnvAndServerInfo): Promise<Result<any>> {
    const result = await api.post<Result<any>>(`${prefix}/env`, request)
    return result.data;
}

export async function addServer(request: AddServerRequest): Promise<Result<any>> {
    const result = await api.post<Result<any>>(`${prefix}/server`, request)
    return result.data;
}

export async function deleteEnv(envName: string): Promise<Result<any>> {
    const result = await api.delete<Result<any>>(`${prefix}/env/${envName}`)
    return result.data;
}

export async function deleteServer(request: DeleteServerRequest): Promise<Result<any>> {
    const result = await api.delete<Result<any>>(`${prefix}/server`, {data: request})
    return result.data;
}