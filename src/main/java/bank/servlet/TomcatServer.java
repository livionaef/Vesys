package bank.servlet;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import bank.local.Driver;
import bank.local.Driver.LocalBank;

public class TomcatServer {
	
	public static void main(String[] args) throws Exception {
		LocalBank bank = new Driver.LocalBank();
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(1234);
		tomcat.setBaseDir("build");
		
		// contextPath anpassen nach Servers.txt config!
		String contextPath = "/servlet-bank";
		String urlPattern = "/*";
		Context context = tomcat.addContext(contextPath, new File(".").getAbsolutePath());
		
		String servletName = "ServerServlet";
        Tomcat.addServlet(context, servletName, new ServerServlet(bank));
        context.addServletMappingDecoded(urlPattern, servletName);

        tomcat.getConnector(); // creates the default connector
        tomcat.start();
        tomcat.getServer().await();
	}
}
