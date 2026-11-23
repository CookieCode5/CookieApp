package com.cookiecode.cookieapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.cookiecode.cookieapp.model.enums.Unit;

public class UnitTest {
    @Test
    void toBaseConvertsCorrectly() {
        assertEquals(1500, Unit.KG.toBase(1.5), 0.001);
        assertEquals(30, Unit.CL.toBase(3), 0.001);
        assertEquals(15, Unit.TABLESPOON.toBase(1), 0.001);
    }
}
