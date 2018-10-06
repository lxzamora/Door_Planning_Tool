select * from tbl_loc_load_plan_rds_vw limit 10;


create temp table ffo_extracts_temp as
select cldr_dt as date, orig_sic_cd as orig_sic,  fnl_dest_sic_cd as dest_sic, orig_in_wgt as weight_in, orig_in_vol as cube_in, orig_out_wgt as weight_out, orig_out_vol as cube_out from FLO_FLOW_PLAN_SUMMARY_VW
where orig_shft_cd = 'OTB' and date >= '2014-03-07' and date <= '2014-03-14' 
and orig_sic='NGV' distribute on (orig_sic, dest_sic); 


create temp table current_loc_load_plan as
select * from tbl_loc_load_plan_rds_vw where REPL_LST_UPDT_TMST>'2014-03-13' and current_sic='NGV';



create temp table ffo_extracts_otb as  
select a.date as date, a.orig_sic as orig_sic, 'O' as orig_shift,a.dest_sic as dest_sic, '' as load_to_mode1, '' as load_to_shift1, case b.day_frt_ind when 'D' then b.frst_fac_sic||' D'  else b.frst_fac_sic end as load_to_sic1, '' as load_to_shift2, b.scnd_fac_sic as load_to_sic2, '' as load_to_shift3,b.thrd_fac_sic as load_to_sic3, b.must_clear_sic as must_clear_sic, '' as must_clear_shift, case b.day_frt_ind when 'D' then 'Y'  else '' end as daylane_freight, a.weight_in as weight_in, a.cube_in as cube_in, a.weight_out as weight_out, a.cube_out as cube_out 
from ffo_extracts_temp as a 
join
current_loc_load_plan as b
on a.orig_sic = b.current_sic
and a.dest_sic = b.dest_sic;

update ffo_extracts_otb set load_to_sic1='' where load_to_sic1 is null;
update ffo_extracts_otb set load_to_sic2='' where load_to_sic2 is null;
update ffo_extracts_otb set load_to_sic3='' where load_to_sic3 is null;

update ffo_extracts_otb set must_clear_sic ='' where must_clear_sic is null;

update ffo_extracts_otb set must_clear_sic = '' where daylane_freight = 'Y';



create  temp table ffo_base_plan_od as
(select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, dest_sic, sum(case when cube_out/1157.0 >=0.4 or weight_out >=22500 then 1 else 0 end) as head_load_hit_days, head_load_hit_days/5.0 as head_load_hit_ratio, case when head_load_hit_days =0 then 0 else sum(case when cube_out/1157.0 >=0.4 or weight_out >=22500 then weight_out else 0 end)/head_load_hit_days end as head_load_avg_weight, case when head_load_hit_days=0 then 0 else sum(case when cube_out/1157.0 >=0.4 or weight_out >=22500 then cube_out/1157.0 else 0 end)/head_load_hit_days end as head_load_avg_cube, sum(case when cube_out/1157.0 >=0.85 or weight_out >=22500 then 1 else 0 end) as bypass_hit_days,  bypass_hit_days/5.0 as bypass_hit_ratio, case when bypass_hit_days = 0 then 0 else sum(case when cube_out/1157.0 >=0.85 or weight_out >=22500 then weight_out else 0 end)/bypass_hit_days end as bypass_avg_weight, case when bypass_hit_days = 0 then 0 else sum(case when cube_out/1157.0 >=0.85 or weight_out >=22500 then cube_out/1157.0 else 0 end)/bypass_hit_days end as bypass_avg_cube, case when head_load_hit_ratio >= 0.6 then 'X' else '' end as head_load, case when bypass_hit_ratio >= 0.8 then 'X' else '' end as bypass, sum(cube_out/1157.0)/5.0 as avg_cube, sum(weight_out)/5.0 as avg_weight  from ffo_extracts_otb 
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);

select * from ffo_base_plan_od;

