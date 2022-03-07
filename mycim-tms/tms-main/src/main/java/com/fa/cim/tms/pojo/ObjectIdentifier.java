package com.fa.cim.tms.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * description:
 * <p>ObjectIdentifier .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/3        ********             miner               create file
 *
 * @author: miner
 * @date: 2018/12/3 13:40
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Getter
@Setter
@ToString
public class ObjectIdentifier {

    /**
     * business value
     */
    private String value;
    /**
     * primary key value
     */
    private String referenceKey;

    public ObjectIdentifier() {

    }

    public ObjectIdentifier(String value, String referenceKey) {
        this.value = value;
        this.referenceKey = referenceKey;
    }

    public ObjectIdentifier(String value) {
        this.value = value;
    }

    public static ObjectIdentifier buildWithValue(String value) {
        return new ObjectIdentifier(value, null);
    }

    public static ObjectIdentifier buildWithRefkey(String referenceKey) {
        return new ObjectIdentifier(null, referenceKey);
    }

    public static ObjectIdentifier build(String value, String referenceKey) {
        return new ObjectIdentifier(value, referenceKey);
    }

    public static String fetchValue(ObjectIdentifier objectIdentifier) {
        return objectIdentifier == null ? null : objectIdentifier.getValue();
    }

    public static String fetchReferenceKey(ObjectIdentifier objectIdentifier) {
        return objectIdentifier == null ? null : objectIdentifier.getReferenceKey();
    }

    public static boolean isEmpty(ObjectIdentifier objectIdentifier) {
        return null == objectIdentifier ||
                (StringUtils.isEmpty(objectIdentifier.referenceKey) && StringUtils.isEmpty(objectIdentifier.value));
    }

    /**
     * description:
     * <p>use to fix old ObjectIdentifier equals issue</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param targetValue targetValue
     * @return boolean
     * @author Ho
     * @date 2018/12/11 15:08:02
     */
    public boolean equals(String targetValue) {
        if (this.value == null && targetValue == null) {
            return true;
        }
        if (!StringUtils.isEmpty(this.value)) {
            return this.value.equals(targetValue);
        }
        return StringUtils.isEmpty(targetValue);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param o object
     * @return boolean
     * @author miner
     * @date 2018/12/20 15:32:07
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ObjectIdentifier)) {
            return false;
        }
        ObjectIdentifier that = (ObjectIdentifier) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(referenceKey, that.referenceKey);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return hashCode
     * @author miner
     * @date 2018/12/20 15:32:10
     */
    @Override
    public int hashCode() {
        return Objects.hash(value, referenceKey);
    }

}
