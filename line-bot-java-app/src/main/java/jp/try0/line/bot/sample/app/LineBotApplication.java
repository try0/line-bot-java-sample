package jp.try0.line.bot.sample.app;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;

import jp.try0.line.bot.sample.app.guice.AppModule;
import jp.try0.line.bot.sample.app.resource.AbstractWebhookResource;
import jp.try0.line.bot.sample.app.resource.WebhookResource;
import jp.try0.line.bot.sample.app.service.LineService;

/**
 * LineBotアプリケーション
 *
 * @author Ryo Tsunoda
 *
 */
@ApplicationPath("/app")
public class LineBotApplication extends ResourceConfig {

	private static Logger logger = LoggerFactory.getLogger(AbstractWebhookResource.class);

	/**
	 * TODO　チャネルシークレット<br>
	 * もしくは起動引数
	 *
	 * @see LineService#CHANNEL_SECRET_PROP_KEY
	 */
	public static final String CHANNEL_SECRET = "";

	/**
	 * TODO　チャネルアクセストークン<br>
	 * もしくは起動引数
	 *
	 * @see LineService#CHANNEL_SECRET_PROP_KEY
	 */
	public static final String CHANNEL_ACCESS_TOKEN = "";

	public static final String DEPLOYED_PATH = "https://try0.jp/app/line-bot-sample-app";

	/**
	 * コンストラクター
	 *
	 * @param serviceLocator
	 */
	@Inject
	public LineBotApplication(ServiceLocator serviceLocator) {

		logger.info("Initialize LineBotApplication");

		logger.info("Initialize guice container");
		GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
		GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
		guiceBridge.bridgeGuiceInjector(Guice.createInjector(new AppModule()));

		LineService.initialize(CHANNEL_SECRET, CHANNEL_ACCESS_TOKEN);

		logger.info("Register resources");
		register(WebhookResource.class);
		register(ThrowableMapper.class);

		logger.info("");
	}

}
