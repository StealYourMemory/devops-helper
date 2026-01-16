import type DevopsWorkIssueWrapper from "@/@types/transfer/devopsWorkIssueWrapper";

export default  function tableRowClassName({row}: { row: DevopsWorkIssueWrapper }) {
    if(!row.success){
        return 'warning-row'
    }else {
        return '';
    }
}