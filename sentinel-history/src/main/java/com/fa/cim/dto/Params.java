package com.fa.cim.dto;

import com.fa.cim.Custom.List;
import lombok.Data;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/25 14:33:23
 */
public class Params {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/2/26 16:39:32
     */
    @Data
    public static class CreateLotOperationStartEventRecord {
        private Infos.LotOperationStartEventRecord lotOperationStartEventRecord;
        private List<Infos.UserDataSet> userDataSets;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/5/31 18:07
     */
    @Data
    public static class Param<T> {
        private T t;

        public Param() {

        }

        public Param(T t) {
            this.t=t;
        }

        public T getValue() {
            return t;
        }

        public void setValue(T t) {
            this.t=t;
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/6/1 14:34
     */
    @Data
    public  static class String {
        private java.lang.String value;

        public java.lang.String toString() {
            return value;
        }

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2019/3/1 16:42
     */
    @Data
    public static class CreateLotOperationCompleteEventRecord {
        private Infos.LotOperationCompleteEventRecord lotOperationCompleteEventRecord;
        private List<Infos.UserDataSet> userDataSets;
    }

}