create temp table exclusive_lanes as
select LD_AT_NODE_NM, LD_AT_SHFT_CD, LD_TO_NODE_NM, LD_TO_SHFT_CD, LD_LEG_TYP from prd_whseview..FLO_LOAD_LEG_VW where ld_leg_typ = 0 and move_mode_typ = 'S';

create temp table ffo_base_plan_od_exclusive as 
select A.orig_sic, A.orig_shift, A.load_to_mode1, A.load_to_sic1, A.load_to_shift1, A.must_clear_sic, A.must_clear_shift, A.daylane_freight, A.load_to_mode2, A.load_to_sic2, A.load_to_shift2,case when B.ld_leg_typ is null then '' else 'HSS' end as load_to_mode3,  A.load_to_sic3, A.load_to_shift3, A.dest_sic,A.head_load,A.bypass,A.head_load_hit_ratio, A.head_load_avg_weight,A.head_load_avg_cube,A.bypass_hit_ratio, A.bypass_avg_weight,A.bypass_avg_cube, A.avg_weight, A.avg_cube from (select A.orig_sic, A.orig_shift, A.load_to_mode1, A.load_to_sic1, A.load_to_shift1, A.must_clear_sic, A.must_clear_shift, A.daylane_freight, case when B.ld_leg_typ is null then '' else 'HSS' end as load_to_mode2, A.load_to_sic2, A.load_to_shift2, A.load_to_sic3, A.load_to_shift3, A.dest_sic,A.head_load_hit_ratio, A.head_load_avg_weight,A.head_load_avg_cube,A.bypass_hit_ratio, A.bypass_avg_weight,A.bypass_avg_cube, A.head_load,A.bypass, A.avg_weight, A.avg_cube from ffo_base_plan_od as A left outer join exclusive_lanes as B
on A.load_to_sic1 = B.ld_at_node_nm and
      A.load_to_shift1 = B.ld_at_shft_cd and
      A.load_to_sic2 = B.ld_to_node_nm and
      A.load_to_shift2 = B.ld_to_shft_cd) as A
left outer join exclusive_lanes as B
on
      A.load_to_sic2 = B.ld_at_node_nm and
      A.load_to_shift2 = B.ld_at_shft_cd and
      A.load_to_sic3 = B.ld_to_node_nm and
      A.load_to_shift3 = B.ld_to_shft_cd;


create temp table ffo_base_plan_3rd_fac_combined as 
select orig_sic,
       orig_shift,
       load_to_mode1,
       load_to_sic1,
       load_to_shift1,
       must_clear_sic,
       must_clear_shift,
       daylane_freight,
       '' as load_to_mode2,
       load_to_sic2,
       load_to_shift2,
       '' as load_to_mode3,
       load_to_sic3,
       load_to_shift3,
       '' as dest_sic,
       case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load,
       case when sum(mark_bypass)/5.0 >= 0.8 then 'X' else '' end as bypass,
       sum(mark_head_load)/5.0 as head_load_hit_ratio,
       case when sum(mark_head_load) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_head_load) end as head_load_avg_weight, 
       case when sum(mark_head_load)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_head_load) end as head_load_avg_cube, 
       sum(mark_bypass)/5.0 as bypass_hit_ratio, 
       case when sum(mark_bypass) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_bypass) end as bypass_avg_weight, 
       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube,
       sum(total_weight_out)/5.0 as avg_weight,
       sum(total_cube_out)/5.0 as avg_cube
 from
      (select date,
              orig_sic,
              orig_shift,
              load_to_mode1,
              load_to_sic1,
              load_to_shift1,
              must_clear_sic, 
              must_clear_shift,
              daylane_freight,
              load_to_sic2,
              load_to_shift2,
              load_to_sic3,
              load_to_shift3,
              sum(weight_out) as total_weight_out,
              sum(cube_out)/1157.0 as total_cube_out,
              case when total_cube_out>=0.4 or total_weight_out >=22500 then 1 else 0 end as mark_head_load,
              case when total_cube_out>=0.85 or total_weight_out >=22500 then 1 else 0 end as mark_bypass   
       from ffo_extracts_otb
       where load_to_sic3 != ''
       group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13
      ) as A
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,13, 14;


