package com.fa.cim.newIntegration.durable.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * <p>CarrierListbyCategoryCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/3/3/003   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/3/3/003 13:12
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class CarrierListbyCategoryCase {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private DurableController durableController;

    @Autowired
    private DurableInqController durableInqController;

    @Autowired
    private RetCodeConfig retCodeConfig;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public ObjectIdentifier emptyID = ObjectIdentifier.build(null,null);
    public ObjectIdentifier normalUse = ObjectIdentifier.build("NORMAL_USE",null);
    public ObjectIdentifier stockerID = ObjectIdentifier.build("STK0101",null);
    public ObjectIdentifier carrierID = new ObjectIdentifier("CRUP0001","FRCAST.81057531304862583");

    public void DUR2_1_1NormalUseOfCarrierListByCategory(){
        Params.CarrierListInqParams params = new Params.CarrierListInqParams();
        params.setMaxRetrieveCount(300L);
        params.setUser(getUser());
        params.setDurablesSubStatus(normalUse);
        params.setCassetteStatus("AVAILABLE");
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setPage(1);
        searchCondition.setSize(10);
        params.setSearchCondition(searchCondition);
        Response response = durableInqController.carrierListInq(params);
        Results.CarrierListInq170Result carrierListInq170Result = (Results.CarrierListInq170Result) response.getBody();
        Page<Infos.FoundCassette> foundCassettes = carrierListInq170Result.getFoundCassette();
        List<Infos.FoundCassette> cassettes = foundCassettes.getContent();
        for (Infos.FoundCassette cassette:cassettes) {
            if (!CimStringUtils.equals(cassette.getCassetteStatus(),"AVAILABLE")){
                throw new ServiceException(retCodeConfig.getInvalidCassetteState());
            }
        }
    }

    public void DUR2_1_2CarrierListSearchByCarrierID(){
        Params.CarrierListInqParams params = new Params.CarrierListInqParams();
        params.setMaxRetrieveCount(300L);
        params.setUser(getUser());
        params.setCassetteID(carrierID);
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setPage(1);
        searchCondition.setSize(10);
        params.setSearchCondition(searchCondition);
        Response response = durableInqController.carrierListInq(params);
        Results.CarrierListInq170Result carrierListInq170Result = (Results.CarrierListInq170Result) response.getBody();
        Page<Infos.FoundCassette> foundCassettes = carrierListInq170Result.getFoundCassette();
        List<Infos.FoundCassette> cassettes = foundCassettes.getContent();
        for (Infos.FoundCassette cassette:cassettes) {
            if (!CimObjectUtils.equalsWithValue(cassette.getCassetteID(),carrierID)){
                throw new ServiceException(retCodeConfig.getCassetteNotSame());
            }
        }
    }

    public void DUR2_1_3CarrierListSearchByCarrierType(){
        Params.CarrierListInqParams params = new Params.CarrierListInqParams();
        params.setMaxRetrieveCount(300L);
        params.setUser(getUser());
        params.setCassetteCategory("FOSB");
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setPage(1);
        searchCondition.setSize(10);
        params.setSearchCondition(searchCondition);
        Response response = durableInqController.carrierListInq(params);
        Results.CarrierListInq170Result carrierListInq170Result = (Results.CarrierListInq170Result) response.getBody();
        Page<Infos.FoundCassette> foundCassettes = carrierListInq170Result.getFoundCassette();
        List<Infos.FoundCassette> cassettes = foundCassettes.getContent();
        for (Infos.FoundCassette cassette:cassettes) {
            if (!CimStringUtils.equals(cassette.getCassetteCategory(),"FOSB")){
                throw new ServiceException(retCodeConfig.getCassetteCategoryMismatch());
            }
        }
    }

    public void DUR2_1_4CarrierListSearchByStationID(){
        Params.CarrierListInqParams params = new Params.CarrierListInqParams();
        params.setMaxRetrieveCount(300L);
        params.setUser(getUser());
        params.setStockerID(stockerID);
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setPage(1);
        searchCondition.setSize(10);
        params.setSearchCondition(searchCondition);
        Response response = durableInqController.carrierListInq(params);
        Results.CarrierListInq170Result carrierListInq170Result = (Results.CarrierListInq170Result) response.getBody();
        Page<Infos.FoundCassette> foundCassettes = carrierListInq170Result.getFoundCassette();
        List<Infos.FoundCassette> cassettes = foundCassettes.getContent();
        for (Infos.FoundCassette cassette:cassettes) {
            if (!CimObjectUtils.equalsWithValue(cassette.getStockerID(),stockerID)){
                throw new ServiceException(retCodeConfig.getCassetteCategoryMismatch());
            }
        }
    }

    public void DUR2_1_5CarrierListSearchByWrongInfo(){
        Params.CarrierDetailInfoInqParams params = new Params.CarrierDetailInfoInqParams();
        params.setUser(getUser());
        params.setDurableOperationInfoFlag(true);
        params.setDurableWipOperationInfoFlag(true);
        params.setCassetteID(ObjectIdentifier.buildWithValue("FOUP0010008"));
        try {
            durableInqController.carrierDetailInfoInq(params);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode() != 1424 ){
                throw new ServiceException(retCodeConfig.getError());
            }
        }

    }

}
