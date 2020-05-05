package bank.servlet.html;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class TomcatServer {

	// TODO: Only default & create are accessible (path changes in browser url)
	// and only the main default page gets displayed
	// the other pages are not displayed
	// ? What is missing in this implementation ?
	public static void main(String[] args) throws Exception {
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(8080);
		tomcat.setBaseDir("build");

		String contextPath = "/livio";
		String urlPattern = "/*";
		Context context = tomcat.addContext(contextPath, new File(".").getAbsolutePath());

		String servletName = "CreateServlet";
		Tomcat.addServlet(context, servletName, new CreateServlet());
		context.addServletMappingDecoded(urlPattern, servletName);
		
		servletName = "DepositServlet";
		Tomcat.addServlet(context, servletName, new DepositServlet());
		context.addServletMappingDecoded(urlPattern, servletName);
		
		servletName = "WithdrawServlet";
		Tomcat.addServlet(context, servletName, new WithdrawServlet());
		context.addServletMappingDecoded(urlPattern, servletName);
		
		servletName = "TransferServlet";
		Tomcat.addServlet(context, servletName, new TransferServlet());
		context.addServletMappingDecoded(urlPattern, servletName);
		
		servletName = "CloseServlet";
		Tomcat.addServlet(context, servletName, new CloseServlet());
		context.addServletMappingDecoded(urlPattern, servletName);

		servletName = "DefaultServlet";
		Tomcat.addServlet(context, servletName, new DefaultServlet());
		context.addServletMappingDecoded(urlPattern, servletName);

		tomcat.getConnector(); // creates the default connector
		tomcat.start();
		tomcat.getServer().await();
	}
}
