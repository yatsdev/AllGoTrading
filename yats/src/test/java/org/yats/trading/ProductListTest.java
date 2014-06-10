package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProductListTest {

    public static final String PRODUCT_LIST_PATH = "config/CFDProductList.csv";

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

// appended route to all products with this:
//    @Test
//    public void canWriteProductList()
//    {
//        list.writeWithAppend("config/CFDProductListOut.csv", ",RouteAGT");
//    }



    @BeforeMethod
    public void setUp() {
        list = new ProductList();
        list.read(PRODUCT_LIST_PATH);
    }

    ProductList list;

    } // class
