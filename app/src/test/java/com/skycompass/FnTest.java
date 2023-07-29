package com.skycompass;

import org.junit.Test;

import static org.junit.Assert.*;

import com.skycompass.util.Fn;

public class FnTest {

    @Test
    public void ModularTest() {

        assertEquals(Fn.modular(0,7),0);
        assertEquals(Fn.modular(1,7),1);
        assertEquals(Fn.modular(7,7),0);
        assertEquals(Fn.modular(8,7),1);
        assertEquals(Fn.modular(14,7),0);
        assertEquals(Fn.modular(15,7),1);

        assertEquals(Fn.modular(-0,7),0);
        assertEquals(Fn.modular(-1,7),6);
        assertEquals(Fn.modular(-7,7),0);
        assertEquals(Fn.modular(-8,7),6);
        assertEquals(Fn.modular(-14,7),0);
        assertEquals(Fn.modular(-15,7),6);

        assertEquals(Fn.modular(0f,7f),0,0f);
        assertEquals(Fn.modular(1f,7f),1,0f);
        assertEquals(Fn.modular(7f,7f),0,0f);
        assertEquals(Fn.modular(8f,7f),1,0f);
        assertEquals(Fn.modular(14f,7f),0,0f);
        assertEquals(Fn.modular(15f,7f),1,0f);

        assertEquals(Fn.modular(-0f,7f),0f,0f);
        assertEquals(Fn.modular(-1f,7f),6f,0f);
        assertEquals(Fn.modular(-7f,7f),0f,0f);
        assertEquals(Fn.modular(-8f,7f),6f,0f);
        assertEquals(Fn.modular(-14f,7f),0f,0f);
        assertEquals(Fn.modular(-15f,7f),6f,0f);

    }

    @Test
    public void ClampTest() {

        assertEquals(Fn.clamp(0,-10,10),0);
        assertEquals(Fn.clamp(1,-10,10),1);
        assertEquals(Fn.clamp(10,-10,10),10);
        assertEquals(Fn.clamp(-10,-10,10),-10);
        assertEquals(Fn.clamp(100,-10,10),10);
        assertEquals(Fn.clamp(-100,-10,10),-10);

        assertEquals(Fn.clamp(0f,-10f,10f),0f,0f);
        assertEquals(Fn.clamp(1f,-10f,10f),1f,0f);
        assertEquals(Fn.clamp(10f,-10f,10f),10f,0f);
        assertEquals(Fn.clamp(-10f,-10f,10f),-10f,0f);
        assertEquals(Fn.clamp(100f,-10f,10f),10f,0f);
        assertEquals(Fn.clamp(-100f,-10f,10f),-10f,0f);

    }

}
