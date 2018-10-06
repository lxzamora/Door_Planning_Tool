

--drop table ffo_extracts_otb;
create temp table ffo_extracts_otb as
select * from ffo_extracts
where orig_shift = 'O' and date between '2013-07-25' and '2013-08-01' 
and orig_sic='NCT';



--drop table ffo_base_plan_od
create  temp table ffo_base_plan_od as
(select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, move_to_sic1, move_to_shift1, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, dest_sic, sum(weight_in)/5.0 as avg_weight_in, sum((cube_in/1157.0)/5.0) as avg_cube_in, sum(weight_out)/5.0 as avg_weight_out, sum((cube_out/1157.0)/5.0) as avg_cube_out, count(distinct date)/5.0 as daily_building_frequency, case when sum(case when cube_out/1157.0 >=0.4 then 1 else 0 end)/5.0 >= 0.6 then 'X' else '' end as head_load, case when sum(case when cube_out/1157.0 >= 0.85 then 1 else 0 end)/5.0 >= 0.8 then 'X' else '' end as bypass   from ffo_extracts_otb 
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);



---capability

--NGV 675162.8 , this is the total weight. -- NCT 555,860
select sum(avg_weight_out) from ffo_base_plan_od;

--create table ffo_opportunity
--(sic varchar(2),
--capacity double);



--1. remove sics that are capable of pure od load from data, then regroup by fac 3, 2, 1

--Removing pure loading opportunities
--This table has excluded all possibility of pure loading;
create temp table ffo_extracts_otb_od_head_load_removed as
select ffo_extracts_otb.* from ffo_extracts_otb left outer join ffo_base_plan_od on 
ffo_extracts_otb.orig_sic = ffo_base_plan_od.orig_sic and
ffo_extracts_otb.dest_sic = ffo_base_plan_od.dest_sic
where head_load != 'X';

--NGV 26960 -- NCT 13,920
select sum(weight_out)/5.0 from ffo_extracts_otb_od_head_load_removed where load_to_sic3 != '';

--NGV 393583 -- NCT 325,216
select sum(weight_out)/5.0 from ffo_extracts_otb_od_head_load_removed where load_to_sic2 != '' and load_to_sic3 = '';

--NGV 126166 -- NCT 169,442
select sum(weight_out)/5.0 from ffo_extracts_otb_od_head_load_removed where load_to_sic1 != '' and load_to_sic2 = '';


---This table has excluded all possibility of combining at 3rd FAC and beyond;
--drop table ffo_extracts_otb_pure_3rd_fac_removed;
--drop table ffo_extracts_otb_od_3rd_fac_removed;
create temp table ffo_extracts_otb_3rd_fac_removed as
select ffo_extracts_otb_od_head_load_removed.* from ffo_extracts_otb_od_head_load_removed left outer join 
(select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, move_to_sic1, move_to_shift1, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, '' as dest_sic, case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load, '' as bypass, count(distinct date)/5.0 as daily_building_frequency, sum(total_weight_in)/5.0 as avg_weight_in, sum(total_cube_in/5.0) as avg_cube_in, sum(total_weight_out)/5.0 as avg_weight_out, sum(total_cube_out/5.0) as avg_cube_out  from
(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, move_to_sic1, move_to_shift1, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, sum(weight_in) as total_weight_in, sum(cube_in/1157) as total_cube_in, sum(weight_out) as total_weight_out, sum(cube_out)/1157.0 as total_cube_out, case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load   from ffo_extracts_otb_od_head_load_removed
where load_to_sic3 != '' and load_to_shift3 != '' 
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12) as A
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
having head_load ='X') as A
on ffo_extracts_otb_od_head_load_removed.orig_sic = A.orig_sic
and ffo_extracts_otb_od_head_load_removed.orig_shift = A.orig_shift
and ffo_extracts_otb_od_head_load_removed.load_to_mode1 = A.load_to_mode1
and ffo_extracts_otb_od_head_load_removed.load_to_sic1 = A.load_to_sic1
and ffo_extracts_otb_od_head_load_removed.load_to_shift1 = A.load_to_shift1
and ffo_extracts_otb_od_head_load_removed.move_to_sic1 = A.move_to_sic1
and ffo_extracts_otb_od_head_load_removed.move_to_shift1 = A.move_to_shift1
and ffo_extracts_otb_od_head_load_removed.load_to_sic2 = A.load_to_sic2
and ffo_extracts_otb_od_head_load_removed.load_to_shift2 = A.load_to_shift2
and ffo_extracts_otb_od_head_load_removed.load_to_sic3 = A.load_to_sic3
and ffo_extracts_otb_od_head_load_removed.load_to_shift3 = A.load_to_shift3
where A.avg_weight_in is null;


