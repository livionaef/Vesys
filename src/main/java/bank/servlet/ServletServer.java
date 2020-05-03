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
	private ServletConfig config; // XXX wird nciht verwendet.

	public ServletServer(Bank bank) {
		this.bank = bank;
	}
	
	public void init(ServletConfig config) throws ServletException {
		// XXX braucht es folglich auch nicht. Dieser Code steht so in der Basisklasse GenericServlet.
		//     Sie können also die ganze Methode streichen.
		this.config = config;
		this.init();
	}

	@Override
	public void init() throws ServletException {
		// XXX diese Methode braucht es so. Da wird die lokale Bank erzeugt.
		ServletContext context = getServletContext();
		if (context.getAttribute("bank") == null) {
			context.setAttribute("bank", new Driver.LocalBank());
		}
		bank = (Bank) context.getAttribute("bank");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		out.write("Use the POST method to access this Servlet!".getBytes());
		// XXX so können Sie prüfen (mit dem Browser) ob das servlet läuft
		out.flush();
		out.close();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("appliccation/octet-stream");
		// XXX aber Sie haben hier nur die Command-Object Variante implementiert.
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
