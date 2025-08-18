package hotel;


import java.sql.*;

public class dbcn {

	private static final String URL="jdbc:sqlserver://DESKTOP-JL615BG:1433;databaseName=hotel;integratedSecurity=true;encrypt=false";
	
	public static Connection getConnection() throws SQLException{
		 return DriverManager.getConnection(URL);
	}
	
	
}

