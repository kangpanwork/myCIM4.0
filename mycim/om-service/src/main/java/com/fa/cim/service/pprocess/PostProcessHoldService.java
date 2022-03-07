package com.fa.cim.service.pprocess;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

public interface PostProcessHoldService {

    /**
     * lock the lot by holding the lot with reason code "LOCK"
     * @param objCommon objCommon
     * @param lotID lotID
     * @return the hold record
     */
    Infos.LotHoldReq sxLockHoldOnLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * release the lock on lot with reason code "LOCK"
     *  @param objCommon objCommon
     * @param lotID lotID
     * @param lotHoldReq lot hold record to release
     */
    void sxReleaseLockHoldOnLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Infos.LotHoldReq lotHoldReq);

    /**
     * lock the durable by holding the durable with reason doe "LOCK"
     *
     * @param objCommon objCommon
     * @param durableID lotID
     */
    void sxLockHoldOnDurable(Infos.ObjCommon objCommon, ObjectIdentifier durableID);

    /**
     * release the lock on durable with reason code "LOCK"
     *
     * @param objCommon objCommon
     * @param durableID durableID
     */
    void sxReleaseLockHoldOnDurable(Infos.ObjCommon objCommon, ObjectIdentifier durableID);

}
