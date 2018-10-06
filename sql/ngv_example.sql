create temp table ngv_10_10 as
select * from ffo_extracts_dayfreight where orig_sic='NGV' and date >='2013-10-04' and date <='2013-10-10' orig_shift='O';

