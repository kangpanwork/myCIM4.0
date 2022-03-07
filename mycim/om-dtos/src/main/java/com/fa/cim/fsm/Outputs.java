package com.fa.cim.fsm;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Outputs {
    @Data
    public static class ObjExperimentalFutureLotInfoGetOut {
        private com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo strExperimentalFutureLotInfo;
        private String siInfo;
    }

    @Data
    public static class ObjExperimentalFutureLotActualInfoCreateOut {
        private ObjectIdentifier lotFamilyId;
        private ObjectIdentifier splitRouteId;
        private String splitOperationNumber;
        private ObjectIdentifier originalRouteId;
        private String originalOperationNumber;
        private boolean actionEmail;
        private boolean actionHold;
        private Boolean actionSeparateHold;
        private Boolean actionCombineHold;
        private String testMemo;
        private List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strExperimentalFutureLotDetailInfoSeq;
        private com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailResultInfo strExperimentalLotDetailResultInfo;
        private Object siInfo;
    }

}
