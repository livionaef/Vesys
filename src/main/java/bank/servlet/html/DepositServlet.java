package bank.servlet.html;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import bank.Bank;

@WebServlet("/deposit")
public class DepositServlet extends AbstractBankServlet {

	private static final long serialVersionUID = -1187029657498000895L;

	public DepositServlet(Bank bank) {
		this.bank = bank;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write(getHeader("Deposit Money for Account"));
		writer.write("<form action=\"deposit\" method=\"post\"/><br/>");
		writer.write("Amount:<br/><input type=\"number\" name=\"amount\"/><br/>");
		writer.write("<input type=\"submit\" " + "name=\"submit\" value=\"Deposit\"/>");
		writer.write("</form>");
		writer.write(getFooter());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String amount = request.getParameter("amount");
		String error = null;
		if (amount == null || amount.trim().contentEquals("")) {
			error = "Amount not set!";
		} else {
			response.sendRedirect("");
		}
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write(getHeader("Error"));
		writer.write(error + "<br/><a href=\"deposit\">Back</a>");
		writer.write(getFooter());
	}
}
