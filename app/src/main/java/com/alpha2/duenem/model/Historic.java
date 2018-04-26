package com.alpha2.duenem.model;

import java.util.Date;

/**
 * Created by aluno on 26/04/18.
 */

public class Historic {
    private Date date;
    private int percent_of_rights = 0;
    private int time = 0;

    Historic(Date date, int percent_of_rights, int time){
        this.date = date;
        this.percent_of_rights = percent_of_rights;
        this.time = time;
    }

    public int getPercent_of_rights() {
        return percent_of_rights;
    }

    public void setPercent_of_rights(int number_of_questions_right) {
        this.percent_of_rights = number_of_questions_right;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int  time) {
        this.time = time;
    }
}
