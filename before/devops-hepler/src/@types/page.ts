export default interface Page<T>{
    paging?:boolean;
    page?: number;
    size?: number;
    total?:number;
    sortBy?:string;
    order?:string;
    records?: T[];
}