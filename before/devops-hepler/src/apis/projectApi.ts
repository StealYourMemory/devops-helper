import api from './axios'
import type {Result} from '@/@types/Result'
import type ProjectInfoResponse from '@/@types/deploy/common/projectInfoResponse'
import type {ProjectBranchResponse} from '@/@types/deploy/common/projectBranchResponse';
import type BuildHistoryResponse from "@/@types/deploy/common/buildHistoryResponse";
import type K8sEnvAndServerInfo from "@/@types/deploy/common/k8sEnvAndServerInfo";

const prefix: string = '/project/meta'

//获取项目列表 用于下拉选择项目
export async function getProjectInfo(): Promise<Result<ProjectInfoResponse[]>> {
    const result = await api.get<Result<ProjectInfoResponse[]>>(`${prefix}/name`)
    return result.data;
}

//获取某项目下的分支信息
export async function getProjectBranch(projectName: string): Promise<Result<ProjectBranchResponse>> {
    const result = await api.get<Result<ProjectBranchResponse>>(`${prefix}/branch/${projectName}`)
    return result.data;
}

//获取环境信息
export async function getEnvironment(): Promise<Result<K8sEnvAndServerInfo[]>> {
    const result = await api.get<Result<K8sEnvAndServerInfo[]>>(`${prefix}/environment`)
    return result.data;
}

//获取某项目和分支下的tag信息
export async function getProjectTag(projectName: string, branch: string): Promise<Result<string>> {
    const result = await api.get<Result<string>>(`${prefix}/tag/${projectName}/${branch}`)
    return result.data;
}

//获取某项目下的打包历史
export async function getBuildHistory(projectName:string,search?:string): Promise<Result<BuildHistoryResponse[]>> {
    let url = `${prefix}/build-history/${projectName}`
    search = search?.trim();
    if(search){
        url += `?search=${search}`
    }
    const result = await api.get<Result<BuildHistoryResponse[]>>(url);
    return result.data;
}
export async function getBuildDetail(projectName:string,buildId:number) : Promise<Result<string>>{
    const result = await api.get<Result<string>>(`${prefix}/build-detail/${projectName}/${buildId}`)
    return result.data;
}