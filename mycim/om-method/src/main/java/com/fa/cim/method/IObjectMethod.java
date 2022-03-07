package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.newcore.bo.CimBO;
import com.fa.cim.sorter.Info;

import java.util.List;

/**
 * description:
 * This file use to define the IObjectMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/25        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/9/25 18:04
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IObjectMethod {


    void objectCheckForCreation(Infos.ObjCommon objCommon, String className, ObjectIdentifier durableID);
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         -
     * @param objectIDListGetDR -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Outputs.ObjectIDList>
     * @author Sun
     * @date 11/16/2018 3:26 PM
     */
    Outputs.ObjectIDList objectIDListGetDR(Infos.ObjCommon objCommon, Infos.objObjectIDListGetDR objectIDListGetDR);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/24 17:03
     * @param objCommon
     * @param in -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.ObjectIDList>>
     */
    List<Infos.ObjectIDList> objectIDListGetForProcessDefinitionDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/24 17:03
     * @param objCommon
     * @param in -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.ObjectIDList>>
     */
    List<Infos.ObjectIDList> objectIDListGetForLotDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/24 17:03
     * @param objCommon
     * @param in -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.ObjectIDList>>
     */
    List<Infos.ObjectIDList> objectIDListGetForEquipmentDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/24 17:03
     * @param objCommon
     * @param in -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.ObjectIDList>>
     */
    List<Infos.ObjectIDList> objectIDListGetForAreaBankDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/24 17:04
     * @param objCommon
     * @param in -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.ObjectIDList>>
     */
    List<Infos.ObjectIDList> objectIDListGetForDurableDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/5/24 17:04
     * @param objCommon
     * @param in -
     * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.ObjectIDList>>
     */
    List<Infos.ObjectIDList> objectIDListGetForDurableSubStateDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in);

    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @date 2019/8/13 17:44
      * @param objCommon -
      * @param strCDAValueInqInParm -
      * @param bo -
      * @return com.fa.cim.common.support.RetCode<java.util.List<com.fa.cim.dto.Infos.UserData>>
      */
    <T extends CimBO> List<Infos.UserData> objectUserDataGet(Infos.ObjCommon objCommon, Infos.CDAValueInqInParm strCDAValueInqInParm, T bo);

    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @since 2019/8/8 14:54
      * @param objCommon -
      * @param objObjectGetIn -
      * @return com.fa.cim.common.support.RetCode<T>
      */
    <T extends CimBO> T objectGet(Infos.ObjCommon objCommon, Inputs.ObjObjectGetIn objObjectGetIn);
    
    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @since  2019/8/15 14:30
      * @param objCommon -
      * @param userData -
      * @param bo -
      */
    <T extends CimBO> void objectUserDataSet(Infos.ObjCommon objCommon, Infos.UserData userData, T bo);


    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @since 2019/8/15 14:33
      * @param objCommon -
      * @param userData -
      * @param bo -
      */
    <T extends CimBO> void objectUserDataRemove(Infos.ObjCommon objCommon, Infos.UserData userData, T bo);

    /**     
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * @author Bear
      * @since 2019/8/15 14:34
      * @param objCommon -
      * @param bo -
      * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjectClassIDInfoGetDROut>
      */
    <T extends CimBO> Outputs.ObjectClassIDInfoGetDROut ObjectClassIDInfoGetDR(Infos.ObjCommon objCommon, T bo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/16 17:00
     * @param objCommon
     * @param objectValidSorterJobGetIn -
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Outputs.ObjObjectValidSorterJobGetOut>
     */
    Info.ObjObjectValidSorterJobGetOut objectValidSorterJobGet(Infos.ObjCommon objCommon, Info.ObjectValidSorterJobGetIn objectValidSorterJobGetIn);

    /**
     * description:
     * get eqp lock mode
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  - objCommon
     * @param objLockModeIn - objLockModeIn
     * @return com.fa.cim.pojo.obj.Outputs.ObjLockModeOut
     * @author Bear
     * @date 2018/4/12
     */
    Outputs.ObjLockModeOut objectLockModeGet(Infos.ObjCommon objCommon, Inputs.ObjLockModeIn objLockModeIn);
}
