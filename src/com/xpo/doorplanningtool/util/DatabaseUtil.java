package com.xpo.doorplanningtool.util;

import com.xpo.doorplanningtool.vo.Plan;

import java.sql.*;

public class DatabaseUtil {


    public static ResultSet executeAddDoorQuery(Statement statement, Plan plan) throws SQLException {
        String add_door_sql_query = "select distinct A.door_sic from \n" +
                "sic_doors_tmp as A\n" +
                "left join\n" +
                "sic_doors_tmp as B\n" +
                " on\n" +
                "A.door_sic = B.door_sic and\n" +
                "A.orig_sic = B.orig_sic and \n" +
                "B.launch_date = A.launch_date -7 and\n" +
                "A.shift = B.shift\n" +
                "where A.launch_date = '" + plan.getInstruction_date() + "' and A.orig_sic='" + plan.getSic() + "' and B.door_sic is null and A.shift = '" + plan.getShift() + "'\n"  +
                "order by A.door_sic; ";

        return statement.executeQuery(add_door_sql_query);
    }

    public static ResultSet executeRemoveDoorQuery(Statement statement, Plan plan) throws SQLException {
        String remove_door_sql_query = "select A.door_sic from (select distinct door_sic from \n" +
                "sic_doors_tmp where launch_date= '" + plan.getPrevious_instruction_date() + "' and orig_sic='" + plan.getSic() + "' and shift = '" + plan.getShift() + "' ) as A\n" +
                "left join\n" +
                "(select distinct door_sic from \n" +
                "sic_doors_tmp where launch_date='" + plan.getInstruction_date() + "' and orig_sic = '" + plan.getSic() + "' and shift ='" + plan.getShift() + "') as B on A.door_sic = B.door_sic  where B.door_sic is null ;\n";

        return statement.executeQuery(remove_door_sql_query);
    }

    public static ResultSet executeQuery1(Statement statement, Plan plan) throws SQLException {
        String sql_query1 = "select *  from ffo_planning_tool order by orig_sic, orig_shift, load_to_mode1, load_to_sic1 desc, load_to_shift1 desc, must_clear_sic desc, must_clear_shift desc, daylane_freight desc, load_to_sic2 desc, load_to_shift2 desc, load_to_sic3 desc, load_to_shift3 desc, dest_sic desc;";
        System.out.println(sql_query1);
        ResultSet rs = statement.executeQuery(sql_query1);
        return rs;
    }

