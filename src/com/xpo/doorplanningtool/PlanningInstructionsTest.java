package com.xpo.doorplanningtool;

import com.xpo.doorplanningtool.cnst.QueryConstants;
import com.xpo.doorplanningtool.database.DBConnection;
import com.xpo.doorplanningtool.util.DatabaseUtil;
import com.xpo.doorplanningtool.util.EmailUtil;
import com.xpo.doorplanningtool.vo.BypassLane;
import com.xpo.doorplanningtool.vo.Plan;
import com.xpo.doorplanningtool.vo.Threshold;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.xml.crypto.Data;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlanningInstructionsTest {

    String input_file_path = "\\\\cgoprfp003\\public\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\\";
    String output_file_path = "\\\\cgoprfp003\\Public\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\\";
    //String output_file_name_str1 = "\\\\cgoprfp003\\public\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\\";
    //String output_file_path = "C:\\Projects\\data\\";
    String door_planning_text = "_door_planning_";
    String file_extension = ".xls";

    //private static final Logger log = Logger.getLogger(PlanningInstructionsTest.class);
    void generateInstructions(Plan plan, Threshold threshold) {
        String sic = plan.getSic();
        boolean fac_shift = plan.isFac_shift();

        String to_address1 =  "opspersonnel-" + sic + "@con-way.com";
        //String to_address1 = "Liza.Zamora@xpo.com";

        PatternLayout layout = new PatternLayout();
        String conversionPattern = "%-7p %d [%t] %c %x - %m%n";
        layout.setConversionPattern(conversionPattern);

        // creates file appender
        FileAppender fileAppender = new FileAppender();
        fileAppender.setFile("\\\\cgoprfp003\\Public\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\\Door Planning Tool\\planningInstructionsTest_log.txt");
        fileAppender.setLayout(layout);
        fileAppender.activateOptions();

        // configures the root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(fileAppender);

        // creates a custom logger and log messages
        Logger logger = Logger.getLogger(workbooks.class);

        logger.debug("Started");

        DBConnection prdwhsevwConn = null;
        DBConnection prdcwfengConn = null;
        System.out.println(sic);

        int rowCounter = 0;
        int sheet2_rowCounter = 0;

        try {
            String shift_abbreviation = "OTB";
            if (plan.isFac_shift())
                shift_abbreviation = "FAC";
            prdcwfengConn = getConnection_PRD_CWFENG();

            if (plan.isIs_exception_date()) {
                //get the excel file in the o drive for the sic, strip out the second page, add an empty page
                FileInputStream input_document = new FileInputStream(new File(input_file_path + sic + door_planning_text + shift_abbreviation + file_extension));
                //Access the workbook
                HSSFWorkbook my_xls_workbook = new HSSFWorkbook(input_document);
                //Access the worksheet, so that we can update / modify it.
                HSSFSheet my_worksheet = my_xls_workbook.getSheetAt(1);
                HSSFRow row = null;
                // Access the cell first to update the value

                for (int nrow = 1; nrow < 150; nrow++) {
                    row = my_worksheet.getRow(nrow);

                //The below method removes only cell values not row.
                    if (!(row == null))
                        my_worksheet.removeRow(row);
                }

                //set cell value at tow 2 column 1.
                //Close the InputStream
                input_document.close();
                //Open FileOutputStream to write updates
                String output_file_name = output_file_path + sic + door_planning_text + shift_abbreviation + file_extension;

                FileOutputStream output_file = new FileOutputStream(new File(output_file_name));
                //write changes
                my_xls_workbook.write(output_file);
                //close the stream
                output_file.close();

                if (plan.isSending_email_ind()) {
                    EmailUtil sendEmail = new EmailUtil();
                    sendEmail.sendJavaMail(plan.getSic(), output_file_name, to_address1, shift_abbreviation);
                }
                return;
            }

            // connection to an MS SQL SERVER

            prdwhsevwConn = getConnection_PRD_WHSEVIEW();

            DatabaseUtil.executeUpdate1(prdwhsevwConn, plan, threshold);
            ResultSet rs = DatabaseUtil.executeQuery1(prdwhsevwConn, plan);
            rs.setFetchSize(10000);

            String orig_sic;
            String orig_shift;
            String load_to_sic1;
            String must_clear_sic;
            String daylane_freight;
            String load_to_mode2;
            String load_to_sic2;
            String load_to_mode3;
            String load_to_sic3;
            String dest_sic;
            String head_load;
            String bypass;
            double head_load_hit_ratio;
            double head_load_avg_weight;
            double head_load_avg_cube;
            double bypass_hit_ratio;
            double bypass_avg_weight;
            double bypass_avg_cube;
            double avg_weight;
            double avg_cube;

            Workbook workbook = new HSSFWorkbook();

            Sheet sheet = workbook.createSheet("Door Planning");
            Sheet sheet1 = workbook.createSheet("Door Changes");
            Sheet sheet2 = workbook.createSheet("Condensed Door Planning");
            //System.out.println("point 3");

            //the header
            Font header_font = workbook.createFont();
            header_font.setFontName("ARIAL");
            header_font.setFontHeightInPoints((short) 13);
            header_font.setBoldweight(Font.BOLDWEIGHT_BOLD);

            CellStyle header_style = workbook.createCellStyle();
            header_style.setFont(header_font);
            header_style.setAlignment(CellStyle.ALIGN_CENTER);
            header_style.setWrapText(true);

            CellStyle header_highlighted_style = workbook.createCellStyle();
            header_highlighted_style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            header_highlighted_style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            header_highlighted_style.setFont(header_font);
            header_highlighted_style.setAlignment(CellStyle.ALIGN_CENTER);
            //header_highlighted_style.setWrapText(true);

            String headers[];
            String headers2[];
            headers = new String[]{"SIC", "Shift", "DAYFRT", "1st FAC", "Must Clear", "2nd FAC", "3rd FAC",
                    "Dest Sic", "Bypass", "AVG WGT", "AVG CUBE"};

            headers2 = new String[]{"Load SIC","Unload SIC","DAYHAUL","1st FAC","Must Clear","2nd FAC","3rd FAC"};

            if (fac_shift) {
                headers = new String[]{"SIC", "Shift", "DAYFRT", "1st FAC", "Must Clear", "2nd FAC", "3rd FAC",
                        "Dest Sic", "Load Door", "Bypass"};
            }

            //create header row
            Row row = sheet.createRow(rowCounter);
            Row sheet2_header_row = sheet2.createRow(sheet2_rowCounter);

            // Create a cell and put a value in it.
            for (int i = 0; i < headers.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                if (i >= 2 && i <= 6) {
                    cell.setCellStyle(header_highlighted_style);
                } else {
                    cell.setCellStyle(header_style);
                }
            }

            //Create header row of third sheet
            for (int i = 0; i < headers2.length; i++){
                Cell cell = sheet2_header_row.createCell(i);
                cell.setCellValue(headers2[i]);
                //cell.setCellStyle(header_style);
            }

            //general label content format
            Font text_font = workbook.createFont();
            text_font.setFontName("ARIAL");
            text_font.setFontHeightInPoints((short) 10);

            Font bold_font = workbook.createFont();
            bold_font.setFontName("ARIAL");
            bold_font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            bold_font.setFontHeightInPoints((short) 10);

            CellStyle text_style = workbook.createCellStyle();
            text_style.setFont(text_font);
            text_style.setAlignment(CellStyle.ALIGN_CENTER);

            CellStyle pct_style = workbook.createCellStyle();
            pct_style.setDataFormat(workbook.createDataFormat().getFormat("0%"));
            pct_style.setAlignment(CellStyle.ALIGN_CENTER);
            header_highlighted_style.setFont(text_font);

            CellStyle callout_style = workbook.createCellStyle();
            callout_style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            callout_style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            callout_style.setFont(bold_font);
            callout_style.setAlignment(CellStyle.ALIGN_CENTER);

            CellStyle number_style = workbook.createCellStyle();
            number_style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            number_style.setAlignment(CellStyle.ALIGN_CENTER);
            number_style.setFont(text_font);

            //highlighted format for Recommended door
            CellStyle x_style = workbook.createCellStyle();
            x_style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            x_style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            x_style.setFont(bold_font);
            x_style.setAlignment(CellStyle.ALIGN_CENTER);

            //highlighted format for 1st FAC
            CellStyle first_FAC_style = workbook.createCellStyle();
            first_FAC_style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            first_FAC_style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            first_FAC_style.setFont(bold_font);
            first_FAC_style.setAlignment(CellStyle.ALIGN_CENTER);

            //highlighted format for must clear FAC
            CellStyle must_clear_style = workbook.createCellStyle();
            must_clear_style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            must_clear_style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            must_clear_style.setFont(bold_font);
            must_clear_style.setAlignment(CellStyle.ALIGN_CENTER);

            //highlighted format for 2nd FAC
            CellStyle second_FAC_style = workbook.createCellStyle();
            second_FAC_style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            second_FAC_style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            second_FAC_style.setFont(bold_font);
            second_FAC_style.setAlignment(CellStyle.ALIGN_CENTER);

            //highlighted format for 3rd FAC
            CellStyle third_FAC_style = workbook.createCellStyle();
            third_FAC_style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            third_FAC_style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            third_FAC_style.setFont(bold_font);
            third_FAC_style.setAlignment(CellStyle.ALIGN_CENTER);

            //highlighted format for dest
            CellStyle dest_style = workbook.createCellStyle();
            dest_style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            dest_style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            dest_style.setAlignment(CellStyle.ALIGN_CENTER);

            CellStyle empty_row_style = workbook.createCellStyle();
            empty_row_style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            empty_row_style.setFillPattern(CellStyle.SOLID_FOREGROUND);

            // System.out.println("point 3");

            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            while (rs.next()) {
                rowCounter++;
                //sheet2_rowCounter++;
                Row new_row = sheet.createRow(rowCounter);

                String dayhaul = "";
                boolean recommended_door = false;
                orig_sic = rs.getString("orig_sic");
                orig_shift = rs.getString("orig_shift");
                load_to_sic1 = rs.getString("load_to_sic1");
                must_clear_sic = rs.getString("must_clear_sic");
                daylane_freight = rs.getString("daylane_freight");

                if (daylane_freight.equals("Y")) {
                    daylane_freight = "D";
                    dayhaul = "Y";
                } else {
                    daylane_freight = "";
                    dayhaul = "N";
                }

                load_to_sic2 = rs.getString("load_to_sic2");
                load_to_sic3 = rs.getString("load_to_sic3");
                dest_sic = rs.getString("dest_sic");
                head_load = rs.getString("head_load");
                bypass = rs.getString("bypass");
                avg_weight = rs.getDouble("avg_weight");
                avg_cube = rs.getDouble("avg_cube");

                String firstFAC = load_to_sic1;
                if(load_to_sic1.contains(" D")){
                    firstFAC = load_to_sic1.substring(0,3);
                    dayhaul = "Y";
                }

                //unload_sic logic in condensed door planning
                String unload_sic = "";
                if(dest_sic.length()!=0){
                    unload_sic = dest_sic.trim();
                }else if(load_to_sic3.length()!=0){
                    unload_sic = load_to_sic3.trim();
                }else if(load_to_sic2.length()!=0){
                    unload_sic = load_to_sic2.trim();
                }else if(must_clear_sic.length()!=0){
                    unload_sic = must_clear_sic.trim();
                }else if(load_to_sic1.length()!=0){
                    unload_sic = firstFAC.trim();
                }

                if (head_load.equals("X") && (!fac_shift)) {
                    recommended_door = true;
                }

                if (bypass.equals("X")) {
                    //prints bypass indicator in first sheet
                    //modified 03062019 to add condensed door tab to fac
                    Cell cell;
                    if(fac_shift) {
                        cell = new_row.createCell(9);
                    }else {
                        cell = new_row.createCell(8);
                    }
                    //end modification
                    cell.setCellValue(head_load);
                    cell.setCellStyle(x_style);
                    recommended_door = true;

                    if(dest_sic.equals("")) {
                        sheet2_rowCounter++;
                        Row sheet2_new_row = sheet2.createRow(sheet2_rowCounter);

                        //System.out.println(dest_sic + "----" + sheet2_rowCounter);

                        //prints row in third sheet
                        Cell orig_sic_cell = sheet2_new_row.createCell(0);
                        orig_sic_cell.setCellValue(orig_sic);
                        orig_sic_cell.setCellStyle(text_style);

                        Cell dest_sic_cell = sheet2_new_row.createCell(1);
                        dest_sic_cell.setCellValue(unload_sic);
                        dest_sic_cell.setCellStyle(text_style);

                        Cell daylane_freight_cell = sheet2_new_row.createCell(2);
                        daylane_freight_cell.setCellValue(dayhaul);
                        daylane_freight_cell.setCellStyle(text_style);

                        Cell load_to_sic1_cell = sheet2_new_row.createCell(3);
                        load_to_sic1_cell.setCellValue(firstFAC.trim());
                        load_to_sic1_cell.setCellStyle(text_style);

                        Cell must_clear_sic_cell = sheet2_new_row.createCell(4);
                        must_clear_sic_cell.setCellValue(must_clear_sic);
                        must_clear_sic_cell.setCellStyle(text_style);

                        Cell load_to_sic2_cell = sheet2_new_row.createCell(5);
                        load_to_sic2_cell.setCellValue(load_to_sic2);
                        load_to_sic2_cell.setCellStyle(text_style);

                        Cell load_to_sic3_cell = sheet2_new_row.createCell(6);
                        load_to_sic3_cell.setCellValue(load_to_sic3);
                        load_to_sic3_cell.setCellStyle(text_style);

                        // insert insertquery here
                        if(!fac_shift) {
                            BypassLane bypassLane = new BypassLane(timeStamp, plan.getInstruction_date(), orig_sic, "OTB", firstFAC.trim(), must_clear_sic, dayhaul, load_to_sic2, load_to_sic3, unload_sic, "", bypass);
                            DatabaseUtil.insertBypassLane(prdcwfengConn, bypassLane);
                        }else{
                            BypassLane bypassLane =  new BypassLane(timeStamp,plan.getInstruction_date(),orig_sic,"FAC",firstFAC.trim(),must_clear_sic,dayhaul,load_to_sic2,load_to_sic3,unload_sic, head_load, bypass);
                            DatabaseUtil.insertBypassLane(prdcwfengConn,bypassLane);
                        }
//                        for (Cell cell2 : sheet2_new_row) {
//                            System.out.print(cell2.getStringCellValue() + "   ");
//                        }
//                        System.out.print("\n");
                    }
                }

//                if(!(head_load.equals("X")||bypass.equals("X")) && !fac_shift){
//                    sheet2_rowCounter--;
//                }

                //removed 03062019 to add condensed door planning to FAC
//                if (bypass.equals("X") && fac_shift) {
//                    Cell cell = new_row.createCell(9);
//                    cell.setCellValue(head_load);
//                    cell.setCellStyle(x_style);
//                    recommended_door = true;
//                }

                if (dest_sic.equals("")) {
                    if (load_to_sic3.equals("")) {
                        if (load_to_sic2.equals("")) {
                            if (must_clear_sic.equals("")) {
                                if (!load_to_sic1.equals("")) {
                                    load_to_sic1 = load_to_sic1 + " Total";
                                    Cell cell = new_row.createCell(3);
                                    cell.setCellValue(load_to_sic1);
                                    cell.setCellStyle(recommended_door ? callout_style : first_FAC_style);

                                    if (!fac_shift)    //only OTB need to print out avg weight and cube
                                    {
                                        //ONLY OUTBOUND overrides bypass logic
                                        // overrides bypass logic and add bypass indicator for all 1st FAC total
                                        Cell forced_bypass_cell = new_row.createCell(8);
                                        forced_bypass_cell.setCellValue("X");
                                        forced_bypass_cell.setCellStyle(x_style);

                                        Cell avg_weight_cell = new_row.createCell(9);
                                        avg_weight_cell.setCellValue(avg_weight);
                                        avg_weight_cell.setCellStyle(number_style);

                                        Cell avg_cube_cell = new_row.createCell(10);
                                        avg_cube_cell.setCellValue(avg_cube);
                                        avg_cube_cell.setCellStyle(pct_style);

                                        if(!bypass.equals("X")){
                                            sheet2_rowCounter++;
                                            Row sheet2_new_row = sheet2.createRow(sheet2_rowCounter);

                                            //prints row in third sheet
                                            Cell orig_sic_cell = sheet2_new_row.createCell(0);
                                            orig_sic_cell.setCellValue(orig_sic);
                                            orig_sic_cell.setCellStyle(text_style);

                                            Cell dest_sic_cell = sheet2_new_row.createCell(1);
                                            dest_sic_cell.setCellValue(unload_sic);
                                            dest_sic_cell.setCellStyle(text_style);

                                            Cell daylane_freight_cell = sheet2_new_row.createCell(2);
                                            daylane_freight_cell.setCellValue(dayhaul);
                                            daylane_freight_cell.setCellStyle(text_style);

                                            Cell load_to_sic1_cell = sheet2_new_row.createCell(3);
                                            load_to_sic1_cell.setCellValue(firstFAC.trim());
                                            load_to_sic1_cell.setCellStyle(text_style);

                                            Cell must_clear_sic_cell = sheet2_new_row.createCell(4);
                                            must_clear_sic_cell.setCellValue(must_clear_sic);
                                            must_clear_sic_cell.setCellStyle(text_style);

                                            Cell load_to_sic2_cell = sheet2_new_row.createCell(5);
                                            load_to_sic2_cell.setCellValue(load_to_sic2);
                                            load_to_sic2_cell.setCellStyle(text_style);

                                            Cell load_to_sic3_cell = sheet2_new_row.createCell(6);
                                            load_to_sic3_cell.setCellValue(load_to_sic3);
                                            load_to_sic3_cell.setCellStyle(text_style);

                                            // insert insertquery here
                                            BypassLane bypassLane =  new BypassLane(timeStamp,plan.getInstruction_date(),orig_sic,"OTB",firstFAC.trim(),must_clear_sic,dayhaul,load_to_sic2,load_to_sic3,unload_sic,"", "X");
                                            DatabaseUtil.insertBypassLane(prdcwfengConn,bypassLane);
//                                            for (Cell cell2 : sheet2_new_row) {
//                                                System.out.print(cell2.getStringCellValue() + "   ");
//                                            }
//                                            System.out.print("\n");
                                        }
                                    }

                                                                        //insert a gray row
                                    rowCounter++;
                                    Row empty_row = sheet.createRow(rowCounter);
                                    empty_row.setRowStyle(empty_row_style);
                                    continue;
                                }
                            } else {
                                //print move to sic 1 and total
                                must_clear_sic = must_clear_sic + " Total";
                                Cell cell = new_row.createCell(4);
                                cell.setCellValue(must_clear_sic);
                                cell.setCellStyle(recommended_door ? callout_style : must_clear_style);

                                if (fac_shift && !daylane_freight.equals("D")) {
                                    Cell load_door_cell = new_row.createCell(8);
                                    load_door_cell.setCellValue("X");
                                    load_door_cell.setCellStyle(x_style);
                                }
//                                //removes duplicate row in third sheet
//                                if(bypass.equals("X") && !fac_shift) {
//                                    sheet2_rowCounter--;
//                                }

                                continue;
                            }
                        } else {
                            //print load to sic 2 and total
                            load_to_sic2 = load_to_sic2 + " Total";
                            Cell cell = new_row.createCell(5);
                            cell.setCellValue(load_to_sic2);
                            cell.setCellStyle(recommended_door ? callout_style : second_FAC_style);

                            if (fac_shift && daylane_freight.equals("D")) {
                                Cell load_door_cell = new_row.createCell(8);
                                load_door_cell.setCellValue("X");
                                load_door_cell.setCellStyle(x_style);
                            }
//                            //removes duplicate row in third sheet
//                            if(bypass.equals("X") && !fac_shift) {
//                                sheet2_rowCounter--;
//                            }

                            continue;
                        }
                    } else {
                        //print load to sic 3 and total
                        load_to_sic3 = load_to_sic3 + " Total";
                        Cell cell = new_row.createCell(6);
                        cell.setCellValue(load_to_sic3);
                        cell.setCellStyle(recommended_door ? callout_style : third_FAC_style);
//                        //removes duplicate row in third sheet
//                        if(bypass.equals("X") && !fac_shift) {
//                            sheet2_rowCounter--;
//                        }

                        continue;
                    }

                }

                Cell orig_sic_cell = new_row.createCell(0);
                orig_sic_cell.setCellValue(orig_sic);
                orig_sic_cell.setCellStyle(text_style);

                Cell orig_shift_cell = new_row.createCell(1);
                orig_shift_cell.setCellValue(orig_shift);
                orig_shift_cell.setCellStyle(text_style);

                Cell daylane_freight_cell = new_row.createCell(2);
                daylane_freight_cell.setCellValue(daylane_freight);
                daylane_freight_cell.setCellStyle(text_style);

                Cell load_to_sic1_cell = new_row.createCell(3);
                load_to_sic1_cell.setCellValue(load_to_sic1);
                load_to_sic1_cell.setCellStyle(text_style);

                Cell must_clear_sic_cell = new_row.createCell(4);
                must_clear_sic_cell.setCellValue(must_clear_sic);
                must_clear_sic_cell.setCellStyle(text_style);

                Cell load_to_sic2_cell = new_row.createCell(5);
                load_to_sic2_cell.setCellValue(load_to_sic2);
                load_to_sic2_cell.setCellStyle(text_style);

                Cell load_to_sic3_cell = new_row.createCell(6);
                load_to_sic3_cell.setCellValue(load_to_sic3);
                load_to_sic3_cell.setCellStyle(text_style);

                Cell dest_sic_cell = new_row.createCell(7);
                dest_sic_cell.setCellValue(dest_sic);
                dest_sic_cell.setCellStyle(recommended_door ? callout_style : dest_style);

            }

            rowCounter--;
            //sheet2_rowCounter--;
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(4);
            sheet.autoSizeColumn(5);
            sheet.autoSizeColumn(6);
            sheet.autoSizeColumn(7);
            sheet.autoSizeColumn(8);
            sheet.autoSizeColumn(9);
            for(int i=0;i<headers2.length;i++){
                sheet2.autoSizeColumn(i);
            }

            sheet.createFreezePane(0, 1, 0, 1);
            //sheet.setAutoFilter(new CellRangeAddress.valueOf("C5:F200"));
            sheet.setAutoFilter(new CellRangeAddress(0, rowCounter, 0, 9));

            sheet.getPrintSetup().setLandscape(true);
            sheet.setAutobreaks(true);
            sheet.setFitToPage(true);
            sheet.getPrintSetup().setFitWidth((short) 1);
            sheet.getPrintSetup().setFitHeight((short) 0);

            //System.out.println(sql_query2);
            ResultSet rs2 = DatabaseUtil.executeQuerySicDoors(prdwhsevwConn, plan);

            if (!rs2.next()) {
                System.out.println("no data");
            } else {
                DatabaseUtil.insertSicDoorsTemp(prdcwfengConn, plan, rs2);
            }

            rs = DatabaseUtil.executeAddDoorQuery(prdcwfengConn, plan);
            rs.setFetchSize(1000);
            rowCounter = 0;

            String added_door;
            String removed_door;

            Row header_row = sheet1.createRow(rowCounter);

            Cell added_door_header = header_row.createCell(0);
            added_door_header.setCellValue("Added Doors");
            added_door_header.setCellStyle(header_highlighted_style);
            while (rs.next()) {
                rowCounter++;
                Row new_row = sheet1.createRow(rowCounter);
                Cell added_door_cell = new_row.createCell(0);
                added_door = rs.getString("door_sic");
                added_door_cell.setCellValue(added_door);
                added_door_cell.setCellStyle(text_style);

            }

            rowCounter += 2;
            Row removed_header_row = sheet1.createRow(rowCounter);
            Cell removed_door_header = removed_header_row.createCell(0);

            removed_door_header.setCellValue("Removed Doors");
            removed_door_header.setCellStyle(header_highlighted_style);

            rs = DatabaseUtil.executeRemoveDoorQuery(prdcwfengConn, plan);

            rs.setFetchSize(1000);
            while (rs.next()) {
                rowCounter++;
                Row new_row = sheet1.createRow(rowCounter);
                Cell removed_door_cell = new_row.createCell(0);
                removed_door = rs.getString("door_sic");
                removed_door_cell.setCellValue(removed_door);
                removed_door_cell.setCellStyle(text_style);
            }

            sheet1.autoSizeColumn(0);
            sheet1.getPrintSetup().setLandscape(true);
            sheet1.setFitToPage(true);
            sheet1.getPrintSetup().setFitWidth((short) 1);
            sheet1.getPrintSetup().setFitHeight((short) 0);

//            if(fac_shift){
//                workbook.removeSheetAt(2);
//            }

            String door_planning_file = output_file_path + sic + door_planning_text + shift_abbreviation + file_extension;

            System.out.println(door_planning_file);

            //log.info(door_planning_file);

            FileOutputStream fileOut_official = new FileOutputStream(door_planning_file);

            workbook.write(fileOut_official);

            fileOut_official.close();

            if (plan.isSending_email_ind()) {
                EmailUtil sendEmail = new EmailUtil();
                sendEmail.sendJavaMail(sic, door_planning_file, to_address1, shift_abbreviation);
            }
            prdwhsevwConn.close();
            prdcwfengConn.close();
        } catch (SQLException e) {
            System.out.println
                    ("Error, SQL failed.\n");


        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.


        } finally {
            try {
                prdwhsevwConn.close();
            } catch (Exception e) { /* ignored */ }
            try {
                prdcwfengConn.close();
            } catch (Exception e) { /* ignored */ }
            //    try { conn.close(); } catch (Exception e) { /* ignored */ }
        }

    }

    public static DBConnection getConnection_PRD_CWFENG() throws SQLException {
        DBConnection connection = new DBConnection(QueryConstants.NETEZZA_PRDCWFENG_URL.getValue(),QueryConstants.NETEZZA_USERNAME.getValue(),QueryConstants.NETEZZA_PASSWORD.getValue());
        connection.createConnection();
        return connection;

    }

    public static DBConnection getConnection_PRD_WHSEVIEW() throws SQLException {
        DBConnection connection = new DBConnection(QueryConstants.NETEZZA_PRDWHSEVW_URL.getValue(),QueryConstants.NETEZZA_USERNAME.getValue(),QueryConstants.NETEZZA_PASSWORD.getValue());
        connection.createConnection();
        return connection;
    }


}