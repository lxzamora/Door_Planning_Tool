select * from sic_doors_tmp where LAUNCH_DATE='2014-12-22';

insert into sic_doors_tmp
select '2014-12-29' as launch_date, DOOR_SIC, ORIG_SIC, shift from 
 sic_doors_tmp where LAUNCH_DATE='2014-12-22';