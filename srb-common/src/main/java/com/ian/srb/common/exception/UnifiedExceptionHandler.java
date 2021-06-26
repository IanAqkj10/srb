package com.ian.srb.common.exception;

import com.ian.srb.common.result.Rs;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author:IanJ
 * @date:2021/6/14 16:54
 */

//@Slf4j
@RestControllerAdvice
@Component
public class UnifiedExceptionHandler {



    /**
     * 未定义异常
     */
    @ExceptionHandler(value = Exception.class) //当controller中抛出Exception，则捕获
    public Rs handleException(Exception e) {
        //log.error(e.getMessage(), e);
        return Rs.error();
    }

    @ExceptionHandler(value = ArithmeticException.class) //当controller中抛出Exception，则捕获
    public Rs handleException(ArithmeticException e) {
//        log.error(e.getMessage(), e);
        return Rs.error().message(e.getMessage());
    }

    @ExceptionHandler(value = BusinessException.class) //当controller中抛出Exception，则捕获
    public Rs handleException(BusinessException e) {
//        log.error(e.getMessage(), e);
        return Rs.error().message(e.getMessage()).code(e.getCode());
    }


}
