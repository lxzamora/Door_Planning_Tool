import java.io.*;
import java.sql.*;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: zhang.mingming
 * Date: 8/13/13
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class RehandleOpportunity {
    //open and read in file, if unable to read file, print no file found and exit program.

    void calculateOpportunity(String sic, String beginning_date, String ending_date)
    {
        try
        {
            Connection theConn = null;
            System.out.println(sic);
            // connection to an MS SQL SERVER
            theConn = getConnection_PRD_WHSEVIEW();

           // String base_sql_updated = "create temp table ffo_extracts_otb as\n" +
           //       "select * from ffo_extracts_dayfreight\n" +
           //        "where orig_shift = 'O' and date >= '"+ beginning_date + "' and date <= '" + ending_date + "' \n" +
           //        "and orig_sic='" + sic + "' and dest_sic != 'NGZ' and dest_sic!= 'NRA' distribute on (orig_sic, dest_sic);\n" +
           //        "update ffo_extracts_otb set load_to_sic1= load_to_sic1||' D' where daylane_freight='Y';";

            String base_sql_updated = "create temp table ffo_extracts_otb as \n" +
                    "select cldr_dt as date, orig_sic_cd as orig_sic, 'O' as orig_shift, fnl_dest_sic_cd as dest_sic, ld_to_1_mode_cd as load_to_mode1, ld_to_1_shft_cd as load_to_shift1, case dy_frt_ind when 'Y' then ld_to_1_sic_cd||' D'  else ld_to_1_sic_cd end as load_to_sic1, ld_to_2_shft_cd as load_to_shift2,ld_to_2_sic_cd as load_to_sic2, ld_to_3_shft_cd as load_to_shift3,ld_to_3_sic_cd as load_to_sic3, must_clear_sic_cd as must_clear_sic, must_clear_shft_cd as must_clear_shift, dy_frt_ind as daylane_freight, orig_in_wgt as weight_in, orig_in_vol as cube_in, orig_out_wgt as weight_out, orig_out_vol as cube_out from FLO_FLOW_PLAN_SUMMARY_VW\n" +
                    "where orig_shft_cd = 'OTB' and date >= '"+ beginning_date + "' and date <= '" + ending_date + "' \n" +
                    "and orig_sic='" + sic + "' distribute on (orig_sic, dest_sic); \n"+
                    "update ffo_extracts_otb set load_to_sic2='' where load_to_sic2 is null;\n" +
                    "update ffo_extracts_otb set load_to_sic3='' where load_to_sic3 is null;\n" +
                    "update ffo_extracts_otb set load_to_shift2='' where load_to_shift2 is null;\n" +
                    "update ffo_extracts_otb set load_to_shift3='' where load_to_shift3 is null;\n" +
                    "update ffo_extracts_otb set must_clear_sic ='' where must_clear_sic is null;\n" +
                    "update ffo_extracts_otb set must_clear_shift ='' where must_clear_shift is null;\n" +
                    "update ffo_extracts_otb set must_clear_sic = '' where daylane_freight = 'Y';\n" +
                    "update ffo_extracts_otb set must_clear_shift = '' where daylane_freight = 'Y';\n";

            System.out.println(base_sql_updated);
            Statement stmt = theConn.createStatement();
            stmt.executeUpdate(base_sql_updated);
            calculateBasePlanOpportunity(sic, stmt);
            calculateDynamicPlanOpportunity(sic, stmt);
        }
        catch(Exception e)
        {
            System.out.println
                    ("Error, connection not made or ffo_extracts_otb not created.\n");
        }


    }
    void calculateBasePlanOpportunity(String sic, Statement stmt)
    {

        int rowCounter=0;
        int scheduleCounter = 0;

        try
        {


            String base_sql_updated = "create  temp table ffo_base_plan_od as\n" +
                    "(select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, dest_sic, sum(weight_in)/5.0 as avg_weight_in, sum((cube_in/1157.0)/5.0) as avg_cube_in, sum(weight_out)/5.0 as avg_weight_out, sum((cube_out/1157.0)/5.0) as avg_cube_out, count(distinct date)/5.0 as daily_building_frequency, case when sum(case when cube_out/1157.0 >=0.4 then 1 else 0 end)/5.0 >= 0.6 then 'X' else '' end as head_load, case when sum(case when cube_out/1157.0 >= 0.85 then 1 else 0 end)/5.0 >= 0.8 then 'X' else '' end as bypass   from ffo_extracts_otb \n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);\n" +
                    "create temp table ffo_extracts_otb_od_head_load_removed as\n" +
                    "select ffo_extracts_otb.* from ffo_extracts_otb left outer join ffo_base_plan_od on \n" +
                    "ffo_extracts_otb.orig_sic = ffo_base_plan_od.orig_sic and\n" +
                    "ffo_extracts_otb.dest_sic = ffo_base_plan_od.dest_sic\n" +
                    "where head_load != 'X';\n" +
                    "create temp table ffo_extracts_otb_3rd_fac_removed as\n" +
                    "select ffo_extracts_otb_od_head_load_removed.* from ffo_extracts_otb_od_head_load_removed left outer join \n" +
                    "(select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, '' as dest_sic, case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load, '' as bypass, count(distinct date)/5.0 as daily_building_frequency, sum(total_weight_in)/5.0 as avg_weight_in, sum(total_cube_in/5.0) as avg_cube_in, sum(total_weight_out)/5.0 as avg_weight_out, sum(total_cube_out/5.0) as avg_cube_out  from\n" +
                    "(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, sum(weight_in) as total_weight_in, sum(cube_in/1157) as total_cube_in, sum(weight_out) as total_weight_out, sum(cube_out)/1157.0 as total_cube_out, case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load   from ffo_extracts_otb_od_head_load_removed\n" +
                    "where load_to_sic3 != '' and load_to_shift3 != '' \n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13) as A\n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12\n" +
                    "having head_load ='X') as A\n" +
                    "on ffo_extracts_otb_od_head_load_removed.orig_sic = A.orig_sic\n" +
                    "and ffo_extracts_otb_od_head_load_removed.orig_shift = A.orig_shift\n" +
                    "and ffo_extracts_otb_od_head_load_removed.load_to_mode1 = A.load_to_mode1\n" +
                    "and ffo_extracts_otb_od_head_load_removed.load_to_sic1 = A.load_to_sic1\n" +
                    "and ffo_extracts_otb_od_head_load_removed.load_to_shift1 = A.load_to_shift1\n" +
                    "and ffo_extracts_otb_od_head_load_removed.must_clear_sic = A.must_clear_sic\n" +
                    "and ffo_extracts_otb_od_head_load_removed.must_clear_shift = A.must_clear_shift\n" +
                    "and ffo_extracts_otb_od_head_load_removed.daylane_freight = A.daylane_freight\n" +
                    "and ffo_extracts_otb_od_head_load_removed.load_to_sic2 = A.load_to_sic2\n" +
                    "and ffo_extracts_otb_od_head_load_removed.load_to_shift2 = A.load_to_shift2\n" +
                    "and ffo_extracts_otb_od_head_load_removed.load_to_sic3 = A.load_to_sic3\n" +
                    "and ffo_extracts_otb_od_head_load_removed.load_to_shift3 = A.load_to_shift3\n" +
                    "where A.avg_weight_in is null;\n" +
                    "\n" +
                    "\n" +
                    "--drop table ffo_extracts_otb_2nd_fac_removed;\n" +
                    "--This table has excluded all possibility of combining at 2nd FAC and beyond;\n" +
                    "create temp table ffo_extracts_otb_2nd_fac_removed as\n" +
                    "select ffo_extracts_otb_3rd_fac_removed.* from ffo_extracts_otb_3rd_fac_removed left outer join \n" +
                    "(select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, '' as load_to_sic3, '' as load_to_shift3, '' as dest_sic, case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load, '' as bypass, count(distinct date)/5.0 as daily_building_frequency, sum(total_weight_in)/5.0 as avg_weight_in, sum(total_cube_in/5.0) as avg_cube_in, sum(total_weight_out)/5.0 as avg_weight_out, sum(total_cube_out/5.0) as avg_cube_out  from\n" +
                    "(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, sum(weight_in) as total_weight_in, sum(cube_in/1157) as total_cube_in, sum(weight_out) as total_weight_out, sum(cube_out)/1157.0 as total_cube_out, case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load   from ffo_extracts_otb_3rd_fac_removed\n" +
                    "where load_to_sic2 != '' and load_to_shift2 != '' \n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11) as A\n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10\n" +
                    "having head_load='X') as A\n" +
                    "on ffo_extracts_otb_3rd_fac_removed.orig_sic = A.orig_sic\n" +
                    "and ffo_extracts_otb_3rd_fac_removed.orig_shift = A.orig_shift\n" +
                    "and ffo_extracts_otb_3rd_fac_removed.load_to_mode1 = A.load_to_mode1\n" +
                    "and ffo_extracts_otb_3rd_fac_removed.load_to_sic1 = A.load_to_sic1\n" +
                    "and ffo_extracts_otb_3rd_fac_removed.load_to_shift1 = A.load_to_shift1\n" +
                    "and ffo_extracts_otb_3rd_fac_removed.must_clear_sic = A.must_clear_sic\n" +
                    "and ffo_extracts_otb_3rd_fac_removed.must_clear_shift = A.must_clear_shift\n" +
                    "and ffo_extracts_otb_3rd_fac_removed.daylane_freight = A.daylane_freight\n" +
                    "and ffo_extracts_otb_3rd_fac_removed.load_to_sic2 = A.load_to_sic2\n" +
                    "and ffo_extracts_otb_3rd_fac_removed.load_to_shift2 = A.load_to_shift2\n" +
                    "where A.avg_weight_in is null;" +
                    "\n" ;

            stmt.executeUpdate(base_sql_updated);

            System.out.println(base_sql_updated);

            //calculateBaseOpportunity()
            //calculateDynamicOpportunity()

            String total_base_freight_query = "select sum(avg_weight_out) as sum_weight from ffo_base_plan_od;";

            ResultSet rs = stmt.executeQuery(total_base_freight_query);

            System.out.println( total_base_freight_query);
            rs.setFetchSize(100);

            int total_freight = 0;
            if (rs.next()) {
                total_freight = rs.getInt("sum_weight");
                if (rs.wasNull()) {
                    System.out.println("no sum weight\n");
                }
            }


            String third_fac_freight_query  = "select sum(weight_out)/5.0 as avg_third_fac_weight from ffo_extracts_otb_od_head_load_removed where load_to_sic3 != '';";
            rs = stmt.executeQuery(third_fac_freight_query);
            rs.setFetchSize(100);
            System.out.println( third_fac_freight_query);

            double third_fac_freight = 0.0;
            if (rs.next()) {
                third_fac_freight = rs.getDouble("avg_third_fac_weight");
                if (rs.wasNull()) {
                    System.out.println("no avg_third_fac_weight\n");
                }
            }

            String second_fac_freight_query = "select sum(weight_out)/5.0 as avg_second_fac_weight from ffo_extracts_otb_od_head_load_removed where load_to_sic2 != '' and load_to_sic3 = '';";
            rs = stmt.executeQuery(second_fac_freight_query);
            rs.setFetchSize(100);
            System.out.println( second_fac_freight_query);


            double second_fac_freight = 0.0;
            if (rs.next()) {
                second_fac_freight = rs.getDouble("avg_second_fac_weight");
                if (rs.wasNull()) {
                    System.out.println("no avg_second_fac_weight\n");
                }
            }

            String first_fac_freight_query = "select sum(weight_out)/5.0 as avg_first_fac_weight from ffo_extracts_otb_od_head_load_removed where load_to_sic1 != '' and load_to_sic2 = '';";
            rs = stmt.executeQuery(first_fac_freight_query);
            rs.setFetchSize(100);
            System.out.println( first_fac_freight_query);

            double first_fac_freight = 0.0;
            if (rs.next()) {
                first_fac_freight = rs.getDouble("avg_first_fac_weight");
                if (rs.wasNull()) {
                    System.out.println("no avg_first_fac_weight\n");
                }
            }


            String freight_combine_3rd_fac_query = "select sum(avg_weight_out) as freight_combine_3rd_fac from (select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, '' as dest_sic, case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load, '' as bypass, count(distinct date)/5.0 as daily_building_frequency, sum(total_weight_in)/5.0 as avg_weight_in, sum(total_cube_in/5.0) as avg_cube_in, sum(total_weight_out)/5.0 as avg_weight_out, sum(total_cube_out/5.0) as avg_cube_out  from\n" +
                    "(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, sum(weight_in) as total_weight_in, sum(cube_in/1157) as total_cube_in, sum(weight_out) as total_weight_out, sum(cube_out)/1157.0 as total_cube_out, case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load   from ffo_extracts_otb_od_head_load_removed\n" +
                    "where load_to_sic3 != '' and load_to_shift3 != '' \n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13) as A\n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12\n" +
                    "having head_load ='X') as A;";

            rs = stmt.executeQuery(freight_combine_3rd_fac_query);
            rs.setFetchSize(100);

            System.out.println(freight_combine_3rd_fac_query);
            double freight_combine_3rd_fac = 0.0;
            if (rs.next()) {
                freight_combine_3rd_fac = rs.getDouble("freight_combine_3rd_fac");
                if (rs.wasNull()) {
                    System.out.println("no freight_combine_3rd_fac\n");
                    System.out.println("freight_combine_3rd_fac is " + freight_combine_3rd_fac + "\n");
                }
            }


            String freight_combine_2nd_fac_query = "select sum(avg_weight_out) as freight_combine_2nd_fac from (select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, '' as load_to_sic3, '' as load_to_shift3, '' as dest_sic, case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load, '' as bypass, count(distinct date)/5.0 as daily_building_frequency, sum(total_weight_in)/5.0 as avg_weight_in, sum(total_cube_in/5.0) as avg_cube_in, sum(total_weight_out)/5.0 as avg_weight_out, sum(total_cube_out/5.0) as avg_cube_out  from\n" +
                    "(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, sum(weight_in) as total_weight_in, sum(cube_in/1157) as total_cube_in, sum(weight_out) as total_weight_out, sum(cube_out)/1157.0 as total_cube_out, case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load   from ffo_extracts_otb_3rd_fac_removed\n" +
                    "where load_to_sic2 != '' and load_to_shift2 != '' \n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11) as A\n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10\n" +
                    "having head_load='X') as A;";

            rs = stmt.executeQuery(freight_combine_2nd_fac_query);
            rs.setFetchSize(100);
            System.out.println(freight_combine_2nd_fac_query);

            double freight_combine_2nd_fac = 0.0;
            if (rs.next()) {
                freight_combine_2nd_fac = rs.getDouble("freight_combine_2nd_fac");
                if (rs.wasNull()) {
                    System.out.println("no freight_combine_2nd_fac\n");
                }
            }


            //total rehandle times for NGV = 1st FAC * 1 + 2nd FAC * 2 + 3rd FAC * 3 - Freight that can be combined at 3rd FAC * 2 - freight that can be combined at 2nd FAC * 1 = 640137/675162 = 94.8%
            double rehandle_capability = (first_fac_freight + second_fac_freight * 2 + third_fac_freight * 3 - freight_combine_3rd_fac * 2 - freight_combine_2nd_fac)/total_freight;

            System.out.println(sic + " base rehandle_capability is  " + rehandle_capability + "\n");

        }
        catch(Exception e)
        {
            System.out.println
                    ("Error, SQL failed.\n");
        }
    }
    void calculateDynamicPlanOpportunity(String sic, Statement stmt)
    {

        int rowCounter=0;
        int scheduleCounter = 0;

        try
        {


            String base_sql_updated = "create temp table ffo_dynamic_od as\n" +
                    "select *  from ffo_extracts_otb where cube_out/1157.0>=0.4;create temp table ffo_extracts_otb_headload_removed as\n" +
                    "select ffo_extracts_otb.* from ffo_extracts_otb left outer join ffo_dynamic_od on \n" +
                    "ffo_extracts_otb.orig_sic = ffo_dynamic_od.orig_sic and\n" +
                    "ffo_extracts_otb.dest_sic = ffo_dynamic_od.dest_sic and\n" +
                    "ffo_extracts_otb.date = ffo_dynamic_od.date\n" +
                    "where ffo_dynamic_od.orig_sic is null and ffo_dynamic_od.dest_sic is null;\n" +
                    "\n" +
                    "\n" +
                    "---This table has excluded all possibility of combining at 3rd FAC and beyond;\n" +
                    "create temp table ffo_extracts_otb_3rd_fac_removed_dynamic as\n" +
                    "select B.* from ffo_extracts_otb_headload_removed as B left outer join \n" +
                    "(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, sum(weight_out) as total_weight_out  from ffo_extracts_otb_headload_removed\n" +
                    "where load_to_sic3 != '' and load_to_shift3 != '' \n" +
                    "group by 1, 2, 3, 4,5,6,7,8,9,10,11, 12, 13\n" +
                    "having sum(cube_out/1157.0) >= 0.4 ) as A\n" +
                    "on B.date = A.date\n" +
                    "and B.orig_sic = A.orig_sic\n" +
                    "and B.orig_shift = A.orig_shift\n" +
                    "and B.load_to_mode1 = A.load_to_mode1\n" +
                    "and B.load_to_sic1 = A.load_to_sic1\n" +
                    "and B.load_to_shift1 = A.load_to_shift1\n" +
                    "and B.must_clear_sic = A.must_clear_sic\n" +
                    "and B.must_clear_shift = A.must_clear_shift\n" +
                    "and B.daylane_freight = A.daylane_freight\n" +
                    "and B.load_to_sic2 = A.load_to_sic2\n" +
                    "and B.load_to_shift2 = A.load_to_shift2\n" +
                    "and B.load_to_sic3 = A.load_to_sic3\n" +
                    "and B.load_to_shift3 = A.load_to_shift3\n" +
                    "where A.total_weight_out is null;\n" +
                    "\n" +
                    "\n" +
                    "--This table has excluded all possibility of combining at 2nd FAC and beyond;\n" +
                    "create temp table ffo_extracts_otb_2nd_fac_removed_dynamic as\n" +
                    "select B.* from ffo_extracts_otb_3rd_fac_removed_dynamic as B left outer join \n" +
                    "(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, sum(weight_out) as total_weight_out  from ffo_extracts_otb_3rd_fac_removed_dynamic\n" +
                    "where load_to_sic2 != '' and load_to_shift2 != '' \n" +
                    "group by 1, 2, 3, 4,5,6,7,8,9,10, 11\n" +
                    "having sum(cube_out/1157.0) >= 0.4 ) as A\n" +
                    "on B.date = A.date\n" +
                    "and B.orig_sic = A.orig_sic\n" +
                    "and B.orig_shift = A.orig_shift\n" +
                    "and B.load_to_mode1 = A.load_to_mode1\n" +
                    "and B.load_to_sic1 = A.load_to_sic1\n" +
                    "and B.load_to_shift1 = A.load_to_shift1\n" +
                    "and B.must_clear_sic = A.must_clear_sic\n" +
                    "and B.must_clear_shift = A.must_clear_shift\n" +
                    "and B.daylane_freight = A.daylane_freight\n" +
                    "and B.load_to_sic2 = A.load_to_sic2\n" +
                    "and B.load_to_shift2 = A.load_to_shift2\n" +
                    "where A.total_weight_out is null;" ;

            stmt.executeUpdate(base_sql_updated);


            String total_dynamic_freight_query = "select sum(weight_out)/5.0 as sum_weight from ffo_extracts_otb;";

            ResultSet rs = stmt.executeQuery(total_dynamic_freight_query);
            rs.setFetchSize(100);

            int total_freight = 0;
            if (rs.next()) {
                total_freight = rs.getInt("sum_weight");
                if (rs.wasNull()) {
                    System.out.println("no sum weight\n");
                }
            }


            String third_fac_freight_query  = "select sum(weight_out)/5.0 as avg_third_fac_weight from ffo_extracts_otb_headload_removed where load_to_sic3 != '';";
            rs = stmt.executeQuery(third_fac_freight_query);
            rs.setFetchSize(100);

            double third_fac_freight = 0.0;
            if (rs.next()) {
                third_fac_freight = rs.getDouble("avg_third_fac_weight");
                if (rs.wasNull()) {
                    System.out.println("no avg_third_fac_weight\n");
                }
            }

            String second_fac_freight_query = "select sum(weight_out)/5.0 as avg_second_fac_weight from ffo_extracts_otb_headload_removed where load_to_sic2 != '' and load_to_sic3 = '';";
            rs = stmt.executeQuery(second_fac_freight_query);
            rs.setFetchSize(100);


            double second_fac_freight = 0.0;
            if (rs.next()) {
                second_fac_freight = rs.getDouble("avg_second_fac_weight");
                if (rs.wasNull()) {
                    System.out.println("no avg_second_fac_weight\n");
                }
            }

            String first_fac_freight_query = "select sum(weight_out)/5.0 as avg_first_fac_weight from ffo_extracts_otb_headload_removed where load_to_sic1 != '' and load_to_sic2 = '';";
            rs = stmt.executeQuery(first_fac_freight_query);
            rs.setFetchSize(100);

            double first_fac_freight = 0.0;
            if (rs.next()) {
                first_fac_freight = rs.getDouble("avg_first_fac_weight");
                if (rs.wasNull()) {
                    System.out.println("no avg_first_fac_weight\n");
                }
            }

            String freight_combine_3rd_fac_query = "select sum(total_weight_out)/5.0 as freight_combine_3rd_fac from (select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, sum(weight_out) as total_weight_out  from ffo_extracts_otb_headload_removed\n" +
                    "where load_to_sic3 != '' and load_to_shift3 != ''\n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13\n" +
                    "having sum(cube_out/1157.0) >= 0.4) as A;";

            rs = stmt.executeQuery(freight_combine_3rd_fac_query);
            rs.setFetchSize(100);

            double freight_combine_3rd_fac = 0.0;
            if (rs.next()) {
                freight_combine_3rd_fac = rs.getDouble("freight_combine_3rd_fac");
                if (rs.wasNull()) {
                    System.out.println("no freight_combine_3rd_fac\n");
                }
            }


            String freight_combine_2nd_fac_query = "select sum(total_weight_out)/5.0 as freight_combine_2nd_fac from (select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, sum(weight_out) as total_weight_out  from ffo_extracts_otb_3rd_fac_removed_dynamic\n" +
                    "where load_to_sic2 != '' and load_to_shift2 != ''\n" +
                    "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11\n" +
                    "having sum(cube_out/1157.0) >= 0.4) as A;";

            rs = stmt.executeQuery(freight_combine_2nd_fac_query);
            rs.setFetchSize(100);

            double freight_combine_2nd_fac = 0.0;
            if (rs.next()) {
                freight_combine_2nd_fac = rs.getDouble("freight_combine_2nd_fac");
                if (rs.wasNull()) {
                    System.out.println("no freight_combine_2nd_fac\n");
                }
            }

            //total rehandle times for NGV = 1st FAC * 1 + 2nd FAC * 2 + 3rd FAC * 3 - Freight that can be combined at 3rd FAC * 2 - freight that can be combined at 2nd FAC * 1 = 640137/675162 = 94.8%
            double rehandle_capability = (first_fac_freight + second_fac_freight * 2 + third_fac_freight * 3 - freight_combine_3rd_fac * 2 - freight_combine_2nd_fac)/total_freight;

            System.out.println(sic + " first fac freight is  " + first_fac_freight + "\n second fac freight is  " + second_fac_freight + "\n" + "third fac freight is  " + third_fac_freight + "\n" );
            System.out.println(sic + " freight_combine_3rd_fac is  " + freight_combine_3rd_fac + "\n" + " freight_combine_2nd_fac is  " + freight_combine_2nd_fac + "\n" );

            System.out.println(sic + " dynamic rehandle_capability is  " + rehandle_capability + "\n");

        }
        catch(Exception e)
        {
            System.out.println
                    ("Error, SQL failed.\n");
        }
    }
    public static Connection getConnection_PRD_WHSEVIEW() throws SQLException
    {
        try
        {
            Class.forName("org.netezza.Driver").newInstance();
            Connection c = DriverManager.getConnection("jdbc:netezza://npsdwh.con-way.com/PRD_WHSEVIEW", "MIGUEL.BUHAY", "miguel082416");
            c.setAutoCommit(false);
            return c;
        }
        catch (Exception e)
        {
            System.out.println
                    ("Error, Connection not made.\n");
        }
        return null;

    }
}
