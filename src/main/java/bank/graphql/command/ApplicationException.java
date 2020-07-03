package bank.graphql.command;

import java.util.List;
import java.util.Map;
import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.language.SourceLocation;

public class ApplicationException extends RuntimeException implements GraphQLError {
	
	private final Map<String, Object> extensions;
	
	public ApplicationException(Throwable t) {
		super(t);
		extensions = Map.of("exception", t.getClass().getName());
	}
	
	@Override
	public Map<String, Object> getExtensions() { return extensions; }

	@Override
	public List<SourceLocation> getLocations() { return null; }

	@Override
	public ErrorClassification getErrorType() {
		return ErrorType.DataFetchingException;
	}
}
