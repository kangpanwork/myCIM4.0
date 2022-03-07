package com.fa.cim.common.exception;

import com.fa.cim.common.code.CodeTemplate;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.ThreadContextHolder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * description:
 * This ServiceException use to throw to upper module when process not match ourselves business logic.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/27        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/6/27 14:25
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Getter
@Setter
@Slf4j
public class ServiceException extends CimException {

    private static final long serialVersionUID = 7133863454160604785L;

    private String transactionID;

    private String reasonText;

    public ServiceException(String pMessage) {
        super(pMessage);
    }

    public ServiceException(Integer pCode, String pMessage, String pTransactionId) {
        super(pCode, pMessage);
        this.transactionID = pTransactionId;
        this.reasonText = null;
    }

    public ServiceException(Integer pCode, String pMessage, String pTransactionId, String pReasonText) {
        super(pCode, pMessage);
        this.transactionID = pTransactionId;
        this.reasonText = pReasonText;
    }
    public ServiceException(Integer pCode, String pMessage, String pTransactionId, Object pData) {
        super(pCode, pMessage, pData);
        this.transactionID = pTransactionId;
        this.reasonText = null;
    }

    public ServiceException(Integer pCode, String pMessage, String pTransactionId, Object pData, String pReasonText) {
        super(pCode, pMessage, pData);
        this.transactionID = pTransactionId;
        this.reasonText = pReasonText;
    }

    public ServiceException(OmCode omCode){
        super(omCode);
        this.transactionID = ThreadContextHolder.getTransactionId();
        this.reasonText = null;
    }

    public ServiceException(CodeTemplate codeTemplate){
        super(codeTemplate);
        this.transactionID = ThreadContextHolder.getTransactionId();
        this.reasonText = null;
    }

    public ServiceException(OmCode omCode, String pReasonText){
        super(omCode);
        this.transactionID = ThreadContextHolder.getTransactionId();
        this.reasonText = pReasonText;
    }

    public ServiceException(OmCode omCode, Object pData) {
        super(omCode, pData);
        this.reasonText = null;
        this.transactionID = ThreadContextHolder.getTransactionId();
    }

    public ServiceException(ServiceException e, Object pData) {
        super(e.getCode(), e.getMessage(), pData);
        this.reasonText = null;
        this.transactionID = ThreadContextHolder.getTransactionId();
    }

    @Override
    public String toString() {
        return super.toString() + ", code=" + super.getCode() + ", transactionID=" + transactionID;
    }
}
