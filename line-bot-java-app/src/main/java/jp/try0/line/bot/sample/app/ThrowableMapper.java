package jp.try0.line.bot.sample.app;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExceptionMapper
 *
 * @author Ryo Tsunoda
 *
 */
@Provider
@Singleton
public class ThrowableMapper implements ExceptionMapper<Throwable> {

	private static Logger logger = LoggerFactory.getLogger(ThrowableMapper.class);

	@Override
	public Response toResponse(Throwable exception) {
		logger.error("toResponse", exception);

		if (exception instanceof WebApplicationException) {
			return ((WebApplicationException) exception).getResponse();
		}

		Map<String, Object> error = new HashMap<>();
		error.put("status", "error");
		error.put("message", exception.getMessage());

		return Response.status(500).entity(error).build();
	}

}
