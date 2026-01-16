export default interface ProjectDeployInfoResponse {
    id: number;
    envName: string;
    projectName: string;
    branch: string;
    tag: string;
    status: 'Success' | 'Failed' | 'Pending' | 'Aborted';
    tookTime: number;
    remainingTime: number;
    date: number;
}