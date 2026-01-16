import type Page from "@/@types/page";


export interface K8sProxyRecord{
    id:number;
    envName: string;
    proxyHost:string;
    proxyPort:number;
    srcHost:string;
    srcPort:number;
    description:string;
    createTime:number;
}

export interface ProxyRecordFilter{
    envName?:string;
    proxyHost?:string;
    srcHost?:string;
    startTime?:number;
    endTime?:number;
}

export interface AddProxyRequest{
    envName:string;
    srcHost:string;
    srcPort?:number;
    proxyHost:string;
    proxyPort?:number;
    description?: string;
}

export type ProxyRecordFilterPage = ProxyRecordFilter | Page<K8sProxyRecord>;