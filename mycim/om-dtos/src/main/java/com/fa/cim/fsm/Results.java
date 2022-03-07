package com.fa.cim.fsm;

import com.fa.cim.dto.Infos;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Results {
    @Data
    public static class FSMLotInfoInqResult {
        private Boolean editFlag;                                                 //<i>Edit Flag
        private com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo  strExperimentalFutureLotInfo;             //<i>Experimental Lot Information             //D7000015
    }
}
