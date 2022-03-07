package com.fa.cim.common.support;

import com.alibaba.fastjson.annotation.JSONType;
import com.fa.cim.common.exception.ServiceException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * description:
 * This Class use to define the reply for all of controller methods.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Bear         create file
 *
 * @author: Bear
 * @date: 2018/3/20 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
//@ToString
@JSONType(ignores = {"serialVersionUID", "FORMAT_SPLIT"})
public class Response implements Serializable {
    private static final Long serialVersionUID = 3341248196426436741L;

    private static final String FORMAT_SPLIT = "##";

    private Integer code;          // return CimCode
    private String transactionID; // transaction ID
    private String message;        // return message value, if the CimCode is error, the message should not null
    private Object body = null;           // return body;

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code
     * @param transactionID
     * @param message
     * @param body
     * @return
     * @author Bear
     * @date 2018/3/20
     */
    public Response(Integer code, String transactionID, String message, Object body) {
        this.code = code;
        this.transactionID = transactionID;
        this.message = message;
        this.body = body;
    }

    public Response(Integer code, String message, Object body) {
        this.code = code;
        this.message = message;
        this.body = body;
    }


    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param retCode -
     * @return com.fa.cim.dto.Response
     * @author Bear
     * @date 2018/3/20
     */
    public static Response create(RetCode retCode) {
        OmCode returnCode = retCode.getReturnCode();
        return new Response(returnCode == null ? null : returnCode.getCode(), retCode.getTransactionID(),
                returnCode == null ? null : returnCode.getMessage(), retCode.getObject());
    }
    
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/21 0:09
     * @param e -
     * @return com.fa.cim.common.support.Response
     */
    public static Response create(ServiceException e){
        if (e==null){
            throw new RuntimeException("A \"NullPointerException\" could be thrown; \"e\" is nullable here.");
        }
        return new Response(e.getCode(), e.getTransactionID(), e.getMessage(), e.getData());
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code          -
     * @param transactionID -
     * @param message       -
     * @param body          -
     * @return com.fa.cim.dto.Response
     * @author Bear
     * @date 2018/3/20
     */
    public static Response create(Integer code, String transactionID, String message, Object body) {
        return new Response(code, transactionID, message, body);
    }

    public static Response create(Integer code, String message) {
        return new Response(code, null, message, null);
    }

    public static Response create(Integer code, Object body) {
        return new Response(code, null, null, body);
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code          -
     * @param transactionID -
     * @param body          -
     * @param message       -
     * @return com.fa.cim.dto.Response
     * @author Bear
     * @date 2018/3/20
     */
    public static Response createWarn(Integer code, String transactionID, Object body, String message) {
        return new Response(code, transactionID, message, body);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param transactionID transactionID
     * @param body          body
     * @return Response
     * @author PlayBoy
     * @date 2018/7/16
     */
    public static Response createSucc(final String transactionID, final Object body) {
        return new Response(OmCode.SUCCESS_CODE, transactionID, "Success", body);
    }


    public static Response createSucc(final Object body){
        return new Response(OmCode.SUCCESS_CODE, "Success", body);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/10/19 23:17
     * @param transactionID
     * @param omCode
     * @param body -
     * @return com.fa.cim.common.support.Response
     */
    public static Response createSuccWithOmCode(final String transactionID, OmCode omCode, final Object body) {
        return new Response(omCode.getCode(), transactionID, omCode.getMessage(), body);
    }
    
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/28 13:43
     * @param transactionID -
     * @return com.fa.cim.common.support.Response
     */
    public static Response createSucc(final String transactionID) {
        return createSucc(transactionID, null);
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code          -
     * @param transactionID -
     * @param message       -
     * @return com.fa.cim.dto.Response
     * @author Bear
     * @date 2018/3/20
     */
    public static Response createError(final Integer code, final String transactionID, final String message) {
        return new Response(code, transactionID, message, null);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code          -
     * @param transactionID -
     * @param message       -
     * @return com.fa.cim.dto.other.Response
     * @author Bear
     * @date 2018/5/16
     */
    public static Response createError(final OmCode code, final String transactionID, final String message) {
        return new Response(code.getCode(), transactionID, String.format(code.getMessage(), message), null);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code          -
     * @param transactionID -
     * @return com.fa.cim.dto.other.Response
     * @author Bear
     * @date 2018/5/16
     */
    public static Response createError(final OmCode code, final String transactionID) {
        return new Response(code.getCode(), transactionID, code.getMessage(), null);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/7/8 16:44
     * @param code
     * @param transactionID
     * @param data -
     * @return com.fa.cim.common.support.Response
     */
    public static Response createError(final OmCode code, final String transactionID, final Object data) {
        return new Response(code.getCode(), transactionID, code.getMessage(), data);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param transactionID - transaction ID
     * @param message       - message
     * @return com.fa.cim.dto.Response
     * @author PlayBoy
     * @date 2018/6/29
     */
    public static Response createError(final String transactionID, final String message) {
        return new Response(OmCode.ERROR_CODE, transactionID, message, null);
    }


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return java.lang.String
     * @author Bear
     * @date 2018/6/29
     */
    @Override
    public String toString() {
        return String.format("{\"code\":%d, \"transactionID\":\"%s\", \"message\":\"%s\", \"body\":%s}",
                code, transactionID, message, body);
    }
}
