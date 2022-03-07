package com.fa.cim.service.lot;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

public interface ILotProcessOperationService {

    /**
     * have the lot move to next step
     *
     * @param objCommon objCommon
     * @param lotID lotID
     */
    void sxProcessMove(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

}
