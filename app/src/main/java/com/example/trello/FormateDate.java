package com.example.trello;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FormateDate {
    private static String[] months = {"січ.", "лют.", "бер.", "кві.", "трав.", "черв.", "лип.", "серп.", "вер.", "жовт.", "лист.", "груд."};

    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String formattedDate = dateFormat.format(date);

        String year = formattedDate.substring(formattedDate.lastIndexOf('.') + 1);

        int month = Integer.parseInt(formattedDate.substring(formattedDate.indexOf('.') + 1, formattedDate.lastIndexOf('.')));
        String monthString = months[month - 1];

        String day = formattedDate.substring(0, formattedDate.indexOf('.'));

        return day + " " + monthString + " " + year + "р";
    }

    public static String simpleFormatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(date);
    }

    public static Date parseDate(String date) {
        String[] words = date.split(" ");
        int day = Integer.parseInt(words[0]);
        int month = 0;
        int year = Integer.parseInt(words[2].split("р")[0]);

        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(words[1])) {
                month = i;
                break;
            }
        }

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        return calendar.getTime();
    }
}
