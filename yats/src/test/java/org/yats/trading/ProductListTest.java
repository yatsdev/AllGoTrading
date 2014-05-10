package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProductListTest {

    @Test
    public void canReadProductList()
    {
        assert (list.size()>0);
    }


    @Test
    public void canFindSAPBySymbol()
    {
        Product productSAP = list.findBySymbol("SAP");
        assert (productSAP.getSymbol().compareTo("SAP")==0);
    }

    @Test
    public void canFindSAPByProductId()
    {
        Product productSAP = list.getProductForProductId("4663789");
        assert (productSAP.getSymbol().compareTo("SAP")==0);
    }


    @BeforeMethod
    public void setUp() {
        list = new ProductList();
        list.read("config/CFDProductList.csv");
    }

    ProductList list;

    } // class
