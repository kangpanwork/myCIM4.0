package com.fa.cim.tms.status.recovery.config;

import com.fa.cim.tms.status.recovery.dto.Code;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @program: mycim_tms
 * @description: MsgRetCodeConfig
 * @author: miner
 * @create: 2018-11-22 09:26
 */


@Component
@PropertySource("classpath:msg.properties")
@ConfigurationProperties(prefix = "rc")
@Setter
@Getter
public class MsgRetCodeConfig {

    public static final int SUCCESS_CODE = 0;
    public static final int WARNING_CODE = 1;
    public static final int ERROR_CODE = 2;
    public static final int SYSTEM_ERROR = 2037;

    private Code msgOk;                     //RC.MSG_OK=(0,"Normal end.")
    private Code msgInvalidType;            //RC.MSG_INVALID_TYPE=(4093,"Invalid type")
    private Code msgRecordNotFound;         //RC.MSG_RECORD_NOT_FOUND=(4181,"Record not found.")
    private Code msgSystemError;            //RC.MSG_SYSTEM_ERROR=(4191,"The system error has occurred. Please try it again. If you get this message again, Notify System Maintenance Engineer of it")
    private Code msgAbnormalStock;          //RC.MSG_ABNORMAL_STOCK=(4017," Abnormal Stock:  [%s]")
    private Code msgMismatchInoutLength;    //RC.MSG_MISMATCH_INOUT_LENGTH=(4134,"Transfer Requesting Carrier count and Accepted Carrier Transfering count does not match.")
    private Code msgNeedSingleData;         //RC.MSG_NEED_SINGLE_DATA=(4145,"Need single data.")
    private Code msgOmsCastTxNoSend;        //RC.MSG_OMS_CAST_TX_NO_SEND=(4136,"Carrier event was queued successfully and request to OMS will be retried later.")
    private Code msgMcsUknownJobid;         //RC.MSG_MCS_UNKNOWN_JOBID=(109,"MCS does not have corresponding jobID")
    private Code msgUnknownJobid;           //RC.MSG_UNKNOWN_JOBID=(4214,"Unknown jobid")
    private Code msgOmsNoResponse;          //RC.MSG_OMS_NO_RESPONSE=(4131,"There was no response from OMS.")
    private Code msgMcsNoResponse;          //RC.MSG_MCS_NO_RESPONSE=(4001,"There was no response from MCS.")
    private Code msgInvalidLotProcstat;     //RC.MSG_INVALID_LOT_PROCSTAT=(933,"Lot %s processStatus %s is invalid for this request.")
    private Code msgLotNotFoundOM;          //RC.MSG_LOT_NOT_FOUND_OM=(1450,"Lot %s information has not been found.")
    private Code msgDifferrentJobID;        //RC.MSG_DIFFERRENT_JOBID=(4046,"Differrent jobid")
    private Code msgJobNotFound;            //RM.MSG_JOB_NOT_FOUND=(4101,"Job not found")
    private Code msgExistEqp;               //RC.MSG_EXIST_EQP=(4051,"Transfer Job is still remaining. Cannot create new transfer job for the same carrier. Please check Carrier/Lot reservation.")
    private Code msgReportFail;             //RC.MSG_REPORT_FAIL=(4185,"Status Change Report Fail")
    private Code msgNotFoundStocker;        //RC.MSG_NOT_FOUND_STOCKER=(4144,"Not found stocker")
    private Code msgEapNoResponse;          //RC.MSG_EAP_NO_RESPONSE=(2104,"There was no response from EAP.")
    private Code msgTmsNoResponse;          //rc.msg_TMS_NO_REsponse=(40)
}

