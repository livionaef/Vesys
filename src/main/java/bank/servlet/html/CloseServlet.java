package bank.servlet.html;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/close")
public class CloseServlet extends AbstractBankServlet {

	private static final long serialVersionUID = -1187029657498000895L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write(getHeader("Close Account"));
		writer.write("<form action=\"close\" method=\"post\"/><br/>");
		writer.write("Number:<br/><input type=\"number\" name=\"number\"/><br/>");
		writer.write("<input type=\"submit\" "+"name=\"submit\" value=\"Close\"/>");
		writer.write("</form>");
		writer.write(getFooter());
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String number = request.getParameter("number");
		String error = null;
		if (number == null || number.trim().contentEquals("")) {
			error = "Number not set!";
		} else {
			response.sendRedirect("");
		}
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write(getHeader("Error"));
		writer.write(error + "<br/><a href=\"close\">Back</a>");
		writer.write(getFooter());
	}
}
