package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

public interface IFutureHoldMethod {

    Outputs.FutureHoldRequestsForPreviousPO getForResponsibleOperation(Infos.ObjCommon objCommon,
                                                                       ObjectIdentifier lotID,
                                                                       Infos.EffectCondition effectCondition);

    Outputs.FutureHoldRequestsDeleteForPreviousPO deleteForResponsibleOperation(Infos.ObjCommon objCommon,
                                                                                ObjectIdentifier lotID,
                                                                                Infos.EffectCondition effectCondition);
}
