import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.util.Properties;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class UsersServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			     HttpServletResponse response)
		throws IOException {

	    response.setContentType("text/html;charset=UTF-8");
	    PrintWriter out = response.getWriter();
	    String url = "jdbc:mariadb://192.168.111.130:3306/projectdb";
	    String user = "appuser";
	    Properties props = new Properties();
	    FileInputStream fis = new FileInputStream("/etc/dbapp/db.properties");
	    props.load(fis);
	    fis.close();
	    String password = props.getProperty("db.password");

	    try {
	    Class.forName("org.mariadb.jdbc.Driver");

	    Connection conn = DriverManager.getConnection(url, user, password);
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM users");

	    while(rs.next()) {
		out.println("<h1>" + rs.getString("name") + "</h1>");
		}
		rs.close();
		stmt.close();
		conn.close();
		} catch (Exception e) {
			out.println("<h1>DB Error</h1>");
			e.printStackTrace();
	}
}
}