    public static void executeUpdate1(Statement statement, Plan plan) throws SQLException {

        String shift = plan.getShift();
        String beginning_date = plan.getBeginning_date();
        String ending_date = plan.getEnding_date();
        String sic = plan.getSic();
        String shift_abbr = plan.getShift_abbr();
        String bypass_frequency = plan.getBypass_frequency();
        String loc_load_plan_shift_cd = plan.getLoc_load_plan_shift_cd();

        String sql_ffo_extract_temp = "create temp table ffo_extracts_temp as\n" +
                "select cldr_dt as date, orig_sic_cd as orig_sic,  fnl_dest_sic_cd as dest_sic, orig_in_wgt as weight_in, orig_in_vol as cube_in, orig_out_wgt as weight_out, orig_out_vol as cube_out from FLO_FLOW_PLAN_SUMMARY_VW "+
                "where orig_shft_cd = '" + shift + "' and date >= '"+ beginning_date + "' and date <= '" + ending_date + "' \n" +
                "and orig_sic='" + sic + "' distribute on (orig_sic, dest_sic); \n";

           /* if (fac_shift)
            {
                sql_ffo_extract_temp = "create temp table ffo_extracts_temp as\n" +
                        "select cldr_dt as date, LD_TO_1_SIC_CD as orig_sic,  fnl_dest_sic_cd as dest_sic, orig_in_wgt as weight_in, orig_in_vol as cube_in, orig_out_wgt as weight_out, orig_out_vol as cube_out from FLO_FLOW_PLAN_SUMMARY_VW "+
                        "where orig_shft_cd = 'OTB' and date >= '"+ beginning_date + "' and date <= '" + ending_date + "' \n" +
                        "and LD_TO_1_SIC_CD='" + sic + "' distribute on (orig_sic, dest_sic); \n";
            } */

        String sql_updated = sql_ffo_extract_temp +
                "create temp table current_loc_load_plan as\n" +
                "select * from tbl_loc_load_plan_rds_vw where REPL_LST_UPDT_TMST>'" + ending_date + "' and current_sic='" + sic + "' and shift_cd='"+ loc_load_plan_shift_cd + "';\n"+
                "create temp table ffo_extracts_otb as (select a.date as date, a.orig_sic as orig_sic, '" + shift_abbr + "' as orig_shift,a.dest_sic as dest_sic, '' as load_to_mode1, '' as load_to_shift1, case b.day_frt_ind when 'D' then b.frst_fac_sic||' D'  else b.frst_fac_sic end as load_to_sic1, '' as load_to_shift2, b.scnd_fac_sic as load_to_sic2, '' as load_to_shift3,b.thrd_fac_sic as load_to_sic3, b.must_clear_sic as must_clear_sic, '' as must_clear_shift, case b.day_frt_ind when 'D' then 'Y'  else '' end as daylane_freight, a.weight_in as weight_in, a.cube_in as cube_in, a.weight_out as weight_out, a.cube_out as cube_out \n" +
                "from ffo_extracts_temp as a \n" +
                "join\n" +
                "current_loc_load_plan as b\n" +
                "on a.orig_sic = b.current_sic\n" +
                "and a.dest_sic = b.dest_sic);" +
                "\n" +
                "update ffo_extracts_otb set load_to_sic1='' where load_to_sic1 is null;\n" +
                "update ffo_extracts_otb set load_to_sic2='' where load_to_sic2 is null;\n" +
                "update ffo_extracts_otb set load_to_sic3='' where load_to_sic3 is null;\n" +
                "\n" +
                "update ffo_extracts_otb set must_clear_sic ='' where must_clear_sic is null;\n" +
                "\n" +
                "update ffo_extracts_otb set must_clear_sic = '' where daylane_freight = 'Y';\n" +
                "\n" +
                "\n" +
                "\n" +
                "create  temp table ffo_base_plan_od as\n" +
                "(select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, dest_sic, sum(case when cube_out/1270.0 >=0.4 or weight_out >=22500 then 1 else 0 end) as head_load_hit_days, head_load_hit_days/5.0 as head_load_hit_ratio, case when head_load_hit_days =0 then 0 else sum(case when cube_out/1270.0 >=0.4 or weight_out >=22500 then weight_out else 0 end)/head_load_hit_days end as head_load_avg_weight, case when head_load_hit_days=0 then 0 else sum(case when cube_out/1270.0 >=0.4 or weight_out >=22500 then cube_out/1270.0 else 0 end)/head_load_hit_days end as head_load_avg_cube, sum(case when cube_out/1270.0 >=0.85 or weight_out >=22500 then 1 else 0 end) as bypass_hit_days,  bypass_hit_days/5.0 as bypass_hit_ratio, case when bypass_hit_days = 0 then 0 else sum(case when cube_out/1270.0 >=0.85 or weight_out >=22500 then weight_out else 0 end)/bypass_hit_days end as bypass_avg_weight, case when bypass_hit_days = 0 then 0 else sum(case when cube_out/1270.0 >=0.85 or weight_out >=22500 then cube_out/1270.0 else 0 end)/bypass_hit_days end as bypass_avg_cube, case when head_load_hit_ratio >= 0.6 then 'X' else '' end as head_load, case when bypass_hit_ratio >=" + bypass_frequency + " then 'X' else '' end as bypass, sum(cube_out/1270.0)/5.0 as avg_cube, sum(weight_out)/5.0 as avg_weight  from ffo_extracts_otb \n" +
                "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);\n" +
                "\n" +
                "create temp table exclusive_lanes as\n" +
                "select LD_AT_NODE_NM, LD_AT_SHFT_CD, LD_TO_NODE_NM, LD_TO_SHFT_CD, LD_LEG_TYP from prd_whseview..FLO_LOAD_LEG_VW where ld_leg_typ = 0 and move_mode_typ = 'S';\n" +
                "\n" +
                "create temp table ffo_base_plan_od_exclusive as \n" +
                "select A.orig_sic, A.orig_shift, A.load_to_mode1, A.load_to_sic1, A.load_to_shift1, A.must_clear_sic, A.must_clear_shift, A.daylane_freight, A.load_to_mode2, A.load_to_sic2, A.load_to_shift2,case when B.ld_leg_typ is null then '' else 'HSS' end as load_to_mode3,  A.load_to_sic3, A.load_to_shift3, A.dest_sic,A.head_load,A.bypass,A.head_load_hit_ratio, A.head_load_avg_weight,A.head_load_avg_cube,A.bypass_hit_ratio, A.bypass_avg_weight,A.bypass_avg_cube, A.avg_weight, A.avg_cube from (select A.orig_sic, A.orig_shift, A.load_to_mode1, A.load_to_sic1, A.load_to_shift1, A.must_clear_sic, A.must_clear_shift, A.daylane_freight, case when B.ld_leg_typ is null then '' else 'HSS' end as load_to_mode2, A.load_to_sic2, A.load_to_shift2, A.load_to_sic3, A.load_to_shift3, A.dest_sic,A.head_load_hit_ratio, A.head_load_avg_weight,A.head_load_avg_cube,A.bypass_hit_ratio, A.bypass_avg_weight,A.bypass_avg_cube, A.head_load,A.bypass, A.avg_weight, A.avg_cube from ffo_base_plan_od as A left outer join exclusive_lanes as B\n" +
                "on A.load_to_sic1 = B.ld_at_node_nm and\n" +
                "      A.load_to_shift1 = B.ld_at_shft_cd and\n" +
                "      A.load_to_sic2 = B.ld_to_node_nm and\n" +
                "      A.load_to_shift2 = B.ld_to_shft_cd) as A\n" +
                "left outer join exclusive_lanes as B\n" +
                "on\n" +
                "      A.load_to_sic2 = B.ld_at_node_nm and\n" +
                "      A.load_to_shift2 = B.ld_at_shft_cd and\n" +
                "      A.load_to_sic3 = B.ld_to_node_nm and\n" +
                "      A.load_to_shift3 = B.ld_to_shft_cd;\n" +
                "\n" +
                "\n" +
                "create temp table ffo_base_plan_3rd_fac_combined as \n" +
                "select orig_sic,\n" +
                "       orig_shift,\n" +
                "       load_to_mode1,\n" +
                "       load_to_sic1,\n" +
                "       load_to_shift1,\n" +
                "       must_clear_sic,\n" +
                "       must_clear_shift,\n" +
                "       daylane_freight,\n" +
                "       '' as load_to_mode2,\n" +
                "       load_to_sic2,\n" +
                "       load_to_shift2,\n" +
                "       '' as load_to_mode3,\n" +
                "       load_to_sic3,\n" +
                "       load_to_shift3,\n" +
                "       '' as dest_sic,\n" +
                "       case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load,\n" +
                "       case when sum(mark_bypass)/5.0 >= " + bypass_frequency + " then 'X' else '' end as bypass,\n" +
                "       sum(mark_head_load)/5.0 as head_load_hit_ratio,\n" +
                "       case when sum(mark_head_load) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_head_load) end as head_load_avg_weight, \n" +
                "       case when sum(mark_head_load)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_head_load) end as head_load_avg_cube, \n" +
                "       sum(mark_bypass)/5.0 as bypass_hit_ratio, \n" +
                "       case when sum(mark_bypass) =0 then 0 else sum(case when total_cube_out >=0.85 then total_weight_out else 0 end)/sum(mark_bypass) end as bypass_avg_weight, \n" +
                "       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.85 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube,\n" +
                "       sum(total_weight_out)/5.0 as avg_weight,\n" +
                "       sum(total_cube_out)/5.0 as avg_cube\n" +
                " from\n" +
                "      (select date,\n" +
                "              orig_sic,\n" +
                "              orig_shift,\n" +
                "              load_to_mode1,\n" +
                "              load_to_sic1,\n" +
                "              load_to_shift1,\n" +
                "              must_clear_sic, \n" +
                "              must_clear_shift,\n" +
                "              daylane_freight,\n" +
                "              load_to_sic2,\n" +
                "              load_to_shift2,\n" +
                "              load_to_sic3,\n" +
                "              load_to_shift3,\n" +
                "              sum(weight_out) as total_weight_out,\n" +
                "              sum(cube_out)/1270.0 as total_cube_out,\n" +
                "              case when total_cube_out>=0.4 or total_weight_out >=22500 then 1 else 0 end as mark_head_load,\n" +
                "              case when total_cube_out>=0.85 or total_weight_out >=22500 then 1 else 0 end as mark_bypass   \n" +
                "       from ffo_extracts_otb\n" +
                "       where load_to_sic3 != ''  \n" +
                "       group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13\n" +
                "      ) as A\n" +
                "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,13, 14;\n" +
                "\n" +
                "\n" +
                "create temp table ffo_base_plan_2nd_fac_combined as \n" +
                "select orig_sic,\n" +
                "       orig_shift,\n" +
                "       load_to_mode1,\n" +
                "       load_to_sic1,\n" +
                "       load_to_shift1,\n" +
                "       must_clear_sic,\n" +
                "       must_clear_shift,\n" +
                "       daylane_freight,\n" +
                "       '' as load_to_mode2,\n" +
                "       load_to_sic2,\n" +
                "       load_to_shift2,\n" +
                "       '' as load_to_mode3,\n" +
                "       '' as load_to_sic3,\n" +
                "       '' as load_to_shift3,\n" +
                "       '' as dest_sic,\n" +
                "       case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load,\n" +
                "       case when sum(mark_bypass)/5.0 >= "+ bypass_frequency + " then 'X' else '' end as bypass,\n" +
                "       sum(mark_head_load)/5.0 as head_load_hit_ratio,\n" +
                "       case when sum(mark_head_load) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_head_load) end as head_load_avg_weight, \n" +
                "       case when sum(mark_head_load)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_head_load) end as head_load_avg_cube, \n" +
                "       sum(mark_bypass)/5.0 as bypass_hit_ratio, \n" +
                "       case when sum(mark_bypass) =0 then 0 else sum(case when total_cube_out >=0.85 then total_weight_out else 0 end)/sum(mark_bypass) end as bypass_avg_weight, \n" +
                "       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.85 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube, \n" +
                "       sum(total_weight_out)/5.0 as avg_weight, \n" +
                "       sum(total_cube_out)/5.0 as avg_cube \n" +
                " from\n" +
                "      (select date,\n" +
                "              orig_sic,\n" +
                "              orig_shift,\n" +
                "              load_to_mode1,\n" +
                "              load_to_sic1,\n" +
                "              load_to_shift1,\n" +
                "              must_clear_sic, \n" +
                "              must_clear_shift,\n" +
                "              daylane_freight,\n" +
                "              load_to_sic2,\n" +
                "              load_to_shift2,\n" +
                "              sum(weight_out) as total_weight_out,\n" +
                "              sum(cube_out)/1270.0 as total_cube_out,\n" +
                "              case when total_cube_out>=0.4 or total_weight_out >=22500 then 1 else 0 end as mark_head_load,\n" +
                "              case when total_cube_out>=0.85 or total_weight_out >=22500 then 1 else 0 end as mark_bypass   \n" +
                "       from ffo_extracts_otb\n" +
                "       where load_to_sic2 != '' \n" +
                "       group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11\n" +
                "      ) as A\n" +
                "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12;\n" +
                "\n" +
                "\n" +
                "\n" +
                "create temp table ffo_base_plan_must_clear_sic_combined as \n" +
                "select orig_sic,\n" +
                "       orig_shift,\n" +
                "       load_to_mode1,\n" +
                "       load_to_sic1,\n" +
                "       load_to_shift1,\n" +
                "       must_clear_sic,\n" +
                "       must_clear_shift,\n" +
                "       daylane_freight,\n" +
                "       '' as load_to_mode2,\n" +
                "       '' as load_to_sic2,\n" +
                "       '' as load_to_shift2,\n" +
                "       '' as load_to_mode3,\n" +
                "       '' as load_to_sic3,\n" +
                "       '' as load_to_shift3,\n" +
                "       '' as dest_sic,\n" +
                "       case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load,\n" +
                "       case when sum(mark_bypass)/5.0 >= " + bypass_frequency + " then 'X' else '' end as bypass,\n" +
                "       sum(mark_head_load)/5.0 as head_load_hit_ratio,\n" +
                "       case when sum(mark_head_load) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_head_load) end as head_load_avg_weight, \n" +
                "       case when sum(mark_head_load)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_head_load) end as head_load_avg_cube, \n" +
                "       sum(mark_bypass)/5.0 as bypass_hit_ratio, \n" +
                "       case when sum(mark_bypass) =0 then 0 else sum(case when total_cube_out >=0.85 then total_weight_out else 0 end)/sum(mark_bypass) end as bypass_avg_weight, \n" +
                "       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.85 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube, \n" +
                "       sum(total_weight_out)/5.0 as avg_weight,\n" +
                "       sum(total_cube_out)/5.0 as avg_cube \n" +
                "  from\n" +
                "      (select date,\n" +
                "              orig_sic,\n" +
                "              orig_shift,\n" +
                "              load_to_mode1,\n" +
                "              load_to_sic1,\n" +
                "              load_to_shift1,\n" +
                "              must_clear_sic, \n" +
                "              must_clear_shift,\n" +
                "              daylane_freight,\n" +
                "              sum(weight_out) as total_weight_out,\n" +
                "              sum(cube_out)/1270.0 as total_cube_out,\n" +
                "              case when total_cube_out>=0.4 or total_weight_out >=22500 then 1 else 0 end as mark_head_load,\n" +
                "              case when total_cube_out>=0.85 or total_weight_out >=22500 then 1 else 0 end as mark_bypass   \n" +
                "       from ffo_extracts_otb\n" +
                "       where must_clear_sic != '' \n" +
                "       group by 1, 2, 3, 4, 5, 6, 7, 8, 9\n" +
                "      ) as A\n" +
                "group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10;\n" +
                "\n" +
                "create temp table ffo_base_plan_1st_fac_combined as \n" +
                "select orig_sic,\n" +
                "       orig_shift,\n" +
                "       load_to_mode1,\n" +
                "       load_to_sic1,\n" +
                "       load_to_shift1,\n" +
                "       '' as must_clear_sic,\n" +
                "       '' as must_clear_shift,\n" +
                "       '' as daylane_freight,\n" +
                "       '' as load_to_mode2,\n" +
                "       '' as load_to_sic2,\n" +
                "       '' as load_to_shift2,\n" +
                "       '' as load_to_mode3,\n" +
                "       '' as load_to_sic3,\n" +
                "       '' as load_to_shift3,\n" +
                "       '' as dest_sic,\n" +
                "       case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load,\n" +
                "       case when sum(mark_bypass)/5.0 >= " + bypass_frequency + " then 'X' else '' end as bypass,\n" +
                "       sum(mark_head_load)/5.0 as head_load_hit_ratio,\n" +
                "       case when sum(mark_head_load) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_head_load) end as head_load_avg_weight, \n" +
                "       case when sum(mark_head_load)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_head_load) end as head_load_avg_cube, \n" +
                "       sum(mark_bypass)/5.0 as bypass_hit_ratio, \n" +
                "       case when sum(mark_bypass) =0 then 0 else sum(case when total_cube_out >=0.85 then total_weight_out else 0 end)/sum(mark_bypass) end as bypass_avg_weight, \n" +
                "       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.85 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube, \n" +
                "       sum(total_weight_out)/5.0 as avg_weight,\n" +
                "       sum(total_cube_out)/5.0 as avg_cube \n" +
                "  from\n" +
                "      (select date,\n" +
                "              orig_sic,\n" +
                "              orig_shift,\n" +
                "              load_to_mode1,\n" +
                "              load_to_sic1,\n" +
                "              load_to_shift1,\n" +
                "              sum(weight_out) as total_weight_out,\n" +
                "              sum(cube_out)/1270.0 as total_cube_out,\n" +
                "              case when total_cube_out>=0.4 or total_weight_out >=22500 then 1 else 0 end as mark_head_load,\n" +
                "              case when total_cube_out>=0.85 or total_weight_out >=22500 then 1 else 0 end as mark_bypass   \n" +
                "       from ffo_extracts_otb\n" +
                "       where load_to_sic1 != '' \n" +
                "       group by 1, 2, 3, 4, 5, 6\n" +
                "      ) as A\n" +
                "group by 1, 2, 3, 4, 5, 6, 7;\n" +
                "create temp table ffo_planning_tool as \n" +
                "((select * from ffo_base_plan_od_exclusive \n" +
                "union\n" +
                "--group by load_to_sic3 and load_to_shift3\n" +
                "select * from ffo_base_plan_3rd_fac_combined\n" +
                "union\n" +
                "--group by load_to_sic2 and load_to_shift2\n" +
                "select * from ffo_base_plan_2nd_fac_combined\n" +
                "union\n" +
                "--group by must_clear_sic and must_clear_shift\n" +
                "select * from ffo_base_plan_must_clear_sic_combined\n" +
                "union\n" +
                "select * from ffo_base_plan_1st_fac_combined)\n" +
                "--group by load_to_sic1 and load_to_shift1\n" +
                "\n" +
                "order by orig_sic, orig_shift, load_to_mode1, load_to_sic1 desc, load_to_shift1 desc, must_clear_sic desc, must_clear_shift desc, load_to_sic2 desc, load_to_shift2 desc, load_to_sic3 desc, load_to_shift3 desc, dest_sic desc\n" +
                ");\n"
                +
                   /* " insert into sic_doors as \n" +
                    "(select \"" + instruction_date + "\" as launch_date, dest_sic as sic, "+ sic + " as orig_sic from ffo_base_plan_od_exclusive where head_load ='X'\n" +
                    "union\n" +
                    "select \"" + instruction_date + "\" as launch_date, load_to_sic3 as sic,  "+ sic + " from ffo_base_plan_3rd_fac_combined where head_load ='X'\n" +
                    "union\n" +
                    "select \"" + instruction_date + "\" as launch_date, load_to_sic2 as sic, "+ sic + "  from ffo_base_plan_2nd_fac_combined where head_load ='X'\n" +
                    "union\n" +
                    "select \"" + instruction_date + "\" as launch_date, must_clear_sic as sic, "+ sic + "   from ffo_base_plan_must_clear_sic_combined where head_load ='X'\n" +
                    "union\n" +
                    "select \"" + instruction_date + "\" as launch_date, load_to_sic1 as sic, "+ sic + " from ffo_base_plan_1st_fac_combined where head_load ='X');";    */
                " create temp table sic_doors as \n" +
                "(select  dest_sic as sic, bypass from ffo_base_plan_od_exclusive where head_load ='X'\n" +
                "union\n" +
                "select load_to_sic3 as sic, bypass from ffo_base_plan_3rd_fac_combined where head_load ='X'\n" +
                "union\n" +
                "select load_to_sic2 as sic, bypass from ffo_base_plan_2nd_fac_combined where head_load ='X'\n" +
                "union\n" +
                "select  must_clear_sic as sic, bypass  from ffo_base_plan_must_clear_sic_combined where head_load ='X'\n" +
                "union\n" +
                "select load_to_sic1 as sic, bypass from ffo_base_plan_1st_fac_combined where head_load ='X');";

        System.out.println(sql_updated);
        statement.executeUpdate(sql_updated);

    }
}
