package com.fa.cim.excel;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * description:
 * <p>ExcelTest .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/18         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/11/18 17:13
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
//@ContextConfiguration
@ContextConfiguration
@Slf4j
public class OMS_API_EXCEL {

    private static final String FILE_PATH = "C:\\Users\\Administrator\\Desktop\\OMS API.xlsx";

    private static final String CORE_PACKAGE_INFO_NAME = "com.fa.cim.dto";
    private static final String CORE_PACKAGE_CONTROLLER_NAME = "com.fa.cim.controller";


    @Test
    public void generateInfosInterfaceExcel() {
        List<ExcelMap> values = this.generateInfoExcelValue(CORE_PACKAGE_INFO_NAME);
        boolean isSuccess = ExcelUtils.writeInfoExcel(FILE_PATH, "Infos", getInfosExcelTitles(), values);
        System.out.println("Create Excel Success ? | " + isSuccess);
    }

    @Test
    public void generateControllerInterfaceExcel() {
        List<ExcelMap> values = this.generateControllerExcelValue(CORE_PACKAGE_CONTROLLER_NAME);
        boolean isSuccess = ExcelUtils.writeControllerExcel(FILE_PATH, "Controller", getControllerExcelTitles(), values);
        System.out.println("Create Excel Success ? | " + isSuccess);
    }

    private List<ExcelMap> generateControllerExcelValue(String packageName) {
        Set<Class<?>> classes = ExcelUtils.getClasses(packageName);
        List<ExcelMap> values = new ArrayList<>();
        Iterator<Class<?>> classIterator = classes.iterator();
        for (int i = 0;i<classes.size();i++){
            String className = classIterator.next().getName();
            Integer tst1 = className.indexOf(".interfaces.");
            Integer tst2 = className.indexOf(".tcc.");
            if (tst1 == -1 && tst2 == -1){
            }else {
                classIterator.remove();
                i--;
            }
        }
        classes.forEach(data -> {
            Method[] declaredMethods = data.getDeclaredMethods();
            for (Method method : declaredMethods){
                if (method.getModifiers()!=1){
                    break;
                }
                String classUrl = data.getDeclaredAnnotation(RequestMapping.class).value()[0];
                String simpleName = method.getName();
                ExcelMap entry = new ExcelMap();
                entry.setColumnName("Method name");
                entry.setColumnValue(simpleName);
                Class<?>[] parameterTypes = method.getParameterTypes();
                List<ExcelMap> excelMaps = new ArrayList<>();
                if (CimObjectUtils.isEmpty(parameterTypes)){
                    ExcelMap excelMap = new ExcelMap();
                    excelMap.setColumnName("Parameter name");
                    excelMap.setColumnValue("");
                    ExcelMap.Node node = new ExcelMap.Node();
                    node.setName("Data Type");
                    node.setValue("");
                    excelMap.setDataType(node);
                    entry.setMethods(excelMaps);
                }else {
                    Class<?> parameterType = parameterTypes[0];
                    Field[] declaredFields = parameterType.getDeclaredFields();
                    for (Field field : declaredFields){
                        ExcelMap excelMap = new ExcelMap();
                        String name = field.getName();
                        excelMaps.add(excelMap);
                        String type = field.getType().getSimpleName();
                        excelMap.setColumnName("Parameter name");
                        excelMap.setColumnValue(name);
                        ExcelMap.Node dataType = new ExcelMap.Node();
                        ExcelMap.Node url = new ExcelMap.Node();
                        ExcelMap.Node txID = new ExcelMap.Node();
                        if (CimStringUtils.equals(type,"List")){
                            String fullType = field.getGenericType().getTypeName();
                            String temType = fullType.substring(fullType.lastIndexOf("$")+1);
                            type = type + "<" + temType;
                        }
                        type = type.replace("java.util.List<java.lang.","");
                        type = type.replace("java.util.List<com.fa.cim.common.support.","");
                        dataType.setName("Data type");
                        dataType.setValue(type);
                        if (!CimObjectUtils.isEmpty(method.getDeclaredAnnotation(RequestMapping.class))){
                            String urlValue = method.getDeclaredAnnotation(RequestMapping.class).value()[0];
                            urlValue = classUrl+urlValue;
                            url.setName("URL");
                            url.setValue(urlValue);
                        }else {
                            url.setName("URL");
                            url.setValue("");
                        }

                        if (!CimObjectUtils.isEmpty(method.getDeclaredAnnotation(CimMapping.class))){
                            TransactionIDEnum[] IDValues = method.getDeclaredAnnotation(CimMapping.class).value();
                            String txValue = "";
                            if(IDValues.length>1){
                                for (TransactionIDEnum id:IDValues){
                                    txValue = txValue + id.getValue()+" ";
                                }
                            }else {
                                txValue = IDValues[0].getValue();
                            }

                            txID.setName("txID");
                            txID.setValue(txValue);
                        }else {
                            txID.setName("txID");
                            txID.setValue("");
                        }
                        excelMap.setTransactionID(txID);
                        excelMap.setURL(url);
                        excelMap.setDataType(dataType);
                        entry.setMethods(excelMaps);
                    }
                    values.add(entry);
                }
            }
            /*Method[] declaredMethods = data.getDeclaredMethods();
            for (Method method : declaredMethods) {
                com.fa.cim.excel.ExcelMap excelMap = new com.fa.cim.excel.ExcelMap();
                excelMaps.add(excelMap);
                excelMap.setColumnName("Data Type");
                excelMap.setColumnValue(method.getName());

                Class<?>[] parameterTypes = method.getParameterTypes();
                com.fa.cim.excel.ExcelMap.Node node = new com.fa.cim.excel.ExcelMap.Node();
                excelMap.setParams(node);
                if (ObjectUtils.isEmpty(parameterTypes)){
                    node.setName("Description");
                    node.setValue("");
                }else {
                    String fullParams = parameterTypes[0].getName();
                    String param = fullParams.substring(fullParams.indexOf("$")+1);
                    node.setName("Description");
                    node.setValue(param);
                }
            }*/
        });
        return values;
    }

