package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProductListTest {

    @Test
    public void canReadProductList()
    {
        list.read("config/CFDProductList.csv");
        assert (list.size()>0);
    }


    @BeforeMethod
    public void setUp() {
        list = new ProductList();
    }

    ProductList list;

    } // class
