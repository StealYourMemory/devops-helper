import type Page from "@/@types/page";

export interface OperationLog{
    id:number;
    user:string;
    requestId:string;
    operation:string;
    requestDetail:string;
    success?:boolean;
    resultDetail?:string;
    startTime:number;
    endTime?:number;
}

export interface LogFilter {
    user?:string;
    operation?:string
    success?:boolean;
    startTime?:number;
    endTime?:number;
}

export type LogFilterPage = LogFilter | Page<OperationLog>;
