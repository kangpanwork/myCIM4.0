package com.fa.cim.idp.tms.adaptor.result;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.idp.tms.adaptor.TmsAdapt;
import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/3/27                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/3/27 15:51
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class TmsTransportJobInqResult implements TmsAdapt<Results.TransportJobInqResult,TmsTransportJobInqResult> {

    private static final long serialVersionUID = -2074983001210419109L;

    private String                                   inquiryType;
    private List<TmsTransportJobInqData>             jobInqData;
    private Object                                   siInfo;

    @Override
    public Results.TransportJobInqResult adapt() {
        Results.TransportJobInqResult result = new Results.TransportJobInqResult();
        result.setInquiryType(this.inquiryType);
        result.setSiInfo(this.siInfo);
        if (null != this.jobInqData) {
            List<Infos.TransportJobInqData> list = new ArrayList<>(this.jobInqData.size());
            result.setJobInqData(list);
            for (TmsTransportJobInqData data : this.jobInqData) {
                list.add(data.adapt());
            }
        }
        return result;
    }

    @Override
    public TmsTransportJobInqResult from(Results.TransportJobInqResult obj) {
        this.inquiryType = obj.getInquiryType();
        this.siInfo = obj.getSiInfo();
        if (null != obj.getJobInqData()) {
            List<Infos.TransportJobInqData> jobInqData = obj.getJobInqData();
            this.jobInqData = new ArrayList<>(obj.getJobInqData().size());
            for (Infos.TransportJobInqData data : jobInqData) {
                this.jobInqData.add(new TmsTransportJobInqData().from(data));
            }
        }
        return this;
    }

    @Data
    public static class TmsTransportJobInqData implements TmsAdapt<Infos.TransportJobInqData,TmsTransportJobInqData>{

        private static final long serialVersionUID = 9056337988157360094L;

        private String                                  jobID;
        private String                                  transportType;
        private String                                  jobStatus;
        private List<TmsCarrierJobInqInfo>              carrierJobInqInfo;
        private Object                                  siInfo;

        @Override
        public Infos.TransportJobInqData adapt() {
            Infos.TransportJobInqData result = new Infos.TransportJobInqData();
            result.setJobID(this.jobID);
            result.setTransportType(this.transportType);
            result.setJobStatus(this.jobStatus);
            result.setSiInfo(this.siInfo);
            if (null != this.carrierJobInqInfo){
                List<Infos.CarrierJobInqInfo> jobInqDataList = new ArrayList<>(this.carrierJobInqInfo.size());
                result.setCarrierJobInqInfo(jobInqDataList);
                for (TmsCarrierJobInqInfo data : this.carrierJobInqInfo) {
                    jobInqDataList.add(data.adapt());
                }
            }
            return result;
        }

        @Override
        public TmsTransportJobInqData from(Infos.TransportJobInqData obj) {
            this.jobID = obj.getJobID();
            this.transportType = obj.getTransportType();
            this.jobStatus = obj.getJobStatus();
            if (null != obj.getCarrierJobInqInfo()){
                List<Infos.CarrierJobInqInfo> carrierJobInqInfo = obj.getCarrierJobInqInfo();
                this.carrierJobInqInfo = new ArrayList<>(obj.getCarrierJobInqInfo().size());
                for (Infos.CarrierJobInqInfo jobInqInfo : carrierJobInqInfo) {
                    this.carrierJobInqInfo.add(new TmsCarrierJobInqInfo().from(jobInqInfo));
                }
            }
            return this;
        }

        @Data
        public static class TmsCarrierJobInqInfo implements TmsAdapt<Infos.CarrierJobInqInfo,TmsCarrierJobInqInfo> {

            private static final long serialVersionUID = 9056337988157360094L;

            private String carrierId;
            private String jobId;
            private String carrierJobId;
            private String transportType;
            private String zoneType;
            private Integer n2PurgeFlag;
            private String fromMachineId;
            private String fromPortId;
            private String toStockerGroup;
            private String toMachineId;
            private String toPortId;
            private String expectedStrtTime;
            private String expectedEndTime;
            private String estimateStrtTime;
            private String estimateEndTime;
            private Integer mandatoryFlag;
            private String priority;
            private String jobStatus;
            private String carrierJobStatus;
            private Timestamp timestp;

            @Override
            public Infos.CarrierJobInqInfo adapt() {
                Infos.CarrierJobInqInfo result = new Infos.CarrierJobInqInfo();
                result.setCarrierJobID(this.carrierJobId);
                result.setCarrierJobStatus(this.carrierJobStatus);
                result.setCarrierID(ObjectIdentifier.buildWithValue(this.carrierId));
                result.setZoneType(this.zoneType);
                result.setN2PurgeFlag((null !=this.n2PurgeFlag && this.n2PurgeFlag == 1) ? true : false);
                result.setFromMachineID(ObjectIdentifier.buildWithValue(this.fromMachineId));
                result.setFromPortID(ObjectIdentifier.buildWithValue(this.fromPortId));
                result.setToMachineID(ObjectIdentifier.buildWithValue(this.toMachineId));
                result.setToPortID(ObjectIdentifier.buildWithValue(this.toPortId));
                result.setExpectedStartTime(this.expectedStrtTime);
                result.setExpectedEndTime(this.expectedEndTime);
                result.setMandatoryFlag((null != this.mandatoryFlag && this.mandatoryFlag == 1) ? true : false);
                result.setPriority(this.priority);
                result.setEstimatedStartTime(this.estimateStrtTime);
                result.setEstimatedEndTime(this.estimateEndTime);
                result.setSiInfo("");
                return result;
            }

            @Override
            public TmsCarrierJobInqInfo from(Infos.CarrierJobInqInfo obj) {
                this.carrierJobId = obj.getCarrierJobID();
                this.carrierJobStatus = obj.getCarrierJobStatus();
                this.carrierId = ObjectIdentifier.fetchValue(obj.getCarrierID());
                this.zoneType = obj.getZoneType();
                this.n2PurgeFlag = obj.isN2PurgeFlag() == true ? 1 : 0;
                this.fromMachineId = ObjectIdentifier.fetchValue(obj.getFromMachineID());
                this.fromPortId = ObjectIdentifier.fetchValue(obj.getFromPortID());
                this.toMachineId = ObjectIdentifier.fetchValue(obj.getToMachineID());
                this.toPortId = ObjectIdentifier.fetchValue(obj.getToPortID());
                this.expectedStrtTime = obj.getExpectedStartTime();
                this.expectedEndTime = obj.getExpectedEndTime();
                this.mandatoryFlag = obj.isMandatoryFlag() == true ? 1 : 0;
                this.priority = obj.getPriority();
                this.estimateStrtTime = obj.getEstimatedStartTime();
                this.estimateEndTime = obj.getEstimatedEndTime();
                return this;
            }
        }
    }
}
