import { defineStore } from 'pinia';
import type DeployRequest from "@/@types/deploy/common/deployRequest";

const useDeployProjectStore = defineStore('deployProjectInfo', {
    state: () => ({
        projectName: '',
        branch: '',
        environment: '',
        tag: ''
    }),
    actions: {
        setDeployProject(deployRequest:DeployRequest) {
            this.projectName = deployRequest.projectName;
            this.branch = deployRequest.branch || '';
            this.tag = deployRequest.tag;
            this.environment = deployRequest.environment || '';
        },
        clear(){
            this.projectName = '';
            this.branch = '';
            this.environment = '';
            this.tag = '';
        }
    }
});

export default useDeployProjectStore;
