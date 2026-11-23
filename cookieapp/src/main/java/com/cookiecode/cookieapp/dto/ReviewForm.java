package com.cookiecode.cookieapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewForm {
    @Min(1) @Max(5) private int rating = 5;
    @Size(max = 1000) private String comment;
}