--drop table ffo_extracts_otb_2nd_fac_removed;
--This table has excluded all possibility of combining at 2nd FAC and beyond;
create temp table ffo_extracts_otb_2nd_fac_removed as
select ffo_extracts_otb_3rd_fac_removed.* from ffo_extracts_otb_3rd_fac_removed left outer join 
(select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, move_to_sic1, move_to_shift1, load_to_sic2, load_to_shift2, '' as load_to_sic3, '' as load_to_shift3, '' as dest_sic, case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load, '' as bypass, count(distinct date)/5.0 as daily_building_frequency, sum(total_weight_in)/5.0 as avg_weight_in, sum(total_cube_in/5.0) as avg_cube_in, sum(total_weight_out)/5.0 as avg_weight_out, sum(total_cube_out/5.0) as avg_cube_out  from
(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, move_to_sic1, move_to_shift1, load_to_sic2, load_to_shift2, sum(weight_in) as total_weight_in, sum(cube_in/1157) as total_cube_in, sum(weight_out) as total_weight_out, sum(cube_out)/1157.0 as total_cube_out, case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load   from ffo_extracts_otb_3rd_fac_removed
where load_to_sic2 != '' and load_to_shift2 != '' 
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10) as A
group by 1, 2, 3, 4, 5, 6, 7, 8, 9
having head_load='X') as A
on ffo_extracts_otb_3rd_fac_removed.orig_sic = A.orig_sic
and ffo_extracts_otb_3rd_fac_removed.orig_shift = A.orig_shift
and ffo_extracts_otb_3rd_fac_removed.load_to_mode1 = A.load_to_mode1
and ffo_extracts_otb_3rd_fac_removed.load_to_sic1 = A.load_to_sic1
and ffo_extracts_otb_3rd_fac_removed.load_to_shift1 = A.load_to_shift1
and ffo_extracts_otb_3rd_fac_removed.move_to_sic1 = A.move_to_sic1
and ffo_extracts_otb_3rd_fac_removed.move_to_shift1 = A.move_to_shift1
and ffo_extracts_otb_3rd_fac_removed.load_to_sic2 = A.load_to_sic2
and ffo_extracts_otb_3rd_fac_removed.load_to_shift2 = A.load_to_shift2
where A.avg_weight_in is null;

--freights that can be combined at 3rd FAC, NGV 15,220, NCT 0
select sum(avg_weight_out) from (select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, move_to_sic1, move_to_shift1, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, '' as dest_sic, case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load, '' as bypass, count(distinct date)/5.0 as daily_building_frequency, sum(total_weight_in)/5.0 as avg_weight_in, sum(total_cube_in/5.0) as avg_cube_in, sum(total_weight_out)/5.0 as avg_weight_out, sum(total_cube_out/5.0) as avg_cube_out  from
(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, move_to_sic1, move_to_shift1, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, sum(weight_in) as total_weight_in, sum(cube_in/1157) as total_cube_in, sum(weight_out) as total_weight_out, sum(cube_out)/1157.0 as total_cube_out, case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load   from ffo_extracts_otb_od_head_load_removed
where load_to_sic3 != '' and load_to_shift3 != '' 
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12) as A
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
having head_load ='X') as A

--freights that can be combined at 2nd FAC, NGV 323635, NCT 235765
select sum(avg_weight_out) from (select orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, move_to_sic1, move_to_shift1, load_to_sic2, load_to_shift2, '' as load_to_sic3, '' as load_to_shift3, '' as dest_sic, case when sum(mark_head_load)/5.0 >= 0.6 then 'X' else '' end as head_load, '' as bypass, count(distinct date)/5.0 as daily_building_frequency, sum(total_weight_in)/5.0 as avg_weight_in, sum(total_cube_in/5.0) as avg_cube_in, sum(total_weight_out)/5.0 as avg_weight_out, sum(total_cube_out/5.0) as avg_cube_out  from
(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, move_to_sic1, move_to_shift1, load_to_sic2, load_to_shift2, sum(weight_in) as total_weight_in, sum(cube_in/1157) as total_cube_in, sum(weight_out) as total_weight_out, sum(cube_out)/1157.0 as total_cube_out, case when total_cube_out>=0.4 then 1 else 0 end as mark_head_load   from ffo_extracts_otb_3rd_fac_removed
where load_to_sic2 != '' and load_to_shift2 != '' 
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10) as A
group by 1, 2, 3, 4, 5, 6, 7, 8, 9
having head_load='X') as A


--total rehandle times for NGV = 1st FAC * 1 + 2nd FAC * 2 + 3rd FAC * 3 - Freight that can be combined at 3rd FAC * 3 - freight that can be combined at 2nd FAC * 2 - freight that can be combined at 1st FAC * 1 = 126166*1 + 393583* 2 + 26960 * 3 - 323635 * 1 - 15,220 * 2 = 640137/675162 = 94.8%

--total rehandle times for NCT = 1st FAC * 1 + 2nd FAC * 2 + 3rd FAC * 3 - Freight that can be combined at 3rd FAC * 3 - freight that can be combined at 2nd FAC * 2 - freight that can be combined at 1st FAC * 1 = 169442*1 + 325,216* 2 + 13920 * 3 - 235765 * 1 = 625869/555,860 = 112.6%








