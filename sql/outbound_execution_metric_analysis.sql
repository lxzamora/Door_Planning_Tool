drop table schedule_with_close;

create temp table schedule_with_close as
select	a19.*, a17.TRLR_LD_DEST_SIC_CD
from	LNH_TRAILER_SHIPMENT_VW	a16
	join	LNH_TRAILER_CLOSE_VW	a17
	  on 	(a16.TRLR_CLS_DT_PST = a17.TRLR_CLS_DT_PST and 
	a16.TRLR_NBR = a17.TRLR_NBR and 
	a16.TRLR_PFX = a17.TRLR_PFX and 
	a16.TRLR_SFX = a17.TRLR_SFX)
	join	LNH_SCHEDULE_TRAILER_VW	a18
	  on 	(a16.TRLR_CLS_DT_PST = a18.TRLR_CLS_DT_PST and 
	a16.TRLR_NBR = a18.TRLR_NBR and 
	a16.TRLR_PFX = a18.TRLR_PFX and 
	a16.TRLR_SFX = a18.TRLR_SFX)
	join	LNH_EXECUTED_SCHEDULE_VW	a19
	  on 	(a18.LNH_SCH_CRTE_TMST_PDX = a19.LNH_SCH_CRTE_TMST_PDX and 
	a18.LNH_SCH_NBR = a19.LNH_SCH_NBR)
where DATE(a19.ACTL_DISP_DT_LCL) between '2013-11-4' and '2013-11-7' 
and a19.ACTL_DISP_SHFT_CD = 'OTB';

select count(*) from schedule_with_close;

drop table schedule_with_close_same_as_load_leg_dest;

create temp table schedule_with_close_same_as_load_leg_dest as
select schedule_with_close.*,tbl_flo_load_leg_vw.ld_to_node_nm, tbl_flo_load_leg_vw.ld_to_shft_cd 
from schedule_with_close
join tbl_flo_load_leg_vw
on schedule_with_close.SCH_ORIG_SIC_CD = tbl_flo_load_leg_vw.LD_AT_NODE_NM
and tbl_flo_load_leg_vw.LD_AT_SHFT_CD = 'O' 
and schedule_with_close.TRLR_LD_DEST_SIC_CD = tbl_flo_load_leg_vw.LD_TO_NODE_NM


select * from tbl_flo_load_leg_vw
where ld_at_node_nm='NGV' and ld_at_shft_cd='O';

select count(*) from schedule_with_close_same_as_load_leg_dest;

select schedule_with_close.* 
from schedule_with_close 
left join
schedule_with_close_same_as_load_leg_dest
on schedule_with_close.LNH_SCH_CRTE_TMST_PDX = schedule_with_close_same_as_load_leg_dest.LNH_SCH_CRTE_TMST_PDX
and schedule_with_close.LNH_SCH_NBR = schedule_with_close_same_as_load_leg_dest.LNH_SCH_NBR
where schedule_with_close_same_as_load_leg_dest.LNH_SCH_CRTE_TMST_PDX is null;

select * 
from schedule_with_close
where TRLR_LD_DEST_SIC_CD != SCH_DEST_SIC_CD and TRLR_LD_DEST_SIC_CD != SCH_FNL_DEST_SIC_CD
and DATE(ACTL_DISP_DT_LCL) = '2013-11-4' ;

select * from flo_flow_plan_summary_vw where cldr_dt='2013-11-04';

select * from schedule_with_close_same_as_load_leg_dest where SCH_ORIG_SIC_CD = 'NGV' and SCH_DEST_SIC_CD='LDA';

select	
    a13.LOC_SIC_SLT  schedule_dest,
	a14.TRLR_NBR  TRLR_NBR,
	a15.LNH_SCH_NBR  LNH_SCH_NBR,
	a14.TRLR_PFX  EQP_PFX,
	a14.TRLR_SFX  EQP_SFX,
	a15.LNH_SCH_CRTE_TMST_PDX  LNH_SCH_CRTE_TMST_PDX,
	a12.CLDR_DT  CALENDAR_DATE,
	a16.ACTL_DISP_SHFT_CD  SHIFT_CD,
	CASE WHEN a16.SCH_SVC_TYP = 'SUBSTITUTE' THEN 'Y' ELSE 'N' END  SCH_SVC_TYP,
	a15.LNH_SCH_NBR  LNH_SCH_NBR0,
	a15.LNH_SCH_CRTE_TMST_PDX  LNH_SCH_CRTE_TMST_PDX0,
	max(a16.LNH_SCH_NBR)  SCH_NBR,
	a16.SCH_DEST_SIC_CD ,
	a16.SCH_ORIG_SIC_CD,
	sum(a11.PUP_VOL_PCT)  total_vol
