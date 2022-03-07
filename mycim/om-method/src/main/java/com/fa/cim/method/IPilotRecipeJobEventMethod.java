package com.fa.cim.method;

import com.fa.cim.dto.Infos;

public interface IPilotRecipeJobEventMethod {

    /**
     * description: create event for recipe job
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 15:18                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/17 15:18
     * @param objCommon - user permission information
     * @param transactionID - function id
     * @param memo - content description
     * @param params - create an param to event
     * @return void
     */
    void pilotEventMake(Infos.ObjCommon objCommon, String transactionID, String memo, com.fa.cim.fsm.Infos.PilotEventMakeInfo params);

}
