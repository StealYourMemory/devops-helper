import type {Result} from "@/@types/Result";
import type BuildHistoryResponse from "@/@types/deploy/common/buildHistoryResponse";

export default interface BuildAndDeployProgressResponse{
    requestId:string;
    buildProgress: Result<BuildHistoryResponse> | null;
    deployProgress: string[] | null;
}