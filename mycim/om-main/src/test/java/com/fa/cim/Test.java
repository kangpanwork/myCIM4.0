package com.fa.cim;

import org.testng.TestNG;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/30          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/6/30 9:59
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Test {
    public static void main(String[] args) {
        TestNG testNG = new TestNG();
        List<String> suites = new ArrayList<>();
        List<String> testNames = new ArrayList<>();
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        System.out.println(path);
        suites.add(path + "/oms-testng.xml");
        testNames.add("scrap-testcase");
        testNames.add("lotholdrelease-testcase");
        testNG.setTestSuites(suites);
        testNG.setTestNames(testNames);
        testNG.run();
    }
}