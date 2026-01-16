package com.coderzoe.common.log;

import com.coderzoe.common.Result;
import com.coderzoe.common.exception.CommonException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author yinhuasheng
 * @date 2024/8/19 11:42
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("@annotation(com.coderzoe.common.log.Log)")
    public void logPoint() {
    }

    /**
     * 后置记录日志 即函数执行完成记录日志
     *
     * @param joinPoint 切入点
     * @param result    返回结果
     */
    @AfterReturning(pointcut = "logPoint()", returning = "result")
    public void after(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null, result);
    }

    /**
     * 异常抛出的日志记录
     *
     * @param joinPoint 切入点
     * @param exception 异常
     */
    @AfterThrowing(pointcut = "logPoint()", throwing = "exception")
    public void throwing(JoinPoint joinPoint, Exception exception) {
        handleLog(joinPoint, exception, null);
    }


    /**
     * 实际进行日志处理的类
     *
     * @param joinPoint 日志记录
     * @param exception 异常
     * @param result    结果
     */
    @SuppressWarnings("rawtypes")
    private void handleLog(JoinPoint joinPoint, Exception exception, Object result) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Log log = methodSignature.getMethod().getAnnotation(Log.class);
        String operationMethod = methodSignature.getDeclaringTypeName() + "#" + methodSignature.getMethod().getName();
        StringBuilder operationDetail = new StringBuilder();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            operationDetail.append("参数").append(i + 1).append(": ").append(args[i]).append(" ===");
        }
        //后置切入 函数执行成功
        LogInfo logInfo;
        if (exception == null) {
            if (result instanceof Result && !((Result) result).isSuccess()) {
                logInfo = LogInfo.error(log.title(), operationMethod,
                        operationDetail.toString(), ((Result<?>) result).getMsg(), LogLevel.ERROR);
            } else {
                logInfo = LogInfo.success(log.title(), operationMethod,
                        operationDetail.toString(), result.toString(), log.level());
            }
        } else { //异常切入 函数执行失败
            logInfo = LogInfo.error(log.title(), operationMethod,
                    operationDetail.toString(), exception.getMessage(), LogLevel.ERROR);
        }
        save(logInfo);
    }


    private void save(LogInfo log) {
        switch (log.getLogLevel()) {
            case INFO:
                logger.info(log.toString());
                break;
            case DEBUG:
                logger.debug(log.toString());
                break;
            case WARN:
                logger.warn(log.toString());
                break;
            case ERROR:
                logger.error(log.toString());
                break;
            default:
                throw new CommonException(log.getLogLevel().toString());
        }
    }
}
