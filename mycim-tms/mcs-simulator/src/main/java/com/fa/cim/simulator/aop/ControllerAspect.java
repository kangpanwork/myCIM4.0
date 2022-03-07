package com.fa.cim.simulator.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.config.RetCodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletRequest;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.List;

/**
 * description:
 * This ControllerAspect used to handle global exceptions.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/28        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/6/28 11:10
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@ControllerAdvice
public class ControllerAspect {
    /**
     * ServiceException logger ,in case of close ServiceException log in the future.
     */
    private static final Logger SERVICE_LOGGER = LoggerFactory.getLogger(ServiceException.class);


    /**
     * description:
     * catch ServiceException
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param e
     * @return com.fa.cim.dto.Response
     * @author PlayBoy
     * @date 2018/6/29
     */
    @ResponseBody
    @ExceptionHandler(value = ServiceException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Response serviceExceptionHandler(ServiceException e) {
        SERVICE_LOGGER.info(String.format(">>>> e.getData(): %s", e.getData()));
        SERVICE_LOGGER.info(String.format(">>>> e.getData().getClass(): %s", e.getData() == null ? null : e.getData().getClass()));
        SERVICE_LOGGER.error(">>>>>> serviceExceptionHandler(): ", e);
        String transactionID = e.getTransactionID();
        if (StringUtils.isEmpty(transactionID)) {
            transactionID = ThreadContextHolder.getTransactionId();
        }
        return Response.create(e.getCode(), transactionID, e.getLocalizedMessage(), e.getData());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(value = Exception.class)
    public Response errorHandler(Exception e) {
        log.error(">>>>>> errorHandler(): ", e);
        return Response.create(RetCodeConfig.SYSTEM_ERROR,
                ThreadContextHolder.getTransactionId(), StringUtils.isEmpty(e.getLocalizedMessage()) ?
                        "System internal error, please contact the administrator!" :
                        e.getLocalizedMessage(), null);
    }

    //  Failure of model validation will result in this exception

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/11 15:40                     Aoki                Create
     *
     * @param exception
     * @param request
     * @return com.fa.cim.common.support.Response
     * @author Aoki
     * @date 2021/3/11 15:40
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public Response handleException(ConstraintViolationException exception, ServletRequest request) throws IOException {
        SERVICE_LOGGER.debug("start get transactionID >>>>>>>>>>>>>>>>>>>>");
        String strRequest = IOUtils.toString(request.getInputStream(), "UTF-8");
        JSONObject jsonObject = JSONObject.parseObject(strRequest);
        JSONObject userJsonObject = jsonObject.getJSONObject("user");
        User user = JSON.toJavaObject(userJsonObject, User.class);

        SERVICE_LOGGER.error("ConstraintViolationException >>>>>>>>>>>>>> {}", exception);
        String transactionId = "";
        transactionId = user.getFunctionID();

        return Response.createError(923, transactionId, exception.getMessage());
    }

    //  This exception is caused by a failure in parameter verification

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/11 15:39                     Aoki                Create
     *
     * @param ex
     * @param request
     * @return com.fa.cim.common.support.Response
     * @author Aoki
     * @date 2021/3/11 15:39
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public Response resolveMethodArgumentNotValidException(MethodArgumentNotValidException ex, ServletRequest request) throws IOException {
        SERVICE_LOGGER.debug("start get transactionID >>>>>>>>>>>>>>");
        String strRequest = IOUtils.toString(request.getInputStream(), "UTF-8");
        JSONObject jsonObject = JSONObject.parseObject(strRequest);
        JSONObject userJsonObject = jsonObject.getJSONObject("user");
        User user = JSON.toJavaObject(userJsonObject, User.class);

        SERVICE_LOGGER.error("MethodArgumentNotValidException >>>>>>>>>>>>>> {}", ex);
        String transactionId = "";
        transactionId = user.getFunctionID();
        List<ObjectError> objectErrors = ex.getBindingResult().getAllErrors();
        if (!CollectionUtils.isEmpty(objectErrors)) {
            StringBuilder msgBuilder = new StringBuilder();
            for (ObjectError objectError : objectErrors) {
                msgBuilder.append(objectError.getDefaultMessage()).append(",");
            }
            String errorMessage = msgBuilder.toString();
            if (errorMessage.length() > 1) {
                errorMessage = errorMessage.substring(0, errorMessage.length() - 1);
            }
            return Response.createError(923, transactionId, errorMessage);
        }
        return null;
    }
}
