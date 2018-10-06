package com.xpo.doorplanningtool;

import com.xpo.doorplanningtool.util.DatabaseUtil;
import com.xpo.doorplanningtool.util.EmailUtil;
import com.xpo.doorplanningtool.vo.Plan;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import  org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import java.io.*;
import java.security.KeyStore;
import java.sql.*;
import java.util.*;
import java.util.Date;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;


import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;

/**
 * Created with IntelliJ IDEA.
 * User: zhang.mingming
 * Date: 8/13/13
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlanningInstructionsTest {
    //private static final Logger log = Logger.getLogger(PlanningInstructionsTest.class);
    void generateInstructions(Plan plan)
    {

        String sic = plan.getSic();
        boolean fac_shift = plan.isFac_shift();

        PatternLayout layout = new PatternLayout();
        String conversionPattern = "%-7p %d [%t] %c %x - %m%n";
        layout.setConversionPattern(conversionPattern);

        // creates file appender
        FileAppender fileAppender = new FileAppender();
        fileAppender.setFile("C:\\Projects\\planningInstructionsTest_log.txt");
        fileAppender.setLayout(layout);
        fileAppender.activateOptions();

        // configures the root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(fileAppender);

        // creates a custom logger and log messages
        Logger logger = Logger.getLogger(workbooks.class);


        logger.debug("Started");



        Connection prdwhsevwConn = null;
        Connection prdcwfengConn = null;
        System.out.println(sic);

        int rowCounter=0;
        int scheduleCounter = 0;

        try
        {
            String shift_abbreviation = "OTB";
            if(plan.isFac_shift())
                shift_abbreviation = "FAC";

            prdcwfengConn = getConnection_PRD_CWFENG();

            if ( plan.isIs_exception_date() )
            {
                //get the excel file in the o drive for the sic, strip out the second page, add an empty page

                FileInputStream input_document = new FileInputStream(new File("\\\\cgoprfp003\\public\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\\" + sic + "_door_planning_" + shift_abbreviation + ".xls"));

                //Access the workbook
                HSSFWorkbook my_xls_workbook = new HSSFWorkbook(input_document);
                //Access the worksheet, so that we can update / modify it.
                HSSFSheet my_worksheet = my_xls_workbook.getSheetAt(1);

                HSSFRow row = null;
                // Access the cell first to update the value

                for ( int nrow = 1; nrow < 150; nrow++ )
                {
                    row= my_worksheet.getRow(nrow);

//The below method removes only cell values not row.
                    if (! (row == null) )
                        my_worksheet.removeRow(row);
                }

                //set cell value at tow 2 column 1.


                //Close the InputStream
                input_document.close();
                //Open FileOutputStream to write updates
                String output_file_name = "C:\\Projects\\data\\" + sic + "_door_planning_" + shift_abbreviation + ".xls";
                //String output_file_name = "\\\\cgoprfp003\\public\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\\" + sic + "_door_planning_" + shift_abbreviation + ".xls";
                FileOutputStream output_file =new FileOutputStream(new File(output_file_name));
                //write changes
                my_xls_workbook.write(output_file);
                //close the stream
                output_file.close();

                if(plan.isSending_email_ind())
                {
                    EmailUtil sendEmail = new EmailUtil();
                    String to_address1 =  "Liza.Zamora@xpo.com";
                    //String to_address1 =  "opspersonnel-" + sic + "@con-way.com";
                    sendEmail.sendJavaMail(plan.getSic(), output_file_name, to_address1, shift_abbreviation);
                }
                return;
            }

            // connection to an MS SQL SERVER

            prdwhsevwConn = getConnection_PRD_WHSEVIEW();

            Statement stmt = prdwhsevwConn.createStatement();
            DatabaseUtil.executeUpdate1(stmt, plan);
            ResultSet rs = DatabaseUtil.executeQuery1(stmt, plan);
            rs.setFetchSize(10000);

            Statement stmt_cwfeng = prdcwfengConn.createStatement();

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
            //System.out.println("point 3");

            //the header
            Font header_font = workbook.createFont();
            header_font.setFontName("ARIAL");
            header_font.setFontHeightInPoints((short)13);
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




            //header_format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);

            // String headers[] = new String[] {"Sic", "Shift", "Load To Mode1", "1st FAC", "Must Move", "Must Move to Shift", "Load To Mode2", "2nd FAC",  "Load To Mode3", "3rd FAC",
            //         "Dest Sic", "Head Load", "Bypass", "Head Load Hit Ratio", "Head Load Avg Weight", "Head Load Avg Cube", "Bypass Hit Ratio", "Bypass Avg Weight", "Bypass Avg Cube"};

            // String headers[] = new String[] {"SIC", "Shift", "Load To Mode1", "DAYFRT", "1st FAC", "Must Clear", "Must Clear Shift", "2nd FAC", "3rd FAC",
            //                  "Dest Sic", "Head Load", "Bypass", "Head Load Hit Ratio", "Head Load Avg Weight", "Head Load Avg Cube", "Bypass Hit Ratio", "Bypass Avg Weight", "Bypass Avg Cube"};


            String headers[];
            headers = new String[] {"SIC", "Shift", "DAYFRT", "1st FAC", "Must Clear",  "2nd FAC", "3rd FAC",
                    "Dest Sic", "Head Load", "Bypass", "AVG WGT", "AVG CUBE"};

            if (fac_shift)
            {
                headers = new String[] {"SIC", "Shift", "DAYFRT", "1st FAC", "Must Clear",  "2nd FAC", "3rd FAC",
                        "Dest Sic", "Load Door", "Bypass"};
            }

            //create header row
            Row row = sheet.createRow(rowCounter);

            // Create a cell and put a value in it.


            for( int i = 0; i < headers.length; i++)
            {
                Cell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                if ( i >=2 && i <= 6 )
                {
                    cell.setCellStyle(header_highlighted_style);
                }
                else
                {
                    cell.setCellStyle(header_style);
                }


            }

            //general label content format
            Font text_font = workbook.createFont();
            text_font.setFontName("ARIAL");
            text_font.setFontHeightInPoints((short)10);

            Font bold_font = workbook.createFont();
            bold_font.setFontName("ARIAL");
            bold_font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            bold_font.setFontHeightInPoints((short)10);

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

            while (rs.next()) {
                rowCounter++;
                Row new_row = sheet.createRow(rowCounter);
                boolean recommended_door = false;
                orig_sic = rs.getString("orig_sic");
                orig_shift = rs.getString("orig_shift");
                //load_to_mode1 = rs.getString("load_to_mode1");
                //if ( !load_to_mode1.equals('S'))
                //{
                //   load_to_mode1 = "";
                //}
                load_to_sic1 = rs.getString("load_to_sic1");
                must_clear_sic = rs.getString("must_clear_sic");
                // must_clear_shift = rs.getString("must_clear_shift");
                daylane_freight = rs.getString("daylane_freight");
                if (daylane_freight.equals("Y"))
                {
                    daylane_freight = "D";
                }
                else
                {
                    daylane_freight = "";
                }

                load_to_sic2 = rs.getString("load_to_sic2");
                load_to_sic3 = rs.getString("load_to_sic3");
                dest_sic = rs.getString("dest_sic");
                head_load = rs.getString("head_load");
                bypass = rs.getString("bypass");
                avg_weight = rs.getDouble("avg_weight");
                avg_cube = rs.getDouble("avg_cube");

                if (head_load.equals("X") && (!fac_shift))
                {
                    Cell cell = new_row.createCell(8);
                    cell.setCellValue(head_load);
                    cell.setCellStyle(x_style);
                    recommended_door = true;
                }


                if (bypass.equals("X"))
                {
                    Cell cell = new_row.createCell(9);
                    cell.setCellValue(head_load);
                    cell.setCellStyle(x_style);
                    recommended_door = true;
                }




                if (dest_sic.equals(""))
                {
                    if (load_to_sic3.equals("") )
                    {
                        if ( load_to_sic2.equals(""))
                        {
                            if ( must_clear_sic.equals(""))
                            {
                                if (!load_to_sic1.equals(""))
                                {
                                    load_to_sic1 = load_to_sic1 + " Total";
                                    Cell cell = new_row.createCell(3);
                                    cell.setCellValue(load_to_sic1);
                                    cell.setCellStyle(recommended_door?callout_style:first_FAC_style);

                                    if (!fac_shift)    //only OTB need to print out avg weight and cube
                                    {
                                        Cell avg_weight_cell = new_row.createCell(10);
                                        avg_weight_cell.setCellValue(avg_weight);
                                        avg_weight_cell.setCellStyle(number_style);

                                        Cell avg_cube_cell = new_row.createCell(11);
                                        avg_cube_cell.setCellValue(avg_cube);
                                        avg_cube_cell.setCellStyle(pct_style);
                                    }



                                    //insert a gray row
                                    rowCounter++;
                                    Row empty_row = sheet.createRow(rowCounter);

                                    empty_row.setRowStyle(empty_row_style);
                                    continue;
                                }
                            }
                            else
                            {
                                //print move to sic 1 and total
                                must_clear_sic = must_clear_sic + " Total";
                                Cell cell = new_row.createCell(4);
                                cell.setCellValue(must_clear_sic);
                                cell.setCellStyle(recommended_door?callout_style:must_clear_style);

                                if (fac_shift && !daylane_freight.equals("D"))
                                {
                                    Cell load_door_cell = new_row.createCell(8);
                                    load_door_cell.setCellValue("X");
                                    load_door_cell.setCellStyle(x_style);
                                }


                                continue;
                            }
                        }
                        else
                        {
                            //print load to sic 2 and total
                            load_to_sic2 = load_to_sic2 + " Total";
                            Cell cell = new_row.createCell(5);
                            cell.setCellValue(load_to_sic2);
                            cell.setCellStyle(recommended_door?callout_style:second_FAC_style);

                            if (fac_shift && daylane_freight.equals("D"))
                            {
                                Cell load_door_cell = new_row.createCell(8);
                                load_door_cell.setCellValue("X");
                                load_door_cell.setCellStyle(x_style);
                            }

                            continue;
                        }
                    }
                    else
                    {
                        //print load to sic 3 and total
                        load_to_sic3 = load_to_sic3 + " Total";
                        Cell cell = new_row.createCell(6);
                        cell.setCellValue(load_to_sic3);
                        cell.setCellStyle(recommended_door?callout_style:third_FAC_style);

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
                dest_sic_cell.setCellStyle(recommended_door?callout_style:dest_style);

            }
            rowCounter--;
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(4);
            sheet.autoSizeColumn(5);
            sheet.autoSizeColumn(6);
            sheet.autoSizeColumn(7);
            sheet.autoSizeColumn(8);
            sheet.autoSizeColumn(9);

            sheet.createFreezePane(0, 1, 0, 1);
            //sheet.setAutoFilter(new CellRangeAddress.valueOf("C5:F200"));
            sheet.setAutoFilter(new CellRangeAddress(0, rowCounter,0, 9));

            sheet.getPrintSetup().setLandscape(true);
            sheet.setAutobreaks(true);
            sheet.setFitToPage(true);
            sheet.getPrintSetup().setFitWidth((short)1);
            sheet.getPrintSetup().setFitHeight((short)0);

            // sheet.setColumnView(3, 12);
            // sheet.setColumnView(4, 12);
            // sheet.setColumnView(11, 10);
            // sheet.setColumnView(14, 10);
            // sheet.setColumnView(15, 10);
            // sheet.setColumnView(16, 11);
            // sheet.setColumnView(17, 11);


            //System.out.println("point 4");

            String sql_query2 = "select sic from sic_doors;";

            if(fac_shift)
                sql_query2 = "select sic from sic_doors where bypass ='X';";

            //System.out.println(sql_query2);
            ResultSet rs2 = stmt.executeQuery(sql_query2);
            //System.out.println("point 4.1");
            // rs2.setFetchSize(10000);


            String door_sic;
            int door_sic_counter = 0;

            PreparedStatement prepared_statement = null;



         /*   if (rs2.last()) {
                int rowcount = rs2.getRow();
                System.out.println("rowcount is" + rowcount);
                rs2.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
            }   */
            //System.out.println("point 4.9");
            //prdcwfengConn.setAutoCommit(false);
            //System.out.println("point 4.10");


            if (!rs2.next() )
            {
                System.out.println("no data");
            }
            else
            {
                String insertTableSQL =  "insert into sic_doors_tmp (LAUNCH_DATE, DOOR_SIC, ORIG_SIC, SHIFT) values (?, ?, ?, ?)";
                //System.out.println("point 4.11");
                prepared_statement = prdcwfengConn.prepareStatement(insertTableSQL);
                //System.out.println("point 4.12");
                do {
                    door_sic_counter++;
                    door_sic = rs2.getString("sic");
                    String sql_updated_cwfeng = "insert into sic_doors_tmp values('" + plan.getInstruction_date() +"', '" + door_sic + "','" + sic + "','" + plan.getShift() + "');"; // to be updated
                    //System.out.println(sql_updated_cwfeng + "\n");
                    prepared_statement.setString(1, plan.getInstruction_date());
                    prepared_statement.setString(2, door_sic);
                    prepared_statement.setString(3, sic);
                    prepared_statement.setString(4, plan.getShift());
                    prepared_statement.addBatch();
                    //stmt_cwfeng.executeUpdate(sql_updated_cwfeng);
                }   while (rs2.next());
                prepared_statement.executeBatch();
                prdcwfengConn.commit();
            }

            rs = DatabaseUtil.executeAddDoorQuery(stmt_cwfeng, plan);
            rs.setFetchSize(1000);
            rowCounter = 0;


            String added_door;
            String removed_door;

            Row header_row = sheet1.createRow(rowCounter);


            Cell added_door_header = header_row.createCell(0);

            added_door_header.setCellValue("Added Doors");
            added_door_header.setCellStyle(header_highlighted_style);
            while (rs.next())
            {
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


            rs = DatabaseUtil.executeRemoveDoorQuery(stmt_cwfeng, plan);

            rs.setFetchSize(1000);
            //System.out.println("point 6");
            while (rs.next())
            {
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
            sheet1.getPrintSetup().setFitWidth((short)1);
            sheet1.getPrintSetup().setFitHeight((short)0);


            //String door_planning_file = "O:\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\\" + sic + "_door_planning_" + shift_abbreviation + ".xls";
            //String door_planning_file = "\\\\cgoprfp003\\public\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\\" + sic + "_door_planning_" + shift_abbreviation + ".xls";
            String door_planning_file = "C:\\Projects\\" + sic + "_door_planning_" + shift_abbreviation + ".xls";

            System.out.println(door_planning_file);

            //log.info(door_planning_file);

            FileOutputStream fileOut_official = new FileOutputStream(door_planning_file);

            workbook.write(fileOut_official);

            fileOut_official.close();

            if(plan.isSending_email_ind())
            {
                EmailUtil sendEmail = new EmailUtil();
                String to_address1 =  "Liza.Zamora@xpo.com";
                //String to_address1 =  "opspersonnel-" + sic + "@con-way.com";
                sendEmail.sendJavaMail(sic, door_planning_file, to_address1, shift_abbreviation);
            }
            prdwhsevwConn.close();
            prdcwfengConn.close();
        }
        catch (SQLException e)
        {
            System.out.println
                    ("Error, SQL failed.\n");


        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.


        } finally {
            try { prdwhsevwConn.close(); } catch (Exception e) { /* ignored */ }
            try { prdcwfengConn.close(); } catch (Exception e) { /* ignored */ }
            //    try { conn.close(); } catch (Exception e) { /* ignored */ }
        }

    }
    public static Connection getConnection_PRD_CWFENG() throws SQLException
    {
        try
        {
            Class.forName("org.netezza.Driver").newInstance();
            Connection c = DriverManager.getConnection("jdbc:netezza://npsdwh.con-way.com/PRD_CWFENG", "MXBUHAY", "miguel082416");
            c.setAutoCommit(false);
            return c;
        }
        catch (Exception e)
        {
            System.out.println
                    ("Error, CWFENG Connection not made.\n");
        }
        return null;

    }
    public static Connection getConnection_PRD_WHSEVIEW() throws SQLException
    {
        try
        {
            Class.forName("org.netezza.Driver").newInstance();
            Connection c = DriverManager.getConnection("jdbc:netezza://npsdwh.con-way.com/PRD_WHSEVIEW?allowMultiQuery=true", "MXBUHAY", "miguel082416");
            c.setAutoCommit(false);
            return c;
        }
        catch (Exception e)
        {
            System.out.println
                    ("Error, WHSEVIEW Connection not made.\n");
        }
        return null;
    }



}
