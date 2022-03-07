package com.fa.cim.aop;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletRequest;
import javax.validation.ConstraintViolationException;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;

import lombok.extern.slf4j.Slf4j;

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
     * catch {@link CoreFrameworkException}
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param e {@link CoreFrameworkException}
     * @return com.fa.cim.dto.Response
     * @author Yuri
     * @date 2019/9/19
     */
    @ResponseBody
    @ExceptionHandler(value = CoreFrameworkException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Response coreFrameworkExceptionHandler(CoreFrameworkException e) {
        String transactionID = Optional.ofNullable(e.getTransactionId()).orElse(ThreadContextHolder.getTransactionId());
        SERVICE_LOGGER.error(">>>>>> coreFrameworkExceptionHandler(): ", e);
        return Response.create(e.getCode(), transactionID, e.getMessage(), null);
    }

    /**
     * description:
     * catch {@link CoreFrameworkException}
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param e {@link CoreFrameworkException}
     * @return com.fa.cim.dto.Response
     * @author Yuri
     * @date 2019/9/19
     */
    @ResponseBody
    @ExceptionHandler(value = NumberFormatException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Response numberFormatExceptionHandler(NumberFormatException e) {
        SERVICE_LOGGER.error(">>>>>> numberFormatExceptionHandler(): ", e);
        return Response.create(-1, e.getMessage());
    }

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

    /**
     * description:
     * catch global exceptions
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
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
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
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
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
