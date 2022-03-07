package com.fa.cim.common.utils;

import com.fa.cim.common.code.CodeTemplate;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.RetCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * This Validations use to check business logic.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/27        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/6/27 16:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Validations {

    private Validations() {
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/10 14:40
     * @param expression
     * @param code -
     * @return void
     */
    public static void check(final boolean expression, final OmCode code) {
        if (expression) {
            throw new ServiceException(code.getCode(), code.getMessage(), ThreadContextHolder.getTransactionId());
        }
    }

    public static void check(final boolean expression, final CodeTemplate code) {
        if (expression) {
            throw new ServiceException(code.getCode(), code.getMessage(), ThreadContextHolder.getTransactionId());
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/10/10 11:31
     * @param expression
     * @param code
     * @param codeArgs -
     * @return void
     */
    public static void check(final boolean expression, OmCode code, Object... codeArgs) {
        if (expression) {
            List<String> strs = new ArrayList<>();
            for (Object obj : codeArgs) {
                if (obj instanceof ObjectIdentifier) {
                    strs.add(ObjectIdentifier.fetchValue((ObjectIdentifier) obj));
                } else {
                    strs.add(String.valueOf(obj));
                }
            }
            code = new OmCode(code, strs.toArray(new String[strs.size()]));
            throw new ServiceException(code.getCode(), code.getMessage(), ThreadContextHolder.getTransactionId());
        }
    }

    public static ServiceException buildException (OmCode code, Object... codeArgs) {
        List<String> strs = new ArrayList<>();
        for (Object obj : codeArgs) {
            if (obj instanceof ObjectIdentifier) {
                strs.add(ObjectIdentifier.fetchValue((ObjectIdentifier) obj));
            } else {
                strs.add(String.valueOf(obj));
            }
        }
        OmCode codeClone = new OmCode(code, strs.toArray(new String[strs.size()]));
        return new ServiceException(codeClone.getCode(), codeClone.getMessage(), ThreadContextHolder.getTransactionId());
    }

    public static void check(final boolean expression, Object data, OmCode code, Object... codeArgs) {
        if (expression) {
            List<String> strs = new ArrayList<>();
            for (Object obj : codeArgs) {
                if (obj instanceof ObjectIdentifier) {
                    strs.add(ObjectIdentifier.fetchValue((ObjectIdentifier) obj));
                } else {
                    strs.add(String.valueOf(obj));
                }
            }
            code = new OmCode(code, strs.toArray(new String[strs.size()]));
            throw new ServiceException(code.getCode(), code.getMessage(), ThreadContextHolder.getTransactionId(), data);
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/9/25 15:44
     * @param code -
     * @return void
     */
    public static void check(final OmCode code, Object... codeArgs) {
        check(true, code, codeArgs);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param expression expression
     * @param code       code
     * @param message    message
     * @author PlayBoy
     * @date 2018/7/30
     */
    public static void check(final boolean expression, final Integer code, final String message) {
        if (expression) {
            throw new ServiceException(code, message, ThreadContextHolder.getTransactionId());
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param expression
     * @param message
     * @return
     * @author PlayBoy
     * @date 2018/8/15
     */
    public static void check(final boolean expression, final String message) {
        if (expression) {
            throw new ServiceException(OmCode.ERROR_CODE, message, ThreadContextHolder.getTransactionId());
        }
    }

    public static void assertCheck(final boolean expression, final String message) {
        if (!expression) {
            throw new ServiceException(message);
        }
    }

    /**
     * description: if code is not success ,then throw ServiceException
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code
     * @param txId
     * @author PlayBoy
     * @date 2018/7/25
     */
    public static void isOK(final OmCode code, final String txId) {
        if (code == null) {
            throw new RuntimeException("CimCode is null");
        }
        if (!isSuccess(code)) {
            throw new ServiceException(code.getCode(), code.getMessage(), txId);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code code
     * @param txId txId
     * @param data data
     * @author PlayBoy
     * @date 2018/12/5 13:11:19
     */
    public static void isOK(final OmCode code, final String txId, final Object data) {
        if (code == null) {
            throw new RuntimeException("CimCode is null");
        }
        if (!isSuccess(code)) {
            throw new ServiceException(code.getCode(), code.getMessage(), txId, data);
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param codeNum codeNum
     * @author PlayBoy
     * @date 2018/6/29
     */
    public static boolean isSuccess(final Integer codeNum) {
        return OmCode.SUCCESS_CODE == codeNum;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param code   -
     * @param target -
     * @return boolean
     * @author Bear
     * @date 2018/10/29 13:34
     */
    public static boolean isEquals(OmCode code, RetCode target) {
        if (null == code || null == target || null == target.getReturnCode()) {
            return false;
        }
        return isEquals(code, target.getReturnCode());
    }

    public static boolean isEquals(CodeTemplate code, RetCode target) {
        if (null == code || null == target || null == target.getReturnCode()) {
            return false;
        }
        return isEquals(code, target.getReturnCode());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code   code
     * @param target target
     * @return boolean
     * @author PlayBoy
     * @date 2018/11/5 18:04:40
     */
    public static boolean isEquals(OmCode code, OmCode target) {
        if (null == code || null == target) {
            return false;
        }
        return code.getCode() == target.getCode();
    }

    public static boolean isEquals(CodeTemplate code, CodeTemplate target) {
        if (null == code || null == target) {
            return false;
        }
        return code.getCode() == target.getCode();
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/7/4 10:45
     * @param code
     * @param target -
     * @return boolean
     */
    public static boolean isEquals(OmCode code, int target) {
        if (code == null) {
            return false;
        }
        return code.getCode() == target;
    }

    public static boolean isEquals(int src, OmCode code) {
        if (code == null) {
            return false;
        }
        return src == code.getCode();
    }

    public static boolean equalsIn(int target, OmCode... code) {
        return null != code && code.length > 0 &&
                Arrays.stream(code).parallel().anyMatch(iCode -> iCode.getCode() == target);
    }

    public static boolean unequalsIn(int target, OmCode... code) {
        return !equalsIn(target,code);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code code
     * @author PlayBoy
     * @date 2018/6/29
     */
    public static boolean isSuccess(final OmCode code) {
        return code != null && OmCode.SUCCESS_CODE == code.getCode();
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param retCode retCode
     * @return boolean
     * @author PlayBoy
     * @date 2018/10/29 10:54:42
     */
    public static boolean isSuccess(RetCode retCode) {
        return null != retCode && isSuccess(retCode.getReturnCode());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param retCode
     * @return isNotSuccess
     * @author PlayBoy
     * @date 2018/11/21 13:26:36
     */
    public static boolean isNotSuccess(RetCode retCode) {
        return !isSuccess(retCode);
    }

    public static void isSuccessWithExceptionList(final Response response) {
        if (OmCode.SUCCESS_CODE != response.getCode()) {
            throw new ServiceException(response.getCode(), response.getMessage(), response.getTransactionID(), response.getBody());
        }
    }

    public static void isSuccessWithException(final Response response) {
        if (response.getCode() == null) {
            throw new ServiceException("CimCode is null");
        }
        if (OmCode.SUCCESS_CODE != response.getCode()) {
            throw new ServiceException(response.getCode(), response.getMessage(), response.getTransactionID());
        }
    }
}
