package com.fa.cim.controller.interfaces.season;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @exception
 * @author ho
 * @date 2020/5/27 13:29
 */
public interface ISeasoningInqController {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param machineParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 13:35
     */
    Response machineSeasonPlanInq(Params.IdentifierParams machineParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param machineParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/6/23 12:42
     */
    Response allSeasonProductInq(@RequestBody Params.IdentifierParams machineParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param machineParams
     * @return com.fa.cim.common.support.Response
     * @exception 
     * @author ho
     * @date 2020/6/30 13:13
     */
    Response allSeasonProductRecipeInq(@RequestBody Params.IdentifierParams machineParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 13:35
     */
    Response seasonPlanInfoInq(Params.IdentifierParams seasonParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param recipeGroupParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 13:36
     */
    Response recipeGroupInfoInq(Params.IdentifierParams recipeGroupParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param machineParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 13:40
     */
    Response machineSeasonJobInq(Params.IdentifierParams machineParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonJobParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/5/27 13:41
     */
    Response seasonJobInfoInq(Params.IdentifierParams seasonJobParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param userParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/6/4 14:04
     */
    Response seasonTypesInq(Params.UserParams userParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param userParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/6/5 16:35
     */
    Response allSeasonStatusInq(Params.UserParams userParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param userParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/6/5 16:41
     */
    Response allSeasonJobStatusInq(Params.UserParams userParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param userParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/6/5 10:09
     */
    Response recipeGroupInq(Params.UserParams userParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonWhatNextLotListParams
     * @return com.fa.cim.common.support.Response
     * @exception
     * @author ho
     * @date 2020/6/11 12:39
     */
    Response seasonWhatNextInq(Params.SeasonWhatNextLotListParams seasonWhatNextLotListParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonLotsMoveInReserveInfoInqParams
     * @return com.fa.cim.common.support.Response
     * @exception 
     * @author ho
     * @date 2020/7/28 15:07
     */
    Response seasonLotsMoveInReserveInfoInq(@RequestBody Params.SeasonLotsMoveInReserveInfoInqParams seasonLotsMoveInReserveInfoInqParams);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonLotsMoveInReserveInfoForIBInqParams
     * @return com.fa.cim.common.support.Response
     * @exception 
     * @author ho
     * @date 2020/7/28 15:33
     */
    Response seasonLotsMoveInReserveInfoForIBInq(@RequestBody Params.SeasonLotsMoveInReserveInfoForIBInqParams seasonLotsMoveInReserveInfoForIBInqParams);

}