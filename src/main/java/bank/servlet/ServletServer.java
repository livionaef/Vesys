package bank.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bank.Bank;
import bank.local.Driver;

public class ServletServer extends HttpServlet {
	
//	static Bank bank;
//	
//	static {
//		bank = new Driver.LocalBank();
//	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// ...
	}
	
	@Override
	public void init() throws ServletException {
		ServletContext context = getServletContext();
		if(context.getAttribute("bank") == null) {
			context.setAttribute("bank", new Driver.LocalBank());
		}
		bank = (Bank) context.getAttribute("bank");
	};
	
	private Bank bank;
}
