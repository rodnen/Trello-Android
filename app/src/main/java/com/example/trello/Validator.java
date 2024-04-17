package com.example.trello;

import androidx.fragment.app.FragmentActivity;

import java.util.Date;
import java.util.Vector;

public class Validator {

    public static void validateName(String name) throws Exception {
        if(name.length() <= 0) throw new Exception("Назва не може бути пустою");
        if(name.length() > 20) throw new Exception("Назва не може бути довшою за 20 символів");
    }

    public static void validateDeadline(Date date) throws Exception {
        Date currentDate = new Date();
        if(date == null) throw new Exception("Оберіть дату");
        if (date.before(currentDate)) throw new Exception("Дата не може бути меншою за поточну");
    }

    public static void validateDescription(String description) throws Exception {
        if(description.length() <= 0) throw new Exception("Опис не може бути пустим");
        if(description.length() > 2000) throw new Exception("Опис не може бути довшим за 2000 символів");
    }

    public static void validateTaskCount(List list) throws Exception {
        if(list.getTasks().size() == 10){
            throw new Exception("Список не можу містити більше 10 задач");
        }
    }
}
