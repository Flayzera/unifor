package com.project.studyroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class RescheduleRequestDTO {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date newStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date newEnd;

    public RescheduleRequestDTO() {
    }

    public Date getNewStart() {
        return newStart;
    }

    public void setNewStart(Date newStart) {
        this.newStart = newStart;
    }

    public Date getNewEnd() {
        return newEnd;
    }

    public void setNewEnd(Date newEnd) {
        this.newEnd = newEnd;
    }
}
