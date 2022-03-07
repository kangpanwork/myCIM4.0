package com.fa.cim.idp.tms.adaptor.param;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.idp.tms.adaptor.TmsAdapt;
import com.fa.cim.idp.tms.adaptor.common.TmsIdentifier;
import com.fa.cim.idp.tms.adaptor.common.TmsUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>TmsTransportJobCreateReqParam .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/12        ********             Yuri               create file
 *
 * @author: Yuri
 * @date: 2019/4/12 15:49
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class TmsTransportJobCreateReqParam implements TmsAdapt<Params.TransportJobCreateReqParams, TmsTransportJobCreateReqParam> {

	private static final long serialVersionUID = 3988761238866900711L;
	private List<TmsJobCreateData> jobCreateData;
	private String jobID;
	private Boolean rerouteFlag;
	private String transportType;
	private TmsUser requestUserID;

	@Override
	public Params.TransportJobCreateReqParams adapt() {
		Params.TransportJobCreateReqParams result = new Params.TransportJobCreateReqParams();
		result.setJobID(this.jobID);
		result.setRerouteFlag(this.rerouteFlag);
		result.setTransportType(this.transportType);
		if (null != this.jobCreateData) {
			List<Infos.JobCreateArray> jobCreateData = new ArrayList<>(this.jobCreateData.size());
			result.setJobCreateData(jobCreateData);
			for (TmsJobCreateData data : this.jobCreateData) {
				jobCreateData.add(data.adapt());
			}
		}
		return result;
	}

	@Override
	public TmsTransportJobCreateReqParam from(Params.TransportJobCreateReqParams obj) {
		this.jobID = obj.getJobID();
		this.rerouteFlag = obj.isRerouteFlag();
		this.transportType = obj.getTransportType();
		if (null != obj.getJobCreateData()) {
			List<Infos.JobCreateArray> jobCreateData = obj.getJobCreateData();
			this.jobCreateData = new ArrayList<>(jobCreateData.size());
			for (Infos.JobCreateArray data : jobCreateData) {
				this.jobCreateData.add(new TmsJobCreateData().from(data));
			}
		}
		return this;
	}

	@Data
	public static class TmsJobCreateData implements TmsAdapt<Infos.JobCreateArray, TmsJobCreateData>{
		private static final long serialVersionUID = -1373542357691327512L;
		private TmsIdentifier carrierID;
		private String carrierJobID;
		private String expectedEndTime;
		private String expectedStartTime;
		private TmsIdentifier fromMachineID;
		private TmsIdentifier fromPortID;
		private Boolean mandatoryFlag;
		private Boolean n2PurgeFlag;
		private String priority;
		private List<TmsToMachine> toMachine;
		private String toStockerGroup;
		private String zoneType;

		@Override
		public Infos.JobCreateArray adapt() {
			Infos.JobCreateArray result = new Infos.JobCreateArray();
			result.setCarrierID(this.carrierID.adapt());
			result.setCarrierJobID(this.carrierJobID);
			result.setExpectedEndTime(this.expectedEndTime);
			result.setExpectedStartTime(this.expectedStartTime);
			result.setFromMachineID(this.fromMachineID.adapt());
			result.setFromPortID(this.fromPortID.adapt());
			result.setMandatoryFlag(this.mandatoryFlag);
			result.setN2PurgeFlag(this.n2PurgeFlag);
			result.setPriority(this.priority);
			result.setToStockerGroup(this.toStockerGroup);
			result.setZoneType(this.zoneType);
			if (null != this.toMachine) {
				List<Infos.ToDestination> list = new ArrayList<>(this.toMachine.size());
				for (TmsToMachine data : this.toMachine) {
					list.add(data.adapt());
				}
				result.setToMachine(list);
			}
			return result;
		}

		@Override
		public TmsJobCreateData from(Infos.JobCreateArray obj) {
			this.carrierID = new TmsIdentifier().from(obj.getCarrierID());
			this.carrierJobID = obj.getCarrierJobID();
			this.expectedEndTime = obj.getExpectedEndTime();
			this.expectedStartTime = obj.getExpectedStartTime();
			this.fromMachineID = new TmsIdentifier().from(obj.getFromMachineID());
			this.fromPortID = new TmsIdentifier().from(obj.getFromPortID());
			this.mandatoryFlag = obj.getMandatoryFlag();
			this.n2PurgeFlag = obj.getN2PurgeFlag();
			this.priority = obj.getPriority();
			if (null != obj.getToMachine()) {
				List<Infos.ToDestination> fromList = obj.getToMachine();
				this.toMachine = new ArrayList<>(fromList.size());
				for (Infos.ToDestination data : fromList) {
					this.toMachine.add(new TmsToMachine().from(data));
				}
			}
			this.toStockerGroup = obj.getToStockerGroup();
			this.zoneType = obj.getZoneType();
			return this;
		}

		@Data
		public static class TmsToMachine implements TmsAdapt<Infos.ToDestination, TmsToMachine>{
			private static final long serialVersionUID = 4570908610187852948L;
			private TmsIdentifier toMachineID;
			private TmsIdentifier toPortID;

			@Override
			public Infos.ToDestination adapt() {
				Infos.ToDestination result = new Infos.ToDestination();
				result.setToMachineID(this.toMachineID.adapt());
				result.setToPortID(this.toPortID.adapt());
				return result;
			}

			@Override
			public TmsToMachine from(Infos.ToDestination obj) {
				this.toMachineID = new TmsIdentifier().from(obj.getToMachineID());
				this.toPortID = new TmsIdentifier().from(obj.getToPortID());
				return this;
			}
		}
	}

}