create temp table ffo_base_plan_2nd_fac_combined as 
select orig_sic,
       orig_shift,
       load_to_mode1,
       load_to_sic1,
       load_to_shift1,
       must_clear_sic,
       must_clear_shift,
       daylane_freight,
       '' as load_to_mode2,
       load_to_sic2,
       load_to_shift2,
       '' as load_to_mode3,
       '' as load_to_sic3,
       '' as load_to_shift3,
       '' as dest_sic,
       case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load,
       case when sum(mark_bypass)/5.0 >= 0.8 then 'X' else '' end as bypass,
       sum(mark_head_load)/5.0 as head_load_hit_ratio,
       case when sum(mark_head_load) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_head_load) end as head_load_avg_weight, 
       case when sum(mark_head_load)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_head_load) end as head_load_avg_cube, 
       sum(mark_bypass)/5.0 as bypass_hit_ratio, 
       case when sum(mark_bypass) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_bypass) end as bypass_avg_weight, 
       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube, 
       sum(total_weight_out)/5.0 as avg_weight, 
       sum(total_cube_out)/5.0 as avg_cube 
 from
      (select date,
              orig_sic,
              orig_shift,
              load_to_mode1,
              load_to_sic1,
              load_to_shift1,
              must_clear_sic, 
              must_clear_shift,
              daylane_freight,
              load_to_sic2,
              load_to_shift2,
              sum(weight_out) as total_weight_out,
              sum(cube_out)/1157.0 as total_cube_out,
              case when total_cube_out>=0.4 or total_weight_out >=22500 then 1 else 0 end as mark_head_load,
              case when total_cube_out>=0.85 or total_weight_out >=22500 then 1 else 0 end as mark_bypass   
       from ffo_extracts_otb
       where load_to_sic2 != '' 
       group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
      ) as A
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12;



create temp table ffo_base_plan_must_clear_sic_combined as 
select orig_sic,
       orig_shift,
       load_to_mode1,
       load_to_sic1,
       load_to_shift1,
       must_clear_sic,
       must_clear_shift,
       daylane_freight,
       '' as load_to_mode2,
       '' as load_to_sic2,
       '' as load_to_shift2,
       '' as load_to_mode3,
       '' as load_to_sic3,
       '' as load_to_shift3,
       '' as dest_sic,
       case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load,
       case when sum(mark_bypass)/5.0 >= 0.8 then 'X' else '' end as bypass,
       sum(mark_head_load)/5.0 as head_load_hit_ratio,
       case when sum(mark_head_load) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_head_load) end as head_load_avg_weight, 
       case when sum(mark_head_load)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_head_load) end as head_load_avg_cube, 
       sum(mark_bypass)/5.0 as bypass_hit_ratio, 
       case when sum(mark_bypass) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_bypass) end as bypass_avg_weight, 
       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube, 
       sum(total_weight_out)/5.0 as avg_weight,
       sum(total_cube_out)/5.0 as avg_cube 
  from
      (select date,
              orig_sic,
              orig_shift,
              load_to_mode1,
              load_to_sic1,
              load_to_shift1,
              must_clear_sic, 
              must_clear_shift,
              daylane_freight,
              sum(weight_out) as total_weight_out,
              sum(cube_out)/1157.0 as total_cube_out,
              case when total_cube_out>=0.4 or total_weight_out >=22500 then 1 else 0 end as mark_head_load,
              case when total_cube_out>=0.85 or total_weight_out >=22500 then 1 else 0 end as mark_bypass   
       from ffo_extracts_otb
       where must_clear_sic != '' 
       group by 1, 2, 3, 4, 5, 6, 7, 8, 9
      ) as A
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10;

