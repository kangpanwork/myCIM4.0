package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.RetCode;

import java.util.List;

/**
 * description:
 * <p>IStartLotMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/5        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/11/5 16:50
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IStartLotMethod {

    /**
     * description:
     * <p>Check if actions of each strDCDef related to strLotInCassette.lotID included in strStartCassette.
     * Then return the out-parameter data sequence as bellows;
     * If, the lot which is required to do action of SPEC check result is Monitor-lot,
     * and, some production lots are connected to its monitor, all of production lots are
     * effected the same action.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param outData                            outData
     * @param objCommon                          objCommon
     * @param startCassetteList                  startCassetteList
     * @param equipmentID                        equipmentID
     * @param interFabMonitorGroupActionInfoList interFabMonitorGroupActionInfoList
     * @param dcActionLotResultList              dcActionLotResultList
     * @return
     * @author PlayBoy
     * @date 2018/11/5 17:44:17
     */
    Outputs.ObjStartLotActionListEffectSpecCheckOut startLotActionListEffectSpecCheck(Outputs.ObjStartLotActionListEffectSpecCheckOut outData, Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList,
                                                                                               ObjectIdentifier equipmentID, List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList,
                                                                                               List<Results.DCActionLotResult> dcActionLotResultList);

    /**
     * description:
     * <p>Check if actions of each strSpcCheckLot.
     * Then return the out-parameter data sequence as bellows;
     * If, the lot which is required to do action of SPC check result is Monitor-lot,
     * and, some production lots are connected to its monitor, all of production lots are
     * effected the same action.</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon                          objCommon
     * @param strSpcCheckLot                     strSpcCheckLot
     * @param spcIFParmList                      spcIFParmList
     * @param equipmentID                        equipmentID
     * @param interFabMonitorGroupActionInfoList interFabMonitorGroupActionInfoList
     * @param dcActionLotResultList              dcActionLotResultList
     * @return
     * @author PlayBoy
     * @date 2018/11/6 18:53:24
     */
    Outputs.ObjStartLotActionListEffectSPCCheckOut startLotActionListEffectSPCCheck(Infos.ObjCommon objCommon, List<Infos.SpcCheckLot> strSpcCheckLot,
                                                                                             List<Infos.SpcIFParm> spcIFParmList, List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList,
                                                                                             ObjectIdentifier equipmentID, List<Results.DCActionLotResult> dcActionLotResultList);
}
