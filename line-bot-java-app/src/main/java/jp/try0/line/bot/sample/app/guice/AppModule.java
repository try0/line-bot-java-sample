package jp.try0.line.bot.sample.app.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import jp.try0.line.bot.sample.app.ThrowableMapper;
import jp.try0.line.bot.sample.app.resource.WebhookResource;

/**
 * 実装クラスバインドモジュール
 *
 * @author Ryo Tsunoda
 *
 */
public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(WebhookResource.class);
		bind(ThrowableMapper.class);

		bindInterceptor(Matchers.any(), Matchers.any(), new LoggingInterceptor());
	}

}
