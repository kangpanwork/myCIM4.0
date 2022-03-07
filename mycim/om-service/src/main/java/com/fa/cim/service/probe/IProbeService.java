package com.fa.cim.service.probe;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @exception
 * @author ho
 * @date 2020/11/11 17:52
 */
public interface IProbeService {


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param fixtureID
     * @param fixtureStatus
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @exception
     * @author Jerry_Huang
     * @date 2020/11/12 11:44
     */
    Results.FixtureStatusChangeRptResult sxFixtureStatusChangeRpt(Infos.ObjCommon strObjCommonIn,
                                                                  ObjectIdentifier fixtureID, String fixtureStatus, String claimMemo);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param fixtureID
     * @param claimMemo
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @exception
     * @author ho
     * @date 2020/11/11 18:08
     */
    ObjectIdentifier sxProbeUsageCountResetReq(Infos.ObjCommon strObjCommonIn,
                                               ObjectIdentifier fixtureID,
                                               String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param fixtureID
     * @param claimMemo
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @exception
     * @author ho
     * @date 2020/11/12 18:08
     */
   void sxFixtureStatusMultiChangeRpt(Infos.ObjCommon strObjCommonIn,
                                      String fixtureStatus,
                                      List<ObjectIdentifier> fixtureID,
                                      String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param stockerID
     * @param equipmentID
     * @param strXferFixture
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @exception
     * @author ho
     * @date 2020/11/13 09:08
     */
    Results.FixtureXferStatusChangeRptResult sxFixtureXferStatusChangeRpt(Infos.ObjCommon strObjCommonIn,
                                                                          ObjectIdentifier stockerID,
                                                                          ObjectIdentifier equipmentID,
                                                                          List<Infos.XferFixture> strXferFixture);
}
