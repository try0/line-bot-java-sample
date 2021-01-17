package jp.try0.line.bot.sample.app.service;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.inject.ImplementedBy;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.CallbackRequest;

/**
 * {@link LineMessagingClient}ラッパークラス
 *
 * @author Ryo Tsunoda
 *
 */
@ImplementedBy(LineServiceImpl.class)
public interface LineService extends LineMessagingClient, Serializable {

	final String CHANNEL_SECRET_PROP_KEY = "line-channel-secret";
	final String CHANNEL_ACCESS_TOKEN_PROP_KEY = "line-channel-access-token";

	/**
	 * 初期化
	 *
	 * @param chanelSecret
	 * @param accessToekn
	 */
	static void initialize(String chanelSecret, String accessToekn) {
		if (!Strings.isNullOrEmpty(chanelSecret)) {
			System.setProperty(CHANNEL_SECRET_PROP_KEY, chanelSecret);
		}

		if (!Strings.isNullOrEmpty(accessToekn)) {
			System.setProperty(CHANNEL_ACCESS_TOKEN_PROP_KEY, accessToekn);
		}

		if (Strings.isNullOrEmpty(System.getProperty(CHANNEL_SECRET_PROP_KEY))
				|| Strings.isNullOrEmpty(System.getProperty(CHANNEL_ACCESS_TOKEN_PROP_KEY))) {
			throw new IllegalStateException("チャネルシークレット、チャネルアクセストークンを設定してください。");
		}
	}

	/**
	 * チャネルシークレットを取得します。
	 *
	 * @return
	 */
	default String getChannelSecret() {
		return System.getProperty(CHANNEL_SECRET_PROP_KEY);
	}

	/**
	 * アクセストークンを取得します。
	 *
	 * @return
	 */
	default String getChannelAccessToken() {
		return System.getProperty(CHANNEL_ACCESS_TOKEN_PROP_KEY);
	}

	LineMessagingClient getLineMessagingClient();

	CallbackRequest getCallbackRequest(HttpServletRequest request);

}