from	SHIPMENT_VW	a11
	cross join	CALENDAR_DAY_VW	a12
	cross join	SIC_MASTER_REF_VW	a13
    cross join    SIC_MASTER_REF_VW	a18
    
	join	LNH_TRAILER_SHIPMENT_VW	a14
	  on 	(a11.SHPMT_INSTC_ID = a14.SHPMT_INSTC_ID)
	join	LNH_SCHEDULE_TRAILER_VW	a15
	  on 	(a14.TRLR_CLS_DT_PST = a15.TRLR_CLS_DT_PST and 
	a14.TRLR_NBR = a15.TRLR_NBR and 
	a14.TRLR_PFX = a15.TRLR_PFX and 
	a14.TRLR_SFX = a15.TRLR_SFX)
	join	LNH_EXECUTED_SCHEDULE_VW	a16
	  on 	(a15.LNH_SCH_CRTE_TMST_PDX = a16.LNH_SCH_CRTE_TMST_PDX and 
	a15.LNH_SCH_NBR = a16.LNH_SCH_NBR)
    join	LNH_TRAILER_CLOSE_VW	a17
	  on 	(a15.TRLR_CLS_DT_PST = a17.TRLR_CLS_DT_PST and 
	a15.TRLR_NBR = a17.TRLR_NBR and 
	a15.TRLR_PFX = a17.TRLR_PFX and 
	a15.TRLR_SFX = a17.TRLR_SFX)
where	(--a16.SCH_DEST_SIC_CD = 'NGV'
 --and a12.CLDR_DT >= '28-oct-2013' and a12.cldr_dt<='14-nov-2013'
 --and a12.CLDR_DT ='11-nov-2013' 
-- and a16.ACTL_ARIV_SHFT_CD = 'INB'
 and a16.SCH_DEST_SIC_CD = a13.LOC_SIC_SLT
and a11.destination_SIC_id = a18.sic_id
and a18.sic_id = 'NNA'
and a11.DELIVERY_DATE = '12-nov-2013'
 and a11.BILL_CLAS_CD in ('A', 'C', 'D', 'E')
 and a12.CLDR_DT = DATE(a16.ACTL_ARIV_DT_LCL))

 --and a18.loc_sic_slt = 'NNA'
group by	a13.LOC_SIC_SLT,
	a14.TRLR_NBR,
	a15.LNH_SCH_NBR,
	a14.TRLR_PFX,
	a14.TRLR_SFX,
	a15.LNH_SCH_CRTE_TMST_PDX,
	a12.CLDR_DT,
	a16.ACTL_DISP_SHFT_CD,
	CASE WHEN a16.SCH_SVC_TYP = 'SUBSTITUTE' THEN 'Y' ELSE 'N' END,
	a15.LNH_SCH_NBR,
	a15.LNH_SCH_CRTE_TMST_PDX,
	a16.SCH_DEST_SIC_CD,
	a16.SCH_ORIG_SIC_CD 


select	a13.LOC_SIC_SLT  LOC_SIC_SLT,
	a12.CLDR_DT  CALENDAR_DATE,
	a16.ACTL_DISP_SHFT_CD  SHIFT_CD,
	sum(a11.PUP_VOL_PCT)  total_vol
from	SHIPMENT_VW	a11
	cross join	CALENDAR_DAY_VW	a12
	cross join	SIC_MASTER_REF_VW	a13
    cross join    SIC_MASTER_REF_VW	a18  
	join	LNH_TRAILER_SHIPMENT_VW	a14
	  on 	(a11.SHPMT_INSTC_ID = a14.SHPMT_INSTC_ID)
	join	LNH_SCHEDULE_TRAILER_VW	a15
	  on 	(a14.TRLR_CLS_DT_PST = a15.TRLR_CLS_DT_PST and 
	a14.TRLR_NBR = a15.TRLR_NBR and 
	a14.TRLR_PFX = a15.TRLR_PFX and 
	a14.TRLR_SFX = a15.TRLR_SFX)
	join	LNH_EXECUTED_SCHEDULE_VW	a16
	  on 	(a15.LNH_SCH_CRTE_TMST_PDX = a16.LNH_SCH_CRTE_TMST_PDX and 
	a15.LNH_SCH_NBR = a16.LNH_SCH_NBR)
    join	LNH_TRAILER_CLOSE_VW	a17
	  on 	(a15.TRLR_CLS_DT_PST = a17.TRLR_CLS_DT_PST and 
	a15.TRLR_NBR = a17.TRLR_NBR and 
	a15.TRLR_PFX = a17.TRLR_PFX and 
	a15.TRLR_SFX = a17.TRLR_SFX)
where	(a16.SCH_ORIG_SIC_CD = 'NGV'
 and a12.CLDR_DT >= '28-oct-2013' and a12.cldr_dt<='14-nov-2013'
 and a16.ACTL_DISP_SHFT_CD = 'OTB'
 and a16.SCH_ORIG_SIC_CD = a13.LOC_SIC_SLT
and a11.destination_SIC_id = a18.sic_id
 and a11.BILL_CLAS_CD in ('A', 'C', 'D', 'E')
 and a12.CLDR_DT = DATE(a16.ACTL_DISP_DT_LCL))
 and a18.loc_sic_slt = 'NAT'
group by	a13.LOC_SIC_SLT,
	a12.CLDR_DT,
	a16.ACTL_DISP_SHFT_CD,
	a16.SCH_ORIG_SIC_CD 

