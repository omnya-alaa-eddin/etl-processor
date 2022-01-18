package com.simon.etlProcessor.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.simon.etlProcessor.config.DatabaseConnection;
import com.simon.etlProcessor.model.ProductLiquidity;

public class ProductDao {

    static Connection con = DatabaseConnection.getConnection();

    final static String GET_PRODUCT_QUERY = "select * from PRODUCT_LIQUIDITY where tenant_id=?";

    public Map<String, ProductLiquidity> getProductsLiquidity(String tenant) {

        try {
            log("---trying to get products" + tenant);
            PreparedStatement ps = null;
            ps = con.prepareStatement(GET_PRODUCT_QUERY);
            ps.setString(1, tenant);
            ResultSet rs = ps.executeQuery();

            Map<String, ProductLiquidity> productsLiquidity = new HashMap<>();

            while (rs.next()) {
                int id = rs.getInt("p_id");
                int order = rs.getInt("p_order");
                String client = rs.getString("tenant_id");
                String name = rs.getString("p_name");
                ProductLiquidity pl = new ProductLiquidity();
                pl.setProductID(id);
                pl.setProductName(name);
                pl.setProductOrder(order);
                pl.setTenant_id(client);
                productsLiquidity.put(pl.getProductName(), pl);
            }
            return productsLiquidity;

        } catch (

        Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // Simple log utility
    private static void log(String string) {
        System.out.println(string);

    }
}
