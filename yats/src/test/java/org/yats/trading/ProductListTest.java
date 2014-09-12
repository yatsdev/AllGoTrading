package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProductListTest {

    public static final String PRODUCT_LIST_PATH = "config/CFDProductList.csv";

    @Test
    public void canReadProductList()
    {
        assert (list.size()>0);
        assert (list.isEveryUnderlyingAvailableAsProduct());
        assert (list.isEveryUnitAvailableAsProduct());
    }


    @Test
    public void canFindSAPBySymbol()
    {
        Product productSAP = list.findBySymbol("TEST_SAP");
        assert (productSAP.getSymbol().compareTo("TEST_SAP")==0);
    }

    @Test
    public void canFindSAPByProductId()
    {
        Product productSAP = list.getProductWith("4663789");
        assert (productSAP.getSymbol().compareTo("SAP")==0);
    }

    @Test
    public void canFilterOnUnderlying()
    {
        IProvideProduct productsWithUnderlyingSGD = list.getProductsWithUnderlying("CCY_SGD");
        assert (productsWithUnderlyingSGD.values().size()>0);
        assert (productsWithUnderlyingSGD.values().size()< list.size() / 5);
    }

    @Test
    public void canFilterOnUnit()
    {
        IProvideProduct productsWithUnderlyingSGD = list.getProductsWithUnit("CCY_SGD");
        assert (productsWithUnderlyingSGD.values().size()>0);
        assert (productsWithUnderlyingSGD.values().size()< list.size() / 5);
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
