package com.fa.cim.service.sampling;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

public interface ISamplingService {

    /**
     * description: lot sampling check keep or skip
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon       - common
     * @param lotID           - lotId
     *                        true = skip  false keep
     * @param transactionId   = call sampling of  tx id , not tx of post process
     * @param samplingExecute = execute type
     * @author YJ
     * @date 2020/12/17 0017 10:31
     */
    void sxLotSamplingCheckThenSkipReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String transactionId, String samplingExecute);

}
