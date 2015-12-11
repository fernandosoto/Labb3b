package com.example.fernando.labb3b;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import org.junit.Test;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Test
    public void bitTest()
    {
        String bits = Integer.toBinaryString(1);
        String bit = bits.substring(bits.length()-1);
        Integer.valueOf(bit);
        System.out.println("LOL:" + bit);
    }
}