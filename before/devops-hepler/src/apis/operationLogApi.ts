import type {Result} from "@/@types/Result";
import api from "@/apis/axios";
import type {LogFilterPage} from "@/@types/operationLog";

const prefix: string = '/operation-log'

//获取日志
export async function getLog(filter: LogFilterPage): Promise<Result<LogFilterPage>> {
    const result = await api.post<Result<LogFilterPage>>(`${prefix}/log`, filter);
    return result.data;
}