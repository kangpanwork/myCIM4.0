package com.fa.cim.common.constant;

/**
 * Mapping the Listener with proper Params class and TransactionIDEnum
 * And the corresponding topics (for both request topic and reply topic) will be generated as calling the methods getTopic and getRspTopic
 * And the toString is override with getting the value from TransactionIDEnum which is the transactionID;
 * @author Yuri
 * @date 2018/10/19
 */
public enum TransactionTopicEnum {
    OM_EQP_LIST_INQ (TransactionIDEnum.EQP_LIST_INQ),
    OM_BANK_LIST_INQ(TransactionIDEnum.BANK_LIST_INQ),
    TMS_TRANSPORT_JOB_CREATE_REQ(TransactionIDEnum.TMS_TRANSPORT_JOB_CREATE_REQ),
    OM_EQP_LOT_CASSETTE_XFER_STATUS_CHANGE_RPT(TransactionIDEnum.EQP_LOT_CASSETTE_XFER_STATUS_CHANGE_RPT),
    OM_STOCKER_INFO_INQ(TransactionIDEnum.STOCKER_INFO_INQ),
    OM_CASSETTE_STATUS_INQ(TransactionIDEnum.CASSETTE_STATUS_INQ),
    ;

    private TransactionIDEnum transactionID;

    TransactionTopicEnum(TransactionIDEnum transactionID) {
        this.transactionID = transactionID;
    }

    public String getTopic() {
        return transactionID.getValue() + "REQ";
    }

    public String getRspTopic() {
        return transactionID.getValue() + "RSP";
    }


    /**
     * Return the transactionID;
     * @return String
     */
    @Override
    public String toString() {
        return transactionID.getValue();
    }
}