    private List<ExcelMap> generateInfoExcelValue(String packageName) {
        Set<Class<?>> classes = ExcelUtils.getClasses(packageName);
        List<ExcelMap> values = new ArrayList<>();
        Iterator<Class<?>> classIterator = classes.iterator();
        for (int i = 0;i<classes.size();i++){
            String className = classIterator.next().getName();
            Integer tst1 = className.indexOf("com.fa.cim.dto.Infos");
            if (tst1 == -1){
                classIterator.remove();
                i--;
            }else {
            }
        }
        classes.forEach(data -> {
            ExcelMap entry = new ExcelMap();
            String fullName = data.getSimpleName();
            Field[] declaredFields = data.getDeclaredFields();
            List<ExcelMap> excelMaps = new ArrayList<>();
            if (CimObjectUtils.isEmpty(declaredFields)){
            }else {
                entry.setColumnName("Parameter class");
                entry.setColumnValue(fullName);
                for (Field field : declaredFields){
                    String simpleName = field.getName();
                    ExcelMap excelMap = new ExcelMap();
                    excelMaps.add(excelMap);
                    excelMap.setColumnName("Parameter name");
                    excelMap.setColumnValue(simpleName);
                    Class<?> paramsType = field.getType();
                    String type = paramsType.getSimpleName();
                    ExcelMap.Node node = new ExcelMap.Node();
                    if (CimStringUtils.equals(type,"List")){
                        String fullType = field.getGenericType().getTypeName();
                        String temType = fullType.substring(fullType.lastIndexOf("$")+1);
                        type = type + "<" + temType;
                    }
                    type  = type.replace("java.util.List<java.lang.","");
                    type  = type.replace("java.util.List<com.fa.cim.common.support.","");
                    node.setName("Data type");
                    node.setValue(type);
                    excelMap.setDataType(node);
                }
                entry.setMethods(excelMaps);
                values.add(entry);
            }
            /*Method[] declaredMethods = data.getDeclaredMethods();
            for (Method method : declaredMethods) {
                com.fa.cim.excel.ExcelMap excelMap = new com.fa.cim.excel.ExcelMap();
                excelMaps.add(excelMap);
                excelMap.setColumnName("Data Type");
                excelMap.setColumnValue(method.getName());

                Class<?>[] parameterTypes = method.getParameterTypes();
                com.fa.cim.excel.ExcelMap.Node node = new com.fa.cim.excel.ExcelMap.Node();
                excelMap.setParams(node);
                if (ObjectUtils.isEmpty(parameterTypes)){
                    node.setName("Description");
                    node.setValue("");
                }else {
                    String fullParams = parameterTypes[0].getName();
                    String param = fullParams.substring(fullParams.indexOf("$")+1);
                    node.setName("Description");
                    node.setValue(param);
                }
            }*/
        });
        return values;
    }

    private List<String> getInfosExcelTitles() {
        List<String> titles = new ArrayList<>();
        titles.add("Parameter class");
        titles.add("Parameter name");
        titles.add("Data type");
        titles.add("Description");
        return titles;
    }

    private List<String> getControllerExcelTitles() {
        List<String> titles = new ArrayList<>();
        titles.add("txID");
        titles.add("Method name");
        titles.add("URL");
        titles.add("Parameter name");
        titles.add("Data type");
        titles.add("Description");
        return titles;
    }

    private List<String> getConstantExcelTitles() {
        List<String> titles = new ArrayList<>();
        titles.add("Key");
        titles.add("valve");
        return titles;
    }


    private static final String CORE_PACKAGE_CONSTANT_NAME = "com.fa.cim.common.constant";
    private static final String FILE_CONSTANT_PATH = "E:\\OMS Constant.xlsx";

    @Test
    public void jerryTest() {
        Map<String, Map<String, Object>> values = this.generateConstantExcelValue(CORE_PACKAGE_CONSTANT_NAME);
        boolean isSuccess = ExcelUtils.writeConstantExcel(FILE_CONSTANT_PATH, this.getConstantExcelTitles(), values);
        System.out.println("Create Excel Success ? | " + isSuccess);

    }
    private Map<String, Map<String, Object>> generateConstantExcelValue(String packageName) {
        Set<Class<?>> classes = ExcelUtils.getClasses(packageName);
        Map<String, Map<String, Object>> values = new HashMap<>();
        Iterator<Class<?>> classIterator = classes.iterator();

        for (int i = 0;i<classes.size();i++){
            String className = classIterator.next().getName();
            Boolean b = className.contains("Enum");
            if (b){
                classIterator.remove();
                i--;
            }else {
            }
        }
        classes.forEach(data -> {
            String fullName = data.getSimpleName();
            Map<String, Object> value = new HashMap<>();
            Field[] declaredFields = data.getDeclaredFields();
            if (CimObjectUtils.isEmpty(declaredFields)){
            }else {
                for (Field field : declaredFields){
                    try {
                        String dataName = field.getName();
                        Object dataValue  = field.get(data);
                        value.put(dataName,dataValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            values.put(fullName, value);
        });
        return values;
    }
}
