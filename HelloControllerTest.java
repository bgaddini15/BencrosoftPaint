package com.example.bencrosoftpaint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelloControllerTest {

    @Test
    void textShouldBePotato(){
        var controller = new HelloController();
        controller.paintText = "Potato";
        assertEquals("Potato", controller.getPaintText());
    }

    @Test
    void textShouldBeMemory(){
        var controller = new HelloController();
        controller.paintText = "Memory";
        assertEquals("Memory", controller.getPaintText());
    }

    @Test
    void numSidesShouldBeSeven(){
        var controller = new HelloController();
        controller.numSides = 7;
        assertEquals(7, controller.getNumSides());
    }

    @Test
    void numSidesShouldBeEleven(){
        var controller = new HelloController();
        controller.numSides = 11;
        assertEquals(11, controller.getNumSides());
    }
}