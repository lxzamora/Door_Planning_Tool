package com.xpo.doorplanningtool; /**
 * Created with IntelliJ IDEA.
 * User: zhang.mingming
 * Date: 7/22/13
 * Time: 10:41 PM
 * To change this template use File | Settings | File Templates.
 */
import com.xpo.doorplanningtool.vo.Plan;
import com.xpo.doorplanningtool.vo.Threshold;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class workbooks {

    //add appender to any Logger (here is root)

    public static void main(String[] args) {

        int length = args.length;
        Calendar current_date = new GregorianCalendar();
        Calendar ending_date = new GregorianCalendar();
        Calendar beginning_date = new GregorianCalendar();
        Calendar instruction_date = new GregorianCalendar();
        Calendar prev_instruction_date = new GregorianCalendar();
        String ending_date_str, beginning_date_str, instruction_date_str, prev_instruction_date_str;
        String max_cube_out, max_weight_out, bypass_threshold, headload_threshold;


        boolean ending_date_supplied = false;
        boolean report_opportunity = false;
        boolean planning_instructions = false;
        boolean sending_email = false;
        boolean fac_shift = false;
        boolean holiday = false;
        boolean is_exception_date = false;

        RehandleOpportunity rehandleOpportunity = new RehandleOpportunity();
        //PlanningInstructions planningInstructions = new PlanningInstructions();
        PlanningInstructionsTest planningInstructionsTest = new PlanningInstructionsTest();

        Properties prop = new Properties();
        Threshold threshold = new Threshold();

        InputStream input = null;

        try {

            input = new FileInputStream("\\\\cgoprfp003\\Public\\Freight\\FreightFlowPlans\\PLANNING WORKBOOKS\\Door Planning Tool\\config.properties");
            // load a properties file
            prop.load(input);
            // get the property value
            System.out.println("max_cube_out is "+prop.getProperty("max_cube_out"));
            System.out.println("max_weight_out is "+prop.getProperty("max_weight_out"));
            System.out.println("bypass_threshold is "+prop.getProperty("bypass_threshold"));
            System.out.println("headload_threshold is "+prop.getProperty("headload_threshold"));
            threshold.setMax_cube_out(prop.getProperty("max_cube_out"));
            threshold.setMax_weight_out(prop.getProperty("max_weight_out"));
            threshold.setBypass_threshold(prop.getProperty("bypass_threshold"));
            threshold.setHeadload_threshold(prop.getProperty("headload_threshold"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for ( int i = 0; i < length; i ++ )
        {

            if ( args[i].substring(0,2).equals("-d") )
            {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                try
                {
                    Date model_run_date = df.parse(args[i].substring(2));
                    System.out.print(model_run_date);
                    ending_date.setTime(model_run_date);
                    ending_date.get(Calendar.DATE);
                    ending_date.get(Calendar.DAY_OF_WEEK);
                    ending_date_supplied = true;
                    beginning_date.setTime(model_run_date);
                    beginning_date.add(Calendar.DAY_OF_YEAR, -6) ;
                    instruction_date.setTime(model_run_date);
                    instruction_date.add(Calendar.DAY_OF_YEAR, +4);
                    prev_instruction_date.setTime(model_run_date);
                    prev_instruction_date.add(Calendar.DAY_OF_YEAR, -3);

                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }

            }

            if (args[i].equals("-h"))
            {
                holiday = true;
            }

            if ( args[i].equals("-i") )
            {
                planning_instructions = true;

                if ( !ending_date_supplied )
                {

                    // // by default using [today - 9 days, today - 3 day] as beginning date and ending date
                    //check how many rev days in this period, if < 5, then beginning_date = beginning_date.add(Calendar.DAY_OF_YEAR, -16)
                    if( !holiday )
                        beginning_date.add(Calendar.DAY_OF_YEAR, -9) ;
                    else
                        beginning_date.add(Calendar.DAY_OF_YEAR, -16) ;

                    //check how many rev days in this period, if < 5, then beginning_date = beginning_date.add(Calendar.DAY_OF_YEAR, -10)
                    if( !holiday )
                       ending_date.add(Calendar.DAY_OF_YEAR, -3) ;
                    else
                        ending_date.add(Calendar.DAY_OF_YEAR, -10);

                    instruction_date.add(Calendar.DAY_OF_YEAR, +1);    //Monday next week
                    prev_instruction_date.add(Calendar.DAY_OF_YEAR, -6);


                }
                else
                {
                    if( holiday )
                        beginning_date.add(Calendar.DAY_OF_YEAR, -7) ;


                    //check how many rev days in this period, if < 5, then beginning_date = beginning_date.add(Calendar.DAY_OF_YEAR, -10)
                    if( holiday )
                        ending_date.add(Calendar.DAY_OF_YEAR, -7) ;

                }
            }

            if ( args[i].equals("-m"))
            {
                sending_email = true;
            }

            if ( args[i].equals("-r") )
            {
                if ( planning_instructions )
                {
                    System.out.print("Instructions and Rehandle opportunity can't be generated with the same command! \n");
                    return;
                }
                report_opportunity = true;
                if ( !ending_date_supplied )
                {
                    //by default using last week's Mon - Friday as beginning date and ending date

                    int dayOfWeek = current_date.get(Calendar.DAY_OF_WEEK);

                    System.out.println(dayOfWeek);

                    switch (dayOfWeek)
                    {
                        case 1:
                            beginning_date.add(Calendar.DAY_OF_YEAR, -6);
                            ending_date.add(Calendar.DAY_OF_YEAR, -2);
                            break;
                        case 2:
                            beginning_date.add(Calendar.DAY_OF_YEAR, -7);
                            ending_date.add(Calendar.DAY_OF_YEAR, -3);
                            break;
                        case 3:
                            beginning_date.add(Calendar.DAY_OF_YEAR, -8);
                            ending_date.add(Calendar.DAY_OF_YEAR, -4);
                            break;
                        case 4:
                            beginning_date.add(Calendar.DAY_OF_YEAR, -9);
                            ending_date.add(Calendar.DAY_OF_YEAR, -5);
                            break;
                        case 5:
                            beginning_date.add(Calendar.DAY_OF_YEAR, -10);
                            ending_date.add(Calendar.DAY_OF_YEAR, -6);
                            break;
                        case 6:
                            beginning_date.add(Calendar.DAY_OF_YEAR, -11);
                            ending_date.add(Calendar.DAY_OF_YEAR, -7);
                            break;
                        case 7:
                            beginning_date.add(Calendar.DAY_OF_YEAR, -12);
                            ending_date.add(Calendar.DAY_OF_YEAR, -8);
                            break;

                    }
                }

            }

        }
            //read in a file of sic list
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
        beginning_date_str = "20" + sdf.format(beginning_date.getTime());
        ending_date_str = "20" + sdf.format(ending_date.getTime());
        instruction_date_str =  "20" + sdf.format(instruction_date.getTime());
        prev_instruction_date_str =  "20" + sdf.format(prev_instruction_date.getTime());


        System.out.println("beginning date is " + beginning_date_str +"\n");
        System.out.println("ending date is " + ending_date_str + "\n");
        System.out.println("Instruction date is " + instruction_date_str + "\n");

        ArrayList<String> sicArray = new ArrayList<String>();
        ArrayList<String> exceptionDateArray = new ArrayList<String>();

          try {
              //BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\zhang.mingming\\Documents\\Projects\\FFO doorplanning tool POI\\data\\sic.txt"));
              String sic_file;

              sic_file =   "C:\\Projects\\data\\sic.txt";

              BufferedReader br = new BufferedReader(new FileReader(sic_file));
              String line = br.readLine();

              while (line != null) {
                sicArray.add(line);

                line = br.readLine();
              }

              br.close();

              String exception_date_file;

              exception_date_file =   "C:\\Projects\\data\\exceptions.txt";

              BufferedReader br_exception = new BufferedReader(new FileReader(exception_date_file));
              line = br_exception.readLine();

              while (line != null) {
                  exceptionDateArray.add(line);

                  line = br_exception.readLine();
              }

              br_exception.close();


           }
           catch (IOException ioe)
           {
              ioe.printStackTrace();
           }

           for (String exception_date : exceptionDateArray )
           {
               //if any of the exception_date matches the instruction date, set is_exception_date to true, then break
               if(exception_date.equals(instruction_date_str))
               {
                   is_exception_date = true;
                   System.out.print(exception_date + " exception date! \n");
                   break;
               }

           }
           for (String sic : sicArray)
           {
               if(sic.isEmpty())
                   break;
               //generate rehandle opportunities
               String delims = "\\s+";
               String[] tokens = sic.split(delims);
               sic = tokens[0];
               String fac_indication;
               if ( tokens.length > 1 )
                   fac_shift = true;

               if (report_opportunity)
                   rehandleOpportunity.calculateOpportunity(sic, beginning_date_str, ending_date_str);
               else if (planning_instructions)
                   planningInstructionsTest.generateInstructions(new Plan(sic, beginning_date_str, ending_date_str, instruction_date_str, prev_instruction_date_str, sending_email, fac_shift, is_exception_date), threshold);

           }

    }
}

