package com.fa.cim.idp.tms.adaptor.common;

import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.idp.tms.adaptor.TmsAdapt;
import lombok.Data;

/**
 * description:
 * <p>TmsStrResult .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/12        ********             Yuri               create file
 *
 * @author: Yuri
 * @date: 2019/4/12 15:09
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class TmsStrResult <T> implements TmsAdapt<RetCode, TmsStrResult> {

	private static final long serialVersionUID = 1442246375909691212L;
	public String messageID;
	public String messageText;
	public String reasonText;
	public String returnCode;
	public String transactionID;

	@Override
	public RetCode adapt() {
		RetCode<T> retCode = new RetCode<>();
		retCode.setMessageID(this.messageID);
		retCode.setMessageText(this.messageText);
		retCode.setReasonText(this.reasonText);
		OmCode omCode = new OmCode();
		omCode.setCode(Integer.parseInt(returnCode));
		omCode.setMessage(messageText);
		retCode.setReturnCode(omCode);
		retCode.setTransactionID(transactionID);
		return retCode;
	}

	@Override
	public TmsStrResult from(RetCode obj) {
		this.messageID = obj.getMessageID();
		this.messageText = obj.getMessageText();
		this.reasonText = obj.getReasonText();
		this.returnCode = String.valueOf(obj.getReturnCode().getCode());
		this.transactionID = obj.getTransactionID();
		return this;
	}
}
