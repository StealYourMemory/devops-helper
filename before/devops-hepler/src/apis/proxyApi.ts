import type {Result} from "@/@types/Result";
import api from "@/apis/axios";
import type {AddProxyRequest, K8sProxyRecord, ProxyRecordFilterPage} from "@/@types/proxy/proxy";

const prefix: string = '/k8s'

export async function getProxyRecord(filter: ProxyRecordFilterPage): Promise<Result<ProxyRecordFilterPage>> {
    const result = await api.post<Result<ProxyRecordFilterPage>>(`${prefix}/proxy/query`, filter);
    return result.data;
}


export async function addProxy(request: AddProxyRequest): Promise<Result<K8sProxyRecord>> {
    const result = await api.post<Result<K8sProxyRecord>>(`${prefix}/proxy`, request);
    return result.data;
}

export async function deleteProxy(id: number): Promise<Result<K8sProxyRecord>> {
    const result = await api.delete<Result<K8sProxyRecord>>(`${prefix}/proxy/${id}`);
    return result.data;
}