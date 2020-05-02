package bank.servlet;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import bank.local.Driver;
import bank.local.Driver.LocalBank;

public class TomcatServer {
	
	public static void main(String[] args) throws Exception {
		new TomcatServer(1234);
	}

	private TomcatServer(int port) throws Exception {
		LocalBank bank = new Driver.LocalBank();
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(port);
		Context context = tomcat.addContext("", new File(".").getAbsolutePath());
        Tomcat.addServlet(context, "ServletServer", new ServletServer(bank));
        context.addServletMappingDecoded("/*", "ServletServer");

        tomcat.start();
        tomcat.getServer().await();
	}
}
