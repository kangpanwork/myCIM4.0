//package com.fa.cim.filter;
//
//import com.fa.cim.common.support.ParameterWrapper;
//import com.fa.cim.intercept.CimHttpServletRequestWrapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.annotation.Order;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//
///**
// * description:
// * DefaultFilter .
// * change history:
// * date             defect#             person             comments
// * ---------------------------------------------------------------------------------------------------------------------
// * 2018/8/1        ********             PlayBoy               create file
// *
// * @author: PlayBoy
// * @date: 2018/8/1 17:42
// * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
// */
//@Order(0)
//@WebFilter(filterName = "DefaultFilter", urlPatterns = "/*")
//@Slf4j
//public class DefaultFilter implements Filter {
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        log.debug("DefaultFilter: enter doFilter()");
//        ServletRequest requestWrapper = null;
//        if (request instanceof HttpServletRequest) {
//            requestWrapper = new CimHttpServletRequestWrapper(new ParameterWrapper((HttpServletRequest) request));
//        }
//        if (requestWrapper == null) {
//            chain.doFilter(request, response);
//        } else {
//            //put CimHttpServletRequestWrapper to next filter,Handling ServletInputStream can only be read once
//            chain.doFilter(requestWrapper, response);
//        }
//    }
//
//    @Override
//    public void destroy() {
//
//    }
//}
