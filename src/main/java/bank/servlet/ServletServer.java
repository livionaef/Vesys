package bank.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import bank.Bank;
import bank.command.Command;
import bank.local.Driver;

public class ServletServer extends HttpServlet {

	private static final long serialVersionUID = -3139256775365877245L;
	private Bank bank;	
	private ServletConfig config;

	public ServletServer(Bank bank) {
		this.bank = bank;
	}
	
	public void init(ServletConfig config) throws ServletException {
		this.config = config;
		this.init();
	}

	@Override
	public void init() throws ServletException {
		ServletContext context = getServletContext();
		if (context.getAttribute("bank") == null) {
			context.setAttribute("bank", new Driver.LocalBank());
		}
		bank = (Bank) context.getAttribute("bank");
	};

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		out.write("Use the POST method to access this Servlet!".getBytes());
		out.flush();
		out.close();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("appliccation/octet-stream");
		try (ObjectInputStream  in  = new ObjectInputStream(request.getInputStream());
			 ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream())) {
			Command command = (Command) in.readObject();
			command.execute(bank);
			out.writeObject(command);
			response.getOutputStream().close();
		} catch (Exception e) {
			throw new InternalError(e.getMessage());
		}
	}
}
