package com.simon.etlProcessor.domain;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class ProductDaoTest {

    ProductDao dao = new ProductDao();

    @Test
    void testGetProductsLiquidity() {
        assertNotEquals(dao.getProductsLiquidity("c1"), null);
    }

}
