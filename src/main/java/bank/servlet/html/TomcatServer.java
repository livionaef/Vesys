package bank.servlet.html;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import bank.local.Driver;
import bank.local.Driver.LocalBank;

public class TomcatServer {

	// TODO: Only default & create are accessible (path changes in browser url)
	// and only the main default page gets displayed
	// the other pages are not displayed
	// ? What is missing in this implementation ?
	public static void main(String[] args) throws Exception {
		LocalBank bank = new Driver.LocalBank();
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(8080);
		tomcat.setBaseDir("build");

		String contextPath = "/livio";
		String urlPattern = "/*";
		Context context = tomcat.addContext(contextPath, new File(".").getAbsolutePath());

		String[] servletNames = { "CreateServlet", "DepositServlet", "WithdrawServlet", "TransferServlet", "CloseServlet", "DefaultServlet" };
		for (String servletName : servletNames) {
			Tomcat.addServlet(context, servletName, new CreateServlet(bank));
			context.addServletMappingDecoded(urlPattern, servletName);
		}

		tomcat.getConnector(); // creates the default connector
		tomcat.start();
		tomcat.getServer().await();
	}
}
