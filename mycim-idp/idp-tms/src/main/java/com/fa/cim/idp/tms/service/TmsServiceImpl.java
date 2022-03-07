package com.fa.cim.idp.tms.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.*;
import com.fa.cim.idp.tms.adaptor.common.TmsUser;
import com.fa.cim.idp.tms.adaptor.param.*;
import com.fa.cim.idp.tms.api.TmsService;
import com.fa.cim.idp.tms.menu.TmsTransactionIDEnum;
import com.fa.cim.idp.tms.param.TmsRequest;
import com.fa.cim.idp.tms.remote.IXferRemoteManager;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * description:
 * <p>TmsServiceImpl .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/12        ********             Yuri               create file
 *
 * @author: Yuri
 * @date: 2019/4/12 16:25
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class TmsServiceImpl implements TmsService {


	@Autowired
	private IXferRemoteManager xferRemoteManager;


	@Override
	public Results.TransportJobCreateReqResult rtransportJobCreateReq(Infos.ObjCommon objCommon, User user, Params.TransportJobCreateReqParams param) {
		TmsTransportJobCreateReqParam tmsParam = new TmsTransportJobCreateReqParam().from(param);
		tmsParam.setRequestUserID(new TmsUser().from(user));

		//transfer to tms
		TmsRequest tmsRequest=new TmsRequest();
		tmsRequest.setFunctionId(TmsTransactionIDEnum.ROM01.getValue());
		tmsRequest.setMessageID(UUID.randomUUID().toString());
		tmsRequest.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
		tmsRequest.setMessageBody(JSON.toJSONString(tmsParam));
		String requestJson= JSONObject.toJSONString(tmsRequest);
		log.info("call tms request :{} " , requestJson);

		//call tms and return
		Response response = null;
		try {
			response = xferRemoteManager.rtransportJobCreateReq(tmsParam);
		} catch(CimIntegrationException e) {
			Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
		}

		Results.TransportJobCreateReqResult transportJobCreateReqResult = JSON.parseObject(response.getBody().toString(), Results.TransportJobCreateReqResult.class);
		return transportJobCreateReqResult;
	}

	@Override
	public Results.TransportJobCreateReqResult transportJobCreateReq(Infos.ObjCommon objCommon, User user, Params.TransportJobCreateReqParams param) {

		TmsTransportJobCreateReqParam tmsParam = new TmsTransportJobCreateReqParam().from(param);
		tmsParam.setRequestUserID(new TmsUser().from(user));
		if (CimStringUtils.equals("S",param.getTransportType()) && CimArrayUtils.getSize(param.getJobCreateData()) > 1){
			tmsParam.setTransportType("B");
		}

		//transfer to tms
		TmsRequest tmsRequest=new TmsRequest();
		tmsRequest.setFunctionId(TmsTransactionIDEnum.OM01.getValue());
		tmsRequest.setMessageID(UUID.randomUUID().toString());
		tmsRequest.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
		tmsRequest.setMessageBody(JSON.toJSONString(tmsParam));
		String requestJson= JSONObject.toJSONString(tmsRequest);
		log.info("call tms request :{} " , requestJson);

		//call tms and return
		Response response = null;
		try {
			response = xferRemoteManager.transportJobCreateReq(tmsParam);
		} catch(CimIntegrationException e) {
			Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
		}

		Results.TransportJobCreateReqResult transportJobCreateReqResult = JSON.parseObject(response.getBody().toString(), Results.TransportJobCreateReqResult.class);
		return transportJobCreateReqResult;
	}

	@Override
	public Outputs.SendTransportJobInqOut transportJobInq(Inputs.SendTransportJobInqIn param) {

		TmsTransportJobInqParam tmsParam = new TmsTransportJobInqParam().from(param);

		//transfer to tms
		TmsRequest tmsRequest=new TmsRequest();
		tmsRequest.setFunctionId(TmsTransactionIDEnum.OM14.getValue());
		tmsRequest.setMessageID(UUID.randomUUID().toString());
		tmsRequest.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
		tmsRequest.setMessageBody(JSON.toJSONString(tmsParam));
		String requestJson= JSONObject.toJSONString(tmsRequest);
		log.info("call tms request :{} " , requestJson);

		Response response = null;
		try {
			response = xferRemoteManager.transportJobInq(tmsParam);
		} catch(CimIntegrationException e) {
			Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
		}

//		Results.TransportJobInqResult transportJobInqResult = JSON.parseObject(response.getBody().toString(), TmsTransportJobInqResult.class).adapt();
		Results.TransportJobInqResult transportJobInqResult = JSON.parseObject(response.getBody().toString(), Results.TransportJobInqResult.class);

		Outputs.SendTransportJobInqOut out = new Outputs.SendTransportJobInqOut();
		out.setStrTransportJobInqResult(transportJobInqResult);
		return out;
	}

	@Override
	public Outputs.SendTransportJobInqOut rtransportJobInq(Inputs.SendRTMSTransportJobInqIn param) {
		Inputs.SendTransportJobInqIn inputParam = new Inputs.SendTransportJobInqIn();
		Infos.TransportJobInq transportJobInq = new Infos.TransportJobInq();
		inputParam.setTransportJobInq(transportJobInq);
		inputParam.setUser(param.getRequestUserID());
		inputParam.setStrObjCommonIn(param.getStrObjCommonIn());

		ObjectIdentifier reticlePodID = param.getReticlePodID();
		ObjectIdentifier toMachineID = param.getToMachineID();
		ObjectIdentifier toStockerID = param.getToStockerID();
		ObjectIdentifier fromMachineID = param.getFromMachineID();
		ObjectIdentifier fromStockerID = param.getFromStockerID();
		if(ObjectIdentifier.isNotEmptyWithValue(reticlePodID)) // reticlePodID
		{
			transportJobInq.setInquiryType(BizConstant.SP_TRANSFERJOB_INQ_TYPE_C);
			transportJobInq.setCarrierID(reticlePodID);
		}
		else if(ObjectIdentifier.isNotEmptyWithValue(toMachineID)) // toMachineID
		{
			transportJobInq.setInquiryType(BizConstant.SP_TRANSFERJOB_INQ_TYPE_T);
			transportJobInq.setToMachineID(toMachineID);
		}
		else if(ObjectIdentifier.isNotEmptyWithValue(toStockerID)) // toStockerID
		{
			transportJobInq.setInquiryType(BizConstant.SP_TRANSFERJOB_INQ_TYPE_T);
			transportJobInq.setToMachineID(toStockerID);
		}
		else if(ObjectIdentifier.isNotEmptyWithValue(fromMachineID)) // fromMachineID
		{
			transportJobInq.setInquiryType(BizConstant.SP_TRANSFERJOB_INQ_TYPE_F);
			transportJobInq.setFromMachineID(fromMachineID);
		}
		else if(ObjectIdentifier.isNotEmptyWithValue(fromStockerID)) // fromStockerID
		{
			transportJobInq.setInquiryType(BizConstant.SP_TRANSFERJOB_INQ_TYPE_F);
			transportJobInq.setFromMachineID(fromStockerID);
		}

		TmsTransportJobInqParam tmsParam = new TmsTransportJobInqParam().from(inputParam);
		//transfer to tms
		TmsRequest tmsRequest=new TmsRequest();
		tmsRequest.setFunctionId(TmsTransactionIDEnum.ROM14.getValue());
		tmsRequest.setMessageID(UUID.randomUUID().toString());
		tmsRequest.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
		tmsRequest.setMessageBody(JSON.toJSONString(tmsParam));
		String requestJson= JSONObject.toJSONString(tmsRequest);
		log.info("call tms request :{} " , requestJson);

		Response response = null;
		try {
			response = xferRemoteManager.rtransportJobInq(tmsParam);
		} catch(CimIntegrationException e) {
			Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
		}
		Results.TransportJobInqResult transportJobInqResult = JSON.parseObject(response.getBody().toString(), Results.TransportJobInqResult.class);

		Outputs.SendTransportJobInqOut out = new Outputs.SendTransportJobInqOut();
		out.setStrTransportJobInqResult(transportJobInqResult);
		return out;
	}

	@Override
	public Outputs.SendTransportJobCancelReqOut transportJobCancelReq(Inputs.SendTransportJobCancelReqIn param) {
		TmsTransportJobCancelReqParam tmsParam = new TmsTransportJobCancelReqParam().from(param);

		//transfer to tms
		TmsRequest tmsRequest=new TmsRequest();
		tmsRequest.setFunctionId(TmsTransactionIDEnum.OM04.getValue());
		tmsRequest.setMessageID(UUID.randomUUID().toString());
		tmsRequest.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
		tmsRequest.setMessageBody(JSON.toJSONString(tmsParam));
		String requestJson= JSONObject.toJSONString(tmsRequest);
		log.info("call tms request :{} " , requestJson);

		Response response = null;
		try {
			response = xferRemoteManager.transportJobCancelReq(tmsParam);
		} catch(CimIntegrationException e) {
			Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
		}

		Results.TransportJobCancelReqResult transportJobCancelReqResult = JSON.parseObject(response.getBody().toString(), Results.TransportJobCancelReqResult.class);
		Outputs.SendTransportJobCancelReqOut out = new Outputs.SendTransportJobCancelReqOut();
		out.setStrTransportJobCancelReqResult(transportJobCancelReqResult);
		return out;
	}

	@Override
	public Outputs.SendTransportJobCancelReqOut rtransportJobCancelReq(Inputs.SendTransportJobCancelReqIn param) {
		TmsTransportJobCancelReqParam tmsParam = new TmsTransportJobCancelReqParam().from(param);

		//transfer to tms
		TmsRequest tmsRequest=new TmsRequest();
		tmsRequest.setFunctionId(TmsTransactionIDEnum.ROM04.getValue());
		tmsRequest.setMessageID(UUID.randomUUID().toString());
		tmsRequest.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
		tmsRequest.setMessageBody(JSON.toJSONString(tmsParam));
		String requestJson= JSONObject.toJSONString(tmsRequest);
		log.info("call tms request :{} " , requestJson);

		Response response = null;
		try {
			response = xferRemoteManager.rtransportJobCancelReq(tmsParam);
		} catch(CimIntegrationException e) {
			Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
		}

		Results.TransportJobCancelReqResult transportJobCancelReqResult = JSON.parseObject(response.getBody().toString(), Results.TransportJobCancelReqResult.class);
		Outputs.SendTransportJobCancelReqOut out = new Outputs.SendTransportJobCancelReqOut();
		out.setStrTransportJobCancelReqResult(transportJobCancelReqResult);
		return out;
	}


	@Override
	public Outputs.SendStockerDetailInfoInqOut stockerDetailInfoInq(Infos.ObjCommon objCommon, User user, Inputs.StockerDetailInfoInq param) {
		TmsStockerDetailInfoInqParam tmsParam = new TmsStockerDetailInfoInqParam().from(param);
		tmsParam.setRequestUserID(new TmsUser().from(user));
		//transfer to tms
		TmsRequest tmsRequest=new TmsRequest();
		tmsRequest.setFunctionId(TmsTransactionIDEnum.OM10.getValue());
		tmsRequest.setMessageID(UUID.randomUUID().toString());
		tmsRequest.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
		tmsRequest.setMessageBody(JSON.toJSONString(tmsParam));
		String requestJson= JSONObject.toJSONString(tmsRequest);
		log.info("call tms request :{} " , requestJson);

		Response response = null;
		try {
			response = xferRemoteManager.stockerDetailInfoInq(tmsParam);
		} catch(CimIntegrationException e) {
			Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
		}

		Results.StockerDetailInfoInqResult stockerDetailInfoInqResult = JSON.parseObject(response.getBody().toString(), Results.StockerDetailInfoInqResult.class);
		Outputs.SendStockerDetailInfoInqOut out = new Outputs.SendStockerDetailInfoInqOut();
		out.setStrStockerDetailInfoInqResult(stockerDetailInfoInqResult);
		return out;
	}

	@Override
	public Results.PriorityChangeReqResult priorityChangeReq(Infos.ObjCommon objCommon, User user, Infos.PriorityChangeReq param) {
		TmsPriorityChangeReqParam tmsParam = new TmsPriorityChangeReqParam().from(param);
		tmsParam.setRequestUserID(new TmsUser().from(user));

		//transfer to tms
		TmsRequest tmsRequest=new TmsRequest();
		tmsRequest.setFunctionId(TmsTransactionIDEnum.OM12.getValue());
		tmsRequest.setMessageID(UUID.randomUUID().toString());
		tmsRequest.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
		tmsRequest.setMessageBody(JSON.toJSONString(tmsParam));
		String requestJson= JSONObject.toJSONString(tmsRequest);
		log.info("call tms request :{} " , requestJson);

		Response response = null;
		try {
			response = xferRemoteManager.priorityChangeReq(tmsParam);
		} catch(CimIntegrationException e) {
			Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
		}

		Results.PriorityChangeReqResult priorityChangeReqResult = JSON.parseObject(response.getBody().toString(), Results.PriorityChangeReqResult.class);
		return priorityChangeReqResult;

		//TODO: If TMS return "No responce from AMHS",MM treat it as OK
	}

	@Override
	public Results.AmhsUploadInventoryReqResult uploadInventoryReq(Inputs.SendUploadInventoryReqIn param) {

		TmsUploadInventoryReqParam tmsParam = new TmsUploadInventoryReqParam().from(param);

		//transfer to tms
		TmsRequest tmsRequest=new TmsRequest();
		tmsRequest.setFunctionId(TmsTransactionIDEnum.OM09.getValue());
		tmsRequest.setMessageID(UUID.randomUUID().toString());
		tmsRequest.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
		tmsRequest.setMessageBody(JSON.toJSONString(tmsParam));
		String requestJson= JSONObject.toJSONString(tmsRequest);
		log.info("call tms request :{} " , requestJson);

		Response response = null;
		try {
			response = xferRemoteManager.uploadInventoryReq(tmsParam);
		} catch(CimIntegrationException e) {
			Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
		}

		Validations.isSuccessWithException(response);
		return JSON.parseObject(response.getBody().toString(), Results.AmhsUploadInventoryReqResult.class);
	}

}
