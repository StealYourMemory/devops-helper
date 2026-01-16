import type {Result} from "@/@types/Result";
import api from "@/apis/axios";
import type DevopsSprintOptionResponse from "@/@types/transfer/devopsSprintOptionResponse";
import type DevopsWorkIssueWrapper from "@/@types/transfer/devopsWorkIssueWrapper";

const prefix: string = '/devops'

//获取devops sprints
export async function getSprintOptionals(): Promise<Result<DevopsSprintOptionResponse[]>> {
    const result = await api.get<Result<DevopsSprintOptionResponse[]>>(`${prefix}/sprints-optional`)
    return result.data;
}


export async function getRequestList(sprint:string): Promise<Result<DevopsWorkIssueWrapper[]>> {
    const result = await api.get<Result<DevopsWorkIssueWrapper[]>>(`${prefix}/request-list?sprint=${sprint}`)
    return result.data;
}

export async function getBugList(sprint:string): Promise<Result<DevopsWorkIssueWrapper[]>> {
    const result = await api.get<Result<DevopsWorkIssueWrapper[]>>(`${prefix}/bug-list?sprint=${sprint}`)
    return result.data;
}

export async function sendMail(sendRequest:DevopsWorkIssueWrapper[]){
    const result = await api.post<Result<any>>(`${prefix}/send-mail`, sendRequest)
    return result.data;
}