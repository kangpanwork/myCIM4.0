package com.fa.cim.common.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description:
 *      Override the HttpServletRequestWrapper to fix the params format.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/5/29        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/5/29 13:10
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class ParameterWrapper extends HttpServletRequestWrapper {
    private Map<String, String[]> params = new HashMap<>();

    public ParameterWrapper(HttpServletRequest request) {
        super(request);
        //this.params = new HashMap<>(request.getParameterMap());
        addParameters(request.getParameterMap());
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/29
     * @param extraParams -
     * @return void
     */
    public void addParameters(Map<String, String[]> extraParams) {
        for (Map.Entry<String, String[]> entry : extraParams.entrySet()) {
            addParameters(entry.getKey(), entry.getValue());
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/29
     * @param name -
     * @param value -
     * @return void
     */
    public void addParameters(String name, Object value) {
        name = fixName(name);
        if (null != value) {
            if (value instanceof String[]) {
                params.put(name, (String[]) value);
            } else if (value instanceof String) {
                params.put(name, new String[]{(String) value});
            } else {
                params.put(name, new String[]{String.valueOf(value)});
            }
        }
    }

    /**
     * description:
     *      fix name,
     *      for example:      X: String Type,  N: Numeric Type
     *          //form-table format
     *          X[X] -> X.X
     *          X[N][X] -> X[N].C
     *          X[X][N][X] -> X.X[N].X
     *          X[X][N][X][X] -> X.X[N].X.X
     *          X[X][X][N][C] -> X.X.X[N].X
     *
     *          // normal format
     *          X.X -> X.X
     *          X[N].X -> X[N].X
     *          X.X[N].X -> X.X[1].X
     *          X.X[N].X.X -> X.X[N].X.X
     *          X.X.X[N].X -> X.X.X[N].X
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/29
     * @param name -
     * @return java.lang.String
     */
    public String fixName(String name) {
        //String tmp = String.copyValueOf(name.toCharArray());
        int start = name.indexOf('[');
        String buf = "";
        String result = "";
        final int notFound = -1;
        while (notFound != start) {
            String head = name.substring(0, start);
            int end = name.indexOf(']');
            String middle = name.substring(start + 1, end);
            if (!isNumeric(middle)) {
                buf = String.format("%s%s.%s", buf, head, middle);
            } else {
                buf = String.format("%s%s[%s]", buf, head, middle);
            }
            name = name.substring(end + 1);
            start = name.indexOf('[');
        }

        if (name.length() != 0) {
            if (name.substring(0, 1).equals(".")) {
                buf = String.format("%s%s", buf, name);
            } else {
                buf = ("".equals(buf) ? String.format("%s", name) : String.format("%s.%s", buf, name));
            }
        }
        result = buf;
        return result;
    }

    /**
     * description:
     *      judge thee str is numeric, if yes then return true, otherwise return false
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/29
     * @param str -
     * @return boolean
     */
    private boolean isNumeric(String str) {
        if (null == str || 0 == str.length()) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
    /***********************************************  重写下述方法 ****************************************************/
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/29
     * @param name -
     * @return java.lang.String
     */
    @Override
    public String getParameter(String name) {
        String[] values = params.get(name);
        if (null == values || 0 == values.length) {
            return null;
        }
        return values[0];
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/29
     * @return java.util.Map<java.lang.String, java.lang.String[]>
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return params;
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/29
     * @return java.util.Enumeration<java.lang.String>
     */
    @Override
    public Enumeration<String> getParameterNames() {
        Vector<String> nameList = new Vector<String>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            nameList.add(entry.getKey());
        }
        return nameList.elements();
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/29
     * @param paramName -
     * @return java.lang.String[]
     */
    @Override
    public String[] getParameterValues(String paramName) {
        if (null == paramName || 0 == paramName.length()) {
            return  null;
        }
        return params.get(paramName);
    }
}
