import LocalStorageUtil from "@/utils/LocalStorage";
import {PROJECT_WEIGHT_MAP_KEY,PROJECT_WEIGHT_STEP_KEY} from "@/utils/Constants";
import {projectWeightStep} from "@/config/config";

interface ProjectWeight {
    project:string;
    weight:number;
}

interface ProjectOption {
    label: string;
    value: string;
    disabled?: boolean;
}

export function projectWeightUpdate(project:string){
    if(project != null && project.length > 0 ){
        const weightStep = Number(LocalStorageUtil.get(PROJECT_WEIGHT_STEP_KEY)) + projectWeightStep;
        LocalStorageUtil.set(PROJECT_WEIGHT_STEP_KEY,weightStep.toString());
        const map = getProjectWeightMap();
        const weight = map.get(project)?.weight || 0;
        map.set(project,{project:project,weight:weight+weightStep});
        const mapArray = Array.from(map, ([key, value]) => ([key, value]));
        LocalStorageUtil.set(PROJECT_WEIGHT_MAP_KEY,JSON.stringify(mapArray));
    }
}

export function projectWeightSort(projectList:ProjectOption[]){
    //排序 将排序结果返回
    const map = getProjectWeightMap();
    projectList.sort((p1,p2) =>{
        return (map.get(p2.value)?.weight || 0)-(map.get(p1.value)?.weight || 0 );
    });
}

function getProjectWeightMap():Map<string,ProjectWeight>{
    const json = LocalStorageUtil.get(PROJECT_WEIGHT_MAP_KEY);
    const map= new Map<string,ProjectWeight>();
    if(json != null){
        const parsed = JSON.parse(json);
        parsed.forEach(([key, value]: [string, ProjectWeight]) => {
            map.set(key, value);
        });
    }
    return map;
}