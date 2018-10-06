
create temp table ffo_extracts_otb as
select * from ffo_extracts_dayfreight
where orig_shift = 'O' and date between '2013-07-01' and '2013-07-03' 
and orig_sic='NCT' and dest_sic != 'NGZ' and dest_sic != 'NRA';



--select * from prd_whseview..tbl_flo_load_option_vw   where ld_at_shft_cd='O' and ld_at_node_nm='NGV' and fnl_dest_node_nm='LAO' order by 1, 2, 3, 4, 5, 6
--select * from prd_whseview..tbl_flo_load_leg_vw where ld_at_shft_cd='O' and ld_at_node_nm='NGV' 

--select * from prd_whseview..tbl_flo_load_option_vw 
--join prd_whseview..tbl_flo_load_leg_vw 
--on tbl_flo_load_option_vw.ld_at_node_nm = tbl_flo_load_leg_vw.ld_at_node_nm
--and tbl_flo_load_option_vw.ld_at_shft_cd = tbl_flo_load_leg_vw.ld_at_shft_cd
--and tbl_flo_load_option_vw.fnl_dest_node_nm = tbl_flo_load_leg_vw.fnl_dest_node_nm


create temp table ffo_dynamic_od as
select *  from ffo_extracts_otb where cube_out/1157.0>=0.4;


---capability

--NGV 675162.8 remember this number, nCT 586,654
select sum(weight_out)/5.0 from ffo_extracts_otb;

--create table ffo_opportunity
--(sic varchar(2),
--capacity double);


--1. remove sics that are capable of pure od load from data, then regroup by fac 3, 2, 1

--Removing pure loading opportunities
--This table has excluded all possibility of pure loading;
create temp table ffo_extracts_otb_headload_removed as
select ffo_extracts_otb.* from ffo_extracts_otb left outer join ffo_dynamic_od on 
ffo_extracts_otb.orig_sic = ffo_dynamic_od.orig_sic and
ffo_extracts_otb.dest_sic = ffo_dynamic_od.dest_sic and
ffo_extracts_otb.date = ffo_dynamic_od.date
where ffo_dynamic_od.orig_sic is null and ffo_dynamic_od.dest_sic is null;


---This table has excluded all possibility of combining at 3rd FAC and beyond;
--drop table ffo_extracts_otb_pure_3rd_fac_removed;
create temp table ffo_extracts_otb_3rd_fac_removed_dynamic as
select B.* from ffo_extracts_otb_headload_removed as B left outer join 
(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, sum(weight_out) as total_weight_out  from ffo_extracts_otb_headload_removed
where load_to_sic3 != '' and load_to_shift3 != '' 
group by 1, 2, 3, 4,5,6,7,8,9,10,11, 12, 13
having sum(cube_out/1157.0) >= 0.4 ) as A
on B.date = A.date
and B.orig_sic = A.orig_sic
and B.orig_shift = A.orig_shift
and B.load_to_mode1 = A.load_to_mode1
and B.load_to_sic1 = A.load_to_sic1
and B.load_to_shift1 = A.load_to_shift1
and B.must_clear_sic = A.must_clear_sic
and B.must_clear_shift = A.must_clear_shift
and B.daylane_freight = A.daylane_freight
and B.load_to_sic2 = A.load_to_sic2
and B.load_to_shift2 = A.load_to_shift2
and B.load_to_sic3 = A.load_to_sic3
and B.load_to_shift3 = A.load_to_shift3
where A.total_weight_out is null;


--This table has excluded all possibility of combining at 2nd FAC and beyond;
create temp table ffo_extracts_otb_2nd_fac_removed_dynamic as
select B.* from ffo_extracts_otb_3rd_fac_removed_dynamic as B left outer join 
(select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, sum(weight_out) as total_weight_out  from ffo_extracts_otb_3rd_fac_removed_dynamic
where load_to_sic2 != '' and load_to_shift2 != '' 
group by 1, 2, 3, 4,5,6,7,8,9,10, 11
having sum(cube_out/1157.0) >= 0.4 ) as A
on B.date = A.date
and B.orig_sic = A.orig_sic
and B.orig_shift = A.orig_shift
and B.load_to_mode1 = A.load_to_mode1
and B.load_to_sic1 = A.load_to_sic1
and B.load_to_shift1 = A.load_to_shift1
and B.must_clear_sic = A.must_clear_sic
and B.must_clear_shift = A.must_clear_shift
and B.daylane_freight = A.daylane_freight
and B.load_to_sic2 = A.load_to_sic2
and B.load_to_shift2 = A.load_to_shift2
where A.total_weight_out is null;


--NGV 85,398 --NCT 106,055
select sum(weight_out)/5.0 from ffo_extracts_otb_headload_removed where load_to_sic2 = '';

--NGV 267,224 -- NCT 220,831
select sum(weight_out)/5.0 from ffo_extracts_otb_headload_removed where load_to_sic2 != '' and load_to_sic3 = '' ;

--NGV 20,587 -- NCT 13,444
select sum(weight_out)/5.0 from ffo_extracts_otb_headload_removed where load_to_sic3 != '';


--freights that can be combined at 3rd FAC, NGV 0, NCT 0
select sum(total_weight_out)/5.0 from (select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, load_to_sic3, load_to_shift3, sum(weight_out) as total_weight_out  from ffo_extracts_otb_headload_removed
where load_to_sic3 != '' and load_to_shift3 != ''
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13
having sum(cube_out/1157.0) >= 0.4) as A

--freights that can be combined at 2nd FAC, NGV 193,008, NCT 101763
select sum(total_weight_out)/5.0 from (select date, orig_sic, orig_shift, load_to_mode1, load_to_sic1, load_to_shift1, must_clear_sic, must_clear_shift, daylane_freight, load_to_sic2, load_to_shift2, sum(weight_out) as total_weight_out  from ffo_extracts_otb_3rd_fac_removed_dynamic
where load_to_sic2 != '' and load_to_shift2 != ''
group by 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
having sum(cube_out/1157.0) >= 0.4) as A





--total rehandle times for NCT = 1st FAC * 1 + 2nd FAC * 2 + 3rd FAC * 3 - Freight that can be combined at 3rd FAC * 2 - freight that can be combined at 2nd FAC * 1  = 106,055*1 + 220,831* 2 + 13,444 * 3 - 148,267 = 439782/586654 = 75.0%












