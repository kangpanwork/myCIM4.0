package com.fa.cim.dto;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.BaseStaticMethod;
import com.fa.cim.common.utils.CimLongUtils;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.entity.nonruntime.CimEqpAuto3SettingDO;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.entity.runtime.port.CimPortDO;
import com.fa.cim.entity.runtime.wafer.CimWaferDO;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.person.UserGroupAccessControlInfo;
import com.fa.cim.newcore.dto.season.SeasonDTO;
import com.fa.cim.sorter.Info;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/27        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/27 15:02
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
public class Infos {

    @Data
    public static class CarrierJobResult {
        private String carrierJobID;
        private String carrierJobStatus;
        private ObjectIdentifier carrierID;
        private String zoneType;
        private boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private ObjectIdentifier toMachine;
        private ObjectIdentifier toPortID;
        private String expectedStartTime;
        private String expectedEndTime;
        private boolean mandatoryFlag;
        private String priority;
        private String estimatedStartTime;
        private String estimatedEndTime;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/3/17 14:38
     */
    @Data
    public static class StockerEqp {
        private Long priority;
        private ObjectIdentifier equipmentID;
        private Boolean availableStateFlag;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @return
     * @exception
     * @date 2019/12/30 11:05
     */
    @Data
    public static class APCRunTimeCapabilityResponse {
        private APCBaseIdentification strAPCBaseIdentification;
        private List<APCRunTimeCapability> strAPCRunTimeCapability;
        private APCBaseReturnCode strAPCBaseReturnCode;
        private Object siInfo;
    }

    @Data
    public static class APCIf {
        private ObjectIdentifier equipmentID;
        private String eqpDescription;
        private String APCSystemName;
        private boolean APCIgnoreable;
        private ObjectIdentifier APCRep1UserID;
        private ObjectIdentifier APCRep2UserID;
        private String APCConfigStatus;
        private ObjectIdentifier registeredUserID;
        private String registeredTimeStamp;
        private String registeredMemo;
        private ObjectIdentifier approvedUserID;
        private String approvedTimeStamp;
        private String approvedMemo;
        private String updateTimeStamp;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @return
     * @exception
     * @date 2019/12/30 11:05
     */
    @Data
    public static class RTDDataGetDROut {
        private List<RTDConfigInfo> RTDRecords;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @return
     * @exception
     * @date 2019/12/30 11:05
     */
    @Data
    public static class RTDConfigInfo {
        private String functionCode;
        private String stationID;
        private String optionAttributes;
        private Long defaultTimeout;
        private Object siInfo;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @return
     * @exception
     * @date 2019/12/30 11:05
     */
    @Data
    public static class APCBaseReturnCode {
        private String state;
        private String reasonCode;
        private String reasonText;
        private String messageText;
        private Object siInfo;
    }

    @Data
    public static class retMessage {
        private Integer returnCode;
        private String messageText;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @return
     * @exception
     * @date 2019/12/30 11:05
     */
    @Data
    public static class APCRunTimeCapability {
        private List<APCBaseAPCSystemFunction1> strAPCBaseAPCSystemFunction1;
        private List<APCSpecialInstruction> strAPCSpecialInstruction;
        private List<APCLotWaferCollection> strAPCLotWaferCollection;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @date 2020/5/20 11:19
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Season {
        private ObjectIdentifier seasonID;
        private String condType;
        private String seasonType;
        private ObjectIdentifier productID;
        private ObjectIdentifier eqpID;
        private Timestamp createTime;
        private Timestamp lastModifyTime;
        private Timestamp lastSBYTime;
        private Boolean pmFlag;
        private String lastRecipe;
        private Timestamp lastSeasonTime;
        private ObjectIdentifier userID;
        private String status;
        private List<SeasonChamber> chambers;
        private List<SeasonProdRecipe> recipes;
        private List<SeasonProduct> seasonProducts;
        private SeasonParam param;
        private Integer priority;

        public Season(SeasonDTO.Season season) {
            this.seasonID = season.getSeasonID();
            this.condType = season.getCondType();
            this.seasonType = season.getSeasonType();
            this.productID = season.getProductID();
            this.eqpID = season.getEqpID();
            this.createTime = season.getCreateTime();
            this.lastModifyTime = season.getLastModifyTime();
            this.lastSBYTime = season.getLastSBYTime();
            this.pmFlag = season.getPmFlag();
            this.lastRecipe = season.getLastRecipe();
            this.lastSeasonTime = season.getLastSeasonTime();
            this.userID = season.getUserID();
            this.status = season.getStatus();
            this.chambers = Optional.ofNullable(season.getChambers())
                    .map(data -> data.stream().map(SeasonChamber::new).collect(Collectors.toList()))
                    .orElseGet(() -> Collections.emptyList());
            this.recipes = Optional.ofNullable(season.getRecipes())
                    .map(data -> data.stream().map(SeasonProdRecipe::new).collect(Collectors.toList()))
                    .orElseGet(() -> Collections.emptyList());
            this.seasonProducts = Optional.ofNullable(season.getSeasonProducts())
                    .map(data -> data.stream().map(SeasonProduct::new).collect(Collectors.toList()))
                    .orElseGet(() -> Collections.emptyList());
            this.param = new SeasonParam(season.getParam());
            this.priority = season.getPriority();
        }

        public SeasonDTO.Season convert() {
            SeasonDTO.Season retVal = new SeasonDTO.Season();
            retVal.setSeasonID(this.seasonID);
            retVal.setCondType(this.condType);
            retVal.setSeasonType(this.seasonType);
            retVal.setProductID(this.productID);
            retVal.setEqpID(this.eqpID);
            retVal.setCreateTime(this.createTime);
            retVal.setLastModifyTime(this.lastModifyTime);
            retVal.setLastSBYTime(this.lastSBYTime);
            retVal.setPmFlag(this.pmFlag);
            retVal.setLastRecipe(this.lastRecipe);
            retVal.setLastSeasonTime(this.lastSeasonTime);
            retVal.setUserID(this.userID);
            retVal.setStatus(this.status);
            retVal.setChambers(Optional.ofNullable(this.getChambers())
                    .map(data -> data.stream().map(SeasonChamber::convert).collect(Collectors.toList()))
                    .orElseGet(() -> Collections.emptyList()));
            retVal.setRecipes(Optional.ofNullable(this.getRecipes())
                    .map(data -> data.stream().map(SeasonProdRecipe::convert).collect(Collectors.toList()))
                    .orElseGet(() -> Collections.emptyList()));
            retVal.setSeasonProducts(Optional.ofNullable(this.getSeasonProducts())
                    .map(data -> data.stream().map(SeasonProduct::convert).collect(Collectors.toList()))
                    .orElseGet(() -> Collections.emptyList()));
            retVal.setParam(Optional.ofNullable(this.getParam()).map(SeasonParam::convert).orElse(null));
            retVal.setPriority(this.priority);
            return retVal;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/28 12:50
     */
    @Data
    public static class HsSeasonSetup {
        private String action;
        private Timestamp actionTime;
        private Timestamp createTime;
        private ObjectIdentifier seasonID;
        private String condType;
        private String seasonType;
        private ObjectIdentifier productID;
        private ObjectIdentifier eqpID;
        private Timestamp lastSeasonTime;
        private ObjectIdentifier userID;
        private String status;
        private List<SeasonChamber> chambers;
        private List<SeasonProdRecipe> recipes;
        private List<SeasonProduct> seasonProducts;
        private SeasonParam param;
        private Integer priority;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/20 17:47
     * @deprecated use {@link com.fa.cim.newcore.dto.rcpgrp.RecipeGroup.Info} instead
     */
    @Data
    @Deprecated
    public static class RecipeGroup {
        private ObjectIdentifier recipeGroupID;
        private List<ObjectIdentifier> recipeInfos;
        private String description;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/20 11:24
     */
    @Data
    @NoArgsConstructor
    public static class SeasonChamber {
        private ObjectIdentifier chamberID;
        private Integer seqNo;

        public SeasonChamber(SeasonDTO.SeasonChamber seasonChamber) {
            this.chamberID = seasonChamber.getChamberID();
            this.seqNo = seasonChamber.getSeqNo();
        }

        public SeasonDTO.SeasonChamber convert() {
            SeasonDTO.SeasonChamber retVal = new SeasonDTO.SeasonChamber();
            retVal.setChamberID(this.chamberID);
            retVal.setSeqNo(this.seqNo);
            return retVal;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/20 12:38
     */
    @Data
    @NoArgsConstructor
    public static class SeasonProdRecipe {
        private ObjectIdentifier recipeID;
        private Integer seqNo;

        public SeasonProdRecipe(SeasonDTO.SeasonProdRecipe recipe) {
            this.recipeID = recipe.getRecipeID();
            this.seqNo = recipe.getSeqNo();
        }

        public SeasonDTO.SeasonProdRecipe convert() {
            SeasonDTO.SeasonProdRecipe retVal = new SeasonDTO.SeasonProdRecipe();
            retVal.setRecipeID(this.recipeID);
            retVal.setSeqNo(this.seqNo);
            return retVal;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/20 12:43
     */
    @Data
    @NoArgsConstructor
    public static class SeasonProduct {
        private ObjectIdentifier productID;
        private Integer quantity;
        private Integer seqNo;

        public SeasonProduct(SeasonDTO.SeasonProduct product) {
            this.productID = product.getProductID();
            this.quantity = product.getQuantity();
            this.seqNo = product.getSeqNo();
        }

        public SeasonDTO.SeasonProduct convert() {
            SeasonDTO.SeasonProduct retVal = new SeasonDTO.SeasonProduct();
            retVal.setProductID(this.productID);
            retVal.setQuantity(this.quantity);
            retVal.setSeqNo(this.seqNo);
            return retVal;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/20 12:43
     */
    @Data
    @NoArgsConstructor
    public static class SeasonParam {
        private Integer maxIdleTime;
        private Integer intervalBetweenSeason;
        //        private Integer minSeasonWaferPerChamber;
//        private Integer minSeasonWaferPerJob;
        private Boolean seasonGroupFlag;
        private Boolean noChamberFlag;
//        private Boolean noIdleFlag;
        private Boolean waitFlag;
        private Timestamp firstTriggerTime;
        private ObjectIdentifier fromRecipeGroup;
        private ObjectIdentifier toRecipeGroup;

        public SeasonParam(SeasonDTO.SeasonParam seasonParam) {
            this.maxIdleTime = seasonParam.getMaxIdleTime();
            this.intervalBetweenSeason = seasonParam.getIntervalBetweenSeason();
            this.seasonGroupFlag = seasonParam.getSeasonGroupFlag();
            this.noChamberFlag = seasonParam.getNoChamberFlag();
            this.waitFlag = seasonParam.getWaitFlag();
            this.firstTriggerTime = seasonParam.getFirstTriggerTime();
            this.fromRecipeGroup = seasonParam.getFromRecipeGroup();
            this.toRecipeGroup = seasonParam.getToRecipeGroup();
        }

        public SeasonDTO.SeasonParam convert() {
            SeasonDTO.SeasonParam retVal = new SeasonDTO.SeasonParam();
            retVal.setMaxIdleTime(this.maxIdleTime);
            retVal.setIntervalBetweenSeason(this.intervalBetweenSeason);
            retVal.setSeasonGroupFlag(this.seasonGroupFlag);
            retVal.setNoChamberFlag(this.noChamberFlag);
            retVal.setWaitFlag(this.waitFlag);
            retVal.setFirstTriggerTime(this.firstTriggerTime);
            retVal.setFromRecipeGroup(this.fromRecipeGroup);
            retVal.setToRecipeGroup(this.toRecipeGroup);
            return  retVal;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/20 12:43
     */
    @Data
    public static class MachineSeasonpriority {
        private ObjectIdentifier eqpID;
        private List<SeasonPriority> priorities;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/22 16:51
     */
    @Data
    @NoArgsConstructor
    public static class SeasonJob {
        private String condType;
        private String seasonType;
        private ObjectIdentifier seasonProductID;
        private ObjectIdentifier eqpID;
        private Timestamp createTime;
        private Timestamp lastModifyTime;
        private ObjectIdentifier userID;
        private Integer priority;
        private ObjectIdentifier seasonJobID;
        private ObjectIdentifier seasonID;
        private String chamber;
        private String seasonJobStatus;
        private ObjectIdentifier seasonLotID;
        private ObjectIdentifier seasonCarrierID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier seasonRcpID;
        private ObjectIdentifier carrierID;
        private Integer waferQty;
        private Integer minSeasonWaferCount;
        private Integer maxIdleTime;
        private Integer intervalBetweenSeason;
        //        private Integer minSeasonWaferPerChamber;
//        private Integer minSeasonWaferPerJob;
        private Boolean seasonGroupFlag;
//        private Boolean noIdleFlag;
        private Boolean waitFlag;
        private ObjectIdentifier fromRecipe;
        private ObjectIdentifier toRecipe;
        private Timestamp moveInTime;
        private Timestamp moveOutTime;
        private String opeMemo;

        public SeasonJob (SeasonDTO.SeasonJob seasonJob) {
            this.carrierID = seasonJob.getCarrierID();
            this.chamber = seasonJob.getChamber();
            this.condType = seasonJob.getCondType();
            this.createTime = seasonJob.getCreateTime();
            this.eqpID = seasonJob.getEqpID();
            this.fromRecipe = seasonJob.getFromRecipe();
            this.intervalBetweenSeason = seasonJob.getIntervalBetweenSeason();
            this.lastModifyTime = seasonJob.getLastModifyTime();
            this.lotID = seasonJob.getLotID();
            this.maxIdleTime = seasonJob.getMaxIdleTime();
            this.minSeasonWaferCount = seasonJob.getMinSeasonWaferCount();
            this.moveInTime = seasonJob.getMoveInTime();
            this.moveOutTime = seasonJob.getMoveOutTime();
            this.opeMemo = seasonJob.getOpeMemo();
            this.priority = seasonJob.getPriority();
            this.seasonCarrierID = seasonJob.getSeasonCarrierID();
            this.seasonGroupFlag = seasonJob.getSeasonGroupFlag();
            this.seasonID = seasonJob.getSeasonID();
            this.seasonJobID = seasonJob.getSeasonJobID();
            this.seasonJobStatus = seasonJob.getSeasonJobStatus();
            this.seasonLotID = seasonJob.getSeasonLotID();
            this.seasonProductID = seasonJob.getSeasonProductID();
            this.seasonRcpID = seasonJob.getSeasonRcpID();
            this.seasonType = seasonJob.getSeasonType();
            this.toRecipe = seasonJob.getToRecipe();
            this.userID = seasonJob.getUserID();
            this.waferQty = seasonJob.getWaferQty();
            this.waitFlag = seasonJob.getWaitFlag();
        }

        public SeasonDTO.SeasonJob convert() {
            SeasonDTO.SeasonJob retVal = new SeasonDTO.SeasonJob();
            retVal.setCondType(this.condType);
            retVal.setSeasonType(this.seasonType);
            retVal.setSeasonProductID(this.seasonProductID);
            retVal.setEqpID(this.eqpID);
            retVal.setCreateTime(this.createTime);
            retVal.setLastModifyTime(this.lastModifyTime);
            retVal.setUserID(this.userID);
            retVal.setPriority(this.priority);
            retVal.setSeasonJobID(this.seasonJobID);
            retVal.setSeasonID(this.seasonID);
            retVal.setChamber(this.chamber);
            retVal.setSeasonJobStatus(this.seasonJobStatus);
            retVal.setSeasonLotID(this.seasonLotID);
            retVal.setSeasonCarrierID(this.seasonCarrierID);
            retVal.setLotID(this.lotID);
            retVal.setSeasonRcpID(this.seasonRcpID);
            retVal.setCarrierID(this.carrierID);
            retVal.setWaferQty(this.waferQty);
            retVal.setMinSeasonWaferCount(this.minSeasonWaferCount);
            retVal.setMaxIdleTime(this.maxIdleTime);
            retVal.setIntervalBetweenSeason(this.intervalBetweenSeason);
            retVal.setSeasonGroupFlag(this.seasonGroupFlag);
            retVal.setWaitFlag(this.waitFlag);
            retVal.setFromRecipe(this.fromRecipe);
            retVal.setToRecipe(this.toRecipe);
            retVal.setMoveInTime(this.moveInTime);
            retVal.setMoveOutTime(this.moveOutTime);
            retVal.setOpeMemo(this.opeMemo);
            return retVal;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/22 16:51
     */
    @Data
    public static class HsSeasonJob {
        private String action;
        private Timestamp actionTime;
        private Timestamp createTime;
        private String condType;
        private String seasonType;
        private ObjectIdentifier seasonProductID;
        private ObjectIdentifier eqpID;
        private ObjectIdentifier userID;
        private Integer priority;
        private ObjectIdentifier seasonJobID;
        private ObjectIdentifier seasonID;
        private String chamber;
        private String seasonJobStatus;
        private ObjectIdentifier seasonLotID;
        private ObjectIdentifier seasonCarrierID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier seasonRcpID;
        private ObjectIdentifier carrierID;
        private Integer waferQty;
        private Integer minSeasonWaferCount;
        private Integer maxIdleTime;
        private Integer intervalBetweenSeason;
        private Boolean seasonGroupFlag;
        private Boolean noIdleFlag;
        private ObjectIdentifier fromRecipe;
        private ObjectIdentifier toRecipe;
        private Timestamp moveInTime;
        private Timestamp moveOutTime;
        private String opeMemo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/5/20 12:43
     */
    @Data
    public static class SeasonPriority {
        private ObjectIdentifier seasonID;
        private Integer priority;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @return
     * @exception
     * @date 2019/12/30 11:05
     */
    @Data
    public static class APCLotWaferCollection {
        private String lotID;
        private List<String> waferID;
        private Object siInfo;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @return
     * @exception
     * @date 2019/12/30 11:05
     */
    @Data
    public static class APCSpecialInstruction {
        private String instructionID;
        private List<APCBaseFactoryEntity> strAPCBaseFactoryEntity;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @return
     * @exception
     * @date 2019/12/30 11:05
     */
    @Data
    public static class APCBaseFactoryEntity {
        private String className;
        private String id;
        private String attrib;
        private Object siInfo;
    }

    @Data
    public static class APCBaseAPCSystemFunction1 {
        private String type;
        private String controlFrequency;
        private String description;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2019/12/30 11:07
     */
    @Data
    public static class APCBaseIdentification {
        private String systemName;
        private String userId;
        private String password;
        private String newPassword;
        private String clientNode;
        private Object siInfo;
    }

    @Data
    public static class ActualChamberStatus {
        private ObjectIdentifier chamberID;
        private ObjectIdentifier actualStatusCode;        // Reported and translated status
        private ObjectIdentifier chamberStatusCode;       // Converted status
        private int result;                  // return code of check result
    }

    @Data
    public static class ChamberStateHisByAuto {
        private ObjectIdentifier chamberID;
        private ObjectIdentifier chamberStatusCode;
        private ObjectIdentifier E10Status;
        private ObjectIdentifier actualStatus;
        private ObjectIdentifier actualE10Status;
        private ObjectIdentifier previousStatus;
        private ObjectIdentifier previousE10Status;
        private ObjectIdentifier previousActualStatus;
        private ObjectIdentifier previousActualE10Status;
        private String prevStateStartTime;
        private Boolean genHistory;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/1/7 11:03:15
     */
    @Data
    public static class TempFlowBatch {
        private ObjectIdentifier temporaryFlowBatchID;
        private ObjectIdentifier cassetteID;
        private List<TempFlowBatchLot> strTempFlowBatchLot;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/25 14:21
     */
    @Data
    public static class OperationHistoryFillInTxPLQ008DROut {
        private List<OperationHisInfo> strOperationHisInfo;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/25 14:23
     */
    @Data
    public static class OperationHistoryFillInTxPLQ008DRIn {
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String operationPass;
        private String operationCategory;
        private Boolean pinPointFlag;
        private Object siInfo;

        //add fromTimeStamp and toTimeStamp for runcard history
        private String fromTimeStamp;
        private String toTimeStamp;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/25 13:50
     */
    @Data
    public static class OpeHisReticleInfo {
        private String reticleID;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/25 13:53
     */
    @Data
    public static class OpeHisFixtureInfo {
        private String fixtureID;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/26 10:22
     */
    @Data
    public static class LotOpeHisRParmGetDROut {
        private List<OpeHisRecipeParmInfo> strOpeHisRecipeParmInfo;
        private List<OpeHisRecipeParmWaferInfo> strOpeHisRecipeParmWaferInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 14:04
     */
    @Data
    public static class TargetTableInfo {
        private String tableName;
        private List<HashedInfo> strHashedInfoSeq;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 14:15
     */
    @Data
    public static class TableRecordInfo {
        private String tableName;
        private List<String> columnNames;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 14:17
     */
    @Data
    public static class TableRecordValue {
        private String tableName;
        private String reportTimeStamp;
        private List<Object> columnValues;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/25 13:47
     */
    @Data
    public static class OperationHisInfo {
        private String reportTimeStamp;
        private String routeID;
        private String operationNumber;
        private String operationPass;
        private String operationID;
        private String operationName;
        private String operationCategory;
        private String stageID;
        private String stageGroupID;
        private String maskLevel;
        private String locationID;
        private String workArea;
        private String equipmentID;
        private String equipmentName;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<OpeHisReticleInfo> strOpeHisReticleInfo;
        private List<OpeHisFixtureInfo> strOpeHisFixtureInfo;
        private List<OpeHisRecipeParmInfo> strOpeHisRecipeParmInfo;
        private List<OpeHisRecipeParmWaferInfo> strOpeHisRecipeParmWaferInfo;
        private String flowBatchID;
        private String controlJobID;
        private Long reworkCount;
        private String initialHoldFlag;
        private String lastHoldReleaseFlag;
        private String holdType;
        private String holdTimeStamp;
        private String holdUserID;
        private String holdReasonCodeID;
        private String holdReasonCodeDescription;
        private String reasonCodeID;
        private String reasonCodeDescription;
        private String bankID;
        private String previousBankID;
        private String cassetteID;
        private String cassetteCategory;
        private String productType;
        private String testType;
        private String externalPriority;
        private String priorityClass;
        private String productID;
        private String productGroupID;
        private String technologyID;
        private String customerCode;
        private String customerProductID;
        private String orderNumber;
        private String lotOwnerID;
        private String dueTimeStamp;
        private String waferHistoryTimeStamp;
        private Long originalWaferQuantity;
        private Long currentWaferQuantity;
        private Long productWaferQuantity;
        private Long controlWaferQuantity;
        private Long claimedProductWaferQuantity;
        private Long claimedControlWaferQuantity;
        private Long totalGoodUnit;
        private Long totalFailUnit;
        private String userID;
        private String claimMemo;
        private String storeTimeStamp;
        private Boolean testCriteriaFlag;
        private String recipeParameterChangeType;
        private String relatedLotID;
        private String bondingGroupID;
        private String pdType;
        private Object siInfo;
        private String department;
        private String section;
        private String monitorGroupId;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/23 10:37
     */
    @Data
    public static class ExpiredQrestTimeLotListGetDROut {
        private List<ObjectIdentifier> lotIDs;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/23 10:40
     */
    @Data
    public static class ExpiredQrestTimeLotListGetDRIn {
        private Long maxRetrieveCount;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/1/7 15:38:40
     */
    @Data
    public static class FlowBatchCandidateLot {
        private String flowBatchID;
        private FlowBatchLotInfo flowBatchLotInfo;
        private Object siInfo;
    }

    @Data
    public static class ChangedLotSchedule {
        private String operationNumber;
        private ObjectIdentifier equipmentID;
        private String plannedStartTime;
        private String plannedFinishTime;
    }

    @Data
    public static class DynamicRouteList {
        private ObjectIdentifier routeID;   // route id
        private String routeDescription;     // route description
        private Boolean activeVersionFlag;       // active version flag
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/12/4 14:04:47
     */
    @Data
    public static class LotFuturePctrlListInqInParm {
        private ObjectIdentifier lotID;
        private List<HashedInfo> strRequestActionFlagSeq;
        private Integer searchCount;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/1/9 15:54:00
     */
    @Data
    public static class CassetteAssignedMahineGetDR {
        private ObjectIdentifier machineID;
        private Boolean equipmentFlag;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/28 13:28
     */
    @Data
    public static class LotQtimeInfoGetForClearByProcessOperationOut {
        private RetCode strResult;
        private List<String> qTimeClearList;
        //        private List<HoldList>          strLotHoldReleaseList;
//        private List<HoldList>          strFutureHoldCancelList;
        private List<LotHoldReq> strLotHoldReleaseList;
        private List<LotHoldReq> strFutureHoldCancelList;
        private List<FutureReworkInfo> strFutureReworkCancelList;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/28 13:30
     */
    @Data
    public static class LotQtimeInfoGetForClearByProcessOperationIn {
        private ObjectIdentifier lotID;
        private Boolean allClearFlag;
        private Boolean previousOperationFlag;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/12/4 14:13:53
     */
    @Data
    public static class OperationFutureActionAttributes {
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String operationName;
        private String mainPOS;
        private String modulePOS;
        private String originalMainPDID;
        private String originalOpeNo;
        private String subOrigMainPDID;
        private String subOrigOpeNo;
        private List<HashedInfo> strFutureActionFlagSeq;
        private Object siInfo;
    }

    @Data
    public static class EqpChamberState {
        private ObjectIdentifier chamberID;           //  returned( updated & not-changed)
        private ObjectIdentifier chamberStatusCode;        // DCR 9900001
        private ObjectIdentifier chamberE10Status;         // DCR 9900001
        private ObjectIdentifier actualStatus;             // Actual status at this time          DCR 9900001
        private ObjectIdentifier actualE10Status;          // Actual E10 status at this time      DCR 9900001
        private ObjectIdentifier previousStatus;           // ChamberStatus which was overwritten 0.01
        private ObjectIdentifier previousE10Status;        // ChamberStatus which was overwritten DCR 9900001
        private ObjectIdentifier previousActualStatus;     // Actual status at this time          DCR 9900001
        private ObjectIdentifier previousActualE10Status;  // Actual E10 status at this time      DCR 9900001
        private String prevStateStartTime;  // Overwritten ChamberStatus started time  0.01
    }

    /*@Data
    public static class EqpChamberStatus {
        private String  chamberID;            //Chamber ID
        private String  chamberStatusCode;    //Chamber State CimCode
    }*/

    @Data
    public static class EqpChamberStatusCheckResult {
        private ObjectIdentifier chamberID;            //Chamber ID
        private ObjectIdentifier chamberStatusCode;    //Chamber State CimCode
        private int result;
    }

    @Data
    public static class EqpInBuffer {
        private ObjectIdentifier portID;
        private ObjectIdentifier equipmentShelfID;
        private String portType;
        private String portStatus;
        private String processJobID;
        private String eapPJStatus;
        private ObjectIdentifier waferID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier controlJobID;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/12 10:09:42
     */
    @Data
    public static class EqpOnePortGroupInfo {
        private ObjectIdentifier equipmentID;
        private PortGroup strPortGroup;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/12 10:09:42
     */
    @Data
    public static class EqpPortInfoOrderByGroup {
        private ObjectIdentifier equipmentID;
        private List<PortGroup> strPortGroup;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/7                            Yuri            Add Comparator for Collections.sort
     *
     * @author Ho
     * @date 2018/10/12 10:10:10
     */
    @Data
    public static class PortGroup {
        private String portGroup;
        private List<String> accessModes;
        private List<Infos.CapableOperationMode> capableOperationModes;
        private List<PortID> strPortID;
        private String siInfo;

        private static SortByDispatchStateTimestampComparator sortByDispatchStateTimestampComparator = new SortByDispatchStateTimestampComparator();

        public static SortByDispatchStateTimestampComparator getSortByDispatchStateTimestampComparator() {
            return sortByDispatchStateTimestampComparator;
        }

        private static class SortByDispatchStateTimestampComparator implements Comparator<PortGroup> {

            @Override
            public int compare(PortGroup o1, PortGroup o2) {
                return PortID.sortByDispatchStateTimestampComparator.compare(
                        Collections.max(o1.strPortID, PortID.sortByDispatchStateTimestampComparator),
                        Collections.max(o2.strPortID, PortID.sortByDispatchStateTimestampComparator));
            }
        }

    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/9 15:57:34
     */
    @Data
    public static class AvailableStocker {
        private ObjectIdentifier stockerID;
        private String stockerName;
        private String siInfo;
    }

    @Data
    public static class ReticlePodInStocker {
        private ObjectIdentifier reticlePodID;
        private List<ObjectIdentifier> reticleID;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Bear
     * @date 2018/10/11 14:52
     * @return
     */
    @Data
    public static class FuncList {
        private String subSystemID;   // sub system id
        private List<FuncID> funcIDList; // Function IDs. For example, the ID of TxFutureHoldReq is "TXPC041".
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Bear
     * @date 2018/10/11 14:54
     * @return
     */
    @Data
    public static class FuncID {
        private String categoryID;   // Category ID. It depends on SM setting.
        private String functionID;   // function ID. It depends on SM setting.
        private String permission;   // Permission. It depends on SM setting.
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/9 10:13:58
     */
    @Data
    public static class StockerInfo {
        private ObjectIdentifier stockerID;
        private String description;
        private Boolean utsFlag;
        private String siInfo;
    }

    @Data
    public static class EqpProcessBuffer {
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unloadPortID;
        private String processJobID;
        private String eapPJStatus;
        private ObjectIdentifier waferID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier chamberID;
        private String siInfo;
    }

    @Data
    public static class EqpOutBuffer {
        private ObjectIdentifier portID;
        private ObjectIdentifier equipmentShelfID;
        private String portType;
        private String portStatus;
        private String processJobID;
        private String eapPJStatus;
        private ObjectIdentifier waferID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier controlJobID;
        private String siInfo;
    }

    @Data
    public static class EqpControlJobInfo {
        private ObjectIdentifier controlJobID;
        private String eapControlJobStatus;
        private String omsControlJobStatus;
        private String siInfo;
    }

    @Data
    public static class DCActionResult {
        private ObjectIdentifier measurementLotID;
        private ObjectIdentifier lotID;
        private Boolean monitorLotFlag;
        private ObjectIdentifier dcDefID;
        private ObjectIdentifier dcSpecID;
        private String checkType;
        private String reasonCode;
        private String actionCode;
        private ObjectIdentifier processRouteID;
        private String processOperationNumber;
        private ObjectIdentifier processOperationID;
        private Integer processPassCount;
        private ObjectIdentifier processEquipmentID;
        private ObjectIdentifier processRecipeID;
        private String processFabID;
        private ObjectIdentifier measureRouteID;
        private String measureOperationNumber;
        private ObjectIdentifier measureOperationID;
        private Integer measurePassCount;
        private ObjectIdentifier measureEquipmentID;
        private ObjectIdentifier measureRecipeID;
        private String measureFabID;
        private String bankID;
        private String reworkRouteID;
        private List<EntityIdentifier> entities;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/9/26 14:13:49
     */
    @Data
    public static class DataSpecCheckResult {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private List<DCDef> strDCDefResult;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/9/26 14:13:49
     */
    @Data
    public static class CandidateDurableStatus {
        private String durableStatus;
        private String description;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/15 13:24:09
     */
    @Data
    public static class DurableAttribute implements Cloneable {
        private ObjectIdentifier durableID;
        private String description;
        private String category;
        private String carrierType;
        private Boolean usageCheckFlag;
        private String maximumRunTime;
        private Double maximumOperationStartCount;
        private Integer intervalBetweenPM;
        private Integer capacity;
        private Integer nominalSize;
        private String contents;
        private String instanceName;
        private String productUsage;
        private List<UserData> userDatas;

        @Override
        public DurableAttribute clone() throws CloneNotSupportedException {
            DurableAttribute newDurableAttribute = (DurableAttribute) super.clone();
            List<UserData> newUserDatas = new ArrayList<>();
            for (UserData userData : userDatas) {
                newUserDatas.add(userData.clone());
            }

            newDurableAttribute.setUserDatas(newUserDatas);
            return (DurableAttribute) super.clone();
        }
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/15 13:23:45
     */
    @Data
    public static class UserData {
        private String name;
        private String type;
        private String value;
        private String originator;
        private String siInfo;

        @Override
        public UserData clone() throws CloneNotSupportedException {
            return (UserData) super.clone();
        }
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @return
     * @date 2018/10/8 10:22:27
     */
    @Data
    public static class ResourceInfo {
        private String resourceID;
        private String resourceType;
        private Boolean resourceAvailable;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @return
     * @date 2018/10/8 10:23:52
     */
    @Data
    public static class ZoneInfo {
        private String zoneID;
        private String zoneDescription;
        private Integer standerdCapacityOfZone;
        private Integer emergencyCapacityOfZone;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/8 10:27:26
     */
    @Data
    public static class CarrierInStocker {
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier lotID;
        private Boolean emptyFlag;
        private String carrierCategory;
        private String transferJobStatus;
        private String multiLotType;
        private String resrvUserId;
        private Boolean dispatchReserved;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/9/28 17:16:33
     */
    @Data
    public static class DurableSubStateGetOut {
        private String durableStatus;
        private ObjectIdentifier durableSubStatus;
        private String siInfo;
    }

    @Data
    public static class WorkArea {
        private ObjectIdentifier workArea;
        private String description;
        private String siInfo;

        public WorkArea() {

        }

        public WorkArea(ObjectIdentifier workArea, String description) {
            this.workArea = workArea;
            this.description = description;
        }
    }

    @Data
    public static class BankAttributes {
        private ObjectIdentifier bankID;
        private String bankName;
        private boolean receiveBankFlag;
        private boolean shipBankFlag;
        private boolean stbBankFlag;
        private boolean bankInBankFlag;
        private boolean recyclenBankFlag;
        private boolean controlWaferBankFlag;
        private String productType;
        private boolean waferIdGenerateBankFlag;
        private boolean productBankFlag;
        private List<ObjectIdentifier> nextBankID;
        private Object reserve;
    }

    @Data
    public static class BackupAddress {
        private String hostName;     //<i>Host Name
        private String serverName;   //<i>Server Name
        private String itDaemonPort; //<i>IT Daemon port Number
        private Object reserve;       //<i>Reserved for SI customization

        public BackupAddress() {
        }

        public BackupAddress(CimLotDO lot) {
            this.hostName = lot.getHostName();
        }
    }

    @Data
    public static class PosBackupAddress {
        private String hostName;
        private String serverName;
        private String itDaemonPort;
    }


    @Data
    public static class BinCount {
        private String binNumber;
        private String dieCount;
        private ObjectIdentifier binSpecID;
        private String binSpecCodeDes;
        private String binpassCriteriaCode;
    }

    @Data
    public static class ChamberInfo {
        private String dTheSystemKey;
        private String chamberID;            //P3100336
        private String currentStateID;       //P3100336
        private Boolean availableFlag;
        private Boolean conditionalAvailable;
    }

    @Data
    public static class StartSeqNo {
        private String key;                //<i>Key for
        private Integer startSeqNo;         //<i>Start Sequence Number
    }

    @Data
    public static class BackupProcess {
        private String entryRouteId;            //Entry Route ID that a lot entries in other Line.
        private String entryOperationNumber;    //Entry Operation Number that a lot entries in other Line.
        private String exitRouteId;             //Exit Route ID that a lot exits from other Line.
        private String exitOperationNumber;     //Exit Operation Number that a lot exits from other Line.
    }

    @Data
    public static class ConfigInfo {
        private String objectKey;               //Object Key
        private String description;             //Description
        private String type;                    //Type
        private String category;                //Configuration Category
        private String value;                   //Value
    }

    @Data
    public static class EqpMonitorDetailInfo {
        private ObjectIdentifier eqpMonitorID;                      //<i>eqp MonitorID
        private ObjectIdentifier equipmentID;                       //<i>eqp  ID
        private ObjectIdentifier chamberID;                         //<i>Chamber ID
        private String description;                       //<i>Description
        private String monitorType;                       //<i>MonitorType (Routine or Manual)
        private String scheduleType;                      //<i>ScheduleType (In R14.0, "Time")
        List<EqpMonitorProductInfo> strEqpMonitorProductInfoSeq;       //<i>Product list of eqp Monitor
        private Timestamp startTimeStamp;                    //<i>First Execution Time of eqp Monitor
        private Integer executionInterval;                 //<i>Interval of eqp Monitor Execution
        private Integer warningInterval;                   //<i>Time for Expiration Notification
        private Integer expirationInterval;                //<i>Time for Expiration
        private Boolean standAloneFlag;                    //<i>For Future Enhancement
        private Boolean kitFlag;                           //<i>Kit is required or not
        private Integer maxRetryCount;                     //<i>Max Retry Count before eqp Monitor fail
        private EqpStatus eqpStateAtStart;                   //<i>eqp State changed at eqp Monitor start
        private EqpStatus eqpStateAtPassed;                  //<i>eqp State changed at eqp Monitor passed
        private EqpStatus eqpStateAtFailed;                  //<i>eqp State changed at eqp Monitor failed
        List<EqpMonitorActionInfo> strEqpMonitorActionInfoSeq;        //<i>Action List of eqp Monitor
        private String monitorStatus;                     //<i>Status of eqp Monitor
        private String warningTime;                       //<i>Time that Expiration Notification was sent
        private String expirationTime;                    //<i>The Time of eqp Monitor Expiration
        private String nextExecutionTime;                 //<i>The Time of Next eqp Monitor Execution
        private String scheduleBaseTimeStamp;             //<i>The Base Time for Estimating Next Execution Time
        private Integer scheduleAdjustment;                //<i>The Adjustment Factor for eqp Monitor Next Execution
        private String lastMonitorTimeStamp;              //<i>The Time of Last eqp Monitor Job Completion
        private String lastMonitorResult;                 //<i>The Result of Last eqp Monitor Job.
        private Timestamp lastMonitorPassedTimeStamp;        //<i>The Time of Last "Passed" eqp Monitor Job.
        private Timestamp lastClaimedTimeStamp;              //<i>Last Claimed Time Stamp
        private ObjectIdentifier lastClaimedUser;                   //<i>Last Claimed User
    }

    @Data
    public static class EqpMonitorProductInfo {
        private ObjectIdentifier productID;                          //<i>Product ID for eqp Monitor
        private ObjectIdentifier recipeID;                          //<i>recipe ID for eqp Monitor
        private Integer waferCount;                         //<i>wafer Count of the Product
        private Integer startSeqNo;                         //<i>Start Order Control at Monitor Operation
    }

    @Data
    public static class EqpStatus {
        private ObjectIdentifier E10Status;                          //<i>E10 Status
        private ObjectIdentifier equipmentStatusCode;                //<i>eqp Status CimCode
        private String equipmentStatusName;                //<i>eqp Status Name
        private String equipmentStatusDescription;         //<i>eqp Status Description
        private Boolean availableFlag;                      //<i>Available Flag
    }

    @Data
    public static class XferCassette {
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier portID;
        private String transferStatus;
        private String xPosition;
        private String yPosition;
        private String zPosition;
    }

    @Data
    public static class EqpMonitorActionInfo {
        private String eventType;                          //<i>Event Type of Action
        private String action;                             //<i>Action
        private ObjectIdentifier reasonCodeID;                       //<i>Reason CimCode ID
        private ObjectIdentifier sysMessageCodeID;                   //<i>System Message CimCode ID
        private String customField;                        //<i>Custom Field
    }

    @Data
    public static class ReturnCodeInfo {
        private ObjectIdentifier lotID;
        private OmCode returnCode;
    }

    @Data
    public static class QTimeRestriction {
        private String qTimeRestriction_CN;
        private String qTimeRestriction_RID;
    }

    @Data
    public static class BondingGroupInfo {
        private String bondingGroupID;
        private String bondingGroupState;
        private ObjectIdentifier targetEquipmentID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier updateUserID;
        private String createTime;
        private String updateTime;
        private List<BondingMapInfo> bondingMapInfoList;
        private String claimMemo;
    }

    @Data
    public static class BondingMapInfo {
        private String bondingGroupID;
        private ObjectIdentifier baseLotID;
        private ObjectIdentifier baseProductID;
        private ObjectIdentifier baseCarrierID;
        private Long baseSlotNo;
        private ObjectIdentifier baseWaferID;
        private String baseBondingSide;
        private ObjectIdentifier planTopLotID;
        private ObjectIdentifier planTopProductID;
        private ObjectIdentifier planTopCarrierID;
        private Long planTopSlotNo;
        private ObjectIdentifier planTopWaferID;
        private String planTopBondingSide;
        private ObjectIdentifier actualTopLotID;
        private ObjectIdentifier actualTopProductID;
        private ObjectIdentifier actualTopCarrierID;
        private Long actualTopSlotNo;
        private ObjectIdentifier actualTopWaferID;
        private String actualTopBondingSide;
        private String bondingProcessState;
        private String processCompleteTime;
    }

    @Data
    public static class CassetteBrInfo {
        private String description;       //<i>Description
        private String cassetteCategory; //
        private String carrierType; //
        private boolean usageCheckFlag;  //<i>Usage Check Flag
        private Long capacity;            //<i>Capacity                                                             //D8000084
        private Long nominalSize;        //<i>Nominal Size                                                         //D8000084
        private String contents;         //<i>Contents                                                             //D8000084
        private Object siInfo;           //<i>Reserved for SI customization            //<i>Reserved for SI customization
        private String productUsage;
        private boolean relationFoupFlag;
    }

    @Data
    public static class CassetteJobResult {
        private String cassetteJobID;
        private String cassetteJobStatus;
        private String cassetteID;
        private String zoneType;

        private boolean n2PurgeFlag;  // N2 Purge Flag
        private String formMachineID;
        private String fromPortID;
        private String toStockerGroup;

        private ObjectIdentifier toMachine;
        private String toPortID;
        private String expectedStartTime;
        private String expectedEndTime;

        private boolean mandatoryFlag; //Mandatory Flag
        private String priority;
        private String estimatedStartTime; // Estimated Start Time
        private String estimatedEndTime;  // Estimated End Time

        private Object reserve;             //<i>Reserved for myCIM4.0 customization
    }

    @Data
    public static class CassetteLoadPort {
        private ObjectIdentifier cassetteID;          //cassette ID
        private ObjectIdentifier portID;              //port ID
        private Object reserve;             //<i>Reserved for myCIM4.0 customization
    }

    @Data
    public static class CassettePmInfo {
        private String runTime;                       //<i>Run Time
        private String maximumRunTime;                //<i>Maximum Run Time
        private Long operationStartCount;           //<i>Operation Start Count
        private Long maximumOperationStartCount;    //<i>Maximum Operation Start Count
        private Long passageTimeFromLastPM;         //<i>Passage Time From Last PM    //minutes D3100008
        private Long intervalBetweenPM;             //<i>Interval Between PM          //minutes D3100008
        private String lastMaintenanceTimeStamp;   //<i>Last Maintenance Time Stamp
        private String lastMaintenancePerson;      //<i>Last Maintenance person
        private Object siInfo;                       //<i>Reserved for SI customization
    }

    @Data
    public static class CassetteStatusInfo {
        private String cassetteStatus;                     //<i>Carrier Status
        private String transferStatus;                     //<i>Transfer Status
        private ObjectIdentifier transferReserveUserID;              //<i>Transfer Reserve User ID
        private String transferReserveUserReference;
        private ObjectIdentifier equipmentID;                        //<i>eqp ID
        private String equipmentReference;
        private ObjectIdentifier stockerID;                          //<i>stocker ID
        private String stockerReference;
        private boolean emptyFlag;                          //<i>Empty Flag
        private String multiLotType;                       //<i>Multi lot Type
        private String zoneType;                           //<i>Zone Type
        private String priority;                           //<i>Priority
        private List<ContainedLotInfo> strContainedLotInfo;                //<i>Sequence of Contained lot Information
        private String lastClaimedTimeStamp;               //<i>Last Claimed Time Stamp
        private ObjectIdentifier lastClaimedPerson;                  //<i>Last Claimed person
        private String lastClaimedPersonReference;
        private boolean sorterJobExistFlag;                 //<i>Sorter Job Existence Flag
        private boolean inPostProcessFlagOfCassettea;        //<i>InPostProcessFlag of cassette
        private ObjectIdentifier slmRsvEquipmentID;                  //<i>Reserved eqp for SLM
        private String slmRsvEquipmentReference;
        private String interFabXferState;                  //<i>interFab Xfer State

        private ObjectIdentifier controlJobID;
        private ObjectIdentifier durableControlJobID;
        private ObjectIdentifier durableSubStatus;
        private String durableSubStatusName;
        private String durableSubStatusDescription;
        private Boolean availableForDurableFlag;
        private boolean durableSTBFlag;
        private List<HashedInfo> strDurableStatusList;
        private String processLagTime;

        private ShelfPosition shelfPosition; //for e-rack, added by nyx
        private Object siInfo;                             //<i>Reserved for SI customization

        private String jobStatus;
    }

    @Data
    public static class CandidateChamber {
        //private List<CimLogicalRecipeDSetPrstDO> chamberList; // chamber info
        private List<Infos.Chamber> chamberList;
        private boolean inhibitFlag;  // Inhibit check result
    }

    @Data
    public static class ContainedLotInfo {
        private ObjectIdentifier lotID;    //<i>lot ID
        private boolean autoDispatchDisableFlag;       //<i>Auto dispatch Disable Flag
        private Object siInfo;                     //<i>Reserved for SI customization
    }

    @Data
    public static class ControlJobCreateRequest {
        private ObjectIdentifier equipmentID;     // eqp ID
        private String portGroup;       // port group ID
        private List<StartCassette> startCassetteList;
    }


    @Data
    public static class Coordinate3D {
        private Long x;
        private Long y;
        private Long z;
    }

    @Data
    public static class DCDef {
        private ObjectIdentifier dataCollectionDefinitionID;
        private String description;
        private String dataCollectionType;
        private List<DCItem> dcItemList;
        private Boolean calculationRequiredFlag;
        private Boolean specCheckRequiredFlag;
        private ObjectIdentifier dataCollectionSpecificationID;
        private String dcSpecDescription;
        private List<DCSpec> dcSpecList;
        private ObjectIdentifier previousDataCollectionDefinitionID;
        private ObjectIdentifier previousOperationID;
        private String previousOperationNumber;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/20 17:07
     */
    @Data
    public static class CollectedDataChangeEventMakeIn {
        private String transactionID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier dataCollectionDefinitionID;
        private List<DCItem> strPreviousRawDCItem;
        private List<DCItem> strCurrentRawDCItem;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class DCItem {
        private String dataCollectionItemName;
        private String dataCollectionMode;
        private String dataCollectionUnit;
        private String dataType;
        private String itemType;
        private String measurementType;
        private ObjectIdentifier waferID;
        private String waferPosition;
        private String sitePosition;
        private Boolean historyRequiredFlag;
        private String calculationType;
        private String calculationExpression;
        private String dataValue;
        private String targetValue;
        private String dataItemName;
        private Boolean screenLimitUpperRequired;
        private Double screenLimitUpper;
        private String actionCodesUscrn;
        private Boolean screenLimitLowerRequired;
        private Boolean specLimitUpperRequired;
        private Boolean controlLimitUpperRequired;
        private Boolean specLimitLowerRequired;
        private Boolean controlLimitLowerRequired;
        private Double screenLimitLower;
        private Double controlLimitUpper;
        private String actionCodesUcl;
        private Double specLimitUpper;
        private Double specLimitLower;
        private Double controlLimitLower;
        private String actionCodesLsl;
        private String actionCodesLscrn;
        private String actionCodesLcl;
        private String target;
        private String tag;
        private String dcSpecGroup;
        private String siInfo;
        private String actionCodesUsl;
        private String specCheckResult;
        private List<String> actionCode;
    }

    @Data
    public static class DCSpec {
        private String dataItemName;
        private Boolean screenLimitUpperRequired;
        private Double screenLimitUpper;
        private String actionCodesUscrn;
        private Boolean screenLimitLowerRequired;
        private Double screenLimitLower;
        private String actionCodesLscrn;
        private Boolean specLimitUpperRequired;
        private Double specLimitUpper;
        private String actionCodesUsl;
        private Boolean specLimitLowerRequired;
        private Double specLimitLower;
        private String actionCodesLsl;
        private Boolean controlLimitUpperRequired;
        private Double controlLimitUpper;
        private String actionCodesUcl;
        private Boolean controlLimitLowerRequired;
        private Double controlLimitLower;
        private String actionCodesLcl;
        private Double target;
        private String tag;
    }

    @Data
    public static class DCSpecData {
        private String dcItemName;
        private String dcItemDataType;
        private String dcItemType;
        private String dcItemMeasType;
        private String dcItemMode;
        private String dcItemTarget;
        private String dcSpecScreenUpperReq;
        private String dcSpecScreenUpperLimit;
        private String dcSpecScreenUpperAction;
        private String dcSpecScreenLowerReq;
        private String dcSpecScreenLowerLimit;
        private String dcSpecScreenLowerAction;

        private String dcSpecSPECUpperReq;
        private String dcSpecSPECUpperLimit;
        private String dcSpecSPECUpperAction;
        private String dcSpecSPECLowerReq;
        private String dcSpecSPECLowerLimit;
        private String dcSpecSPECLowerAction;

        private String dcSpecCNTLUpperReq;
        private String dcSpecCNTLUpperLimit;
        private String dcSpecCNTLUpperAction;
        private String dcSpecCNTLLowerReq;
        private String dcSpecCNTLLowerLimit;
        private String dcSpecCNTLLowerAction;
    }


    @Data
    public static class DirectedSourceLot {
        private ObjectIdentifier sourceLotID;     //<i>Source lot ID
        private List<SourceProduct> strSourceProduct;    //<i>Sequence of Source Product
        private Object reserve;          //<i>Reserved for SI customization
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/24 16:11:42
     */
    @Data
    public static class DurableOperationNameAttributes {
        private Integer seqno;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String operationName;
        private String operationPass;
        private String objrefPO;
        private ProcessRef processRef;
        private String testType;
        private String inspectionType;
        private ObjectIdentifier stageID;
        private ObjectIdentifier stageGroupID;
        private String maskLevel;
        private String departmentNumber;
        private Boolean mandatoryOperationFlag;
        private Double standardCycleTime;
        private String plannedStartTime;
        private String plannedEndTime;
        private ObjectIdentifier plannedMachine;
        private String actualStartTime;
        private String actualCompTime;
        private ObjectIdentifier assignedMachine;
        private List<ObjectIdentifier> machines;
        private String siInfo;
    }

    @Data
    public static class DurableLocationInfo {
        private String instanceName;           //<i>Instance Name
        private boolean currentLocationFlag;    //<i>Current Location Flag
        private String backupState;            //<i>Backup State
    }

    @Data
    public static class DurableOperationInfo {
        private ObjectIdentifier startBankID;                //<i>Start bank ID
        private ObjectIdentifier routeID;                    //<i>Route ID
        private ObjectIdentifier operationID;                //<i>Operation ID
        private String operationNumber;            //<i>Operation Number
        private String operationName;              //<i>Operation Name
        private ObjectIdentifier stageID;                    //<i>Stage ID
        private String department;                 //<i>Department
        private Boolean mandatoryOperationFlag;     //<i>Mandatory Operation Flag
        private List<LotEquipmentList> strEquipmentList;           //<i>Sequence of eqp List
        private Timestamp queuedTimeStamp;            //<i>Queued TimeStamp
        private Long reworkCount;                    //<i>Rework Count
        private ObjectIdentifier logicalRecipeID;            //<i>Logical Recipe ID
        private Double standardProcessTime;        //<i>Standard Process Time
        private String dueTimeStamp;               //<i>Due TimeStamp
        private ObjectIdentifier bankID;                     //<i>bank ID
        private Object siInfo;                     //<i>Reserved for SI customization
    }

    @Data
    public static class DurableStartRecipe {
        private ObjectIdentifier logicalRecipeId;                             //Logical Recipe ID
        private ObjectIdentifier machineRecipeId;                             //Machine Recipe ID
        private String physicalRecipeId;                            //Physical Recipe ID
        private Boolean parameterUpdateFlag;                        //Parameter Update Flag
        private List<StartRecipeParameter> StartRecipeParameterS;   //Sequence of Start Recipe Parameter
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/2 14:22
     */
    @Data
    public static class DurableOperationMoveEventMakeLocateIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private Boolean locateDirection;
        private Inputs.OldCurrentPOData strOldCurrentPOData;
        private String transactionID;
        private String claimMemo;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/2 13:36
     */
    @Data
    public static class DurableOperationMoveEventMakeGatePassIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private String transactionID;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class DurableWipOperationInfo {
        private ObjectIdentifier responsibleRouteID;         //<i>Responsible Route ID
        private ObjectIdentifier responsibleOperationID;     //<i>Responsible Operation ID
        private String responsibleOperationNumber; //<i>Responsible Operation Number
        private String responsibleOperationName;   //<i>Responsible Operation Name
        private Object siInfo;                     //<i>Reserved for SI customization
    }

    @Data
    public static class EnvVariableList {
        private String envName;         // environment name
        private String envValue;        // environment value
    }

    @Data
    public static class WhatNextAttributes {
        private ObjectIdentifier lotID;                                  //<i>lot ID
        private ObjectIdentifier cassetteID;                             //<i>Carrier ID
        private String lotType;                                //<i>lot Type
        private String multiLotType;                           //<i>Multi lot Type
        private String transferStatus;                         //<i>Transfer Status
        private ObjectIdentifier transferReserveUserID;                  //<i>Transfer Reserve User ID
        private ObjectIdentifier stockerID;                              //<i>stocker ID
        private ObjectIdentifier equipmentID;                            //<i>eqp ID
        private ObjectIdentifier flowBatchID;                            //<i>Flow Batch ID
        private ObjectIdentifier controlJob;                             //<i>Control Job
        private ObjectIdentifier processReserveEquipmentID;              //<i>Process Reserve eqp ID
        private ObjectIdentifier processReserveUserID;                   //<i>Process Reserve User ID
        private ObjectIdentifier productID;                              //<i>Product ID
        private Boolean recipeAvailableFlag;                    //<i>Recipe Available Flag
        private ObjectIdentifier logicalRecipeID;                        //<i>Logical Recipe ID
        private ObjectIdentifier machineRecipeID;                        //<i>Machine Recipe ID
        private ObjectIdentifier processMonitorProductID;                //<i>Process Monitor Product ID
        private List<ObjectIdentifier> reticleGroupIDs;                        //<i>Sequence of Reticle Group IDs
        private Boolean reticleExistFlag;                       //<i>Reticle Exist Flag
        private Timestamp lastClaimedTimeStamp;                   //<i>Last Claimed Time Stamp
        private Timestamp stateChangeTimeStamp;                   //<i>State Change Time Stamp
        private Timestamp inventoryChangeTimeStamp;               //<i>Inventory Change Time Stamp
        private Timestamp dueTimeStamp;                           //<i>Due Time Stamp
        private Timestamp planStartTimeStamp;                     //<i>Plan Start Time Stamp
        private Timestamp planEndTimeStamp;                       //<i>Plan End Time Stamp
        private ObjectIdentifier plannedEquipmentID;                     //<i>Planned eqp ID
        private Timestamp queuedTimeStamp;                        //<i>Queued Time Stamp
        private String priorityClass;                          //<i>Priority Class
        private double internalPriority;                       //<i>Internal Priority
        private String externalPriority;                       //<i>External Priority
        private Boolean qtimeFlag;                              //<i>Qtime Flag
        private List<LotQtimeInfo> strLotQtimeInfo;                        //<i>Sequence of lot Q Time Info
        private boolean minQTimeFlag;                                   //<i>Min Q-Time Flag
        private List<LotQtimeInfo> minQTimeInfos;                       //<i>Sequence of lot Min Q-Time Info
        private Timestamp preOperationCompTimeStamp;              //<i>Pre Operation Comp Time Stamp
        private ObjectIdentifier routeID;                                //<i>Route ID
        private ObjectIdentifier operationID;                            //<i>Operation ID
        private String operationNumber;                        //<i>Operation Number
        private ObjectIdentifier testTypeID;                             //<i>Test Type ID
        private String inspectionType;                         //<i>Inspection Type
        private ObjectIdentifier stageID;                                //<i>Stage ID
        private Boolean mandatoryOperationFlag;                 //<i>Mandatory Operation Flag
        private Boolean processHoldFlag;                        //<i>Process Hold Flag
        private Integer totalWaferCount;                        //<i>Total wafer Count
        private Integer totalGoodDieCount;                      //<i>Total Good Die Count
        private ObjectIdentifier next2EquipmentID;                       //<i>Next2 eqp ID
        private ObjectIdentifier next2LogicalRecipeID;                   //<i>Next2 Logical Recipe ID
        private LotNoteFlagInfo strLotNoteFlagInfo;                     //<i>lot Note Flag Information
        private List<EntityInhibitAttributes> entityInhibitions;                      //<i>Sequence of Entity Inhibitions
        private String physicalRecipeID;                       //<i>Physical Recipe ID
        private Boolean operableFlagForCurrentMachineState;     //<i>Operable Flag for Current Machine State
        private Boolean operableFlagForMultiRecipeCapability;   //<i>perable Flag for Multi Recipe Capability
        private String requiredCassetteCategory;               //<i>Required Carrier Type
        private String cassetteCategory;                       //<i>Carrier Type
        private String next2requiredCassetteCategory;          //<i>Next2 Required Carrier Type
        private Boolean sorterJobExistFlag;                     //<i>Sorter Job Existence Flag
        private Boolean inPostProcessFlagOfCassette;            //<i>InPostProcessFlag of cassette
        private Boolean inPostProcessFlagOfLot;                 //<i>InPostProcessFlag of lot
        private String bondingFlowSectionName;                 //<i>Bonding Flow Section Name
        private String bondingCategory;                        //<i>Bonding Category
        //<c>SP_Lot_BondingCategory_Base
        //<c>SP_Lot_BondingCategory_Top
        private String topProductID;                           //<i>Top Product ID
        private String bondingGroupID;                         //<i>Bonding Group ID
        private Boolean autoDispatchDisableFlag;                //<i>Auto dispatch Disable Flag
        private Boolean monitorOperationFlag;                   //<i>lot is on eqp Monitor Target Operation or not
        private Boolean pilotRunFlag;                           //<i>lot is on eqp Monitor Target Operation or not
        private String eqpMonitorJobID;                        //<i>eqp Monitor job ID
        private Integer startSeqNo;                             //<i>Start Order Control at Monitor Operation                   //<i>Reserved for SI customization
        private Boolean capabilityFlag;                           //<i>eqp's capability match the PD's requirment or not
    }


    @Data
    public static class LotQtimeInfo {
        private ObjectIdentifier qrestrictionTriggerRouteID;            //<i>Q Restriction Trigger Route ID
        private String qrestrictionTriggerOperationNumber;    //<i>Q Restriction Trigger Operation Number
        private String qrestrictionTriggerTimeStamp;          //<i>Q Restriction Trigger Time Stamp
        private ObjectIdentifier qrestrictionTargetRouteID;             //<i>Q Restriction Target Route ID
        private String qrestrictionTargetOperationNumber;     //<i>Q Restriction Target Operation Number
        private String qrestrictionTargetTimeStamp;           //<i>Q Restriction Target Time Stamp
        private String qrestrictionRemainTime;                //<i>Q Restriction Remain Time  //0.30
        private String watchDogRequired;                      //<i>Watch Dog Required         //0.45
        private String actionDoneFlag;                        //<i>Action Done Flag           //0.45
    }

    @Data
    public static class LotNoteFlagInfo {
        private Boolean lotCommentFlag;        //<i>If lot Comment exist, True.
        private Boolean lotNoteFlag;           //<i>If lot Note exist, True.
        private Boolean lotOperationNoteFlag;  //<i>If lot Operation Note exist, True.
    }

    @Data
    public static class WaferTransfer {
        private ObjectIdentifier waferID;
        private ObjectIdentifier destinationCassetteID;
        private Boolean bDestinationCassetteManagedByOM;
        private Boolean bOriginalCassetteManagedByOM;
        private Integer destinationSlotNumber;
        private ObjectIdentifier originalCassetteID;
        private Integer originalSlotNumber;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/1/10 14:42:02
     */
    @Data
    public static class ReasonCodeAttributes {
        private ObjectIdentifier reasonCodeID;
        private String codeDescription;
        private Object siInfo;
    }

    @Data
    public static class EntityInhibitAttributes {
        private List<EntityIdentifier> entities;          //<i>Sequence of Entities
        private List<String> subLotTypes;       //<i>Sequence of Sub lot Types.
        private String startTimeStamp;    //<i>Start Time Stamp
        private String endTimeStamp;      //<i>End Time Stamp
        private String reasonCode;        //<i>Inhibit Registration Reason CimCode.
        private String reasonDesc;        //<i>Inhibit Registretion Reason Description
        private String memo;              //<i>Comment for inhibition
        private ObjectIdentifier ownerID;           //<i>Owner ID of who registed the Inhibition.
        private String claimedTimeStamp;  //<i>Claimed Time Stamp
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/22 13:16:39
     */
    @Data
    public static class ReticleAdditionalAttribute {
        private Timestamp lastUsedTime;
        private ObjectIdentifier reserveUserID;
        private ObjectIdentifier transferReserveUserID;
        private ObjectIdentifier transferDestinationEqpID;
        private ObjectIdentifier transferDestinationStockerID;
        private Timestamp transferReserveTimestamp;
        private ObjectIdentifier transferReserveReticlePodID;
        private List<ObjectIdentifier> reservedControlJobs;
        private String siInfo;
    }

    @Data
    public static class EntityIdentifier {
        private String className;         //<i>Class Name should be selected from the following items.
        //<c>SP_InhibitClassID_Product           "Product Specification"
        //<c>SP_InhibitClassID_Route             "Route"
        //<c>SP_InhibitClassID_Operation         "Operation"
        //<c>SP_InhibitClassID_Process           "Process Definition"
        //<c>SP_InhibitClassID_MachineRecipe     "Machine Recipe"
        //<c>SP_InhibitClassID_Equipment         "eqp"
        //<c>SP_InhibitClassID_Reticle           "Reticle"
        //<c>SP_InhibitClassID_ReticleGroup      "Reticle Group"
        //<c>SP_InhibitClassID_Fixture           "Fixture"
        //<c>SP_InhibitClassID_FixtureGroup      "Fixture Group"
        //<c>SP_InhibitClassID_Stage             "Stage"                     // D4100113 add
        //<c>SP_InhibitClassID_ModulePD          "Module Process Definition" // D4100113 add
        //<c>SP_InhibitClassID_Chamber           "Infos.Chamber"                   // D6000217
        private ObjectIdentifier objectID;            //<i>Object ID (Entity ID)  (Not Support)
        private String attribution;              //<i>Attribution            (Not Support)
        private String siInfo;              //<i>Reserved for SI customization  //D4200055

        public EntityIdentifier() {
        }

        public EntityIdentifier(String className, ObjectIdentifier objectID) {
            this.className = className;
            this.objectID = objectID;
        }

        public EntityIdentifier(String className, ObjectIdentifier objectID, String attribution) {
            this.className = className;
            this.objectID = objectID;
            this.attribution = attribution;
        }
    }


    @Data
    @NoArgsConstructor
    public static class EntityInhibitInfo {
        private ObjectIdentifier entityInhibitID;
        private String stringifiedObjectReference;
        private EntityInhibitAttributes entityInhibitAttributes;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/5/17 13:55
     */
    @Data
    public static class EntityInhibitCheckForEntitiesOut {
        private List<EntityInhibitInfo> entityInhibitInfo;
        private Object siInfo;
    }

    @Data
    public static class EntityInhibitRecord {
        private static final Long serialVersionUID = 8336473612450935823L;
        private String id;
        private String referenceKey;
        private String description;
        private Timestamp startTimeStamp;
        private Timestamp endTimeStamp;
        private Timestamp changedTimeStamp;
        private List<EntityIdentifier> entities;
        private List<String> subLotTypes;
        private ObjectIdentifier reasonCode;
        private String claimMemo;
        private ObjectIdentifier owner;
    }

    @Data
    public static class EqpBrInfo {
        private String equipmentName;                  //<i>eqp Name
        private ObjectIdentifier workBay;                       //<i>Work area ID
        private String equipmentOwner;                 //<i>eqp Owner
        private String eapResourceName;                //<i>EAP Resource Name
        private String equipmentCategory;              //<i>eqp Category
        private String equipmentGroup;
        private String equipmentModelNo;
        //<c>SP_Mc_Category_Dummy               "Dummy"
        //<c>SP_Mc_Category_WaferSorter         "wafer Sorter"
        //<c>SP_Mc_Category_AssemblyVendor      "Assembly Vendor"
        //<c>SP_Mc_Category_Inspection          "Inspection"
        //<c>SP_Mc_Category_Test                "Test"
        //<c>SP_Mc_Category_Measurement         "Measurement"
        //<c>SP_Mc_Category_Process             "Process"
        //<c>SP_Mc_Category_CircuitProbe        "Circuit Probe"
        //<c>SP_Mc_Category_InternalBuffer      "Internal Buffer"
        private boolean reticleUseFlag;                 //<i>Reticle Use Flag
        private boolean fixtureUseFlag;                 //<i>Fixture Use Flag
        private boolean cassetteChangeFlag;             //<i>Carrier Change Flag
        private boolean startLotsNotifyRequiredFlag;    //<i>Start Lots Notify Required Flag
        private boolean monitorCreationFlag;            //<i>Monitor Creation Flag
        private boolean eqpToEqpTransferFlag;           //<i>eqp to Eqipment Transfer Flag
        private boolean takeInOutTransferFlag;          //<i>Take in out Transfer Flag
        private boolean emptyCassetteRequireFlag;       //<i>Empty Carrier Required Flag
        private String fmcSwitch;
        private boolean fmcCapabilityFlag;
        private ObjectIdentifier monitorBank;                    //<i>Monitor bank ID
        private ObjectIdentifier dummyBank;                      //<i>Dummy bank ID
        private List<String> specialControl;            //<i>Sequence of Special Control
        private String multiRecipeCapability;          //<i>Multi recipe Capability
        private long maxBatchSize;                   //<i>Maximum Process Batch Size             //DSIV00001830
        private long minBatchSize;                   //<i>Minimum Process Batch Size             //DSIV00001830
        private long minWaferCount;                  //<i>Minimum wafer Count                    //DSIV00001830
        private boolean processJobLevelCtrl;            //<i>Process Job Level Control Flag         //DSN000015229
        private List<String>  contaminationList;      //eqp contamination list
        private String prControl;
    }

    @Data
    public static class ChangeLotScheduleReturn {
        private ObjectIdentifier lotID;
        private String returnCode;

        public ChangeLotScheduleReturn() {
        }

        public ChangeLotScheduleReturn(ObjectIdentifier lotID, String returnCode) {
            this.lotID = lotID;
            this.returnCode = returnCode;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/23 11:03:45
     */
    @Data
    public static class ChangeLotReturn {
        private ObjectIdentifier lotID;
        private String returnCode;
        private Object siInfo;
    }

    @Data
    public static class EqpChamberInfo {
        private List<EqpChamberStatusInfo> eqpChamberStatuses;    //<i>Sequence of eqp Infos.Chamber Status
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/10 10:39:10
     */
    @Data
    public static class EqpChamberStatus {
        private ObjectIdentifier chamberID;
        private ObjectIdentifier chamberStatusCode;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/13 15:10:30
     */
    @Data
    public static class EquipmentListInfoGetDROut {
        private ObjectIdentifier workArea;
        private List<AreaStocker> strAreaStocker;
        private List<AreaEqp> strAreaEqp;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 15:48
     */
    @Data
    public static class EquipmentHistoryGetDROut {
        private List<TableRecordInfo> strTableRecordInfoSeq;
        private List<TableRecordValue> strTableRecordValueSeq;
        private Page<List<TableRecordValue>> strTableRecordValuePage;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 17:20
     */
    @Data
    public static class EquipmentMonitorJobLotHistoryGetDROut {
        private List<TableRecordInfo> strTableRecordInfoSeq;
        private List<TableRecordValue> strTableRecordValueSeq;
        private Page<List<TableRecordValue>> strTableRecordValuePage;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 16:22
     */
    @Data
    public static class LotControlJobHistoryGetDROut {
        private List<TableRecordInfo> strTableRecordInfoSeq;
        private List<TableRecordValue> strTableRecordValueSeq;
        private Page<List<TableRecordValue>> strTableRecordValuePage;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/8/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/8/20 16:21
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class RunCardHistoryGetDROut {
        private List<TableRecordInfo> strTableRecordInfoSeq;
        private List<TableRecordValue> strTableRecordValueSeq;
        private Page<List<TableRecordValue>> strTableRecordValuePage;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 16:25
     */
    @Data
    public static class LotControlJobHistoryGetDRIn {
        private String fromTimeStamp;
        private String toTimeStamp;
        private String historyCategory;
        private List<TargetTableInfo> strTargetTableInfoSeq;
        private Long maxRecordCount;
        private SearchCondition searchCondition;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 16:34
     */
    @Data
    public static class LotProcessHistoryGetDROut {
        private List<TableRecordInfo> strTableRecordInfoSeq;
        private List<TableRecordValue> strTableRecordValueSeq;
        private Page<List<TableRecordValue>> strTableRecordValuePage;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 16:37
     */
    @Data
    public static class LotProcessHistoryGetDRIn {
        private String fromTimeStamp;
        private String toTimeStamp;
        private String historyCategory;
        private List<TargetTableInfo> strTargetTableInfoSeq;
        private Long maxRecordCount;
        private SearchCondition searchCondition;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 16:48
     */
    @Data
    public static class RecipeParameterAdjustHistoryGetDRIn {
        private String fromTimeStamp;
        private String toTimeStamp;
        private String historyCategory;
        private List<TargetTableInfo> strTargetTableInfoSeq;
        private Long maxRecordCount;
        private SearchCondition searchCondition;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 15:52
     */
    @Data
    public static class EquipmentHistoryGetDRIn {
        private String fromTimeStamp;
        private String toTimeStamp;
        private List<TargetTableInfo> strTargetTableInfoSeq;
        private Long maxRecordCount;
        private SearchCondition searchCondition;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 17:22
     */
    @Data
    public static class EquipmentMonitorJobLotHistoryGetDRIn {
        private String fromTimeStamp;
        private String toTimeStamp;
        private String historyCategory;
        private List<TargetTableInfo> strTargetTableInfoSeq;
        private Long maxRecordCount;
        private SearchCondition searchCondition;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/8/20                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/8/20 16:19
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class RunCardHistoryGetDRIn {
        private String fromTimeStamp;
        private String toTimeStamp;
        private String historyCategory;
        private List<TargetTableInfo> strTargetTableInfoSeq;
        private Long maxRecordCount;
        private SearchCondition searchCondition;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/13 15:16:27
     */
    @Data
    public static class EquipmentListInfoGetDRIn {
        private ObjectIdentifier workArea;
        private ObjectIdentifier equipmentID;
        private List<String> fpcCategories;
        private String stockerType;
        private String whiteDefSearchCriteria;
        private String specialControl;
        private Object siInfo;
    }

    @Data
    public static class EqpChamberStatusInfo {
        private ObjectIdentifier chamberID;                   //<i>Infos.Chamber ID
        private ObjectIdentifier chamberStatusCode;           //<i>Infos.Chamber State CimCode
        private ObjectIdentifier e10Status;                   //<i>E10 State CimCode
        private String chamberStatusName;           //<i>Infos.Chamber State Name
        private String chamberStatusDescription;    //<i>Cahmber State Description
        private String changeTimeStamp;             //<i>Change Timestamp
        private ObjectIdentifier changeUserID;                //<i>Change User ID
        private ObjectIdentifier actualStatusCode;            //<i>Actual State CimCode
        private ObjectIdentifier actualE10Status;             //<i>Actual E10 State CimCode
        private String actualStatusName;            //<i>Actual State Name
        private String actualStatusDescription;     //<i>Actual State Description
        private String actualChangeTimeStamp;       //<i>Actual Change Timestamp
        private boolean chamberAvailableFlag;        //<i>Infos.Chamber Available Flag
        private String contaminationList;            // chamber's contamination list
        private String siInfo;
    }

    @Data
    public static class EqpTopInfo {
        private ObjectIdentifier equipmentID;
        private String equipmentCategory;
        private List<String> specialControls;
    }

    @Data
    public static class EqpContainer {
        private ObjectIdentifier equipmentContainerID;
        private ObjectIdentifier chamberID;
        private Integer maxCapacity;
        private Integer maxRsvCount;
        private Long currentCapacity;
        private List<EqpContainerPosition> eqpContainerPosition;
        private Object siInfo;
    }

    @Data
    public static class AreaStocker {
        private ObjectIdentifier stockerID;
        private String description;
        private String stockerType;
        private ObjectIdentifier stockerStatus;
        private ObjectIdentifier e10Status;
        private Integer displayPositionX;
        private Integer displayPositionY;
        private Boolean utsFlag;
        private Boolean slmUTSFlag;
        private Integer maxUTSCapacity;
        private String siInfo;
    }

    @Data
    public static class AreaEqp {
        private ObjectIdentifier equipmentID;
        private String equipmentCategory;
        private String description;
        private String equipmentMode;
        private ObjectIdentifier equipmentStatusCode;
        private ObjectIdentifier e10Status;
        private Timestamp changeTimeStamp;
        private Timestamp actualChangeTimeStamp;
        private String changeUser;
        private ObjectIdentifier actualStatus;
        private String multiRecipeCapa;
        private Integer displayPositionX;
        private Integer displayPositionY;
        private Boolean availableStateFlag;
        private Boolean whiteDefFlag;
        private Boolean slmCapabilityFlag;
        private Integer wipLotCount;
        private String slmSwitch; //DB2是String来表示On Off, 我们改为Boolean同理
        private List<String> fpcCategories;
        private String siInfo;
    }

    @Data
    public static class EqpContainerInfo {
        private ObjectIdentifier equipmentID;
        private List<EqpContainer> eqpContainerList;
        private Object siInfo;
    }

    @Data
    public static class EqpContainerPosition {
        private ObjectIdentifier containerPositionID;        //<i>Container Position ID
        private ObjectIdentifier controlJobID;               //<i>Control Job ID
        private String processJobID;               //<i>Process Job ID
        private ObjectIdentifier lotID;                      //<i>lot ID
        private ObjectIdentifier waferID;                    //<i>wafer ID
        private ObjectIdentifier srcCassetteID;              //<i>Src cassette ID
        private ObjectIdentifier srcPortID;                  //<i>Src port ID
        private Integer srcSlotNo;                  //<i>Src Slot No
        private String fmcState;                   //<i>SLM State
        private ObjectIdentifier destCassetteID;             //<i>Dest cassette ID
        private ObjectIdentifier destPortID;                 //<i>Dest port ID
        private Integer destSlotNo;                 //<i>Dest Slot No
        private Timestamp estimatedProcessEndTime;    //<i>Estimated Process End Time
        private Timestamp processStartTime;           //<i>Process Start Time
        private Timestamp processCompleteTime;        //<i>Process Complete Time
        private Timestamp lastClaimedTimeStamp;       //<i>Last Claimed Time Stamp
        private ObjectIdentifier lastClaimedUserID;          //<i>Last Claimed User ID
        private Object siInfo;                     //<i>Reserved for SI customization
    }

    @Data
    public static class EqpInprocessingControlJob {
        private ObjectIdentifier controlJobID;               //<i>Control Job ID  //(R30)
        private List<EqpInprocessingLot> eqpInprocessingLotList;      //<i>Sequence of Eqp Inprocessing lot //(R30)
        private String siInfo;                     //<i>Reserved for SI customization   //D4200055
    }

    @Data
    public static class EqpInprocessingLot {
        /* (R30)    objectIdentifier                    controlJobID */
        private ObjectIdentifier lotID;                          //<i>lot ID
        private ObjectIdentifier loadPortID;                     //<i>Load port ID
        private ObjectIdentifier unloadPortID;                   //<i>Unload port ID
        private long unloadSequenceNumber;           //<i>Unload Sequence Number
        private ObjectIdentifier cassetteID;                     //<i>Carrier ID
        private boolean edcRequiredFlag;     //<i>Data Collection Required Flag //(R30)
        private String holdState;                    //<i>Hold State    //D4100079
        //<c>If SP_SQL_NOT_FOUND            ""
        //<c>CIMFW_Lot_HoldState_OnHold     "ONHOLD"
        //<c>CIMFW_Lot_HoldState_NotOnHold  "NOTONHOLD"
    }

    @Data
    public static class EqpMonitorID {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier chamberID;
        private ObjectIdentifier eqpMonitorID;
        private ObjectIdentifier eqpMonitorJobID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/2 17:52
     */
    @Data
    public static class DurableOperationStartEventMakeOpeStartIn {
        private ObjectIdentifier equipmentID;
        private String operationMode;
        private ObjectIdentifier durableControlJobID;
        private String durableCategory;
        private List<StartDurable> strStartDurables;
        private DurableStartRecipe strDurableStartRecipe;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class StrSLMMsgQueueRecord {
        private ObjectIdentifier equipmentID;
        private String eventName;
        private ObjectIdentifier portID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier controlJobID;
        private String messageID;
    }

    @Data
    public static class EqpMonitorJobLotInfo {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier chamberID;
        private ObjectIdentifier eqpMonitorID;
        private ObjectIdentifier eqpMonitorJobID;
        private ObjectIdentifier lotID;
        private String monitorLotStatus;
        private Long startSeqNo;
        private String monitorOpeKey;
        private ObjectIdentifier monitorRouteID;
        private String monitorOpeNo;
        private Boolean exitFlag;
        private Long result;
        private Object siInfo;
    }

    @Data
    public static class EqpMonitorLabelInfo {
        private String operationLabel;              //Label for eqp Monitor
        private boolean exitFlag;                   // exitOperationFlag
        private String monitorOperationPosObjRef;   // monitorProcessOperationSpecification
        private String monitorOperationKey;         // monitorModuleOperationNo
        private String customField;
    }

    @Data
    public static class EqpPMInfo {
        private Integer runWafer;                      //<i>Run wafer
        private Integer maximumRunWafer;               //<i>Maximum Run Wafers
        private String runTime;                       //<i>Run Time
        private String maximumRunTime;                //<i>Maximum Run Time
        private Integer moveInCount;           //<i>Operation Start Count
        private Integer maximumMoveInCount;    //<i>Maximum Operation Start Count
        private Integer pmIntervalTime;                //<i>Interval between PM Time
        private String lastMaintenanceTimeStamp;      //<i>Last Maintenance Timestamp
    }

    @Data
    public static class EqpPortInfo {
        /**
         * Sequence of eqp port Status
         */
        private List<EqpPortStatus> eqpPortStatuses;
    }

    @Data
    public static class EqpPortLotInfo {
        private ObjectIdentifier cassetteId;
        private String loadPurposeType;
        private ObjectIdentifier loadPortId;
        private ObjectIdentifier lotId;
        private boolean operationStartFlag;
        private boolean monitorLotFlag;
        private String lotType;
        private String subLotType;
    }

    @Data
    public static class EqpPortStatus {
        private ObjectIdentifier portID;                         //<i>port ID
        private ObjectIdentifier associatedPortID;               //<i>Associated port ID
        private String portGroup;                      //<i>port Group Name
        private Long loadSequenceNumber;             //<i>Load Sequence Number
        private String portUsage;                    //<i>port Usage
        // <c>CIMFW_PortRsc_Input        "INPUT"
        //<c>CIMFW_PortRsc_Output       "OUTPUT"
        //<c>CIMFW_PortRsc_InputOutput  "INPUT_OUTPUT"
        private String loadPurposeType;             //<i>Load Purpose Type
        //<c>SP_LoadPurposeType_Any                 "Any Purpose"
        //<c>SP_LoadPurposeType_Other               "Other"
        //<c>SP_LoadPurposeType_EmptyCassette       "Empty cassette"
        //<c>SP_LoadPurposeType_FillerDummy         "Filler Dummy lot"
        //<c>SP_LoadPurposeType_ProcessLot          "Process lot"
        //<c>SP_LoadPurposeType_ProcessMonitorLot   "Process Monitor lot"
        //<c>SP_LoadPurposeType_SideDummyLot        "Side Dummy lot"
        //<c>SP_LoadPurposeType_WaitingMonitorLot   "Waiting Monitor lot"
        //<c>SP_LoadPurposeType_InternalBuffer      "Internal Buffer Eqp"
        private ObjectIdentifier operationModeID;             //<i>Capable Operation Mode ID
        private String portState;                    //<i>port State
        //<c>SP_PortRsc_PortState_LoadAvail         "LoadAvail"
        //<c>SP_PortRsc_PortState_LoadReq           "LoadReq"
        //<c>SP_PortRsc_PortState_LoadComp          "LoadComp"
        //<c>SP_PortRsc_PortState_UnloadReq         "UnloadReq"
        //<c>SP_PortRsc_PortState_UnloadComp        "UnloadComp"
        //<c>SP_PortRsc_PortState_UnloadAvail       "UnloadAvail"
        //<c>SP_PortRsc_PortState_Unknown           "-"
        //<c>SP_PortRsc_PortState_UnknownForTCS     "Unknown"
        //<c>SP_PortRsc_PortState_Down              "Down"
        //objectIdentifier                        loadResrvedCassetteID;
        private ObjectIdentifier loadResrvedCassetteID;      //<i>Load Reserved Carrier ID  //R30 DCR3000096
        private String dispatchState;               //<i>dispatch State
        //<c>SP_PortRsc_DispatchState_Required          "Required"
        //<c>SP_PortRsc_DispatchState_Dispatched        "Dispatched"
        //<c>SP_PortRsc_DispatchState_NotDispatched     "NotDispatched"
        //<c>SP_PortRsc_DispatchState_Error             "Error"
        private String dispatchStateTimeStamp;        //<i>dispatch State Timestamp
        //(R30)  objectIdentifier                        dispatchedCassetteID;
        private ObjectIdentifier loadedCassetteID;               //<i>Loaded Carrier ID
        private ObjectIdentifier dispatchLoadCassetteID;         //<i>dispatch Load Carrier ID  //(R30)
        private ObjectIdentifier dispatchUnloadCassetteID;       //<i>dispatch Unload Carrier ID    //(R30)
        private String cassetteLoadPurposeType;        //<i>cassette Load Purpose Type (reference loadPurposeType)
        private ObjectIdentifier cassetteControlJobID;           //<i>cassette Control Job ID
        private String operationMode;                  //<i>Capable Operation Mode ID   //(R30)  //【bear】delete the redundant data
        private String onlineMode;                   //<i>Online mode    //(R30)
        //<c>SP_Eqp_OnlineMode_Offline          "Off-Line"
        //<c>SP_Eqp_OnlineMode_OnlineLocal      "On-Line Local"
        //<c>SP_Eqp_OnlineMode_OnlineRemote     "On-Line Remote"
        private String dispatchMode;                 //<i>dispatch Mode  //(R30)
        //<c>SP_Eqp_DispatchMode_Manual     "Manual"
        //<c>SP_Eqp_DispatchMode_Auto       "Auto"
        private String accessMode;                   //<i>Access Mode    //(R30)
        //<c>SP_Eqp_AccessMode_Manual       "Manual"
        //<c>SP_Eqp_AccessMode_Auto         "Auto"
        private String moveInMode;          //<i>Operation Start Mode   //(R30)
        //<c>SP_Eqp_StartMode_Manual        "Manual"
        //<c>SP_Eqp_StartMode_Auto          "Auto"
        private String moveOutMode;           //<i>Operation Comp Mode    //(R30)
        //<c>SP_Eqp_CompMode_Manual         "Manual"
        //<c>SP_Eqp_CompMode_Auto           "Auto"
        private List<LotOnPort> lotOnPorts;             //<i>Sequence of lot On port List
        private List<String> cassetteCategoryCapability; //<i>Sequence of Carrier Type Capability List  //D4000016
        private String waferBondingPortType;
    }

    @Data
    public static class EqpReservedControlJobInfo {
        private List<StartReservedControlJobInfo> moveInReservedControlJobInfoList;       //<i>Sequence of Start Reserved Control Job
    }

    @Data
    public static class EqpStatusInfo {
        private ObjectIdentifier equipmentStatusCode;           //<i>eqp State CimCode
        private String e10Status;                     //<i>E10 State CimCode
        private String equipmentStatusName;           //<i>eqp State Name
        private String equipmentStatusDescription;    //<i>eqp State Description
        private String changeTimeStamp;               //<i>Change Timestamp
        private ObjectIdentifier changeUserID;                  //<i>Change User ID
        private ObjectIdentifier actualStatusCode;              //<i>Actual State CimCode
        private String actualE10Status;               //<i>Actual E10 State CimCode
        private String actualStatusName;              //<i>Actual State Name
        private String actualStatusDescription;       //<i>Actual State Description
        private String actualChangeTimeStamp;         //<i>Actual Change Timestamp
        private Boolean equipmentAvailableFlag;        //<i>eqp Available Flag
        private ObjectIdentifier lastRecipeID;                  //<i>Last Recipe ID
        private Integer maxCountForFlowBatch;          //<i>Max count for flowbatch
        private ObjectIdentifier reservedFlowBatchID;           //<i>Reserved Flow Batch ID
        private List<ObjectIdentifier> reservedControlJobID;     //<i>Sequence of Reserved Control Job IDs
        private List<ObjectIdentifier> reservedFlowBatchIDs;   //<i>Sequence of Reserved Flow Batch ID
        private boolean isStorageBool;
    }

    @Data
    public static class EqpStockerInfo {
        private List<EqpStockerStatus> eqpStockerStatusList;    //<i>Sequence of eqp stocker Status
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/6 10:25
     */
    @Deprecated
    @Data
    public static class LotOperationCompleteEventRecord extends EventRecord {
        private LotEventData lotData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<String> reticleIDs;
        private List<String> fixtureIDs;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Integer previousOperationPassCount;
        private String previousObjrefPOS;
        private String batchID;
        private String controlJobID;
        private Boolean locateBackFlag;
        private Boolean testCriteriaResult;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private List<WaferLevelRecipeEventData> waferLevelRecipe;
        private List<WaferPassCountNoPreviousEventData> processWafers;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/12 13:35
     */
    @Deprecated
    @Data
    public static class WaferChamberProcessEventRecord extends EventRecord {
        private String waferID;
        private String lotID;
        private String routeID;
        private String opeNo;
        private Long passCount;
        private String equipmentID;
        private String processResourceID;
        private String processTime;
        private String actionCode;
        private String controlJobID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/6 10:26
     */
    @Deprecated
    @Data
    public static class WaferPassCountNoPreviousEventData {
        private String waferID;
        private Boolean currentOperationFlag;
        private Integer passCount;
    }

    @Data
    public static class EqpStockerStatus {
        private ObjectIdentifier stockerID;        //<i>stocker ID
        private String stockerType;      //<i>stocker Type
        //<c>SP_Stocker_Type_Auto           "Auto"
        //<c>SP_Stocker_Type_Interm         "Interm"
        //<c>SP_Stocker_Type_Shelf          "Shelf"
        //<c>SP_Stocker_Type_Reticle        "Reticle"
        //<c>SP_Stocker_Type_Fixture        "Fixture"
        //<c>SP_Stocker_Type_InterBay       "Inter Bay"
        //<c>SP_Stocker_Type_IntraBay       "Intra Bay"
        //<c>SP_Stocker_Type_ReticleShelf   "ReticleShelf"
        //<c>SP_Stocker_Type_ReticlePod     "reticlepod"
        //<c>SP_Stocker_Type_BareReticle    "BareReticle"
        private ObjectIdentifier stockerStatus;       //<i>stocker State
        private String stockerPriority;    //<i>stocker Priority
        private String e10Status;          //<i>stocker E10 status         //D8000028
        private boolean ohbFlag;            //<i>UTS Flag                   //D8000028
        private long maxOHBFlag;     //<i>Max UTS Capacity           //D8000028
        private String siInfo;             //<i>Reserved for SI customization
    }

    @Data
    public static class EquipmentAdditionalReticleAttribute {
        private long reticleStoreMaxCount;       //<i>Reticle Store Max Count
        private long reticleStoreLimitCount;     //<i>Reticle Sotre Limit Count
        private List<ReticlePodPortInfo> reticlePodPortIDList;          //<i>Sequence of Reticle Pod Information
        private List<StoredReticle> storedReticleList;             //<i>Sequence of Stored Reticle
    }

    @Data
    public static class EquipmentLoadPortAttribute {
        private ObjectIdentifier equipmentID;
        private List<CassetteLoadPort> cassetteLoadPortList;
        private String reserve;                   //<i>Reserved for SI customization
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/2 15:47:50
     */
    @Data
    public static class EquipmentShelfSpaceForInternalBufferGet {
        private Long emptyCassetteSpace;
        private Long fillerDummyLotSpace;
        private Long processLotSpace;
        private Long processMonitorLotSpace;
        private Long sideDummyLotSpace;
        private Long waitingMonitorLotSpace;
        //add other space
        private Long otherSpace;
        private String siInfo;
    }

    @Deprecated
    @Data
    public static class EventData {
        private String transactionID;
        private Timestamp eventTimeStamp;
        private Double eventShopDate;
        private ObjectIdentifier userID;
        private String eventMemo;
        private Timestamp eventCreatimeTimeStamp;

        public EventData() {
            this.setEventCreatimeTimeStamp(BaseStaticMethod.getCurrentTimeStamp());
        }

        public EventData(ObjCommon objCommon, String transactionID, String claimMemo) {
            this.transactionID = transactionID;
            this.eventTimeStamp = objCommon.getTimeStamp().getReportTimeStamp();
            this.eventShopDate = objCommon.getTimeStamp().getReportShopDate();
            this.userID = objCommon.getUser().getUserID();
            this.eventMemo = claimMemo;
            this.setEventCreatimeTimeStamp(BaseStaticMethod.getCurrentTimeStamp());
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/20 13:48
     */
    @Data
    public static class ProcessCollectedDataUpdateIn {
        private ObjectIdentifier lotID;
        private ObjectIdentifier controlJobID;
        private List<DCDef> strDCDef;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    @NoArgsConstructor
    public static class EventParameter {
        private String parameterName;     //<i>Parameter Name
        private String parameterValue;    //<i>Parameter Value
        private Object reserve;             //<i>Reserved for SI customization

        public EventParameter(String name, String value) {
            parameterName = name;
            parameterValue = value;
        }
    }

    @Deprecated
    @Data
    public static abstract class EventRecord {
        private EventData eventCommon;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/2 13:14
     */
    @Data
    public static class DurableOperationCompleteEventMakeOpeCompIn {
        private ObjectIdentifier equipmentID;
        private String operationMode;
        private ObjectIdentifier durableControlJobID;
        private String durableCategory;
        private List<StartDurable> strStartDurables;
        private DurableStartRecipe strDurableStartRecipe;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class FlowBatchControl {
        private String name;
        private Integer operationCount;
        private Integer maxLotSize;
        private Integer minLotSize;
        private Integer minWaferSize;
        private Double offsetTime;
    }

    @Data
    public static class PosFlowBatchControl {
        private String name;
        private Long size;
        private Long minimumSize;
        private boolean targetOperation;
        private Double flowBatchingOffsetTime;
        private Long minWaferCount;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 15:34
     */
    @Data
    public static class DurableHistoryGetDROut {
        private List<TableRecordInfo> strTableRecordInfoSeq;
        private List<TableRecordValue> strTableRecordValueSeq;
        private Page<List<TableRecordValue>> strTableRecordValuePage;
        private Object siInfo;
    }

    @Data
    public static class ReticleHistoryGetDROut {
        private List<TableRecordInfo> strTableRecordInfoSeq;
        private List<TableRecordValue> strTableRecordValueSeq;
        private Page<List<TableRecordValue>> strTableRecordValuePage;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/4/29 15:37
     */
    @Data
    public static class DurableHistoryGetDRIn {
        private String fromTimeStamp;
        private String toTimeStamp;
        private List<TargetTableInfo> strTargetTableInfoSeq;
        private SearchCondition searchCondition;
        private Long maxRecordCount;
        private Object siInfo;
    }

    @Data
    public static class ReticleHistoryGetDRIn {
        private String fromTimeStamp;
        private String toTimeStamp;
        private List<TargetTableInfo> strTargetTableInfoSeq;
        private Long maxRecordCount;
        private SearchCondition searchCondition;
        private Object siInfo;
    }

    @Data
    public static class FlowBatchLotInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier carrierID;
        private String subLotType;
        private String lotStatus;
        private String priorityClass;
        private ObjectIdentifier productID;
        private FlowBatchControl flowBatchControl;
        private List<FlowBatchSection> flowBatchSectionList;
    }

    @Data
    public static class FlowBatchSection {
        private ObjectIdentifier operationID;
        private String operationNumber;    //D5000154
        private Boolean entryOperationFlag;
        private Boolean targetOperationFlag;
    }

    @Data
    public static class FoundCassette {
        private ObjectIdentifier cassetteID;
        private String description;
        private String cassetteCategory;
        private String carrierType;
        private Boolean emptyFlag;
        private String zoneType;
        private String multiLotType;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private String cassetteStatus;
        private ObjectIdentifier durableSubStatus;    // durable Sub Status
        private Boolean availableForDurableFlag;    // Process Available for durable Flag
        private String transferStatus;
        private Boolean usageCheckFlag;

        private String maximumRunTime;
        private Double maximumOperationStartCount;
        private Integer intervalBetweenPM;
        private Integer capacity;
        private Integer nominalSize;

        private String contents;
        private String instanceName;
        private Boolean currentLocationFlag;
        private String backupState;
        private Boolean sorterJobExistFlag;
        private Boolean inPostProcessFlagOfCassette;
        private ObjectIdentifier slmReservedEquipmentID;
        private String interFabTransferState;           // interFab Xfer State
        private String productUsage;
        private ObjectIdentifier durableControlJobID;
        private Boolean durablesSTBFlag;
        private ObjectIdentifier bankID;
        private String dueTimeStamp; // Due Infos.TimeStamp
        private ObjectIdentifier startBankID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private Boolean bankInRequiredFlag; // BankIn Required Flag
        private List<HashedInfo> durableStatusList;  //<i>The following status are set.
        //<c>[0]:"durable Representative State"
        //<c>[1]:"durable State"
        //<c>[2]:"durable Production State"
        //<c>[3]:"durable Hold State"
        //<c>[4]:"durable Finished State"
        //<c>[5]:"durable Process State"
        //<c>[6]:"durable Inventory State"
        private ObjectIdentifier holdReasonCodeID;    // Hold Reason CimCode ID
        private Boolean relationFoupFlag;//千岛二期增加

    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/19 09:59:00
     */
    @Data
    public static class PosDurableHoldRecord {
        private String holdType;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier holdPerson;
        private String holdTimeStamp;
        private ObjectIdentifier relatedDurable;
        private Boolean responsibleOperationFlag;
        private ObjectIdentifier responsibleRouteID;
        private String responsibleOperationNumber;
        private String responsibleOperationName;
        private String holdClaimMemo;
    }

    @Data
    public static class DurableHoldRecord {
        private String d_key;
        private String holdType;
        private ObjectIdentifier holdReasonCodeID;
        private ObjectIdentifier holdUserID;
        private String holdTime;
        private String responsibleOperationMark;
        private ObjectIdentifier relatedDurableID;
        private String claimMemo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/22 11:16:29
     */
    @Data
    public static class ReticleBrInfo {
        private String description;
        private String reticlePartNumber;
        private String reticleSerialNumber;
        private String supplierName;
        private Boolean usageCheckFlag;
        private ObjectIdentifier reticleGroupID;
        private String reticleGroupDescription;
        private String siInfo;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/22 11:20:23
     */
    @Data
    public static class ReticlePmInfo {
        private Long runTimeStamp;
        private Long maximumRunTimeStamp;
        private Integer operationStartCount;
        private Integer maximumOperationStartCount;
        private Long passageTimeFromLastPM;
        private Integer intervalBetweenPM;
        private Timestamp lastMaintenanceTimeStamp;
        private ObjectIdentifier lastMaintenancePerson;
        private String siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/29 10:52:09
     */
    @Data
    public static class XferReticle {
        private ObjectIdentifier reticleID;
        private String transferStatus;
        private Timestamp transferStatusChangeTimeStamp;
        private String siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/29 15:34:22
     */
    @Data
    public static class DurableControlJobStatusGet {
        private String durableControlJobStatus;
        private ObjectIdentifier lastClaimedUserID;
        private Timestamp lastClaimedTimeStamp;
        private String siInfo;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/29 14:04:08
     */
    @Data
    public static class ReticleChangeTransportState {
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        List<XferReticle> strXferReticle;
        private String siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/12/5 14:04:18
     */
    @Data
    public static class ProcessOriginalOperationGetOut {
        private Long branchNestLevel;
        private String originalMainPDID;
        private String originalOpeNo;
        private String subOrigMainPDID;
        private String subOrigOpeNo;
        private String branchMainPDID;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/12/5 14:21:49
     */
    @Data
    public static class ProcHoldListAttributes {
        private Boolean cancelableFlag;
        private Boolean withExecHoldFlag;
        private ObjectIdentifier routeID;
        private ObjectIdentifier productID;
        private ObjectIdentifier userID;
        private ObjectIdentifier reasonCodeID;
        private String holdType;
        private String operationNumber;
        private String operationName;
        private String userName;
        private String reasonCodeDescription;
        private String reportTimeStamp;
        private String claimMemo;
        private Object siInfo;
		private String department;
		private String section;
    }

    @Data
    public static class FoundReticle {
        private ObjectIdentifier reticleID;                     //<i>Reticle ID
        private ObjectIdentifier bankID;
        private ObjectIdentifier startBankID;
        private ObjectIdentifier holdReasonCodeID;
        private ObjectIdentifier routeID;
        private Timestamp dueTimeStamp;
        private Timestamp storeTimeStamp;
        private Timestamp lastUsedTimeStamp;
        private String description;                   //<i>Description
        private String operationNumber;
        private String reticleLocation;
        private ObjectIdentifier reticleGroupID;                //<i>Reticle Group ID
        private long reticleGroupSequenceNumber;    //<i>Reticle Group Sequence Number
        private String reticlePartNumber;             //<i>Reticle Part Number
        private String reticleSerialNumber;           //<i>Reticle Serial Number
        private Boolean whiteDefFlag;                  //<i>White Definition Flag     //D8000024
        private Boolean bankInRequiredFlag;
        private String fpcCategory;                   //<i>FPC Category              //D8000024
        private ReticleStatusInfo reticleStatusInfo;             //<i>Reticle Status Information
    }

    @Data
    public static class FPCInfo {
        private String fpcID;   // FPC_ID(LotFamilyID + Infos.TimeStamp)
        private ObjectIdentifier lotFamilyID;
        private ObjectIdentifier mainProcessDefinitionID;
        private String operationNumber;
        private String originalOperationNumber;
        private ObjectIdentifier originalMainProcessDefinitionID;
        private ObjectIdentifier subMainProcessDefinitionID;
        private String subOperationNumber;
        private Integer fpcGroupNumber;
        private String fpcType;         // SP_FPCTypeByLot or SP_FPCType_ByWafer

        private ObjectIdentifier mergeMainProcessDefinitionID;
        private String mergeOperationNumber;
        private String fpcCategory;
        private ObjectIdentifier processDefinitionID;
        private String processDefinitionType;
        private boolean skipFalg;
        private String correspondOperationNumber;
        private boolean restrictEquipmentFlag;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier machineRecipeID;
        private ObjectIdentifier dcDefineID;
        private ObjectIdentifier dcSpecID;
        private boolean sendEmailFlag;
        private boolean holdLotFlag;
        private String recipeParameterChangeType;       // SP_Rparm_ChangeType_ByLot or SP_Rparm_ChangeType_ByWafer

        private List<LotWaferInfo> lotWaferInfoList;
        private List<ReticleInfo> reticleInfoList;
        private List<DCSpecDetailInfo> dcSpecList;         // sequence of DC Spec Item

        private String description;         // FPC description
        private String createTime;
        private String updateTime;
        private String claimUserID;
        private List<CorrespondingOperationInfo> correspondingOperationInfoList;

        //added field for runCard
        private Boolean runCardFlag;
        private String psmKey;//一个PSM可以创建多个同样的OpeNumber,此字段为了区分唯一的psm下的数据,ex:DEV_BRCH.01/./1000.0200/./[NP000012.00A.01, NP000012.00A.02, NP000012.00A.03]
    }

    @Data
    public static class FPCProcessCondition {
        private ObjectIdentifier objectID;
        private String objectType;
        private List<String> fpcCategories;   // Sequence of FPC Category
        private boolean whiteDefFlag;   // white definition flag

        public FPCProcessCondition() {
        }

        public FPCProcessCondition(ObjectIdentifier objectID, String objectType) {
            this.objectID = objectID;
            this.objectType = objectType;
        }
    }

    @Data
    public static class HashedInfo {
        private String hashKey;
        private String hashData;

        public HashedInfo() {
        }

        public HashedInfo(String hashKey, String hashData) {
            this.hashKey = hashKey;
            this.hashData = hashData;
        }
    }

    @Data
    public static class HoldHistory {
        private Boolean movementFlag;
        private Boolean changeStateFlag;
        private String holdType;
        private ObjectIdentifier holdReasonCode;
        private ObjectIdentifier holdPerson;
        private String holdTime;
        private Boolean responsibleOperationFlag;
        private Boolean responsibleOperationExistFlag;    //P5100043
        private String holdClaimMemo;
        private ObjectIdentifier releaseReasonCode;
        private ObjectIdentifier releasePerson;
        private Timestamp releaseTime;
        private String releaseClaimMemo;
        private String siInfo;
        private String departmentNamePlate;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/7/19 14:20
     */
    @Data
    public static class MMQTIME {
        private String lotID;

        private String lotObj;

        private String qrestObj;

        private Timestamp minTargetTime;

        private Timestamp maxTargetTime;

        private Timestamp triggerTime;

        private String minRemainTime;

        private String maxRemainTime;

        private String posID;

        private String posObj;

        private String targetOperationNumber;

        private String maxmes;

        private String minmes;

        private String triggerOperationNumber;

        private Integer exptimes;

        private Boolean maxFlag;

        private String qtimeType;

        private String qtimeName;
    }

    @Data
    public static class LoadingVerifiedLot {
        private Boolean moveInFlag;     //<i>Operation Start Flag. TRUE means this lot is an object of operation start.
        private Boolean monitorLotFlag;         //<i>Monitor lot Flag. TRUE means this lot is representative lot of monitor group.
        private ObjectIdentifier lotID;                  //<i>lot ID
        private String verifyNGReason;         //<i>Verify NG Reason  //D4000017
    }

    @Data
    public static class LotAutoDispatchControlInfo {
        private ObjectIdentifier lotID;                    //<i>lot ID
        private ObjectIdentifier routeID;                  //<i>Route ID
        private String operationNumber;          //<i>Operation Number
        private boolean singleTriggerFlag;        //<i>Single Trigger Flag
        private String description;              //<i>Description
        private ObjectIdentifier updateUserID;             //<i>Update User ID
        private String updateTimeStamp;          //<i>Update Time Stamp
        private Object siInfo;                   //<i>Reserved for SI customization
    }

    @Data
    public static class LotAutoDispatchControlUpdateInfo {
        private ObjectIdentifier lotID;
        private List<Infos.AutoDispatchControlUpdateInfo> autoDispatchControlUpdateInfoList;
    }

    @Data
    public static class AutoDispatchControlUpdateInfo {
        private String updateMode;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private boolean singleTriggerFlag;
        private String description;
    }

    @Data
    public static class AutoDispatchControlRecord {
        private ObjectIdentifier routeID;
        private String operationNumber;
        private boolean singleTriggerFlag;
        private String description;
        private ObjectIdentifier updateUserID;
        private String updateTimeStamp;
    }

    @Deprecated
    @Data
    public static class AutoDispatchControlEventRecord extends EventRecord {
        private String lotID;
        private String action;
        private String routeID;
        private String operationNumber;
        private boolean singleTriggerFlag;
        private String description;
    }

    @Data
    public static class LotBackupData {
        private String backupState;       //<i>Backup State
        private BackupAddress backupAddress;  //<i>Backup Address Information
        private BackupProcess backupProcess;  //<i>Backup Process Information
        private Object reserve; //<i>Reserved for SI customization
    }

    @Data
    public static class PosLotBackupData {
        private String backupState;
        private String hostName;
        private String serverName;
        private String itDaemonPort;
        private String entryRouteID;
        private String entryOperationNumber;
        private String exitRouteID;
        private String exitOperationNumber;
    }

    @Data
    public static class LotBackupInfo {
        private Boolean backupProcessingFlag;                       //<i>Backup Processing Flag. If during a Backup Operation, True.
        private Boolean currentLocationFlag;                        //<i>Current Location Flag. If on the line of own Server, True.
        private Boolean transferFlag;                               //<i>Transfer Flag. If transferring between sites, True.
        private BackupAddress bornSiteAddress;                   //<i>Born Site Address Information
        private List<LotBackupData> lotBackupSourceDataList;     //<i>Sequence of Backup Source Data Information
        private List<LotBackupData> lotBackupDestDataList;    //<i>Sequence of Backup Destination Data Information
        private ObjectIdentifier returnRouteID;             //<i>Return Route ID          //D4200252
        private String returnOperationNumber;     //<i>Return Operation Number  //D4200252
        private Object reserve;
    }

    @Data
    public static class LotBasicInfo {
        private ObjectIdentifier lotID;
        private String lotType;
        private String subLotType;
        private String lotContent;
        private String lotStatus;
        private List<LotStatusList> lotStatusList;
        private Timestamp dueTimeStamp;
        private Integer priorityClass;
        private Double internalPriority;
        private int externalPriority;
        private Integer totalWaferCount;
        private Integer productWaferCount;
        private Integer controlWaferCount;
        private Integer totalGoodDieCount;
        private Integer totalBadDieCount;
        private ObjectIdentifier bankID;
        private Boolean qtimeFlag;
        private boolean minQTimeFlag;
        private Timestamp processLagTime;
        private ObjectIdentifier parentLotID;
        private String vendorLotID;
        private ObjectIdentifier familyLotID;
        private Timestamp lastClaimedTimeStamp;
        private ObjectIdentifier lastClaimedUserID;
        private String stateChangeTimeStamp;
        private Timestamp inventoryChangeTimeStamp;
        private String requiredCassetteCategory;
        private Boolean sorterJobExistFlag;
        private Boolean inPostProcessFlagOfCassette;
        private Boolean inPostProcessFlagOfLot;
        private String interFabXferState;
        private String bondingGroupID;
        private Boolean autoDispatchControlFlag;
        private EqpMonitorID eqpMonitorID;
        private String contaminationLevel;
        private Boolean prFlag;
        private String requiredContaminationLevel;
        private String dispatchReadiness;
        private boolean flip;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @return
     * @exception
     * @date 2019/8/1 12:15
     */
    @Data
    public static class DurablebankmoveeventMakeIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private String transactionID;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class LotControlJobInfo {
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier processReserveEquipmentID;
        private ObjectIdentifier processReserveUserID;
    }

    @Data
    public static class LotControlUseInfo {
        private Integer usedCount;
        private Integer recycleCount;
        private Integer usageLimit;
        private Integer recycleLimit;
        private String controlUseState;
    }

    @Data
    public static class LotEquipmentList {
        private ObjectIdentifier equipmentID;
        private String equipmentName;

        public LotEquipmentList() {
        }

        public LotEquipmentList(ObjectIdentifier equipmentID, String equipmentName) {
            this.equipmentID = equipmentID;
            this.equipmentName = equipmentName;
        }
    }

    @Data
    public static class SimpleOperationInfo {
        private ObjectIdentifier routeID;    //<i>Route ID
        private String opeNo;      //<i>Operation Number
    }


    @Deprecated
    @Data
    public static class LotEventData {
        private String lotID;
        private String lotType;
        private String cassetteID;
        private String lotStatus;
        private String customerID;
        private Long priorityClass;
        private String productID;
        private Long originalWaferQuantity;
        private Long currentWaferQuantity;
        private Long productWaferQuantity;
        private Long controlWaferQuantity;
        private String holdState;
        private String bankID;
        private String routeID;
        private String operationNumber;
        private String operationID;
        private Long operationPassCount;
        private String objrefPOS;
        private String waferHistoryTimeStamp;
        private String objrefPO;
        private String objrefMainPF;
        private String objrefModulePOS;
        //private stringSequence samplingWafers;
        private List<String> samplingWafers;
    }

    @Data
    public static class LotFlowBatchInfo {
        private ObjectIdentifier flowBatchID;
        private ObjectIdentifier flowBatchReserveEquipmentID;
    }

    @Data
    public static class LotHoldReq {
        private String holdType;
        private ObjectIdentifier holdReasonCodeID;
        private ObjectIdentifier holdUserID;
        private String responsibleOperationMark;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier relatedLotID;
        private String claimMemo;
        private String department;
        private String section;
		// edit lot hold
		private ObjectIdentifier oldHoldReasonCodeID;
		private ObjectIdentifier oldHoldUserID;
		private ObjectIdentifier oldRelatedLotID;
        private String oldDepartment;
		private String oldSection;
    }

    @Data
    public static class LotInCassette {
        private Boolean moveInFlag;
        private Boolean monitorLotFlag;
        private ObjectIdentifier lotID;
        private String lotType;
        private String subLotType;
        private StartRecipe startRecipe;
        private String recipeParameterChangeType;
        private List<LotWafer> lotWaferList;
        private ObjectIdentifier productID;
        private StartOperationInfo startOperationInfo;
        //qiandao project apc requirment add technologyID and productGroupID
        private ObjectIdentifier technologyID;
        private ObjectIdentifier productGroupID;
        private String furnaceSpecificControl;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/1 13:48:55
     */
    @Data
    public static class MonRelatedProdLots {
        private ObjectIdentifier productLotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private String siInfo;
    }

    @Data
    public static class LotInfo {
        private LotBasicInfo lotBasicInfo;
        private LotControlUseInfo lotControlUseInfo;
        private LotFlowBatchInfo lotFlowBatchInfo;
        private LotNoteFlagInfo lotNoteFlagInfo;
        private LotOperationInfo lotOperationInfo;
        private LotOrderInfo lotOrderInfo;
        private LotControlJobInfo lotControlJobInfo;
        private LotProductInfo lotProductInfo;
        private LotRecipeInfo lotRecipeInfo;
        private LotLocationInfo lotLocationInfo;
        private LotWipOperationInfo lotWipOperationInfo;
        private List<LotWaferAttributes> lotWaferAttributesList;
        private List<EntityInhibitAttributes> entityInhibitAttributesList;
        private LotBackupInfo lotBackupInfo;
        private Object reserve;
    }

    @Data
    public static class LotInfoInqFlag {
        private Boolean lotBasicInfoFlag;
        private Boolean lotControlUseInfoFlag;
        private Boolean lotFlowBatchInfoFlag;
        private Boolean lotNoteFlagInfoFlag;
        private Boolean lotOperationInfoFlag;
        private Boolean lotOrderInfoFlag;
        private Boolean lotControlJobInfoFlag;
        private Boolean lotProductInfoFlag;
        private Boolean lotRecipeInfoFlag;
        private Boolean lotLocationInfoFlag;
        private Boolean lotWipOperationInfoFlag;
        private Boolean lotWaferAttributesFlag;
        private Boolean lotBackupInfoFlag;
        private Boolean lotListInCassetteInfoFlag;
        private Boolean lotWaferMapInCassetteInfoFlag;
    }

    @Data
    public static class LotInfoOnPort {
        private ObjectIdentifier lotID;    //lot ID
        private String lotType;  //lot Type
    }

    @Data
    public static class LotStatusInformation {
        private String lotStatus;
        private String planStartTimeStamp;
        private Boolean lotInfoChangeFlag;
    }

    @Data
    @NoArgsConstructor
    public static class LotListAttributes {
        private ObjectIdentifier lotID;                                 //<i>lot ID
        private String lotType;                               //<i>lot Type
        private String lotStatus;                             //<i>lot Status
        private List<LotStatusList> lotStatusList;                         //<i>Sequence of lot Status List
        private ObjectIdentifier bankID;                                //<i>bank ID
        private String orderNumber;                           //<i>Order Number
        private String customerCode;                          //<i>Customer CimCode
        private ObjectIdentifier productID;                             //<i>Product ID
        private String lastClaimedTimeStamp;                  //<i>Last Claimed Time Stamp
        private String dueTimeStamp;                          //<i>Due Time Stamp
        private ObjectIdentifier routeID;                               //<i>Route ID
        private String operationNumber;                       //<i>Operation Number
        private Integer totalWaferCount;                       //<i>Total wafer Count
        private Boolean bankInRequiredFlag;                    //<i>bank In Required Flag
        private String controlUseState;                       //<i>Control Use State
        private Integer usedCount;                             //<i>Used Count
        private String completionTimeStamp;                   //<i>Completion Time Stamp
        private ObjectIdentifier lotFamilyID;                           //<i>lot Family ID
        private ObjectIdentifier productRequestID;                      //<i>Product Request ID
        private String subLotType;                            //<i>Sub lot Type
        private ObjectIdentifier lotOwnerID;                            //<i>lot Owner ID
        private String requiredCassetteCategory;              //<i>Required Carrier Type
        private LotBackupInfo lotBackupInfo;                         //<i>lot Backup Info
        private ObjectIdentifier carrierID;                             //<i>Carrier ID
        private ObjectIdentifier equipmentID;                           //<i>eqp ID
        private ObjectIdentifier holdReasonCodeID;                      //<i>Hold Reason CimCode ID
        private Boolean sorterJobExistFlag;                    //<i>Sorter Job Existence Flag
        private Boolean inPostProcessFlagOfCassette;           //<i>InPostProcessFlag of cassette
        private Boolean inPostProcessFlagOfLot;                //<i>InPostProcessFlag of lot
        private String interFabXferState;                     //<i>interFab Xfer State
        private String bondingGroupID;                        //<i>Bonding Group ID
        private Boolean autoDispatchControlFlag;               //<i>Auto dispatch Control Flag
        private ObjectIdentifier eqpMonitorJobID;                       //<i>eqp Monitor Job ID
        private ObjectIdentifier operationID;                           //<i>Operation ID
        private String pdType;                                //<i>Process Definition Type
        private String scheduleMode;                          //<i>PD Type
        private String planStartTimeStamp;                    //<i>Plan Start Time
        private Integer priorityClass;                         //<i>Priority Class
        private Boolean lotInfoChangeFlag;                     //<i>lot Info Change Flag
        private String lotQtimeState;                     //<i>lot qtime state
        private String qtimeRemainTime;
        private String carrierCategory;
        private String carrierType;
        private Boolean flip;                             //千岛二期增加



        public LotListAttributes(CimLotDO lot) {
            this.flip=lot.getFlip() == null ? false : lot.getFlip();
            this.lotID = new ObjectIdentifier(lot.getLotID(), lot.getId());
            this.lotType = lot.getLotType();
            this.bankID = new ObjectIdentifier(lot.getBankID(), lot.getBankObj());
            this.orderNumber = lot.getOrderNumber();
            String lastClaimedTimeStamp = (lot.getLastClamiedTimeStamp() != null) ? (lot.getLastClamiedTimeStamp().toString()) : BizConstant.EMPTY;
            this.lastClaimedTimeStamp = lastClaimedTimeStamp;
            String dueTimeStamp = (lot.getPlanEndTimeStamp() != null) ? (lot.getPlanEndTimeStamp().toString()) : BizConstant.EMPTY;
            this.dueTimeStamp = dueTimeStamp;
            this.lotStatus = lot.getLotState();
            this.customerCode = lot.getCustomerID();                                  //customerCode = customerID
            this.productID = new ObjectIdentifier(lot.getProductSpecificationID(), lot.getProductSpecificationObj());   // product ID = product request specification ID = product specification ID
            this.routeID = new ObjectIdentifier(lot.getRouteID(), lot.getRouteObj());
            this.operationNumber = lot.getOperationNumber();
            this.totalWaferCount = lot.getWaferCount();
            this.bankInRequiredFlag = lot.getBankInReqired();
            this.controlUseState = lot.getControlUseState();
            this.usedCount = lot.getUsedCount();
            Timestamp completionTimeStamp = lot.getCompletionTimeStamp();
            this.completionTimeStamp = null == completionTimeStamp ? null : completionTimeStamp.toString();
            this.lotFamilyID = new ObjectIdentifier(lot.getLotFamilyID(), lot.getLotFamilyReferenceKey());
            this.productRequestID = new ObjectIdentifier(lot.getProductRequestID(), lot.getProductRequestObj());
            this.subLotType = lot.getSubLotType();
            this.lotOwnerID = new ObjectIdentifier(lot.getLotOwnerID(), lot.getLotOwnerIDObj());
            this.requiredCassetteCategory = lot.getRequiredCassetteCategory();

            Infos.LotBackupInfo lotBackupInfo = new Infos.LotBackupInfo();
            lotBackupInfo.setBackupProcessingFlag(lot.getBackupProcessingFlag());
            lotBackupInfo.setCurrentLocationFlag(lot.getCurrentLocationFlag());
            lotBackupInfo.setTransferFlag(lot.getTransferFlag());
            Infos.BackupAddress backupAddress = new Infos.BackupAddress();
            backupAddress.setHostName(lot.getHostName());
            lotBackupInfo.setBornSiteAddress(backupAddress);
            this.lotBackupInfo = lotBackupInfo;
        }
    }

    @Data
    public static class LotListInCassetteInfo {
        private ObjectIdentifier cassetteID;
        private List<ObjectIdentifier> lotIDList;
        private String multiLotType;        // multi lot type
        private boolean relationFoupFlag;
        // <c>SP_Cas_MultiLotType_SingleLotSingleRecipe    "SL-SR"
        // <c>SP_Cas_MultiLotType_MultiLotSingleRecipe     "ML-SR"
        // <c>SP_Cas_MultiLotType_MultiLotMultiRecipe      "ML-MR"
    }

    @Data
    public static class LotLocationInfo {
        private ObjectIdentifier cassetteID;
        private String transferStatus;          // please check the annotations at the head of this file
        private String transferReserveUserID;
        private ObjectIdentifier stockerID;
        private ShelfPosition shelfPosition; //for e-rack, added by nyx
        private ObjectIdentifier equipmentID;
        private Integer shelfPositionX;           //Shelf PositionX
        private Integer shelfPositionY;           //Shelf PositionY
        private Integer shelfPositionZ;           //Shelf PositionZ
        private String cassetteCategory;         // Carrier Type, please check the annotations at the head of this file
        private Object reserve;                   //<i>Reserved for myCIM4.0 customization
        private String carrierType;
    }

    @Data
    public static class LotOnPort {
        private boolean moveInFlag;             //<i>Operation Start Flag
        private boolean monitorLotFlag;                 //<i>Monitor lot Flag
        private ObjectIdentifier lotID;                          //<i>lot ID
        private String subLotType;                     //<i>Sublot Type
        private String siInfo;                         //<i>Reserved for SI customization
        private String lotType;                      //<i>lot Type
        //<c>SP_Lot_Type_ProductionLot           "Production"
        //<c>SP_Lot_Type_EngineeringLot          "Engineering"
        //<c>SP_Lot_Type_ProductionMonitorLot    "Process Monitor"
        //<c>SP_Lot_Type_EquipmentMonitorLot     "eqp Monitor"
        //<c>SP_Lot_Type_DummyLot                "Dummy"
        //<c>SP_Lot_Type_VendorLot               "Vendor"
        //<c>SP_Lot_Type_RecycleLot              "Recycle"
        //<c>SP_Lot_Type_CorrelationLot          "Correlation"
    }

    @Data
    public static class LotOperationInfo {
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String operationName;
        private ObjectIdentifier stageID;
        private String maskLevel;
        private String department;
        private Boolean mandatoryOperationFlag;
        private Boolean processHoldFlag;
        private List<LotEquipmentList> lotEquipmentList;
        private Timestamp planStartTimeStamp;
        private Timestamp planEndTimeStamp;
        private ObjectIdentifier plannedEquipmentID;
        private Timestamp queuedTimeStamp;
        private ObjectIdentifier testSpecID;
        private String inspectionType;
        private Integer reworkCount;
        private String processDefinitionType;
        private List<SimpleOperationInfo> backOperationList;
    }

    @Data
    public static class LotOrderInfo {
        private String orderNumber;
        private String customerCode;
        private Boolean shipRequireFlag;
    }

    @Data
    public static class LotProductInfo {
        private ObjectIdentifier productID;
        private String productType;
        private ObjectIdentifier productGroupID;
        private String technologyCode;
        private String manufacturingLayer;
        private String reticleSetID;
        private ObjectIdentifier bomID;
    }

    @Data
    public static class LotRecipeInfo {
        private ObjectIdentifier logicalRecipeID;
        private ObjectIdentifier processMonitorProductID;
        private List<ObjectIdentifier> reticleGroupList;
        private Boolean ffEnforceFlag;
        private Boolean fbEnforceFlag;
        private ObjectIdentifier testTypeID;
    }

    @Data
    public static class LotStatusList {
        private String stateName;   //<i>State Name
        private String stateValue;  //<i>State Value
        private Object reserve;       //<i>Reserved for SI customization

        public LotStatusList() {
        }

        public LotStatusList(String stateName, String stateValue) {
            this.stateName = stateName;
            this.stateValue = stateValue;
        }
    }

    @Data
    public static class LotTypeInfo {
        private ObjectIdentifier lotType; //lot Type
        private List<SubLotType> strSubLotTypes; //Sequence of Sub lot Type
    }

    @Data
    public static class LotWafer {
        private ObjectIdentifier waferID;
        private Long slotNumber;
        private Boolean controlWaferFlag;
        private Boolean processJobExecFlag;
        private Boolean parameterUpdateFlag;
        private String processJobStatus;
        private List<StartRecipeParameter> startRecipeParameterList;
        private String aliasName;//sort设备在On-Route需要T7code
    }

    //todo:PANDA可删除，待定
    @Data
    public static class LotWaferAttributes {
        private ObjectIdentifier waferID;
        private ObjectIdentifier cassetteID;
        private String aliasWaferName;
        private Integer slotNumber;
        private ObjectIdentifier productID;
        private Integer grossUnitCount;
        private Integer goodUnitCount;
        private Integer repairUnitCount;
        private Integer failUnitCount;
        private Boolean controlWaferFlag;
        private Boolean STBAllocFlag;
        private Integer reworkCount;
        private Integer eqpMonitorUsedCount;
        private Integer usageCount;
        private Integer recycleCount;
    }

    @Data
    public static class LotWaferInfo {
        private ObjectIdentifier waferID;
        private List<RecipeParameterInfo> recipeParameterInfoList;
    }

    @Data
    public static class LotWaferMap {
        private ObjectIdentifier cassetteID;      // cassette id
        private ObjectIdentifier lotID;           // lot id
        private ObjectIdentifier waferID;         // wafer id
        private long slotNumber;       // slot number
        private boolean controlWaferFlag;   // control wafer flag
    }

    @Data
    public static class LotWipOperationInfo {
        private ObjectIdentifier responsibleRouteID;
        private ObjectIdentifier responsibleOperationID;
        private String responsibleOperationNumber;
        private String responsibleOperationName;
    }

    @Data
    public static class MaterialOutSpec {
        private List<SlmSlotMap> sourceMapList;     // Sequence of SLM Slot Map For Source
        private List<SlmSlotMap> destinationMapList;// Sequence of SLM Slot Map For Destination
    }

    @Data
    public static class LotReQueueReturn {
        private ObjectIdentifier lotID;
        private String returnCode;
    }

    @Data
    public static class LotReQueueAttributes {
        private ObjectIdentifier lotID;
        private ObjectIdentifier productID;
        private ObjectIdentifier routeID;
        private String currentOperationNumber;
    }

    @Data
    public static class NewLotAttributes implements Cloneable {
        private ObjectIdentifier cassetteID;
        private List<NewWaferAttributes> newWaferAttributesList;
        private Object reserve;         //<i>Reserved for myCIM4.0 customization

        @Override
        public NewLotAttributes clone() {
            try {
                return (NewLotAttributes) super.clone();
            } catch (CloneNotSupportedException e) {
                log.error(e.getMessage(), e);
            }
            NewLotAttributes newLotAttributes = new NewLotAttributes();
            newLotAttributes.setCassetteID(this.getCassetteID());
            newLotAttributes.setReserve(this.getReserve());
            for (NewWaferAttributes newWaferAttributes : this.getNewWaferAttributesList()) {
                newLotAttributes.getNewWaferAttributesList().add(newWaferAttributes.clone());
            }
            return newLotAttributes;
        }
    }

    @Data
    public static class NewPreparedLotInfo {
        private String stbSourceLotID;     //<i>lot ID of last STB source lot
        private String lotType;            //<i>lot Type of last STB source lot
        private String subLotType;         //<i>Sub lot Type of last STB source lot
        private ObjectIdentifier productID;          //<i>Product ID of last STB source lot
        private Long waferCount;         //<i>lot's wafer count of last STB source lot
    }

    @Data
    public static class NewVendorLotInfo {
        private String originalVendorLotID;         // STB source lot id
        private String subLotType;                   // sub lot type
        private ObjectIdentifier productID;        // product specification id
        private String vendorLotID;                  // vendor lot id
        private String vendorName;                   // vendor name
        private Integer waferCount;                     // lot's wafer count
    }

    @Data
    public static class NewWaferAttributes implements Cloneable {
        private Integer newSlotNumber;
        private ObjectIdentifier newLotID;
        private ObjectIdentifier newWaferID;
        private ObjectIdentifier sourceLotID;
        private ObjectIdentifier sourceWaferID;
        private String waferAliasName;

        @Override
        public NewWaferAttributes clone() {
            try {
                return (NewWaferAttributes) super.clone();
            } catch (CloneNotSupportedException e) {
                log.error(e.getMessage(), e);
            }
            NewWaferAttributes newWaferAttributes = new NewWaferAttributes();
            newWaferAttributes.setNewSlotNumber(this.newSlotNumber);
            newWaferAttributes.setNewLotID(this.newLotID);
            newWaferAttributes.setNewWaferID(this.newWaferID);
            newWaferAttributes.setSourceLotID(this.sourceLotID);
            newWaferAttributes.setSourceWaferID(this.sourceWaferID);
            newWaferAttributes.setWaferAliasName(this.waferAliasName);
            return newWaferAttributes;
        }
    }

    @Data
    public static class ObjCommon {
        private String transactionID;
        private User user;
        private TimeStamp timeStamp;
        private Object reserve;           //<i>Reserved for myCIM4.0 customization

        public ObjCommon duplicate() {
            ObjCommon retVal = new ObjCommon();
            retVal.transactionID = transactionID;
            retVal.user = user.duplicate();
            retVal.timeStamp = timeStamp.duplicate();
            retVal.reserve = reserve;
            return retVal;
        }
    }

    @Data
    public static class OpeCompLot {
        private ObjectIdentifier lotID;
        private String lotStatus;
        private String specificationCheckResult;     // specification check result
        private String spcCheckResult;
        private SpcResult spcResult;                       // spc check result

        private List<ObjectIdentifier> holdReleasedLotIDs;
        private List<Infos.ApcBaseCassette> apcBaseCassetteList;
        private String apcifControlStatus;
        private String dcsifControlStatus;

    }

    /*@Data
    public static class OperationMode {
        private String operationMode;           //<i>Operation Mode
        private String onlineMode;              //<i>Online Mode
        private String dispatchMode;            //<i>dispatch Mode
        private String accessMode;              //<i>Access Mode
        private String operationStartMode;      //<i>Operation Start Mode
        private String operationCompMode;       //<i>Operation Comp Mode
        private String description;             //<i>Description
    }*/

    @Data
    public static class OperationNameAttributes {
        private static final Long serialVersionUID = -98473612456935823L;
        private Integer sequenceNumber;                          //<i>Sequence Number. Not used.  //D4100020
        private ObjectIdentifier routeID;                                 //<i>Route ID
        private ObjectIdentifier operationID;                             //<i>Operation ID
        private String operationNumber;                         //<i>Operation Number
        private String operationName;                           //<i>Operation Name
        private String operationPass;                           //<i>Operation Pass
        private String objrefPO;                                //<i>Object Reference PO
        private ProcessRef processRef;                              //<i>Process Reference //D4100020
        private String testType;                                //<i>Test Type
        private String inspectionType;                          //<i>Inspection Type
        private ObjectIdentifier stageID;                                 //<i>Stage ID
        private ObjectIdentifier stageGroupID;                            //<i>Stage Group ID
        private String maskLevel;                               //<i>Mask Level
        private String departmentNumber;                        //<i>Department Number
        private Boolean mandatoryOperationFlag;                  //<i>Mandatory Operation Flag
        private Double standardCycleTime;                       //<i>Standard Cycle Time
        private Timestamp plannedStartTime;                        //<i>Planned Start Time
        private Timestamp plannedEndTime;                          //<i>Planned End Time
        private ObjectIdentifier plannedMachine;                          //<i>Planned Machine
        private Timestamp actualStartTime;                         //<i>Actual Start Time
        private Timestamp actualCompTime;                          //<i>Actual Comp Time
        private ObjectIdentifier assignedMachine;                         //<i>Assigned Machine
        private Boolean qtimeFlag;                               //<i>Qtime Flag //3.00 (R22)
        private List<ObjectIdentifier> machineList;                        //<i>Sequence of Machines //3.00 (R22)
        private List<ProcessBackupData> processBackupDataList;    //<i>Sequence of Process Backup Data //D4200062
        private String pdType;
    }

    /*@Data
    public static class PortID {
        private String portID;                          //port ID
        private long loadSequenceNoInPortGroup;         //Load Sequence No In port Group
        private String portUsage;                       //port Usage
        private String usageType;                       //Usage Type
        private String loadPurposeType;                 //Load Purpose Type
        private String portState;                       //port State
												        //SP_PortRsc_PortState_LoadAvail         "LoadAvail"
												        //SP_PortRsc_PortState_LoadReq           "LoadReq"
												        //SP_PortRsc_PortState_LoadComp          "LoadComp"
												        //SP_PortRsc_PortState_UnloadReq         "UnloadReq"
												        //SP_PortRsc_PortState_UnloadComp        "UnloadComp"
												        //SP_PortRsc_PortState_UnloadAvail       "UnloadAvail"
												        //SP_PortRsc_PortState_Unknown           "-"
												        //SP_PortRsc_PortState_UnknownForTCS     "Unknown"
												        //SP_PortRsc_PortState_Down              "Down"
        private String cassetteID;                      //Carrier ID
        private String dispatchState;                   //dispatch State
												        //SP_PortRsc_DispatchState_Required          "Required"
												        //SP_PortRsc_DispatchState_Dispatched        "Dispatched"
												        //SP_PortRsc_DispatchState_NotDispatched     "NotDispatched"
												        //SP_PortRsc_DispatchState_Error             "Error"
        private String dispatchStateTimeStamp;         //dispatch State Timestamp
        private String dispatchLoadLotID;               //dispatch Load lot ID
        private String dispatchLoadCassetteID;          //dispatch Load cassette ID
        private String dispatchUnloadLotID;             //dispatch Unload lot ID
        private String dispatchUnloadCassetteID;        //dispatch Unload cassette ID
        private List<Infos.LotInfoOnPort> lotInfoOnPortList;  //Sequence of lot Info On port
        private List<String> categoryCapability;        //dispatch Load lot ID
    }*/

    @Data
    public static class ParameterApplyWaferInfo {
        private ObjectIdentifier waferID;
        private Long slotNumber;
        private boolean controlWaferFlag;
    }

    @Data
    public static class PortOperationMode {
        private ObjectIdentifier portID;
        private String portGroup;
        private String portUsage;
        private OperationMode operationMode;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/23 10:10:23
     */
    @Data
    public static class ChangedLotAttributes {
        private ObjectIdentifier lotID;
        private String customerCode;
        private String manufacturingOrderNumber;
        private ObjectIdentifier lotOwner;
        private String externalPriority;
        private String lotComment;
        private String subLotType;
        private Object siInfo;
    }

    @Data
    public static class PortID {
        private ObjectIdentifier portID;
        private Long loadSequenceNoInPortGroup;  // Load Sequence No In port Group
        private String portUsage; // port Usage
        private String usageType; //Usage Type
        private String loadPurposeType;
        private String portState; //port State
        //<c>SP_PortRsc_PortState_LoadAvail         "LoadAvail"
        //<c>SP_PortRsc_PortState_LoadReq           "LoadReq"
        //<c>SP_PortRsc_PortState_LoadComp          "LoadComp"
        //<c>SP_PortRsc_PortState_UnloadReq         "UnloadReq"
        //<c>SP_PortRsc_PortState_UnloadComp        "UnloadComp"
        //<c>SP_PortRsc_PortState_UnloadAvail       "UnloadAvail"
        //<c>SP_PortRsc_PortState_Unknown           "-"
        //<c>SP_PortRsc_PortState_UnknownForTCS     "Unknown"
        //<c>SP_PortRsc_PortState_Down              "Down"
        private ObjectIdentifier cassetteID;

        private String dispatchState; //dispacth state
        //<c>SP_PortRsc_DispatchState_Required          "Required"
        //<c>SP_PortRsc_DispatchState_Dispatched        "Dispatched"
        //<c>SP_PortRsc_DispatchState_NotDispatched     "NotDispatched"
        //<c>SP_PortRsc_DispatchState_Error             "Error"
        private String dispatchStateTimeStamp;
        private ObjectIdentifier dispatchLoadLotID;
        private ObjectIdentifier dispatchLoadCassetteID;
        private ObjectIdentifier dispatchUnloadLotID;
        private ObjectIdentifier dispatchUnloadCassetteID;
        private List<LotInfoOnPort> lotInfoOnPortList;   //Sequence of lot info on port
        private List<String> categoryCapability;    // Sequence of Category Capability

        public PortID() {
        }

        public PortID(CimPortDO port) {
            if (null == port) {
                return;
            }
            this.portID = new ObjectIdentifier(port.getPortID(), port.getId());
            this.loadSequenceNoInPortGroup = CimNumberUtils.longValue(port.getLoadSequenceInGroup());
            this.portUsage = port.getPortUsage();
            this.loadPurposeType = port.getLoadPurposeType();
            this.portState = port.getPortState();
            this.dispatchState = port.getPortDispatchState();
            this.dispatchStateTimeStamp = port.getPortDispatchTime().toString();
            this.dispatchLoadCassetteID = new ObjectIdentifier(port.getLoadDispatchCassetteID(), port.getLoadDispatchCassetteObj());
            this.dispatchUnloadCassetteID = new ObjectIdentifier(port.getUnloadDispatchCassetteID(), port.getUnloadDispatchCassetteObj());
        }

        // ----------------------- Comparator --------------------------

        private static SortByDispatchStateTimestampComparator sortByDispatchStateTimestampComparator = new SortByDispatchStateTimestampComparator();

        public static SortByDispatchStateTimestampComparator getDispatchStateTimestampComparator() {
            return sortByDispatchStateTimestampComparator;
        }

        public static class SortByDispatchStateTimestampComparator implements Comparator<PortID> {
            @Override
            public int compare(PortID o1, PortID o2) {
                Timestamp o1Timestamp = Timestamp.valueOf(o2.dispatchStateTimeStamp);
                Timestamp o2Timestamp = Timestamp.valueOf(o2.dispatchStateTimeStamp);
                return o1Timestamp.compareTo(o2Timestamp);
            }
        }

        private static SortByLoadSequenceNoInPOrtGroupComparator sortByLoadSequenceNoInPOrtGroupComparator = new SortByLoadSequenceNoInPOrtGroupComparator();

        private static SortByLoadSequenceNoInPOrtGroupComparator getLoadSequenceNoInPOrtGroupComparator() {
            return sortByLoadSequenceNoInPOrtGroupComparator;
        }

        private static class SortByLoadSequenceNoInPOrtGroupComparator implements Comparator<PortID> {

            @Override
            public int compare(PortID o1, PortID o2) {
                return (int) (o1.loadSequenceNoInPortGroup - o2.loadSequenceNoInPortGroup);
            }
        }

        // -------------------- End of Comparator --------------------------
    }

    @Data
    public static class PostProcessActionInfo implements Cloneable{
        private String dKey;
        private Integer sequenceNumber;
        private String execCondition;   // exec condition
        private List<Long> execConditionList; // sequence of exec condition
        private String watchDogName;
        private String postProcessID;
        private Integer syncFlag;       // false:sync, true:async
        private String transationID;
        private String targetType;
        private PostProcessTargetObject postProcessTargetObject;    // post process target objects
        private Boolean commitFlag; // false: not commit, true: commit
        private String status;
        private Integer splitCount;
        private Integer errorAction;
        private String createTime;
        private String updateTime;
        private String claimUserID;     // claim user ID
        private String claimTime;       // claim time
        private String claimMemo;
        private Double claimShopDate;
        private String extEventID;
        private Object reserve;           //<i>Reserved for myCIM4.0 customization


        public PostProcessActionInfo clone(){
            PostProcessActionInfo actionInfo = null;
            try {
                actionInfo = (PostProcessActionInfo) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return actionInfo;
        }
    }

    @Data
    public static class PostProcessAdditionalInfo {
        private String dKey;
        private Integer sequenceNumber;
        private String name;
        private String value;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/10/16 15:59:30
     */
    @Data
    public static class DCSpecDetailInfo {
        private String dataItemName;
        private Boolean screenLimitUpperRequired;
        private Double screenLimitUpper;
        private String actionCodes_uscrn;
        private Boolean screenLimitLowerRequired;
        private Double screenLimitLower;
        private String actionCodes_lscrn;
        private Boolean specLimitUpperRequired;
        private Double specLimitUpper;
        private String actionCodes_usl;
        private Boolean specLimitLowerRequired;
        private Double specLimitLower;
        private String actionCodes_lsl;
        private Boolean controlLimitUpperRequired;
        private Double controlLimitUpper;
        private String actionCodes_ucl;
        private Boolean controlLimitLowerRequired;
        private Double controlLimitLower;
        private String actionCodes_lcl;
        private Double target;
        private String tag;
        private String dcSpecGroup;
        private String siInfo;
    }


    @Data
    public static class PostProcessTargetObject {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private Object reserve;           //<i>Reserved for myCIM4.0 customization
    }

    @Data
    public static class PreparationCancelledLotInfo {
        private ObjectIdentifier lotID;           // preparation cancelled lot id
        private String lotType;         // lot type
        private String subLotType;      // sub lot type
        private String lotStatus;       // lot status
        private ObjectIdentifier productID;       // product specification id
        private ObjectIdentifier cassetteID;      // cassette id
        private ObjectIdentifier bankID;           // bank id
        private long waferCount;        // lot's wafer count
    }

    @Data
    public static class PriorityInfo {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
        private Object reserve;
    }


    @Data
    public static class PriorityChangeReq {
        private String jobID;
        private List<PriorityInfo> priorityInfoData;
    }

    @Data
    public static class PreparationCancelledWaferInfo {
        private ObjectIdentifier waferID;                 // wafer id
        private ObjectIdentifier originalVendorLotID;   // original vendor lot id
        private long stbCount;                 // wafer's stbed count
    }

    @Data
    public static class PreparedLotInfo {
        private ObjectIdentifier lotID; // new prepared lot id
        private ObjectIdentifier productID;   // product id
        private String lotType;     // lot type
        private String subLotType;  // sub lot type
        private Long waferCount;    // lot's wafer count
    }

    @Data
    public static class PrivilegeInfo {
        private String subSystem;
        private String category;
        private String privilegeID;
        private String permission;
    }

    @Data
    public static class ProcessBackupData {
        private Boolean sourceFlag;     //<i>Source Flag
        private Boolean entryFlag;      //<i>Entry Flag
        private String hostName;       //<i>Host Name
        private String serverName;     //<i>Server Name
        private ObjectIdentifier lotID;          //<i>lot ID          //P4200564
        private String objrefPO;       //<i>Stringfield Object Reference PosProcessOperation
        private String itDaemonPort;
        private Object siInfo;    //<i>Reserved for SI customization
    }

    @Data
    public static class ProcessHoldSearchKey {
        private ObjectIdentifier routeID;            //<i>Route ID
        private String operationNumber;    //<i>Operation Number
        private ObjectIdentifier productID;          //<i>Product ID
        private String holdType;           //<i>Hold Type
        private ObjectIdentifier userID;
    }

    @Data
    public static class ProcessOperationLot {
        private String poObj;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class ProcessRef {
        private String processFlow;                      //<i>Process Flow
        private String processOperationSpecification;    //<i>Process Operation Specification
        private String mainProcessFlow;                  //<i>Main Process Flow
        private String moduleNumber;                     //<i>Module Number
        private String moduleProcessFlow;                //<i>Module Process Flow
        private String modulePOS;                        //<i>Module pos
        private Object siInfo;                           //<i>Reserved for SI customization
    }

    /**
     * description: pptProdReqListAttributes__180_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/26/2018 9:57 AM
     */
    @Data
    public static class ProdReqListAttribute {
        private ObjectIdentifier lotID;                //<i>lot ID
        private String lotType;              //<i>lot Type
        private String subLotType;           //<i>Sub lot Type
        private ObjectIdentifier productID;            //<i>Product ID
        private String dueTimeStamp;         //<i>Due Time Stamp
        private String priorityClass;        //<i>Priority Class
        private String externalPriority;     //<i>External Priority
        private String orderNumber;          //<i>Order Number
        private String customerCode;         //<i>Customer CimCode
        private String releaseTimeStamp;     //<i>Release Time Stamp
        private ObjectIdentifier routeID;              //<i>Route ID
        private ObjectIdentifier startBankID;          //<i>Start bank ID
        private Long productCount;         //<i>Product Count
        private ObjectIdentifier manufacturingLayerID; //<i>MGF Layer ID
        private String productRequestName;   //<i>Product Request Name
        private String planState;            //<i>Plan State
        private String prodState;            //<i>Product State
        private Long planPriority;         //<i>Plan Priority
        private ObjectIdentifier lotOwnerID;           //<i>lot Owner ID
        private ObjectIdentifier endBankID;            //<i>End bank ID
        private String lotGenType;           //<i>lot Generation Type
        private String scheduleMode;         //<i>Schedule Mode
        private String lotGenMode;           //<i>lot Generation Mode
        private String lotComment;           //<i>lot Comment
        private ObjectIdentifier claimUserID;          //<i>Claim User ID
        private String claimedTimeStamp;     //<i>Claim Time Stamp
        private ObjectIdentifier lotScheduleID;        //<i>lot Schedule ID
        private String orderType;            //<i>Order Type
        private List<ObjectIdentifier> sourceLotIDList; //<i>Source lot IDs
    }

    @Data
    public static class ReceivedLotInfo {
        private ObjectIdentifier newLotID;            // new vendor lot id
        private String subLotType;          // sub lot type
        private long waferCount;            // lot's wafer count
        private String originalVendorLotID; // original vendor lot id
    }

    @Data
    public static class RecipeBodyManagement {
        private ObjectIdentifier machineRecipeId;             //Machine Recipe ID
        private String physicalRecipeId;            //Physical Recipe ID
        private String fileLocation;                //File Location
        private String fileName;                    //File Name
        private Boolean formatFlag;                 //Format Flag
        private Boolean forceDownLoadFlag;          //Force Down Load Flag
        private Boolean recipeBodyConfirmFlag;      //Recipe Body Confirm Flag
        private Boolean conditionalDownLoadFlag;    //Conditional Down Load Flag
    }

    @Data
    public static class RecipeParameter {
        private String parameterName;
        private String unit;
        private String dataType;
        private String defaultValue;
        private String lowerLimit;
        private String upperLimit;
        private Boolean useCurrentValueFlag;
        private String tag;
    }

    @Data
    public static class RecipeParameterInfo {
        private long sequenceNumber;
        private String parameterName;         // Recipe Parameter Name
        private String parameterUnit;        // Recipe Parameter Unit
        private String parameterDataType;   // Recipe Parameter Data Type
        private String parameterLowerLimit;
        private String parameterUpperLimit;
        private boolean useCurrentSettingValueFlag;
        private String parameterTargetValue;    // Recipe Parameter Target Value;
        private String parameterValue;
    }

    @Data
    public static class ReleasedLotReturn {
        private ObjectIdentifier lotID;
        private Integer returnCode;
        private Object reserve;      //Reserved for SI customization
    }

    @Data
    public static class ReleaseLotAttributes {
        private ObjectIdentifier lotID;                                 //<i>lot ID
        private ObjectIdentifier productID;                             //<i>Product ID
        private String customerCode;                          //<i>Customer CimCode
        private String manufacturingOrderNumber;              //<i>Manufacturing Order Number
        private String lotOwner;                              //<i>lot Owner
        private String lotType;                               //<i>lot Type
        private String subLotType;                            //<i>Sub lot Type
        private ObjectIdentifier routeID;                               //<i>Route ID
        private String lotGenerationType;                     //<i>lot Generation Type
        private String schedulingMode;                        //<i>Scheduling Mode
        private String lotIDGenerationMode;                   //<i>lot ID Generation Mode
        private String productDefinitionMode;                 //<i>Product Definition Mode
        private String priorityClass;                         //<i>Priority Class
        private String externalPriority;                      //<i>External Priority
        private String plannedStartTime;                      //<i>Planned Start Time
        private String plannedFinishTime;                     //<i>Planned Finished Time
        private String lotComment;                             //<i>lot Comment
        private Integer productQuantity;                       //<i>Product Quantity
        private String department; //for start hold
        private String section; //for start hold
        private String reasonCode; //for start hold
        private List<DirectedSourceLot> directedSourceLotList;      //<i>Sequence of Directed Source lot
        private List<ReleaseLotSchedule> releaseLotScheduleList;    //<i>Sequence of Released lot Schedule
        private Object reserve;      //<i>Reserved for SI customization
    }

    @Data
    public static class ReleaseLotSchedule {
        private String operationNumber;       //<i>Operation Number
        private ObjectIdentifier equipmentID; //<i>eqp ID
        private String plannedStartTime;     //<i>Planned Start Time
        private String plannedFinishTime;    //<i>Planned Finish Time
        private Object reserve;                 //<i>Reserved for SI customization
    }

    @Data
    public static class ReticleInfo {
        private Integer sequenceNumber;
        private ObjectIdentifier reticleID;
        private ObjectIdentifier reticleGroup;
        private Object reserve;                 //<i>Reserved for SI customization
    }

    @Data
    public static class ReticleStatusInfo {
        private String reticleStatus;          //<i>Reticle Status
        private String transferStatus;         //<i>Transfer Status
        private String reticleLocation;
        private String inspectionType;
        private String durableSubStatusName;
        private String durableSubStatusDescription;
        private ObjectIdentifier equipmentID;            //<i>eqp ID
        private ObjectIdentifier durableControlJobID;
        private ObjectIdentifier transferReserveUserID;
        private ObjectIdentifier durableSubStatus;
        private ObjectIdentifier stockerID;              //<i>stocker ID
        private ShelfPosition shelfPosition; //for e-rack, added by nyx
        private ObjectIdentifier reticlePodID;           //<i>Reticle Pod ID    //D3100008
        private Timestamp lastClaimedTimeStamp;   //<i>Last Claimed Time Stamp
        private Timestamp ProcessLagTime;
        private Boolean durableSTBFlag;
        private List<HashedInfo> strDurableStatusList;
        private Boolean availableForDurableFlag;
        private Boolean inPostProcessFlagOfReticle;
        private ObjectIdentifier lastClaimedPerson;      //<i>Last Claimed person
    }

    @Data
    public static class ReticlePodPortInfo {
        private ObjectIdentifier reticlePodPortID;              //<i>Reticle Pod port ID
        private String eqpCategory;                   //<i>eqp Category
        private String accessMode;                    //<i>Access Mode
        private String portStatus;                    //<i>port Status
        private String portStatusChangeTimestamp;     //<i>port Status Change Time Stamp
        private ObjectIdentifier portStatusChangeUserID;        //<i>port Status Change User ID
        private ObjectIdentifier loadedReticlePodID;            //<i>Loaded Reticle Pod ID
        private ObjectIdentifier reservedReticlePodID;          //<i>Reserved Reticle Pod ID
        private String transferReserveStatus;         //<i>Transfer Reserve Status
        private String transferReserveTimestamp;      //<i>Transfer Reserve Time Stamp
        private String dispatchStatus;                //<i>dispatch Status
        private String dispatchTimestamp;             //<i>dispatch Time Stamp
        private String siInfo;                        //<i>Reserved for SI customization
    }

    @Data
    public static class ReturnOperation {
        private String processFlow;
        private String operationNumber;
        private String mainProcessFlow;
        private String moduleProcessFlow;
    }

    @Data
    public static class ReworkReq {
        private ObjectIdentifier lotID;            // lot ID
        private ObjectIdentifier currentRouteID;   // current route ID
        private String currentOperationNumber;      // current operation number
        private ObjectIdentifier subRouteID;       // sub route ID
        private String returnOperationNumber;       // return operation number
        private ObjectIdentifier reasonCodeID;     // reason code ID
        private String eventTxId;                   // event Tx ID
        private Boolean forceReworkFlag;            // force rework flag
        private Boolean dynamicRouteFlag;           // dynamic route flag
        private Object siInfo;
    }


    @Data
    public static class ReScheduledLotAttributes implements Cloneable {
        private ObjectIdentifier lotID;
        private ObjectIdentifier productID;
        private String originalRouteID;
        private ObjectIdentifier routeID;
        private String originalOperationNumber;
        private String currentOperationNumber;
        private String subLotType;
        private String shedulingMode;
        private String priorityClass;
        private String plannedStartTime;
        private String plannedFinishTime;
        private List<ChangedLotSchedule> changedLotScheduleList;

        @Override
        public ReScheduledLotAttributes clone() {
            try {
                return (ReScheduledLotAttributes) super.clone();
            } catch (CloneNotSupportedException e) {
                Validations.check(true, e.getMessage());
            }
            return null;
        }
    }

    @Data
    public static class SlmSlotMap {
        private ObjectIdentifier cassetteID;  // cassette ID
        private ObjectIdentifier waferID;     // wafer ID
        private int slotNumber;    // Slot Number
    }

    @Data
    public static class SorterComponentJobListAttributes {
        private ObjectIdentifier sorterComponentJobID;                // Sorter Component Job ID
        private String requestTimeStamp;                    // <String Type> Request Time Stamp
        private ObjectIdentifier originalCarrierID;                  // original Carrier ID
        private ObjectIdentifier originalPortID;                      // original port ID
        private String originalCarrierTransferState;      // original Carrier Xfer Status
        private ObjectIdentifier originalCarrierEquipmentID;        // original Carrier eqp ID
        private ObjectIdentifier originalCarrierStockerID;          // original Carrier Stoker ID
        private ObjectIdentifier destinationCarrierID;               // destination Carrier ID
        private ObjectIdentifier destinationPortID;                  // destination port ID
        private String destinationCarrierTranferStatus;   // destination Carrier Xfer Status
        private ObjectIdentifier destinationCarrierEquipmentID;   // destination Carrier eqp ID
        private ObjectIdentifier destinationCarrierStockerID;      // destination Carrier stocker ID
        private String componentSorterJobStatus;          // component sorter job status
        private ObjectIdentifier preSorterComponentJobID;          // Pre Sorter Component Job ID
        private List<WaferSorterSlotMap> waferSorterSlotMapList;
        private Object reserve;                 //<i>Reserved for SI customization
        private Info.SortJobPostAct postAct; //千岛二期增加
        private String requestUserID;
        private Integer jobSequence;
        private String actionCode;
    }

    @Data
    public static class SorterJobListAttributes {
        private ObjectIdentifier sorterJobID;         // Sorter Job ID
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier portGoupID;
        private String sorterJobStatus;
        private ObjectIdentifier requestUserID;
        private Timestamp requestTimeStamp;    // <String Type>Request Time Stamp
        private Integer componentCount;        // Component Count
        private ObjectIdentifier preSorterJobID;      // Pre Sorter Job ID
        private Boolean waferIDReadFlag;    // wafer ID Req Flag
        private List<SorterComponentJobListAttributes> sorterComponentJobListAttributesList;  // list of sorter component job information
        private Object reserve;              //<i>Reserved for SI customization
    }

    @Data
    public static class SortJobListAttributes {
        private ObjectIdentifier sorterJobID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier portGroupID;
        private String sorterJobStatus;
        private ObjectIdentifier requestUserID;
        private String requestTimeStamp;
        private int componentCount;
        private ObjectIdentifier preSorterJobID;
        private String sorterJobCategory;
        private boolean waferIDReadFlag;
        private List<SorterComponentJobListAttributes> sorterComponentJobListAttributesList;
        private Object reserve;
        private String ctrljobId;

    }

    @Data
    public static class SourceLot {
        private List<ObjectIdentifier> products;
        private ObjectIdentifier lotID;             //<i>lot ID
        private ObjectIdentifier cassetteID;        //<i>Carrier ID
        private String lotType;           //<i>lot Type
        private ObjectIdentifier bankID;            //<i>bank ID
        private String bankName;          //<i>bank Name
        private Integer usageCount;
        private Integer recycleCount;
        private Integer usageLimit;
        private Integer recycleLimit;
        private String vendorLotID;       //<i>Vendor lot ID
        private String vendorID;          //<i>Vendor ID
        private ObjectIdentifier stockerID;         //<i>stocker ID
        private String transferStatus;    //<i>Transfer Status
        private Long shelfPositionX;    //<i>Shelf Position X
        private Long shelfPositionY;    //<i>Shelf Position Y
        private Long shelfPositionZ;    //<i>Shelf Position Z
        private ObjectIdentifier productID;         //<i>Product ID
        private ObjectIdentifier productGroupID;    //<i>Product Group ID
        private Integer waferCount;        //<i>wafer Count
        private Integer notAllocCount;     //<i>Not Allocate Count
        private Integer usedCount;         //<i>Used Count
        private Integer priority;          //<i>Priority            //DSN000101503
        private String customField;       //<i>Customer field      //DSN000101503
        private String manufacturingLayer;          //<i>Manufacturing layer
        private ObjectIdentifier customerID;        //<i>Customer ID
        private ObjectIdentifier lotOwnerID;        //<i>lot Owner ID
        private String orderNumber;       //<i>Order Number
        private String lotState;          //<i>lot State
        private String lotFinishedState;  //<i>lot Finished State
        private Timestamp completionTimeStamp; //<i>Completion Time
        private Timestamp plannedCompletionDateTimeStamp; //<i>Planned Completion Date
        private Integer controlQuantity;   //<i>Control Quantity
        private ObjectIdentifier mainProcessDefinitionID;        //<i>Main PD
        private String operationNumber;   //<i>Operation Number
        private ObjectIdentifier processDefinitionID; //<i>Process Definition
        private Long remainCycleTime;   //<i>Remain Cycle Time
        private Object siInfo;            //<i>Reserved for SI customization
    }

    @Data
    public static class SourceLotEx {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private List<ObjectIdentifier> waferIDList;  //【bear】 The products name is changed to the waferIDs name
    }

    @Data
    public static class SourceProduct {
        private String sourceProduct;    //<i>Source Product
        private Object reserve;              //<i>Reserved for SI customization
    }

    @Data
    public static class SourceWafersAttributes {
        private ObjectIdentifier waferID;
        private String goodUnitCount;
        private String failUnitCount;
    }

    @Data
    public static class SpcChart {
        private String chartGroupID;        // chart group ID
        private String chartID;              // chart ID
        private String chartType;            // chart type
        private String speckCheck;           // speck check
        private List<SpcReturnCode> spcReturnCodeList;   // sequence of SPC(statistical process control) return code information
        private String chartOwnerMailAddress;   // chart owner mail address
        private List<String> chartSubOwnerMailAddress;  // sequence of chart sub owner mail addresses
    }

    @Data
    public static class SpcDcItemAndChart {
        private String dataCollectionItemName;  // SPC(statistical process control) DC(data collection )item name
        private List<SpcChart> spcChartList;  // sequence of SPC(statistical process control) chart info
    }

    @Data
    public static class SpcResult {
        private List<SpcDcItemAndChart> spcDcItem;   // sequence of SPC(statistical process control) DC(data collection) Item and chart info
        private String bankID;
        private String reworkRouteID;
    }

    @Data
    public static class SpcReturnCode {
        private String rule;                     // the rule
        private String returnCodeStatus;        // return code status
    }

    @Data
    public static class StartCassette {
        private Long loadSequenceNumber;
        private ObjectIdentifier cassetteID;
        private String loadPurposeType;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unloadPortID;
        private List<LotInCassette> lotInCassetteList;
    }

    @Data
    public static class PosStartCassetteInfo {
        private Long loadSequenceNumber;
        private ObjectIdentifier cassetteID;
        private String loadPurposeType;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unloadPortID;
        private List<PosLotInCassetteInfo> lotInCassetteInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PosLotInCassetteInfo {
        private boolean operationStartFlag;
        private boolean monitorLotFlag;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class StartDurable {
        private ObjectIdentifier durableId;                                   //durable ID
        private StartDurablePort startDurablePort;            //Start durable port
        private StartOperationInfo startOperationInfo;        //Start Operation Information
    }

    @Data
    @NoArgsConstructor
    public static class StartDurableInfo {
        private ObjectIdentifier durableID;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unloadPortID;
        private Long loadSequenceNumber;
        private String loadPurposeType;

        public StartDurableInfo(ObjectIdentifier durableID, ObjectIdentifier loadPortID, ObjectIdentifier unloadPortID, Long loadSequenceNumber,
                                String loadPurposeType) {
            this.durableID = durableID;
            this.loadPortID = loadPortID;
            this.unloadPortID = unloadPortID;
            this.loadSequenceNumber = loadSequenceNumber;
            this.loadPurposeType = loadPurposeType;
        }
    }

    @Data
    public static class StartDurablePort {
        private String loadPurposeType;     //Load Purpose Type
        private ObjectIdentifier loadPortID;          //Load port ID
        private Long loadSequenceNumber;   //Load Sequence Number
        private ObjectIdentifier unloadPortID;        //Unload port ID
        private Long unloadSequenceNumber;   //Unload Sequence Number
    }

    @Data
    public static class StartFixture {
        private ObjectIdentifier fixtureID;
        private String fixtureCategory;
    }

    @Data
    public static class StartOperationInfo {
        private ObjectIdentifier processFlowID;
        //QianDao MES-EAP integratin update maskLevel for photoLayer
        private String maskLevel;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private Integer passCount;
    }

    @Data
    public static class StartRecipe {
        private ObjectIdentifier machineRecipeID;
        private ObjectIdentifier logicalRecipeID;
        private String physicalRecipeID;
        private List<Infos.StartReticleInfo> startReticleList;
        private List<Infos.StartFixtureInfo> startFixtureList;
        private Boolean dataCollectionFlag;
        private List<Infos.DataCollectionInfo> dcDefList;
        private ObjectIdentifier chamberLevelRecipeID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/9/26 10:36
     */
    @Data
    public static class MfgRestrictRequestForMultiFabIn {
        private List<EntityInhibitAttributesWithFabInfo> strEntityInhibitionsWithFabInfo;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class StartRecipeParameter {
        private String parameterName;
        private String parameterValue;
        private String targetValue;
        private Boolean useCurrentSettingValueFlag;
    }

    @Data
    public static class StartRecipeParameterSetInfo {
        private Integer setNumber;
        private List<StartRecipeParameter> recipeParameterList;
        private List<ParameterApplyWaferInfo> applyWaferInfoList;
        private ObjectIdentifier machineRecipe;
        private String physicalRecipe;
    }


    @Data
    public static class StartReservedControlJobInfo {
        private String portGroupID;        //<i>port Group ID
        private ObjectIdentifier controlJobID;       //<i>Control Job ID
    }

    @Data
    public static class StartReticle {
        private Long sequenceNumber;
        private ObjectIdentifier reticleID;
    }

    @Data
    public static class STBCancelInfo {
        private ObjectIdentifier productReqID;   // Prepared lot's product request ID
        private NewPreparedLotInfo newPreparedLotInfo;  //Prepared lot information
        private NewLotAttributes newLotAttributes;      //New lot attribute
        private Object siInfo;                           //<i>Reserved for SI customization
    }

    @Data
    public static class STBInfo {
        private String vendorLotID;
        private String vendorLotSubLotType;
        private ObjectIdentifier vendorLotProdSpecID;
        private String vendorVendLotID;
        private String vendorVendName;
        private String STBSourceLotID;
    }

    @Data
    public static class STBCancelledLotInfo {
        private ObjectIdentifier lotID;              //<i>STB cancelled lot ID
        private String lotType;            //<i>lottype
        private String subLotType;         //<i>Sub lot Type
        private String lotStatus;          //<i>lot Status
        private ObjectIdentifier productID;          //<i>ProdSpec ID
        private ObjectIdentifier cassetteID;         //<i>cassette ID
        private ObjectIdentifier routeID;            //<i>Route which lot is on
        private String operationNumber;    //<i>Operation number
        private ObjectIdentifier routeStartBankID;   //<i>Start bank of route which lot is on
        private Long waferCount;         //<i>lot's wafer count
    }

    @Data
    public static class STBCancelWaferInfo {
        private ObjectIdentifier waferID;            //<i>wafer ID
        private ObjectIdentifier currentLotID;       //<i>Current lot ID
        private String stbSourceLotID;     //<i>STB source lot ID
        private Long slotNo;             //<i>Slot Number
    }

    @Data
    public static class STBSourceLotInfo {
        private String sourceLotID;
        private String sourceLotLotType;
        private String sourceLotSubLotType;
        private ObjectIdentifier sourceLotProdSpecID;
    }

    @Data
    public static class StoredReticle {
        private ObjectIdentifier reticleID;        //<i>Reticle ID
        private String description;      //<i>Description
        private ObjectIdentifier reticleGroupID;   //<i>Reticle Group ID
        private String status;           //<i>Status
        private String siInfo;           //<i>Reserved for SI customization
    }

    @Data
    public static class TimeStamp {
        private Timestamp reportTimeStamp = new Timestamp(System.currentTimeMillis());
        private Double reportShopDate;
        private Object reserve;         //<i>Reserved for myCIM4.0 customization

        public TimeStamp duplicate() {
            TimeStamp timeStamp = new TimeStamp();
            timeStamp.reportTimeStamp = new Timestamp(timeStamp.reportTimeStamp.getTime());
            timeStamp.reportShopDate = reportShopDate;
            timeStamp.reserve = reserve;
            return timeStamp;
        }
    }

    @Deprecated
    @Data
    public static class VendorLotEventRecord extends EventRecord {
        private LotEventData lotDate;
        private String vendorLotID;
        private Long claimQuantity;

        public VendorLotEventRecord(LotEventData lotEventData, ObjCommon objCommon,
                                    Params.VendorLotEventMakeParams vendorLotEventMakeParams) {
            this.lotDate = new LotEventData();
            this.lotDate.setLotID(lotEventData.getLotID());
            this.lotDate.setBankID(lotEventData.getBankID());
            this.lotDate.setCassetteID(lotEventData.getCassetteID());
            this.lotDate.setLotStatus(lotEventData.getLotStatus());
            this.lotDate.setCustomerID(lotEventData.getCustomerID());
            this.lotDate.setPriorityClass(lotEventData.getPriorityClass());
            this.lotDate.setProductID(lotEventData.getProductID());
            this.lotDate.setOriginalWaferQuantity(lotEventData.getOriginalWaferQuantity());
            this.lotDate.setCurrentWaferQuantity(lotEventData.getCurrentWaferQuantity());
            this.lotDate.setProductWaferQuantity(lotEventData.getProductWaferQuantity());
            this.lotDate.setControlWaferQuantity(lotEventData.getControlWaferQuantity());
            this.lotDate.setHoldState(lotEventData.getHoldState());
            this.lotDate.setBankID(lotEventData.getBankID());
            this.lotDate.setRouteID(lotEventData.getRouteID());
            this.lotDate.setOperationNumber(lotEventData.getOperationNumber());
            this.lotDate.setOperationID(lotEventData.getOperationID());
            this.lotDate.setOperationPassCount(lotEventData.getOperationPassCount());
            this.lotDate.setObjrefPOS(lotEventData.getObjrefPOS());
            this.lotDate.setWaferHistoryTimeStamp(lotEventData.getWaferHistoryTimeStamp());
            this.lotDate.setObjrefMainPF(lotEventData.getObjrefMainPF());
            this.lotDate.setObjrefModulePOS(lotEventData.getObjrefModulePOS());

            super.eventCommon = new EventData(objCommon, objCommon.getTransactionID(), vendorLotEventMakeParams.getClaimMemo());
            this.vendorLotID = vendorLotEventMakeParams.getVendorLotID();
            this.claimQuantity = vendorLotEventMakeParams.getClaimQuantity();
        }

        public VendorLotEventRecord() {
        }
    }

    @Deprecated
    @Data
    public static class DynamicBufferResourceChangeEventRecord extends EventRecord {
        private String equipmentID;
        private String equipmentState;
        private String E10State;
        private String bufferCategory;
        private Long smCapacity;
        private Long dynamicCapacity;
    }

    @Deprecated
    @Data
    public static class NoteChangeEventRecord extends EventRecord {
        private String objectID;
        private String noteType;
        private String action;
        private String routeID;
        private String operationID;
        private String operationNumber;
        private String noteTitle;
        private String noteContents;
        private String ownerID;
    }

    @Deprecated
    @Data
    public static class EqpPortStatusChangeEventRecord extends EventRecord {
        private String portType;
        private String portID;
        private String equipmentID;
        private String portUsage;
        private String portStatus;
        private String accessMode;
        private String dispatchState;
        private String dispatchTime;
        private String dispatchDurableID;
    }

    @Data
    public static class WaferBinSummary {
        private ObjectIdentifier waferId;
        private ObjectIdentifier testTypeId;
        private ObjectIdentifier lotId;
        private ObjectIdentifier processGroupID;
        private ObjectIdentifier controlJobId;
        private ObjectIdentifier productId;
        private ObjectIdentifier equipmentId;
        private ObjectIdentifier userId;
        private ObjectIdentifier testSpecId;
        private String testProgramId;
        private ObjectIdentifier binDefinitionId;
        private int goodUnitCount;
        private int repairUnitCount;
        private int failUnitCount;
        private boolean waferCheckResult;
        private String lotActionCode;
        private String lotActionParameter;
        private String waferActionCode;
        private String waferActionParameter;
        private String testStartTimeStamp;
        private String testFinishTimeStamp;
        private long binReportCount;
        private List<BinCount> binCounts;
    }

    @Data
    public static class WaferMapInCassetteInfo {
        private Integer slotNumber;
        private ObjectIdentifier waferID;
        private String aliasWaferName;
        private ObjectIdentifier lotID;
        private String scrapState;
    }

    @Data
    public static class WaferSorterSlotMap {
        private String portGroup;                            //<i>port Group
        private ObjectIdentifier equipmentID;                          //<i>eqp ID
        private String actionCode;                           //<i>Action CimCode
        private String requestTime;                          //<i>Request Time
        private String direction;                            //<i>Direction
        private ObjectIdentifier waferID;                              //<i>wafer ID
        private ObjectIdentifier lotID;                                //<i>lot ID
        private ObjectIdentifier destinationCassetteID;                //<i>Destination Carrier ID
        private ObjectIdentifier destinationPortID;                    //<i>Destination port ID
        private Boolean destinationCassetteManagedByOM;  //<i>Destination Carrier Maneged By OM Flag. Managed : True
        private Long destinationSlotNumber;                //<i>Destination Slot Number
        private ObjectIdentifier originalCassetteID;                   //<i>Original Carrier ID
        private ObjectIdentifier originalPortID;                       //<i>Original port ID
        private Boolean originalCassetteManagedByOM;     //<i>Original Carrier Managed By OM Flag. Managed True
        private Long originalSlotNumber;                   //<i>Original Slot Number
        private ObjectIdentifier requestUserID;                        //<i>Request User ID
        private String replyTime;                            //<i>Reply Time
        private String sorterStatus;                         //<i>Sorted Status
        private String aliasName;                            // T7_Code
        private String slotMapCompareStatus;                 //<i>Slot Map Compare Status<c>SP_Sorter_OK, SPSorter_ERROR
        private String omsCompareStatus;                      //<i>OMS Compare Status

        public WaferSorterSlotMap() {

        }

        public WaferSorterSlotMap(boolean isInit) {
            if (isInit) {
                init();
            }
        }

        private void init() {
            this.portGroup = BizConstant.EMPTY;
            this.equipmentID = new ObjectIdentifier("", "");
            this.actionCode = BizConstant.EMPTY;
            this.requestTime = BizConstant.EMPTY;
            this.direction = BizConstant.EMPTY;
            this.waferID = new ObjectIdentifier("", "");
            this.lotID = new ObjectIdentifier("", "");
            this.destinationCassetteID = new ObjectIdentifier("", "");
            this.destinationPortID = new ObjectIdentifier("", "");
            this.destinationCassetteManagedByOM = Boolean.FALSE;
            this.destinationSlotNumber = 0L;
            this.originalCassetteID = new ObjectIdentifier("", "");
            this.originalPortID = new ObjectIdentifier("", "");
            this.originalCassetteManagedByOM = false;
            this.originalSlotNumber = 0L;
            this.requestUserID = new ObjectIdentifier("", "");
            this.replyTime = BizConstant.EMPTY;
            this.sorterStatus = BizConstant.EMPTY;
            this.slotMapCompareStatus = BizConstant.EMPTY;
            this.omsCompareStatus = BizConstant.EMPTY;
        }
    }

    /**
     * description:
     * objEquipmentState_Convert_out_struct
     *
     * @author PlayBoy
     * @date 2018/7/3
     */
    @Data
    public static class EqpStateConvert {
        private String convertedStatusCode;
        private boolean stateConverted;
    }

    /**
     * description:
     * pptEqpStatusChangeReqResult_struct
     *
     * @author PlayBoy
     * @date 2018/7/3
     */
    @Data
    public static class EqpStatusChangeReq {
        private ObjectIdentifier equipmentId;
        private ObjectIdentifier equipmentStatusCode;
    }

    /**
     * description:
     * objEquipment_backupState_Get_out_struct
     *
     * @author PlayBoy
     * @since 2018/7/3
     */
    @Data
    public static class EqpBackupState {
        private ObjectIdentifier backupEquipmentStatus;
        private ObjectIdentifier backupE10Status;
    }

    @Data
    public static class SpcCheckLot {
        private String processSequenceNumber;     //<i>Process Sequence Number
        private ObjectIdentifier lotID;                     //<i>lot ID
        private ObjectIdentifier productID;                 //<i>Product ID
        private ObjectIdentifier processRouteID;            //<i>Process Route ID
        private String processOperationNumber;    //<i>Process Operation Number
        private ObjectIdentifier processEquipmentID;        //<i>Process eqp ID
        private ObjectIdentifier processMachineRecipeID;    //<i>Process Machine Recipe ID
        private ObjectIdentifier processChamberID;          //<i>Process Chamber ID    //D5100227
        private String spcCheckResult;            //<i>SPC Check Result
        private SpcResult spcResult;              //<i>SPC Result Information
        private List<String> spcActionCode;             //<i>Sequence of SPC Action CimCode
        private Boolean requestOtherFabFlag;       //<i>Request Other Fab Flag
        private String processFabID;              //<i>Process FabID
        private String processObjrefPO;           //<i>Stringfield Object Reference PosProcessOperation
        private ObjectIdentifier dcDefID;                   //<i>DC Def ID
        private ObjectIdentifier dcSpecID;                  //<i>DC Spec ID
        private String siInfo;
        /**
         * Task-311 add ocapNo for constraint memo
         */
        private String ocapNo;
    }

    /**
     * description:
     * posMachineStateConvertCondition_struct
     *
     * @author PlayBoy
     * @date 2018/7/3
     */
    @Data
    public static class EqpStateConvertCondition {
        private String checkSequence;
        private String convertLogic;
        private String attributeValue;
        private ObjectIdentifier toMachineStateCode;
    }

    @Data
    public static class LotHoldListAttributes {
        private String holdType;
        private ObjectIdentifier reasonCodeID;                //<i>Reason CimCode ID
        private String codeDescription;             //<i>CimCode Description
        private ObjectIdentifier userID;                      //<i>User ID
        private String userName;                    //<i>User Name
        private String holdTimeStamp;               //<i>Hold Time Stamp
        private String responsibleOperationMark;    //<c>SP_ResponsibleOperation_Current        "C"
        //<c>SP_ResponsibleOperation_Previous       "P"
        private ObjectIdentifier responsibleRouteID;          //<i>Responsible Route ID
        private String responsibleOperationNumber;  //<i>Responsible Operation Number
        private String responsibleOperationName;    //<i>Responsible Operation Name
        private ObjectIdentifier relatedLotID;                 //<i>Related lot ID
        private String claimMemo;                   //<i>Claim Comment
		private String department;
		private String section;
    }

    @Data
    public static class ContainedLotsInFlowBatch {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private Long processSequenceNumber;
    }

    @Data
    public static class FlowBatchedCassetteInfo {
        private ObjectIdentifier cassetteID;               //<i>Carrier ID
        private Long processSequenceNumber;    //<i>Process Sequence Number
        private List<FlowBatchedLotInfo> strFlowBatchedLotInfo;    //<i>Sequence of pptFlowBatchedLotInfo struct
    }

    @Data
    public static class FlowBatchedLotInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private long processSequenceNumber;
        private String lotStatus;                  //<i>lot Status
        private String transferStatus;             //<i>Transfer Status
        private ObjectIdentifier stockerID;                  //<i>stocker ID
        private ObjectIdentifier equipmentID;                //<i>eqp ID
        private String priorityClass;              //<i>Priority Class
        private ObjectIdentifier productID;                  //<i>Product ID
        private long flowBatchOperationCount;    //<i>Flow Batch Operation Count
        private long flowBatchLotSize;           //<i>Flow Batch lot Size
        private List<EntityInhibitAttributes> entityInhibitions;          //<i>Sequence of pptEntityInhibitAttributes struct
    }

    @Data
    public static class EffectCondition {
        private String phase;
        private String triggerLevel;

        public EffectCondition() {
        }

        public EffectCondition(String phase, String triggerLevel) {
            this.phase = phase;
            this.triggerLevel = triggerLevel;
        }
    }

    /**
     * description:
     * pptLoadedLot_struct
     *
     * @author PlayBoy
     * @date 2018/7/20
     */
    @Data
    public static class LoadedLot {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unloadPortID;
        private ObjectIdentifier controlJobID;
        private Long processSequenceNumber;
        private String lotType;
        private String loadPurposeType;
        private boolean operationTableFlag;
    }

    /**
     * description:
     * pptControlJobCassette_struct
     *
     * @author PlayBoy
     * @date 2018/7/23
     */
    @Data
    public static class ControlJobCassette {
        private Long loadSequenceNumber;    //<i>Load Sequence Number
        private ObjectIdentifier cassetteID;            //<i>Carrier ID
        private ObjectIdentifier loadPurposeType;       //<i>Load Purpose Type
        private ObjectIdentifier loadPortID;            //<i>Load port ID
        private ObjectIdentifier unloadPortID;          //<i>Unload port ID
        private List<ControlJobLot> controlJobLotList;      //<i>Sequence of Control Job lot
    }

    /**
     * description:
     * pptControlJobLot_struct
     *
     * @author PlayBoy
     * @date 2018/7/23
     */
    @Data
    public static class ControlJobLot {
        /**
         * Operation Start Flag
         */
        private Boolean operationStartFlag;
        /**
         * Monitor lot Flag
         */
        private Boolean monitorLotFlag;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class FutureHoldSearchKey {
        private ObjectIdentifier lotID;              //<i>lot ID
        private String holdType;           //<i>Hold Type
        private ObjectIdentifier reasonCodeID;       //<i>Reason CimCode ID
        private ObjectIdentifier userID;             //<i>User ID
        private ObjectIdentifier routeID;            //<i>Route ID
        private String operationNumber;    //<i>Operation Number
        private ObjectIdentifier relatedLotID;       //<i>Related lot ID
        private String phase;              //<i>Phase            // Add 2002-01-30
        private String triggerLevel;       //<i>Trigger Level    // Add 2002-01-30
        private String siInfo;             //<i>Reserved for SI customization
    }

    @Data
    public static class BankInLotResult {
        private ObjectIdentifier lotId;
        private OmCode returnCode;

        public BankInLotResult() {
        }

        public BankInLotResult(ObjectIdentifier lotId, OmCode returnCode) {
            this.lotId = lotId;
            this.returnCode = returnCode;
        }
    }

    @Data
    public static class FutureHoldListAttributes {
        private ObjectIdentifier lotID;                  //<i>lot ID //D4100087 add
        private Boolean cancelableFlag;         //<i>Cancelable Flag. If HoldType=SP_HoldType_FutureHold, a CancelableFlag is True.
        private String holdType;               //<i>Hold Type
        //<c>SP_HoldType_LotHold                   "HoldLot"
        //<c>SP_HoldType_BankHold                  "HoldLotInBank"
        //<c>SP_HoldType_FutureHold                "FutureHold"
        //<c>SP_HoldType_MergeHold                 "MergeHold"
        //<c>SP_HoldType_MonitorSPCHold            "MonitorSPCHold"
        //<c>SP_HoldType_MonitorSpecHold           "MonitorSpecHold"
        //<c>SP_HoldType_ReworkHold                "ReworkHold"
        //<c>SP_HoldType_SPCOutOfRangeHold         "SPCOutOfRangeHold"
        //<c>SP_HoldType_SpecOverHold              "SpecOverHold"
        //<c>SP_HoldType_WaitingMonitorResultHold  "WaitingMonitorHold"
        //<c>SP_HoldType_ProcessHold               "ProcessHold"
        //<c>SP_HoldType_RecipeHold                "RecipeHold"
        //<c>SP_HoldType_RunningHold               "RunningHold"
        //<c>SP_HoldType_ForceCompHold             "ForceCompHold"
        //<c>SP_QTimeOverHold                      "QTimeOverHold"
        //<c>SP_HoldType_PSM_Hold                  "PlannedSplitAndMargeHold"
        //<c>SP_AddToQueueErrHold                  "AddToQueueErrHold"
        private ObjectIdentifier routeID;                //<i>Route ID
        private String operationNumber;        //<i>Operation Number
        private String operationName;          //<i>Operation Name
        private ObjectIdentifier userID;                 //<i>User ID
        private String userName;               //<i>User Name
        private ObjectIdentifier reasonCodeID;           //<i>Reason CimCode ID
        private String reasonCodeDescription;  //<i>Reason CimCode Description
        private String reportTimeStamp;        //<i>Related Time Stamp
        private ObjectIdentifier relatedLotID;           //<i>Related lot ID
        private Boolean postFlag;               //<i>Post Flag            //D4100087 add
        private Boolean singleTriggerFlag;      //<i>Single Trigger Flag  //D4100087 add
        private String claimMemo;              //<i>Claim Memo
        private String siInfo;                             //<i>Reserved for SI customization
		private String department;
		private String section;
    }

    @Data
    public static class DurableSubStatusInfo {
        private String durableSubStatus;               //<i>durable Sub Status
        private String durableSubStatusName;           //<i>durable Sub Status Name
        private String durableSubStatusDescription;    //<i>durable Sub Status Description
        private String durableStatus;                  //<i>durable Status
        private Boolean availableForDurableFlag;        //<i>Process Available for durable Flag
        private Boolean changeFromOtherFlag;            //<i>Change from Other durable Status Flag
        private Boolean changeToOtherFlag;              //<i>Change to Other durable Status Flag
        private Boolean conditionalAvailableFlag;       //<i>Conditional Available Flag
        private List<String> availableSubLotTypes;           //<i>Sequence of Available Sub lot Type
        private List<String> nextTransitionDurableSubStatus; //<i>Sequence of Next Transition durable Sub Status
        private String siInfo;                         //<i>Reserved for SI customization
    }

    /**
     * description:
     * pptReticleDispatchJob_struct
     *
     * @author PlayBoy
     * @date 2018/7/30
     */
    @Data
    public static class ReticleDispatchJob {
        private String dispatchStationID;
        private String requestedTimestamp;
        private ObjectIdentifier reticleID;
        private ObjectIdentifier reticlePodID;
        private String reticleDispatchJobID;
        private ObjectIdentifier fromEquipmentID;
        private String fromEquipmentCategory;
        /**
         * Destination eqp ID
         */
        private ObjectIdentifier toEquipmentID;
        /**
         * Destination eqp category
         */
        private String toEquipmentCategory;
        private Long priority;
        private ObjectIdentifier requestUserID;
        private String jobStatus;
        private String jobStatusChangeTimestamp;
    }

    @Data
    public static class FutureHoldHistory {
        private String entryType;
        private String holdType;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier person;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private Boolean postFlag;
        private Boolean singleTriggerFlag;
        private ObjectIdentifier relatedLotID;
        private String claimMemo;
    }

    @Data
    public static class OperationProcessRefListAttributes {
        private Integer seqno;             //<i>Sequence Number. Not used.
        private ObjectIdentifier routeID;         //<i>Route ID
        private String operationNumber; //<i>Operation Number
        private ProcessRef processRef;  //<i>Process Reference
    }

    /**
     * description:
     * pptAPCBaseCassette_struck
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    @Data
    public static class ApcBaseCassette {
        private String cassetteID;
        private String loadSequenceNumber;
        private String loadPurposeType;
        private String loadPortID;
        private String unloadPortID;
        /**
         * lot information for APC
         */
        private List<ApcBaseLot> apcBaseLotList;
    }

    /**
     * description:
     * pptAPCBaseLot_struck
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    @Data
    public static class ApcBaseLot {
        /**
         * Operation Start Flag. TRUE means this lot is an object of operation start.
         */
        private boolean operationStartFlag;
        /**
         * Monitor lot Flag. TRUE means this lot is representative lot of monitor group.
         */
        private boolean monitorLotFlag;
        private String sendAheadType;
        private String specialInstructionID;
        private String lotID;
        private String lotType;
        private String subLotType;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private String productID;
        private String productType;
        private String productGroupID;
        private String technologyCode;
        private String lotStatus;
        private String dueTimeStamp;
        private String priorityClass;
        private String internalPriority;
        private String externalPriority;
        private Long totalWaferCount;
        private Long productWaferCount;
        private Long controlWaferCount;
        private Long totalGoodDieCount;
        private Long totalBadDieCount;
        /**
         * qtime Flag. If Qtime restriction exist, True.
         */
        private boolean qTimeFlag;
        private String lastClaimedTimeStamp;
        private String lastClaimedUserID;
        private String stateChangeTimeStamp;
        private String routeID;
        private String operationID;
        private String operationNumber;
        private String operationName;
        private Long operationPassCount;
        private String stageID;
        private String maskLevel;
        private String department;
        private String planStartTimeStamp;
        private String planEndTimeStamp;
        private String releaseWeek;
        private String queuedTimeStamp;
        private String testSpecID;
        private String inspectionType;
        private Long reworkCount;
        private String experimentSplitLot;
        private String inlineTestCode;
        private String jobClass;
        private String lineCode;
        private String lotGrade;
        private String qualityCode;
        private String lotProductType;
        private String familyCode;
        private String kerfPNEC;
        private String stepArrayPNEC;
        /**
         * Reticle Information for APC
         */
        private List<ApcBaseReticle> apcBaseReticleList;
        /**
         * Fixture Information for APC
         */
        private List<ApcBaseFixture> apcBaseFixtureList;
        /**
         * wafer Information for APC
         */
        private List<ApcBaseWafer> apcBaseWaferList;
        /**
         * APC System Information for APC
         */
        private List<ApcBaseApcSystem> apcBaseApcSystemList;
    }

    /**
     * description:
     * pptAPCBaseReticle_struct
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    @Data
    public static class ApcBaseReticle {
        private String reticleID;
        private String groupID;
    }

    /**
     * description:
     * pptAPCBaseFixture_struct
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    @Data
    public static class ApcBaseFixture {
        private String fixtureID;
        private String fixtureCategory;
    }

    /**
     * description:
     * pptAPCBaseWafer_struct
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    @Data
    public static class ApcBaseWafer {
        private String waferID;
        private Long slotNumber;
        private boolean controlWaferFlag;
        private boolean sendAheadWaferFlag;
        private boolean processFlag;
        private boolean experimentSplitWafer;
        /**
         * Sequence of Reticle Parameter for APC
         */
        private List<ApcBaseRecipeParameter> apcBaseRecipeParameterList;
        /**
         * Sequence of Experiment History for APC
         */
        private List<String> apcBaseExperimentalHistoryList;
        /**
         * Sequence of Run History for APC
         */
        private List<String> apcBaseRunHistoryList;
    }

    /**
     * description:
     * pptAPCBaseAPCSystem_struct
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    @Data
    public static class ApcBaseApcSystem {
        /**
         * APC System Name
         */
        private String apcBaseAPCSystemName;
        /**
         * APC System Function
         */
        private ApcBaseApcSystemFunction apcBaseAPCSystemFunction;
    }

    /**
     * description:
     * pptAPCBaseRecipeParameter_struct
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    @Data
    public static class ApcBaseRecipeParameter {
        /**
         * Recipe Parameter Name
         */
        private String name;
        /**
         * Recipe Parameter Value
         */
        private String value;
        /**
         * Recipe Parameter Lower Limit
         */
        private String valueLowerLimit;
        /**
         * Recipe Parameter Upper Limit
         */
        private String valueUpperLimit;
        /**
         * Use Current Setting Value Flag
         */
        private boolean useCurrentValueFlag;
    }

    /**
     * description:
     * pptAPCBaseAPCSystemFunction_struct
     *
     * @author PlayBoy
     * @date 2018/8/1
     */
    @Data
    public static class ApcBaseApcSystemFunction {
        /**
         * Function Type
         */
        String type;
        String controlFrequency;
        String description;
    }

    /**
     * description:
     * pptCandidateE10Status_struct
     *
     * @author PlayBoy
     * @date 2018/8/2
     */
    @Data
    public static class CandidateE10Status {
        /**
         * E10 Status
         */
        ObjectIdentifier e10Status;
        /**
         * Sequence of pptCandidateEqpStatus struct,can be replace by entity of eqpstate
         */
        List<Infos.CandidateEqpStatus> candidateEqpStatusList;

        public CandidateE10Status() {
        }

        public CandidateE10Status(boolean isInit) {
            if (isInit) {
                init();
            }
        }

        public void init() {
            this.e10Status = new ObjectIdentifier();
            this.candidateEqpStatusList = new ArrayList<>();
        }
    }

    /**
     * description:
     * pptCandidateEqpStatus_struct
     *
     * @author PlayBoy
     * @date 2018/8/2
     */
    @Data
    public static class CandidateEqpStatus {
        /**
         * eqp Status CimCode
         */
        ObjectIdentifier equipmentStatusCode;
        /**
         * eqp Status Name
         */
        String equipmentStatusName;
        /**
         * eqp Status Description
         */
        String equipmentStatusDescription;
        /**
         * Available Flag
         */
        boolean availableFlag;
    }

    /**
     * description:
     * pptEqpContainerPositionInfo_struct
     *
     * @author PlayBoy
     * @date 2018/8/8
     */
    @Data
    public static class EqpContainerPositionInfo {
        private ObjectIdentifier equipmentID;
        private List<EqpContainerPosition> eqpContainerPositionList;
    }

    /**
     * description:
     * pptQTimeActionRegistInfo_struct
     *
     * @author PlayBoy
     * @date 2018/8/8
     */
    @Data
    public static class QTimeActionRegisterInfo {
        private ObjectIdentifier lotID;
        /**
         * Sequence of lot Hold Action Information
         */
        private List<LotHoldReq> lotHoldList;
        /**
         * Sequence of Future Hold Action Information
         */
        private List<LotHoldReq> futureHoldList;
        /**
         * Sequence of Future Rework Action Information
         */
        private List<FutureReworkInfo> futureReworkList;
    }

    /**
     * description:
     * pptQTimeActionRegistInfo_struct
     *
     * @author PlayBoy
     * @date 2018/8/8
     */
    @Data
    public static class FutureReworkInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private String modifyTimeStamp;
        private ObjectIdentifier modifyUserID;
        /**
         * Sequence of Future Rework Detail Information
         */
        private List<FutureReworkDetailInfo> futureReworkDetailInfoList;
    }

    /**
     * description:
     * pptFutureReworkDetailInfo_struct
     *
     * @author PlayBoy
     * @date 2018/8/8
     */
    @Data
    public static class FutureReworkDetailInfo {
        private String trigger;
        private ObjectIdentifier reworkRouteID;
        private String returnOperationNumber;
        private ObjectIdentifier reasonCodeID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/12/5 11:16:12
     */
    @Data
    public static class ExperimentalLotInfo {
        private ObjectIdentifier lotFamilyID;
        private ObjectIdentifier splitRouteID;
        private ObjectIdentifier originalRouteID;
        private ObjectIdentifier modifyUserID;
        private String splitOperationNumber;
        private String originalOperationNumber;
        private String testMemo;
        private String actionTimeStamp;
        private String modifyTimeStamp;
        private Boolean actionEMail;
        private Boolean actionHold;
        //add auto separate and combine  - jerry
        private Boolean actionSeparateHold;
        private Boolean actionCombineHold;
        private Boolean execFlag;
        private List<ExperimentalLotDetailInfo> strExperimentalLotDetailInfoSeq;
        private Object siInfo;
        //add run card flag for UI confirm
        private Boolean runCardFlag;

        //add planSplitJobID for history
        private String psmJobID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/12/5 11:20:01
     */
    @Data
    public static class ExperimentalLotDetailInfo {
        private ObjectIdentifier subRouteID;
        private String returnOperationNumber;
        private String mergeOperationNumber;
        private String memo;
        private String actionTimeStamp;
        private List<ObjectIdentifier> waferIDs;
        private Boolean dynamicFlag;
        private Boolean execFlag;
        private Object siInfo;
        //add run card psmKey     //use doc select psm_key to update and  psm delete the run card data
        private String psmKey;
        private Boolean modifyFlag;
    }

    /**
     * description:
     * pptSchdlChangeReservation__110_struct
     *
     * @author PlayBoy
     * @date 2018/8/10
     */
    @Data
    public static class ScheduleChangeReservation {
        private String eventID;
        private ObjectIdentifier objectID;
        private String objectType;
        private ObjectIdentifier targetRouteID;
        private String targetOperationNumber;
        private ObjectIdentifier productID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private String subLotType;
        private String startDate;
        private String startTime;
        private String endDate;
        private String endTime;
        private boolean eraseAfterUsedFlag;
        private Long maxLotCnt;
        private Long applyLotCnt;
        private String status;
        private String actionCode;
        /**
         * Created by lot Information Change Flag
         */
        boolean lotInfoChangeFlag;
    }

    @Data
    public static class LotInControlJobInfo {
        private Boolean monitorLotFlag;
        private ObjectIdentifier lotID;
    }

    @Data
    public static class MachineCassette {
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unloadPortID;
        private String status;
        private String loadPurposeType;
        private List<MachineCassetteLot> machineCassetteLots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MachineCassetteLot {
        private ObjectIdentifier lotID;
        private boolean operationStartFlag;
        private boolean monitorLotFlag;
    }

    @Data
    public static class LotWaferInfoAttributes {
        private ObjectIdentifier waferID;            //<i>wafer ID
        private ObjectIdentifier cassetteID;         //<i>Carrier ID
        private String aliasWaferName;     //<i>Alias wafer Name
        private Integer slotNumber;         //<i>Slot Number
        private ObjectIdentifier productID;          //<i>Product ID
        private Integer grossUnitCount;     //<i>Gross Unit Count
        private Integer goodUnitCount;      //<i>Good Unit Count
        private Integer repairUnitCount;    //<i>Repair Unit Count
        private Integer failUnitCount;      //<i>Fail Unit Count
        private Boolean controlWaferFlag;   //<i>Control wafer Flag
        private Boolean STBAllocFlag;       //<i>STB Allocation Flag. If Recyclable, True.
        private Integer eqpMonitorUsedCount;//<i>eqp Monitor Used Count
    }

    @Data
    public static class UpdateLotAttributes {
        private ObjectIdentifier lotID;
        private ObjectIdentifier productID;
        private String customerCode;
        private String manufacturingOrderNumber;
        private String lotOwner;
        private String subLotType;
        private ObjectIdentifier routeID;
        private String lotGenerationType;
        private String schedulingMode;
        private String productDefinitionMode;                 //<i>Product Definition Mode
        private String priorityClass;                         //<i>Priority Class
        private String externalPriority;                      //<i>External Priority
        private String plannedStartTime;                      //<i>Planned Start Time
        private String plannedFinishTime;                     //<i>Planned Finished Time
        private String lotComment;                            //<i>lot Comment
        private Integer productQuantity;                       //<i>Product Quantity
        private List<DirectedSourceLot> directedSourceLotList;
        private List<ReleaseLotSchedule> updateLotScheduleList;

    }

    @Data
    public static class ActualStartInformationForPO {
        private ObjectIdentifier assignedMachine;
        private String assignedPortGroup;
        private ObjectIdentifier assignedLogicalRecipe;
        private ObjectIdentifier assignedMachineRecipe;
        private String assignedPhysicalRecipe;
        private List<Infos.StartReticleInfo> assignedReticles;
        private List<Infos.StartFixtureInfo> assignedFixtures;
        private String assignedRecipeParameterChangeType;
        private List<Infos.StartRecipeParameterSetInfo> assignedRecipeParameterSets;
        private Boolean assignedDataCollectionFlag;
        private List<Infos.DataCollectionInfo> assignedDataCollections;
        private List<String> assignedSamplingWafers;
    }

    @Data
    public static class StartReticleInfo {
        private Integer sequenceNumber;
        private ObjectIdentifier reticleID;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartFixtureInfo {
        private ObjectIdentifier fixtureID;
        private String fixtureCategory;

        public StartFixtureInfo(ProcessDTO.StartFixtureInfo fixtureInfo) {
            this.fixtureID = fixtureInfo.getFixtureID();
            this.fixtureCategory = fixtureInfo.getFixtureCategory();
        }

        public ProcessDTO.StartFixtureInfo convert() {
            ProcessDTO.StartFixtureInfo retVal = new ProcessDTO.StartFixtureInfo();
            retVal.setFixtureID(this.fixtureID);
            retVal.setFixtureCategory(this.fixtureCategory);
            return retVal;
        }
    }

    @Data
    public static class DataCollectionInfo {
        private ObjectIdentifier dataCollectionDefinitionID;
        private String description;
        private String dataCollectionType;
        private List<DataCollectionItemInfo> dcItems;
        private Boolean calculationRequiredFlag;
        private Boolean specCheckRequiredFlag;
        private ObjectIdentifier dataCollectionSpecificationID;
        private String dcSpecDescription;
        private List<DataCollectionSpecInfo> dcSpecs;
        private ObjectIdentifier previousDataCollectionDefinitionID;
        private ObjectIdentifier previousOperationID;
        private String previousOperationNumber;
        private String edcSettingType;

        public ProcessDTO.DataCollectionInfo convert() {
            ProcessDTO.DataCollectionInfo dataCollectionInfo = new ProcessDTO.DataCollectionInfo();
            dataCollectionInfo.setDataCollectionType(this.getDataCollectionType());
            dataCollectionInfo.setDataCollectionDefinitionID(this.getDataCollectionDefinitionID());
            dataCollectionInfo.setDataCollectionSpecificationID(this.getDataCollectionSpecificationID());
            dataCollectionInfo.setPreviousDataCollectionDefinitionID(this.getPreviousDataCollectionDefinitionID());
            dataCollectionInfo.setCalculationRequiredFlag(this.getCalculationRequiredFlag());
            dataCollectionInfo.setDcSpecDescription(this.getDcSpecDescription());
            dataCollectionInfo.setPreviousOperationID(this.getPreviousOperationID());
            dataCollectionInfo.setPreviousOperationNumber(this.getPreviousOperationNumber());
            dataCollectionInfo.setSpecCheckRequiredFlag(this.getSpecCheckRequiredFlag());
            dataCollectionInfo.setDescription(this.getDescription());
            dataCollectionInfo.setEdcSettingType(this.edcSettingType);

            // EDC Specification Info
            dataCollectionInfo.setDcSpecs(Optional.ofNullable(this.getDcSpecs())
                    .map(data -> data.stream()
                            .map(Infos.DataCollectionSpecInfo::convert)
                            .collect(Collectors.toList())
                    )
                    .orElseGet(Collections::emptyList)
            );

            // EDC Items Info
            dataCollectionInfo.setDcItems(Optional.ofNullable(this.getDcItems())
                    .map(data -> data.stream()
                            .map(Infos.DataCollectionItemInfo::convert)
                            .collect(Collectors.toList())
                    )
                    .orElseGet(Collections::emptyList)
            );
            return dataCollectionInfo;
        }
    }


    @Data
    public static class DataCollectionItemInfo {
        private String dataCollectionItemName;
        private String dataCollectionMode;
        private String dataCollectionUnit;
        private String dataType;
        private String itemType;
        private String measurementType;
        private ObjectIdentifier waferID;
        private String waferPosition;
        private String sitePosition;
        private Boolean historyRequiredFlag;
        private String calculationType;
        private String calculationExpression;
        private String dataValue;
        private String targetValue;
        private String specCheckResult;
        private String actionCodes;
        private int seqNo;
        private Integer waferCount;
        private Integer siteCount;

        public ProcessDTO.DataCollectionItemInfo convert() {
            ProcessDTO.DataCollectionItemInfo itemInfo = new ProcessDTO.DataCollectionItemInfo();
            itemInfo.setDataCollectionItemName(this.getDataCollectionItemName());
            itemInfo.setDataCollectionMode(this.getDataCollectionMode());
            itemInfo.setDataCollectionUnit(this.getDataCollectionUnit());
            itemInfo.setDataType(this.getDataType());
            itemInfo.setItemType(this.getItemType());
            itemInfo.setMeasurementType(this.getMeasurementType());
            itemInfo.setWaferID(this.getWaferID());
            itemInfo.setWaferPosition(this.getWaferPosition());
            itemInfo.setSitePosition(this.getSitePosition());
            itemInfo.setHistoryRequiredFlag(this.getHistoryRequiredFlag());
            itemInfo.setCalculationType(this.getCalculationType());
            itemInfo.setCalculationExpression(this.getCalculationExpression());
            itemInfo.setDataValue(this.getDataValue());
            itemInfo.setTargetValue(this.getTargetValue());
            itemInfo.setSpecCheckResult(this.getSpecCheckResult());
            itemInfo.setActionCodes(this.getActionCodes());
            itemInfo.setSeqNo(this.seqNo);
            itemInfo.setWaferCount(this.waferCount);
            itemInfo.setSiteCount(this.siteCount);
            return itemInfo;
        }
    }

    @Data
    public static class DataCollectionSpecInfo {
        private String dataItemName;
        private Boolean screenLimitUpperRequired;
        private Double screenLimitUpper;
        private String actionCodesUscrn;
        private Boolean screenLimitLowerRequired;
        private Double screenLimitLower;
        private String actionCodesLscrn;
        private Boolean specLimitUpperRequired;
        private Double specLimitUpper;
        private String actionCodesUsl;
        private Boolean specLimitLowerRequired;
        private Double specLimitLower;
        private String actionCodesLsl;
        private Boolean controlLimitUpperRequired;
        private Double controlLimitUpper;
        private String actionCodesUcl;
        private Boolean controlLimitLowerRequired;
        private Double controlLimitLower;
        private String actionCodesLcl;
        private Double target;
        private String tag;
        private String dcSpecGroup;

        public ProcessDTO.DataCollectionSpecInfo convert() {
            ProcessDTO.DataCollectionSpecInfo specInfo = new ProcessDTO.DataCollectionSpecInfo();
            specInfo.setDataItemName(this.getDataItemName());
            specInfo.setScreenLimitUpperRequired(this.getScreenLimitUpperRequired());
            specInfo.setScreenLimitUpper(this.getScreenLimitUpper());
            specInfo.setActionCodesUscrn(this.getActionCodesUscrn());
            specInfo.setScreenLimitLowerRequired(this.getScreenLimitLowerRequired());
            specInfo.setScreenLimitLower(this.getScreenLimitLower());
            specInfo.setActionCodesLscrn(this.getActionCodesLscrn());
            specInfo.setSpecLimitUpperRequired(this.getSpecLimitUpperRequired());
            specInfo.setSpecLimitUpper(this.getSpecLimitUpper());
            specInfo.setActionCodesUsl(this.getActionCodesUsl());
            specInfo.setSpecLimitLowerRequired(this.getSpecLimitLowerRequired());
            specInfo.setSpecLimitLower(this.getSpecLimitLower());
            specInfo.setActionCodesLsl(this.getActionCodesLsl());
            specInfo.setControlLimitUpperRequired(this.getControlLimitUpperRequired());
            specInfo.setControlLimitUpper(this.getControlLimitUpper());
            specInfo.setActionCodesUcl(this.getActionCodesUcl());
            specInfo.setControlLimitLowerRequired(this.getControlLimitLowerRequired());
            specInfo.setControlLimitLower(this.getControlLimitLower());
            specInfo.setActionCodesLcl(this.getActionCodesLcl());
            specInfo.setTarget(this.getTarget());
            specInfo.setTag(this.getTag());
            specInfo.setDcSpecGroup(this.getDcSpecGroup());
            return specInfo;
        }
    }

    @Data
    @NoArgsConstructor
    public static class EqpMonitorJobInfo {
        private ObjectIdentifier eqpMonitorJobID;                //<i>eqp Monitor Job ID
        private ObjectIdentifier equipmentID;                    //<i>eqp ID
        private ObjectIdentifier chamberID;                      //<i>Chamber ID
        private ObjectIdentifier eqpMonitorID;                   //<i>eqp Monitor ID
        private String monitorJobStatus;               //<i>Status of eqp Monitor Job
        private List<EqpMonitorLotInfo> strEqpMonitorLotInfoSeq;        //<i>List of eqp Monitor lots
        private Integer retryCount;                     //<i>Current Retry Count
        private String startTimeStamp;                 //<i>Start Time of the eqp Monitor Job
        private ObjectIdentifier startUser;                      //<i>Start User of eqp Monitor Job
        private String lastClaimedTimeStamp;           //<i>Last Claimed Time Stamp
        private ObjectIdentifier lastClaimedUser;                //<i>Last Claimed User


        public EqpMonitorJobInfo(MachineDTO.EqpMonitorJobInfo eqpMonitorJobInfo) {
            this.eqpMonitorJobID = eqpMonitorJobInfo.getEqpMonitorJobID();
            this.equipmentID = eqpMonitorJobInfo.getEquipmentID();
            this.chamberID = eqpMonitorJobInfo.getChamberID();
            this.eqpMonitorID = eqpMonitorJobInfo.getEqpMonitorID();
            this.monitorJobStatus = eqpMonitorJobInfo.getMonitorJobStatus();
            this.strEqpMonitorLotInfoSeq = Optional.ofNullable(eqpMonitorJobInfo.getStrEqpMonitorLotInfoSeq())
                    .map(data -> data.stream().map(EqpMonitorLotInfo::new).collect(Collectors.toList()))
                    .orElseGet(() -> Collections.emptyList());
            this.retryCount = eqpMonitorJobInfo.getRetryCount();
            this.startTimeStamp = eqpMonitorJobInfo.getStartTimeStamp();
            this.startUser = eqpMonitorJobInfo.getStartUser();
            this.lastClaimedTimeStamp = eqpMonitorJobInfo.getLastClaimedTimeStamp();
            this.lastClaimedUser = eqpMonitorJobInfo.getLastClaimedUser();
        }
    }

    @Data
    @NoArgsConstructor
    public static class EqpMonitorLotInfo {
        private ObjectIdentifier lotID;                              //<i>lot ID
        private String monitorLotStatus;                   //<i>lot Status for eqp Monitor
        private Integer startSeqNo;                         //<i>Start Order Control at Monitor Operation
        private String monitorOpeKey;                      //<i>Key for eqp Monitor Job created Operation
        private String monitorRouteID;                     //<i>Monitor Route ID
        private String monitorOpeNo;                       //<i>Monitor Operation Number
        private Boolean exitFlag;                           //<i>True when lot passes Exit Operation
        private Integer result;                             //<i>True when Spec/SPC check fails for the lot

        public EqpMonitorLotInfo(MachineDTO.EqpMonitorLotInfo eqpMonitorLotInfo) {
            this.lotID = eqpMonitorLotInfo.getLotID();
            this.monitorLotStatus = eqpMonitorLotInfo.getMonitorLotStatus();
            this.startSeqNo = eqpMonitorLotInfo.getStartSeqNo();
            this.monitorOpeKey = eqpMonitorLotInfo.getMonitorOpeKey();
            this.monitorRouteID = eqpMonitorLotInfo.getMonitorRouteID();
            this.monitorOpeNo = eqpMonitorLotInfo.getMonitorOpeNo();
            this.exitFlag = eqpMonitorLotInfo.getExitFlag();
            this.result = eqpMonitorLotInfo.getResult();
        }
    }

    @Data
    public static class ActiveModuleInfo {
        private String processFlowID;
        private String processOperationSpecificationsID;
        private Integer sequenceNumber;

        private String mainProcessDefinitionID;
        private String stageID;
    }


    @Data
    public static class FlowSectionControl {
        private Boolean flowSectionTargetFlag;
        private Boolean flowSectionEntryFlag;
        private String flowSectionID;
        private String flowSectionCategory;

    }

    @Data
    public static class PDModuleInfo {
        private String operationID;
        private String operationType;
        private String planStartTime;
    }

    @Data
    public static class LotHoldRecordInfo {
        private String dKey;
        private String holdType;
        private ObjectIdentifier holdReasonID;
        private ObjectIdentifier holdUserID;
        private String holdTime;
        private ObjectIdentifier relatedLotID;
        private String holdClaimMemo;
        private String responsibleOperationMark;
    }

    @Data
    @ToString
    public static class InProcessingLot {
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unloadPortID;
        private Long unloadSequenceNumber;
        private ObjectIdentifier carrierID;
        private String holdState;
    }

    @Data
    public static class ScrapWafers {
        private ObjectIdentifier waferID;         //<i>wafer ID
        private ObjectIdentifier reasonCodeID;    //<i>Reason CimCode ID

        public ScrapWafers() {
        }

        public ScrapWafers(ObjectIdentifier reasonCodeID) {
            this.reasonCodeID = reasonCodeID;
        }

        public ScrapWafers(ObjectIdentifier waferID, ObjectIdentifier reasonCodeID) {
            this.waferID = waferID;
            this.reasonCodeID = reasonCodeID;
        }
    }

    /**
     * description:pptCandidateChamberStatusInfo_struct
     *
     * @author PlayBoy
     * @date 2018/9/21 15:03:20
     */
    @Data
    public static class CandidateChamberStatusInfo {
        private ObjectIdentifier chamberID;
        private ObjectIdentifier currentStatusCode;
        private String currentStatusName;
        private String currentStatusDescription;
        private Boolean chamberAvailableFlag;
        private CandidateE10ChamberStatus candidateCurrentE10;
        private List<CandidateE10ChamberStatus> candidateOtherE10List;
    }

    /**
     * description:pptCandidateE10ChamberStatus_struct
     *
     * @author PlayBoy
     * @date 2018/9/21 15:07:23
     */
    @Data
    public static class CandidateE10ChamberStatus {
        private ObjectIdentifier e10StatusCode;
        private List<CandidateChamberStatus> candidateChamberStatusList;
    }

    /**
     * description: pptCandidateChamberStatus_struct
     *
     * @author PlayBoy
     * @date 2018/9/21 15:10:33
     */
    @Data
    public static class CandidateChamberStatus {
        private ObjectIdentifier chamberStatusCode;
        private String chamberStatusName;
        private String chamberStatusDescription;
        private Boolean availableFlag;
    }

    @Data
    public static class LotTypeSublotTypeInfo implements Serializable {
        private String id;
        private String lotTypeID;
        private String lotTypeObj;
        private String subLotType;
        private String description;
        private String leadingChar;

        public LotTypeSublotTypeInfo() {
        }

        public LotTypeSublotTypeInfo(String id, String lotTypeID, String subLotType, String description, String leadingChar) {
            this.id = id;
            this.lotTypeID = lotTypeID;
            this.subLotType = subLotType;
            this.description = description;
            this.leadingChar = leadingChar;
        }
    }

    @Data
    @Deprecated
    public static class SplitData {
        private List<CimWaferDO> splitProducts;
        private String newLotID;
    }

    @Data
    public static class LotSpliteData {
        private List<WaferInfo> splitProducts;
        private String newLotID;
    }

    @Data
    public static class ScrapCancelWafers {
        private ObjectIdentifier waferID;          //<i>wafer ID
        private Integer slotNumber;                  //<i>wafer's slot number
        private ObjectIdentifier reasonCodeID;     //<i>Reason CimCode ID
        private ObjectIdentifier reasonedLotID;    //<i>Reasoned lot ID
        private String scrappedTimeStamp;         //<i>Time stamp of scrapped

        public ScrapCancelWafers() {
        }

        public ScrapCancelWafers(ObjectIdentifier waferID, Integer slotNumber, ObjectIdentifier reasonCodeID, ObjectIdentifier reasonedLotID, String scrappedTimeStamp) {
            this.waferID = waferID;
            this.slotNumber = slotNumber;
            this.reasonCodeID = reasonCodeID;
            this.reasonedLotID = reasonedLotID;
            this.scrappedTimeStamp = scrappedTimeStamp;
        }
    }

    @Data
    public static class EntityInhibitDetailAttributes {
        private List<EntityIdentifier> entities;
        private List<String> subLotTypes;
        private String startTimeStamp;
        private String endTimeStamp;
        private String reasonCode;
        private String reasonDesc;
        private String memo;
        private ObjectIdentifier ownerID;
        private String claimedTimeStamp;
        private String status;
        //private EntityInhibitAttributes entityInhibitAttributes;
        private List<EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos;
        private List<EntityInhibitExceptionLotInfo> entityInhibitExceptionLotInfos;
        //enhance constraint need
        private List<Infos.EntityIdentifier> exceptionEntities;
        private String functionRule;
        private boolean specTool;
    }

    @Data
    public static class ConstraintDetailAttributes {
        private List<EntityIdentifier> entities;
        private List<String> subLotTypes;
        private String startTimeStamp;
        private String endTimeStamp;
        private String constraintType;
        private String memo;
        private String reasonCode;
        private String status;
        private ObjectIdentifier ownerID;
        private String claimedTimeStamp;
        private List<Infos.EntityIdentifier> exceptionEntities;
        private List<Infos.EntityInhibitExceptionLotInfo> exceptionLotList;
        private String functionRule;
        private boolean specTool;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/3/20 10:38
     */
    @Data
    public static class InventoriedReticlePodInfo {
        private ObjectIdentifier reticlePodID;
        private List<ObjectIdentifier> reticleID;
        private String returnCode;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/3/20 10:38
     */
    @Data
    public static class StockerLoadLotDeleteIn {
        private ObjectIdentifier stockerID;
        private ObjectIdentifier reticlePodPortID;
        private Object siInfo;
    }

    @Data
    public static class EntityInhibitReasonDetailInfo {
        private String relatedLotID;                   //<i>Related lot ID
        private String relatedControlJobID;            //<i>Related Control Job ID
        private String relatedFabID;                   //<i>Related Fab ID
        private String relatedRouteID;                 //<i>Related Main PD ID
        private String relatedProcessDefinitionID;     //<i>Related Process Definition ID
        private String relatedOperationNumber;         //<i>Related Operation Number
        private String relatedOperationPassCount;      //<i>Related Operation Pass Count
        private List<EntityInhibitSpcChartInfo> strEntityInhibitSpcChartInfos;  //<i>Sequence of Related SPC Chart
    }

    @Data
    public static class EntityInhibitSpcChartInfo {
        private String relatedSpcDcType;            //<i>Related SPC DC Type
        private String relatedSpcChartGroupID;      //<i>Related SPC Chart Group ID
        private String relatedSpcChartID;           //<i>Related SPC Chart ID
        private String relatedSpcChartType;         //<i>Related SPC Chart Type
        private String relatedSpcChartUrl;          //<i>Related SPC Chart URL
    }

    @Data
    public static class EntityInhibitDetailInfo {
        private ObjectIdentifier entityInhibitID;          //<i>Entity Inhibition ID
        private boolean specTool;
        private EntityInhibitDetailAttributes entityInhibitDetailAttributes;  //<i>Detail Attributes of the Entity Inhibition
    }

    @Data
    public static class ConstraintEqpDetailInfo {
        private ObjectIdentifier entityInhibitID;          //<i>Entity Inhibition ID
        private ConstraintDetailAttributes entityInhibitDetailAttributes;  //<i>Detail Attributes of the Entity Inhibition
    }

    @Data
    public static class ConstraintHistoryDetailInfo {
        private String id;
        private String constriantID;
        private String historyCategory;
        private String startTime;
        private String endTime;
        private String resonCode;
        private String resonDescription;
        private String description;
        private String claimTime;
        private String trxWorkTime;
        private User userID;
        private String memo;
        private String storeTime;
        private String reportTime;
        private String restrictContext;
        private String type;
        private Boolean specificTool;
        private List<ExceptionEntityRecord> exceptionEntiyRecords;
        private List<EntityRecord> entityRecords;
    }

    @Data
    public static class EntityRecord {
        private String constraintType;
        private EntityIdentifier entityIdentifier;
        private String entityAttribute;
    }

    @Data
    public static class ExceptionEntityRecord {
        private String classType;
        private EntityIdentifier entityIdentifier;
        private String entityAttribute;
    }
    @Data
    public static class ConstraintHistoryParam {
        private ObjectIdentifier equipmentID;
        private String functionRule;
        private SearchCondition searchCondition;
    }

    @Data
    public static class QueriedEntityInhibit {
        private String id;
        private String entityInhibitID;
        private String entityInhibitDescription;
        private String entityInhibitStartTime;
        private String entityInhibitEndTime;
        private String entityInhibitChangeTime;
        private String entityInhibitOwnerID;
        private String entityInhibitOwnerObj;
        private String entityInhibitRsnCode;
        private String entityInhibitRsnObj;
        private String entityInhibitClaimMemo;
        private int entityInhibitEntityDataSeqNo;
        private String entityInhibitEntityClassName;
        private String entityInhibitEntityID;
        private String entityInhibitEntityObj;
        private String entityInhibitEntityAttrib;
        private int entityInhibitSlotTPDataSeqNo;
        private String entityInhibitSlotTPSubLotType;
        private String codeDescription;
    }

    /**
     * description:pptLot_struct
     *
     * @author PlayBoy
     * @date 2018/10/8 15:19:11
     */
    @Data
    public static class PLot {
        private ObjectIdentifier lotID;
        private List<PWafer> waferList;
    }

    /**
     * description:pptLot_struct
     *
     * @author PlayBoy
     * @date 2018/10/8 15:19:11
     */
    @Data
    public static class PWafer {
        private ObjectIdentifier waferID;
        private Integer slotNumber;
        private String aliasName;
    }

    @Data
    public static class ScrapHistories {
        private ObjectIdentifier lotID;         //<i>lot ID
        private ObjectIdentifier waferID;       //<i>wafer ID
        private String scrappedTimeStamp;    //<i>Scrapped Time Stamp
    }

    @Data
    public static class CodeInfo {
        private ObjectIdentifier code;          //<i>CimCode
        private String codeName;      //<i>CimCode Name
        private String categoryID;    //<i>Category ID
        private String description;   //<i>Description
        private String priorityObj;   //<i>Priority Obj
    }

    @Data
    public static class ConnectedRouteList {
        private ObjectIdentifier routeID;                   //<i>Route ID
        private String routeDescription;          //<i>Route Description
        private ObjectIdentifier manufacturingLayerID;      //<i>Manufacturing Layer ID
        private String routeOwner;                //<i>Route Owner
        private ObjectIdentifier startBankID;               //<i>Start bank ID
        private ObjectIdentifier endBankID;                 //<i>End bank ID
        private ObjectIdentifier returnRouteID;             //<i>Return Route ID
        private String returnOperationNumber;     //<i>Return Operation Number
    }

    @Data
    public static class ProcessFlowConnection {
        private ObjectIdentifier branchPD;
        private String returnOperationNumber;
    }

    @Deprecated
    @Data
    public static class ExceptionLotRecord {
        private ObjectIdentifier lotID;
        private Boolean singleTriggerFlag;
        private Boolean usedFlag;
        private ObjectIdentifier claimUserID;
        private Timestamp claimTimeStamp;
        private String claimMemo;
    }

    @Data
    public static class EntityInhibitExceptionLotInfo {
        private ObjectIdentifier lotID;             //<i>Entity Inhibit Exception lot
        private Boolean singleTriggerFlag; //<i>0:Multi  1:Single
        private Boolean usedFlag;          //<i>0:NotUse 1:Used
        private ObjectIdentifier claimUserID;       //<i>Owner ID of who registered the Inhibit Exception.
        private String claimMemo;         //<i>Comment for Inhibit Exception
        private String claimTime;         //<i>Claimed Time Stamp
    }

    /**
     * description:
     * <p>pptCollectedDataItem_struct</p>
     *
     * @author PlayBoy
     * @date 2018/10/16 09:51:23
     */
    @Data
    public static class CollectedDataItem {
        private ObjectIdentifier lotID;
        private ObjectIdentifier dataCollectionDefinitionID;
        private String dataCollectionItemName;
        private String dataCollectionMode;
        private String dataType;
        private String itemType;
        private String measurementType;
        private String sitePosition;
        private String edcSettingType;
        private Integer waferCount;
        private Integer siteCount;
        private List<CollectedData> collectedDataList;
    }

    /**
     * description:
     * <p>pptCollectedData_struct</p>
     *
     * @author PlayBoy
     * @date 2018/10/16 09:59:27
     */
    @Data
    public static class CollectedData {
        private ObjectIdentifier waferID;
        private String waferPosition;
        private String processJobID;
        private String processJobPosition;
        private String dataValue;
    }

    @Data
    public static class PosCollectedData {
        private ObjectIdentifier waferID;
        private String waferPosition;
        private String processJobID;
        private String processJobPosition;
        private String dataValue;
        private String dataItemName;
        private String specCheckResult;
        private String valType;
        private String sitePosition;
        private String value;
    }

    /**
     * description:
     * <p>posProcessWafer_struct</p>
     * <p> merge with pptProcessWafer__120_struct by Sun</p>
     *
     * @author PlayBoy
     * @date 2018/10/16 10:47:41
     */
    @Data
    public static class ProcessWafer {
        private ObjectIdentifier cassetteID;
        private Integer slotNumber;
        private ObjectIdentifier waferID;
        private String aliasWaferName;
        private ObjectIdentifier lotID;
        private Boolean samplingWaferFlag;
        private String processJob;
        private String processJobPosition;
    }

    @Data
    public static class PosProcessWafer {
        private String waferID;
        private boolean samplingWaferFlag;
        private String prcsJob;
        private String prcsJobPosition;
    }

    /**
     * description:
     * <p>posWaferInfo_struct</p>
     *
     * @author PlayBoy
     * @date 2018/10/16 13:38:42
     */
    @Data
    public static class WaferInfo {
        private ObjectIdentifier waferID;
        private String originalIdentifier;
        private ObjectIdentifier materialContainer;
        private Boolean controlWafer;
        private Boolean stbAllocated;
        private String scrapState;
        private Integer nominalSize;
        private Integer position;
        private String partNumber;
        private String serialNumber;
        private ObjectIdentifier lotID;
        private ObjectIdentifier previousLot;
        private ObjectIdentifier productSpecification;
        private ObjectIdentifier subProductSpecification;
        private ObjectIdentifier lastClaimedPerson;
        private String lastClaimedTimeStamp;
        private Integer recycleLimit;
        private Integer recycleCount;
        private Integer totalDiceQuantity;
        private Integer goodDiceQuantity;
        private Integer repairedDiceQuantity;
        private Integer badDiceQuantity;
        private List<StackedWafer> stackedWafers;
        private Integer eqpMonitorUsedCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Wafer {
        private ObjectIdentifier waferID;
        private long slotNumber;
    }

    @Data
    public static class WaferSorter {
        private ObjectIdentifier waferID;
        private String aliasName;
    }

    /**
     * description:
     *
     * @author PlayBoy
     * @date 2018/10/16 13:45:26
     */
    @Data
    public static class StackedWafer {
        private String previousAliasWaferName;
        private ObjectIdentifier topWaferID;
        private String topAliasWaferName;
        private ObjectIdentifier topLotID;
        private String stackedTimeStamp;
        private long materialOffset;
        private String fabID;
    }

    @Data
    public static class EqpPortAttributes {
        private ObjectIdentifier portID;                        //<i>port ID
        private String portName;                      //<i>port name
        private ObjectIdentifier ownerID;                       //<i>Owner ID
        private Integer loadSequenceNumber;            //<i>Load Sequence Number
        private Integer unloadSequenceNumber;          //<i>Unload Sequence Number
        private String loadPurposeType;               //<i>Load Purpose Type
        //<c>SP_LoadPurposeType_Any                 "Any Purpose"
        //<c>SP_LoadPurposeType_Other               "Other"
        //<c>SP_LoadPurposeType_EmptyCassette       "Empty cassette"
        //<c>SP_LoadPurposeType_FillerDummy         "Filler Dummy lot"
        //<c>SP_LoadPurposeType_ProcessLot          "Process lot"
        //<c>SP_LoadPurposeType_ProcessMonitorLot   "Process Monitor lot"
        //<c>SP_LoadPurposeType_SideDummyLot        "Side Dummy lot"
        //<c>SP_LoadPurposeType_WaitingMonitorLot   "Waiting Monitor lot"
        //<c>SP_LoadPurposeType_InternalBuffer      "Internal Buffer Eqp"
        private String E10Status;                     //<i>E10 status
        private ObjectIdentifier equipmentID;                   //<i>eqp ID
        private String portUsage;                     //<i>port Usage
        //<c>CIMFW_PortRsc_Input        "INPUT"
        //<c>CIMFW_PortRsc_Output       "OUTPUT"
        //<c>CIMFW_PortRsc_InputOutput  "INPUT_OUTPUT"
        private String portUsageType;                 //<i>port Usage type
        private ObjectIdentifier associatedPortID;              //<i>Associated port ID
        private String portGroup;                     //<i>port Group Name
        private String portState;                     //<i>port State
        //<c>SP_PortRsc_PortState_LoadAvail         "LoadAvail"
        //<c>SP_PortRsc_PortState_LoadReq           "LoadReq"
        //<c>SP_PortRsc_PortState_LoadComp          "LoadComp"
        //<c>SP_PortRsc_PortState_UnloadReq         "UnloadReq"
        //<c>SP_PortRsc_PortState_UnloadComp        "UnloadComp"
        //<c>SP_PortRsc_PortState_UnloadAvail       "UnloadAvail"
        //<c>SP_PortRsc_PortState_Unknown           "-"
        //<c>SP_PortRsc_PortState_UnknownForTCS     "Unknown"
        //<c>SP_PortRsc_PortState_Down              "Down"
        private String lastClaimedTimeStamp;          //<i>Last Claimed TimeStamp
        private ObjectIdentifier lastClaimedUserID;             //<i>Last Claimed User ID
        private String lastMaintTimeStamp;            //<i>Last Maint TimeStamp
        private ObjectIdentifier lastMaintUserID;               //<i>Last Maint User ID
        private String statusChangeTimeStamp;         //<i>Status change TimeStamp
        private ObjectIdentifier statusChangeUserID;            //<i>Status change User ID
        private String portClaimMode;                 //<i>port claim mode
        private String portTransferMode;              //<i>port transfer mode
        private String portLotSelMode;                //<i>port lot sel mode
        private String portQueueMode;                 //<i>port queue mode
        private String dispatchState;                 //<i>dispatch State
        //<c>SP_PortRsc_DispatchState_Required          "Required"
        //<c>SP_PortRsc_DispatchState_Dispatched        "Dispatched"
        //<c>SP_PortRsc_DispatchState_NotDispatched     "NotDispatched"
        //<c>SP_PortRsc_DispatchState_Error             "Error"
        private String portDispatchTimeStamp;         //<i>port dispatch TimeStamp
        private ObjectIdentifier loadDispatchLotID;             //Load dispatch lot ID
        private ObjectIdentifier loadDispatchCassetteID;        //Load dispatch cassette ID
        private ObjectIdentifier unloadDispatchLotID;           //UnLoad dispatch lot ID
        private ObjectIdentifier unloadDispatchCassetteID;      //UnLoad dispatch cassette ID
        private ObjectIdentifier currentOperationModeID;        //<i>Current Operation Mode ID
        private String operationModeDescription;      //<i>Operation Mode description
        private String onlineMode;                    //<i>Online mode
        //<c>SP_Eqp_OnlineMode_Offline          "Off-Line"
        //<c>SP_Eqp_OnlineMode_OnlineLocal      "On-Line Local"
        //<c>SP_Eqp_OnlineMode_OnlineRemote     "On-Line Remote"
        private String dispatchMode;                  //<i>dispatch Mode
        //<c>SP_Eqp_DispatchMode_Manual     "Manual"
        //<c>SP_Eqp_DispatchMode_Auto       "Auto"
        private String currentAccessMode;                 //<i>Access Mode
        //<c>SP_Eqp_AccessMode_Manual       "Manual"
        //<c>SP_Eqp_AccessMode_Auto         "Auto"
        private String operationStartMode;            //<i>Operation Start Mode
        //<c>SP_Eqp_StartMode_Manual        "Manual"
        //<c>SP_Eqp_StartMode_Auto          "Auto"
        private String operationCompMode;             //<i>Operation Comp Mode
        //<c>SP_Eqp_CompMode_Manual         "Manual"
        //<c>SP_Eqp_CompMode_Auto           "Auto"
        private ObjectIdentifier loadedCassetteID;              //<i>Loaded Carrier ID
        private ObjectIdentifier lastM3CompCassetteID;          //<i>Last M3 Comp cassette ID
        private List<String> accessModes;                   //<i>Capable access modes
        private List<String> capableOperationModes;         //<i>Capable operation modes
        private List<String> cassetteCategoryCapability;    //<i>Sequence of Carrier Type Capability List
        private List<String> specialPortControls;           //<i>Special port controls
    }

    @Data
    public static class EqpPortEventOnTCS {
        private ObjectIdentifier portID;         //<i>port ID
        private String portStatus;     //<i>port Status. The following values are available.
        //<c>   SP_PortRsc_PortState_LoadAvail       "LoadAvail"
        //<c>   SP_PortRsc_PortState_LoadReq         "LoadReq"
        //<c>   SP_PortRsc_PortState_LoadComp        "LoadComp"
        //<c>   SP_PortRsc_PortState_UnloadAvail     "UnloadAvail"
        //<c>   SP_PortRsc_PortState_UnloadReq       "UnloadReq"
        //<c>   SP_PortRsc_PortState_UnloadComp      "UnloadComp"
        //<c>   SP_PortRsc_PortState_UnknownForTCS   "Unknown"
        //<c>   SP_PortRsc_PortState_Down            "Down"
        private ObjectIdentifier lotID;          //<i>lot ID
        private ObjectIdentifier cassetteID;     //<i>Carrier ID
    }

    @Data
    public static class DeleteUnloadableLot {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier unloadPortID;
    }

    @Data
    public static class MonitorGroups {
        private ObjectIdentifier monitorGroupID;              //<i>Monitor Group ID
        private ObjectIdentifier monitorLotID;                //<i>Monitor Lot ID
        private ObjectIdentifier monitorCassetteID;           //<i>Monitor Carrier ID
        private List<MonitoredLots> strMonitoredLots;    //<i>Sequence of Monitored Lots
    }

    @Data
    public static class MonitoredLots {
        private ObjectIdentifier lotID;         //<i>Monitored Lot ID    // /* Monitored Lot ID */
        private ObjectIdentifier cassetteID;    //<i>Carrier ID
    }

    @Data
    public static class InventoryLotInfo {
        private ObjectIdentifier cassetteID;        //<i>Carrier ID
        private ObjectIdentifier lotID;             //<i>lot ID
        private String carrierStatus;        //<i>Carrier Status
        private String transferJobStatus;    //<i>Transfer Job Status
    }

    @Data
    public static class CarrierInfo {
        private ObjectIdentifier carrierID;               //<i>Carrier ID
        private List<ObjectIdentifier> lotID;                   //<i>Sequence of lot ID    //0825
        private String zoneID;                  //<i>Zone ID
        private String shelfType;               //<i>Shelf Type
        private String stockInTime;             //<i>Stock In Time
        private Boolean alternateStockerFlag;    //<i>Alternate stocker Flag
        private ObjectIdentifier cassetteID;              //<i>Carrier ID
    }

    @Data
    public static class BrCarrierInfo {
        private String identifier;
        private String carrierId;
        private String description;
        private String instanceName;
        private ObjectIdentifier carrierCategory;
        private String carrierType;
        private Boolean usageCheckRequiredFlag;
        private Integer maximumRunTime;
        private Integer maximumStartCount;
        private Integer capacity;
        private Integer nominalSize;
        private String contents;
        private Integer intervalBetweenPM;
        private List<Infos.UserDataSet> userDataSets;
        private String usageType;   // Contamination Function
    }

    @Data
    public static class ReleaseCancelLotReturn {
        private ObjectIdentifier lotID;         //<i>lot ID
        private String returnCode;    //<i>Return CimCode
    }

    @Deprecated
    @Data
    public static class ProductRequestEventRecord extends EventRecord {
        private String lotID;
        private String lotType;
        private String subLotType;
        private String routeID;
        private Long productQuantity;
        private String planStartTime;
        private String planCompTime;
        private String lotGenerationType;
        private String lotScheduleMode;
        private String lotIDGenerationMode;
        private String productDefinitionMode;
        private long externalPriority;
        private Long priorityClass;
        private String productID;
        private String lotOwnerID;
        private String orderNumber;
        private String customerID;
        private String lotComment;
    }

    @Data
    public static class ProductIDListAttributes {
        private ObjectIdentifier productID;                          //<i>Product ID
        private String productIDDescription;               //<i>Product ID Description
        private String productType;                        //<i>Product Type
        private ObjectIdentifier productOwnerID;                     //<i>Product Owner ID
        private ObjectIdentifier manufacturingLayerID;               //<i>Manufacturing Layer ID
        private String manufacturingLayerIDDescription;    //<i>Manufacturing Layer ID Description
        private ObjectIdentifier productGroupID;                     //<i>Product Group ID
        private String productGroupIDDescription;          //<i>Product Group ID Description
        private ObjectIdentifier technologyID;                       //<i>Technology ID
        private String technologyIDDescription;            //<i>Technology ID Description
        private ObjectIdentifier lotType;                            //<i>lot Type
        private List<String> candidateSubLotTypes;               //<i>Sequence of Candidate Sub lot Types
        private ObjectIdentifier routeID;                            //<i>Route ID
        private String routeIDDescription;                 //<i>Route ID Description
        private List<ObjectIdentifier> sourceProductID;                    //<i>Sequence of Source Product ID
        private ObjectIdentifier startBankID;                        //<i>Start bank ID
        private String state;                              //<i>State    //D4200038
        private String lotGenType;                         //<i>lot Generation Type
        private Integer releaseSize;                        //<i>product release size
        private Integer planYeild;                          //<i>product plan yield
        private Integer usageLimit;
        private Integer recycleLimit;
    }

    @Data
    public static class AliasWaferName {
        private ObjectIdentifier waferID;
        private String aliasWaferName;

        public AliasWaferName() {
        }

        public AliasWaferName(ObjectIdentifier waferID, String aliasWaferName) {
            this.waferID = waferID;
            this.aliasWaferName = aliasWaferName;
        }
    }

    @Data
    public static class LotCommentInfo {
        private ObjectIdentifier reportUserID;          //<i>Report User ID
        private String reportTimeStamp;          //<i>Report Time Stamp
        private String lotCommentTitle;          //<i>lot Comment Title
        private String lotCommentDescription;    //<i>lot Comment Description
    }

    @Data
    public static class ControlJobInfo {
        private List<ControlJobCassette> controlJobCassetteList;  //<i>Sequence of Control Job Carrier
    }

    @Data
    @NoArgsConstructor
    @Deprecated
    public static class TranJobCreateReq {
        private String jobID;
        private boolean rerouteFlag;
        private String transportType;
        private List<JobCreateArray> jobCreateData;
        private Object siInfo;
    }

    @Data
    public static class JobCreateArray {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String zoneType;
        private Boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private String toStockerGroup;
        private List<ToDestination> toMachine;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
    }

    @Data
    public static class ToDestination {
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
    }

    @Data
    public static class LotNoteInfo {
        private ObjectIdentifier reportUserID;       //<i>Report User ID
        private String reportTimeStamp;       //<i>Report Time Stamp
        private String lotNoteTitle;          //<i>lot Note Title
        private String lotNoteDescription;    //<i>lot Note Description
    }

    @Data
    public static class LotOperationNoteList {
        private ObjectIdentifier reportUserID;     //<i>Report User ID
        private String reportTimeStamp;  //<i>Report Time Stamp
        private String noteTitle;        //<i>Note Title
        private ObjectIdentifier routeID;          //<i>Route ID
        private ObjectIdentifier operationID;      //<i>Operation ID
        private String operationNumber;  //<i>Operation Number
    }

    @Data
    public static class SubMfgLayerAttributes {
        private ObjectIdentifier mfgLayerID;                         //<i>Manufacturing Layer ID
        private String mfgLayerDescriptionIDDescription;    //<i>Manufacturing Layer Description ID Description
    }

    @Data
    public static class ProcessDefinitionIndexList {
        private ObjectIdentifier processDefinitionID;
        private String description;
        private ObjectIdentifier manufacturingLayerID;
        private String owner;
        private ObjectIdentifier startBankID;
        private ObjectIdentifier endBankID;
    }

    @Data
    public static class VirtualOperationLot {
        private ObjectIdentifier lotID;
        private String lotStatus;
        private ObjectIdentifier cassetteID;
        private String lotType;
        private String multiLotType;
        private String transferStatus;
        private ObjectIdentifier transferReserveUserID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private List<LotEquipmentList> lotEquipmentListList;       // Sequence Of Lot Equipment List
        private ObjectIdentifier flowBatchID;
        private ObjectIdentifier controlJob;
        private ObjectIdentifier processReserveEquipmentID;
        private ObjectIdentifier processReserveUserID;
        private ObjectIdentifier productID;
        private Boolean recipeAvailableFlag;
        private ObjectIdentifier logicalRecipeID;
        private ObjectIdentifier machineRecipeID;
        private ObjectIdentifier processMonitorProductID;
        private List<ObjectIdentifier> reticleGroupIDList;
        private Boolean reticleExistFlag;
        private String lastClaimedTimeStamp;
        private String stateChangeTimeStamp;
        private String inventoryChangeTimeStamp;
        private String queuedTimeStamp;
        private String dueTimeStamp;
        private String planStartTimeStamp;
        private String planEndTimeStamp;
        private ObjectIdentifier plannedEquipmentID;
        private String priorityClass;
        private String internalPriority;
        private String externalPriority;
        private Boolean qtimeFlag;
        private List<LotQtimeInfo> lotQtimeInfoList;
        private boolean minQTimeFlag;
        private List<LotQtimeInfo> minQTimeInfos;
        private String preOperationCompTImeStamp;    // Pre Operation Comp Time Stamp
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private ObjectIdentifier testTypeID;
        private String inspectionType;
        private ObjectIdentifier stageID;
        private Boolean mandatoryOperationFlag;
        private Boolean processHoldFlag;
        private Long totalWaferCount;
        private Long totalGoodDieCount;
        private ObjectIdentifier next2EquipmentID;
        private ObjectIdentifier next2LogicalRecipeID;
        private LotNoteFlagInfo lotNoteFlagInfo;
        private List<EntityInhibitAttributes> entityInhibitAttributesList;
        private String physicalRecipeID;
        private Boolean operableFlagForCurrentMachineStateFlag;
        private Boolean operableFlagForMultiRecipeCapabilityFlag;
        private String requiredCassetteCategory;
        private String cassetteCategory;
        private String next2requiredCassetteCategory;
        private Boolean sorterJobExistFlag;
        private Boolean inPostProcessFlagOfLotFlag;
        private Boolean inPostProcessFlagOfCassetteFlag;
        private String bondingFlowSectionName;
        private String bondingCateGory;
        private ObjectIdentifier topProductID;
        private String bondingGroupID;
        private Boolean autoDispatchDisableFlag;   // Auto Dispatch Disable Flag
        private Boolean monitorOperationFlag;
        private ObjectIdentifier eqpMonitorJobID;
        private Long startSeqNo;   // Start Order Control at Monitor Operation
    }

    @Data
    public static class VirtualOperationInprocessingLot {
        private ObjectIdentifier controlJobID;                                             //<i>Control Job ID
        private ObjectIdentifier lotID;                                                    //<i>Lot ID
        private ObjectIdentifier cassetteID;                                               //<i>Carrier ID
        private ObjectIdentifier equipmentID;                                              //<i>Equipment ID
        private ObjectIdentifier routeID;                                                  //<i>Route ID
        private ObjectIdentifier operationID;                                              //<i>Operation ID
        private String operationNumber;                                                     //<i>OperationNumber
    }

    @Data
    public static class DCItemSpecification {
        private String dataItemName;
        private Boolean screenLimitUpperRequired;
        private Double screenLimitUpper;
        private String actionCodesUscrn;
        private Boolean screenLimitLowerRequired;
        private Double screenLimitLower;
        private String actionCodesLscrn;
        private Boolean specLimitUpperRequired;
        private Double specLimitUpper;
        private String actionCodesUsl;
        private Boolean specLimitLowerRequired;
        private Double specLimitLower;
        private String actionCodesLsl;
        private Boolean controlLimitUpperRequired;
        private Double controlLimitUpper;
        private String actionCodesUcl;
        private Boolean controlLimitLowerRequired;
        private Double controlLimitLower;
        private String actionCodesLcl;
        private Double target;
        private String tag;
        private String dcSpecGroup;
    }

    @Data
    public static class DCItemData {
        private String dataItemName;
        private String waferPosition;
        private String sitePosition;
        private String valType;
        private String itemType;
        private String calculationType;
        private String calculationExpression;
        private String inputValue;
        private Double numValue;
        private String specCheckResult;
        private List<String> actionCodes;
        private String waferID;
        private String measurementType;
    }

    @Data
    public static class RouteIndexInformation {
        private ObjectIdentifier routeID;                 //<i>Route ID
        private String routeDescription;                  //<i>Route Description
        private ObjectIdentifier manufacturingLayerID;    //<i>Manufacturing Layer ID
        private String routeOwner;                        //<i>Route Owner
        private ObjectIdentifier startBankID;             //<i>Start bank ID
        private ObjectIdentifier endBankID;               //<i>End bank ID
    }

    @Data
    public static class StageInformation {
        private ObjectIdentifier stageID;         //<i>Stage ID
        private String stageName;                 //<i>Stage Name
        private String stageNo;                   //<i>Stage No
        private String description;               //<i>Description
        private ObjectIdentifier stageGroupID;    //<i>Stage Group ID
    }

    @Data
    public static class ProcessRecipeParameter {
        private String processJobID;
        private Boolean processStartFlag;
        private List<ObjectIdentifier> waferIDs;
        private List<StartRecipeParameter> startRecipeParameterList;
    }

    @Data
    public static class ProcessJobMapInfoRptInParm {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<ProcessJobMapInfo> processJobMapInfoList;
    }

    @Data
    public static class CalculationExpressionInfo {
        private Short whichDef;
        private String itemName;
        private String sitePosition;
        private String waferPosition;
        private String wholeExpression;
    }

    /**
     * description:
     * <p>pptLotExtPrty_struct</p>
     *
     * @author PlayBoy
     * @date 2018/10/25 13:24:02
     */
    @Data
    public static class LotExternalPriority {
        private ObjectIdentifier lotID;
        private int externalPriority;
    }

    /**
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/19                           Wind
     *
     * @author Wind
     * @date 2018/10/19 15:19
     */
    @Data
    public static class EqpNote {
        private String noteTitle;
        private String equipmentNote;
        private ObjectIdentifier claimUserID;
        private String claimUserName;
        private String claimTimeStamp;
    }

    @Data
    public static class CommonEqpBrInfo {
        private String equipmentName;                  //<i>eqp Name
        private ObjectIdentifier workArea;                       //<i>Work area ID
        private String equipmentOwner;                 //<i>eqp Owner
        private String tcsResourceName;                //<i>TCS Resource Name
        private String equipmentCategory;              //<i>eqp Category
        //<c>SP_Mc_Category_Dummy               "Dummy"
        //<c>SP_Mc_Category_WaferSorter         "wafer Sorter"
        //<c>SP_Mc_Category_AssemblyVendor      "Assembly Vendor"
        //<c>SP_Mc_Category_Inspection          "Inspection"
        //<c>SP_Mc_Category_Test                "Test"
        //<c>SP_Mc_Category_Measurement         "Measurement"
        //<c>SP_Mc_Category_Process             "Process"
        //<c>SP_Mc_Category_CircuitProbe        "Circuit Probe"
        //<c>SP_Mc_Category_InternalBuffer      "Internal Buffer"
        private boolean reticleUseFlag;                 //<i>Reticle Use Flag
        private boolean fixtureUseFlag;                 //<i>Fixture Use Flag
        private boolean cassetteChangeFlag;             //<i>Carrier Change Flag
        private boolean startLotsNotifyRequiredFlag;    //<i>Start Lots Notify Required Flag
        private boolean monitorCreationFlag;            //<i>Monitor Creation Flag
        private boolean eqpToEqpTransferFlag;           //<i>eqp to Eqipment Transfer Flag
        private boolean takeInOutTransferFlag;          //<i>Take in out Transfer Flag
        private boolean emptyCassetteRequireFlag;       //<i>Empty Carrier Required Flag
        private String slmSwitch;              //<i>slmSwitch            //DSIV00000099
        private Boolean slmCapabilityFlag;                      //<i>slmCapabilityFlag;                            //DSIV00000099
        private ObjectIdentifier monitorBank;                    //<i>Monitor bank ID
        private ObjectIdentifier dummyBank;                      //<i>Dummy bank ID
        private List<ControlBank> controlBanks;   //<i>Sequence of Control Banks
        private List<String> specialControl;            //<i>Sequence of Special Control
        private String multiRecipeCapability;          //<i>Multi recipe Capability
        private long maxBatchSize;                   //<i>Maximum Process Batch Size             //DSIV00001830
        private long minBatchSize;                   //<i>Minimum Process Batch Size             //DSIV00001830
        private long minWaferCount;                  //<i>Minimum wafer Count                    //DSIV00001830
        private boolean processJobLevelCtrl;            //<i>Process Job Level Control Flag         //DSN000015229
    }

    @Data
    public static class ControlBank {
        private String controlLotType;           //<i>Control lot Type
        private ObjectIdentifier controlBankID;    //<i>Control bank ID
    }

    @Data
    public static class EqpInternalBufferInfo {
        private String bufferCategory;      //<i>Buffer Category
        private String bufferCapacity;      //<i>Buffer Capacity
        private List<ShelfInBuffer> shelfInBufferList;    //<i>Sequence of Shelf In Buffer
    }

    @Data
    public static class ShelfInBuffer {
        private Integer shelfOrderNumber;        //<i>Shelf Order Number
        private ObjectIdentifier controlJobID;            //<i>Control Job ID
        private ObjectIdentifier durableControlJobID;            //<i>Control Job ID
        private ObjectIdentifier loadedCarrierID;         //<i>Loaded Carrier ID
        private List<LotOnPort> lotInShelfList;           //<i>Sequence of lot In Shelf //0.0.1
        private ObjectIdentifier reservedCarrierID;       //<i>Reserved Carrier ID
        private ObjectIdentifier reservedLoadPortID;      //<i>Reserved Load port ID
        private ObjectIdentifier reservedUnloadPortID;    //<i>Reserved Unload port ID
    }

    @Data
    public static class InventoriedLotInfo {
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier lotID;
        private String returnCode;
    }

    @Data
    public static class EqpBrInfoForInternalBuffer {
        private String equipmentName;                  //<i>eqp Name
        private ObjectIdentifier workBay;                       //<i>Work area
        private String equipmentOwner;                 //<i>eqp Owner
        private String eqpResourceName;                //<i>TCS Resource Name
        private String equipmentCategory;              //<i>eqp Category
        private boolean reticleUseFlag;                 //<i>Reticle Use Flag
        private boolean fixtureUseFlag;                 //<i>Fixture Use Flag
        private boolean cassetteChangeFlag;             //<i>Carrier Change Flag
        private boolean startLotsNotifyRequiredFlag;    //<i>Start Lots Notify Required Flag
        private boolean monitorCreationFlag;            //<i>Monitor Creation Flag
        private boolean eqpToEqpTransferFlag;           //<i>eqp To eqp Transfer Flag
        private boolean takeInOutTransferFlag;          //<i>Take In Out Transfer Flag
        private boolean emptyCassetteRequireFlag;       //<i>Empty Carrier Require Flag
        private List<ControlBank> controlBanks;                   //<i>Sequence of Control Banks
        private List<String> specialControl;                 //<i>Sequence of Special Control
        private String multiRecipeCapability;          //<i>Multi Recipe Capability    //D5000024
        private long maxBatchSize;                   //<i>Maximum Process Batch Size             //DSN000015229
        private long minBatchSize;                   //<i>Minimum Process Batch Size             //DSN000015229
        private long minWaferCount;                  //<i>Minimum wafer Count                    //DSN000015229
        private boolean processJobLevelCtrl;            //<i>Process Job Level Control Flag         //DSN000015229
        private String prControl;
    }

    @Data
    public static class WhatNextStandbyAttributes {
        private ObjectIdentifier lotID;                       //<i>lot ID
        private ObjectIdentifier cassetteID;                  //<i>Carrier ID
        private String lotType;                     //<i>lot Type
        private String multiLotType;                //<i>Multi lot Type
        private String lotStatus;                   //<i>lot Status
        private String transferStatus;              //<i>Transfer Status
        private ObjectIdentifier equipmentID;                 //<i>eqp ID
        private ObjectIdentifier stockerID;                   //<i>stocker ID
        private ObjectIdentifier bankID;                      //<i>bank ID
        private ObjectIdentifier productID;                   //<i>Product ID
        private String lastClaimedTimeStamp;        //<i>Last Claimed Time Stamp
        private Long totalWaferCount;             //<i>Total wafer Count
        private String controlUseState;             //<i>Control Use State
        private Long usedCount;                   //<i>Used Count
        private String requiredCassetteCategory;    //<i>Required Carrier Type
        private String cassetteCategory;            //<i>Carrier Type
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/26                              Wind
     *
     * @author Wind
     * @date 2018/10/26 14:53
     */
    @Data
    public static class DeltaDcDefInfo {
        private String processOperation;
        private ObjectIdentifier deltaDCDefinition;
    }

    @Data
    public static class MoveReticles {
        private ObjectIdentifier reticleID;     //<i>Reticle ID
        private Integer slotNumber;    //<i>Slot Number

        public MoveReticles() {

        }

        public MoveReticles(ObjectIdentifier reticleID, Integer slotNumber) {
            this.reticleID = reticleID;
            this.slotNumber = slotNumber;
        }
    }

    @Data
    public static class PartialReworkReq {
        private ObjectIdentifier parentLotID;              //<i>Parent lot ID
        private List<ObjectIdentifier> childWaferID;             //<i>Sequence of Child wafer ID
        private ObjectIdentifier subRouteID;               //<i>Sub Route ID
        private String returnOperationNumber;    //<i>Return Operation Number
        private ObjectIdentifier reasonCodeID;             //<i>Reason CimCode ID
        private String eventTxId;                //<i>Event Tx ID
        private Boolean bForceRework;             //<i>Force Rework Flag
        private Boolean bDynamicRoute;            //<i>Dynamic Route Flag
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30                           Wind
     *
     * @author Wind
     * @date 2018/10/30 9:20
     */
    @Data
    public static class QrestTimeInfo {
        private ObjectIdentifier qrestrictionTriggerRouteID;
        private String qrestrictionTriggerOperationNumber;
        private String qrestrictionTriggerTimeStamp;
        private ObjectIdentifier qrestrictionTargetRouteID;
        private String qrestrictionTargetOperationNumber;
        private String qrestrictionTargetTimeStamp;
        private ObjectIdentifier waferID;
        private String previousTargetInfo;
        private String qrestrictionRemainTime;
        private Long expiredTimeDuration;
        private String specificControl;
        private String originalQTime;
        private String qTimeType;
        private String processDefinitionLevel;
        private String qrestrictionTriggerBranchInfo;
        private String qrestrictionTriggerReturnInfo;
        private String qrestrictionTargetBranchInfo;
        private String qrestrictionTargetReturnInfo;
        private String watchDogRequired;
        private String actionDoneFlag;
        private List<QTimeActionInfo> strQtimeActionInfoList;
        private boolean manualCreated;
        private boolean preTrigger;
    }

    @Data
    public static class ToMachine {
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String reserve;
    }

    @Data
    public static class ReserveLot {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier forEquipmentID;
        private String reserve;
    }

    @Data
    public static class EquipmentStatus {
        private ObjectIdentifier equipmentID;
        private String reserve;
    }

    @Data
    public static class XferReticlePod {
        private ObjectIdentifier reticlePodID;
        private String transferStatus;
        private String transferStatusChangeTimeStamp;
    }

    /**
     * description: posQTimeRestrictionInfo_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/1/2018 1:29 PM
     */
    @Data
    public static class QTimeRestrictionInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier waferID;
        private String originalQTime;
        private String qTimeType;
        private String processDefinitionLevel;
        private ObjectIdentifier triggerMainProcessDefinition;
        private String triggerOperationNumber;
        private String triggerBranchInfo;
        private String triggerReturnInfo;
        private String triggerTimeStamp;
        private ObjectIdentifier targetMainProcessDefinition;
        private String targetOperationNumber;
        private String targetBranchInfo;
        private String targetReturnInfo;
        private String targetTimeStamp;
        private String previousTargetInfo;
        private String control;
        private Boolean watchdogRequired;
        private Boolean actionDone;
        private Boolean manualCreated;
        private Boolean preTrigger;
        List<QTimeRestrictionAction> actions;
    }

    @Data
    public static class QTimeRestrictionBackupAction {
        private String previousTargetInfo;
        private String targetTimeStamp;
        private String action;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier actionRouteID;
        private String operationNumber;
        private String timing;
        private ObjectIdentifier mainProcessDefinition;
        private ObjectIdentifier messageDefinition;
        private String customField;
        private Boolean watchdogRequired;
        private Boolean actionDone;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/9/19 17:11
     */
    @Data
    public static class QTimeTargetOpeReplaceIn {
        private ObjectIdentifier lotID;
        private Boolean specificControlFlag;
        private Object siInfo;
    }

    /**
     * description:posQTimeRestrictionAction_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/1/2018 1:30 PM
     */
    @Data
    public static class QTimeRestrictionAction {
        private String targetTimeStamp;
        private String action;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier actionRouteID;
        private String operationNumber;
        private String timing;
        private ObjectIdentifier mainProcessDefinition;
        private ObjectIdentifier messageDefinition;
        private String customField;
        private Boolean watchdogRequired;
        private Boolean actionDone;
    }

    /**
     * description: posReplaceTimeRestrictionSpecification_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/1/2018 1:31 PM
     */
    @Data
    public static class ReplaceTimeRestrictionSpecification {
        private String originalQTime;
        private String processDefinitionLevel;
        private ObjectIdentifier productSpecification;
        private String qTimeType;
        private Double expiredTimeDuration;
        private ObjectIdentifier triggerRouteID;
        private String triggerOperationNumber;
        private String triggerBranchInfo;
        private String triggerReturnInfo;
        private ObjectIdentifier targetRouteID;
        private String targetOperationNumber;
        private String targetBranchInfo;
        private String targetReturnInfo;
        private String control;
        private List<TimeRestrictionAction> actions;
    }

    /**
     * description:posTimeRestrictionAction_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/1/2018 3:22 PM
     */
    @Data
    public static class TimeRestrictionAction {
        private Double expiredTimeDuration;
        private String action;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier actionRouteID;
        private String operationNumber;
        private String timing;
        private ObjectIdentifier mainProcessDefinition;
        private ObjectIdentifier messageDefinition;
        private String customField;
        private String qTimeType;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30                          Wind
     *
     * @author Wind
     * @date 2018/10/30 9:24
     */
    @Data
    public static class QTimeActionInfo {
        private String qrestrictionTargetTimeStamp;
        private Long expiredTimeDuration;
        private String qrestrictionAction;
        private ObjectIdentifier reasonCodeID;
        private ObjectIdentifier actionRouteID;
        private String actionOperationNumber;
        private String futureHoldTiming;
        private ObjectIdentifier reworkRouteID;
        private ObjectIdentifier messageID;
        private String customField;
        private String watchDogRequired;
        private String actionDoneFlag;
    }

    /**
     * description:
     * <p>pptMessageAttributes_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/5 16:58:03
     */
    @Data
    public static class MessageAttributes {
        private ObjectIdentifier messageID;
        private ObjectIdentifier lotID;
        private String lotStatus;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private String reasonCode;
        private String messageText;
    }

    /**
     * description:
     * <p>pptInterFabMonitorGroupActionInfo_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/5 17:00:27
     */
    @Data
    public static class InterFabMonitorGroupActionInfo {
        private String fabID;
        private ObjectIdentifier monitoringLotID;
        private MonitorGroupReleaseInfo monitorGroupReleaseInfo;
        private List<MonitoredLotHoldInfo> monitoredLotHoldInfoList;
        private List<MonitoredLotMailInfo> monitoredLotMailInfoList;
    }

    /**
     * description:
     * <p>pptEntityInhibitAttributesWithFabInfo__101_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/5 17:00:28
     */
    @Data
    public static class EntityInhibitAttributesWithFabInfo {
        private String fabID;
        private EntityInhibitAttributes entityInhibitAttributes;
        private List<EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/9/26 10:43
     */
    @Data
    public static class EntityInhibitXmlCreateOut {
        private String xml;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/9/26 10:46
     */
    @Data
    public static class EntityInhibitXmlCreateIn {
        private EntityInhibitAttributesWithFabInfo strEntityInhibitionWithFabInfo;
        private String claimMemo;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/9/26 11:27
     */
    @Data
    public static class SystemMessageRequestForMultiFabIn {
        private List<MessageAttributesWithFabInfo> strMessageListWithFabInfo;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/9/26 13:21
     */
    @Data
    public static class EDCWithSpecCheckActionReqInParm {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<StartCassette> strStartCassette;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/10/8 14:32
     */
    @Data
    public static class MonitorGroupDeleteCompOut {
        private List<MonitoredCompLots> strMonitoredCompLots;
        private List<InterFabMonitorGroupActionInfo> strInterFabMonitorGroupActionInfoSequence;
        private Object siInfo;
    }

    @Data
    public static class PostConcurrentTaskExecuteReqResult {
        private List<PostProcessActionResultSet> strPostProcessActionResultSets;
        private List<OpeCompLot> strLotSpecSPCCheckResultSeq;
        private Object siInfo;
    }

    @Data
    public static class PostProcessActionResultSet {
        private String dKey;
        private List<PostProcessActionInfo> strPostProcessActionInfoSeq;
        private List<PostProcessActionResult> strPostProcessActionResultSeq;
        private Object siInfo;
    }

    @Data
    public static class PostProcessActionResult {
        private String dKey;
        private Long seqNo;
        private String postProcID;
        private String executionResult;
        private Object siInfo;
    }


    @Data
    public static class MonitorGroupDeleteCompIn {
        private ObjectIdentifier lotID;
        private List<InterFabMonitorGroupActionInfo> strInterFabMonitorGroupActionInfoSequence;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/9/26 11:27
     */
    @Data
    public static class MessageAttributesWithFabInfo {
        private String fabID;
        private MessageAttributes strMessageAttributes;
        private Object siInfo;
    }

    /**
     * description:
     * <p>pptDCActionResultInfo_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/5 17:21:05
     */
    @Data
    public static class DCActionResultInfo {
        private ObjectIdentifier lotID;
        private Boolean monitorLotFlag;
        private ObjectIdentifier dcDefID;
        private ObjectIdentifier dcSpecID;
        private String checkType;
        private String reasonCode;
        private String actionCode;
        private String correspondingObjRefPO;
        private String bankID;
        private String reworkRouteID;
        private List<EntityIdentifier> entities;
    }

    /**
     * description:
     * <p>pptMonitorGroupReleaseInfo_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/5 17:32:20
     */
    @Data
    public static class MonitorGroupReleaseInfo {
        private Boolean groupReleaseFlag;
        private ObjectIdentifier monitorGroupID;
        private String lotType;
    }

    @Data
    public static class MonitoredLotHoldInfo {
        private ObjectIdentifier lotID;
        private List<HoldInfo> holdInfoList;
    }

    @Data
    public static class MonitoredLotMailInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier routeID;
        private String opeNo;
        private List<ObjectIdentifier> messageIDSeq;
        private List<String> reasonCodeSeq;
    }

    @Data
    public static class HoldInfo {
        private String reasonCode;
        private String holdType;
    }

    @Data
    public static class LotHoldEffectList {
        private ObjectIdentifier lotID;
        private String holdType;
        private ObjectIdentifier reasonCodeID;
        private ObjectIdentifier userID;
        private String responsibleOperationMark;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier relatedLotID;
        private String claimMemo;
    }

    /**
     * description:
     * <p>pptSpcIFParm_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/6 18:22:29
     */
    @Data
    public static class SpcIFParm {
        private Inputs.SpcInput spcInput;
        private Outputs.SpcOutput spcOutput;
    }

    @Data
    public static class QtimeListInqInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier waferID;
        private String qTimeType;
        private Boolean activeQTime;
        private String type;    // QTime类型: max/min(默认max)
    }

    @Data
    public static class QtimePageListInqInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier waferID;
        private String qTimeType;
        private Boolean activeQTime;
        private SearchCondition searchCondition;
        private String type;    // QTime类型: max/min(默认max)
    }

    @Data
    public static class SpcDcItem {
        private String dataItemName;
        private String waferID;
        private String waferPosition;
        private String sitePosition;
        private Object dataValue;
        private Double targetValue;
        private String specCheckResult;
        private String comment;

        //add some new param
        private Double specLimitUpper;
        private Double specLimitLower;
        private Double controlLimitUpper;
        private Double controlLimitLower;
    }

    @Data
    public static class WaferIDByChamber {
        private String procChamber;
        private List<String> waferIDs;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/3/19 10:57
     */
    @Data
    public static class InventoryReticlePodInfo {
        private ObjectIdentifier reticlePodID;
        private String transferJobStatus;
        private Object siInfo;
    }

    @Data
    public static class ChamberHoldAction {
        private String chamberID;
        private String chamberHoldAction;
        private String chamberRecipeHoldAction;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/3/19 13:52
     */
    @Data
    public static class ReticlePodCurrentMachineGetOut {
        private ObjectIdentifier currentMachineID;
        private ObjectIdentifier currentReticlePodPortID;
        private Object siInfo;
    }


    @Data
    public static class BankMove {
        /**
         * lot ID that you want to move.
         */
        private ObjectIdentifier lotID;
        /**
         * bank ID that you want to move a lot to.
         */
        private ObjectIdentifier bankID;
    }

    @Data
    public static class MailSend {
        private MessageAttributes messageAttributes;
        private String chartMailAddress;
    }

    @Data
    public static class ReworkBranch {
        private ObjectIdentifier lotID;
        private ObjectIdentifier reworkRouteID;
        private String returnOperationNumber;
    }

    @Data
    public static class MailSendWithFabInfo {
        private String fabID;
        /**
         * Message Attributes with Mail Address
         */
        private MailSend strMailSend;
    }

    @Data
    public static class BranchReq {
        private ObjectIdentifier lotID;                     //<i>lot ID
        private ObjectIdentifier currentRouteID;            //<i>Current Route ID
        private String currentOperationNumber;    //<i>Current Operation Number
        private ObjectIdentifier subRouteID;                //<i>Sub Route ID
        private String returnOperationNumber;     //<i>Return Operation Number
        private String eventTxId;                 //<i>Event Tx ID
        private Boolean bDynamicRoute;             //<i>Dynamic Route Flag
        private String claimMemo;
    }

    @Data
    public static class CandidatePortMode {
        private ObjectIdentifier portID;             //<i>port ID
        private String portGroup;          //<i>port Group
        private String portUsage;          //<i>port Usage
        private List<OperationMode> strOperationMode;   //<i>Sequence of pptOperationMode struct
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/6                          Wind
     *
     * @author Wind
     * @date 2018/11/6 11:02
     */
    @Data
    public static class ReticlePodListInfo {
        private ObjectIdentifier reticlePodID;
        private ReticlePodBrInfo reticlePodBRInfo;
        private ReticlePodStatusInfo reticlePodStatusInfo;
        private ReticlePodPmInfo reticlePodPMInfo;
        private DurableLocationInfo reticlePodLocationInfo;
        private ObjectIdentifier bankID;
        private String dueTimeStamp;
        private ObjectIdentifier startBankID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private boolean bankInRequiredFlag;
        private ObjectIdentifier holdReasonCodeID;
    }

    @Data
    public static class ReticlePodBrInfo {
        private String description;
        private String reticlePodCategory;
        private long capacity;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/20 10:23:37
     */
    @Data
    public static class ReticlePodInfo {
        private String identifier;
        private String reticlePodId;
        private String description;
        private String instanceName;
        private ObjectIdentifier reticlePodCategory;
        private Integer intervalBetweenPM;
        private Integer slotPositionCount;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class ReticlePodStatusInfo {
        private String reticlePodStatus;
        private String transferStatus;
        private ObjectIdentifier durableSubStatus;
        private String durableSubStatusName;
        private String durableSubStatusDescription;
        private boolean availableForDurableFlag;
        private String processLagTime;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier durableControlJobID;
        private boolean durableSTBFlag;
        private ObjectIdentifier transferReserveUserID;
        private boolean inPostProcessFlagOfReticlePod;
        private boolean emptyFlag;
        private List<ContainedReticleInfo> strContainedReticleInfo;
        private String lastClaimedTimeStamp;
        private ObjectIdentifier lastClaimedPerson;
        private List<HashedInfo> strDurableStatusList;
        private ShelfPosition shelfPosition; //for e-rack, added by nyx
    }

    @Data
    public static class ContainedReticleInfo {
        private long slotNo;
        private ObjectIdentifier reticleID;
        private ReticleBrInfo strReticleBrInfo;
    }

    @Data
    public static class ReticlePodPmInfo {

        private long passageTimeFromLastPM;
        private long intervalBetweenPM;
        private String lastMaintenanceTimeStamp;
        private ObjectIdentifier lastMaintenancePerson;
    }


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/7/2018 4:05 PM
     */
    @Data
    public static class QtimeInfo {
        private ObjectIdentifier restrictionTriggerRouteID;            //<i>Q Restriction Trigger Route ID
        private String restrictionTriggerOperationNumber;    //<i>Q Restriction Trigger Operation Number
        private String restrictionTriggerTimeStamp;          //<i>Q Restriction Trigger Time Stamp
        private String minRemainTime;
        private ObjectIdentifier restrictionTargetRouteID;             //<i>Q Restriction Target Route ID
        private String restrictionTargetOperationNumber;     //<i>Q Restriction Target Operation Number
        private String restrictionTargetTimeStamp;           //<i>Q Restriction Target Time Stamp
        private ObjectIdentifier waferID;                               //<i>Q Restriction wafer ID
        private String previousTargetInfo;                    //<i>Q Restriction Previous Target Infomation
        private String restrictionRemainTime;                //<i>Q Restriction Remain Time
        private Long expiredTimeDuration;                   //<i>Q Restriction Expired Time Duration
        private String specificControl;                       //<i>Specific Control
        private String originalQTime;                         //<i>Original qtime
        private String qTimeType;                             //<i>qtime Type (By lot/By wafer)
        private String processDefinitionLevel;                //<i>Process Definition Level (Main/Module)
        private String restrictionTriggerBranchInfo;         //<i>Q Restriction Trigger Branch Info
        private String restrictionTriggerReturnInfo;         //<i>Q Restriction Trigger Return Info
        private String restrictionTargetBranchInfo;          //<i>Q Restriction Target Branch Info
        private String restrictionTargetReturnInfo;          //<i>Q Restriction Target Return Info
        private String watchDogRequired;                      //<i>Watch Dog Required
        private String actionDoneFlag;                        //<i>Action Done Flag
        private List<QTimeActionInfo> strQtimeActionInfoList;                //<i>Sequence of Q Time Action Information
        private Boolean manualCreated;                         //<i>Manual Created
        private Boolean preTrigger;                            //<i>Pre-Trigger Flag
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/2 10:06
     */
    @Data
    public static class DurableHoldEventMakeIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private List<DurableHoldHistory> strHoldHistoryList;
        private String transactionID;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/2 10:08
     */
    @Data
    public static class DurableHoldHistory {
        private Boolean movementFlag;
        private Boolean changeStateFlag;
        private String holdType;
        private ObjectIdentifier holdReasonCode;
        private ObjectIdentifier holdPerson;
        private String holdTime;
        private ObjectIdentifier relatedDurable;
        private Boolean responsibleOperationFlag;
        private Boolean responsibleOperationExistFlag;
        private ObjectIdentifier releaseReasonCode;
        private ObjectIdentifier releasePerson;
        private String releaseTime;
        private String releaseClaimMemo;
        private ObjectIdentifier responsibleRouteID;
        private String responsibleOperationNumber;
        private String responsibleOperationName;
        private String holdClaimMemo;
        private Object siInfo;
    }

    @Deprecated
    @Data
    public static class QTimeEventRecord extends EventRecord {
        private String lotID;
        private String waferID;
        private String originalQTime;
        private String qTimeType;
        private String processDefinitionLevel;
        private String opeCategory;
        private String triggerMainProcessDefinitionID;
        private String triggerOperationNumber;
        private String triggerBranchInfo;
        private String triggerReturnInfo;
        private String triggerTimeStamp;
        private String targetMainProcessDefinitionID;
        private String targetOperationNumber;
        private String targetBranchInfo;
        private String targetReturnInfo;
        private String targetTimeStamp;
        private String previousTargetInfo;
        private String control;
        private Boolean watchdogRequired;
        private Boolean actionDone;
        private Boolean manualCreated;
        private Boolean preTrigger;
        private List<QTimeActionEventData> actions;
    }

    @Deprecated
    @Data
    public static class QTimeActionEventData {
        private String targetTimeStamp;
        private String action;
        private String reasonCode;
        private String actionRouteID;
        private String operationNumber;
        private String timing;
        private String mainProcessDefinitionID;
        private String messageDefinitionID;
        private String customField;
        private Boolean watchdogRequired;
        private Boolean actionDone;
    }

    @Data
    public static class SysMsgStockInfo {
        private String subSystemID;
        private String systemMessageCode;
        private String systemMessageText;
        private Boolean notifyFlag;
        private ObjectIdentifier equipmentID;
        private String equipmentStatus;
        private ObjectIdentifier stockerID;
        private String stockerStatus;
        private ObjectIdentifier AGVID;
        private String AGVStatus;
        private ObjectIdentifier lotID;
        private String lotStatus;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String systemMessageTimeStamp;
        private String reserve;
    }

    @Data
    public static class EqpTargetPortInfo {
        private ObjectIdentifier equipmentID;
        private List<PortGroup> portGroups;
    }

    @Data
    public static class EqpAuto3SettingInfo {
        private ObjectIdentifier eqpID;
        private String carrierTransferRequestEvent;
        private String watchdogName; // watchdogName
        private String reserve;

        public EqpAuto3SettingInfo() {
        }

        public EqpAuto3SettingInfo(CimEqpAuto3SettingDO eqpAuto3Setting) {
            this.eqpID = new ObjectIdentifier(eqpAuto3Setting.getEqpID());
            this.carrierTransferRequestEvent = eqpAuto3Setting.getCdrEvent();
            this.watchdogName = eqpAuto3Setting.getWatchdogName();
        }
    }

    @Data
    public static class CassetteDBINfoGetDRInfo {
        private ObjectIdentifier cassetteID;
        private Boolean durableOperationInfoFlag;
        private Boolean durableWipOperationInfoFlag;
    }

    @Data
    public static class LotCtrlStatus {
        private String controlUseStatus;
        private String description;

        public LotCtrlStatus() {
        }

        public LotCtrlStatus(String controlUseStatus, String description) {
            this.controlUseStatus = controlUseStatus;
            this.description = description;
        }
    }


    @Data
    public static class ReticlePodAdditionalAttribute {
        private boolean transferReservedFlag;
        private ObjectIdentifier transferDestEquipmentID;
        private List<ContainedReticleInfo> strReservedReticleInfo;
        private ObjectIdentifier transferReservedUserID;
    }

    @Data
    public static class MonitoredLot {
        private ObjectIdentifier lotID;
        private String processOperation;
    }

    /**
     * description:
     * <p>posActionResultInfo_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/8 12:58:13
     */
    @Data
    public static class ActionResultInfo {
        private ObjectIdentifier lotID;
        private Boolean monitorLotFlag;
        private ObjectIdentifier dataCollectionDefinitionID;
        private ObjectIdentifier dataCollectionSpecificationID;
        private String checkType;
        private String reasonCode;
        private String actionCode;
        private String correspondingObjRefPO;
        private String bankID;
        private String reworkRouteID;
        private List<ActionEntityInfo> entities;
    }

    /**
     * description:
     * <p>posActionEntityInfo_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/8 13:01:24
     */
    @Data
    public static class ActionEntityInfo {
        private String className;
        private ObjectIdentifier objectID;
        private String attribution;
    }

    /**
     * description:
     * <p>posCorrespondingOperationInfo_struct</p>
     *
     * @author PlayBoy
     * @date 2018/11/8 15:09:07
     */
    @Data
    public static class CorrespondingOperationInfo {
        private String correspondingOperationNumber;
        private String dcSpecGroup;

        public CorrespondingOperationInfo() {

        }

        public CorrespondingOperationInfo(String correspondingOperationNumber) {
            this.correspondingOperationNumber = correspondingOperationNumber;
        }
    }

    /**
     * description:getPFAndPDFieldByPFIDAndPDLevel
     *
     * @author Nyx
     * @date 2018/11/12 15:32
     */
    @Data
    public static class PFAndPDField {
        private String mainProcessDefinitionID;
        private Boolean state;
        private String processDefinitionType;
        private String mainProcessDefinitionObj;

    }

    /**
     * description: EventRecordType Enum
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/12/2018 4:26 PM
     */
    public enum EventRecordType {
        posProductRequestEventRecord(1),
        posLotChangeEventRecord(2),
        posLotWaferMoveEventRecord(3),
        posLotOperationMoveEventRecord(4),
        posLotHoldEventRecord(5),
        posLotReworkEventRecord(6),
        posLotWaferScrapEventRecord(7),
        posLotBankMoveEventRecord(8),
        posVendorLotEventRecord(9),
        posLotWaferSortEventRecord(10),
        posProcessHoldEventRecord(11),
        posLotFlowBatchEventRecord(12),
        posEquipmentStatusChangeEventRecord(13),
        posChamberStatusChangeEventRecord(14),
        posEquipmentAlarmEventRecord(15),
        posSystemMessageEventRecord(16),
        posPortStatusChangeEventRecord(17),
        posEntityInhibitEventRecord(18),
        posProcessStatusEventRecord(19),
        posRecipeBodyManageEventRecord(20),
        posCollectedDataEventRecord(21),
        posEquipmentModeChangeEventRecord(22),
        posLotOperationStartEventRecord(23),
        posLotOperationCompleteEventRecord(24),
        posLotReticleSetChangeEventRecord(25),
        posPlannedSplitEventRecord(26),
        posDurableChangeEventRecord(27),
        posLotFutureHoldEventRecord(28),
        posScriptParameterChangeEventRecord(29),
        posBackupChannelEventRecord(30),
        posBackupOperationEventRecord(31),
        posWaferChamberProcessEventRecord(32),
        posAPCInterfaceEventRecord(33),
        posAPCProcessDispositionEventRecord(34),
        posControlJobStatusChangeEventRecord(35),
        posFutureReworkEventRecord(36),
        posFPCEventRecord(37),
        posDurableEventRecord(38),
        posQTimeEventRecord(39);

        private int _value;

        EventRecordType(int value) {
            _value = value;
        }

        public int value() {
            return _value;
        }
    }

    @Data
    public static class BufferResourceInfo {
        private String bufferCategory;
        private long smCapacity;
        private long smInUseCapacity;
        private long dynamicCapacity;
        private long dynamicInUseCapacity;
    }

    @Data
    public static class ReserveCancelLotCarrier {
        private ObjectIdentifier lotID;             //<i>lot ID
        private ObjectIdentifier carrierID;         //<i>Carrier ID
        private ObjectIdentifier forEquipmentID;    //<i>For eqp ID
    }

    @Data
    public static class ReserveCancelLot {
        private ObjectIdentifier lotID;             //<i>lot ID
        private ObjectIdentifier cassetteID;        //<i>Carrier ID
        private ObjectIdentifier forEquipmentID;    //<i>For eqp ID

        public ReserveCancelLot(ObjectIdentifier lotID, ObjectIdentifier cassetteID, ObjectIdentifier forEquipmentID) {
            this.lotID = lotID;
            this.cassetteID = cassetteID;
            this.forEquipmentID = forEquipmentID;
        }
    }

    @Data
    public static class BufferResourceUpdateInfo {
        private String bufferCategory;
        private long smCapacity;
        private long dynamicCapacity;
        private long newCapacity;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/14/2018 3:28 PM
     */
    @Data
    public static class BranchInfo {
        private ObjectIdentifier routeID;
        private String operationNumber;
        private String processOperation;
        private String reworkOutKey;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/14/2018 4:15 PM
     */
    @Data
    public static class ReturnInfo {
        private ObjectIdentifier routeID;
        private String operationNumber;
        private String processFlow;
        private String mainProcessFlow;
        private String moduleProcessFlow;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/16/2018 9:50 AM
     */
    @Data
    public static class OperationNumberListAttributes {
        private Integer seqno;             //<i>Sequence Number. Not used.
        private ObjectIdentifier routeID;  //<i>Route ID
        private String operationNumber;    //<i>Operation Number
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/16/2018 2:54 PM
     */
    @Data
    public static class ConnectedRoute {
        private ObjectIdentifier routeID;      //<i>Route ID
        private String processDefinitionType;  //<i>Process Definition Type
        private String returnOperationNumber;  //<i>Return Operation Number
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/16/2018 3:23 PM
     */
    @Data
    public static class objObjectIDListGetDR {
        private String className;
        private ObjectIdentifier objectID;
        List<AdditionalSearchCondition> strAdditionalSearchConditionSeq;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/16/2018 3:23 PM
     */
    @Data
    public static class AdditionalSearchCondition {
        private String className;              //<i>Class Neme
        private String conditionName;          //<i>Condition Name
        private String conditionValue;         //<i>Condition Value
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/16/2018 3:35 PM
     */
    @Data
    public static class ObjectIDInformation {
        private ObjectIdentifier objectID;  //<i>Object ID
        private ObjectIdentifier durableStatus;
        private String description;              //<i>Description
    }

    @Data
    @NoArgsConstructor
    public static class LotBaseInfo {
        private ObjectIdentifier lotID;
        // private String lotName;
        private String representativeState;
        private List<LotStatusList> lotStatusList;
        private String createdTimeStamp;
        private String activatedTimeStamp;
        private String releasedTimeStamp;
        private String completionTimeStamp;
        private ObjectIdentifier lotFamilyID;
        private Boolean originalLotFlag;
        private ObjectIdentifier parentLotID;
        private Double currentYield;
        private String processOperationObjRef;
        private ObjectIdentifier productRequestID;
        private int externalPriority;
        private Double internalPriority;
        private ObjectIdentifier productSpecificationID;
        private String processFlowContextObjRef;
        private String lotType;
        private String subLotTpye;
        private ObjectIdentifier bankID;
        private String commeentObjRef;
        private String propertySetObjRef;
        private Long totalWaferCount;
        private Long productWaferCount;
        private Long controlWaferCount;
        private ObjectIdentifier flowBatchID;
        private String claimedTimeStamp;
        private ObjectIdentifier claimUSerID;
        private ObjectIdentifier stateChangeUserID;
        private String stateChangedTimeStamp;
        private String inventoryStateChangedTimeStamp;
        private String vendorLotIdentifier;
        private String vendorLotName;
        private String lotContents;
        private Boolean shipRequiredFlag;
        private Boolean dispatchReservedFlag;
        private ObjectIdentifier previousBankID;
        private List<LotStatusList> lotPreviousStatusList;
        private Boolean responsibleOperationFlag;
        private Long priorityClass;
        private String planStartTimeStamp;
        private String planEndTimeStamp;
        private Long originalWaferCount;
        private String waferHistoryTimeStamp;
        private ObjectIdentifier lotOwnerID;
        private ObjectIdentifier customerID;
        private String orderNumber;
        private String scheduleMode;
        private String productMode;
        private String MFGLayer;
        private Long notAllocateWaferCount;
        private ObjectIdentifier controlMonitorGroupID;
        private Long monitorLotUsedCount;
        private String queuedTimeStamp;
        private ObjectIdentifier mainProcessDefinitionID;
        private String opeNo;
        private Boolean bankInRequiredFlag;
        private ObjectIdentifier lotScheduleID;
        private Long usedCount;
        private String controlUseState;
        private ObjectIdentifier pilotWaferID;
        private Long pilotWaferPosition;
        private Long pilotProcessCount;
        private ObjectIdentifier controlJobID;
        private String requiredCassetteCategory;
        private ObjectIdentifier reticleSetID;
        private String previousControlUseState;
        private Boolean serialManagementFlag;
        private String oderType;
        private ObjectIdentifier vendorProductSpecificationID;
        private String operationName;
        private String processLagTimeStamp;
        private Boolean backupProcessingFlag;
        private Boolean currentLocationFlag;
        private Boolean transferFlag;
        private BackupAddress bornSiteAddress;
        private ObjectIdentifier inventoryStateChangedPersonID;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Yuri
     * @date 11/22/2018 5:52 PM
     */
    @Data
    public static class QTimeInformation {
        private String originalQTime;
        private String qTimeType;
        private String processDefinitionLevel;
        private ObjectIdentifier waferID;
        private ObjectIdentifier qrestrictionTriggerRouteID;
        private String qrestrictionTriggerOperationNumber;
        private String qrestrictionTriggerBranchInfo;
        private String qrestrictionTriggerReturnInfo;
        private String qrestrictionTriggerTimeStamp;
        private ObjectIdentifier qrestrictionTargetRouteID;
        private String qrestrictionTargetOperationNumber;
        private String qrestrictionTargetBranchInfo;
        private String qrestrictionTargetReturnInfo;
        private String qrestrictionTargetTimeStamp;
        private String qrestrictionRemainTime;
        private String previousTargetInfo;
        private String control;
        private boolean qrestrictionWatchRequiredFlag;
        private boolean qrestrictionActionDoneFlag;
        private boolean qrestrictionManualCreatedFlag;
        private boolean qrestrictionPreTrigger;
    }

    @Data
    public static class DCDefDataItem {
        private ObjectIdentifier dcDefID;
        private List<DCDefDataItemDetailInfo> dcDefDataItemDetailInfoSeq;
    }

    @Data
    public static class DCDefDataItemDetailInfo {
        private String dataItemName;
        private String description;
        private String dataCollectionMode;
        private String dataCollectionUnit;
        private String dataType;
        private String itemType;
        private String measurementType;
        private String waferPosition;
        private String sitePosition;
        private Boolean historyRequiredFlag;
        private String calculationType;
        private String calculationExpression;
        private String tag;
    }

    @Data
    public static class DCSpecDataItem {
        private ObjectIdentifier dcSpecID;
        private List<DCSpecDetailInfo> dcSpecDetailInfoSeq;
    }

    @Data
    public static class OperationDataCollectionSetting {
        private String recipeType;
        private List<DefaultRecipeWithDCID> defaultRecipeSettings;
        private List<EqpSpecificRecipeWithDCID> eqpSpecificRecipeSettings;
        private ObjectIdentifier measurementDCSpecID;
        private List<DeltaDCSetting> deltaDCSettings;
        private List<CorrespondingOperationInfo> correspondingOperations;
    }

    @Data
    public static class DefaultRecipeWithDCID {
        private ObjectIdentifier recipeID;
        private List<Chamber> chamberSeq;
        private ObjectIdentifier dcDefID;
        private ObjectIdentifier dcSpecID;
    }

    @Data
    public static class Chamber {
        private ObjectIdentifier chamberID;
        private Boolean state;
    }

    @Data
    public static class EqpSpecificRecipeWithDCID {
        private ObjectIdentifier recipeID;
        private List<Chamber> chamberSeq;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier dcSpecID;
    }

    @Data
    public static class DeltaDCSetting {
        private ObjectIdentifier previousOperationID;
        private String previousOperationNumber;
        private ObjectIdentifier previousDCDefID;
        private ObjectIdentifier postOperationID;
        private String postOperationNumber;
        private ObjectIdentifier postDCDefID;
        private ObjectIdentifier deltaDCDefID;
        private ObjectIdentifier deltaDCSpecID;
    }

    @Data
    public static class SourceLotsAttributes {
        private ObjectIdentifier lotID;                                         //<i>lot ID
        private ObjectIdentifier cassetteID;                                    //<i>Carrier ID
        private ObjectIdentifier stockerID;                                     //<i>stocker ID
        private String transferStatus;                                 //<i>Transfer Status
        private String shelfPositionX;                                 //<i>Shelf Position X
        private String shelfPositionY;                                 //<i>Shelf Position Y
        private String shelfPositionZ;                                 //<i>Shelf Position Z
        private List<SourceWafersAttributes> sourceWafersAttributesList;                //<i>Sequence of Source Wafers Attributes
    }

    @Data
    public static class DurableRegistInfo {
        private Boolean updateFlag;                 //<i>UpdateFlag
        private String className;                  //<i>ClassName
        private List<DurableAttribute> durableAttributeList;    //<i>durable Attribute List
    }

    @Data
    public static class ObjectResult {
        private ObjectIdentifier objectID;                      //<i>Object ID
        private String objectType;                    //<i>Object Type
        private RetCode<Object> result;                     //<i>Transaction Execution Result Information
    }

    @Data
    public static class UserDefinedData {
        private String classID;          //<i>Class ID
        private long seqNo;            //<i>Sequence Number
        private String name;             //<i>Name
        private String type;             //<i>Type
        private String description;      //<i>Description
        private String initialValue;     //<i>Initial value
        private String selectedClassID;  //<i>Selected class ID
        private String selectedCategory; //<i>Selected category
        private Boolean mandatoryFlag;    //<i>Mandatory Flag
    }

    @Data
    public static class WaferListInLotFamilyInfo {
        private ObjectIdentifier lotID;               //<i>lot ID
        private ObjectIdentifier waferID;             //<i>wafer ID
        private Boolean scrapFlag;    //<i>Scrap Flag
        private Integer slotNumber;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 11/27/2018 5:51 PM
     */
    @Data
    public static class EntityInhibitExceptionLot {
        private ObjectIdentifier lotID;             //<i>Entity Inhibit Exception lot
        private ObjectIdentifier entityInhibitID;   //<i>Entity Inhibition ID
        private Boolean singleTriggerFlag; //<i>0:Multi  1:Single
    }

    @Data
    public static class CandidateDurableSubStatusDetail {
        private String durableStatus;
        private List<CandidateDurableSubStatus> candidateDurableSubStatuses;

        public CandidateDurableSubStatusDetail() {
        }

        public CandidateDurableSubStatusDetail(String durableStatus, List<CandidateDurableSubStatus> candidateDurableSubStatuses) {
            this.durableStatus = durableStatus;
            this.candidateDurableSubStatuses = candidateDurableSubStatuses;
        }
    }

    @Data
    public static class CandidateDurableSubStatus {
        private ObjectIdentifier durableSubStatus;
        private String durableSubStatusName;
        private String durableSubStatusDescription;
        private Boolean availableForDurableFlag;

        public CandidateDurableSubStatus() {
        }

        public CandidateDurableSubStatus(ObjectIdentifier durableSubStatus, String durableSubStatusName, String durableSubStatusDescription, Boolean availableForDurableFlag) {
            this.durableSubStatus = durableSubStatus;
            this.durableSubStatusName = durableSubStatusName;
            this.durableSubStatusDescription = durableSubStatusDescription;
            this.availableForDurableFlag = availableForDurableFlag;
        }
    }

    @Data
    public static class MandPRecipeInfo {
        private ObjectIdentifier machineRecipeID;
        private String physicalRecipeID;
    }

    @Data
    public static class LotStatusInfo {
        private ObjectIdentifier lotID;                      //<i>lot ID
        private ObjectIdentifier productID;                  //<i>Product ID
        private String representativeState;        //<i>Representative lot Status
        private LotStatusAttributes currentStatus;        //<i>Current lot Status
        private boolean onFloorFlag;                //<i>lot on floor Flag
        private boolean onHoldFlag;                 //<i>lot on hold Flag
        private long priorityClass;              //<i>lot Priority Class
        private long externalPriority;           //<i>lot external priority
        private double internalPriority;           //<i>lot internal priority
    }

    @Data
    public static class LotStatusAttributes {
        private String lotState;               //<i>lot state
        private String productionState;        //<i>lot production state
        private String holdState;              //<i>lot hold state
        private String finishedState;          //<i>lot finished state
        private String processState;           //<i>lot process state
        private String inventoryState;         //<i>lot inventory state
    }

    @Data
    public static class ProductGroupIDListAttributes {
        private ObjectIdentifier productGroupID;
        private String productGroupName;
        private String description;
        private ObjectIdentifier technologyID;
        private ObjectIdentifier ownerID;
        private Double XChipSize;
        private Double YChipSize;
        private Double planCycleTime;
        private Double planYield;
        private Long grossDieCount;
    }

    @Data
    public static class GatePassLotInfo {
        private ObjectIdentifier lotID;                     //<i>lot ID
        private ObjectIdentifier currentRouteID;            //<i>Current Route ID
        private String currentOperationNumber;    //<i>Current Operation Number
    }

    @Data
    public static class FlowBatchByManualActionReqCassette {
        private ObjectIdentifier cassetteID;
        private List<ObjectIdentifier> lotID;
        //private ObjectIdentifier fromFlowBatchID;
    }

    @Data
    public static class ReFlowBatchByManualActionReqCassette {
        private ObjectIdentifier cassetteID;
        private List<ObjectIdentifier> lotID;
        private ObjectIdentifier fromFlowBatchID;
    }

    @Data
    public static class ContainedCassettesInFlowBatch {
        private ObjectIdentifier cassetteID;
        private long processSequenceNumber;
        private List<ContainedLotInCassetteInFlowBatch> strContainedLotInCassetteInFlowBatch;
    }

    @Data
    public static class ContainedLotInCassetteInFlowBatch {
        private ObjectIdentifier lotID;
        private List<LotWafer> strLotWafer;
    }

    @Data
    public static class RemoveCassette {
        private ObjectIdentifier cassetteID;
        private List<ObjectIdentifier> lotID;
    }

    @Data
    public static class BatchingReqLot {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
    }

    @Data
    public static class BatchedLot {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
        private long processSequenceNumber;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/10/2018 5:14 PM
     */
    @Data
    public static class ProcessJob {
        private String processJobID;             //<i>Process Job ID
        private String processJobPosition;        //<i>Process Job Position in the controlJob
        private ObjectIdentifier machineRecipeID; //<i>Machine Recipe ID
        private String physicalRecipeID;           //<i>Physical Recipe ID
        private Boolean processStartFlag;         //<i>Process Start Flag
        private List<ProcessWafer> processWaferList;            //<i>Process wafer List
    }

    @Data
    public static class LotProcessOperation {
        private String lotID;
        private String objrefPO;
    }

    @Data
    public static class FutureQtimeInfo {
        private ObjectIdentifier triggerRouteID;         //<i>Trigger Route ID
        private String triggerOperationNumber; //<i>Trigger Operation Number
        private ObjectIdentifier targetRouteID;          //<i>Target Route ID
        private String targetOperationNumber;  //<i>Target Operation Number
        private String qTimeType;              //<i>qtime Type (By lot/By wafer)
        private long expiredTimeDuration;    //<i>Expired Time Duration
        private String qrestrictionAction;     //<i>Queue Time Restriction Action
        private ObjectIdentifier reasonCode;             //<i>Reason CimCode
        private ObjectIdentifier actionRouteID;          //<i>Action Route  CimCode
        private String actionOperationNumber;  //<i>Action Operation Number
        private ObjectIdentifier reworkRouteID;          //<i>Rework Route ID
        private String futureHoldTiming;       //<i>Future Hold Timing
        private ObjectIdentifier messageID;              //<i>Message ID
        private String customField;            //<i>Custom Field
        private boolean isMinQTime;             // 是否是Min QTime(true.Min QTime; false.Max QTime)
    }

    @Data
    public static class ScriptInfo {
        private ObjectIdentifier routeID;         //<i>Route ID
        private ObjectIdentifier operationID;     //<i>Operation ID
        private String OperationNumber; //<i>Operation Number
        private ObjectIdentifier pre1;            //<i>Pre1
        private ObjectIdentifier pre2;            //<i>Pre2
        private ObjectIdentifier post;            //<i>Post
    }

    /**
     * @author ZQI
     * @date: 2018/12/05
     */
    @Data
    public static class MachineRecipeInfo {
        /**
         * Machine Recipe ID
         */
        private ObjectIdentifier machineRecipeID;
        /**
         * Physical Recipe ID
         */
        private String physicalRecipeID;
        /**
         * File Location
         */
        private String fileLocation;
        /**
         * File Name
         */
        private String fileName;
        /**
         * Format Flag
         */
        private Boolean formatFlag;
        /**
         * Upload eqp ID
         */
        private ObjectIdentifier uploadEquipmentID;
        /**
         * Last Upload User
         */
        private ObjectIdentifier lastUploadUser;
        /**
         * Last Upload Time Stamp
         */
        private Timestamp lastUploadTimeStamp;
        /**
         * Last Delete User
         */
        private ObjectIdentifier lastDeleteUser;
        /**
         * Last Delete Time Stamp
         */
        private Timestamp lastDeleteTimeStamp;
        /**
         * Sequence of Recipe Download eqp Information
         */
        private List<RecipeDownloadEquipmentInfo> strRecipeDownloadEquipmentInfo;
    }

    /**
     * @author ZQI
     * @date: 2018/12/05
     */
    @Data
    public static class RecipeDownloadEquipmentInfo {
        /**
         * eqp ID
         */
        private ObjectIdentifier equipmentID;
        /**
         * Last Download User
         */
        private ObjectIdentifier lastDownloadUser;
        /**
         * Last Download Time Stamp
         */
        private Timestamp lastDownloadTimeStamp;

        public RecipeDownloadEquipmentInfo() {

        }

        public RecipeDownloadEquipmentInfo(ObjectIdentifier equipmentID, ObjectIdentifier lastDownloadUser, Timestamp lastDownloadTimeStamp) {
            this.equipmentID = equipmentID;
            this.lastDownloadUser = lastDownloadUser;
            this.lastDownloadTimeStamp = lastDownloadTimeStamp;
        }
    }

    @Data
    public static class FlowBatchLostLotInfo {
        private ObjectIdentifier lotID;                   //<i>lot ID
        private ObjectIdentifier cassetteID;              //<i>Carrier ID
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/11/2018 5:20 PM
     */
    @Data
    public static class OperationNameAttributesFromHistory {
        private Long seqno;              //<i>Sequence Number
        private ObjectIdentifier routeID;            //<i>Route ID
        private ObjectIdentifier operationID;        //<i>Operation ID
        private String operationNumber;    //<i>Operation Number
        private String operationName;      //<i>Operation Name
        private String operationPass;      //<i>Operation Pass
        private String pdType;             //<i>Process Definition Type
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/11/2018 6:05 PM
     */
    @Data
    public static class ProcessOperationHistoryInfo {
        private String mainProcessDefinitionID;
        private String processDefinitionID;
        private String processDefinitionType;
        private String operationNumber;
        private Integer operationPassCount;
        private Timestamp claimTime;
        private Timestamp eventCreateTime;
        private Timestamp storeTime;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/5 16:29
     */
    @Data
    public static class LotRecipeParameterEventStructOut {
        private List<OpeHisRecipeParmInfo> strOpeHisRecipeParmInfo;
        private List<OpeHisRecipeParmWaferInfo> strOpeHisRecipeParmWaferInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/5 16:31
     */
    @Data
    public static class OpeHisRecipeParmInfo {
        private String recipeParameterName;
        private String recipeParameterValue;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/5 16:33
     */
    @Data
    public static class OpeHisRecipeParmWaferInfo {
        private ObjectIdentifier waferID;
        private ObjectIdentifier machineRecipeID;
        private List<OpeHisRecipeParmInfo> strOpeHisRecipeParmInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/5 13:57
     */
    @Deprecated
    @Data
    public static class LotOperationStartEventRecord extends EventRecord {
        private LotEventData lotData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<String> reticleIDs;
        private List<String> fixtureIDs;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String batchID;
        private String controlJobID;
        private Boolean locateBackFlag;
        private Boolean testCriteriaResult;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private List<WaferLevelRecipeEventData> waferLevelRecipe;
        private List<String> samplingWafers;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/5 13:58
     */
    @Deprecated
    @Data
    public static class WaferLevelRecipeEventData {
        private String waferID;
        private String machineRecipeID;
        private List<WaferRecipeParmEventData> waferRecipeParameters;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/5 13:58
     */
    @Deprecated
    @Data
    public static class WaferRecipeParmEventData {
        private String parameterName;
        private String parameterValue;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/3/5 13:57
     */
    @Deprecated
    @Data
    public static class RecipeParmEventData {
        private String parameterName;
        private String parameterValue;
    }

    @Data
    public static class FlowBatchControlInfo {
        private String name;
        private Long size;
        private Long minimumSize;
        private Boolean targetOperation;
        private Double flowBatchingOffsetTime;
        private Long minWaferCount;
    }

    @Data
    public static class FlowBatchInfo {
        private ObjectIdentifier flowBatchID;                                               //<i>flowbatch ID
        private List<FlowBatchedCassetteInfoExtend> flowBatchedCassetteInfoList;      //<i>Sequence of Flow Batched Carrier Info
    }

    @Data
    public static class FlowBatchedCassetteInfoExtend {
        private ObjectIdentifier cassetteID;               //<i>Carrier ID
        private Long processSequenceNumber;      //<i>Process Sequence Number
        private List<FlowBatchedLotInfoExtend> flowBatchedLotInfoList;    //<i>Sequence of pptFlowBatchedLotInfo struct
    }

    @Data
    public static class FlowBatchedLotInfoExtend {
        private ObjectIdentifier lotID;                      //<i>lot ID
        private Long processSequenceNumber;      //<i>Process Sequence Number
        private String lotStatus;                  //<i>lot Status
        private String transferStatus;             //<i>Transfer Status
        private ObjectIdentifier stockerID;                  //<i>stocker ID
        private ObjectIdentifier equipmentID;                //<i>eqp ID
        private String priorityClass;              //<i>Priority Class
        private ObjectIdentifier productID;                  //<i>Product ID
        private Long flowBatchOperationCount;    //<i>Flow Batch Operation Count
        private Long flowBatchLotSize;           //<i>Flow Batch lot Size
        private List<EntityInhibitAttributes> entityInhibitionsList;  //<i>Sequence of pptEntityInhibitAttributes struct
    }

/*    @Data
    public static class EntityInhibitAttributesInfo {
        private List<Infos.EntityIdentifier> entityIdentifierList;  //<i>Sequence of Entities
        private List<String>      subLotTypesList;                  //<i>Sequence of Sub lot Types.
        private String            startTimeStamp;                   //<i>Start Time Stamp
        private String            endTimeStamp;                     //<i>End Time Stamp
        private String            reasonCode;                       //<i>Inhibit Registration Reason CimCode.
        private String            reasonDesc;                       //<i>Inhibit Registretion Reason Description
        private String            memo;                             //<i>Comment for inhibition
        private ObjectIdentifier  ownerID;                          //<i>Owner ID of who registed the Inhibition.
        private String            claimedTimeStamp;                 //<i>Claimed Time Stamp
    }*/

    @Data
    public static class FlowBatchInformation {
        private ObjectIdentifier flowBatchID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
    }

    @Data
    public static class RouteInfo {
        private ObjectIdentifier routeID;
        private String routePDType;
        private List<OperationInformation> strOperationInformationList;
    }

    @Data
    public static class OperationInformation {
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String operationPDType;
        private Boolean mandatoryFlag;
        private Boolean whiteDefFlag;
        private String fPCCategory;
        private List<HashedInfo> strHashedInfoSeq;
        private List<NestedRouteInfo> strNestedRouteInfoList;
    }

    @Data
    public static class NestedRouteInfo {
        private ObjectIdentifier routeID;
        private String routePDType;
        private String returnOperationNumber;
        private List<NestedOperationInfo> strNestedOperationInformationList;
    }

    @Data
    public static class NestedOperationInfo {
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String operationPDType;
        private Boolean mandatoryFlag;
        private Boolean whiteDefFlag;
        private String fPCCategory;
        private List<HashedInfo> strHashedInfoSeq;
        private List<Nested2RouteInfo> strNested2RouteInfoList;
    }

    @Data
    public static class Nested2RouteInfo {
        private ObjectIdentifier routeID;
        private String routePDType;
        private String returnOperationNumber;
        private List<Nested2OperationInfo> strNested2OperationInformationList;
    }

    @Data
    public static class Nested2OperationInfo {
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String operationPDType;
        private Boolean mandatoryFlag;
        private Boolean whiteDefFlag;
        private String fPCCategory;
        private List<HashedInfo> strHashedInfoSeq;
    }

    @Data
    public static class WhatNextEquipmentInfoInfo {
        private ObjectIdentifier equipmentID;
        private String equipmentCategory;
        private ObjectIdentifier lastRecipeID;
        private Integer processRunSizeMaximum;
        private Integer processRunSizeMinimum;
        private Boolean bondingEqpFlag;
    }

    @Data
    public static class ObjSamplingMessageAttribute {
        private ObjectIdentifier lotID;
        private Integer messageType;
        private String messageText;
    }

    @Data
    public static class ProcessHoldHistory {
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier productID;
        private String holdType;
        private boolean withExecFlag;
        private ObjectIdentifier reasonCodeID;
    }

    @Data
    public static class ProcessHoldRequest {
        private String operationNumber;
        private ObjectIdentifier requestPerson;
        private String holdType;
        private ObjectIdentifier reasonCode;
        private String claimMemo;
        private String claimedTimeStamp;
        private ObjectIdentifier routeID;
        private ObjectIdentifier productID;
        private boolean withExecHoldFlag;
		private String departmentNamePlate;
		private ObjectIdentifier oldReasonCode;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param * @param null -
     * @author Lin
     * @date 2018/12/18 10:55
     * @return
     */
    @Data
    public static class ChamberProcessLotInfo {
        private ObjectIdentifier lotID;
        private String actionCode;
        private List<ChamberProcessWaferInfo> chamberProcessWaferInfos;
    }

    @Data
    public static class ChamberProcessWaferInfo {
        private ObjectIdentifier waferID;
        private List<ProcessedChamberInfo> processedChamberInfos;
    }

    @Data
    public static class ProcessedChamberInfo {
        private ObjectIdentifier chamberID;
        private String processReportedTimeStamp;
    }

    @Data
    public static class EqpInprocessingControlJobInfo {
        private List<EqpInprocessingControlJob> strEqpInprocessingControlJob;
    }

    @Data
    public static class ProcessResourceInfo {
        private String processWafer;
        private List<ObjectIdentifier> processResourceIDs;
    }

    /**
     * @author ZQI
     * @date: 2018/12/11
     */
    @Data
    public static class EDCConfigListInqInParm {
        /**
         * lot ID
         */
        private ObjectIdentifier lotID;
        /**
         * eqp ID
         */
        private ObjectIdentifier equipmentID;
        /**
         * Machine Recipe ID
         */
        private ObjectIdentifier machineRecipeID;
        /**
         * PD ID
         */
        private ObjectIdentifier pdID;
        /**
         * DC Def ID
         */
        private ObjectIdentifier dcDefID;
        /**
         * Object ID
         */
        private ObjectIdentifier objectID;
        /**
         * Object Type
         */
        private String objectType;
        /**
         * Data Collection Type
         */
        private String dcType;
        /**
         * O/Search Condition of White Definition
         */
        private String whiteDefSearchCriteria;
        /**
         * Max Count  (Range:1-9999 , Default:100)
         */
        private Long maxCount;
        /**
         * FPC Category
         */
        private String FPCCategory;
        /**
         * Search Criteria of Data Collection
         */
        private String dcSearchCriteria;
    }

    /**
     * @author ZQI
     * @date: 2018/12/11
     */
    @Data
    public static class DataCollection {
        /**
         * Object ID
         */
        private ObjectIdentifier objectID;
        /**
         * Object Type
         */
        private String objectType;
        /**
         * DC Type
         */
        private String dcType;
        /**
         * Description
         */
        private String description;
        /**
         * White Deifinition Flag
         */
        private Boolean whiteDefFlag;
        /**
         * FPC Category
         */
        private String FPCCategory;
    }

    @Data
    public static class DefaultRecipeSetting {
        private ObjectIdentifier recipe;
        private List<Chamber> chamberSeq;
        private ObjectIdentifier dcDefinition;
        private ObjectIdentifier dcSpec;
        private ObjectIdentifier binDefinition;
        private List<ObjectIdentifier> fixtureGroups;
        private ObjectIdentifier sampleSpecification;
        private List<ProcessResourceState> processResourceStates;
        private List<RecipeParameter> recipeParameters;
    }

    @Data
    public static class FloatBatch {
        private ObjectIdentifier flowBatchID;          //<i>Flow Batch ID
        private long flowBatchOperationCount;        //<i>Flow Batch Operation Count
        private ObjectIdentifier cassetteID;                         //<i>Carrier ID
        private List<FlowBatchedLotInfo> flowBatchedLotInfos;    //<i>Sequence of pptFlowBatchedLotInfo struct
    }

    @Data
    public static class ProcessResourcePositionInfo {
        private ObjectIdentifier processResourceID;
        private List<WaferPositionInProcessResourceInfo> waferPositionInProcessResourceInfoList;
    }

    @Data
    public static class WaferPositionInProcessResourceInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier waferID;
        private String position;
        private String processReportedTimeStamp;
    }

    /**
     * description:posTimeRestrictionSpecification_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/20/2018 3:27 PM
     */
    @Data
    public static class TimeRestrictionSpecification {
        private ObjectIdentifier productSpecification;
        private DefaultTimeRestrictionSpecification defaultTimeRS;
    }

    /**
     * description:posTimeRestrictionSub_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/20/2018 3:33 PM
     */
    @Data
    public static class TimeRestrictionSub {
        private String originalQTime;
        private String branchInfo;
        private String subRouteID;
        private String triggerOperationNumber;
        private String targetOperationNumber;
        private String control;
    }

    /**
     * description:posTimeRestrictionSubAction_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/20/2018 3:35 PM
     */
    @Data
    public static class TimeRestrictionSubAction {
        private String branchInfo;
        private String subRouteID;
        private Double expiredTimeDuration;
        private String action;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier actionRouteID;
        private String operationNumber;
        private String timing;
        private ObjectIdentifier mainProcessDefinition;
        private ObjectIdentifier messageDefinition;
        private String customField;
    }

    /**
     * description:posTimeRestrictionSpecificationByProductGroup_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/20/2018 4:19 PM
     */
    @Data
    public static class TimeRestrictionSpecificationByProductGroup {
        private ObjectIdentifier productGroup;
        private DefaultTimeRestrictionSpecification defaultTimeRS;
    }

    /**
     * description:posTimeRestrictionSpecificationByTechnology_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/20/2018 4:21 PM
     */
    @Data
    public static class TimeRestrictionSpecificationByTechnology {
        private ObjectIdentifier technology;
        private DefaultTimeRestrictionSpecification defaultTimeRS;
    }

    /**
     * description:posDefaultTimeRestrictionSpecification_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/20/2018 4:21 PM
     */
    @Data
    public static class DefaultTimeRestrictionSpecification {
        private String targetOperationNumber;
        private String qTimeType;
        private Double expiredTimeDuration;
        private List<TimeRestrictionAction> actions;
        private String processDefinitionLevel;
        private List<TimeRestrictionSub> subs;
        private List<TimeRestrictionSubAction> subActions;
        private List<TimeRestrictionSub> reworks;
        private List<TimeRestrictionSubAction> reworkActions;
    }

    /**
     * @author ZQI
     * @date 2018/12/22
     */
    @Data
    public static class FlowBatchedLot {
        /**
         * lot ID
         */
        private ObjectIdentifier lotID;
        /**
         * Carrier ID
         */
        private ObjectIdentifier cassetteID;
        /**
         * Number of sequence
         */
        private Long processSequenceNumber;
    }

    /**
     * description:pptDCDefResult__120_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/24/2018 10:43 AM
     */
    @Data
    public static class DCDefResult {
        private ObjectIdentifier dataCollectionDefinitionID;
        private String description;
        private String dataCollectionType;
        private List<DCItemResult> dcItemResultList;
        private Boolean calculationRequiredFlag;
        private Boolean specCheckRequiredFlag;
        private ObjectIdentifier dataCollectionSpecificationID;
        private String dcSpecDescription;
        private ObjectIdentifier previousDataCollectionDefinitionID;
        private ObjectIdentifier previousOperationID;
        private String previousOperationNumber;
    }

    /**
     * description:pptDCItemResult__120_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 12/24/2018 10:46 AM
     */
    @Data
    public static class DCItemResult {
        private String dataCollectionItemName;       //Data Collection Item Name
        private String dataCollectionMode;           //Data Collection Mode
        private String dataCollectionUnit;           //Data Collection Unit
        private String dataType;                     //Data Type
        private String itemType;                     //Item Type
        private String measurementType;              //Measurement Type
        private String processJobID;                 //Process Job ID
        private ObjectIdentifier waferID;            //wafer ID
        private String waferPosition;                //wafer Position
        private String sitePosition;                 //Site Position
        private Boolean historyRequiredFlag;         //History Required Flag
        private String calculationType;              //Calculation Type
        private String calculationExpression;        //Calculation Expression
        private String dataValue;                    //Data Value
        private String targetValue;                  //Target Value
        private String specCheckResult;              //Specification Check Result
        private List<String> actionCode;            //Sequence of Action Codes
        private String dataItemName;                 //Data Item Name
        private Boolean screenLimitUpperRequired;     //Screen Limit Upper Required Flag
        private Double screenLimitUpper;             //Screen Limit Upper
        private String actionCodesUpperScreen;       //Action Codes Upper Screen
        private Boolean screenLimitLowerRequired;     //Screen Limit Lower Required Flag
        private Double screenLimitLower;             //Screen Limit Lower
        private String actionCodesLowerScreen;       //Action Codes Lower Screen
        private Boolean specLimitUpperRequired;       //Specification Limit Upper Required Flag
        private Double specLimitUpper;               //Specification Limit Upper
        private String actionCodesUpperSpecLimit;    //Action Codes Upper Specification Limit
        private Boolean specLimitLowerRequired;       //Specification Limit Lower Required Flag
        private Double specLimitLower;               //Specification Limit Lower
        private String actionCodesLowerSpecLimit;    //Action Codes Lower Specification Limit
        private Boolean controlLimitUpperRequired;   //Control Limit Upper Required Flag
        private Double controlLimitUpper;            //Control Limit Upper
        private String actionCodesUpperControlLimit; //Action Codes Upper Control Limit
        private Boolean controlLimitLowerRequired;    //Control Limit Lower Required Flag
        private Double controlLimitLower;            //Control Limit Lower
        private String actionCodesLowerControlLimit;//Action Codes Lower Control Limit
        private Double target;                       //Target
        private String tag;                          //Tag
        private String dcSpecGroup;                  //DC Spec Group
    }

    @Data
    public static class RemoveLot {
        private ObjectIdentifier lotID;
        private ObjectIdentifier cassetteID;
    }

    @Data
    public static class PortResource {
        private String portResourceName;
        private String portGroupName;
        private String loadMode;
        private String associatedPortResourceName;
        private String loadPurposeTypeId;
        private Long loadSequenceNumber;
        private Long unloadSequenceNumber;
        private List<ObjectIdentifier> carrierCategories;
        private List<String> specialPortControls;
    }

    @Data
    public static class PortGroupInfo {
        private String portGroupName;
        private List<String> accessModes;
        private List<CapableOperationMode> capableOperationModes;
    }

    @Data
    public static class CapableOperationMode {
        ObjectIdentifier operationMode;
        Boolean validFlag;
    }

    @Data
    public static class EqpMonitorWaferUsedCount {
        private ObjectIdentifier waferID;
        private Long eqpMonitorUsedCount;
    }

    /**
     * @author ZQI
     * @date 2018/12/27
     */
    @Data
    public static class BackupOperation {
        private String processOperation;
        private String reworkOutKey;
    }

    @Data
    public static class MonitoredCompLots {
        private ObjectIdentifier productLotID;
        private ObjectIdentifier productCassetteID;
        private List<LotHoldReq> strLotHoldReleaseReqList;
    }

    @Data
    public static class CheckMonitorInfo {
        private Boolean bMonitorOperationFlag;
        private String strEqpMonitorJobID;
        private Integer nStartSeq;
    }

    @Data
    public static class ChangeLotSchdlReturn {
        private ObjectIdentifier lotID;
        private String returnCode;
    }

    @Data
    public static class WhatNextCastInfo {
        private ObjectIdentifier cassetteID;
        private String transferStatus;
        private ObjectIdentifier transferReserveUserID;
        private String multiLotType;
        private String cassetteCategory;
        private Boolean inPostProcessFlagOfCassette;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier stockerID;
        private Boolean operableFlagForMultiRecipeCapability;
    }

    @Data
    public static class WhatNextRecipeInfo {
        private ObjectIdentifier machineRecipeID;
        private Boolean recipeAvailableFlag;
        private ObjectIdentifier processMonitorProductID;
        private ObjectIdentifier testTypeID;
        private String physicalRecipeID;
    }

    @Data
    public static class DispatchDecision {
        private Timestamp plannedStartTime;
        private Timestamp plannedEndTime;
        private Object activity;
    }

    @Data
    public static class EffectiveWhatNextLogic {
        private String effectiveDateTime;
        private String logicName;
        private String parameters;
    }

    @Data
    public static class LogicInfo {
        private String logicName;
        private String params;
    }

    @Data
    public static class ProcessActivity {
        private List<ObjectIdentifier> processMaterialGroups;
        private ObjectIdentifier plannedMachine;
        private String processActivityAttributes;
    }

    @Data
    public static class WhatNextNext2EQPInfo {
        private ObjectIdentifier next2EquipmentID;
        private ObjectIdentifier Next2LogicalRecipeID;
        private String next2requiredCassetteCategory;
    }

    @Data
    public static class WhatNextChamberInfoInfo {
        private String chamberID;
        private String currentStateID;
        private String currentStateObjRef;
        private Boolean availableFlag;
        private Boolean conditionalAvailable;
    }

    /**
     * @author ZQI
     * @date 2019/01/02
     */
    @Data
    public static class StorageMachineResource {
        private String resourceID;
        private String type;
        private String description;
    }

    @Data
    public static class SpecificMachine {
        private ObjectIdentifier productSpecification;
        private List<ObjectIdentifier> machines;
    }

    @Data
    public static class FlowBatchedCassette {
        private ObjectIdentifier cassetteID;
        private List<ObjectIdentifier> lotID;
        private Long processSequenceNumber;
    }

    @Data
    public static class TempFlowBatchLot {
        private ObjectIdentifier lotID;
        private String lotStatus;
        private String transferStatus;
        private ObjectIdentifier stockerID;
        private ObjectIdentifier equipmentID;
        private String priorityClass;
        private ObjectIdentifier productID;
        private Long flowBatchOperationCount;
        private Long flowBatchLotSize;
        private Long flowBatchLotMinSize;
        private Long flowBatchLotMinWaferSize;
        private List<EntityInhibitAttributes> entityInhibitions;
    }

    @Data
    public static class CDAValueInqInParm {
        private String stringifiedObjectReference;
        private String className;
        private List<HashedInfo> strHashedInfoSeq;
        private String userDataName;
        private String userDataOriginator;
    }

    @Data
    public static class CDAValueUpdateReqInParm {
        private String stringifiedObjectReference;
        private String className;
        private List<HashedInfo> strHashedInfoSeq;
        private String actionCode;
        private List<UserData> strUserDataSeq;
    }

    @Data
    public static class UserDataAction {
        private String userDataName;
        private String originator;
        private String actionCode;
        private String fromType;
        private String fromValue;
        private String toType;
        private String toValue;
    }

    /**
     * @author ZQI
     * @date 2019/01/04
     */
    @Data
    public static class BrStockerInfo {
        private String identifier;
        private String stockerId;
        private String description;
        private String instanceName;
        private String stockerCategoryId;
        private String cellControllerNode;
        private Long maxReticleCapacity;
        private Boolean underTrackStorageFlag;
        private Boolean useForSlmFlag;
        private Long maxUnderTrackStorageCapacity;
        private ObjectIdentifier stockerOwner;
        private String supplierName;
        private String modelNumber;
        private String serialNumber;
        private ObjectIdentifier initialEquipmentStateCode;
        private ObjectIdentifier rawEquipmentStateSetId;
        private List<Infos.BrResource> resources;
        private List<Infos.UserDataSet> userDataSets;
    }

    /**
     * @author ZQI
     * @date 2019/01/04
     */
    @Data
    public static class BrResource {
        private String resourceId;
        private String type;
        private String description;
    }

    /**
     * @author ZQI
     * @date 2019/01/04
     */
    @Data
    public static class UserDataSet {
        private String name;
        private String type;
        private String value;
        private String originator;
    }


    @Data
    public static class UserParameterValue {
        private Long changeType;
        private String parameterName;
        private String dataType;
        private String keyValue;
        private String value;
        private Boolean valueFlag;
        private String description;
    }

    @Data
    public static class BRSUserDefinedVariableInfo {
        private String pVariableName;
        private String pVariableType;
        private String pDescription;
        private String pOwnerClassName;
        private String pDefaultValue;
        private String pOwnerName;
        private String pLevelsSupported;
    }

    /**
     * @author ZQI
     * @date 2019/01/10
     */
    @Data
    public static class EquipmentInfo {
        private String identifier;
        private String equipmentId;
        private String description;
        private String instanceName;
        private String smplPolicyName;
        private String smplWaferAttribute;
        private ObjectIdentifier equipmentOwner;
        private List<String> processResourceNames;
        private List<PortResource> portResources;
        private List<BufferResourceData> bufferResources;
        private List<Infos.PortGroupInfo> portGroups;
        private List<String> reticlePodPorts;
        private Integer maxReticleCapacity;
        private Boolean takeOutInTransferFlag;
        private Boolean batchInNotificationRequiredFlag;
        private Boolean eqpToEqpTransferFlag;
        private Boolean reticleRequiredFlag;
        private Boolean fixtureRequiredFlag;
        private Boolean carrierChangeRequiredFlag;
        private Boolean carrierIdReadableFlag;
        private Boolean waferMapCheckableFlag;
        private Boolean waferIdReadableFlag;
        private Boolean emptyCarrierEarlyOutFlag;
        private Boolean processJobControlFlag;
        private Integer maxWaferCapability;
        private Boolean slmCapabilityFlag;              //<i>SLM Capability                     //DSIV00000099
        private String slmSwitch;                      //<i>SLM Switch                             //DSIV00000099
        private String equipmentCategoryId;
        private String multipleRecipeCapability;
        private ObjectIdentifier equipmentType;
        private List<String> onlineModes;
        private List<String> specialEquipmentControls;
        private Boolean recipeBodyManageFlag;
        private List<RecipeParameterData> equipmentParameters;
        private List<EffectiveControlData> whatNextControls;
        private List<EffectiveControlData> whereNextControls;
        private Boolean processBatchAvailableFlag;
        private String processBatchUnit;
        private Integer minimumProcessBatchSize;
        private Integer maximumProcessBatchSize;
        private Integer minimumWaferCount;
        private Integer reticleStoreLimit;
        private List<StockerData> stockers;
        private Boolean underTrackStoragePriorityFlag;
        private String cellControllerNode;
        private String supplierName;
        private String modelNumber;
        private String serialNumber;
        private Boolean monitorCreationFlag;
        private List<ControlLotBankData> controlLotBanks;
        private List<DocumentData> operationProcedures;
        private Integer maximumRunWafers;
        private Integer maximumRunTime;
        private Integer maximumStartCount;
        private Integer pmIntervalTime;
        private Integer standardWPH;
        private ObjectIdentifier rawEquipmentStateSetId;
        private ObjectIdentifier initialEquipmentStateCode;
        private List<ObjectIdentifier> fpcCategories;
        private Boolean whiteDefinitionFlag;
        private List<UserDataSet> userDataSets;
        private String prControlFlag;   // Contamination Function
        // tool capability function
        private List<String> eqpCapabilities;
        private List<ProcessResourceCapabilityInfo> prCapabilities;
    }

    @Data
    public static class ProcessResourceCapabilityInfo {
        private String processResourceName;
        private List<String> capabilities;
    }

    @Data
    public static class BufferResourceData {
        private String bufferCategory;
        private Integer capacity;
    }

    @Data
    public static class RecipeParameterData {
        private String name;
        private String unit;
        private String dataType;
        private String defaultValue;
        private String lowerLimit;
        private String upperLimit;
        private Boolean useCurrentValueFlag;
        private String tag;
    }

    @Data
    public static class EffectiveControlData {
        private String effectiveDateTime;
        private String parameter;
    }

    @Data
    public static class StockerData {
        private ObjectIdentifier stocker;
        private Boolean underTrackStorageFlag;
        private Boolean slmUnderTrackStorageFlag;
    }

    @Data
    public static class ControlLotBankData {
        private String controlLotType;
        private ObjectIdentifier bank;
    }

    @Data
    public static class DocumentData {
        private String title;
        private ObjectIdentifier documentOwner;
        private String documentType;
        private String contents;
    }

    @Data
    public static class ProcessResourceState {
        private String processResourceName;
        private Boolean state;
    }

    @Data
    public static class MachineLot {
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier lotID;
        private ObjectIdentifier unloadCassetteID;
        private ObjectIdentifier unloadPortID;
        private Long unloadSequenceNumber;
        private Boolean dataCollectionRequiredFlag;
    }

    @Data
    public static class ControlLotBank {
        private String controlLotType;
        private ObjectIdentifier bankID;
    }

    @Data
    public static class CheckMessage {
        private String className;
        private String identifier;
        private String message;
    }

    @Data
    public static class MachineContainerInfo {
        private ObjectIdentifier machineContainer;
        private ObjectIdentifier machine;
        private ObjectIdentifier chamber;
        private Integer maxCapacity;
        private Integer maxReserveCount;
        private String description;
        private String lastClaimedTimeStamp;
        private ObjectIdentifier lastClaimedUser;
    }

    @Data
    public static class MachineSpecificRecipeSetting {
        private ObjectIdentifier machine;
        private ObjectIdentifier recipe;
        private List<ProcessResourceState> processResourceStates;
        private List<RecipeParameter> recipeParameters;
        private ObjectIdentifier dcSpec;
        private ObjectIdentifier setup;
    }

    @Data
    public static class LogicalRecipeInfo {
        private String identifier;
        private String logicalRecipeId;
        private String version;
        private String description;
        private boolean multipleChamberSupportFlag;
        private String recipeType;
        private ObjectIdentifier fpcCategory;
        private boolean whiteDefinitionFlag;
        private ObjectIdentifier testType;
        private List<DefaultRecipeSettingData> defaultRecipeSettings;
        private List<EquipmentSpecificRecipeSettingData> equipmentSpecificRecipeSettings;
        private ObjectIdentifier monitorProduct;
        private boolean monitorHoldFlag;
        private String subLotType;
        private String waferSamplingPolicyName;
        private String waferSamplingAttribute;
        private boolean equipmentDIDefinitionFlag;
        private boolean equipmentDIDefinitionResetFlag;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class DefaultRecipeSettingData {
        private ObjectIdentifier recipe;
        private List<ProcessResourceStateSettingData> processResourceStateSettings;
        private List<ProcessResourceStateData> processResourceStates;
        private List<RecipeParameterData> recipeParameters;
        private ObjectIdentifier dcDefinition;
        private ObjectIdentifier dcSpec;
        private ObjectIdentifier binDefinition;
        private ObjectIdentifier sampleSpec;
        private List<ObjectIdentifier> fixtureGroups;
    }

    @Data
    private static class ProcessResourceStateSettingData {
        private String processResourceName;
        private String state;
    }

    @Data
    public static class ProcessResourceStateData {
        private String processResourceName;
        private Boolean stateFlag;
    }

    @Data
    public static class EquipmentSpecificRecipeSettingData {
        private ObjectIdentifier recipe;
        private List<ProcessResourceStateData> processResourceStates;
        private ObjectIdentifier equipment;
        private List<RecipeParameterData> recipeParameters;
        private ObjectIdentifier dcSpec;
    }

    @Data
    public static class RecipeInfo {
        private String identifier;
        private String recipeNameSpace;
        private String physicalRecipeId;
        private String version;
        private String description;
        private boolean forceDownloadFlag;
        private boolean recipeBodyConfirmFlag;
        private boolean conditionalDownloadFlag;
        private ObjectIdentifier fpcCategory;
        private boolean whiteDefinitionFlag;
        private String fileLocation;
        private List<ObjectIdentifier> equipments;
        private List<ObjectIdentifier> userGroups;
        private String genericRecipe;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class DownloadedMachine {
        private ObjectIdentifier machine;
        private ObjectIdentifier user;
        private Timestamp lastTimeStamp;
    }

    @Data
    public static class FutureReworkRequestDetail {
        private String trigger;
        private ObjectIdentifier reworkRouteID;
        private String returnOperationNumber;
        private ObjectIdentifier reasonCode;
    }

    @Data
    public static class FutureReworkRequestInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private Timestamp lastClaimedTimeStamp;
        private ObjectIdentifier modifier;
        private List<FutureReworkRequestDetail> futureReworkRequestDetails;
    }

    @Data
    public static class BrLotTypeInfo {
        private String identifier;
        private String lotTypeId;
        private String description;
        private List<SubLotTypeData> subLotTypes;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class SubLotTypeData {
        private String subLotTypeId;
        private String description;
        private String leadingCharacterForLotId;
        private int duration;
    }

    @Data
    public static class SubLotType {
        private String subLotType;
        private String description;
        private String leadingCharacter;
        private int duration;
    }

    @Data
    public static class PlannedSplitJobInfoDetail {
        private ObjectIdentifier subRouteID;
        private String returnOperationNumber;
        private String mergeOperationNumber;
        private String memo;
        private boolean dynamicFlag;
        private boolean executedFlag;
        private Timestamp executedTimeStamp;
        private List<ObjectIdentifier> wafers;
        //add run card psmKey     //use doc select psm_key to update and  psm delete the run card data
        private String psmKey;
    }

    @Data
    public static class PlannedSplitJobInfo {
        private ObjectIdentifier lotFamilyID;
        private ObjectIdentifier splitRouteID;
        private String splitOperationNumber;
        private ObjectIdentifier originalRouteID;
        private String originalOperationNumber;
        private boolean actionEMail;
        private boolean actionHold;
        private Boolean actionSeparateHold;
        private Boolean actionCombineHold;
        private String testMemo;
        private boolean executedFlag;
        private Timestamp executedTimeStamp;
        private Timestamp lastClaimedTimeStamp;
        private ObjectIdentifier modifier;
        private List<PlannedSplitJobInfoDetail> plannedSplitJobInfoDetails;
    }

    @Data
    public static class DurableInfo {
        private ObjectIdentifier durableID;
        private int quantity;
        private String durableCategory;
    }

    @Data
    public static class HoldRecord {
        private String holdType;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier holdPerson;
        private String holdTimeStamp;
        private ObjectIdentifier relatedLot;
        private boolean responsibleOperationFlag;
        private String holdClaimMemo;
        private String departmentNamePlate;
		private ObjectIdentifier oldReasonCode;
		private ObjectIdentifier oldHoldPerson;
		private ObjectIdentifier oldRelatedLot;
    }

    @Data
    public static class FutureHoldRecord {
        private String holdType;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier requestPerson;
        private ObjectIdentifier mainProcessDefinition;
        private String operationNumber;
        private String claimedTimeStamp;
        private ObjectIdentifier relatedLot;
        private String claimMemo;
        private boolean singleTriggerFlag;
        private boolean postFlag;
		private String departmentNamePlate;
		// edit
		private ObjectIdentifier oldReasonCode;
		private ObjectIdentifier oldRequestPerson;
		private ObjectIdentifier oldRelatedLot;
    }

    @Data
    public static class DCItemDefinition {
        private String dataItemName;
        private String description;
        private String unitOfMeasure;
        private String valType;
        private String itemType;
        private String dataCollectionMethod;
        private String measType;
        private String waferPosition;
        private String sitePosition;
        private String calculationType;
        private String calculationExpression;
        private boolean isStored;
        private String tag;
    }

    @Data
    public static class Shift {
        private String shiftName;
        private Timestamp startTime;
        private Timestamp endTime;
        Object shiftAttributes;
    }

    @Data
    public static class Coordinate2D {
        private float x;
        private float y;
    }

    @Data
    public static class StatusChangeDurableInfo {
        private ObjectIdentifier durableID;
        private String durableStatus;
        private ObjectIdentifier durableSubStatus;
    }

    @Data
    public static class MultiDurableStatusChangeReqInParm {
        private String durableStatus;
        private ObjectIdentifier durableSubStatus;
        private String durableCategory;
        private String reticleLocation;
        private List<Infos.StatusChangeDurableInfo> statusChangeDurableInfos;
    }

    @Data
    public static class CassetteInfo {
        private String identifier;
        private String carrierId;
        private String description;
        private String instanceName;
        private ObjectIdentifier carrierCategory;
        private String carrierType;
        private Boolean usageCheckRequiredFlag;
        private Integer maximumRunTime;
        private Integer maximumStartCount;
        private Integer capacity;
        private Integer nominalSize;
        private String contents;
        private Integer intervalBetweenPM;
        private String usageType;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class XferJobComp {
        private ObjectIdentifier carrierID;        //<i>Carrier ID
        private ObjectIdentifier toMachineID;    //<i>To Machine ID
        private ObjectIdentifier toPortID;       //<i>To Port ID
        private String transferJobStatus;
    }

    @Deprecated
    @Data
    public static class EventFIFOData {
        private String watchDogName;
        private String eventTime;
        private String eventObjRef;
    }

    @Data
    public static class PosWaferReworkCount {
        private String key;
        private int reworkCount;
    }

    @Data
    public static class PosMultiMainPD {
        private String orderType;
        private String mfgLayer;
        private ObjectIdentifier bom;
        private ObjectIdentifier mainProcessDefinition;
    }

    @Data
    public static class PosProcessOperationEventData {
        private String routeID;
        private String operationNumber;
        private String operationID;
        private int operationPassCount;
        private String objrefPOS;
        private String objrefPO;
        private String objrefMainPF;
        private String objrefModulePOS;
        private List<String> samplingWafers;
    }

    @Data
    public static class PosProcesFlowConnection {
        private ObjectIdentifier branchPD;
        private String returnOperationNumber;
    }

    @Data
    public static class OperationModeInfo {
        private String onlineMode;
        private String accessMode;
        private String dispatchMode;
        private String operationStartMode;
        private String operationCompMode;
    }

    @Data
    public static class OperationMode {
        private ObjectIdentifier operationMode;
        private String onlineMode;
        private String dispatchMode;
        private String accessMode;
        private String moveInMode;
        private String moveOutMode;
        private String description;
    }

    @Data
    public static class EqpMonitorNextExecutionTimeCalculateIn {
        private Timestamp currentScheduleBaseTime;
        private Integer executionInterval;
        private Integer scheduleAdjustment;
        private Timestamp lastMonitorPassedTime;
        private Integer expirationInterval;
        private Boolean futureTimeRequireFlag;
        private String siInfo;
    }

    @Data
    public static class BrEquipmentStateInfo {
        private String identifier;
        private String equipmentStateCode;
        private String equipmentStateName;
        private String equipmentStateDescription;
        private ObjectIdentifier e10State;
        private Boolean equipmentAvailableFlag;
        private Boolean conditionalAvailableFlag;
        private List<String> availableSubLotTypes;
        private Boolean manufacturingStateChangeableFlag;
        private List<ObjectIdentifier> nextTransitionStates;
        private Boolean changeToOtherE10Flag;
        private Boolean changeFromOtherE10Flag;
        private List<Infos.BrEquipmentStateConvertingConditionData> convertingConditions;
        private List<Infos.BrActionCodeData> actionCodes;
        private List<Infos.UserDataSet> userDataSets;
        private String description;

        // user groups access control
        private List<String> userGroups;

    }

    @Data
    public static class BrEquipmentStateConvertingConditionData {
        private ObjectIdentifier toEquipmentStateCode;
        private String attributeValue;
        private String convertingLogic;
        private String checkSequence;
    }

    @Data
    public static class BrActionCodeData {
        private List<ObjectIdentifier> equipmentIds;
        private String actionCode;
        private List<String> actionParameters;
    }

    @Data
    public static class BrRawEquipmentStateSetInfo {
        private String identifier;
        private String rawEquipmentStateSetId;
        private String description;
        private List<Infos.BrTranslatingCodeData> translatingCodes;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class BrTranslatingCodeData {
        private String rawEquipmentStateCode;
        private ObjectIdentifier translatingEquipmentStateCode;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 3/13/2019 6:34 PM
     */
    @Data
    public static class lotFutureHoldDR {
        private String lotID;
        private String futureHoldType;
        private String futureHoldReasonCode;
        private String futureHoldReasonCodeObj;
        private String futureHoldUserID;
        private String futureHoldUserObj;
        private String futureHoldMainProcessDefinitionID;
        private String futureHoldMainProcessDefinitionObj;
        private String futureHoldOperationNumber;
        private String futureHoldClaimTime;
        private String futureHoldRelatedLotID;
        private String futureHoldRelatedLotObj;
        private String futureHoldClaimMemo;
        private String futureHoldSingleTrigFlag;
        private String futureHoldPostFlag;
    }

    @Data
    public static class EqpMonitorJobDetailInfo {
        private ObjectIdentifier eqpMonitorJobID;                //<i>Auto Monitor Job ID
        private ObjectIdentifier equipmentID;                    //<i>Equipment ID
        private ObjectIdentifier chamberID;                      //<i>Chamber ID
        private ObjectIdentifier eqpMonitorID;                   //<i>Auto Monitor ID
        private String monitorJobStatus;               //<i>Status of Auto Monitor Job
        private List<EqpMonitorLot> strEqpMonitorLotSeq;            //<i>Lots Assigned for Auto Monitor
        private Integer retryCount;                     //<i>Current Retry Count
        private Timestamp startTimeStamp;                 //<i>Start Time of Auto Monitor Job
        private ObjectIdentifier startUser;                      //<i>Start User of Auto Monitor Job
        private Timestamp lastClaimedTimeStamp;           //<i>Last Claimed Time Stamp
        private ObjectIdentifier lastClaimedUser;                //<i>Last Claimed User
    }

    @Data
    public static class EqpMonitorJobListGetDRIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier chamberID;
        private ObjectIdentifier eqpMonitorID;
        private ObjectIdentifier eqpMonitorJobID;
    }

    @Data
    public static class EqpMonitorLot {
        private ObjectIdentifier lotID;                          //<i>Lot ID
        private Integer startSeqNo;                     //<i>Start Order Control at Monitor Operation
        private String monitorLotStatus;               //<i>Lot Status for Auto Monitor
        private ObjectIdentifier monitorRouteID;                 //<i>Monitor Route ID
        private String monitorOpeNo;                   //<i>Monitor Operation Number
        private ObjectIdentifier carrierID;                      //<i>Carrier ID
        private String lotStatus;                      //<i>Lot Status
        private String lotType;                        //<i>Lot Type
        private String subLotType;                     //<i>Sub Lot Type
        private ObjectIdentifier routeID;                        //<i>Route ID
        private String opeNo;                          //<i>Operation Number
        private ObjectIdentifier operationID;                    //<i>Operation ID
        private ObjectIdentifier assignedMachineID;              //<i>Assigned Machine ID
        private String productID;                      //<i>Product ID
        private Integer totalWaferCount;                //<i>Total Wafer Count
        private Boolean inhibitFlag;                    //<i>Inhibit
    }

    @Deprecated
    @Data
    public static class TableSRData {
        private String keyValue;
        private Double value;
    }

    @Deprecated
    @Data
    public static class TableSSData {
        private String keyValue;
        private Double value;
    }

    @Data
    public static class MachineStateConvertCondition {
        private String checkSequence;
        private String convertLogic;
        private String attributeValue;
        private ObjectIdentifier toMachineStateCode;
    }

    @Data
    public static class ProcessLagTimeData {
        private ObjectIdentifier productSpecification;
        private Double expiredTimeDuration;
    }

    @Data
    public static class Privilege {
        private String privilegeID;
        private String permission;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/27 14:49
     */
    @Data
    public static class QtimeLotSetClearByOperationCompOut {
        private RetCode strResult;
        //        private List<HoldList>          strLotHoldReleaseList;
        private List<LotHoldReq> strLotHoldReleaseList;
        //        private List<HoldList>          strFutureHoldCancelList;
        private List<LotHoldReq> strFutureHoldCancelList;
        private List<FutureReworkInfo> strFutureReworkCancelList;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/27 14:54
     */
    @Data
    public static class QtimeLotSetClearByOperationCompIn {
        private ObjectIdentifier lotID;
        private Boolean previousOperationFlag;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/27 14:51
     */
    @Data
    public static class HoldList extends HoldReq {
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/27 14:51
     */
    @Data
    public static class HoldReq {
        protected String holdType;

        protected ObjectIdentifier holdReasonCodeID;
        protected ObjectIdentifier holdUserID;
        protected String responsibleOperationMark;

        protected ObjectIdentifier routeID;
        protected String operationNumber;
        protected ObjectIdentifier relatedLotID;
        protected String claimMemo;
        protected Object siInfo;
    }

    @Data
    public static class AreaGroupInfo {
        private String identifier;
        private String areaGroupId;
        private String description;
        List<ObjectIdentifier> workAreas;
        List<ObjectIdentifier> notAuthorizedEquipments;
        List<UserDataSet> userDataSets;
    }

    @Data
    public static class BrUserGroupInfo {
        private String identifier;
        private String userGroupId;
        private Boolean companyFlag;
        private List<ObjectIdentifier> companies;
        private String description;
        private String userGroupType;
        private List<Infos.UserDataSet> userDataSets;

        // for user group control extensive access control
        private List<String> purposes;
        private List<UserGroupAccessControlInfo.Department> departments;
    }

    @Data
    public static class MaxProcessCount {
        private String operationNumber;
        private long count;
    }

    @Data
    public static class UserInfo {
        private String identifier;
        private String userId;
        private String userName;
        private ObjectIdentifier company;
        private ObjectIdentifier department;
        private String password;
        private int expiredPeriod;
        private boolean supervisorFlag;
        private String eMailAddress;
        private String phoneNumber;
        private List<ObjectIdentifier> privilegeGroups;
        private List<ObjectIdentifier> pptAreaGroups;
        private List<ObjectIdentifier> brmUserGroups;
        private boolean brmReleasePermissionFlag;
        private boolean brmReleaseConditionFlag;
        private boolean brmDeletePermissionFlag;
        private boolean brmDeleteConditionFlag;
        private boolean brmActivatePermissionFlag;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class BrSystemMessageCodeInfo {
        private String identifier;
        private String subsystemId;
        private String systemMessageCode;
        private String description;
        private ObjectIdentifier messageDistribution;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class WorkAreaInfo {
        private String identifier;
        private String workAreaId;
        private String description;
        private ObjectIdentifier location;
        private List<Infos.StockerData> stockers;
        private List<ObjectIdentifier> equipments;
        private List<Infos.UserDataSet> userDataSets;
    }


    @Data
    public static class RecipeIdListForDOCInqInParm {
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier machineRecipeID;
        private ObjectIdentifier pdID;
        private String fpcCategory;
        private String whiteDefSearchCriteria;
        private String recipeSearchCriteria;
    }

    @Data
    public static class CustomerInfo {
        private String identifier;
        private String customerId;
        private String description;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class BrProcessDefinitionInfo {
        private String identifier;
        private String processDefinitionId;
        private String version;
        private String description;
        private String processDefinitionName;
        private String pdType;
        private ObjectIdentifier department;
        private ObjectIdentifier fpcCategory;
        private Boolean whiteDefinitionFlag;
        private String defaultLogicalRecipeId;
        private String defaultLogicalRecipeVersion;
        private ObjectIdentifier defaultLogicalRecipe;
        private List<Infos.BrLogicalRecipeDataByTechnology> logicalRecipesByTechnology;
        private List<Infos.BrLogicalRecipeDataByProductGroup> logicalRecipesByProductGroup;
        private List<Infos.BrLogicalRecipeData> logicalRecipes;
        private List<Infos.BrSpecificEquipmentDataByTechnology> specificEquipmentsByTechnology;
        private List<Infos.BrSpecificEquipmentDataByProductGroup> specificEquipmentsByProductGroup;
        private List<Infos.BrSpecificEquipmentData> specificEquipments;
        private List<ObjectIdentifier> equipments;
        private Double standardProcessTime;
        private Double standardWaitTime;
        private Long standardWPH;
        private List<ObjectIdentifier> parts;
        private List<String> jobStatus;
        private List<Infos.UserDataSet> userDataSets;
        // tool capability req
        private String capabilityReq;
    }

    @Data
    public static class BrMainProcessDefinitionInfo {
        private String identifier;
        private String mainProcessDefinitionId;
        private String version;
        private String description;
        private String mainProcessDefinitionType;
        private String processFlowType;
        private Boolean monitorRouteFlag;
        private ObjectIdentifier mfgLayer;
        private ObjectIdentifier startBank;
        private ObjectIdentifier endBank;
        private ObjectIdentifier mainProcessDefinitionOwner;
        private List<Infos.BrModuleProcessDefinitionData> moduleProcessDefinitions;
        private List<Infos.BrPOSData> mainProcessOperations;
        private Infos.BrEnumExpandFlag expandFlag;
        private Boolean dynamicRouteFlag;
        private List<Infos.UserDataSet> userDataSets;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/7 10:07
     */
    @Data
    public static class DCSMgrSendOperationCompletedRptOut {
        private RetCode strResult;
        private DCSControlJobInfo controlJobInfo;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/7 10:08
     */
    @Data
    public static class DCSControlJobInfo {
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<StartCassette> strStartCassette;
        private Object siInfo;
    }

    @Data
    public static class SpecificMachineByTechnology {
        private ObjectIdentifier technology;
        private List<ObjectIdentifier> machines;
    }

    @Data
    public static class SpecificMachineByProductGroup {
        private ObjectIdentifier productGroup;
        private List<ObjectIdentifier> machines;
    }

    @Data
    public static class BrModuleProcessDefinitionInfo {
        private String identifier;
        private String moduleNumber;
        private String moduleProcessDefinitionId;
        private String version;
        private String description;
        private List<Infos.BrModuleProcessOperationData> moduleProcessOperations;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class BrFlowReleaseInfo {
        private Boolean changedFlowFlag;
        private List<Infos.BrOperationReleaseInfo> operations;
    }

    @Data
    public static class BrModuleProcessOperationData {
        private String operationNumber;
        private String processDefinitionId;
        private String version;
        private ObjectIdentifier processDefinition;
        private String processOperationNumber;  //  This is for the target operation of SPC (or SPEC) Check.
        private String recycleSamplingOperationNumber; // This is for the target operation of Recycle.Sampling.
        private List<String> responsibleOperationNumbers;
        private Boolean mandatoryOperationRequiredFlag;
        private Infos.BrFlowBatchControlData flowBatchControl;
        private Infos.BrPilotWaferProcessControlData pilotWaferProcessControl;
        private ObjectIdentifier defaultMeasDCSpec;
        private List<Infos.BrMeasDCSpecDataByTechnology> measDCSpecsByTechnology;
        private List<Infos.BrMeasDCSpecDataByProductGroup> measDCSpecsByProductGroup;
        private List<Infos.BrMeasDCSpecData> measDCSpecs;
        private ObjectIdentifier defaultTestSpec;
        private List<Infos.BrTestSpecDataByTechnology> testSpecsByTechnology;
        private List<Infos.BrTestSpecDataByProductGroup> testSpecsByProductGroup;
        private List<Infos.BrTestSpecData> testSpecs;
        private List<Infos.BrDeltaDCDefinitionData> deltaDCDefinitions;
        private List<Infos.BrDefaultDeltaDCSpecData> defaultDeltaDCSpecs;
        private List<Infos.BrDeltaDCSpecDataByTechnology> deltaDCSpecsByTechnology;
        private List<Infos.BrDeltaDCSpecDataByProductGroup> deltaDCSpecsByProductGroup;
        private List<Infos.BrDeltaDCSpecData> deltaDCSpecs;
        private List<Infos.BrSubFlowData> subProcessDefinitions;
        private List<Infos.BrSubFlowData> reworkProcessDefinitions;
        private Long defaultReworkLimit;
        private List<Infos.BrReworkLimitDataByTechnology> reworkLimitsByTechnology;
        private List<Infos.BrReworkLimitDataByProductGroup> reworkLimitsByProductGroup;
        private List<Infos.BrReworkLimitData> reworkLimits;
        private Long defaultProcessLimit;
        private List<Infos.BrProcessLimitDataByTechnology> processLimitsByTechnology;
        private List<Infos.BrProcessLimitDataByProductGroup> processLimitsByProductGroup;
        private List<Infos.BrProcessLimitData> processLimits;
        private ObjectIdentifier postScript;
        private ObjectIdentifier pre1Script;
        private ObjectIdentifier pre2Script;
        private List<Infos.BrDefaultTimeRestrictionData> defaultTimeRestrictions;
        private List<Infos.BrTimeRestrictionDataByTechnology> timeRestrictionsByTechnology;
        private List<Infos.BrTimeRestrictionDataByProductGroup> timeRestrictionsByProductGroup;
        private List<Infos.BrTimeRestrictionData> timeRestrictions;
        private Long defaultProcessLagTime;
        private List<Infos.BrProcessLagTimeDataByTechnology> processLagTimesByTechnology;
        private List<Infos.BrProcessLagTimeDataByProductGroup> processLagTimesByProductGroup;
        private List<Infos.BrProcessLagTimeData> processLagTimes;
        private List<Infos.BrEqpMonitorSection> eqpMonitor;
        private List<Infos.CorrespondingOperationInfo> correspondingOperations;
        private String sectionControlCategory;  // todo:ZQI  need to remove in the future.

        // Add new member
        private String pdType;
        // -- Support ReplaceTimeRestrictions for SubRoute.
        private List<BrDefaultReplaceTimeRestrictionData> defaultReplaceTimeRestrictionsForSubRoute;
        private List<BrReplaceTimeRestrictionDataByTechnology> replaceTimeRestrictionsByTechnologyForSubRoute;
        private List<BrReplaceTimeRestrictionDataByProductGroup> replaceTimeRestrictionsByProductGroupForSubRoute;
        private List<BrReplaceTimeRestrictionData> replaceTimeRestrictionsForSubRoute;
        // -- Support ReplaceTimeRestrictions for Rework.
        private List<BrDefaultReplaceTimeRestrictionData> defaultReplaceTimeRestrictionsForReworkRoute;
        private List<BrReplaceTimeRestrictionDataByTechnology> replaceTimeRestrictionsByTechnologyForReworkRoute;
        private List<BrReplaceTimeRestrictionDataByProductGroup> replaceTimeRestrictionsByProductGroupForReworkRoute;
        private List<BrReplaceTimeRestrictionData> replaceTimeRestrictionsForReworkRoute;

        // Contamination Function
        private String contaminationInLvlIdent;
        private String contaminationOutLvlIdent;
        private String prFlagIdent;

        // Pilot Run Function
        private List<BrPilotRunSectionInfo> pilotOperations;
    }


    @Data
    public static class BrDefaultReplaceTimeRestrictionData implements Serializable {
        private static final long serialVersionUID = 2919139560972796011L;
        private String originalTargetOperationNumber;
        private String firstConnectingOperationNumber;
        private ObjectIdentifier firstMainProcessDefinition;
        private String secondConnectingOperationNumber;
        private ObjectIdentifier secondMainProcessDefinition;
        private String specificControl;
        private String triggerOperationNumber;
        private String targetOperationNumber;
        private Long duration;
        private String action;
        private ObjectIdentifier reasonCode;
        private ObjectIdentifier actionMainProcessDefinition;
        private String actionOperationNumber;
        private String timing;
        private ObjectIdentifier reworkMainProcessDefinition;
        private ObjectIdentifier messageDistribution;
        private String customField;

        public ObjectIdentifier getConditionInfo() {
            return null;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BrReplaceTimeRestrictionDataByTechnology extends BrDefaultReplaceTimeRestrictionData {
        private static final long serialVersionUID = -7555223485027658649L;
        private ObjectIdentifier technology;

        @Override
        public ObjectIdentifier getConditionInfo() {
            return this.getTechnology();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BrReplaceTimeRestrictionDataByProductGroup extends BrDefaultReplaceTimeRestrictionData {
        private static final long serialVersionUID = 8970889019945991065L;
        private ObjectIdentifier productGroup;

        @Override
        public ObjectIdentifier getConditionInfo() {
            return this.getProductGroup();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BrReplaceTimeRestrictionData extends BrDefaultReplaceTimeRestrictionData {
        private static final long serialVersionUID = -3548365310452709238L;
        private ObjectIdentifier product;

        @Override
        public ObjectIdentifier getConditionInfo() {
            return this.getProduct();
        }
    }

    @Data
    public static class BrOperationReleaseInfo {
        private String operationNumber;
        private Boolean changedOperationFlag;
    }

    @Data
    public static class BrFlowBatchControlData {
        private String sectionControlCategory;   // Support Wafer Stacking function.
        private String name;
        private Boolean targetOperationFlag;
        private Double flowBatchingOffsetTime;
        private Long maxBatchSize;
        private Long minBatchSize;
        private Long minWaferCount;
    }

    @Data
    public static class BrPilotWaferProcessControlData {
        private String name;
        private Long maxPilotProcessCount;
    }

    @Data
    public static class BrMeasDCSpecDataByTechnology {
        private ObjectIdentifier technology;
        private ObjectIdentifier dcSpec;
    }

    @Data
    public static class BrMeasDCSpecDataByProductGroup {
        private ObjectIdentifier productGroup;
        private ObjectIdentifier dcSpec;
    }

    @Data
    public static class BrMeasDCSpecData {
        private ObjectIdentifier product;
        private ObjectIdentifier dcSpec;
    }

    @Data
    public static class BrTestSpecDataByTechnology {
        private ObjectIdentifier technology;
        private ObjectIdentifier testSpec;
    }

    @Data
    public static class BrTestSpecDataByProductGroup {
        private ObjectIdentifier productGroup;
        private ObjectIdentifier testSpec;
    }

    @Data
    public static class BrTestSpecData {
        private ObjectIdentifier product;
        private ObjectIdentifier testSpec;
    }

    @Data
    public static class BrDeltaDCDefinitionData {
        private String preMeasurementOperationNumber;
        private ObjectIdentifier preDCDefinition;
        private ObjectIdentifier currentDCDefinition;
        private ObjectIdentifier deltaDCDefinition;
    }

    @Data
    public static class BrDefaultDeltaDCSpecData {
        private ObjectIdentifier deltaDCDefinition;
        private ObjectIdentifier deltaDCSpec;
    }

    @Data
    public static class BrDeltaDCSpecDataByTechnology {
        private ObjectIdentifier technology;
        private ObjectIdentifier deltaDCDefinition;
        private ObjectIdentifier deltaDCSpec;
    }

    @Data
    public static class BrDeltaDCSpecDataByProductGroup {
        private ObjectIdentifier productGroup;
        private ObjectIdentifier deltaDCDefinition;
        private ObjectIdentifier deltaDCSpec;
    }

    @Data
    public static class BrDeltaDCSpecData {
        private ObjectIdentifier product;
        private ObjectIdentifier deltaDCDefinition;
        private ObjectIdentifier deltaDCSpec;
    }

    @Data
    public static class BrSubFlowData {
        private ObjectIdentifier mainProcessDefinition;
        private String joiningOperationNumber;
    }

    @Data
    public static class BrReworkLimitDataByTechnology {
        private ObjectIdentifier technology;
        private Long limit;
    }

    @Data
    public static class BrReworkLimitDataByProductGroup {
        private ObjectIdentifier productGroup;
        private Long limit;
    }

    @Data
    public static class BrReworkLimitData {
        private ObjectIdentifier product;
        private Long limit;
    }

    @Data
    public static class BrProcessLimitDataByTechnology {
        private ObjectIdentifier technology;
        private Long limit;
    }

    @Data
    public static class BrProcessLimitDataByProductGroup {
        private ObjectIdentifier productGroup;
        private Long limit;
    }

    @Data
    public static class BrProcessLimitData {
        private ObjectIdentifier product;
        private Long limit;
    }

    @Data
    public static class BrDefaultTimeRestrictionData implements Serializable {
        private static final long serialVersionUID = -5080512178603774904L;
        private String targetOperationNumber;
        private Long duration;
        private String action;
        private ObjectIdentifier reasonCode;
        private String actionOperationNumber;
        private String timing;
        private ObjectIdentifier reworkMainProcessDefinition;
        private ObjectIdentifier messageDistribution;
        private String customField;
        private String qTimeType;

        public ObjectIdentifier getConditionInfo() {
            return null;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BrTimeRestrictionDataByTechnology extends BrDefaultTimeRestrictionData {
        private static final long serialVersionUID = -3673648732263380166L;
        private ObjectIdentifier technology;

        @Override
        public ObjectIdentifier getConditionInfo() {
            return this.technology;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BrTimeRestrictionDataByProductGroup extends BrDefaultTimeRestrictionData {
        private static final long serialVersionUID = 3194190613142941858L;
        private ObjectIdentifier productGroup;

        @Override
        public ObjectIdentifier getConditionInfo() {
            return this.productGroup;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BrTimeRestrictionData extends BrDefaultTimeRestrictionData {
        private static final long serialVersionUID = 3465223091450661490L;
        private ObjectIdentifier product;

        @Override
        public ObjectIdentifier getConditionInfo() {
            return this.product;
        }
    }

    @Data
    public static class BrProcessLagTimeDataByTechnology {
        private ObjectIdentifier technology;
        private Long duration;
    }

    @Data
    public static class BrProcessLagTimeDataByProductGroup {
        private ObjectIdentifier productGroup;
        private Long duration;
    }

    @Data
    public static class BrProcessLagTimeData {
        private ObjectIdentifier product;
        private Long duration;
    }

    @Data
    public static class BrModuleProcessDefinitionData {
        private String moduleNumber;
        private String moduleProcessDefinitionId;
        private String version;
        private ObjectIdentifier moduleProcessDefinition;
        private Boolean sameTimeReleaseInChangeNoticeFlag;
        private ObjectIdentifier moduleProcessDefinitionInChangeNotice;
        private ObjectIdentifier stage;
    }

    @Data
    public static class BrPOSData {
        private String moduleNumber;
        private ObjectIdentifier moduleProcessDefinition;
        private Boolean sameTimeReleaseInChangeNoticeFlag;
        private ObjectIdentifier moduleProcessDefinitionInChangeNotice;
        private ObjectIdentifier stage;
        private String operationNumber;
        private ObjectIdentifier processDefinition;
        private ObjectIdentifier photoLayer;
        private ObjectIdentifier carrierCategory;
        private String processOperationNumber;
        private List<Infos.CorrespondingOperationInfo> correspondingOperations;
        private List<String> responsibleOperationNumbers;
        private String recycleSamplingOperationNumber;  // This is for the target operation of Recycle. Sampling
        private Boolean mandatoryOperationRequiredFlag;
        private Boolean autoBankInRequiredFlag;
        private Boolean endOfMonitorRelationFlag;
        private Boolean monitorGroupReleaseFlag;
        private Infos.BrFlowBatchControlData flowBatchControl;
        private Infos.BrPilotWaferProcessControlData pilotWaferProcessControl;
        private ObjectIdentifier defaultMeasDCSpec;
        private List<Infos.BrMeasDCSpecDataByTechnology> measDCSpecsByTechnology;
        private List<Infos.BrMeasDCSpecDataByProductGroup> measDCSpecsByProductGroup;
        private List<Infos.BrMeasDCSpecData> measDCSpecs;
        private ObjectIdentifier defaultTestSpec;
        private List<Infos.BrTestSpecDataByTechnology> testSpecsByTechnology;
        private List<Infos.BrTestSpecDataByProductGroup> testSpecsByProductGroup;
        private List<Infos.BrTestSpecData> testSpecs;
        private List<Infos.BrDeltaDCDefinitionData> deltaDCDefinitions;
        private List<Infos.BrDefaultDeltaDCSpecData> defaultDeltaDCSpecs;
        private List<Infos.BrDeltaDCSpecDataByTechnology> deltaDCSpecsByTechnology;
        private List<Infos.BrDeltaDCSpecDataByProductGroup> deltaDCSpecsByProductGroup;
        private List<Infos.BrDeltaDCSpecData> deltaDCSpecs;
        private List<Infos.BrSubFlowData> subProcessDefinitions;
        private List<Infos.BrSubFlowData> reworkProcessDefinitions;
        private Long defaultReworkLimit;
        private List<Infos.BrReworkLimitDataByTechnology> reworkLimitsByTechnology;
        private List<Infos.BrReworkLimitDataByProductGroup> reworkLimitsByProductGroup;
        private List<Infos.BrReworkLimitData> reworkLimits;
        private Long defaultProcessLimit;
        private List<Infos.BrProcessLimitDataByTechnology> processLimitsByTechnology;
        private List<Infos.BrProcessLimitDataByProductGroup> processLimitsByProductGroup;
        private List<Infos.BrProcessLimitData> processLimits;
        private ObjectIdentifier postScript;
        private ObjectIdentifier pre1Script;
        private ObjectIdentifier pre2Script;
        private List<Infos.BrDefaultTimeRestrictionData> defaultTimeRestrictions;
        private List<Infos.BrTimeRestrictionDataByTechnology> timeRestrictionsByTechnology;
        private List<Infos.BrTimeRestrictionDataByProductGroup> timeRestrictionsByProductGroup;
        private List<Infos.BrTimeRestrictionData> timeRestrictions;
        private Long defaultProcessLagTime;
        private List<Infos.BrProcessLagTimeDataByTechnology> processLagTimesByTechnology;
        private List<Infos.BrProcessLagTimeDataByProductGroup> processLagTimesByProductGroup;
        private List<Infos.BrProcessLagTimeData> processLagTimes;
        private List<Infos.UserDataSet> userDataSets;
        private List<Infos.BrEqpMonitorSection> eqpMonitor;
        private String sectionControlCategory;   // todo:ZQI  need to remove in the future.

        // Add new Member
        private String pdType;
        // -- Support ReplaceTimeRestrictions for SubRoute.
        private List<BrDefaultReplaceTimeRestrictionData> defaultReplaceTimeRestrictionsForSubRoute;
        private List<BrReplaceTimeRestrictionDataByTechnology> replaceTimeRestrictionsByTechnologyForSubRoute;
        private List<BrReplaceTimeRestrictionDataByProductGroup> replaceTimeRestrictionsByProductGroupForSubRoute;
        private List<BrReplaceTimeRestrictionData> replaceTimeRestrictionsForSubRoute;
        // -- Support ReplaceTimeRestrictions for Rework.
        private List<BrDefaultReplaceTimeRestrictionData> defaultReplaceTimeRestrictionsForReworkRoute;
        private List<BrReplaceTimeRestrictionDataByTechnology> replaceTimeRestrictionsByTechnologyForReworkRoute;
        private List<BrReplaceTimeRestrictionDataByProductGroup> replaceTimeRestrictionsByProductGroupForReworkRoute;
        private List<BrReplaceTimeRestrictionData> replaceTimeRestrictionsForReworkRoute;

        // Contamination function
        private String contaminationInLvlIdent;
        private String contaminationOutLvlIdent;
        private String prFlagIdent;

        // Pilot Run Section
        private List<BrPilotRunSectionInfo> pilotOperations;
    }

    public enum BrEnumExpandFlag {
        notNeedToExpand, needToExpand, expanded, needToLateExpan
    }

    @Data
    public static class BrLogicalRecipeDataByTechnology {
        private ObjectIdentifier technology;
        private String logicalRecipeId;
        private String version;
        private ObjectIdentifier logicalRecipe;
    }

    @Data
    public static class BrLogicalRecipeDataByProductGroup {
        private ObjectIdentifier productGroup;
        private String logicalRecipeId;
        private String version;
        private ObjectIdentifier logicalRecipe;
    }

    @Data
    public static class BrLogicalRecipeData {
        private ObjectIdentifier product;
        private String logicalRecipeId;
        private String version;
        private ObjectIdentifier logicalRecipe;
    }

    @Data
    public static class BrSpecificEquipmentDataByTechnology {
        private ObjectIdentifier technology;
        private List<ObjectIdentifier> equipments;
    }

    @Data
    public static class BrSpecificEquipmentDataByProductGroup {
        private ObjectIdentifier productGroup;
        private List<ObjectIdentifier> equipments;
    }

    @Data
    public static class BrSpecificEquipmentData {
        private ObjectIdentifier product;
        private List<ObjectIdentifier> equipments;
    }

    @Data
    public static class ProcessDefinitionInfo {
        private String number;
        private ObjectIdentifier processDefinition;
        private ObjectIdentifier stage;
    }

    @Data
    public static class EqpMonitorInfo {
        private ObjectIdentifier eqpMonitorID;
        private ObjectIdentifier machineID;
        private ObjectIdentifier chamberID;
        private String description;
        private String monitorType;
        private String scheduleType;
        private List<EqpMonitorProductSpecificationInfo> eqpMonitorProdSpecs;
        private Timestamp startTimeStamp;
        private int executionInterval;
        private int warningInterval;
        private int expirationInterval;
        private boolean standAloneFlag;
        private boolean kitFlag;
        private Timestamp warningTimeStamp;
        private int maxRetryCount;
        private ObjectIdentifier machineStateAtStart;
        private ObjectIdentifier machineStateAtPassed;
        private ObjectIdentifier machineStateAtFailed;
        private List<EqpMonitorActionInfo> eqpMonitorActions;
        private String monitorStatus;
        private Timestamp scheduleBaseTimeStamp;
        private int scheduleAdjustment;
        private Timestamp lastMonitorTimeStamp;
        private String lastMonitorResult;
        private Timestamp lastMonitorPassedTimeStamp;
        private Timestamp lastClaimedTimeStamp;
        private ObjectIdentifier lastClaimedUser;
    }

    @Data
    public static class EqpMonitorProductSpecificationInfo {
        private ObjectIdentifier productSpecificationID;
        private ObjectIdentifier recipeID;
        private int waferCount;
        private int startSeqNo;
    }

    @Data
    public static class BrPrivilegeGroupInfo {
        private String identifier;
        private String subSystemName;
        private String privilegeCategory;
        private String privilegeGroupId;
        private String description;
        private List<Privilege> privilegeIds;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class CustomerProductInfo {
        private String identifier;
        private ObjectIdentifier customer;
        private ObjectIdentifier product;
        private String customerProductId;
        private String description;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class BrScriptInfo {
        private String identifier;
        private String scriptId;
        private String description;
        private String phase;
        private String script;
        private String intermediateCode;
        private List<ObjectIdentifier> usedParameters;
        private List<Infos.BrArgumentOfVerbData> usedArguments;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class BrPcsUserDefVariables {
        private String theTableMarker;
        private String keyName;
        private String variableName;
        private String variableType;
        private String variableDescription;
        private String variableOwnerClass;
        private String variableDefaultValue;
        private String variableOwner;
        private String variableScope;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class BrArgumentOfVerbData {
        private String verbName;
        private String classId;
        private String fieldId;
        private ObjectIdentifier objectId;
        private String fieldValue;
    }


    @Data
    public static class EqpMonitorScheduleUpdateIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier eqpMonitorID;
        private String actionType;
        private Long postponeTime;

    }

    @Data
    public static class EqpMonitorStatusChangeIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier eqpMonitorID;
        private String monitorStatus;
    }

    @Data
    public static class EqpMonitorActionExecuteIn {
        private ObjectIdentifier eqpMonitorJobID;
        private ObjectIdentifier eqpMonitorID;
        private String eventType;
        private EqpMonitorActionInfo strEqpMonitorActionInfo;
    }

    @Data
    public static class EqpMonitorJobStatusChangeIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier eqpMonitorID;
        private ObjectIdentifier eqpMonitorJobID;
        private String monitorJobStatus;
    }

    @Data
    public static class EqpMonitorJobCompletedIn {
        private ObjectIdentifier eqpMonitorID;
        private ObjectIdentifier eqpMonitorJobID;
        private String monitorJobStatus;
    }

    @Data
    public static class MonitorProductInfo {
        private ObjectIdentifier productID;     //<i>Product ID for Auto Monitor
        private Integer waferCount;             //<i>Wafer Count of the Product
        private Integer startSeqNo;             //<i>Start Order Control at Monitor Operation
    }

    @Data
    public static class MainRouteInfo {
        private ObjectIdentifier mainRouteID;                      //<i>Main Route ID
        private String mainPDType;                       //<i>Main PD Type
        private List<MainRouteOperationInfo> mainRouteOperationInfos;    //<i>Sequence of Main Route Operation Information
    }

    @Data
    public static class MainRouteOperationInfo {
        private ObjectIdentifier mainPDID;                      //<i>Main PD ID
        private String operationNumber;               //<i>Operation Number
        private String operationPDType;               //<i>Operation PD Type
        private Boolean mandatoryFlag;                 //<i>Mandatory Flag
        private Boolean whiteDefFlag;                  //<i>White Definition Flag of PD
        private String FPCCategory;                   //<i>FPC Category
        private Integer FPCInfoCount;                  //<i>Count of FPC Information
        private List<ConnectedSubRouteInfo> connectedSubRouteInfos;  //<i>Sequence of Sub Route Information
    }

    @Data
    public static class ConnectedSubRouteInfo {
        private ObjectIdentifier routeID;                                //<i>Route ID
        private String routePDType;                            //<i>PD Type
        private String returnOperationNumber;                  //<i>Return Operation Number
        private List<ConnectedSubRouteOperationInfo> connectedSubRouteOperationInfos;  //<i>Sequence of Sub Route Operation Information
    }

    @Data
    public static class ConnectedSubRouteOperationInfo {
        private ObjectIdentifier operationID;                   //<i>Operataion ID
        private String operationNumber;               //<i>Operation Number
        private String operationPDType;               //<i>Operation PD Type
        private Boolean mandatoryFlag;                 //<i>Mandatory Flag
        private Boolean whiteDefFlag;                  //<i>White Definition Flag of PD
        private String FPCCategory;                   //<i>FPC Category
        private Integer FPCInfoCount;                  //<i>Count of FPC Information
        private List<ConnectedSub2RouteInfo> operationInfoList; //<i>Sequence of Route Information
    }

    @Data
    public static class ConnectedSub2RouteInfo {
        private ObjectIdentifier routeID;                                     //<i>Route ID
        private String routePDType;                                 //<i>PD Type
        private String returnOperationNumber;                       //<i>Return Operation Number
        private List<ConnectedSub2RouteOperationInfo> connectedSub2RouteOperationInfos;  //<i>Sequence of Sub Sub Route Operation Information
    }

    @Data
    public static class ConnectedSub2RouteOperationInfo {
        private ObjectIdentifier operationID;           //<i>Operation ID
        private String operationNumber;       //<i>Operation Number
        private String operationPDType;       //<i>Operation PD Type
        private Boolean mandatoryFlag;         //<i>Mandatory Flag
        private Boolean whiteDefFlag;          //<i>White Definition Flag of PD
        private String FPCCategory;           //<i>FPC Category
        private Integer FPCInfoCount;          //<i>Count of FPC Information
    }

    @Data
    public static class LotRouteInfoForFPC {
        private ObjectIdentifier lotID;                      //<i>Lot ID
        private ObjectIdentifier mainPDID;                   //<i>Main PD ID
        private String operationNumber;            //<i>Operation Number
        private ObjectIdentifier originalMainPDID;           //<i>Original Main PDID
        private String originalOperationNumber;    //<i>Original Operation Number
        private ObjectIdentifier subMainPDID;                //<i>Sub Main PD ID
        private String subOperationNumber;         //<i>Sub Operation Number
    }

    @Data
    public static class OperationInfo {
        private ObjectIdentifier operationID;                           //<i>Operation ID
        private String operationNumber;                       //<i>Operation Number
        private Boolean mandatoryFlag;                         //<i>Mandatory Flag  //D8000024
        private List<ConnectedRoute> connectedRouteList;                 //<i>Sequence of Connected Route Information
    }

    @Data
    public static class DeltaDCDefinition {
        private String previousOperationNumber;
        private ObjectIdentifier previousDCDefinition;
        private ObjectIdentifier currentDCDefinition;
        private ObjectIdentifier deltaDCDefinition;
    }

    @Data
    public static class EqpMonitorInfoUpdateIn {
        private EqpMonitorDetailInfo strEqpMonitorDetailInfo;
        private String actionType;
    }

    @Data
    public static class ProcessJobInfo {
        private String processJobID;                   //<i>Process job ID
        private String processJobPosition;             //<i>Process job Position
        private ObjectIdentifier machineRecipeID;                //<i>Machine Recipe ID
        private List<ProcessWafer> processWaferList;             //<i>Sequence of WaferID
        private Boolean processStartFlag;               //<i>Process Start flag
        private String processJobState;                //<i>Process job status
        private String currentAction;                  //<i>Current requested action
        private ObjectIdentifier requestUserID;                  //<i>Current action requested userID
        private String actionRequestTime;              //<i>The requested time for current action
        private List<RecipeParameterInfo> recipeParameterInfoList;     //<i>Sequence of Recipe Parameter
        private List<ObjectIdentifier> reticles;                       //<i>Sequence of reticles used for this process job
        private String reasontext;                     //<i>Reason text from TCS
    }

    @Data
    public static class PosMainPFInfoForModuleOperation {
        private String mainPF;
        private List<String> opeNos;
    }

    @Data
    public static class PosMainPDInfoForModule {
        private String mainPDID;
        private List<String> moduleNos;
    }

    @Data
    public static class PosProcessDefinitionInfo {
        private String number;
        private ObjectIdentifier processDefinition;
        private ObjectIdentifier stage;
    }

    @Data
    public static class PosPilotProcessControl {
        private String name;
        private String startOperationNumber;
        private String endOperationNumber;
        private int maximumPilotProcessCount;
    }

    @Data
    public static class FPCDispatchEqpInfo {
        private Integer FPCGroupNo;        //<i>FPC Group No
        private Boolean skipFlag;          //<i>Skip Flag
        private Boolean restrictEqpFlag;   //<i>Equipment Restriction Flag
        private Boolean sendEmailFlag;     //<i>Reserved for Sending Email Option
        private Boolean holdLotFlag;       //<i>Reserved for Hold Lot Option
        private Boolean splitFlag;         //<i>Group Lot Auto Split Flag
        private List<ObjectIdentifier> dispatchEqpIDs;    //<i>Sequence of Dispatch Equipment
        private List<ObjectIdentifier> waferIDs;          //<i>Sequence of WaferID
    }

    @Data
    public static class DeltaDCSpecification {
        private ObjectIdentifier productSpecification;
        private ObjectIdentifier deltaDCDefinition;
        private ObjectIdentifier deltaDCSpecification;
    }

    @Data
    public static class ProcessLagTimeDataByTechnology {
        private ObjectIdentifier technology;
        private double expiredTimeDuration;
    }

    @Data
    public static class ProcessLagTimeDataByProductGroup {
        private ObjectIdentifier productGroup;
        private double expiredTimeDuration;
    }

    @Data
    public static class MaxReworkCountForProductSpecification {
        private ObjectIdentifier productSpecification;
        private Long maxReworkCount;
    }

    @Data
    public static class DCDefinitionInfo {
        private String identifier;
        private String dcDefinitionId;
        private String description;
        private String dcType;
        private ObjectIdentifier fpcCategory;
        private Boolean whiteDefinitionFlag;
        private List<Infos.DCItemDataInfo> dcItems;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class DCItemDataInfo {
        private String itemType;
        private String measurementType;
        private String dcItemName;
        private String waferPosition;
        private String sitePosition;
        private String dataType;
        private String unit;
        private String dcItemTag;
        private String calculationType;
        private Boolean calculationTypeFlag;
        private String calculationExpression;
        private String dcMode;
        private Boolean historyRequiredFlag;
    }

    @Data
    public static class ReticleSetInfo {
        private String identifier;
        private String reticleSetId;
        private String description;
        private List<DefaultReticleGroupData> defaultReticleGroups;
        private List<SpecificReticleGroupData> specificReticleGroups;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class DefaultReticleGroupData {
        private String photoLayer;
        private List<ObjectIdentifier> reticleGroups;
        private Boolean overrideFlag;
    }

    @Data
    public static class SpecificReticleGroupData {
        private String photoLayer;
        private ObjectIdentifier product;
        private ObjectIdentifier equipment;
        private List<ObjectIdentifier> reticleGroups;
    }

    @Data
    public static class ProcessResourceInfoGroupByProcessResource {
        private ObjectIdentifier processResourceID;
        private List<String> processWafers;
    }

    @Data
    public static class PilotProcessControl {
        private String name;
        private String startOperationNumber;
        private String endOperationNumber;
        private Long maximumPilotProcessCount;
    }

    @Data
    public static class ProcessInfo {
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private Boolean mandatoryOperationFlag;
        private Boolean processHoldFlag;
        private ObjectIdentifier stageID;
        private String inspectionType;
    }

    @Data
    public static class DCSpecificationInfo {
        String identifier;
        String dcSpecId;
        String description;
        ObjectIdentifier fpcCategory;
        Boolean whiteDefinitionFlag;
        private List<ObjectIdentifier> dcDefinitions;
        private List<Infos.DCSpecItemDatainfo> dcSpecItems;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class DCSpecItemDatainfo {
        private String dcItemName;
        private String dataCollectionSpecGroup;
        private Boolean screenUpperLimitCheckRequiredFlag;
        private Double screenUpperLimit;
        private List<String> screenUpperLimitActionIds;
        private Boolean screenLowerLimitCheckRequiredFlag;
        private Double screenLowerLimit;
        private List<String> screenLowerLimitActionIds;
        private Boolean specUpperLimitCheckRequiredFlag;
        private Double specUpperLimit;
        private List<String> specUpperLimitActionIds;
        private List<String> specLowerLimitActionIds;
        private Boolean specLowerLimitCheckRequiredFlag;
        private Double specLowerLimit;
        private Boolean controlUpperLimitCheckRequiredFlag;
        private Double controlUpperLimit;
        private List<String> controlUpperLimitActionIds;
        private List<String> controlLowerLimitActionIds;
        private Boolean controlLowerLimitCheckRequiredFlag;
        private Double controlLowerLimit;
        private Double targetValue;
        private String tag;
    }

    @Data
    public static class BankInfo {
        private String identifier;
        private String bankId;
        private String description;
        private List<ObjectIdentifier> destinationBanks;
        private ObjectIdentifier stocker;
        private Boolean receiveFlag;
        private Boolean stbFlag;
        private Boolean bankInFlag;
        private Boolean shipFlag;
        private String productType;
        private Boolean waferIdAssignmentRequiredFlag;
        private Boolean productionBankFlag;
        private Boolean recycleBankFlag;
        private Boolean controlWaferBankFlag;
        private List<Infos.UserDataSet> userDataSets;

    }


    @Data
    public static class FPCInfoAction {
        private String actionType;            //<i>Action Type
        //<c>SP_FPCInfo_Create
        //<c>SP_FPCInfo_Update
        //<c>SP_FPCInfo_NoChange
        private FPCInfo strFPCInfo;            //<i>FPC Information
    }

    @Data
    public static class DurableControlJobListInfo {
        private ObjectIdentifier durableControlJobID;                    //<i>Durable Control Job ID
        private ObjectIdentifier equipmentID;                            //<i>Equipment ID
        private String durableCategory;                        //<i>Durable Category
        private String status;                                 //<i>Status
        private String estimatedCompletionTime;                //<i>Estimated Process End Time
        private ObjectIdentifier lastClaimedUserID;                      //<i>Last Claimed User ID
        private String lastClaimedTimeStamp;                   //<i>Last Claimed TimeStamp
        private List<DurableControlJobDurable> strDurableControlJobDurables;           //<i>Sequence of Durable ControlJob's Durable Information
    }

    @Data
    public static class DurableControlJobDurable {
        private ObjectIdentifier durableID;                              //<i>Durable ID
        private String loadPurposeType;                        //<i>Load Purpose Type
        private ObjectIdentifier loadPortID;                             //<i>Load Port ID
        private Long loadSequenceNumber;                     //<i>Load Sequence Number
        private ObjectIdentifier unloadPortID;                           //<i>Unload Port ID
        private Long unloadSequenceNumber;                   //<i>Unload Sequence Number
        private ObjectIdentifier reticleGroupID;                         //<i>Reticle Group ID
        private String status;                                 //<i>Status
        private String description;                            //<i>Description
    }

    @Data
    public static class DeltaDCDefinitionInfo {
        private String deltaDCDefinitionId;
        private String description;
        private String dcType;
        private List<Infos.DCItemDataInfo> dcItems;
        private ObjectIdentifier preDCDefinition;
        private ObjectIdentifier currentDCDefinition;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class DeltaDCSpecInfo {
        private String deltaDCSpecId;
        private String description;
        private ObjectIdentifier deltaDCDefinition;
        private List<Infos.DCSpecItemDatainfo> dcSpecItems;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class PosDeltaDCSpecInfo {
        private ObjectIdentifier deltaDCDefinition;
        private ObjectIdentifier deltaDCSpecification;
    }

    @Data
    public static class BrProductInfo {
        private String identifier;
        private String productId;
        private String ec;
        private String description;
        private String state;
        private Boolean fpcAvailableFlag;
        private Boolean serialManagementFlag;
        private ObjectIdentifier productGroup;
        private String productType;
        private ObjectIdentifier productCategory;
        private ObjectIdentifier productOwner;
        private List<Infos.BrSourceProducts> sourceProducts;
        private ObjectIdentifier mainProcessDefinition;
        private String mainProcessDefinitionId;
        private String mainProcessDefinitionVersion;
        private List<Infos.BrMainProcessDefinitionData> mainProcessDefinitions;
        private ObjectIdentifier reticleSet;
        private String lotGenerationType;
        private Long releaseBatchSize;
        private Double plannedCycleTime;
        private Double plannedYield;
        private Long safetyStockLevel;
        private Boolean finishGoodFlag;
        private ObjectIdentifier mfgLayer;
        private ObjectIdentifier bom;
        private String markingCharacter;
        private ObjectIdentifier schedulerUserGroup;
        private List<Infos.UserDataSet> userDataSets;
        private String usageType;   // Contamination Function

        // NPW usage count
        private Integer maxUsageCount;
        // NPW recycle count
        private Integer maxRecycleCount;
    }

    @Data
    public static class BrSourceProducts {
        private ObjectIdentifier sourceProductSpecification;
        private Integer sourcePriority;
        private String sourceCustomField;
    }

    @Data
    public static class DeltaDCSpecificationByProductGroup {
        private ObjectIdentifier productGroup;
        private ObjectIdentifier deltaDCDefinition;
        private ObjectIdentifier deltaDCSpecification;
    }

    @Data
    public static class BrMainProcessDefinitionData {
        private String orderType;
        private ObjectIdentifier mfgLayer;
        private ObjectIdentifier mainProcessDefinition;
        private String mainProcessDefinitionId;
        private String mainProcessDefinitionVersion;
        private ObjectIdentifier bom;
    }

    @Data
    public static class DeltaDCSpecificationByTechnology {
        private ObjectIdentifier technology;
        private ObjectIdentifier deltaDCDefinition;
        private ObjectIdentifier deltaDCSpecification;
    }

    @Data
    public static class DefaultDeltaDCSpecification {
        private ObjectIdentifier deltaDCDefinition;
        private ObjectIdentifier deltaDCSpecification;
    }

    @Data
    public static class MaxReworkCountForProductGroup {
        private ObjectIdentifier productGroup;
        private Long maxReworkCount;
    }

    @Data
    public static class MaxReworkCountForTechnology {
        private ObjectIdentifier technology;
        private Long maxReworkCount;
    }

    @Data
    public static class MeasurementDCSpecification {
        private ObjectIdentifier productSpecification;
        private ObjectIdentifier dcSpecification;
    }

    @Data
    public static class TestSpec {
        private ObjectIdentifier productSpecification;
        private ObjectIdentifier testSpecification;
    }


    @Data
    public static class ProductGroupInfo {
        private String identifier;
        private String productGroupId;
        private String description;
        private ObjectIdentifier technology;
        private ObjectIdentifier productGroupOwner;
        private Coordinate2D chipSize;
        private Double plannedCycleTime;
        private Double plannedYield;
        private Long grossDieCount;
        private List<ObjectIdentifier> userGroups;
        private List<UserDataSet> userDataSets;
    }


    @Data
    public static class BrTechnologyInfo {
        private String identifier;
        private String technologyId;
        private String description;
        private ObjectIdentifier technologyOwner;
        private List<Infos.UserDataSet> userDataSets;
    }


    @Data
    public static class BrProductCategoryInfo {
        private String identifier;
        private String productCategoryId;
        private String description;
        private List<String> availableMainProcessDefinitionTypes;
        private List<ObjectIdentifier> availableLotTypes;
        private List<Infos.UserDataSet> userDataSets;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/22 下午 12:27
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ExperimentalLotRegistInfo {
        private String userID;                         //<i>User ID
        private String action;                         //<i>Action
        private String claimedTimeStamp;               //<i>Claimed Time Stamp
        private ObjectIdentifier lotFamilyID;                    //<i>Lot Family ID                               //D7000015
        private ObjectIdentifier splitRouteID;                   //<i>Split Route ID                              //D7000015
        private String splitOperationNumber;           //<i>Split Operation Number
        private ObjectIdentifier originalRouteID;                //<i>Original Route ID                           //D7000015
        private String originalOperationNumber;        //<i>Original Operation Number                   //D7000015
        private boolean actionEMail;                    //<i>Action EMail Flag
        private boolean actionHold;                     //<i>Action Hold Flag
        private String testMemo;                       //<i>Test Memo
        private List<Infos.ExperimentalLotRegist> strExperimentalLotRegistSeq;    //<i>Sequence of Experimental Lot Regist
        private String siInfo;                                                           //<i>Reserved for SI customization
        //add psmJobID for history
        private String psmJobID;
        //add runCardID for history
        private String runCardID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/22 下午 12:33
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ExperimentalLotRegist {
        private ObjectIdentifier subRouteID;
        private String returnOperationNumber;                                                           //D7000015
        private String mergeOperationNumber;                                                            //D7000015
        private String memo;
        private List<ObjectIdentifier> waferIDs;
        private String siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/25                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/25 10:22
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class PlannedSplitEventRecord extends EventRecord {
        private String action;
        private String lotFamilyID;
        private String splitRouteID;
        private String splitOperationNumber;
        private String originalRouteID;
        private String originalOperationNumber;
        private boolean actionEMail;
        private boolean actionHold;
        private List<Infos.SplitSubRouteEventData> subRoutes;
    }

    @Deprecated
    @Data
    public static class UserDataChangeEventRecord extends EventRecord {
        private String className;
        private String hashedInfo;
        private List<Infos.UserDataChangeActionEventData> actions;
    }

    @Deprecated
    @Data
    public static class UserDataChangeActionEventData {
        private String name;
        private String orig;
        private String actionCode;
        private String fromType;
        private String fromValue;
        private String toType;
        private String toValue;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/25                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/25 10:28
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class SplitSubRouteEventData {
        private String subRouteID;
        private String returnOperationNumber;
        private String mergeOperationNumber;
        private String parentLotID;
        private String childLotID;
        private String memo;
        private List<Infos.SplitedWaferEventData> wafers;
    }

    @Deprecated
    @Data
    public static class ScriptParameterChangeEventRecord extends EventRecord {
        private String parameterClass;
        private String identifier;
        private List<UserParameterValue> userParameterValues;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/25                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/25 10:30
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class SplitedWaferEventData {
        private String waferID;
        private String successFlag;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/27                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/27 10:02
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class InterFabLotXferPlanInfo {
        private ObjectIdentifier lotID;                  //<i>Lot ID
        private long seqNo;                  //<i>Sequence Number
        private String originalFabID;          //<i>Original Fab ID
        private ObjectIdentifier originalRouteID;        //<i>Original RouteID
        private String originalOpeNumber;      //<i>Original Operation Number
        private String destinationFabID;       //<i>Destination Fab ID
        private ObjectIdentifier destinationRouteID;     //<i>Destination RouteID
        private String destinationOpeNumber;   //<i>Destination Operation Number
        private String xferType;               //<i>Transfer Type
        private String description;            //<i>Description
        private ObjectIdentifier modifierUserID;         //<i>Modifier User ID
        private String modifiedTime;           //<i>Modified Time
        private String state;                  //<i>Plan State
        private String stateUpdateTime;        //<i>Plan State Update Time
        private String siInfo;                   //<i>Reserved for SI customization
    }
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 16:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    /*@Data
    public static class LotFamilyCurrentStatus {
        private ObjectIdentifier lotID;
        private String           siInfo;
    }*/

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/3 10:50
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class FPCEventRecord extends EventRecord {
        private String action;
        private String FPCID;
        private String lotFamilyID;
        private String mainPDID;
        private String operationNumber;
        private String originalMainPDID;
        private String originalOperationNumber;
        private String subMainPDID;
        private String subOperationNumber;
        private String FPCGroupNo;
        private String FPCType;
        private String mergeMainPDID;
        private String mergeOperationNumber;
        private String FPCCategory;
        private String pdID;
        private String pdType;
        private String correspondingOperNo;
        private boolean skipFlag;
        private boolean restrictEqpFlag;
        private String equipmentID;
        private String machineRecipeID;
        private String dcDefID;
        private String dcSpecID;
        private String recipeParameterChangeType;
        boolean sendEmailFlag;
        boolean holdLotFlag;
        private String description;
        private String createTime;
        private String updateTime;
        private List<Infos.WaferRecipeParameterEventData> wafers;
        private List<Infos.ReticleEventData> reticles;
        private List<Infos.DCSpecItemEventData> dcSpecItems;
        private List<Infos.PosCorrespondingOperationEventData> correspondingOperations;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/3 11:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class WaferRecipeParameterEventData {
        private String waferID;
        private List<Infos.PosRecipeParameterEventData> recipeParameters;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/3 11:12
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     * @see com.fa.cim.newcore.dto.event.Event.RecipeParameterEventData
     */
    @Deprecated
    @Data
    public static class PosRecipeParameterEventData {
        private long seq_No;
        private String parameterName;
        private String parameterValue;
        private boolean useCurrentSettingValueFlag;
        private String parameterTag;
        private String parameterUnit;
        private String parameterDataType;
        private String parameterLowerLimit;
        private String parameterUpperLimit;
        private String parameterTargetValue;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/3 11:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class ReticleEventData {
        private long seq_No;
        private String reticleID;
        private String reticleGroupID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/3 11:15
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class DCSpecItemEventData {
        private String dcItemName;
        private boolean screenUpperRequired;
        private double screenUpperLimit;
        private String screenUpperActions;
        private boolean screenLowerRequired;
        private double screenLowerLimit;
        private String screenLowerActions;
        private boolean specUpperRequired;
        private double specUpperLimit;
        private String specUpperActions;
        private boolean specLowerRequired;
        private double specLowerLimit;
        private String specLowerActions;
        private boolean controlUpperRequired;
        private double controlUpeerLimit;
        private String controlUpperActions;
        private boolean controlLowerRequired;
        private double controlLowerLimit;
        private String controlLowerActions;
        private double dcItemTargetValue;
        private String dcItemTag;
        private String dcSpecGroup;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/3 11:17
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     * @see com.fa.cim.newcore.dto.event.Event.CorrespondingOperationEventData
     */
    @Deprecated
    @Data
    public static class PosCorrespondingOperationEventData {
        private String correspondingOperationNumber;
        private String dcSpecGroup;
    }

    @Data
    public static class BinDefinitionInfo {
        private String identifier;
        private String binDefinitionId;
        private String description;
        private List<BinData> bins;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class BinData {
        private String binNumber;
        private String binDesc;
        private ObjectIdentifier binSpec;
        private ObjectIdentifier product;
    }

    @Data
    public static class BinSpec {
        private String binNumber;
        private String description;
        private ObjectIdentifier binSpecification;
        private ObjectIdentifier productSpecification;
    }

    @Data
    public static class BinSpecificationInfo {
        private String identifier;
        private String binSpecId;
        private String description;
        private String passCriteriaCode;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class TestSpecificationInfo {
        private String identifier;
        private String testSpecId;
        private String description;
        private ObjectIdentifier targetTestType;
        private Double lotYieldSpec;
        private Double waferYieldSpec;
        private Long lowYieldWaferCountSpec;
        private String combinationCriteriaCode;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class TestTypeInfo {
        private String identifier;
        private String testTypeId;
        private String description;
        private String productType;
        private List<UserDataSet> userDataSets;
    }

    @Deprecated
    @Data
    public static class NewWaferEventData {
        private String waferID;
        private boolean controlWaferFlag;
        private long slotNumber;
        private String originalWaferID;
    }

    @Deprecated
    @Data
    public static class SourceLotEventData {
        private LotEventData sourceLotData;
        private List<WaferEventData> sourceWafers;
        private List<WaferEventData> currentWafers;
    }

    @Deprecated
    @Data
    public static class LotWaferMoveEventRecord extends EventRecord {
        private LotEventData destinationLotData;
        private List<NewWaferEventData> currentWafers;
        private List<SourceLotEventData> sourceLots;
        private List<NewWaferEventData> sourceWafers;
    }

    @Data
    @Deprecated
    public static class LotBankMoveEventRecord extends EventRecord {
        private LotEventData lotData;
        private String previousBankID;
    }

    @Deprecated
    @Data
    public static class CollectedDataEventRecord extends EventRecord {
        private LotEventData measuredLotData;
        private String monitorGroupID;
        private List<ProcessedLotEventData> processedLots;
        private String equipmentID;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
    }

    @Deprecated
    @Data
    public static class ChamberStatusChangeEventRecord extends EventRecord {
        private String equipmentID;
        private String processResourceID;
        private String processResourceState;
        private String startTimeStamp;
        private String newProcessResourceState;
        private String actualProcessResourceE10State;
        private String actualProcessResourceState;
        private String newActualProcessResourceE10State;
        private String newProcessResourceE10State;
        private String processResourceE10State;
        private String newActualProcessResourceState;
    }

    @Deprecated
    @Data
    public static class SystemMessageEventRecord extends EventRecord {
        private String subSystemID;
        private String systemMessageCode;
        private String systemMessageText;
        private boolean notifyFlag;
        private String equipmentID;
        private String equipmentState;
        private String stockerID;
        private String stockerState;
        private String AGVID;
        private String AGVState;
        private String lotID;
        private String lotState;
        private String routeID;
        private String operationNumber;
        private String operationID;
    }

    @Deprecated
    @Data
    public static class DurableEventRecord extends EventRecord {
        private String action;
        private String durableType;
        private String durableID;
        private String description;
        private String categoryID;
        private String instanceName;
        private Boolean usageCheckRequiredFlag;
        private Double durationLimit;
        private Long timeUsedLimit;
        private Long intervalBetweenPM;
        private String contents;
        private Long contentsSize;
        private Long capacity;
    }

    @Deprecated
    @Data
    public static class DurableChangeEventRecord extends EventRecord {
        private String durableID;
        private String durableType;
        private String action;
        private String durableStatus;
        private String durableSubStatus;
        private String xferStatus;
        private String xferStatChgTimeStamp;
        private String location;
    }

    @Deprecated
    @Data
    public static class EquipmentModeChangeEventRecord extends EventRecord {
        private String equipmentID;
        private String portID;
        private String operationMode;
        private String onlineMode;
        private String dispatchMode;
        private String accessMode;
        private String operationStartMode;
        private String operationCompMode;
        private String description;
    }

    @Deprecated
    @Data
    public static class EquipmentStatusChangeEventRecord extends EventRecord {
        private String equipmentID;
        private String stockerID;
        private String equipmentState;
        private String operationMode;
        private String startTimeStamp;
        private String newEquipmentState;
        private String newOperationMode;
        private String E10State;
        private String actualE10State;
        private String actualEquipmentState;
        private String newActualE10State;
        private String newActualEquipmentState;
        private String newE10State;
    }

    @Deprecated
    @Data
    public static class EqpMonitorJobEventRecord extends EventRecord {
        private String opeCategory;
        private String equipmentID;
        private String chamberID;
        private String eqpMonitorID;
        private String eqpMonitorJobID;
        private String monitorJobStatus;
        private String prevMonitorJobStatus;
        private Long retryCount;
        private List<Infos.EqpMonitorLotEventData> monitorLots;
    }

    @Deprecated
    @Data
    public static class EqpMonitorEventRecord extends EventRecord {
        private String opeCategory;
        private String equipmentID;
        private String chamberID;
        private String eqpMonitorID;
        private String monitorType;
        private String monitorStatus;
        private String prevMonitorStatus;
        private List<Infos.EqpMonitorDefEventData> monitorDefs;
        private List<Infos.EqpMonitorDefprodEventData> monitorDefprods;
        private List<Infos.EqpMonitorDefactionEventData> monitorDefactions;
        private List<Infos.EqpMonitorSchchgEventData> monitorSchchgs;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/5 10:36
     */
    @Data
    public static class DurableReworkEventMakeIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private Inputs.OldCurrentPOData strOldCurrentPOData;
        private ObjectIdentifier reasonCodeID;
        private String transactionID;
        private String claimMemo;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/5 11:30
     */
    @Data
    public static class DurablePFXCreateEventMakeIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private ObjectIdentifier routeID;
        private String claimMemo;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/5 13:11
     */
    @Data
    public static class DurablePFXDeleteEventMakeIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private String claimMemo;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/5 10:28
     */
    @Data
    public static class DurableOperationStartEventMakeOpeStartCancelIn {
        private ObjectIdentifier equipmentID;
        private String operationMode;
        private ObjectIdentifier durableControlJobID;
        private String durableCategory;
        private List<StartDurable> strStartDurables;
        private String claimMemo;
        private Object siInfo;
    }

    @Deprecated
    @Data
    public static class DurableOperationStartEventRecord extends EventRecord {
        private DurableEventData durableData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private String durableControlJobID;
        private boolean locateBackFlag;
    }

    @Deprecated
    @Data
    public static class DurableOperationCompleteEventRecord extends EventRecord {
        private DurableEventData durableData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private String durableControlJobID;
        private boolean locateBackFlag;
    }

    @Deprecated
    @Data
    public static class DurablePFXEventRecord extends EventRecord {
        private DurableEventData durableData;
    }

    @Deprecated
    @Data
    public static class DurableControlJobStatusChangeEventRecord extends EventRecord {
        private String durableCtrlJobID;
        private String durableCtrlJobState;
        private String eqpID;
        private String eqpDescription;
        private List<DurableControlJobStatusChangeDurableEventData> durables;
    }

    @Deprecated
    @Data
    public static class DurableBankMoveEventRecord extends EventRecord {
        private DurableEventData durableData;
        private String previousBankID;
    }

    @Deprecated
    @Data
    public static class DurableHoldEventRecord extends EventRecord {
        private DurableEventData durableData;
        private List<DurableHoldEventData> holdRecords;
        private ObjectIdentifier releaseReasonCodeID;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
    }

    @Deprecated
    @Data
    public static class DurableReworkEventRecord extends EventRecord {
        private DurableEventData durableData;
        private ObjectIdentifier reasonCodeID;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private Long reworkCount;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private DurableProcessOperationEventData oldCurrentDurablePOData;
    }

    @Deprecated
    @Data
    public static class DurableOperationMoveEventRecord extends EventRecord {
        private DurableEventData durableData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private String durableControlJobID;
        boolean locateBackFlag;
        private DurableProcessOperationEventData oldCurrentDurablePOData;
    }

    @Deprecated
    @Data
    public static class DurableXferJobStatusChangeEventRecord extends EventRecord {
        private String durableType;
        private String operationCategory;
        private String carrierID;
        private String jobID;
        private String carrierJobID;
        private String transportType;
        private String zoneType;
        private Long n2purgeFlag;
        private String fromMachineID;
        private String fromPortID;
        private String toStockerGroup;
        private String toMachineID;
        private String toPortID;
        private String expectedStrtTime;
        private String expectedEndTime;
        private String estimateStrtTime;
        private String estimateEndTime;
        private Long mandatoryFlag;
        private String priority;
        private String jobStatus;
        private String carrierJobStatus;
    }

    @Deprecated
    @Data
    public static class WaferSortJobEventRecord extends EventRecord {
        private String equipmentID;
        private String portGroupID;
        private String sorterJobID;
        private String componentJobID;
        private String action;
        private String sorterJobStatus;
        private String componentJobStatus;
        private boolean waferIDReadFlag;
        private List<SortJobComponentEventData> componentJobs;
        private List<SortJobSlotMapEventData> slotMaps;
    }

    @Deprecated
    @Data
    public static class LotWaferStackEventRecord extends EventRecord {
        private LotEventData lotData;
        private String bondingGroupID;
        private String bondingCategory;
        private String relatedLotID;
        private String equipmentID;
        private String controlJobID;
        private List<StackWaferEventData> wafers;
    }

    @Deprecated
    @Data
    public static class APCProcessDispositionEventRecord extends EventRecord {
        private String ctrlJob;
        private String eqpID;
        private String eqpDescription;
        private String APCSystemName;
        private String requestCategory;
        private String storeTime;
        private List<APCProcessDispositionLotEventData> lots;
    }

    @Deprecated
    @Data
    public static class ProcessJobChangeEventRecord extends EventRecord {
        private String ctrlJob;
        private String prcsJob;
        private String opeCategory;
        private String processStart;
        private String currentState;
        private List<ProcessJobChangeWaferEventData> wafers;
        private List<ProcessJobChangeRecipeParameterEventData> recipeParameters;
    }

    @Deprecated
    @Data
    public static class CollectedDataChangeEventRecord extends EventRecord {
        private String lotID;
        private String controlJobID;
        private String dataCollectionDefinitionID;
        private List<ChangedDCData> changedDCDataSeq;
    }

    @Deprecated
    @Data
    public static class InterFabXferEventRecord extends EventRecord {
        private String opeCategory;
        private String lotID;
        private String originalFabID;
        private String originalRouteID;
        private String originalOpeNumber;
        private String destinationFabID;
        private String destinationRouteID;
        private String destinationOpeNumber;
        private LotEventData lotData;
    }

    @Deprecated
    @Data
    public static class ChangedDCData {
        private String dataCollectionItemName;
        private String previousDataValue;
        private String currentDataValue;
    }

    @Deprecated
    @Data
    public static class ProcessJobChangeWaferEventData {
        private String waferID;
        private String lotID;
    }

    @Deprecated
    @Data
    public static class ProcessJobChangeRecipeParameterEventData {
        private String parameterName;
        private String previousParameterValue;
        private String parameterValue;
    }

    @Data
    public static class ProcessJobChangeRecipeParameter {
        private String parameterName;
        private String preParameterValue;
        private String parameterValue;
        private Object siInfo;
    }

    @Deprecated
    @Data
    public static class APCProcessDispositionLotEventData {
        private String lotID;
        private String castID;
        private String lotType;
        private String subLotType;
        private String prodSpecID;
        private String mainPDID;
        private String opeNo;
        private String pdID;
        private Long opePassCount;
        private String pdName;
    }

    @Deprecated
    @Data
    public static class StackWaferEventData {
        private String waferID;
        private String originalWaferID;
        private String aliasWaferName;
        private String originalAliasWaferName;
        private boolean controlWaferFlag;
        private String relatedWaferID;
        private String originalCassetteID;
        private Long originalSlotNumber;
        private String destinationCassetteID;
        private Long destinationSlotNumber;
    }

    @Deprecated
    @Data
    public static class SortJobComponentEventData {
        private String sorterJobID;
        private String componentJobID;
        private Long jobSeq;
        private String sourceCassetteID;
        private String destinationCassetteID;
        private String sourcePortID;
        private String destinationPortID;
    }

    @Deprecated
    @Data
    public static class SortJobSlotMapEventData {
        private String sorterJobID;
        private String componentJobID;
        private String lotID;
        private String waferID;
        private String destinationCassetteID;
        private String destinationPortID;
        private boolean destinationManagedByMyCim;
        private Long destinationSlotPosition;
        private String sourceCassetteID;
        private String sourcePortID;
        private boolean sourceManagedByMyCim;
        private Long sourceSlotPosition;
    }

    @Deprecated
    @Data
    public static class DurableProcessOperationEventData {
        private String routeID;
        private String operationNumber;
        private String operationID;
        private Long operationPassCount;
        private String objrefPOS;
        private String objrefPO;
        private String objrefMainPF;
        private String objrefModulePOS;
    }


    @Deprecated
    @Data
    public static class DurableHoldEventData {
        private Boolean movementFlag;
        private Boolean changeStateFlag;
        private String holdType;
        private ObjectIdentifier holdReasonCodeID;
        private String holdUserID;
        private String holdTimeStamp;
        private Boolean responsibleOperationFlag;
        private String holdClaimMemo;
        private Boolean responsibleOperationExistFlag;
        private ObjectIdentifier responsibleRouteID;
        private ObjectIdentifier responsibleOperationID;
        private String responsibleOperationNumber;
        private String responsibleOperationName;
    }

    @Deprecated
    @Data
    public static class DurableControlJobStatusChangeDurableEventData {
        private String durableID;
        private String durableCategory;
        private String prodSpecID;
        private String mainPDID;
        private String opeNo;
        private String pdID;
        private Long opePassCount;
        private String pdName;
    }

    @Data
    public static class DurableEventData {
        private String durableID;
        private String durableCategory;
        private String durableStatus;
        private String holdState;
        private String bankID;
        private String routeID;
        private String operationNumber;
        private String operationID;
        private Long operationPassCount;
        private String objrefPOS;
        private String objrefPO;
        private String objrefMainPF;
        private String objrefModulePOS;
    }

    @Deprecated
    @Data
    public static class EqpMonitorLotEventData {
        private String lotID;
        private String productSpecificationID;
        private Long startSeqNo;
        private String mainPDID;
        private String opeNo;
        private String pdID;
        private Long opePassCount;
    }

    @Deprecated
    @Data
    public static class EqpMonitorSchchgEventData {
        private String prevNextExecutionTime;
        private String nextExecutionTime;
    }

    @Deprecated
    @Data
    public static class EqpMonitorDefactionEventData {
        private String eventType;
        private String action;
        private String reasonCode;
        private String sysMessageID;
        private String customField;
    }

    @Deprecated
    @Data
    public static class EqpMonitorDefprodEventData {
        private String productSpecificationID;
        private Long waferCount;
        private Long startSeqNo;
    }

    @Deprecated
    @Data
    public static class EqpMonitorDefEventData {
        private String description;
        private String scheduleType;
        private String startTimeStamp;
        private Long executionInterval;
        private Long warningInterval;
        private Long expirationInterval;
        private boolean standAloneFlag;
        private boolean kitFlag;
        private Long maxRetryCount;
        private String machineStateAtStart;
        private String machineStateAtPassed;
        private String machineStateAtFailed;
    }

    @Deprecated
    @Data
    public static class RecipeBodyManageEventRecord extends EventRecord {
        private String equipmentID;
        private String actionCode;
        private String machineRecipeID;
        private String physicalRecipeID;
        private String fileLocation;
        private String fileName;
        private boolean formatFlag;
    }

    @Deprecated
    @Data
    public static class LotReticleSetChangeEventRecord extends EventRecord {
        private LotEventData lotData;
        private String reticleSetID;
    }

    @Data
    @Deprecated
    public static class LotWaferSortEventRecord extends EventRecord {
        private String lotID;
        private List<WaferEventData> currentWafers;
        private List<WaferEventData> sourceWafers;
        private String equipmentID;
        private String sorterJobID;
        private String componentJobID;
    }

    @Deprecated
    @Data
    public static class EquipmentFlowBatchMaxCountChangeEventRecord extends EventRecord {
        private String equipmentID;
        private Long newFlowBatchMaxCount;
    }

    @Deprecated
    @Data
    public static class ProcessedLotEventData {
        private String processLotID;
        private String processRouteID;
        private String processOperationNumber;
        private Long processOperationPassCount;
        private String processObjrefPO;
    }

    /**
     * description:pptCollectedDataItem_struct
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 4/11/2019 2:11 PM
     */
    @Data
    public static class CollectedDataItemStruct {
        private ObjectIdentifier lotID;
        private ObjectIdentifier dataCollectionDefinitionID;
        private String dataCollectionItemName;
        private String dataCollectionMode;
        private String dataType;
        private String itemType;
        private String measurementType;
        private String sitePosition;
        private List<CollectedDataStruct> collectedDataList;
    }

    /**
     * description:pptCollectedData_struct, be used in pptCollectedDataItem_struct;
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @author Sun
     * @date 4/11/2019 2:08 PM
     */
    @Data
    public static class CollectedDataStruct {
        private ObjectIdentifier waferID;
        private String waferPosition;
        private String processJobID;
        private String processJobPosition;
        private String dataValue;
    }

    @Deprecated
    @Data
    public static class LotOperationMoveEventRecord extends EventRecord {
        private Infos.LotEventData lotData;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<String> reticleIDs;
        private List<String> fixtureIDs;
        private List<Infos.RecipeParmEventData> recipeParameters;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Integer previousOperationPassCount;
        private String previousObjrefPOS;
        private String batchID;
        private String controlJobID;
        private Boolean locateBackFlag;
        private Boolean testCriteriaResult;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private Infos.ProcessOperationEventData oldCurrentPOData;
        private List<Infos.WaferPassCountEventData> processWafers;
    }

    @Deprecated
    @Data
    public static class ProcessResourceWaferPositionEventRecord extends EventRecord {
        private String lotID;
        private String waferID;
        private String controlJobID;
        private String mainPDID;
        private String opeNo;
        private Long opePassCount;
        private String equipmentID;
        private String processResourceID;
        private String waferPosition;
        private String processTime;
    }

    @Deprecated
    @Data
    public static class ProcessOperationEventData {
        private String routeID;
        private String operationNumber;
        private String operationID;
        private Integer operationPassCount;
        private String objrefPOS;
        private String objrefPO;
        private String objrefMainPF;
        private String objrefModulePOS;
        private List<String> samplingWafers;
    }

    @Deprecated
    @Data
    public static class WaferPassCountEventData {
        private String waferID;
        private Integer previousPassCount;
        private Integer passCount;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/10                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/10 18:15
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class LotHoldEventRecord extends EventRecord {
        private LotEventData lotData;
        private List<Infos.LotHoldEventData> holdRecords;
        private ObjectIdentifier releaseReasonCodeId;
        private String previousRouteId;
        private String previousOperationNumber;
        private String previousOperationId;
        private long previousOperationPassCount;
        private String previousObjrefPOS;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;

    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/11                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/11 10:34
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class LotHoldEventData {
        private boolean movementFlag;
        private boolean changeStateFlag;
        private String holdType;
        private ObjectIdentifier holdReasonCodeId;
        private String holdUserId;
        private String holdTimeStamp;
        private boolean responsibleOperationFlag;
        private String holdClaimMemo;
        private boolean responsibleOperationExistFlag;

    }

    @Deprecated
    @Data
    public static class LotWaferScrapEventRecord extends EventRecord {
        private LotEventData lotData;
        private List<Infos.WaferEventData> currentWafers;
        private List<Infos.WaferScrapEventData> scrapWafers;
        private String reasonRouteID;
        private String reasonOperationNumber;
        private String reasonOperationID;
        private Long reasonOperationPassCount;
    }

    @Deprecated
    @Data
    public static class WaferEventData {
        private String waferID;
        private String originalWaferID;
        private Boolean controlWaferFlag;
        private String originalCassetteID;
        private Long originalSlotNumber;
        private String destinationCassetteID;
        private Long destinationSlotNumber;
    }

    @Deprecated
    @Data
    public static class WaferScrapEventData {
        private String waferID;
        private ObjectIdentifier reasonCodeID;
        private Boolean controlWaferFlag;
    }

    @Deprecated
    @Data
    public static class PortStatusChangeEventRecord extends EventRecord {
        private String equipmentID;
        private String portID;
        private String portState;
        private String lotID;
        private String cassetteID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/1 14:17
     */
    @Data
    public static class DurableControlJobStatusChangeEventMake {
        private String transactionID;
        private ObjectIdentifier durableControlJobID;
        private String durableControlJobStatus;
        private String durableCategory;
        private List<StartDurable> strStartDurables;
        private String claimMemo;
        private Object siInfo;
    }

    @Deprecated
    @Data
    public static class ProcessStatusEventRecord extends EventRecord {
        private LotEventData lotData;
        private String actionCode;
        private String equipmentID;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private String batchID;
        private String controlJobID;
    }

    @Deprecated
    @Data
    public static class LotReworkEventRecord extends EventRecord {
        private LotEventData lotData;
        private ObjectIdentifier reasonCodeID;
        private String previousRouteID;
        private String previousOperationNumber;
        private String previousOperationID;
        private Long previousOperationPassCount;
        private String previousObjrefPOS;
        private Long reworkCount;
        private String previousObjrefMainPF;
        private String previousObjrefModulePOS;
        private Infos.ProcessOperationEventData oldCurrentPOData;
        private List<Infos.WaferReworkCountEventData> reworkWafers;
        private List<Infos.WaferPassCountEventData> processWafers;
    }

    @Deprecated
    @Data
    public static class LotFutureHoldEventRecord extends EventRecord {
        private String lotID;
        private String entryType;
        private String holdType;
        private String registerReasonCode;
        private String registerPerson;
        private String routeID;
        private String opeNo;
        private boolean singleTriggerFlag;
        private boolean postFlag;
        private String relatedLotID;
        private String releaseReasonCode;
    }

    @Deprecated
    @Data
    public static class OwnerChangeEventRecord extends EventRecord {
        private String fromOwnerID;
        private String toOwnerID;
        private List<OwnerChangeObjectEventData> changeObjects;
    }

    @Deprecated
    @Data
    public static class OwnerChangeObjectEventData {
        private String objectName;
        private String hashedInfo;
    }

    @Deprecated
    @Data
    public static class WaferReworkCountEventData {
        private String waferID;
        private Long reworkCount;
    }

    /**
     * @see com.fa.cim.newcore.dto.restriction.Constrain.WholeEntityIdentifier
     */
    @Deprecated
    @Data
    public static class WholeEntityIdentifier {
        List<EntityIdentifier> currentEntities;
        List<EntityIdentifier> targetEntities;
    }

    @Data
    public static class ReservedReticle {
        private ObjectIdentifier reticleID;
        private Long slotNumber;
    }

    @Data
    public static class BrFixtureInfo {
        private String identifier;
        private String fixtureId;
        private String description;
        private boolean usageCheckRequiredFlag;
        private long maximumUsageCount;
        private long maximumUsageDuration;
        private String partNumber;
        private String serialNumber;
        private String supplierName;
        private ObjectIdentifier fixtureGroup;
        private long intervalBetweenPM;
        private boolean touchCountControlRequiredFlag;
        private long maintTouchCount;
        private long maxTouchCount;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class BrReticleInfo {
        private String identifier;
        private String reticleId;
        private String description;
        private String instanceName;
        private boolean usageCheckRequiredFlag;
        private long maximumUsageCount;
        private long maximumUsageDuration;
        private String partNumber;
        private String serialNumber;
        private String supplierName;
        private ObjectIdentifier reticleGroup;
        private long intervalBetweenPM;
        private ObjectIdentifier fpcCategory;
        private boolean whiteDefinitionFlag;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class BrLocationInfo {
        private String identifier;
        private String locationId;
        private String description;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class BrCalendarInfo {
        private String identifier;
        private String julianDate;
        private Long shopDate;
        private Long shopWeek;
        private Long shopMonth;
        private String day;
        private Boolean nonWorkingDayFlag;
        private List<ShiftData> shifts;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class ShiftData {
        private String startTime;
        private String endTime;
    }

    @Data
    public static class StageInfo {
        private String identifier;
        private String stageId;
        private String stageNumber;
        private String description;
        private ObjectIdentifier stageGroup;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class StageGroupInfo {
        private String identifier;
        private String stageGroupId;
        private String description;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class BrReticleGroupInfo {
        private String identifier;
        private String reticleGroupId;
        private String description;
        private String subCategory;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class DurableHoldList {
        private String holdType;                   //<i>Hold Type
        private ObjectIdentifier holdReasonCodeID;           //<i>Hold Reason Code ID
        private ObjectIdentifier holdUserID;                 //<i>Hold User ID
        private String responsibleOperationMark;   //<i>Responsible Operation Mark
        //<c>SP_ResponsibleOperation_Current        "C"
        //<c>SP_ResponsibleOperation_Previous       "P"
        private ObjectIdentifier routeID;                    //<i>Route ID
        private String operationNumber;            //<i>Operation Number
        private ObjectIdentifier relatedDurableID;           //<i>Related Durable ID
        private String relatedDurableCategory;     //<i>Related Durable Category
        private String claimMemo;                  //<i>Claim Memo
    }

    @Data
    public static class PostProcessRegistrationParam {
        private ObjectIdentifier equipmentID;           //<i>Equipment ID
        private ObjectIdentifier controlJobID;          //<i>ControlJob ID
        private List<ObjectIdentifier> lotIDs;                //<i>Lot ID sequence
        private List<ObjectIdentifier> cassetteIDs;           //<i>Cassette ID

        public PostProcessRegistrationParam() {

        }

        public PostProcessRegistrationParam(ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<ObjectIdentifier> lotIDs, List<ObjectIdentifier> cassetteIDs) {
            this.equipmentID = equipmentID;
            this.controlJobID = controlJobID;
            this.lotIDs = lotIDs;
            this.cassetteIDs = cassetteIDs;
        }
    }

    @Data
    public static class BrReticleSetInfo {
        private String identifier;
        private String reticleSetId;
        private String description;
        private List<Infos.BrDefaultReticleGroupData> defaultReticleGroups;
        private List<Infos.BrSpecificReticleGroupData> specificReticleGroups;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class BrDefaultReticleGroupData {
        private ObjectIdentifier photoLayer;
        private List<ObjectIdentifier> reticleGroups;
        private Boolean overrideFlag;
    }

    @Data
    public static class BrSpecificReticleGroupData {
        private ObjectIdentifier photoLayer;
        private ObjectIdentifier product;
        private ObjectIdentifier equipment;
        private List<ObjectIdentifier> reticleGroups;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/9                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/9 19:59
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class UploadInventoryReq {
        private ObjectIdentifier machineID;
        private String uploadLevel;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/5 15:09
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ExperimentalLotDetailResultInfo {
        private ObjectIdentifier userID;                         //<i>User ID
        private String action;                         //<i>Action          //2001-12-04
        private ObjectIdentifier lotFamilyID;                    //<i>Lot Family ID                               //D7000015
        private ObjectIdentifier splitRouteID;                   //<i>Split Route ID                              //D7000015
        private String splitOperationNumber;           //<i>Split Operation Number
        private ObjectIdentifier originalRouteID;                //<i>Original Route ID                           //D7000015
        private String originalOperationNumber;        //<i>Original Operation Number                   //D7000015
        private boolean actionEMail;                    //<i>Action EMail Flag    //2001-12-04
        private boolean actionHold;                     //<i>ActionHold Flag      //2001-12-04
        private List<Infos.ExperimentalLotDetail> strExperimentalLotDetailSeq;    //<i>Sequence of Experimental Lot Detail
        //add psmJobID for history
        private String psmJobID;
        //add runCardID for history
        private String runCardID;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/5 15:13
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ExperimentalLotDetail {
        private ObjectIdentifier subRouteID;
        private String returnOperationNumber;                                                           //D7000015
        private String mergeOperationNumber;                                                            //D7000015
        private String actionTimeStamp;
        private String memo;
        private ObjectIdentifier parentLotID;                                                                     //D7000015
        private ObjectIdentifier childLotID;
        private List<Infos.ExperimentalLotWafer> strExperimentalLotWaferSeq;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/5/5                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/5/5 15:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class ExperimentalLotWafer {
        private ObjectIdentifier waferId;
        private String status;
        private Object siInfo;
    }

    @Deprecated
    @Data
    public static class ControlJobStatusChangeEventRecord extends EventRecord {
        private String ctrlJob;
        private String ctrlJobState;
        private String eqpID;
        private String eqpDescription;
        private List<Infos.ControlJobStatusChangeLotEventData> lots;
    }

    @Deprecated
    @Data
    public static class ControlJobStatusChangeLotEventData {
        private String lotID;
        private String castID;
        private String lotType;
        private String subLotType;
        private String prodSpecID;
        private String mainPDID;
        private String opeNo;
        private String pdID;
        private Long opePassCount;
        private String pdName;
    }

    @Deprecated
    @Data
    public static class EntityInhibitEventRecord extends EventRecord {
        private String inhibitID;
        private String description;
        private String startTimeStamp;
        private String endTimeStamp;
        private List<EntityIdentifier> entities;
        private List<String> subLotTypes;
        private String reasonCode;
        private String reasonDesc;
        private String ownerID;
        private List<EntityInhibitReasonDetailInfo> reasonDetailInfos;
        private List<ExceptionLotRecord> exceptionLots;
        private String appliedContext;
    }

    @Deprecated
    @Data
    public static class ProcessHoldEventRecord extends EventRecord {
        private String routeID;
        private String operationNumber;
        private String operationID;
        private String holdType;
        private ObjectIdentifier reasonCodeID;
        private String objrefPOS;
        private String productID;
        private Boolean withExecHoldFlag;
        private String entryType;
    }

    @Data
    public static class CimObjectListInqInParm {
        private String className;              //<i>Class Neme
        //<c>SP_ClassName_PosProductSpecification   "ProductSpec"
        //<c>SP_ClassName_PosProductGroup  "ProductGroup"
        //<c>SP_ClassName_PosTechnology    "Technology
        private ObjectIdentifier objectID;               //<i>Object ID (wildcard is available)
        private List<AdditionalSearchCondition> strAdditionalSearchConditionSeq; //<i>Claim User ID
    }

    @Data
    public static class ObjectIDList {
        private ObjectIdentifier objectID;               //<i>Object ID
        private String description;            //<i>Description

        public ObjectIDList() {
        }

        public ObjectIDList(ObjectIdentifier objectID, String description) {
            this.objectID = objectID;
            this.description = description;
        }
    }

    @Data
    public static class PostProcessConfigInfo {
        private String patternID;          //<i>Pattern ID
        private Integer seqNo;              //<i>Sequence Number
        private String execCondition;      //<i>Exec Condition
        private List<Integer> execConditionSeq;   //<i>Sequence of Exec Condition
        private String targetType;         //<i>Target Type
        private String postProcID;         //<i>Post Process ID
        private Integer syncFlag;           //<i>Sync Flag
        private Integer errorAction;        //<i>Error Action
        private Integer commitPattern;      //<i>Commit Pattern
        private String createTime;         //<i>Create Time
        private String updateTime;         //<i>Update Time
        private String extEventID;         //<i>External Post Processing Event ID
    }

    @Data
    public static class BrFixtureGroupInfo {
        private String identifier;
        private String fixtureGroupId;
        private String description;
        private ObjectIdentifier fixtureCategory;
        private List<UserDataSet> userDataSets;
    }

    @Deprecated
    @Data
    public static class EqpMonitorCountEventRecord extends EventRecord {
        private LotEventData lotData;
        private String equipmentID;
        private String controlJobID;
        private List<EqpMonitorWaferCountEventData> wafers;
    }

    @Deprecated
    @Data
    public static class EqpMonitorWaferCountEventData {
        private String waferID;
        private Long eqpMonitorUsedCount;
    }

    @Deprecated
    @Data
    public static class FutureReworkEventRecord extends EventRecord {
        private String action;
        private String lotID;
        private String routeID;
        private String operationNumber;
        private List<FutureReworkRouteEventData> reworkRoutes;
    }

    @Deprecated
    @Data
    public static class FutureReworkRouteEventData {
        private String trigger;
        private String reworkRouteID;
        private String returnOperationNumber;
        private ObjectIdentifier reasonCodeID;
    }

    @Deprecated
    @Data
    public static class LotChangeEventRecord extends EventRecord {
        private String lotID;
        private String lotStatus;
        private long externalPriority;
        private String lotOwnerID;
        private String lotComment;
        private String orderNumber;
        private String customerID;
        private Long priorityClass;
        private String productID;
        private String previousProductID;
        private String stageID;
        private String planStartTime;
        private String planCompTime;
    }

    @Deprecated
    @Data
    public static class BackupOperationEventRecord extends EventRecord {
        private LotEventData lotData;
        private String request;
        private String hostName;
        private String serverName;
        private String itDaemonPort;
        private String entryRouteID;
        private String entryOperationNumber;
        private String exitRouteID;
        private String exitOperationNumber;
    }

    @Deprecated
    @Data
    public static class BackupChannelEventRecord extends EventRecord {
        private String request;
        private String categoryLevel;
        private String categoryID;
        private String routeID;
        private String operationNumber;
        private String hostName;
        private String serverName;
        private String itDaemonPort;
        private String entryRouteID;
        private String entryOperationNumber;
        private String exitRouteID;
        private String exitOperationNumber;
        private String state;
        private String startTime;
        private String endTime;
    }

    @Deprecated
    @Data
    public static class EquipmentAlarmEventRecord extends EventRecord {
        private String equipmentID;
        private String stockerID;
        private String AGVID;
        private String alarmCategory;
        private String alarmCode;
        private String alarmID;
        private String alarmText;
    }

    @Data
    public static class MessageRequest {
        private String id;
        private String floorEventTimeStamp;
        private String messageID;
        private String lotOwner;
        private String equipmentOwner;
        private String routeOwner;
        private String lotID;
        private String lotStatus;
        private String reasonCode;
        private String userID;
        private String operationNumber;
        private String operationName;
        private String processDefinitionID;
        private String equipmentID;
        private String routeID;
        private String messageText;
    }

    @Data
    public static class BrMessageDistributionInfo {
        private String identifier;
        private String description;
        private String messageDistributionId;
        private String messageMediaId;
        private String messageType;
        private String templateFileName;
        private String messageDistributionTypeId;
        private String primaryMessage;
        private String secondaryMessage;
        private List<ObjectIdentifier> destinationUsers;
        private String subsystem;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Deprecated
    @Data
    public static class LotFlowBatchEventRecord extends EventRecord {
        private LotEventData lotData;
        private String flowBatchID;
        private String eventType;
        private String targetOperationNumber;
        private String targetEquipmentID;
        private String fromFlowBatchID;
    }

    @Data
    public static class CurrentSlotMapInfo {
        private ObjectIdentifier cassetteID;
        private List<WaferSorterSlotMap> strWaferSorterSlotMapSequence;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/17 15:19
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class TransportJobInq {
        private String inquiryType;
        private ObjectIdentifier carrierID;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier fromMachineID;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/17 15:31
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class TransportJobInqData {
        private String jobID;
        private String transportType;
        private String jobStatus;
        private List<Infos.CarrierJobInqInfo> carrierJobInqInfo;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/17                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/17 15:33
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class CarrierJobInqInfo {
        private String carrierJobID;
        private String carrierJobStatus;
        private ObjectIdentifier carrierID;
        private String zoneType;
        private boolean n2PurgeFlag;
        private ObjectIdentifier fromMachineID;
        private ObjectIdentifier fromPortID;
        private ObjectIdentifier toMachineID;
        private ObjectIdentifier toPortID;
        private String expectedStartTime;
        private String expectedEndTime;
        private boolean mandatoryFlag;
        private String priority;
        private String estimatedStartTime;
        private String estimatedEndTime;
        private String toStockerGroup;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 10:03
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class DelCarrierJob {
        private String carrierJobID;               //<i>Carrier Job ID
        private ObjectIdentifier carrierID;                  //<i>Carrier ID
        private Object siInfo;                     //<i>Reserved for SI customization
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 11:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class TranJobCancelReq {
        private String jobID;
        private List<Infos.CarrierJob> carrierJobData;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 11:03
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class CarrierJob {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 11:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class CarrierJobRc {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String carrierReturnCode;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/19                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/6/19 16:46
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class RecoverWafer {
        private String waferID;    //<i>Wafer ID
        private Object siInfo;     //<i>Reserved for SI customization
    }

    @Data
    public static class ReticleSortInfo {
        private ObjectIdentifier reticleID;                  //<i>Reticle ID
        private ObjectIdentifier destinationReticlePodID;    //<i>Destination Reticle Pod ID
        private int destinationSlotNumber;      //<i>Destination Slot Number
        private ObjectIdentifier originalReticlePodID;       //<i>Original Reticle Pod ID
        private int originalSlotNumber;         //<i>Original Slot Number
    }

    @Data
    public static class WhereNextEqpStatus {
        private ObjectIdentifier equipmentID;
        private String equipmentMode;
        private ObjectIdentifier equipmentStatusCode;
        private ObjectIdentifier E10Status;
        private String equipmentStatusDescription;
        private boolean equipmentAvailableFlag;
        private List<Infos.EqpStockerStatus> eqpStockerStatus;
        private List<Infos.EntityInhibitAttributes> entityInhibitions;
        private Object siInfo;
    }


    @Data
    public static class OpeProcedureInfo {
        ObjectIdentifier reportUserID;
        String reportTimeStamp;
        String title;
        String documentType;
        String contents;
    }

    @Data
    public static class EqpMonitorProductLotMap {
        private EqpMonitorProductInfo strEqpMonitorProductInfo;         //<i>Product of Auto Monitor
        private List<ObjectIdentifier> eqpMonitorLotIDs;                //<i>Auto Monitor Lot IDs
    }

    @Data
    public static class PJStatusChangeReqInParm {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private String processJobID;
        private String currentState;
        /**
         * <c>SP_ProcessJobAction_Abort       "ProcessJobAbort"
         * <c>SP_ProcessJobAction_Pause       "ProcessJobPause"
         * <c>SP_ProcessJobAction_Resume      "ProcessJobResume"
         * <c>SP_ProcessJobAction_Start       "ProcessJobStart"
         * <c>SP_ProcessJobAction_Stop        "ProcessJobStop"
         */
        private String actionCode;
    }

    @Data
    public static class ProcessJobMapInfo {
        private String processJobID;
        private List<ObjectIdentifier> waferSeq;
    }

    @Data
    public static class RsvLotCarrier {
        private ObjectIdentifier lotID;             //<i>Lot ID
        private ObjectIdentifier carrierID;         //<i>Carrier ID
        private ObjectIdentifier forEquipmentID;    //<i>For Equipment ID    // Always Blank
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/3                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/3 15:56
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class CarrierXferReq {
        private ObjectIdentifier carrierID;            //<i>Carrier ID
        private ObjectIdentifier lotID;                //<i>Lot ID
        private String zoneType;             //<i>Zone Type
        private Boolean n2PurgeFlag;          //<i>N2 Purge Flag
        private ObjectIdentifier fromMachineID;        //<i>From Machine ID
        private ObjectIdentifier fromPortID;           //<i>From Port ID
        private String toStockerGroup;       //<i>To Stocker Group
        private List<ToMachine> strToMachine;         //<i>Sequence of To Machine
        private String expectedStartTime;    //<i>Expected Start Time
        private String expectedEndTime;      //<i>Expected End Time
        private Boolean mandatoryFlag;        //<i>Mandatory Flag
        private String priority;             //<i>Priority
        private Object siInfo;               //<i>Reserved for SI customization

    }

    @Data
    public static class DurableXferReq {
        private ObjectIdentifier durableID;            //<i>DUrable ID
        private String zoneType;             //<i>Zone Type
        private Boolean n2PurgeFlag;          //<i>N2 Purge Flag
        private ObjectIdentifier fromMachineID;        //<i>From Machine ID
        private ObjectIdentifier fromPortID;           //<i>From Port ID
        private String toStockerGroup;       //<i>To Stocker Group
        private List<ToMachine> strToMachine;         //<i>Sequence of To Machine
        private String expectedStartTime;    //<i>Expected Start Time
        private String expectedEndTime;      //<i>Expected End Time
        private Boolean mandatoryFlag;        //<i>Mandatory Flag
        private String priority;             //<i>Priority
        private Object siInfo;               //<i>Reserved for SI customization

    }

    /**
     * description:
     * <p>
     * The template is used OMS send messages to FAM.
     * </p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @author ZQI
     * @return
     * @date 2019/7/23 10:15:58
     */
    @Data
    public static class OMSToFAMSendMessageTemplate {
        private String lotId;
        private String lotType;
        private String equipmentId;
        private String productId;
        private String productType;
        private String logicalRecipeId;
        private String machineRecipeId;
        private String processId;
        private String routeId;
        private String stepId;
        private String stepNumber;
        private String operationCompleteType;
    }

    @Data
    public static class MultipleBaseResult {
        private String key;                            //<i>key
        private Integer returnCode;                      //<i>Transaction Result
    }

    @Data
    public static class RecipeParameterCheckConditionForAdjustIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<ProcessRecipeParameter> strProcessRecipeParameterSeq;                      //<i>Transaction Result
    }

    @Data
    public static class CommonObjectIdentifierStruct {
        private List<ObjectIdentifier> objectIdentifierSeq;    //<i>Sequence of objectIdentifier
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/24 15:38
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class LotStartRecipeParameter {
        private ObjectIdentifier lotID;                      //<i>Lot ID
        private String recipeParameterChangeType;  //<i>Recipe Parameter Change Type
        private List<LotWafer> strLotWaferSeq;             //<i>Lot wafer information with recipe parameters
        private Object siInfo;
    }

    @Data
    public static class PartialOpeCompAction {
        private List<ObjectIdentifier> waferIDs;
        private String actionCode;
        private Object siInfo;
    }

    @Data
    public static class PartialOpeCompLot {
        private ObjectIdentifier lotID;
        private String lotStatus;
        private List<ObjectIdentifier> waferIDs;
        private String actionCode;
        private String specCheckResult;
        private String spcCheckResult;
        private String actionResult;
        private SpcResult spcResult;
        private Object siInfo;
    }

    @Data
    public static class ControlJobInformation {
        private ObjectIdentifier controlJobID;
        private ObjectIdentifier ownerID;
        private ObjectIdentifier machineID;
        private String portGroup;
        private String estimatedCompletionTime;
        private boolean reservedFlag;
        private String controlJobStatus;
        private String lastClaimedUser;
        private String lastClaimedTimeStamp;
        private List<ControlJobCassetteInfo> controlJobCassetteInfoList;
        private Object siInfo;
    }

    @Data
    public static class ControlJobCassetteInfo {
        private ObjectIdentifier cassetteID;
        private ObjectIdentifier loadPortID;
        private ObjectIdentifier unloadPortID;
        private long loadSequenceNumber;
        private String loadPurposeType;
        private List<ControlJobCassetteLot> controlJobCassetteLotList;
        private Object siInfo;
    }

    @Data
    public static class ControlJobCassetteLot {
        private ObjectIdentifier lotID;
        private boolean operationStartFlag;
        private boolean monitorLotFlag;
        private List<ObjectIdentifier> waferIDs;
        private Object siInfo;
    }

    @Data
    public static class DurableSubState {
        private String identifier;
        private String durableState;
        private String durableSubState;
        private String name;
        private String description;
        private Boolean durableProcessAvailableFlag;
        private List<String> availableSubLotTypes;
        private List<ObjectIdentifier> nextTransitionDurableSubStates;
        private Boolean changeFromOtherDurableStateFlag;
        private Boolean changeToOtherDurableStateFlag;
        private List<UserDataSet> userDataSets;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 11:14
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OwnerChangeDefinition {
        private String className;                             //<i>Class Name
        private List<Infos.OwnerChangeTableDefinition> strOwnerChangeTableDefinitionSeq;      //<i>Sequence of Table Definition
        private Object siInfo;                                //<i>Reserved for SI customization
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 11:15
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OwnerChangeTableDefinition {
        private String tableName;                             //<i>Table Name
        private String objRefColumn;                          //<i>Column Name of Object Reference
        private String parentTable;                           //<i>Parent Table Name
        private List<Infos.OwnerChangeColumnDefinition> strOwnerChangeColumnDefinitionSeq;     //<i>Sequence of Column Definition
        private Object siInfo;                                //<i>Reserved for SI customization
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 11:17
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OwnerChangeColumnDefinition {
        private String columnName;             //<i>Column Name
        private List<UserData> strUserDataSeq;         //<i>User Data Sequence
        private Object siInfo;                 //<i>Reserved for SI customization
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 11:19
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OwnerChangeDefObjDefinition {
        private Long seqNo;                       //<i>Sequence Number
        private Long commitFlag;                  //<i>Commit Flag
        private String tableName;                   //<i>Table Name
        private String columnName;                  //<i>Column Name
        private String columnObjRef;                //<i>Column Name of Object Reference
        private String keys;                        //<i>Primary Keys. For example, "XXXX_ID,SEQ_NO"
        private Object siInfo;                      //<i>Reserved for SI customization
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/5 15:15
     */
    @Data
    public static class EquipmentAlarm {
        private String alarmCode;
        private String alarmID;
        private String alarmCategory;
        private String alarmText;
        private String alarmTimeStamp;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @exception
     * @date 2019/8/5 15:25
     */
    @Data
    public static class EquipmentContainerMaxRsvCountUpdateEventMakeIn {
        private String transactionID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier equipmentContainerID;
        private Long maxRsvCount;
        private String claimMemo;
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 13:22
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OwnerChangeErrorInfo {
        private String objectName;                 //<i>Object Name
        private List<HashedInfo> strHashedInfoSeq;           //<i>Sequence of Hashed Info
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 13:25
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class OwnerChangeObject {
        private String objectName;                 //<i>Object Name
        private String hashedInfo;                 //<i>String of Hashed Information
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 9:53
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class WaferSorterCompareCassette {
        private ObjectIdentifier cassetteID;                              //<i>Carrier ID
        private ObjectIdentifier portID;                                  //<i>Port ID                          //D4100098
        private List<Infos.WaferSorterCompareSlotMap> strWaferSorterCompareSlotMapSequence;    //<i>Sequence of Wafer Sorter Compare SlotMap
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 9:55
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class WaferSorterCompareSlotMap {
        private Long mmDestinationPosition;       //<i>OMS Destination Position
        private ObjectIdentifier mmDestinationLotID;          //<i>OMS Destination Lot ID
        private ObjectIdentifier mmDestinationWaferID;        //<i>OMS Destination Wafer ID
        private String mmDestinationAliasName;        //<i>OMS Destination Wafer ID  由于eap不报wafer,故以T7_code代替
        private Long tcsDestinationPosition;      //<i>TCS Destination Position
        private ObjectIdentifier tcsDestinationLotID;         //<i>TCS Destination Lot ID
        private ObjectIdentifier tcsDestinationWaferID;       //<i>TCS Destination Wafer I
        private String tcsDestinationAliasName;       //<i>TCS Destination Wafer I 由于eap不报wafer,故以T7_code代替
        private String compareStatus;               //<i>Compare Status
        private Object siInfo;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 15:08
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Data
    public static class WaferSorterActionList {
        private ObjectIdentifier equipmentID;         //<i>Equipment ID
        private String actionCode;          //<i>Action Code
        private String physicalRecipeID;    //<i>Physical Recipe ID
        private ObjectIdentifier userID;              //<i>User ID
        private String storeTime;           //<i>Store Time
        private Object siInfo;                                                    //<i>Reserved for SI customization
    }

    /**
     * description:
     * <p></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2019/8/7         ********             Zack               create file
     *
     * @author Zack
     * @date 2019/8/7 11:00
     * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Deprecated
    @Data
    public static class SLMMaxReserveCountEventRecord extends EventRecord {
        private String machineID;
        private String machineContainerID;
        private Long maxReserveCount;
    }

    @Data
    public static class NPWXferCassette {
        private Long loadSequenceNumber;    //<i>Load Sequence Number
        private ObjectIdentifier cassetteID;            //<i>Carrier ID
        private String loadPurposeType;       //<i>Load Purpose Type
        private ObjectIdentifier loadPortID;            //<i>Load Port ID
        private ObjectIdentifier unloadPortID;          //<i>Unload Port ID
        private Object siInfo;                                                    //<i>Reserved for SI customization
    }

    @Data
    public static class ProdReqInq {
        private ObjectIdentifier lotID;
        private String lotType;
        private String productType;
        private ObjectIdentifier productID;
        private ObjectIdentifier productGroupID;
        private String technologyCode;
        private String dueTimeStamp;
        private String priorityClass;
        private String externalPriority;
        private String orderNumber;
        private String customerCode;
        private String releaseTimeStamp;
        private ObjectIdentifier routeID;
        private ObjectIdentifier startBankID;
        private long productCount;
        private String department;
        private String section;
        private String reasonCode;
        private List<ObjectIdentifier> sourceProductID;
        private List<SourceLotsAttributes> strSourceLotsAttributes;
        private Object siInfo;
        private String productUsage;
    }

    @Data
    public static class InterFabMonitorGroups {
        private ObjectIdentifier monitorGroupID;                //<i>Monitor Group ID
        private ObjectIdentifier monitorLotID;                  //<i>Monitor lot ID
        private ObjectIdentifier monitorCassetteID;             //<i>Monitor Carrier ID
        private List<Infos.InterFabMonitoredLots> strInterFabMonitoredLotsSeq;   //<i>Sequence of Monitored Lots
    }


    @Data
    public static class InterFabMonitoredLots {
        private ObjectIdentifier lotID;         //<i>Monitored lot ID
        private ObjectIdentifier cassetteID;    //<i>Carrier ID
        private String poObj;         //<i>PO Obj
    }

    @Data
    public static class Parts {
        private ObjectIdentifier parts;
        private Integer quantity;
        private Integer serialControlFlag;
    }

    @Data
    public static class BomInfo {
        private String identifier;
        private String bomId;
        private String description;
        private List<Parts> parts;
    }

    @Data
    public static class EqpMonitorSection {
        private String operationLabel;
        private String operationNumber;
        private Boolean exitOperationFlag;
        private String customField;
        private String processOperationSpecification;
    }

    @Data
    public static class BrEqpMonitorSection {
        private String operationLabel;
        private String operationNumber;
        private Boolean exitOperationFlag;
        private String customField;
    }

    @Data
    public static class BondingLotAttributes {
        private ObjectIdentifier lotID;                      //<i>Lot ID
        private String lotType;                    //<i>Lot Type
        private String subLotType;                 //<i>Sub Lot Type
        private String lotStatus;                  //<i>Lot Status
        private List<LotStatusList> strLotStatusListListSeq;        //<i>Sequence of Lot Status List
        private ObjectIdentifier productID;                  //<i>Product ID
        private ObjectIdentifier carrierID;                  //<i>Carrier ID
        private int totalWaferCount;            //<i>Total Wafer Count
        private ObjectIdentifier targetRouteID;              //<i>Target Route ID
        private String targetOpeNo;                //<i>Target Operation Number
        private ObjectIdentifier targetOpeID;                //<i>Target Operation ID
        private ObjectIdentifier targetLogicalRecipeID;      //<i>Target Logical Recipe ID
        private ObjectIdentifier targetMachineRecipeID;      //<i>Target Machine Recipe ID
        private String bondingCategory;            //<i>Bonding Category
        private ObjectIdentifier topProductID;               //<i>Top Product ID
        private String bondingFlowSectionName;     //<i>Bonding Flow Section Name
        private String bondingGroupID;             //<i>Bonding Group ID
        private boolean inPostProcessOfCassette;    //<i>InPostProcess Flag of Cassette
        private boolean inPostProcessOfLot;         //<i>InPostProcess Flag of Lot
    }

    @Data
    public static class FoundFixture {
        private ObjectIdentifier fixtureCategoryID;
        private String fixtureCategoryDescription;
        private ObjectIdentifier fixtureGroupID;
        private String fixtureGroupDescription;
        private FixtureStatusInfo fixtureStatusInfo;
    }

    @Data
    public static class FixtureStatusInfo {
        private ObjectIdentifier fixtureID;
        private String description;
        private String fixturePartNumber;
        private String fixtureSerialNumber;
        private String fixtureStatus;
        private String transferStatus;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier stockerID;
        private String xPosition;
        private String yPosition;
        private String zPosition;
        private String lastClaimedTimeStamp;
        private ObjectIdentifier lastClaimedPerson;
    }

    @Data
    public static class BOMPartsInfo {
        private ObjectIdentifier partID;
        private int qty;
        private int serialControlFlag;
    }

    @Data
    public static class BrSampleSpecificationInfo {
        private String identifier;
        private String sampleSpecId;
        private String description;
        private Double aQLLevel;
        private Long samplingLevel;
        private List<UserDataSet> userDataSets;
    }

    @Data
    public static class DurableHoldListAttributes {
        private String holdType;                   //<i>Hold Type
        private ObjectIdentifier reasonCodeID;               //<i>Reason Code ID
        private String codeDescription;            //<i>Code Description
        private ObjectIdentifier userID;                     //<i>User ID
        private String userName;                   //<i>User Name
        private String holdTimeStamp;              //<i>Hold TimeStamp
        private String responsibleOperationMark;   //<i>Responsible Operation Mark
        //<c>SP_ResponsibleOperation_Current        "C"
        //<c>SP_ResponsibleOperation_Previous       "P"
        private ObjectIdentifier responsibleRouteID;         //<i>Responsible Route ID
        private String responsibleOperationNumber; //<i>Responsible Operation Number
        private String responsibleOperationName;   //<i>Responsible Operation Name
        private ObjectIdentifier relatedDurableID;           //<i>Related Durable ID
        private String relatedDurableCategory;     //<i>Related Durable Category
        private String claimMemo;                  //<i>Claim Memo
    }

    @Data
    public static class TranslatedChamberState {
        private ObjectIdentifier chamberID;
        private ObjectIdentifier translatedStatusCode;
        private Boolean stateTranslated;
    }

    @Data
    public static class ProcessOperationInfo {
        private List<AsgnProcessResourceInfo> strAsgnProcessResourceInfoSeq;
        private List<ProcessResourcePositionInfo> strProcessResourcePositionInfoSeq;
        private List<DCDef> strDCDefSeq;
        private List<AssignedRecipeParameter> strAssignedRecipeParameterSeq;
        private List<AssignedReticle> strAssignedReticleSeq;
        private List<AssignedFixture> strAssignedFixtureSeq;
        private List<SamplingWafer> strSamplingWaferSeq;
        private String poName;
        private String poObjRef;
        private String modPOSObjRef;
        private ObjectIdentifier mainPDID;
        private ObjectIdentifier modulePDID;
        private ObjectIdentifier pdID;
        private String opeName;
        private String moduleNo;
        private String moduleOpeNo;
        private String opeNo;
        private String passCount;
        private String actualStartTime;
        private String actualEndTime;
        private ObjectIdentifier asgnEqpID;
        private String asgnPortGrp;
        private ObjectIdentifier asgnLcrecipeID;
        private ObjectIdentifier asgnRecipeID;
        private String asgnRparmChgType;
        private Boolean asgnDcFlag;
        private String pfObjRef;
        private String mainPFObjRef;
        private String modPFObjRef;
        private ObjectIdentifier planEqpID;
        private Long remainCycleTime;
        private String planStartTime;
        private String planEndTime;
        private Long refCnt;
        private ObjectIdentifier ctrlJobID;
        private Boolean yldchkResult;
        private String fabID;
    }

    @Data
    public static class AsgnProcessResourceInfo {
        private String processWafer;
        private List<Infos.ProcessResourceInfo> strProcessResourceInfoSequence;
    }

    @Data
    public static class AssignedRecipeParameter {
        private Long setNumber;
        private ObjectIdentifier recipeID;
        private String physicalRecipeID;
        private List<AssignedRecipeParameterWafer> strAssignedRecipeParameterWaferSeq;
        private List<AssignedRecipeParameterAttribute> strAssignedRecipeParameterAttributeSeq;
    }

    @Data
    public static class AssignedRecipeParameterWafer {
        private ObjectIdentifier waferID;
        private Long slotNumber;
        private Boolean controlWaferFlag;
    }

    @Data
    public static class AssignedRecipeParameterAttribute {
        private String paramterName;
        private String parameterValue;
        private String targetValue;
        private Boolean useFlag;
    }

    @Data
    public static class AssignedReticle {
        private Long seqNo;
        private ObjectIdentifier reticleID;
    }

    @Data
    public static class AssignedFixture {
        private Long seqNo;
        private ObjectIdentifier reticleID;
        private String fixtureCategory;
    }

    @Data
    public static class SamplingWafer {
        private Long seqNo;
        private ObjectIdentifier samplingWaferID;
    }

    @Data
    public static class SchdlChangeReservation {
        private String eventID;                   //<i>Event ID
        private ObjectIdentifier objectID;                  //<i>Object ID
        private String objectType;                //<i>Object Type
        private ObjectIdentifier targetRouteID;             //<i>Target Route ID
        private String targetOperationNumber;     //<i>Target Operation Number
        private ObjectIdentifier productID;                 //<i>Product ID
        private ObjectIdentifier routeID;                   //<i>Route ID
        private String operationNumber;           //<i>Operation Number
        private String subLotType;                //<i>Sub Lot Type
        private String startDate;                 //<i>Start Date
        private String startTime;                 //<i>Start Time
        private String endDate;                   //<i>End Date
        private String endTime;                   //<i>End Time
        private Boolean eraseAfterUsedFlag;        //<i>Erase After Used Flag
        private Long maxLotCnt;                 //<i>Max Lot Count
        private Long applyLotCnt;               //<i>Apply Lot Count
        private String status;                    //<i>Status
        private String actionCode;                //<i>Action Code
        private Boolean lotInfoChangeFlag;         //<i>Created by Lot Information Change Flag
    }

    @Data
    public static class SendEDCDataItemWithTransitDataInqInParm {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier controlJobID;
        private List<CollectedDataItem> collectedDataItemList;
    }

    @Data
    public static class OwnerChangeReqInParm {
        private ObjectIdentifier fromOwnerID;                 //<i>From Owner ID
        private ObjectIdentifier toOwnerID;                   //<i>To Owner ID
        private String targetClassName;             //<i>Target Class Name
        private String targetTableName;             //<i>Target Table Name
        private String targetColumnName;            //<i>Target Column Name
        private List<Infos.HashedInfo> strHashedInfoSeq;            //<i>Sequence of Hashed Information
        private Object siInfo;                      //<i>Reserved for SI customization
    }

    @Data
    public static class ProcessHoldLot {
        private RetCode strResult;
        private ObjectIdentifier lotID;
        private boolean execPostProcessFlag; // eric: 添加此字段，来判断该Lot是否需要执行PostProcess逻辑
    }

    @Data
    public static class StepContentResetByLotReqInParm {
        private long updateLevel;
        private List<WIPLotResetAttribute> strWIPLotResetAttributes;
    }

    @Data
    public static class WIPLotResetAttribute {
        private ObjectIdentifier lotID;                                 //<i>Lot ID
        private ObjectIdentifier routeID;                               //<i>Route ID
        private String operationNumber;                       //<i>Current Operation Number
    }

    @Data
    public static class WIPLotResetResult {
        private ObjectIdentifier lotID;
        private RetCode strResult;

        public WIPLotResetResult() {
        }

        public WIPLotResetResult(RetCode strResult) {
            this.strResult = strResult;
        }
    }

    @Data
    public static class SorterComponentJobList {
        private ObjectIdentifier componentJobID;                //<i>Component Job ID
        private String sorterComponentJobStatus;      //<i>Sorter Component Job Status
        private ObjectIdentifier originalCassetteID;            //<i>Original Cassette ID
        private ObjectIdentifier destinationCassetteID;         //<i>Destination Cassette ID
        private String requestTimeStamp;              //<i>Request Time Stamp
        private List<WaferSorterSlotMap> waferSorterSlotMapList; //<i>Sequence of Wafer Sorter Slot Map Information
    }

    @Data
    public static class InterFabDestinationInfo {
        private String fabID;          //<i>Fab ID
        private String fabName;        //<i>Fab Name
        private ObjectIdentifier stockerID;      //<i>Stocker ID
        private String SPCHostName;    //<i>SPC Host Name
        private String SPCMarkerName;  //<i>SPC Marker Name
        private String SPCServerName;  //<i>SPC Server Name
        private String DBName;         //<i>Database Name
        private String DBUserID;       //<i>Database User ID
        private String DBPassword;     //<i>Database Password
        private String description;    //<i>Description
        private Object siInfo;                         //<i>Reserved for SI customization
    }

    @Data
    public static class SJStartReqInParm {
        private ObjectIdentifier equipmentID;           //<i>Equipment ID
        private ObjectIdentifier portGroupID;           //<i>Port group ID
        private List<Infos.StartCassette> strStartCassette;      //<i>Start Cassette information
        private String transportType;         //<i>Carrier transport type
        private List<Infos.CarrierXferReq> strCarrierXferReq;     //<i>Carrier Xfer Information
        private ObjectIdentifier sorterJobID;           //<i>Sorter JobID
        private ObjectIdentifier componentJobID;        //<i>Component JobID
    }

    @Data
    public static class PostFilterCreateForExtReqInParm {
        private String objectType;
        private ObjectIdentifier objectID;
    }

    @Data
    public static class ExternalPostProcessFilterInfo {
        private String objectType;         //<i>Object Type of filter (ProductSpec/ProductGroup/Technology/Lot)
        //<c>SP_PostProcess_ObjectType_ProductSpec   "ProductSpec"
        //<c>SP_PostProcess_ObjectType_ProductGroup  "ProductGroup"
        //<c>SP_PostProcess_ObjectType_Technology    "Technology
        //<c>SP_PostProcess_ObjectType_Lot           "Lot
        private ObjectIdentifier objectID;           //<i>ObjectID
        private ObjectIdentifier claimUserID;        //<i>Claim UserID
        private String claimTime;          //<i>Claim TimeStamp
    }

    @Data
    public static class PostFilterListForExtInqInParm {
        private String objectType;         //<i>Object Type of filter (ProductSpec/ProductGroup/Technology/Lot)
        //<c>SP_PostProcess_ObjectType_ProductSpec   "ProductSpec"
        //<c>SP_PostProcess_ObjectType_ProductGroup  "ProductGroup"
        //<c>SP_PostProcess_ObjectType_Technology    "Technology
        //<c>SP_PostProcess_ObjectType_Lot           "Lot
        private ObjectIdentifier objectID;           //<i>Object ID (wildcard is available)
        private ObjectIdentifier userID;             //<i>Claim User ID
    }

    @Data
    public static class DurableDeleteInfo {
        private String className;
        private List<ObjectIdentifier> durableIDList;
    }

    @Data
    public static class PriorityInfoResult {
        private String carrierJobID;
        private ObjectIdentifier carrierID;
        private String expectedStartTime;
        private String expectedEndTime;
        private Boolean mandatoryFlag;
        private String priority;
    }

    @Data
    public static class BondingGroupUpdateResult {
        private String bondingGroupID;
        private Infos.retMessage retMessage;
    }

    @Data
    public static class MtrlOutSpec {
        private List<SlmSlotMap> sourceMapList;
        private List<SlmSlotMap> destinationMapList;
    }

    @Data
    public static class BondingGroupReleaseLotWafer {
        private String bondingGroupID;             //<i>Bonding Group ID
        private ObjectIdentifier parentLotID;                //<i>Parent Lot ID
        private List<ObjectIdentifier> childWaferIDSeq;            //<i>Sequence of Child Wafer ID
    }

    @Data
    public static class StackedWaferInfo {
        private ObjectIdentifier cassetteID;                 //<i>Cassette ID
        private Long slotNo;                     //<i>Slot Number
        private ObjectIdentifier baseLotID;                  //<i>Base Lot ID
        private ObjectIdentifier baseWaferID;                //<i>Base Wafer ID
        private String baseAliasWaferName;         //<i>Base Alias Wafer Name
        private String basePreviousAliasWaferName; //<i>Base Previous Alias Wafer Name
        private String state;                      //<i>State
        private ObjectIdentifier topLotID;                   //<i>Top Lot ID
        private ObjectIdentifier topWaferID;                 //<i>Top Wafer ID
        private String topAliasWaferName;          //<i>Top Alias Wafer Name
        private String stackedTime;                //<i>Stacking Time
        private Long materialOffset;             //<i>Material Offset
        private String fabID;                      //<i>Fab ID
    }

    @Data
    public static class BOMPartsDefInProcess {
        private ObjectIdentifier routeID;                    //<i>Route D
        private String operationNumber;            //<i>Operation Number
        private ObjectIdentifier operationID;                //<i>Operation ID
        private List<BOMPartsInfo> strBOMPartsInfoSeq;         //<i>Sequence of BOM Parts Information
    }

    @Data
    public static class LotInBondingFlowInfo {
        private ObjectIdentifier lotID;                      //<i>Lot ID
        private String lotType;                    //<i>Lot Type
        private String subLotType;                 //<i>Sub Lot Type
        private String lotStatus;                  //<i>Lot Status
        private List<LotStatusList> strLotStatusListSeq;        //<i>Sequence of Lot Status List
        private ObjectIdentifier productID;                  //<i>Product ID
        private ObjectIdentifier carrierID;                  //<i>Carrier ID
        private long totalWaferCount;            //<i>Total Wafer Count
        private String lastClaimedTimeStamp;       //<i>Last Claimed Time Stamp
        private String stateChangeTimeStamp;       //<i>Status Change Time Stamp
        private String inventoryChangeTimeStamp;   //<i>Inventory Change Time Stamp
        private String dueTimeStamp;               //<i>Due Time Stamp
        private String planStartTimeStamp;         //<i>Plan Start Time Stamp
        private String planEndTimeStamp;           //<i>Plan End Time Stamp
        private ObjectIdentifier plannedEquipmentID;         //<i>Planned Equipment ID
        private String queuedTimeStamp;            //<i>Queued Time Stamp
        private String priorityClass;              //<i>Priority Class
        private String internalPriority;           //<i>Internal Priority
        private String externalPriority;           //<i>External Priority
        private boolean qtimeFlag;                  //<i>Qtime Flag
        private List<LotQtimeInfo> strLotQtimeInfo;            //<i>Sequence of Lot Q Time Info
        private boolean minQTimeFlag;                                   //<i>Min Q-Time Flag
        private List<LotQtimeInfo> minQTimeInfos;                       //<i>Sequence of lot Min Q-Time Info
        private String preOperationCompTimeStamp;  //<i>Pre Operation Comp Time Stamp
        private ObjectIdentifier currentRouteID;             //<i>Current Route ID
        private String currentOpeNo;               //<i>Current Operation Number
        private ObjectIdentifier currentOpeID;               //<i>Current Operation ID
        private ObjectIdentifier targetRouteID;              //<i>Target Route ID
        private String targetOpeNo;                //<i>Target Operation Number
        private ObjectIdentifier targetOpeID;                //<i>Target Operation ID
        private ObjectIdentifier targetLogicalRecipeID;      //<i>Target Logical Recipe ID
        private String bondingCategory;            //<i>Bonding Category
        private ObjectIdentifier topProductID;               //<i>Top Product ID
        private String bondingFlowSectionName;     //<i>Bonding Flow Section Name

    }

    @Data
    public static class ProcessOperationInformation {
        private ObjectIdentifier routeID;                    //<i>Route ID
        private ObjectIdentifier operationID;                //<i>Operation ID
        private String operationNumber;            //<i>Operation Number
        private String operationName;              //<i>Operation Name
        private boolean mandatoryOperationFlag;     //<i>Mandatory Operation Flag. If yes, True.
        private ObjectIdentifier stageID;                    //<i>Stage ID of the lot that is defined by SM.
        private String maskLevel;                  //<i>Mask Level
        private String planStartTimeStamp;         //<i>Plan Start Time Stamp
        private String department;                 //<i>Department
        private String planEndTimeStamp;           //<i>Plan End Time Stamp
        private ObjectIdentifier plannedEquipmentID;         //<i>Planned Equipment ID
        private ObjectIdentifier testSpecID;                 //<i>Test Spec ID
        private String inspectionType;             //<i>Inspection Type
        private String reworkCount;                //<i>Rework Count
        private String queuedTimeStamp;            //<i>Queued Time Stamp
    }

    @Data
    public static class APCSystemFunction {
        private String functionType;
        private String controlFrequency;
        private String functionDescription;
    }

    @Data
    public static class APCRecipeParameterResponse {
        private String systemName;
        private List<ApcBaseCassette> strAPCBaseCassette;
        private APCBaseReturnCode strAPCBaseReturnCode;
    }


    @Data
    public static class BondingGroupReleasedLot {
        private ObjectIdentifier parentLotId;
        private ObjectIdentifier releasedLotId;
    }

    @Data
    public static class EqpChamberAvailableInfo {
        ObjectIdentifier chamberID;                              //<i>Chamber ID
        ObjectIdentifier chamberStateCode;                       //<i>Status of the Chamber
        boolean availableFlag;                          //<i>Whether the Chamber is Available State
        boolean conditionalAvailableFlag;
        boolean conditionalAvailableFlagForChamber;
    }

    @Data
    public static class MachineContainerPositionInfo {
        private ObjectIdentifier machineContainerPosition;
        private String position;
        private ObjectIdentifier wafer;
        private ObjectIdentifier machine;
        private ObjectIdentifier machineContainer;
        private ObjectIdentifier controlJob;
        private String processJob;
        private ObjectIdentifier srcCassette;
        private ObjectIdentifier srcPort;
        private long srcSlotNo;
        private String SLMState;
        private ObjectIdentifier destCassette;
        private ObjectIdentifier destPort;
        private long destSlotNo;
        private String processStartTimeStamp;
        private String processCompTimeStamp;
        private String estimatedEndTimeStamp;
        private String lastClaimedTimeStamp;
        private ObjectIdentifier lastClaimedUser;
    }

    @Data
    public static class EntityValue {
        private ObjectIdentifier identifier;
        private String description;
    }

    @Data
    public static class DispatchResult {
        private int numberRows;              //<i>Number of Rows
        private int numberCols;              //<i>Number of Cols
        private String systemErrorCode;         //<i>System Error Code
        private String systemErrorMessage;      //<i>System Error Message
        private String userErrorCode;           //<i>User Error Code
        private String userErrorMessage;        //<i>User Error Message
        private String dispatchErrorMessage;    //<i>Dispatch Error Message
        private List<String> dispatchRows;            //<i>Sequence of Dispatch
        private List<String> columnHeaders;           //<i>Sequence of Column Headers
        private List<Integer> columnWidths;            //<i>Sequence of Column Width
        private List<String> columnTypes;             //<i>Sequence of Column Type
    }

    @Data
    public static class AdjustedRecipeParamResultByLot {
        private String lotID;
        private String errorCode;
        private String errorMessage;
    }

    @Data
    public static class LotEqpMonitorWaferUsedCount {
        private ObjectIdentifier lotID;
        private List<EqpMonitorWaferUsedCount> errorCode;
    }

    @Data
    public static class ShelfPosition {
        private Integer shelfPositionX;
        private Integer shelfPositionY;
        private Integer shelfPositionZ;

        public ShelfPosition() {

        }

        public ShelfPosition(Integer shelfPositionX, Integer shelfPositionY, Integer shelfPositionZ) {
            this.shelfPositionX = shelfPositionX;
            this.shelfPositionY = shelfPositionY;
            this.shelfPositionZ = shelfPositionZ;
        }

        @Override
        public String toString() {
            return shelfPositionX + "-" + shelfPositionY + "-" + shelfPositionZ;
        }
    }

    @Data
    public static class DurablePostProcessRegistrationParm {
        private ObjectIdentifier equipmentID;                //<i>Equipment ID
        private ObjectIdentifier durableControlJobID;        //<i>Durable Control Job ID
        private String durableCategory;            //<i>Durable Category
        private List<ObjectIdentifier> durableIDs;                 //<i>Sequence of Durable ID
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/22 12:48
     */
    @Data
    public static class ProcessOperationListForDurableFromHistoryDRIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private Long searchCount;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/22 12:48
     */
    @Data
    public static class DurableOperationHisInfo {
        private String reportTimeStamp;
        private String routeID;
        private String operationNumber;
        private String operationPass;
        private String operationID;
        private String operationName;
        private String operationCategory;
        private String stageID;
        private String stageGroupID;
        private String maskLevel;
        private String locationID;
        private String workArea;
        private String equipmentID;
        private String equipmentName;
        private String operationMode;
        private String logicalRecipeID;
        private String machineRecipeID;
        private String physicalRecipeID;
        private List<OpeHisRecipeParmInfo> strOpeHisRecipeParmInfo;
        private String durableControlJobID;
        private Long reworkCount;
        private String initialHoldFlag;
        private String lastHoldReleaseFlag;
        private String holdType;
        private String holdTimeStamp;
        private String holdUserID;
        private String holdReasonCodeID;
        private String holdReasonCodeDescription;
        private String reasonCodeID;
        private String reasonCodeDescription;
        private String bankID;
        private String previousBankID;
        private String testType;
        private String productID;
        private String productGroupID;
        private String technologyID;
        private String durableOwnerID;
        private String dueTimeStamp;
        private String userID;
        private String claimMemo;
        private String storeTimeStamp;
        private Boolean testCriteriaFlag;
        private String recipeParameterChangeType;
        private Object siInfo;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/22 12:48
     */
    @Data
    public static class DurableOperationHistoryFillInODRBQ020DRIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private ObjectIdentifier routeID;
        private ObjectIdentifier operationID;
        private String operationNumber;
        private String operationPass;
        private String operationCategory;
        private Boolean pinPointFlag;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/22 12:48
     */
    @Data
    public static class DurableOpeHisRParmGetDRIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private String routeID;
        private String operationNumber;
        private Long operationPassCount;
        private String claimTime;
        private String operationCategory;
        private Object siInfo;
    }

    @Data
    public static class DurableControlJobCreateRequest {
        private String durableCategory;
        private ObjectIdentifier equipmentID;
        private List<StartDurable> strStartDurables;
    }

    @Data
    public static class GatePassDurableInfo {
        private String durableCategory;
        private String currentOperationNumber;
        private ObjectIdentifier durableID;
        private ObjectIdentifier currentRouteID;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class DurableControlJobStartReserveInformationGetOut {
        private ObjectIdentifier equipmentID;
        private String durableCategory;
        private List<StartDurable> strStartDurables;
        private DurableStartRecipe strDurableStartRecipe;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class DurableControlJobStartReserveInformationGetIn {
        private ObjectIdentifier durableControlJobID;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class DurableProcessStateMakeWaitingIn {
        private String durableCategory;
        private List<StartDurable> strStartDurables;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class ProcessStartDurablesReserveInformationClearIn {
        private String durableCategory;
        private List<StartDurable> strStartDurables;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class SendDurableOpeStartCancelReqIn {
        private Params.DurableOperationStartCancelReqInParam strDurableOperationStartCancelReqInParam;
        private String claimMemo;
        private Object siInfo;
    }

    @Data
    public static class SendDurableControlJobActionReqIn {
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier durableControlJobID;
        private String claimMemo;
        private String actionCode;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class DurableCheckConditionForDurablePOIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class ProcessGetTargetOperationForDurableIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private Boolean locateDirection;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class ProcessLocateForDurableOut {
        private Inputs.OldCurrentPOData strOldCurrentPOData;
        private String durableHoldState;
        private Boolean autoBankInFlag;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class ProcessLocateForDurableIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private ProcessRef strProcessRef;
        private Long seqno;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author MINER
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class DurableHoldIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private List<DurableHoldList> strDurableHoldList;
    }


    @Data
    public static class DurableReworkCount {
        private String key;
        private Long reworkCount;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class WhatNextDurableAttributes {
        private String durableCategory;                    //<i>Durable Category
        private ObjectIdentifier durableID;                          //<i>Durable ID
        private String transferStatus;                     //<i>Transfer Status
        private ObjectIdentifier transferReserveUserID;              //<i>Transfer Reserve User ID
        private ObjectIdentifier stockerID;                          //<i>stocker ID
        private ObjectIdentifier equipmentID;                        //<i>Equipment ID
        private ObjectIdentifier durableControlJob;                  //<i>Durable Control Job
        private ObjectIdentifier processReserveEquipmentID;          //<i>Process Reserve Equipment ID
        private ObjectIdentifier processReserveUserID;               //<i>Process Reserve User ID
        private Boolean recipeAvailableFlag;                //<i>Recipe Available Flag
        private ObjectIdentifier logicalRecipeID;                    //<i>Logical Recipe ID
        private ObjectIdentifier machineRecipeID;                    //<i>Machine Recipe ID
        private String lastClaimedTimeStamp;               //<i>Last Claimed Time Stamp
        private String stateChangeTimeStamp;               //<i>State Change Time Stamp
        private String inventoryChangeTimeStamp;           //<i>Inventory Change Time Stamp
        private Double standardProcessTime;                //<i>Standard Process Time
        private String dueTimeStamp;                       //<i>Due Time Stamp
        private String queuedTimeStamp;                    //<i>Queued Time Stamp
        private String preOperationCompTimeStamp;          //<i>Pre Operation Comp Time Stamp
        private ObjectIdentifier startBankID;                        //<i>Start Bank ID
        private ObjectIdentifier routeID;                            //<i>Route ID
        private ObjectIdentifier operationID;                        //<i>Operation ID
        private String operationNumber;                    //<i>Operation Number
        private ObjectIdentifier testTypeID;                         //<i>Test Type ID
        private String inspectionType;                     //<i>Inspection Type
        private ObjectIdentifier stageID;                            //<i>Stage ID
        private Boolean mandatoryOperationFlag;             //<i>Mandatory Operation ID
        private ObjectIdentifier next2EquipmentID;                   //<i>Next2 Equipment ID
        private ObjectIdentifier next2LogicalRecipeID;               //<i>Next2 Logical Recipe ID
        private List<EntityInhibitAttributes> entityInhibitions;                  //<i>Entity Inhibitions
        private String physicalRecipeID;                   //<i>Physical Recipe ID
        private Boolean operableFlagForCurrentMachineState; //<i>Operable Flag For Current Machine State
        private String contentCategory;                    //<i>Content Category
        private Boolean inPostProcessFlagOfDurable;         //<i>InPostProcess Flag of Durable
        private Object siInfo;                             //<i>Reserved for SI customization
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class EquipmentDurablesWhatNextDROut {
        private ObjectIdentifier equipmentID;
        private String equipmentCategory;
        private ObjectIdentifier lastRecipeID;
        private String dispatchRule;
        private Long processRunSizeMaximum;
        private Long processRunSizeMinimum;
        private List<WhatNextDurableAttributes> strWhatNextDurableAttributes;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class EquipmentDurablesWhatNextDRIn {
        private ObjectIdentifier equipmentID;
        private String durableCategory;
        private String selectCriteria;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class MachineRecipeGetListForRecipeBodyManagementForDurableIn {
        private ObjectIdentifier equipmentID;
        private String durableCategory;
        private List<StartDurable> strStartDurables;
        private List<DurableStartRecipe> strDurableStartRecipes;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/6/23 16:47
     */
    @Data
    public static class SendStartDurablesReservationReqIn {
        private Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam;
        private String claimMemo;
        private Object siInfo;
    }


    @Data
    public static class RunCardInfo {
        private String runCardID;
        private ObjectIdentifier lotID;
        private String runCardState;
        private ObjectIdentifier owner;
        private Boolean extApprovalFlag;
        private String createTime;
        private String updateTime;
        private List<String> approvers;
        private List<Infos.RunCardPsmInfo> runCardPsmDocInfos;
        private String claimMemo;
        private Boolean autoCompleteFlag;
    }


    @Data
    public static class RunCardPsmInfo {
        private String psmJobID;
        private Integer sequenceNumber;
        private String psmKey;
        private String createTime;
        private String updateTime;
        private List<Infos.RunCardPsmDocInfo> psmDocInfos;
    }

    @Data
    public static class RunCardPsmDocInfo {
        private String docJobID;
        private Integer sequenceNumber;
        private String psmJobID;
        private String psmKey;
        private String createTime;
        private String updateTime;
    }

    @Data
    public static class RunCardPsmKeyInfo {
        private String subRouteID;
        private String splitOperationNumber;
        private List<String> waferList;
    }


    @Data
    public static class HoldDurableReleaseReqInParam {
        private String durableCategory;            //<i>Durable Category
        private ObjectIdentifier durableID;                  //<i>Durable ID
        private ObjectIdentifier releaseReasonCodeID;        //<i>Release Reason Code ID
        private List<Infos.DurableHoldList> strDurableHoldList;         //<i>Sequence of Durable Hold
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class ProcessCheckGatePassForDurableIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private Object siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class DurableCheckConditionForAutoBankInIn {
        private String durableCategory;
        private ObjectIdentifier durableID;
        private Object siInfo;
    }

    @Data
    public static class ReworkDurableCancelReqInParam {
        private String durableCategory;            //<i>Durable Category
        private ObjectIdentifier durableID;                  //<i>Durable ID
        private ObjectIdentifier currentRouteID;             //<i>Current Route ID
        private String currentOperationNumber;     //<i>Current Operation Number
        private ObjectIdentifier reasonCodeID;               //<i>Reason Code ID
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class ProcessGetReturnOperationForDurableOut {
        private String                              operationNumber;
        private String                              processDefinitionType;
        private Object                              siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class ProcessGetReturnOperationForDurableIn {
        private String                              durableCategory;
        private ObjectIdentifier                    durableID;
        private ObjectIdentifier                    subRouteID;
        private Object                              siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class DurableOriginalRouteListGetOut {
        private List<ObjectIdentifier>              originalRouteID;
        private List<String>                        returnOperationNumber;
        private Object                              siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class DurableOriginalRouteListGetIn {
        private String                              durableCategory;
        private ObjectIdentifier                    durableID;
        private Object                              siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class ProcessDurableReworkCountCheckIn {
        private String                              durableCategory;
        private ObjectIdentifier                    durableID;
        private Object                              siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class ProcessDurableReworkCountIncrementIn {
        private String                              durableCategory;
        private ObjectIdentifier                    durableID;
        private Object                              siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class ProcessBranchRouteForDurableIn {
        private String                              durableCategory;
        private ObjectIdentifier                    durableID;
        private ObjectIdentifier                    subRouteID;
        private String                              returnOperationNumber;
        private Object                              siInfo;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class DurableRouteIDGetIn {
        private String                              durableCategory;
        private ObjectIdentifier                    durableID;
        private Object                              siInfo;
    }

    @Data
    public static class ConnectedDurableRouteListInqInParam {
        private String durableCategory;            //<i>Durable Category
        private ObjectIdentifier durableID;                  //<i>Durable ID
        private String routeType;                  //<i>Route Type
        private ObjectIdentifier routeID;                    //<i>Route ID(optional)
        private String operationNumber;            //<i>Operation Number(optional)
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author ho
     * @exception
     * @date 2020/7/8 15:15
     */
    @Data
    public static class DurableCheckLockHoldConditionForOperationIn {
        private List<String>                      durableCategorySeq;
        private List<ObjectIdentifier>            durableIDSeq;
        private Object                            siInfo;
    }

    @Data
    public static class OmsMsgInfo{
        private String messageDefinitionID;
        private String description;
        private String messageMediaID;
        private String messageType;
        private String tempFileName;
        private String messageDistributeType;
        private String primaryMessage;
        private String secondaryMessage;
        private String subSystem;
    }

    @Data
    public static class AvailableCarrierOut {
        private String foupID;
        private String transferStatus;
    }

    @Data
    public static class DurableLagTimeSentinelTaskInfo {
        private ObjectIdentifier drbID;
        private String category;
    }

    @Data
    public static class BrJobStatusInfo {
        private String jobStatusId;
        private String jobType;
        private String description;
        private List<Infos.UserDataSet> userDataSets;
    }

    @Data
    public static class DurableJobStatusChangeEvent {
        private User user;
        private String durableCategory;
        private ObjectIdentifier durableID;
        private String jobStatus;
        private String equipmentID;
        private String chamberID;
        private String claimMemo;
    }

    @Data
    public static class DurableOnRouteStateGetIn {
        private String                              durableCategory;                        //<i>Durable Category
        private ObjectIdentifier                    durableID;                              //<i>Durable ID
        private Object                              siInfo;                                 //<i>Reserved for SI customization
    }

    @Data
    public static class UsersForOMS {
        private String id;
        private String username;
        private String realName;
        private String mail;
        private String phone;
    }

    @Data
    public static class UserGroupForOMS {
        private String id;
        private String name;
        private List<String> userIds;
    }

    @Data
    public static class AlarmCategory {
        private String id;
        private String name;
        private String description;

        public AlarmCategory() {
        }

        public AlarmCategory(String name) {
            this.name = name;
            this.id = name;
        }
    }

    @Data
    public static class PodInErack {
        private ObjectIdentifier podID;
        private Integer shelfPositionX;
        private Integer shelfPositionY;
        private Integer shelfPositionZ;
        private String shelfType;
    }

    @Data
    public static class TimeRecipeUse {
        private String recipeID; //Sequence of Scrap wafer Information
        private Timestamp lastUseTime;
    }

    @Data
    public static class RecipeTime {
        private String recipeID; //Sequence of Scrap wafer Information
        private Integer time;
        private Timestamp lastUseTime;
    }

    @Data
    public static class MfgMMResponse {
        private List<FailMfgRequest> failMfgRequestList;
    }

    @Data
    public static class FailMfgRequest {
        private ObjectIdentifier entityInhibitID;
        private EntityInhibitDetailAttributes entityInhibitDetailAttributes;
        private Integer reasonCode;
        private String messageText;
    }

    @Data
    public static class DispatchResultRet {
        private Long                numberRows;              //<i>Number of Rows
        private Long                numberCols;              //<i>Number of Cols
        private String              systemErrorCode;         //<i>System Error Code
        private String              systemErrorMessage;      //<i>System Error Message
        private String              userErrorCode;           //<i>User Error Code
        private String              userErrorMessage;        //<i>User Error Message
        private String              dispatchErrorMessage;    //<i>Dispatch Error Message
        List<String>                dispatchRows;            //<i>Sequence of Dispatch
        List<String>                columnHeaders;           //<i>Sequence of Column Headers
        List<Long>                  columnWidths;            //<i>Sequence of Column Width
        List<String>                columnTypes;             //<i>Sequence of Column Type
        private Object              siInfo;                  //<i>Reserved for SI customization
    }

    @Data
    public static class RALSetResult {
        private String                                  reticleID;              //<i>Reticle ID
        private String                                  reticlePodID;           //<i>Reticle Pod ID
        private String                                  RALSetCode;             //<i>Reticle Action List set code
        private String                                  toEquipmentID;          //<i>Destination Equipment ID
        private String                                  priority;               //<i>Request priority
        private String                                  originalJobStatus;      //<i>Original job status
        private String                                  setTimeStamp;           //<i>Set Timpstamp
        private String                                  setResult;              //<i>Set result
        private Object                                  siInfo;                 //<i>Reserved for SI customization
    }

    @Data
    public static class NextMachineForReticlePod {
        private ObjectIdentifier                        reticlePodStockerID ;    //<i>Reticle Pod Stocker ID
        private ObjectIdentifier                        bareReticleStockerID ;   //<i>Bare Reticle Stocker ID
        private ObjectIdentifier                        resourceID ;             //<i>Resource ID
        private ObjectIdentifier                        equipmentID ;            //<i>Equipment ID
        private ObjectIdentifier                        portID ;                 //<i>Port ID
        private Object                                  siInfo;                  //<i>Reserved for SI customization
    }
    @Data
    public static class ReticleComponentJob {
        private String                                  requestedTimestamp;                 //<i>Requested timestamp
        private Long                                    priority;                           //<i>Priority
        private String                                  reticleDispatchJobID;               //<i>Reticle dispatch job
        private String                                  reticleComponentJobID;              //<i>Reticle component job ID
        private ObjectIdentifier                        reticleDispatchJobRequestUserID;    //<i>Reticle dispatch job requested user
        private Long                                    jobSeq;                             //<i>Job Sequence
        private ObjectIdentifier                        reticlePodID;                       //<i>Reticle Pod ID
        private Long                                    slotNo;                             //<i>Slot Number
        private ObjectIdentifier                        reticleID;                          //<i>Reticle ID
        private String                                  jobName;                            //<i>Job name
        private ObjectIdentifier                        toEquipmentID;                      //<i>Destination Equipment ID
        private ObjectIdentifier                        toReticlePodPortID;                 //<i>Destination reticle pod port ID
        private String                                  toEquipmentCategory;                //<i>Destination equipment category
        private ObjectIdentifier                        fromEquipmentID;                    //<i>From Equpment ID
        private ObjectIdentifier                        fromReticlePodPortID;               //<i>From reticle pod port ID
        private String                                  fromEquipmentCategory;              //<i>From equipment category
        private String                                  jobStatus;                          //<i>Job Status
        private String                                  jobStatusChangeTimestamp;           //<i>Job Status change timestamp
        private Object                                  siInfo;
    }

    @Data
    public static class ReticleEventRecord {
        private String            eventTime;                //<i>Event Time
        private String            reticleDispatchJobID;     //<i>Reticle Dispatch Job ID
        private String            reticleComponentJobID;    //<i>Reticle Component Job ID
        private ObjectIdentifier  reticlePodStockerID;      //<i>Reticle Pod Stocker ID
        private ObjectIdentifier  bareReticleStockerID;     //<i>Bare Reticle Stocker ID
        private ObjectIdentifier  resourceID;               //<i>Resource ID
        private ObjectIdentifier  equipmentID;              //<i>Equipment ID
        private ObjectIdentifier  RSPPortID;                //<i>RSP Port ID
        private ObjectIdentifier  reticlePodID;             //<i>Reticle Pod ID
        private String            RSPPortEvent;             //<i>RSP Port Event
        private ObjectIdentifier  reticleID;                //<i>Reticle ID
        private String            reticleJobEvent;          //<i>Reticle Job Event
    }

    @Data
    public static class ReticlePodInfoInStocker {
        private ObjectIdentifier                        reticlePodID;               //<i>Reticle Pod ID
        private ObjectIdentifier                        reticlePodCategoryID;       //<i>Reticle Pod Category ID
        private String                                  reticlePodStatus;           //<i>Reticle Pod Status
        private Boolean                                 emptyFlag;                  //<i>Empty Flag
        private ObjectIdentifier                        reticleID;                  //<i>Reticle ID
        private String                                  reticleSubStatus;           //<i>Reticle Sub Status
        private Boolean                                 reticleInspectionNGFlg;     //<i>Reticle Inspection NG Flag
        private String                                  transferStatus;             //<i>Transfer Status
        private Boolean                                 transferReserveFlag;        //<i>Transfer Reserve Flag
        private ObjectIdentifier                        transferReserveUserID;      //<i>Transfer Reserve User ID
    }


    @Data
    public static class ReticleRetrieveResult {
        private  ObjectIdentifier                        reticleID;                          //<i>Reticle ID
        private  Integer                                    slotNo;                             //<i>Slot Number
        private  Boolean                                 reticleRetrievedFlag;               //<i>Reticle Retrieve request flag
    }

    @Data
    public static class EqpRSPPortEventOnEAP {
        private ObjectIdentifier                        portID;          //<i>Port ID
        private String                                  portStatus;      //<i>Port Status
        private ObjectIdentifier                        reticlePodID;    //<i>Reticle Pod ID
        private Object                                  siInfo;          //<i>Reserved for SI customization
    }

    @Data
    public static class ReticleXferJob {
        private ObjectIdentifier                        fromEquipmentID;        //<i>From Equpment ID
        private ObjectIdentifier                        fromPortID;             //<i>From port ID
        private ObjectIdentifier                        toEquipmentID;          //<i>Destination Equipment ID
        private ObjectIdentifier                        toPortID;               //<i>Destination port ID
        private ObjectIdentifier                        reticlePodID;           //<i>Reticle Pod ID
        private ObjectIdentifier                        reticleID;              //<i>Reticle ID
        private Object                                  siInfo;                 //<i>Reserved for SI customization
    }

    @Data
    public static class CandidateReticlePod {
        private ObjectIdentifier                reticlePodID;                    //<i>Reticle Pod ID
        private Long                            slotNumber;                      //<i>Slot Number
        private String                          reticlePodStatus;                //<i>Reticle Pod Status
        private String                          reticlePodTransferStatus;        //<i>Reticle Pod Transfer Status
        private ObjectIdentifier                currentReticlePodStockerID;      //<i>Current Reticle Pod Stocker ID
        private ObjectIdentifier                currentBareReticleStockerID;     //<i>Current Bare Reticle Stocker ID
        private ObjectIdentifier                currentEquipmentID;              //<i>Current Equipment ID
        private Object                          siInfo;                          //<i>Reserved for SI customization
    }

    @Data
    public static class ReticleStoreResult {
        private ObjectIdentifier                reticleID;          //<i>Reticle ID
        private Integer                            slotNo;             //<i>Slot No
        private Boolean                         reticleStoredFlag;  //<i>Reticle Stored Flag
    }

    @Data
    public static class ReticlePodXferJobCompInfo {
        private ObjectIdentifier                        reticlePodID;                   //<i>Reticle Pod ID
        private ObjectIdentifier                        toMachineID;                    //<i>Transfer Destination
        private ObjectIdentifier                        toPortID;                       //<i>Destination Port
        private String                                  transferJobStatus;              //<i>Transfer job status
    }

    @Data
    public static class ReticlePodJob {
        private String                                  reticlePodJobID;      //<i> Reticle Pod Job ID
        private String                                  reticlePodJobStatus;  //<i> Reticle Pod Job Status
        private ObjectIdentifier                        reticlePodID;         //<i> Reticle Pod ID
        private ObjectIdentifier                        fromMachineID;        //<i> From Machine ID
        private ObjectIdentifier                        fromPortID;           //<i> From Port ID
        private String                                  toStockerGroup;       //<i> To Stocker Group
        private ObjectIdentifier                        toMachineID;          //<i> To Machine ID
        private ObjectIdentifier                        toPortID;             //<i> To Port ID
        private Object                                  siInfo;
    }

    @Data
    public static class ReticlePodXferJob {
        private String                                  jobID;                  //<i>Job ID
        private String                                  jobStatus;              //<i>Job Status
        private String                                  transportType;          //<i>Transport Type
        private List<ReticlePodJob>                     reticlePodJobList;       //<i>Sequence of Reticle Pod Job
        private Object                                  siInfo;
    }

    @Data
    public static class ConstraintByEqpInfo {
        private String equipmentId;
        private String restrictId;
        private String chamberId;
        private String productId;
        private String reticleGrpId;
        private String reticleId;
        private String routeId;
        private String recipeId;
        private String lotId;
        private String processId;
        private String subLotType;
        private String stepNo;
        private String startTime;
        private String endTime;
        private String claimMemo;
        private String description;
        private ObjectIdentifier ownerID;
    }

    @Data
    public static class ExceptionInfo {
        private String objectId;
        private String exception;
    }

    @Data
    public static class ForceMeasurementInfo {
        private String stepNo;
        private String stepType;
        private String stepLabel;
        private ObjectIdentifier stepID;
        private Boolean forceMeasurementFlag;
    }


    @Data
    public static class BrPilotRunSectionInfo {
        private String stepNumber;
        private String stepLabel;
        private String waferSelectionRule;
        private String waferSelectionValue;
    }


    @Data
    public static class CheckPilotRunForEquipmentInfo {
        private ObjectIdentifier pilotRunPlanID;
        private ObjectIdentifier pilotRunJobID;
        private ObjectIdentifier recipeID;
        private ObjectIdentifier productID;
    }

    @Data
    public static class checkPilotRunCompletedInfo {
        private ObjectIdentifier lotID;
        private String  operationNumber;

    }



    @Data
    public static class ReticleHoldReq {
        private String holdType;
        private ObjectIdentifier holdReasonCodeID;
        private ObjectIdentifier holdUserID;
        private String responsibleOperationMark;
        private ObjectIdentifier routeID;
        private String operationNumber;
        private ObjectIdentifier relatedDurableID;
        private String claimMemo;
    }

    @Data
    public static class HoldEquipmentModel {
        private Boolean equipmentHold;
        private String equipmentId;
        private String reason;
    }

    @Data
    public static class InhibitRecipeResultModel {
        private Boolean recipeHold;
        private String recipe;
        private String reason;
    }
    @Data
    public static class ReticleReasonReq {
        private String holdType;
        private ObjectIdentifier holdReasonCodeID;
        private ObjectIdentifier holdUserID;
        private String responsibleOperationMark;
        private ObjectIdentifier reticleID;
        private String claimMemo;
    }



    @Data
    public static class ReticleHoldListAttributes {
        private String holdType;
        private ObjectIdentifier reasonCodeID;                //<i>Reason CimCode ID
        private String codeDescription;             //<i>CimCode Description
        private ObjectIdentifier userID;                      //<i>User ID
        private String userName;                    //<i>User Name
        private String holdTimeStamp;               //<i>Hold Time Stamp
        private String responsibleOperationMark;    //<c>SP_ResponsibleOperation_Current        "C"
        //<c>SP_ResponsibleOperation_Previous       "P"
        private ObjectIdentifier responsibleRouteID;          //<i>Responsible Route ID
        private String responsibleOperationNumber;  //<i>Responsible Operation Number
        private String responsibleOperationName;    //<i>Responsible Operation Name
        private ObjectIdentifier relatedLotID;                 //<i>Related lot ID
        private String claimMemo;                   //<i>Claim Comment
    }

    @Data
    public static class EqpCapabilityInfo{
        private ObjectIdentifier eqpID;
        List<EqpCapabilityDetail> eqpCapabilityDetailList;
    }

    @Data
    public static class EqpCapabilityDetail{
        private ObjectIdentifier chamberID;
        private String eqpCapability;
    }


	@Data
	public static class HoldLotCheckInfo{
        private ObjectIdentifier cassetteID;
        private Boolean backupProcessingFlag;
	}


    @Data
    public static class APCParamInfo{
        private ObjectIdentifier equipmentID;
        private String portGroupID;
        private ObjectIdentifier controlJobID;
        private List<Infos.StartCassette> startCassetteList;
    }
    @Data
    public static class OcapInfo {
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private String ocapNo;
        private Boolean addMeasureFlag;
        private Boolean reMeasureFlag;
        private Boolean removeFlag;
        private String waferList;
        private ObjectIdentifier recipeID;
    }


    @Data
    public static class XferFixture{
        private ObjectIdentifier fixtureID;
        private String transferStatus;
        private String xPosition;
        private String yPosition;
        private String zPosition;
        private String transferStatusChangeTimeStamp;
    }

    @Data
    public static class FixtureBr{
        private String description;                   //<i>Description
        private String fixturePartNumber;             //<i>Fixture Part Number
        private String fixtureSerialNumber;           //<i>Fixture Serial Number
        private String supplierName;                  //<i>Supplier Name
        private boolean usageCheckFlag;                //<i>Usage Check Flag
        private ObjectIdentifier fixtureGroupID;             //<i>Fixture Group ID
        private String  fixtureGroupDescription;       //<i>Fixture Group Description
        private ObjectIdentifier fixtureCategoryID;          //<i>Fixture Category ID
        private String fixtureCategoryDescription;    //<i>Fixture Category Description
    }

    @Data
    public static class FixturePm{
        private String runTime;                       //<i>Run Time
        private String maximumRunTime;                //<i>Maximum Run Time
        private long operationStartCount;           //<i>Operation Start Count
        private long maximumOperationStartCount;    //<i>Maximum Operation Start Count
        private long passageTimeFromLastPM;         //<i>Passage Time From Last PM    //D3100008
        private long intervalBetweenPM;             //<i>Interval Between PM          //D3100008
        private String lastMaintenanceTimeStamp;      //<i>Last Maintenance Time Stamp
        private ObjectIdentifier lastMaintenancePerson;      //<i>Last Maintenance Person
    }

    @Data
    public static class InventoryFixtureInfo{
        private ObjectIdentifier fixtureID;
        private String xPosition;
        private String yPosition;
        private String zPosition;
    }

    @Data
    public static class AdvancedWaferSamplingConvertInfo{
        // wafer 是否进行加工信息
        private LotInCassette lotInCassette;

        // 是否命中advanced wafer sampling 进行加工
        private boolean hitSampling;
    }

}
