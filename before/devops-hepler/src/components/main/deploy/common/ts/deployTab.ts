//记录和存储tabActiveName 保证刷新时也能回到之前的tab

import {ref, watch} from "vue";
import {
    BuildAndDeployName,
    DEPLOY_TAB_ACTIVE_KEY,
    type DeployTabActiveType
} from "@/utils/Constants";
import SessionStorageUtil from "@/utils/SessionStorage";
import type {TabsPaneContext} from "element-plus";

const tabActiveName = ref<DeployTabActiveType>(SessionStorageUtil.get(DEPLOY_TAB_ACTIVE_KEY)as DeployTabActiveType || BuildAndDeployName)

function handleTabClick(tab: TabsPaneContext) {
    SessionStorageUtil.set(DEPLOY_TAB_ACTIVE_KEY, tab.paneName as string);
    tabActiveName.value = tab.paneName as DeployTabActiveType;
}

type HandleTabChange =  (tabName: DeployTabActiveType) => void;

const handleArray:HandleTabChange[] = []

function registerTabWatch(handle:HandleTabChange){
    handleArray.push(handle);
}

watch(tabActiveName,()=>{
    handleArray.forEach(p => p(tabActiveName.value));
})
export default tabActiveName;
export {handleTabClick,registerTabWatch};