import type DeployRequest from "@/@types/deploy/common/deployRequest";
import type {Result} from "@/@types/Result";
import api from "@/apis/axios";
import type BuildAndDeployProgressResponse from "@/@types/deploy/common/buildAndDeployProgressResponse";
import type BuildHistoryResponse from "@/@types/deploy/common/buildHistoryResponse";
import type ProjectDeployInfoResponse from "@/@types/deploy/common/projectDeployInfoResponse";

const prefix: string = '/common-deploy'

export async function buildAndDeploy(deployRequest: DeployRequest): Promise<Result<string>> {
    const result = await api.post<Result<string>>(`${prefix}/build-deploy`, deployRequest)
    return result.data;
}

export async function buildOnly(deployRequest: DeployRequest): Promise<Result<string>> {
    const result = await api.post<Result<string>>(`${prefix}/build-only`, deployRequest)
    return result.data;
}


export async function deployOnly(deployRequest: DeployRequest): Promise<Result<string>> {
    const result = await api.post<Result<string>>(`${prefix}/deploy-only`, deployRequest)
    return result.data;
}

export async function deployDetail(envName: string, projectName: string): Promise<Result<BuildHistoryResponse>> {
    const result = await api.get<Result<BuildHistoryResponse>>(`${prefix}/detail/${envName}/${projectName}`)
    return result.data;
}

export async function deployDetailList(envName: string, projectName: string): Promise<Result<ProjectDeployInfoResponse[]>> {
    let url = `${prefix}/detail/list`
    if (envName && projectName) {
        url += `?envName=${envName}&projectName=${projectName}`
    } else if (envName) {
        url += `?envName=${envName}`;
    } else if (projectName) {
        url += `?projectName=${projectName}`;
    }

    console.log("yhs-test:url" + url);
    const result = await api.get<Result<ProjectDeployInfoResponse[]>>(url)
    return result.data;
}

export async function getBuildAndDeployProgress(requestId: string): Promise<Result<BuildAndDeployProgressResponse | null>> {
    const result = await api.get<Result<BuildAndDeployProgressResponse | null>>(`${prefix}/progress/${requestId}`)
    return result.data;
}