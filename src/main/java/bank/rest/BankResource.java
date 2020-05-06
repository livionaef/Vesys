package bank.rest;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.local.Driver;

@Singleton
@Path("accounts")
public class BankResource {

	private final Bank bank;

	public BankResource() {
		bank = new Driver.LocalBank();
	}

	/**
	 * Returns account numbers as set (serialized into JSON).
	 */
	@GET
	@Produces("application/json")
	public Set<String> getAccountNumbers() throws IOException {
		return bank.getAccountNumbers();
	}

	/**
	 * Returns account resources as URIs.
	 */
	@GET
	@Produces("text/plain")
	public String getAccountNumbers(@Context UriInfo uriInfo) throws IOException {
		StringBuffer buf = new StringBuffer();
		for (String account : bank.getAccountNumbers()) {
			buf.append(uriInfo.getAbsolutePathBuilder().path(account).build());
			buf.append("\n");
		}
		return buf.toString();
	}

	/**
	 * <li>POST Request on account resource.
	 * <li>Parameters are passed in x-www-form-urlencodedformat.
	 * <li>Returns URI of created resource in location field
	 * <li>Result code: 201 Created
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response create(@Context UriInfo uriInfo, @FormParam("owner") String owner) throws IOException {
		String id = bank.createAccount(owner);
		URI location = uriInfo.getRequestUriBuilder().path(id).build();
		return Response.created(location).build();
	}

	/**
	 * <li>Returns an AccountDTOinstance with the fields owner and balance
	 * <li>hashCodedepends on number, active-flag and balance
	 */
	@GET
	@Produces({ "application/json" })
	@Path("{id}")
	public Response getAccount(@PathParam("id") String number, @Context Request request) throws IOException {
		Account a = bank.getAccount(number);
		if (a == null)
			throw new NotFoundException();
		String eTag = "" + a.hashCode();
		var builder = request.evaluatePreconditions(new EntityTag(eTag));
		if (builder != null) {
			return builder.build();
		} else {
			return Response.ok(new AccountDto(a)).tag(eTag).build();
		}
	}
	
	@PUT
	@Path("{id}")
	@Produces({"application/json"})
	@Consumes({"application/json"})
	public Response putAccount(@Context Request request, @PathParam("id") String number, AccountDto dto) throws IOException {
		Account a = bank.getAccount(number);
		if (a == null) throw new NotFoundException("Account not found"); // 404
		
		String eTag = "" + a.hashCode();
		ResponseBuilder builder = request.evaluatePreconditions();
		if (builder != null) {
			return builder.build();
		}
		double delta = dto.getBalance() + a.getBalance();
		if (delta != 0) {
			try {
				if (delta > 0) a.deposit(delta);
				else a.withdraw(-delta);
			} catch (IllegalArgumentException e) { 	// 400
				throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
			} catch (InactiveException e) { 		// 410
				throw new WebApplicationException(e, Response.Status.GONE);
			} catch (OverdrawException e) { 		// 403
				throw new WebApplicationException(e, Response.Status.FORBIDDEN);
			}
			return Response.ok(new AccountDto(a)).tag(eTag).build();
		}
		// TODO: what is returned here?
		return null;
	}
	
	@DELETE
	@Path("{id}")
	public Response delete(@PathParam("id") String number) throws IOException {
		if (bank.closeAccount(number)) {
			return Response.ok().build();
		} else {
			throw new WebApplicationException(Status.NOT_ACCEPTABLE); // 406
		}
	}
}
