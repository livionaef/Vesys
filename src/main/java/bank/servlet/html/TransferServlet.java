package bank.servlet.html;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import bank.Bank;

@WebServlet("/transfer")
public class TransferServlet extends AbstractBankServlet {

	private static final long serialVersionUID = -1187029657498000895L;

	public TransferServlet(Bank bank) {
		this.bank = bank;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		writer.write(getHeader("Transfer Money from Account a to Account b"));
		writer.write("<form action=\"transfer\" method=\"post\"/><br/>");
		writer.write("Account a:<br/><input type=\"text\" name=\"from\"/><br/>");
		writer.write("Account b:<br/><input type=\"text\" name=\"to\"/><br/>");
		writer.write("Amount:<br/><input type=\"number\" name=\"amount\"/><br/>");
		writer.write("<input type=\"submit\" " + "name=\"submit\" value=\"Transfer\"/>");
		writer.write("</form>");
		writer.write(getFooter());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String amount = request.getParameter("amount");
		String from = request.getParameter("from");
		String to = request.getParameter("to");
		String error = "";
		if (from == null || from.trim().contentEquals("")) {
			error += " Account a not set!";
			if (to == null || to.trim().contentEquals("")) {
				error += " Account b not set!";
				if (amount == null || amount.trim().contentEquals("")) {
					error += " Amount not set!";
				} else {
					response.sendRedirect("");
				}
				PrintWriter writer = response.getWriter();
				response.setContentType("text/html");
				writer.write(getHeader("Error"));
				writer.write(error + "<br/><a href=\"transfer\">Back</a>");
				writer.write(getFooter());
			}
		}
	}
}