create table exclusive_lanes as
select LD_AT_NODE_NM, LD_AT_SHFT_CD, LD_TO_NODE_NM, LD_TO_SHFT_CD, LD_LEG_TYP from prd_whseview..TBL_FLO_LOAD_LEG_VW where ld_leg_typ = 0 and move_mode_typ = 'S';

create table ffo_extracts_dayfreight
(date date,
orig_sic varchar(3),
orig_shift varchar(1),
load_to_mode1 varchar(6),
load_to_sic1 varchar(3),
load_to_shift1 varchar(1),
must_clear_sic varchar(3),
must_clear_shift varchar(1),
daylane_freight varchar(1),
move_to_sic1 varchar(3),
move_to_shift1 varchar(1),
load_to_sic2 varchar(3),
load_to_shift2 varchar(1),
load_to_sic3 varchar(3),
load_to_shift3 varchar(1),
dest_sic varchar(3),
weight_in double,
cube_in double,
weight_out double,
cube_out double
);

create table ffo_extracts_weekly_load_dayfreight
(date date,
orig_sic varchar(3),
orig_shift varchar(1),
load_to_mode1 varchar(6),
load_to_sic1 varchar(3),
load_to_shift1 varchar(1),
must_clear_sic varchar(3),
must_clear_shift varchar(1),
daylane_freight varchar(1),
move_to_sic1 varchar(3),
move_to_shift1 varchar(1),
load_to_sic2 varchar(3),
load_to_shift2 varchar(1),
load_to_sic3 varchar(3),
load_to_shift3 varchar(1),
dest_sic varchar(3),
weight_in double,
cube_in double,
weight_out double,
cube_out double
);


insert into ffo_extracts_dayfreight
(select * from ffo_extracts_weekly_load_dayfreight where date >='2013-08-30' and date <='2013-09-05');


update ffo_extracts_dayfreight set load_to_mode1 = '' where load_to_mode1 != 'Exc';
update ffo_extracts_dayfreight set load_to_mode1 = 'HSS' where load_to_mode1 = 'Exc';
update ffo_extracts_dayfreight set load_to_sic2='' where load_to_sic2 is null;
update ffo_extracts_dayfreight set load_to_sic3='' where load_to_sic3 is null;
update ffo_extracts_dayfreight set load_to_shift2='' where load_to_shift2 is null;
update ffo_extracts_dayfreight set load_to_shift3='' where load_to_shift3 is null;
update ffo_extracts_dayfreight set move_to_sic1 ='' where move_to_sic1 is null;
update ffo_extracts_dayfreight set move_to_shift1 ='' where move_to_shift1 is null;
update ffo_extracts_dayfreight set must_clear_sic ='' where must_clear_sic is null;
update ffo_extracts_dayfreight set must_clear_shift ='' where must_clear_shift is null;
update ffo_extracts_dayfreight set must_clear_sic = '' where daylane_freight = 'Y';
update ffo_extracts_dayfreight set must_clear_shift = '' where daylane_freight = 'Y';





--drop table ffo_extracts_otb;

--select distinct date from ffo_extracts_otb ;
create temp table ffo_extracts_otb as
(select * from ffo_extracts_dayfreight
where orig_shift = 'O' and date between '2013-07-01' and '2013-07-03' 
and orig_sic='NCT' and dest_sic!= 'NGZ') distribute on (orig_sic, dest_sic);

update ffo_extracts_otb set load_to_sic1= load_to_sic1||' D' where daylane_freight='Y';

select * from ffo_extracts_otb;


--select * from prd_whseview..tbl_flo_load_option_vw   where ld_at_shft_cd='O' and ld_at_node_nm='NGV' and fnl_dest_node_nm='LAO' order by 1, 2, 3, 4, 5, 6
--select * from prd_whseview..tbl_flo_load_leg_vw where ld_at_shft_cd='O' and ld_at_node_nm='NGV' 

