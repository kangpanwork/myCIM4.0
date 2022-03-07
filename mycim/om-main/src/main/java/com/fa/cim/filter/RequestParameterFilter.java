package com.fa.cim.filter;

/**
 * description: Check whether the incoming parameter has illegal characters such as ' , - , =
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/7        ********            AOKI                create file
 *
 * @author: AOKI
 * @date: 2021/7/7 10:43
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
/*
@WebFilter(filterName = "RequestParameterFilter",
        urlPatterns = {"/lot/lot_list/inq",
        "/lotstart/product_order_released_list/inq"})
public class RequestParameterFilter implements Filter {
    @Resource
    private RetCodeConfig retCodeConfig;
    private final String sqlValidation = "\\b(and|exec|insert|select|drop|grant|alter|delete|update|count|chr" +
            "|mid|master|truncate|char|declare|or)\\b|(\\=|;|\\+|')";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException {
        HttpServletRequest httpServerRequest = (HttpServletRequest) servletRequest;
        String contentType = httpServerRequest.getContentType();
        Pattern compile = Pattern.compile(sqlValidation);
        // contentype is application/json
        if (CimStringUtils.equalsIgnoreCase("application/json",contentType)){
            String jsonString = IOUtils.toString(httpServerRequest.getInputStream(), "UTF-8");
            Map parameterMap = JSON.parseObject(jsonString, Map.class);
            Set<Map.Entry<String,Object>> sets = parameterMap.entrySet();
            for (Map.Entry entry : sets) {
                Object paramter = entry.getValue();
                Matcher matcher = compile.matcher(paramter.toString().toLowerCase());
                boolean existFlag = matcher.find();
                Validations.check(existFlag, retCodeConfig.getInvalidParameter());
            }
        } else {

            Enumeration<String> parameterNames = httpServerRequest.getParameterNames();
            while (parameterNames.hasMoreElements()){
                String parameterName = parameterNames.nextElement();
                String parameter = httpServerRequest.getParameter(parameterName);
                Matcher matcher = compile.matcher(parameter.toLowerCase());
                boolean existFlag = matcher.find();
                Validations.check(existFlag,retCodeConfig.getInvalidParameter());

            }
        }

    }
}
*/
