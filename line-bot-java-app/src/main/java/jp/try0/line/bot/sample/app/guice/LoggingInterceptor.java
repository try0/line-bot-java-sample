package jp.try0.line.bot.sample.app.guice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ログ出力インターセプター
 *
 * @author Ryo Tsunoda
 *
 */
public class LoggingInterceptor implements MethodInterceptor {

	private static Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		logger.info(invocation.getThis().getClass().getSuperclass() + "#" + invocation.getMethod().getName());
		return invocation.proceed();
	}

}
