export default interface FileTree {
    type: string;
    id: string;
    name: string;
    time: string;
    permission: string;
    parentDirectory: string;
    starred: string;
    size: string | null;
    modifierEmail: string | null;
    modifierName: string | null;
    modifierContactEmail: string | null;
    isLeaf: boolean;
    children: FileTree[] | null;
}