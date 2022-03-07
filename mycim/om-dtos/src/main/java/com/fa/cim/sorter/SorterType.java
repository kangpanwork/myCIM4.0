package com.fa.cim.sorter;

import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.newcore.dto.base.CimBaseEnum;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * <p>
 * change history:
 * date            defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/6/23        init                 Jerry               create file
 *
 * @author: Jerry
 * @date: 2021/6/23 2:36 下午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class SorterType {

    public enum Action implements CimBaseEnum<Action> {
        Undefined,
        WaferStart,
        LotTransfer,
        Reset,
        Flip,
        WaferSlotMapAdjust,
        RFIDRead,
        RFIDWrite,
        WaferEnd,
        AdjustByMES,
        AdjustByTool,
        Rotate_,
        T7CodeRead,
        Combine,
        Separate,
        ;

        @Override
        public Action[] allValues() {
            return Action.values();
        }

        @Override
        public Action undefined() {
            return Action.Undefined;
        }

        public static boolean equalsActionCode(String sourceActionCode) {
            if (StringUtils.isEmpty(sourceActionCode)) {
                return false;
            }
            Action[] values = Action.values();
            List<String> values2 = new ArrayList<>();
            for (int i = 0; i <values.length ; i++) {
                if(values[i].equals(Action.Undefined.getValue())){
                    continue;
                }
                values2.add(values[i].getValue());
            }
            for (String destinationActionCode : values2) {
                if ( sourceActionCode.contains("Rotate_")){
                    //特殊处理像Rotate_50这种情况
                    String[] degree = sourceActionCode.split("_",-1);
                    if(StringUtils.isEmpty(degree[1])){
                        return false;
                    }else {
                        try {
                            Integer.valueOf(degree[1]);
                        }catch (Exception e) {
                            return false;
                        }
                    }
                    if (CimStringUtils.equals(degree[0], "Rotate")) {
                        return true;
                    }
                }
                if (destinationActionCode.equals(sourceActionCode)){
                    return true;
                }
            }
            return false;
        }

        public static String getSpecifiedValue(String sourceActionCode) {
            StringBuffer value = new StringBuffer();
            if (sourceActionCode.contains("Rotate_")) {
                String[] degree = sourceActionCode.split("_", -1);
                if (!StringUtils.isEmpty(degree[1])) {
                    value.append(degree[1]);
                }
            }
            return value.toString();
        }
    }

    public enum Status implements CimBaseEnum<Status> {
        Undefined,
        Created,
        Xfer,
        Executing,
        Completed,
        Error,
        Canceled,//对于未执行的Sorter Job,用户可以选择Canceled取消执行操作
        Aborted,//当EAP 或者设备down机时， 人工将Sorter Job从 Executing 切换至Aborted状态
        ForceCompleted,//当出现实物已经完成Sorter job,但是MES 没有收到CJ completed event， 用户可以手动切换sorter job 为completed状态
        ;

        public static boolean equalsJobStatus(String jobStatus) {
            if (StringUtils.isEmpty(jobStatus)) {
                return false;
            }
            Status[] status1 = Status.values();
            List<String> status2=new ArrayList<>();
            for (int i = 0; i <status1.length ; i++) {
                if(status1[i].equals(Status.Undefined.getValue())){
                    continue;
                }
                status2.add(status1[i].getValue());
            }
            for (String status : status2) {
                if (status.equals(jobStatus)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public Status[] allValues() {
            return Status.values();
        }

        @Override
        public Status undefined() {
            return Status.Undefined;
        }
    }

    public enum CarrierType implements CimBaseEnum<CarrierType> {
        Undefined,
        FOSB,
        ;

        @Override
        public CarrierType[] allValues() {
            return CarrierType.values();
        }

        @Override
        public CarrierType undefined() {
            return CarrierType.Undefined;
        }
    }


    public enum JobType implements CimBaseEnum<JobType> {
        Undefined,
        SorterJob,
        ComponentJob,
        ;

        @Override
        public JobType[] allValues() {
            return JobType.values();
        }

        @Override
        public JobType undefined() {
            return JobType.Undefined;
        }
    }

    public enum JobOperation {
        Undefined("Undefined"),
        SortJobCreate("Sort Job Create"),
        SortJobStart("Sort Job Start"),
        SortJobComp("Sort Job Comp"),
        SortJobError("Sort Job Error"),
        SortJobCanceled("Sort Job Canceled"),
        SortJobAborted("Sort Job Aborted"),
        SortJobForceCompleted("Sort Job ForceCompleted"),
        SortJobDeleted("Sort Job Deleted"),
        ComponentJobCreate("Component Job Create"),
        ComponentJobStart("Component Job Start"),
        ComponentJobComp("Component Job Comp"),
        ComponentJobError("Component Job Error"),
        ComponentJobCanceled("Component Job Canceled"),
        ComponentJobAborted("Component Job Aborted"),
        ComponentJobForceCompleted("Component Job ForceCompleted"),
        ComponentJobDeleted("Component Job Deleted"),
        ;

        private String value;

        JobOperation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum JobDirection implements CimBaseEnum<JobDirection> {
        Undefined,
        OMS,
        EAP,
        ;

        @Override
        public JobDirection[] allValues() {
            return JobDirection.values();
        }

        @Override
        public JobDirection undefined() {
            return JobDirection.Undefined;
        }
    }


    public enum EapRepProcess implements CimBaseEnum<EapRepProcess> {
        Undefined,
        Req,
        Rpt,
        ;

        @Override
        public EapRepProcess[] allValues() {
            return EapRepProcess.values();
        }

        @Override
        public EapRepProcess undefined() {
            return EapRepProcess.Undefined;
        }
    }

    public enum CompareResult implements CimBaseEnum<CompareResult> {
        Undefined,
        Match,
        UnMatch,
        Unknown,
        ;

        @Override
        public CompareResult[] allValues() {
            return CompareResult.values();
        }

        @Override
        public CompareResult undefined() {
            return CompareResult.Undefined;
        }
    }


    public enum OperationMode  {
        Semi_1("Semi-1"),
        Auto_1("Auto-1"),
        Auto_2("Auto-2"),
        Auto_3("Auto-3"),
        ;
        private String value;

        OperationMode(String value) {
            this.value=value;
        }

       public String getValue(){
            return value;
       }
    }
}
