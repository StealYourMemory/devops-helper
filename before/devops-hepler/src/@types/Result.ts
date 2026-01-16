interface SuccessResult<T> {
    success: true;
    data: T;
}

interface FailResult {
    success: false;
    msg: string;
}

type Result<T> = SuccessResult<T> | FailResult;

export type { SuccessResult, FailResult, Result };