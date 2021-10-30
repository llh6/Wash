package com.example.wash.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    private String driver = "com.mysql.cj.jdbc.Driver";// MySql驱动

    private  String url = "jdbc:mysql://localhost:3306/washing_machine?serverTimezone=GMT%2B8&useSSL=false&useUnicode=true" ;
            //"&allowPublicKeyRetrieval=true ";
    //?useUnicode=true&characterEncodeing=UTF-8&useSSL=false&serverTimezone=GMT";
    private  String user = "root";// 用户名
    private  String password = "123456";// 密码
    private Connection connection;
    private Statement statement;
    public DBUtil(){
        try{
            //注册驱动
            Class.forName(driver);
            System.out.println("加载驱动成功");
             connection=DriverManager.getConnection(url,user,password);
             System.out.println("数据库已链接成功!!!!");
        }catch (ClassNotFoundException e){
            System.out.println("加载驱动失败??");
           e.printStackTrace();
        }catch (SQLException e){
            System.out.println("数据库链接失败????");
            e.printStackTrace();
        }
    }
    //增删改
    public  int update(String sql){
        try{

            statement=connection.createStatement();
            return statement.executeUpdate(sql);
        }catch (SQLException e){
            e.printStackTrace();
        }
        return -1;
    }
    //查询方法
    public ResultSet query(String sql){
        try{
            //PreparedStatement preparedStatement=connection.prepareStatement(sql);
            if (connection==null){
                System.out.println("connection为null");
            }
            statement=connection.createStatement();
            //return preparedStatement.executeQuery();
            return statement.executeQuery(sql);
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }
//    public static void main(String[] args) {
//        DBUtil db=new DBUtil();
//        String sql="SELECT * FROM washing";
//        ResultSet resultSet=db.query(sql);
//        try {
//            while(resultSet.next()){
//                System.out.println(resultSet.getString("number"));
//                System.out.println(resultSet.getString("status"));
//                //System.out.println(resultSet.getString(3));
//            }
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }

}
