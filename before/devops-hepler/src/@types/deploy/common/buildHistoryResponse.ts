export default interface BuildHistoryResponse {
    id: number;
    projectName:string;
    branch: string;
    tag: string;
    status:'Success' | 'Failed' |'Pending' | 'Aborted';
    tookTime: number;
    remainingTime: number;
    date: number;
}