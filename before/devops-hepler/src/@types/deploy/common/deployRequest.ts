export default interface DeployRequest {
    projectName: string,
    branch?: string,
    environment?: string,
    tag: string
}