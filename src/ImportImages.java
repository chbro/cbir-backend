import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;


public class ImportImages {
	private static Connection getConn() {
	    String driver = "com.mysql.jdbc.Driver";
	    String url = "jdbc:mysql://localhost:3306/test";
	    String username = "root";
	    String password = "";
	    Connection conn = null;
	    try {
	        Class.forName(driver); //classLoader,加载对应驱动
	        conn = (Connection) DriverManager.getConnection(url, username, password);
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return conn;
	}
	//获取最大距离，以便归一化
	public static double getMaxSim(String table, double f[]) {
	    Connection conn = getConn();
	    PreparedStatement pstmt; 
	    String sql = "select * from "+table;
	    double similarity = 0, maxsim = 0;
	    try {
	        pstmt = (PreparedStatement)conn.prepareStatement(sql);
	        ResultSet rs = pstmt.executeQuery();
	        String value;
	        while(rs.next()) {
	            int len = table.equalsIgnoreCase("color")?5:10;
	            for(int i=3;i<=len;i++) {
	           	 	value = rs.getString(i);
	           	 	similarity += Math.pow(Double.parseDouble(value)-f[i-3], 2);
	            }
	            similarity = Math.sqrt(similarity);
	            if(similarity > maxsim) maxsim = similarity;
	       }
	       conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return maxsim;
	}
	//获取总记录条数
	public static int getTotal(String table) {
	    Connection conn = getConn();
	    PreparedStatement pstmt; 
	    String sql = "select * from " + table;
	    int total = 0;
	    try {
	        pstmt = (PreparedStatement)conn.prepareStatement(sql);
	        ResultSet rs = pstmt.executeQuery();
	        while(rs.next()) {
	            total++;
	       }
	       conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return total;
	}
	public static void updateSim(double sim, String table, int id) {
	    Connection conn = getConn();
	    PreparedStatement pstmt; 
	    String sql = "update " + table + " set similarity = " + sim + " where id = " + id;
	    try {
	        pstmt = (PreparedStatement)conn.prepareStatement(sql);
	        pstmt.execute();
	        conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	public static void updateTableSims(String table, double f[]) {
	    Connection conn = getConn();
	    String sql = "select * from "+table;
	    PreparedStatement pstmt;
	    try {
	        pstmt = (PreparedStatement)conn.prepareStatement(sql);
	        ResultSet rs = pstmt.executeQuery(); 
	        double similarity, maxsim = getMaxSim(table, f);
	        String value; 
	         while (rs.next()) { 
	        	 similarity = 0;
	        	 int len = table.equalsIgnoreCase("color")?5:10;
	        	 int id = rs.getInt(1);
	        	 for(int i=3;i<=len;i++) {
	        		 value = rs.getString(i);
	        		 similarity += Math.pow(Double.parseDouble(value)-f[i-3], 2);
	        	 }
	        	 similarity = 1-Math.sqrt(similarity)/maxsim;
	        	 updateSim(similarity, table, id);
	         }
	         conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	//按相似度排序获取图片
	public static String getImg(String table, double f[], int page, boolean retry) {
	    Connection conn = getConn();
	    final int pagesize = 8;
	    String sql = "select * from "+table+" order by similarity desc limit "+(page-1)*pagesize+","+pagesize;
	    PreparedStatement pstmt;
 
	    JsonObject res = new JsonObject();
	    JsonArray array = new JsonArray();
	    try {
	    	if(retry)
	    		updateTableSims(table, f);
	        pstmt = (PreparedStatement)conn.prepareStatement(sql);
	        ResultSet rs = pstmt.executeQuery(); 
	        ResultSetMetaData metaData = rs.getMetaData();
	        int columnCount = metaData.getColumnCount();
	        String columnName, value; 
	         while (rs.next()) { 
	        	 JsonObject jsonObj = new JsonObject();
	             for (int i = 1; i <= columnCount; i++) {  
	                 columnName =metaData.getColumnLabel(i);
	                 //只需要取三列的值返回给前端
	                 if(columnName.equalsIgnoreCase("name") || columnName.equalsIgnoreCase("similarity")
	                		 || columnName.equalsIgnoreCase("path")) {
	                	 value = rs.getString(columnName);
	                	 jsonObj.addProperty(columnName, value);
	                 }
	             }
	             array.add(jsonObj);
	         }
	         res.addProperty("total", getTotal(table));
	         conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    res.add("images", array);
	    return res.toString();
	}
	//图片入库
	public static int insert(String name, double features[], String path, boolean insert_shape) {
	    Connection conn = getConn();
	    int i = 0;
	    String sql;
	    if(features.length == 3)
	    	sql = "insert into color (name,m1,m2,m3,path) values(?,?,?,?,?)";
	    else if(insert_shape)
	    	sql = "insert into shape (name,shape0,shape1,shape2,shape3,shape4,shape5,shape6,shape7,path) values(?,?,?,?,?,?,?,?,?,?)";
	    else
	    	sql = "insert into texture (name,exp1,exp2,exp3,exp4,stadv1,stadv2,stadv3,stadv4,path) values(?,?,?,?,?,?,?,?,?,?)";
	    PreparedStatement pstmt;
	    try {
	        pstmt = (PreparedStatement) conn.prepareStatement(sql);
	        pstmt.setString(1, name);
	        int j = 0;
	        for(;j < features.length; j++) {
	        	pstmt.setDouble(j+2, features[j]);
	        }
	        pstmt.setString(j+2, path);
	        i = pstmt.executeUpdate();
	        pstmt.close();
	        conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return i;
	}
	public static void main(String[] args) throws IOException {
//		double f[] = {0.31434453,0.2251212456,0.1121251325,0.12,0.22,0.32,0.12,0.1111};
//		getImg("texture", f, 1);
//		double s = getMaxSim("color", f);
//		System.out.print(s);
	}
}
