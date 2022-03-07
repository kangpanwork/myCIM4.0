package com.fa.cim.sorter;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * description: carrier处理
 * <p>
 * change history:
 * date             defect#             person             comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/7/20         ********             Ly                 create file
 *
 * @author: Ly
 * @date: 2021/7/20 16:56
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
@Slf4j
public class SorterHandler {
    public static boolean containsFOSB(ObjectIdentifier carrierID) {
        if (!ObjectIdentifier.isEmpty(carrierID)) {
            String resultCarrierID = ObjectIdentifier.fetchValue(carrierID);
            if (resultCarrierID.contains(SorterType.CarrierType.FOSB.getValue())
                    && resultCarrierID.length() >= 4) {
                return true;
            }
        }
        return false;
    }

    /**
    * description: 生成6位数
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/8/4 12:59 Ly Create
    *
    * @author Ly
    * @date 2021/8/4 12:59
    * @param  ‐
    * @return
    */
    public  static String generateSixDigits() {
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        return currentTimeMillis.substring(currentTimeMillis.length() - 6);
    }

    /**
    * description:为生成waferId和lotId准备数据
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/8/4 12:56 Ly Create
    *
    * @author Ly
    * @date 2021/8/4 12:56
    * @param  ‐
    * @return
    */
    public static Infos.NewLotAttributes makeNewlotAttributes(ObjectIdentifier productRequestID,
                                                       List<Info.SortJobListAttributes> jobListGetDROut) {
        //----------------------------------------------------------
        //  actionCode=waferStart  lotID==vendorLotID
        //----------------------------------------------------------
        Info.SortJobListAttributes sortJobInfo = jobListGetDROut.get(0);
        //waferStart多对一 DestinationCarrierID是唯一的
        ObjectIdentifier cassetteID = sortJobInfo.getSorterComponentJobListAttributesList().get(0).getDestinationCarrierID();
        if (log.isDebugEnabled()) {
            log.debug("组装newLotAttributes");
        }
        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        newLotAttributes.setCassetteID(cassetteID);
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        for (Info.SorterComponentJobListAttributes jobListAttributes : sortJobInfo.getSorterComponentJobListAttributesList()) {
            if (SorterType.Action.WaferStart.getValue().equals(jobListAttributes.getActionCode())) {
                for (Info.WaferSorterSlotMap sorterSlotMap : jobListAttributes.getWaferSorterSlotMapList()) {
                    if (ObjectIdentifier.isEmptyWithValue(sorterSlotMap.getLotID())) {
                        continue;
                    }
                    Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                    newWaferAttributes.setNewLotID(productRequestID);
                    newWaferAttributes.setNewSlotNumber(sorterSlotMap.getDestinationSlotNumber());
                    newWaferAttributes.setSourceLotID(sorterSlotMap.getLotID());
                    newWaferAttributes.setSourceWaferID(sorterSlotMap.getWaferID());
                    newWaferAttributes.setWaferAliasName(sorterSlotMap.getAliasName());
                    newWaferAttributesList.add(newWaferAttributes);
                }
            } else {
                continue;
            }
        }
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
        return newLotAttributes;
    }

    /**
    * description:获取可用的carrierID去重后的
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/8/4 12:59 Ly Create
    *
    * @author Ly
    * @date 2021/8/4 12:59
    * @param  ‐
    * @return
    */
    public static List<ObjectIdentifier> getduplicateRemovalCarrierIDs(Info.ComponentJob componentJob){
        Set<ObjectIdentifier> carrierIdSet = new HashSet<>();
        if (!CimObjectUtils.isEmpty(componentJob)) {
            if (!SorterHandler.containsFOSB(componentJob.getDestinationCassetteID())) {
                carrierIdSet.add(componentJob.getDestinationCassetteID());
            }
            if (!SorterHandler.containsFOSB(componentJob.getOriginalCassetteID())) {
                carrierIdSet.add(componentJob.getOriginalCassetteID());
            }
        }

        return new ArrayList<>(carrierIdSet);

    }
}
