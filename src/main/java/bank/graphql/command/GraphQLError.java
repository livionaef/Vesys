package bank.graphql.command;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import graphql.ErrorClassification;
import graphql.GraphqlErrorHelper;
import graphql.language.SourceLocation;

public interface GraphQLError extends Serializable {
	
	String getMessage(); // description of the error
	
	// location(s) at which the error occured
	List<SourceLocation> getLocations();
	
	// error classification (invalid syntax, validation, data fetching
	ErrorClassification getErrorType();
	
	default List<Object> getPath() { return null; }
	default Map<String, Object> toSpezification() {
		return GraphqlErrorHelper.toSpecification((graphql.GraphQLError) this);
	}
	default Map<String, Object> getExtensions() { return null; }
}
