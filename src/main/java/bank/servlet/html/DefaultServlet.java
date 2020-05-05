package bank.servlet.html;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/")
public class DefaultServlet extends AbstractBankServlet {

	private static final long serialVersionUID = 3016239647779625179L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write(getHeader("Bank"));
		writer.write("<a href=\"create\">Create Account</a><br/>");
		writer.write(getAccounts());
		writer.write(getFooter());
	}
}
