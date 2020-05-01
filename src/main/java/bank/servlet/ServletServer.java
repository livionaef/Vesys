package bank.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bank.Bank;
import bank.command.Command;
import bank.local.Driver;

public class ServletServer extends HttpServlet {

	// ???
//	static Bank bank;
//	
//	static {
//		bank = new Driver.LocalBank();
//	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// ...
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("appliccation/octet-stream");
		ObjectInputStream in = new ObjectInputStream(request.getInputStream());
		
		Command command;
		try {
			command = (Command) in.readObject();
			command = command.execute(bank); // needs to be surrounded by try-catch
		} catch (Exception e) {
			throw new InternalError(e.getMessage());
		}
		
		ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
		// chunked for responses > 8K
		// content-length for responses < 8K
		out.writeObject(command);
		out.close();
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

	private ServletConfig config;

	public void init(ServletConfig config) throws ServletException {
		this.config = config;
		this.init();
	}

	// ???
//	public void init() throws ServletException {
//		// NOOP by default
//	}
}
