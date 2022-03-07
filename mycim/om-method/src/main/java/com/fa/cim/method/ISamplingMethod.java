package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

/**
 * description:  sampling method
 *
 * change history:  
 * date             defect#             person             comments  
 * ---------------------------------------------------------------------------------------------------------------------  
 * 2021/6/25 0025          ********            Decade            create file  
 * @author: YJ
 * @date: 2021/6/25 0025 15:06  
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.    
 */
public interface ISamplingMethod {
    /**
     * description: 通过advanced wafer sampling 抽检wafer
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/6/21 0021 13:36                        YJ                Create
     *
     * @author YJ
     * @date 2021/6/21 0021 13:36
     * @param objCommon  -  common params
     * @param lotInCassette - lot in cassette
     * @param equipmentId - equipment Id
     * @return  lot 中的carrier info
     */
	Infos.AdvancedWaferSamplingConvertInfo lotProcessAdvancedWaferSampling(Infos.ObjCommon objCommon,
			Infos.LotInCassette lotInCassette, ObjectIdentifier equipmentId);

}
