export default interface K8sEnvAndServerInfo {
    envName: string,
    envDescription: string,
    envType: string,
    serverSet?: K8sServerInfo[]

}

export interface K8sServerInfo {
    ip: string,
    port: number,
    userName: string,
    password: string
}