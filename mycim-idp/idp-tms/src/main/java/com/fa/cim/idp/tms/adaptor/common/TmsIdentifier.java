package com.fa.cim.idp.tms.adaptor.common;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.idp.tms.adaptor.TmsAdapt;
import lombok.Data;

/**
 * description:
 * <p>TmsIdentifier .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/12        ********             Yuri               create file
 *
 * @author: Yuri
 * @date: 2019/4/12 14:46
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class TmsIdentifier implements TmsAdapt<ObjectIdentifier, TmsIdentifier>{

	private static final long serialVersionUID = -8723465916267464739L;
	private String value;
	private String referenceKey;


	public ObjectIdentifier adapt() {
		ObjectIdentifier result = new ObjectIdentifier();
		result.setReferenceKey(this.referenceKey);
		result.setValue(this.value);
		return result;
	}

	@Override
	public TmsIdentifier from (ObjectIdentifier obj) {
		if (null != obj) {
			this.value = ObjectIdentifier.fetchValue(obj);
			this.referenceKey = ObjectIdentifier.fetchReferenceKey(obj);
		}
		return this;
	}
}