--select * from prd_whseview..tbl_flo_load_option_vw 
--join prd_whseview..tbl_flo_load_leg_vw 
--on tbl_flo_load_option_vw.ld_at_node_nm = tbl_flo_load_leg_vw.ld_at_node_nm
--and tbl_flo_load_option_vw.ld_at_shft_cd = tbl_flo_load_leg_vw.ld_at_shft_cd
--and tbl_flo_load_option_vw.fnl_dest_node_nm = tbl_flo_load_leg_vw.fnl_dest_node_nm




--drop table ffo_base_plan_od
create  temp table ffo_base_plan_od as
(select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight,load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, dest_sic, sum(case when cube_out/1157.0 >=0.4 then 1 else 0 end) as head_load_hit_days, head_load_hit_days/5.0 as head_load_hit_ratio, case when head_load_hit_days =0 then 0 else sum(case when cube_out/1157.0 >=0.4 then weight_out else 0 end)/head_load_hit_days end as head_load_avg_weight, case when head_load_hit_days=0 then 0 else sum(case when cube_out/1157.0 >=0.4 then cube_out/1157.0 else 0 end)/head_load_hit_days end as head_load_avg_cube, sum(case when cube_out/1157.0 >=0.85 then 1 else 0 end) as bypass_hit_days,  bypass_hit_days/5.0 as bypass_hit_ratio, case when bypass_hit_days = 0 then 0 else sum(case when cube_out/1157.0 >=0.85 then weight_out else 0 end)/bypass_hit_days end as bypass_avg_weight, case when bypass_hit_days = 0 then 0 else sum(case when cube_out/1157.0 >=0.85 then cube_out/1157.0 else 0 end)/bypass_hit_days end as bypass_avg_cube, case when head_load_hit_ratio >= 0.6 then 'X' else '' end as head_load, case when bypass_hit_ratio >= 0.8 then 'X' else '' end as bypass   from ffo_extracts_otb 
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13) distribute on (orig_sic, dest_sic);





