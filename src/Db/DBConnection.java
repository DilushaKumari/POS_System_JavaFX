package Db;

import java.sql.*;

public class DBConnection {

    private static DBConnection dbConnection;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos?createDatabaseIfNotExist=true&allowMultiQueries=true", "root", "1234");

            PreparedStatement preparedStatement = connection.prepareStatement("SHOW TABLES");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                String sql = "CREATE TABLE  IF NOT EXISTS customer(customer_id      varchar(4)  not null primary key," +
                        "\tcustomer_name    varchar(50) not null,\n" +
                        "\tcustomer_address varchar(50) \n" +
                        ");\n" +
                        "\n" +
                        "CREATE TABLE  IF NOT EXISTS item\n" +
                        "(\n" +
                        "\titem_id    varchar(4)  not null primary key,\n" +
                        "\titem_des   varchar(50) not null,\n" +
                        "\titem_qty   double      null,\n" +
                        "\titem_price double      null\n" +
                        ");\n" +
                        "\n" +
                        "CREATE TABLE IF NOT EXISTS customer_order\n" +
                        "(\n" +
                        "\torder_id    varchar(5)  not null,\n" +
                        "\tcustomer_id varchar(4)  not null,\n" +
                        "\torder_date  varchar(12) null,\n" +
                        "\tCONSTRAINT PRIMARY KEY(order_id,customer_id),\n" +
                        "\tCONSTRAINT FOREIGN KEY(customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                        ");\n" +
                        "\n" +
                        "CREATE TABLE IF NOT EXISTS order_detail\n" +
                        "(\n" +
                        "  order_id   varchar(5)     not null,\n" +
                        "  item_id    varchar(4)     not null,\n" +
                        "  item_qty   decimal(10, 2) null,\n" +
                        "  item_price decimal(10, 2) null,\n" +
                        "  CONSTRAINT PRIMARY KEY(order_id, item_id),\n" +
                        "  CONSTRAINT FOREIGN KEY(order_id) REFERENCES customer_order(order_id)  ON DELETE CASCADE ON UPDATE CASCADE,\n" +
                        "  CONSTRAINT FOREIGN KEY(item_id) references item(item_id)  ON DELETE CASCADE ON UPDATE CASCADE\n" +
                        ");\n" +
                        "\n" +
                        "\n" +
                        "CREATE TABLE user\n" +
                        "(\n" +
                        "\tuser_id varchar(10),\n" +
                        "\tpassword varchar(50),\n" +
                        "\tDate date\n" +
                        " );\n" +
                        "\n" +
                        " INSERT INTO user VALUES (\"admin\",\"1234\",\"2019-09-20\");";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static DBConnection getInstance() {
        if (dbConnection == null)
            dbConnection = new DBConnection();

        return dbConnection;

//        return dbConnection=((dbConnection==null)?new DBConnection():dbConnection);
    }

    public Connection getConnection() {
        return connection;
    }
}
