package bank.servlet.html;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bank.Bank;

@WebServlet("/create")
public class CreateServlet extends AbstractBankServlet {

	private static final long serialVersionUID = -1187029657498000895L;

	public CreateServlet(Bank bank) {
		this.bank = bank;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write(getHeader("Create Account"));
		writer.write("<form action=\"create\" method=\"post\"/><br/>");
		writer.write("Owner:<br/><input type=\"text\" name=\"owner\"/><br/>");
		writer.write("<input type=\"submit\" " + "name=\"submit\" value=\"Create\"/>");
		writer.write("</form>");
		writer.write(getFooter());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String owner = request.getParameter("owner");
		String error = null;
		if (owner == null || owner.trim().contentEquals("")) {
			error = "Owner not set!";
		} else {
			response.sendRedirect("");
		}
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write(getHeader("Error"));
		writer.write(error + "<br/><a href=\"create\">Back</a>");
		writer.write(getFooter());
	}
}