create temp table ffo_base_plan_od_exclusive as 
select A.orig_sic, A.orig_shift, A.load_to_mode1, A.load_to_sic1, A.load_to_shift1, A.must_clear_sic, A.must_clear_shift, A.daylane_freight, A.load_to_mode2, A.load_to_sic2, A.load_to_shift2,case when B.ld_leg_typ is null then '' else 'HSS' end as load_to_mode3,  A.load_to_sic3, A.load_to_shift3, A.dest_sic,A.head_load,A.bypass,A.head_load_hit_ratio, A.head_load_avg_weight,A.head_load_avg_cube,A.bypass_hit_ratio, A.bypass_avg_weight,A.bypass_avg_cube from (select A.orig_sic, A.orig_shift, A.load_to_mode1, A.load_to_sic1, A.load_to_shift1, A.must_clear_sic, A.must_clear_shift, A.daylane_freight, case when B.ld_leg_typ is null then '' else 'HSS' end as load_to_mode2, A.load_to_sic2, A.load_to_shift2, A.load_to_sic3, A.load_to_shift3, A.dest_sic,A.head_load_hit_ratio, A.head_load_avg_weight,A.head_load_avg_cube,A.bypass_hit_ratio, A.bypass_avg_weight,A.bypass_avg_cube, A.head_load,A.bypass from ffo_base_plan_od as A left outer join exclusive_lanes as B
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

select * from ffo_base_plan_od_exclusive;

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
       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube
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
              case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load,
              case when total_cube_out>=0.85 then 1 else 0 end as mark_bypass   
       from ffo_extracts_otb
       where load_to_sic3 != '' and load_to_shift3 != '' 
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
       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube
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
              case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load,
              case when total_cube_out>=0.85 then 1 else 0 end as mark_bypass   
       from ffo_extracts_otb
       where load_to_sic2 != '' and load_to_shift2 != '' 
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
       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube
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
              case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load,
              case when total_cube_out>=0.85 then 1 else 0 end as mark_bypass   
       from ffo_extracts_otb
       where must_clear_sic != '' and must_clear_shift != '' 
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
       case when sum(mark_bypass)= 0 then 0 else sum(case when total_cube_out >=0.4 then total_cube_out else 0 end)/sum(mark_bypass) end as bypass_avg_cube
 from
      (select date,
              orig_sic,
              orig_shift,
              load_to_mode1,
              load_to_sic1,
              load_to_shift1,
              sum(weight_out) as total_weight_out,
              sum(cube_out)/1157.0 as total_cube_out,
              case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load,
              case when total_cube_out>=0.85 then 1 else 0 end as mark_bypass   
       from ffo_extracts_otb
       where load_to_sic1 != '' and load_to_shift1 != '' 
       group by 1, 2, 3, 4, 5, 6
      ) as A
group by 1, 2, 3, 4, 5, 6, 7, 8;


drop table ffo_planning_tool;
create temp table ffo_planning_tool as
(
(select * from ffo_base_plan_od_exclusive
union
--group by load_to_sic3 and load_to_shift3
select * from ffo_base_plan_3rd_fac_combined
union
--group by load_to_sic2 and load_to_shift2
select * from ffo_base_plan_2nd_fac_combined
union
--group by move_to_sic1 and move_to_shift1
select * from ffo_base_plan_must_clear_sic_combined
union
select * from ffo_base_plan_1st_fac_combined)
--group by load_to_sic1 and load_to_shift1

order by orig_sic, orig_shift, load_to_mode1, load_to_sic1 desc, load_to_shift1 desc, must_clear_sic desc, must_clear_shift desc, daylane_freight desc, load_to_sic2 desc, load_to_shift2 desc, load_to_sic3 desc, load_to_shift3 desc, dest_sic desc
);

select * from ffo_planning_tool;

select dest_sic from ffo_base_plan_od_exclusive where head_load ='X'
union
select load_to_sic3 from ffo_base_plan_3rd_fac_combined where head_load ='X'
union
select load_to_sic2 from ffo_base_plan_2nd_fac_combined where head_load ='X'
union
select must_clear_sic from ffo_base_plan_must_clear_sic_combined where head_load ='X'
union
select load_to_sic1 from ffo_base_plan_1st_fac_combined where head_load ='X'

drop table sic_doors;

create table sic_doors
(
   launch_date Date,
   door_sic varchar(3),
   orig_sic varchar(3),
   constraint pk_sic_doors primary key(LAUNCH_DATE, door_sic, orig_sic) 
);

delete from sic_doors;
select * from sic_doors;

--added door
select A.door_sic from 
(select door_sic from sic_doors where launch_date = '2013-08-05' and orig_sic='NGV') as A
left join
 (select door_sic from sic_doors where launch_date = '2013-07-29' and orig_sic='NGV') as B
 on
A.door_sic = B.door_sic
where B.door_sic is null; 


insert into sic_doors (select '2013-08-05',  dest_sic, orig_sic from ffo_base_plan_od_exclusive where head_load ='X'
union
select '2013-08-05', load_to_sic3, orig_sic from ffo_base_plan_3rd_fac_combined where head_load ='X'
union
select '2013-08-05', load_to_sic2, orig_sic  from ffo_base_plan_2nd_fac_combined where head_load ='X'
union
select '2013-08-05', must_clear_sic, orig_sic  from ffo_base_plan_must_clear_sic_combined where head_load ='X'
union
select '2013-08-05', load_to_sic1, orig_sic  from ffo_base_plan_1st_fac_combined where head_load ='X');

drop table dayfrt_new_workbook;
create temp table dayfrt_new_workbook as
(select distinct orig_sic, dest_sic, must_clear_sic from ffo_extracts_weekly_load_dayfreight where daylane_freight='Y' and orig_sic in('NGV', 'NCT'));

drop table dayfrt_old_workbook;
create temp table dayfrt_old_workbook as
(select distinct current_sic, dest_sic, power_sic from prd_whseview..sim_ldp_prog_lineup_vw where day_frit_flag = 'D' and creation_tmst>='2013-08-29' and current_sic in ('NGV', 'NCT'));

select A.current_sic, A.dest_sic, A.power_sic, B.must_clear_sic
 from dayfrt_old_workbook as A
 left join dayfrt_new_workbook as B 
 on A.current_sic=B.orig_sic and 
    A.dest_sic=B.dest_sic
 where B.orig_sic is null and A.dest_sic in
(select distinct dest_sic from dayfrt_new_workbook) ;
