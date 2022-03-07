package com.fa.cim.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * description:
 * This Class use to define the response for all of controller methods.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Bear         create file
 *
 * @author: Bear
 * @date: 2018/3/20 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class Response implements Serializable {
    private static final Long serialVersionUID = -3341248196426436741L;
    private Integer code;          // return Code
    private String transactionID; // transaction ID
    private String message;        // return message value, if the Code is error, the message should not null
    private Object body;           // return body;

    public Response() { }

    public Response(Integer code, String transactionID, String message, Object body) {
        this.code = code;
        this.transactionID = transactionID;
        this.message = message;
        this.body = body;
    }

    public static Response createError(final Integer code, final String transactionID, final String message) {
        return new Response(code, transactionID, message, null);
    }
}
