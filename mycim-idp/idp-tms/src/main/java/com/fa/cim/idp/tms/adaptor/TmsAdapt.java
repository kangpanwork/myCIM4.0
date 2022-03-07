package com.fa.cim.idp.tms.adaptor;

import java.io.Serializable;

/**
 * description:
 * <p>TmsAdapt .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/12        ********             Yuri               create file
 *
 * @author: Yuri
 * @date: 2019/4/12 15:31
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface TmsAdapt <T, S> extends Serializable {

	T adapt ();

	S from (T obj);
}
