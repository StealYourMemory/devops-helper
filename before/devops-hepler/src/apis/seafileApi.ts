import type {Result} from "@/@types/Result";
import api from "@/apis/axios";
import type FileTree from "@/@types/deploy/transfer/fileTree";
import type BuildAndTransferRequest from "@/@types/deploy/transfer/buildAndTransferRequest";

const prefix: string = '/seafile'

//获取目录结构
export async function getDirectory(path: string): Promise<Result<FileTree[]>> {
    const result = await api.get<Result<FileTree[]>>(`${prefix}/directory?path=${path}`);
    return result.data;
}

//获取文件详情
export async function getFile(path: string): Promise<Result<string>> {
    const result = await api.get<Result<string>>(`${prefix}/file?path=${path}`);
    return result.data;
}

//深度获取信息
export async function getDeep(path: string): Promise<Result<FileTree[]>> {
    const result = await api.get<Result<FileTree[]>>(`${prefix}/deep?path=${path}`);
    return result.data;
}

export async function buildAndTransfer(request: BuildAndTransferRequest): Promise<Result<string>> {
    const result = await api.post<Result<string>>(`${prefix}/transfer`, request)
    return result.data;
}

//归档
export async function archive(path: string): Promise<Result<any>> {
    const result = await api.post<Result<any>>(`${prefix}/archive?path=${path}`)
    return result.data;
}

//获取根目录
export async function getRootDirectory(): Promise<Result<string>> {
    const result = await api.get<Result<string>>(`${prefix}/root/directory`);
    return result.data;
}
