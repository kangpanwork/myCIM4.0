package com.fa.cim.common.exception;

import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.ThreadContextHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>PCS Exception .
 *
 * <p> Used for PCS Service.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/5/15 16:00
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Getter
@Setter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PCSException extends RuntimeException {
    private static final long serialVersionUID = -5826562041869533496L;
    private Integer code;
    private String transactionID;
    private Object data;
    private String reasonText;

    public PCSException(OmCode omCode) {
        super(omCode.getMessage());
        this.code = omCode.getCode();
        this.transactionID = ThreadContextHolder.getTransactionId();
    }

    public PCSException(OmCode omCode, String pReasonText) {
        super(omCode.getMessage());
        this.code = omCode.getCode();
        this.transactionID = ThreadContextHolder.getTransactionId();
        this.reasonText = pReasonText;
    }

    public PCSException(Integer pCode, String pMessage, String pTransactionId) {
        super(pMessage);
        this.code = pCode;
        this.transactionID = pTransactionId;
        this.reasonText = null;
    }

    @Override
    public String toString() {
        return super.toString() + ", code=" + code + ", transactionID=" + transactionID;
    }

    public static void check(boolean expression, OmCode code) {
        if (expression) {
            throw new PCSException(code.getCode(), code.getMessage(), ThreadContextHolder.getTransactionId());
        }
    }

    public static void check(final boolean expression, OmCode code, Object... codeArgs) {
        if (expression) {
            code = new OmCode(code, codeArgs);
            throw new PCSException(code.getCode(), code.getMessage(), ThreadContextHolder.getTransactionId());
        }
    }

    public static void check(final OmCode code, Object... codeArgs) {
        check(true, code, codeArgs);
    }
}
