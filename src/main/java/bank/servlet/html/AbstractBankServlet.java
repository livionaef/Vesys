package bank.servlet.html;

import java.io.IOException;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import bank.Account;
import bank.Bank;
import bank.local.Driver;

public abstract class AbstractBankServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	protected Bank bank;
	private ServletContext context;

	@Override
	public void init() throws ServletException {
		context = getServletContext();
		if (context.getAttribute("bank") == null) {
			context.setAttribute("bank", new Driver.LocalBank());
		}
		bank = (Bank) context.getAttribute("bank");
	}

	protected final String getHeader(String title) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Bank</title></head>\n");
		sb.append("<body><h1>" + title + "</h1>");
		return sb.toString();
	}

	protected final String getFooter() {
		StringBuilder sb = new StringBuilder();
		sb.append("<hr/>\n");
		sb.append("<a href=\"" + context.getContextPath() + "/\">Home</a>");
		sb.append("</body></html>");
		return sb.toString();
	}

	protected final String getAccounts() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("<hr/>\n");
		sb.append("<h2>Accounts:</h2>");
		sb.append("<table> <tr> <td><b>AccountNr</b></td> <td><b>Owner</b></td> <td><b>Balance</b></td> <td></td> </tr>");
		Set<String> accnumbers = bank.getAccountNumbers();
		for (String accnumber : accnumbers) {
			Account account = bank.getAccount(accnumber);
			sb.append("<tr><td>" + accnumber + "</td>");
			sb.append("<td>" + account.getOwner() + "</td>");
			sb.append(String.format("<td>%.2f</td>", account.getBalance()));
			sb.append("<td><a href=\"withdraw?number=" + accnumber + "\">Withdraw Money</a>&nbsp;");
			sb.append("<a href=\"deposit?number=" + accnumber + "\">Deposit Money</a>&nbsp;");
			if (accnumbers.size() > 1) {
				sb.append("<a href=\"transfer?number=" + accnumber + "\">Transfer Money</a>&nbsp;");
			}
			sb.append("<a href=\"close?number=" + accnumber + "\">Close Account</a></td>");
		}
		sb.append("</table>");
		return sb.toString();
	}
}