create temp table ffo_base_plan_1st_fac_combined as 
select orig_sic,
       orig_shift,
       load_to_mode1,
       load_to_sic1,
       load_to_shift1,
       '' as must_clear_sic,
       '' as must_clear_shift,
       '' as daylane_freight,
       '' as load_to_mode2,
       '' as load_to_sic2,
       '' as load_to_shift2,
       '' as load_to_mode3,
       '' as load_to_sic3,
       '' as load_to_shift3,
       '' as dest_sic,
       case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load,
       case when sum(mark_bypass)/5.0 >= 0.8 then 'X' else '' end as bypass,
       sum(mark_head_load)/5.0 as head_load_hit_ratio,
       case when sum(mark_head_load) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_head_load) end as head_load_avg_weight, 
       case when sum(mark_head_load)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_head_load) end as head_load_avg_cube, 
       sum(mark_bypass)/5.0 as bypass_hit_ratio, 
       case when sum(mark_bypass) =0 then 0 else sum(case when total_cube_out >=0.4 then total_weight_out else 0 end)/sum(mark_bypass) end as bypass_avg_weight, 
       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube, 
       sum(total_weight_out)/5.0 as avg_weight,
       sum(total_cube_out)/5.0 as avg_cube 
  from
      (select date,
              orig_sic,
              orig_shift,
              load_to_mode1,
              load_to_sic1,
              load_to_shift1,
              sum(weight_out) as total_weight_out,
              sum(cube_out)/1157.0 as total_cube_out,
              case when total_cube_out>=0.4 or total_weight_out >=22500 then 1 else 0 end as mark_head_load,
              case when total_cube_out>=0.85 or total_weight_out >=22500 then 1 else 0 end as mark_bypass   
       from ffo_extracts_otb
       where load_to_sic1 != '' 
       group by 1, 2, 3, 4, 5, 6
      ) as A
group by 1, 2, 3, 4, 5, 6, 7;
create temp table ffo_planning_tool as 
((select * from ffo_base_plan_od_exclusive 
union
--group by load_to_sic3 and load_to_shift3
select * from ffo_base_plan_3rd_fac_combined
union
--group by load_to_sic2 and load_to_shift2
select * from ffo_base_plan_2nd_fac_combined
union
--group by must_clear_sic and must_clear_shift
select * from ffo_base_plan_must_clear_sic_combined
union
select * from ffo_base_plan_1st_fac_combined)
--group by load_to_sic1 and load_to_shift1

order by orig_sic, orig_shift, load_to_mode1, load_to_sic1 desc, load_to_shift1 desc, must_clear_sic desc, must_clear_shift desc, load_to_sic2 desc, load_to_shift2 desc, load_to_sic3 desc, load_to_shift3 desc, dest_sic desc
);
 create temp table sic_doors as 
(select  dest_sic as sic from ffo_base_plan_od_exclusive where head_load ='X'
union
select load_to_sic3 as sic from ffo_base_plan_3rd_fac_combined where head_load ='X'
union
select load_to_sic2 as sic from ffo_base_plan_2nd_fac_combined where head_load ='X'
union
select  must_clear_sic as sic  from ffo_base_plan_must_clear_sic_combined where head_load ='X'
union
select load_to_sic1 as sic from ffo_base_plan_1st_fac_combined where head_load ='X'); 
select *  from ffo_planning_tool order by orig_sic, orig_shift, load_to_mode1, load_to_sic1 desc, load_to_shift1 desc, must_clear_sic desc, must_clear_shift desc, daylane_freight desc, load_to_sic2 desc, load_to_shift2 desc, load_to_sic3 desc, load_to_shift3 desc, dest_sic desc;
select distinct A.door_sic from 
sic_doors as A
left join
sic_doors as B
 on
A.door_sic = B.door_sic and
A.orig_sic = B.orig_sic and 
B.launch_date = A.launch_date -7
where A.launch_date = '2014-03-18' and A.orig_sic='NGV' and B.door_sic is null order by A.door_sic; 
select A.door_sic from (select distinct door_sic from 
sic_doors where launch_date= '2014-03-11' and orig_sic='NGV') as A
left join
(select distinct door_sic from 
sic_doors where launch_date='2014-03-18' and orig_sic = 'NGV') as B on A.door_sic = B.door_sic where B.door_sic is null;