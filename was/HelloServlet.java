import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HelloServlet extends HttpServlet {


	@Override
	protected void doGet(HttpServletRequest request,
			     HttpServletResponse response)
		throws IOException {

	    response.setContentType("text/html;charset=UTF-8");

	    PrintWriter out = response.getWriter();
	    out.println("<h1>Hello from Tomcat!</h1>");
	}
}
