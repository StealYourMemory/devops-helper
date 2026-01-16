import type DevopsWorkIssueWrapper from "@/@types/transfer/devopsWorkIssueWrapper";
import {ElMessageBox} from "element-plus";

export default function deleteSuggest(item: DevopsWorkIssueWrapper, index: number) {
    ElMessageBox.confirm(
        '确认当前审查没有问题?',
        {
            confirmButtonText: '是',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(() => {
        item.suggest.splice(index, 1);
        if (item.suggest == null || item.suggest.length === 0) {
            item.success = true;
        }
    }).catch(() => {
        //do nothing
    })
}