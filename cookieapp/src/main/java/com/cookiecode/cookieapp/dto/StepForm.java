package com.cookiecode.cookieapp.dto;

public class StepForm {

    private int stepOrder;
    private String title;
    private String instruction;
    private int durationMinutes;
    private String tip;
    private boolean parallelizable;

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public boolean isParallelizable() {
        return parallelizable;
    }

    public void setParallelizable(boolean parallelizable) {
        this.parallelizable = parallelizable;
    }
}