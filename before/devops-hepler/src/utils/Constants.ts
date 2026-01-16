//常量表


const DEPLOY_TAB_ACTIVE_KEY = "DEPLOY_TAB_ACTIVE_KEY"
const BUILD_AND_DEPLOY_REQUEST_KEY = "BUILD_AND_DEPLOY_REQUEST_KEY";
const BUILD_ONLY_REQUEST_KEY = "BUILD_ONLY_REQUEST_KEY";
const DEPLOY_ONLY_REQUEST_KEY = "DEPLOY_ONLY_REQUEST_KEY";
const BUILD_AND_TRANSFER_KEY = "BUILD_AND_TRANSFER_KEY";


const BuildAndDeployName = "BuildAndDeploy";
const BuildOnlyName = "BuildOnly";
const DeployOnlyName = "DeployOnly";

const PROJECT_WEIGHT_MAP_KEY = "PROJECT_WEIGHT_MAP_KEY";
const PROJECT_WEIGHT_STEP_KEY = "PROJECT_WEIGHT_NUMBER_KEY";


type DeployTabActiveType = typeof BuildAndDeployName | typeof BuildOnlyName | typeof DeployOnlyName;
export {
    DEPLOY_TAB_ACTIVE_KEY,
    BuildAndDeployName,
    BuildOnlyName,
    DeployOnlyName,
    BUILD_AND_DEPLOY_REQUEST_KEY,
    BUILD_ONLY_REQUEST_KEY,
    DEPLOY_ONLY_REQUEST_KEY,
    PROJECT_WEIGHT_MAP_KEY,
    PROJECT_WEIGHT_STEP_KEY,
    BUILD_AND_TRANSFER_KEY,
    type DeployTabActiveType
};

