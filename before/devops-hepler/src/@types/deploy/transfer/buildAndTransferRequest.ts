export default interface BuildAndTransferRequest {
    projectName: string;
    branch: string;
    tag: string;
    updateInfo?:string;
}