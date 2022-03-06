package com.example.mynewsapp.util;

import java.util.Calendar;

public class GetDateString {
    // output string like 20220303
    public static String outputCurrentDateString() {
        Calendar c1 = Calendar.getInstance();
        String strMonth;
        String strYear;
        String strDate;
        int year = c1.get(Calendar.YEAR);
        int month = c1.get(Calendar.MONTH) + 1;
        int date = c1.get(Calendar.DATE);

        strYear = String.valueOf(year);
        strMonth = String.valueOf(month);
        strDate = String.valueOf(date);
        if (month < 10) {
            strMonth = "0" + String.valueOf(month);
        }

        //System.out.println("DATE" + strYear + strMonth + strDate);


        return strYear + strMonth + strDate;
    }

    ;

    public static String outputLastMonthDateString() {
        String strMonth;
        String strYear;
        String strDate;
        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.MONTH, -1); //set to prev month

        int year = c1.get(Calendar.YEAR);
        int month = c1.get(Calendar.MONTH) + 1;
        int date = c1.get(Calendar.DATE);

        strYear = String.valueOf(year);
        strMonth = String.valueOf(month);
        strDate = String.valueOf(date);

        if (month < 10) {
            strMonth = "0" + String.valueOf(month);
        }
        //System.out.println("DATE last month " + strYear + strMonth + strDate);


        return strYear + strMonth + strDate;
    }

    ;

}
