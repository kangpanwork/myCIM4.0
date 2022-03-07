package com.fa.cim.excel;

import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.Nullable;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * description:
 * <p>com.fa.cim.excel.ExcelUtils .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/18         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/11/18 16:47
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
public class ExcelUtils {

    /**
     * Create Excel
     *
     * @param filePath  filepath
     * @param sheetName sheet name
     * @param titles    title name
     * @param values    values
     * @return boolean
     * @author ZQI
     */
    public static boolean writeControllerExcel(String filePath, @Nullable String sheetName, List<String> titles, List<ExcelMap> values) {
        HashMap<String, Integer> infos = new HashMap<>();
        //导入infos表做超链接
        try {
            Workbook workbook = readExcel(filePath);
            Sheet sheetInfos = workbook.getSheet("Infos");
            int index = 0;
            int rowNum = sheetInfos.getPhysicalNumberOfRows();
            for (int row=0 ; row< rowNum ; row++){
                Row rowInfo = sheetInfos.getRow(row);
                Cell InfoCell = rowInfo.getCell(index);
                String infoName = InfoCell.getStringCellValue();
                infos.put(infoName,row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean isSuccess = false;
        if (CimStringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("File Path is null.");
        }
        String suffix = getSuffix(filePath);
        if (CimStringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("Suffix is null.");
        }

        Workbook workbook = readExcel(filePath);
        if (null == workbook) {
            if ("xls".equals(suffix.toLowerCase())) {
                workbook = new HSSFWorkbook();
            } else {
                workbook = new XSSFWorkbook();
            }
        }

        // create a sheet.
        Sheet sheet;
        if (CimStringUtils.isEmpty(sheetName)) {
            sheet = workbook.createSheet();
        } else {
            sheet = workbook.createSheet(sheetName);
        }
        sheet.autoSizeColumn(1, true);
        /*sheet.setDefaultColumnWidth((short) 15);*/
        // generate style.
        Map<String, CellStyle> styles = createStyles(workbook);
        // create Row Title.
        Row row = sheet.createRow(0);
        Map<String, Integer> titleOrder = Maps.newHashMap();
        for (int i = 0; i < titles.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(styles.get("header"));
            String title = titles.get(i);
            cell.setCellValue(title);
            titleOrder.put(title, i);
        }

        //设置字体
        CellStyle normalCellStyle1 = workbook.createCellStyle();
        Font normalFont1 = workbook.createFont();
        normalFont1.setFontName(HSSFFont.FONT_ARIAL);
        normalFont1.setFontHeightInPoints((short) 11);
        normalCellStyle1.setFont(normalFont1);
        normalCellStyle1.setWrapText(true);
        normalCellStyle1.setAlignment(HorizontalAlignment.CENTER);
        normalCellStyle1.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle normalCellStyle2 = workbook.createCellStyle();
        Font normalFont2 = workbook.createFont();
        normalFont2.setFontName(HSSFFont.FONT_ARIAL);
        normalFont2.setFontHeightInPoints((short) 11);
        normalCellStyle2.setFont(normalFont2);
        normalCellStyle2.setAlignment(HorizontalAlignment.LEFT);
        normalCellStyle2.setVerticalAlignment(VerticalAlignment.CENTER);


        CellStyle linkCellStyle = workbook.createCellStyle();
        Font linkFont = workbook.createFont();
        linkFont.setColor(Font.COLOR_RED);
        linkFont.setFontHeightInPoints((short) 11);
        linkFont.setFontName(HSSFFont.FONT_ARIAL);
        linkFont.setUnderline((byte)1);
        linkCellStyle.setFont(linkFont);
        normalCellStyle2.setAlignment(HorizontalAlignment.LEFT);
        linkCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Main process.
        Iterator<ExcelMap> iterator = values.iterator();
        // row of excel and start from 1.
        int index = 1;
        while (iterator.hasNext()) {
            ExcelMap entry = iterator.next();

            List<ExcelMap> maps = entry.getMethods();
            if (null != maps && maps.size() > 0) {
                int methodsCount = 0;
                for (ExcelMap map : maps) {
                    row = sheet.createRow(index);
                    //设置TX
                    Cell txCell = row.createCell(titleOrder.get(map.getTransactionID().getName()));
                    txCell.setCellValue(map.getTransactionID().getValue());
                    txCell.setCellStyle(normalCellStyle1);
                    Cell entryCell = row.createCell(titleOrder.get(entry.getColumnName()));
                    entryCell.setCellValue(entry.getColumnValue());
                    entryCell.setCellStyle(normalCellStyle1);
                    //设置URL
                    Cell urlCell = row.createCell(titleOrder.get(map.getURL().getName()));
                    urlCell.setCellValue(map.getURL().getValue());
                    urlCell.setCellStyle(normalCellStyle1);
                    Cell mapCell = row.createCell(titleOrder.get(map.getColumnName()));
                    mapCell.setCellValue(map.getColumnValue());
                    mapCell.setCellStyle(normalCellStyle2);
                    Cell paramsCell = row.createCell(titleOrder.get(map.getDataType().getName()));
                    paramsCell.setCellStyle(normalCellStyle2);
                    String value = map.getDataType().getValue();
                    String tempValue = value.replace("List<","");
                    tempValue = tempValue.replace(">","");
                    Integer rowNumInInfos = infos.get(tempValue);
                    if (CimObjectUtils.isEmpty(rowNumInInfos)){
                    }else {
                        paramsCell.setCellType(CellType.FORMULA);
                        paramsCell.setCellStyle(linkCellStyle);
                        paramsCell.setCellFormula("HYPERLINK(\"#Infos!A"+rowNumInInfos.toString()+"\", \""+value+"\")");
                    }
                    paramsCell.setCellValue(value);
                    row.setHeight((short)400);
                    methodsCount++;
                    index++;
                }
                if (methodsCount > 1) {
                    CellRangeAddress region1 = new CellRangeAddress(index - methodsCount, index - 1, titleOrder.get("txID"), titleOrder.get("txID"));
                    CellRangeAddress region2 = new CellRangeAddress(index - methodsCount, index - 1, titleOrder.get(entry.getColumnName()), titleOrder.get(entry.getColumnName()));
                    CellRangeAddress region3 = new CellRangeAddress(index - methodsCount, index - 1, titleOrder.get("URL"), titleOrder.get("URL"));
                    sheet.addMergedRegion(region1);
                    sheet.addMergedRegion(region2);
                    sheet.addMergedRegion(region3);
                }
            } else {
                row = sheet.createRow(index);
                row.createCell(titleOrder.get(entry.getColumnName())).setCellValue(entry.getColumnValue());
                index++;
            }
        }

        sheet.autoSizeColumn((short)0);
        sheet.autoSizeColumn((short)1);
        sheet.autoSizeColumn((short)2);
        sheet.autoSizeColumn((short)3);
        sheet.autoSizeColumn((short)4);
        sheet.autoSizeColumn((short)5);


        try {
            OutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            isSuccess = true;
            outputStream.close();
            workbook.close();
        } catch (Exception e) {
            log.error("Create Excel file fail.");
            e.printStackTrace();
        }
        return isSuccess;
    }

    public static boolean writeInfoExcel(String filePath, @Nullable String sheetName, List<String> titles, List<ExcelMap> values) {
        HashMap<String, Integer> infos = new HashMap<>();

        boolean isSuccess = false;
        if (CimStringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("File Path is null.");
        }
        String suffix = getSuffix(filePath);
        if (CimStringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("Suffix is null.");
        }

        Workbook workbook = readExcel(filePath);
        if (null == workbook) {
            if ("xls".equals(suffix.toLowerCase())) {
                workbook = new HSSFWorkbook();
            } else {
                workbook = new XSSFWorkbook();
            }
        }

        // create a sheet.
        Sheet sheet;
        if (CimStringUtils.isEmpty(sheetName)) {
            sheet = workbook.createSheet();
        } else {
            sheet = workbook.createSheet(sheetName);
        }
        sheet.autoSizeColumn(1, true);
        /*sheet.setDefaultColumnWidth((short) 15);*/
        // generate style.
        Map<String, CellStyle> styles = createStyles(workbook);
        // create Row Title.
        Row row = sheet.createRow(0);
        Map<String, Integer> titleOrder = Maps.newHashMap();
        for (int i = 0; i < titles.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(styles.get("header"));
            String title = titles.get(i);
            cell.setCellValue(title);
            titleOrder.put(title, i);
        }

        //设置字体
        CellStyle normalCellStyle1 = workbook.createCellStyle();
        Font normalFont1 = workbook.createFont();
        normalFont1.setFontName(HSSFFont.FONT_ARIAL);
        normalFont1.setFontHeightInPoints((short) 11);
        normalCellStyle1.setFont(normalFont1);
        normalCellStyle1.setAlignment(HorizontalAlignment.CENTER);
        normalCellStyle1.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle normalCellStyle2 = workbook.createCellStyle();
        Font normalFont2 = workbook.createFont();
        normalFont2.setFontName(HSSFFont.FONT_ARIAL);
        normalFont2.setFontHeightInPoints((short) 11);
        normalCellStyle2.setFont(normalFont2);
        normalCellStyle2.setAlignment(HorizontalAlignment.LEFT);
        normalCellStyle2.setVerticalAlignment(VerticalAlignment.CENTER);


        CellStyle linkCellStyle = workbook.createCellStyle();
        Font linkFont = workbook.createFont();
        linkFont.setColor(Font.COLOR_RED);
        linkFont.setFontHeightInPoints((short) 11);
        linkFont.setFontName(HSSFFont.FONT_ARIAL);
        linkFont.setUnderline((byte)1);
        linkCellStyle.setFont(linkFont);
        normalCellStyle2.setAlignment(HorizontalAlignment.LEFT);
        linkCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Main process.
        Iterator<ExcelMap> iterator = values.iterator();
        // row of excel and start from 1.
        int index = 1;
        while (iterator.hasNext()) {
            ExcelMap entry = iterator.next();

            List<ExcelMap> maps = entry.getMethods();
            if (null != maps && maps.size() > 0) {
                int methodsCount = 0;
                for (ExcelMap map : maps) {
                    row = sheet.createRow(index);
                    Cell entryCell = row.createCell(titleOrder.get(entry.getColumnName()));
                    entryCell.setCellValue(entry.getColumnValue());
                    entryCell.setCellStyle(normalCellStyle1);
                    Cell mapCell = row.createCell(titleOrder.get(map.getColumnName()));
                    mapCell.setCellValue(map.getColumnValue());
                    mapCell.setCellStyle(normalCellStyle2);
                    Cell paramsCell = row.createCell(titleOrder.get(map.getDataType().getName()));
                    paramsCell.setCellStyle(normalCellStyle2);
                    String value = map.getDataType().getValue();
                    if (CimStringUtils.equals(sheetName,"Controller")){
                        String tempValue = value.replace("List<","");
                        tempValue = tempValue.replace(">","");
                        Integer rowNumInInfos = infos.get(tempValue);
                        if (CimObjectUtils.isEmpty(rowNumInInfos)){
                        }else {
                            paramsCell.setCellType(CellType.FORMULA);
                            paramsCell.setCellStyle(linkCellStyle);
                            paramsCell.setCellFormula("HYPERLINK(\"#Infos!A"+rowNumInInfos.toString()+"\", \""+value+"\")");
                        }
                    }
                    paramsCell.setCellValue(value);
                    row.setHeight((short)400);
                    methodsCount++;
                    index++;
                }
                if (methodsCount > 1) {
                    CellRangeAddress region = new CellRangeAddress(index - methodsCount, index - 1, titleOrder.get(entry.getColumnName()), titleOrder.get(entry.getColumnName()));
                    sheet.addMergedRegion(region);
                    /*CellRangeAddress region2 = new CellRangeAddress(index - methodsCount, index - 1, 1, 1);
                    sheet.addMergedRegion(region2);*/
                }
            } else {
                row = sheet.createRow(index);
                row.createCell(titleOrder.get(entry.getColumnName())).setCellValue(entry.getColumnValue());
                index++;
            }
        }

        if (CimStringUtils.equals(sheetName,"Infos")){
            HashMap<String, Integer> thisInfo = new HashMap<>();
            //根据创建好的表进行超链接
            int rowNum = sheet.getPhysicalNumberOfRows();
            for (int row1=1 ; row1< rowNum ; row1++){
                Row rowInfo = sheet.getRow(row1);
                Cell paramsClass = rowInfo.getCell(0);
                String className = paramsClass.getStringCellValue();
                thisInfo.put(className,row1);
            }

            for (int row1=0 ; row1< rowNum ; row1++){
                Row rowInfo = sheet.getRow(row1);
                rowInfo.setHeight((short)400);
                Cell type = rowInfo.getCell(2);
                if (!CimObjectUtils.isEmpty(type)){
                    String typevalue = type.getStringCellValue();
                    String tempValue = typevalue.replace("List<","");
                    tempValue = tempValue.replace(">","");
                    Integer rowNumInInfos = thisInfo.get(tempValue);
                    if (CimObjectUtils.isEmpty(rowNumInInfos)){
                    }else {
                        type.setCellType(CellType.FORMULA);
                        type.setCellFormula("HYPERLINK(\"#Infos!A"+rowNumInInfos.toString()+"\", \""+typevalue+"\")");
                        type.setCellStyle(linkCellStyle);
                    }
                    type.setCellValue(typevalue);
                }
            }
        }

        sheet.autoSizeColumn((short)0);
        sheet.autoSizeColumn((short)1);
        sheet.autoSizeColumn((short)2);
        sheet.autoSizeColumn((short)3);

        try {
            OutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            isSuccess = true;
            outputStream.close();
            workbook.close();
        } catch (Exception e) {
            log.error("Create Excel file fail.");
            e.printStackTrace();
        }
        return isSuccess;
    }



    /**
     * get the excel by the spec path.
     *
     * @param path path.
     * @return Workbook
     * @author ZQI
     */
    public static Workbook readExcel(String path) {
        if (CimStringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("Path is null.");
        }
        String fileType = path.substring(path.lastIndexOf(".") + 1);
        Workbook workbook = null;
        try {
            InputStream inputStream = new FileInputStream(path);
            switch (fileType) {
                case "xls":
                    workbook = new HSSFWorkbook(inputStream);
                    break;
                case "xlsx":
                    workbook = new XSSFWorkbook(inputStream);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("Not found excel file.And Return a null Object.");
        }
        return workbook;
    }

    /**
     * get file suffix.
     *
     * @param filepath filepath
     */
    private static String getSuffix(String filepath) {
        if (CimStringUtils.isEmpty(filepath)) {
            return "";
        }
        int index = filepath.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return filepath.substring(index + 1, filepath.length());
    }

    /**
     * set style.
     */
    private static Map<String, CellStyle> createStyles(Workbook wb) {
        Map<String, CellStyle> styles = Maps.newHashMap();

        XSSFCellStyle titleStyle = (XSSFCellStyle) wb.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setLocked(true);
        titleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);
        titleStyle.setFont(titleFont);
        styles.put("title", titleStyle);


        XSSFCellStyle headerStyle = (XSSFCellStyle) wb.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setWrapText(true);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        Font headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        styles.put("header", headerStyle);

        Font cellStyleFont = wb.createFont();
        cellStyleFont.setFontHeightInPoints((short) 12);
        cellStyleFont.setColor(IndexedColors.BLUE_GREY.getIndex());

        // 正文样式A
        XSSFCellStyle cellStyleA = (XSSFCellStyle) wb.createCellStyle();
        cellStyleA.setAlignment(HorizontalAlignment.CENTER);
        cellStyleA.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyleA.setWrapText(true);
        cellStyleA.setBorderRight(BorderStyle.THIN);
        cellStyleA.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setBorderLeft(BorderStyle.THIN);
        cellStyleA.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setBorderTop(BorderStyle.THIN);
        cellStyleA.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setBorderBottom(BorderStyle.THIN);
        cellStyleA.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setFont(cellStyleFont);
        styles.put("cellA", cellStyleA);

        // 正文样式B:添加前景色为浅黄色
        XSSFCellStyle cellStyleB = (XSSFCellStyle) wb.createCellStyle();
        cellStyleB.setAlignment(HorizontalAlignment.CENTER);
        cellStyleB.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyleB.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        cellStyleB.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyleB.setWrapText(true);
        cellStyleB.setBorderRight(BorderStyle.THIN);
        cellStyleB.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setBorderLeft(BorderStyle.THIN);
        cellStyleB.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setBorderTop(BorderStyle.THIN);
        cellStyleB.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setBorderBottom(BorderStyle.THIN);
        cellStyleB.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setFont(cellStyleFont);
        styles.put("cellB", cellStyleB);

        return styles;
    }

    /**
     * get all class form specified package.
     *
     * @param pack pack
     * @return Set
     */
    public static Set<Class<?>> getClasses(String pack) {
        Set<Class<?>> retVal = new LinkedHashSet<>();
        // recursive flag.
        boolean recursive = true;
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    log.info("file类型的扫描");
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, retVal);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    log.info("jar类型的扫描");
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            String rootPath = packageName + '.' + className;
                                            retVal.add(Thread.currentThread().getContextClassLoader().loadClass(rootPath));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retVal;
    }

    /**
     * Get all the classes under the package as a file
     *
     * @param packageName packageName
     * @param packagePath packagePath
     * @param recursive   recursive
     * @param classes     classes
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
        // Get the directory of this package And create a file.
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] dirfiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        for (File file : Objects.requireNonNull(dirfiles)) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // if it is a java class. add to the retVal.
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    String rootPath = packageName + '.' + className;
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(rootPath));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeConstantExcel(String filePath, List<String> titles,Map<String, Map<String, Object>> datas) {
        HashMap<String, Integer> infos = new HashMap<>();

        boolean isSuccess = false;
        if (CimStringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("File Path is null.");
        }
        String suffix = getSuffix(filePath);
        if (CimStringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("Suffix is null.");
        }

        //创建工作簿
        Workbook workbook = readExcel(filePath);
        if (null == workbook) {
            if ("xls".equals(suffix.toLowerCase())) {
                workbook = new HSSFWorkbook();
            } else {
                workbook = new XSSFWorkbook();
            }
        }

        //创建多个表
        for (String key : datas.keySet()) {
            // create a sheet.
            Sheet sheet = workbook.createSheet(key);
            Map<String, Object> data = datas.get(key);

            sheet.autoSizeColumn(1, true);
            /*sheet.setDefaultColumnWidth((short) 15);*/
            // generate style.
            // create Row Title.
            Row row = sheet.createRow(0);

            //设置字体
            CellStyle normalCellStyle1 = workbook.createCellStyle();
            Font normalFont1 = workbook.createFont();
            normalFont1.setFontName(HSSFFont.FONT_ARIAL);
            normalFont1.setFontHeightInPoints((short) 11);
            normalCellStyle1.setFont(normalFont1);
            normalCellStyle1.setAlignment(HorizontalAlignment.CENTER);
            normalCellStyle1.setVerticalAlignment(VerticalAlignment.CENTER);


            CellStyle linkCellStyle = workbook.createCellStyle();
            Font linkFont = workbook.createFont();
            linkFont.setColor(Font.COLOR_RED);
            linkFont.setFontHeightInPoints((short) 11);
            linkFont.setFontName(HSSFFont.FONT_ARIAL);
            linkFont.setUnderline((byte)1);
            linkCellStyle.setFont(linkFont);
            linkCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Main process.
            Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
            // row of excel and start from 1.
            int index = 0;
            while (iterator.hasNext()) {
                Map.Entry<String, Object> map = iterator.next();
                row = sheet.createRow(index);
                Cell entryCell1 = row.createCell(0);
                Cell entryCell2 = row.createCell(1);
                entryCell1.setCellValue(map.getKey());
                entryCell1.setCellStyle(normalCellStyle1);
                entryCell2.setCellValue(map.getValue().toString());
                entryCell2.setCellStyle(normalCellStyle1);
                index++;
            }
        }

        try {
            OutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            isSuccess = true;
            outputStream.close();
            workbook.close();
        } catch (Exception e) {
            log.error("Create Excel file fail.");
            e.printStackTrace();
        }
        return isSuccess;
    }
}
