export default interface DevopsWorkIssueWrapper {
    success: boolean;
    suggest: string[];

    id: string;
    key: string;
    projectId: string;
    subject: string;
    workflowId: string;
    stateName: string;
    description: string;
    assigneeId: string;
    sprintId: string;
    versionInfoList: Version[];
    parentSubject: string;
    parentIssueLine: string;
    parentKey: string;
    authorName: string;
    assigneeName: string;
    priorityText: string;
    severityText: string;
    sprintName: string;
    categoryName: string;
    parent: boolean;
    children: DevopsWorkIssueWrapper[] | null;
    resolvedVersionName: string;
    projectIssueTypeName: string;
    projectName: string;
    laneId: string;
}


interface Version {
    id: string,
    name: string
}