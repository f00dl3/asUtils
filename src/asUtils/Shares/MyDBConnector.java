/*
by Anthony Stump
Created: 14 Aug 2017
*/

package asUtils.Shares;

import asUtils.Secure.DatabaseProps;
import java.sql.*;

public class MyDBConnector {

	public static Connection getMyConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		DatabaseProps DBProperties = new DatabaseProps();
		return DriverManager.getConnection(DBProperties.getDbUrl(), DBProperties.getDbUser(), DBProperties.getDbPass());
	}

	public static void main(String[] args) {
		Connection conn = null;
		try {
			conn = getMyConnection();
			System.out.println("MySQL (Core) Connected!");
		}
		catch (SQLException err) { System.out.println(err.getMessage()); }
		catch (Exception e) { System.out.println(e); }

	}
	
}
