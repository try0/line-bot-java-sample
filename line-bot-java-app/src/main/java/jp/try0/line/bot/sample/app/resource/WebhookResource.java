package jp.try0.line.bot.sample.app.resource;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.AccountLinkEvent;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.link.LinkContent;
import com.linecorp.bot.model.event.link.LinkContent.Result;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.IssueLinkTokenResponse;

import jp.try0.line.bot.sample.app.LineBotApplication;

/**
 *
 * LINEからイベントを受け取るリソースクラス<br>
 * LINEのコンソールで、このリソースのパスをWebhook URLに設定する
 *
 * @author Ryo Tsunoda
 *
 */
@Path("/")
public class WebhookResource extends AbstractWebhookResource {

	private static Logger logger = LoggerFactory.getLogger(WebhookResource.class);

	private static final String LINE_ACCOUNT_LINK_URL_FORMAT = "https://access.line.me/dialog/bot/accountLink?linkToken=%s&nonce=%s";

	// TODO 自社サービスアカウント認証URL
	private static final String YOUR_SERVICE_AUTH_FORM_URL_FORMAT = LineBotApplication.DEPLOYED_PATH
			+ "/LinkForm.jsp?lineLinkToken=%s";

	/**
	 * アカウント情報
	 *
	 * @author Ryo Tsunoda
	 *
	 */
	private static class Account implements Serializable {

		public String yourServiceUserId;
		public String yourServicePassword;
		public String yourCreatedNonce;

		public String lineLinkToken;
		public String lineUserId;

		public String getBase64EncodedYourCreatedNonce() {
			byte[] enced = Base64.getEncoder().encode(yourCreatedNonce.getBytes(StandardCharsets.UTF_8));
			return new String(enced, StandardCharsets.UTF_8);
		}

		public void logInfo() {
			logger.info(String.format(
					"yourServiceUserId: %s, yourServicePassword: %s, lineLinkToken: %s, yourCreatedNonce: %s, lineUserId: %s",
					yourServiceUserId, yourServicePassword,
					lineLinkToken, yourCreatedNonce, lineUserId));
		}
	}

	/**
	 * 連携ユーザー情報<br>
	 * Key:nonce(base64), Value:アカウント情報<br>
	 *
	 * 本来なら、DB等で保持
	 */
	private static ConcurrentHashMap<String, Account> userMapping = new ConcurrentHashMap<String, Account>();

	/**
	 * LINEから取得したユーザー連携用トークン
	 */
	private static Set<String> lineLinkTokens = Sets.newConcurrentHashSet();

	/**
	 * コンストラクター
	 */
	public WebhookResource() {
	}

	/**
	 * イベント受信時に実行されます。
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public void hook(@Context final HttpServletRequest request, @Suspended AsyncResponse response)
			throws IOException {
		handleEvent(request, response);
	}

	/**
	 * ユーザーがタイムラインにメッセージを送信したときに実行される。
	 */
	@Override
	protected ReplyMessage onReceiveTextMessageEvent(MessageEvent<TextMessageContent> event) {

		try {

			UserProfileResponse userProfile = getUserProfile(event.getSource().getUserId());
			TextMessageContent userMessage = event.getMessage();

			String botResponseText = "こんにちは！　" + userProfile.getDisplayName() + "\n"
					+ "入力データ：" + userMessage.getText();
			TextMessage textMessage = new TextMessage(botResponseText);

			ReplyMessage rep = new ReplyMessage(event.getReplyToken(), Arrays.asList(textMessage));

			return rep;
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * アカウントフォロー時に実行される。
	 */
	@Override
	protected ReplyMessage onReceiveFollowEvent(FollowEvent event) {
		try {
			// アカウント連携用トークン取得
			IssueLinkTokenResponse linkTokenRes = lineService.issueLinkToken(event.getSource().getUserId())
					.get();

			String lineLinkToken = linkTokenRes.getLinkToken();
			lineLinkTokens.add(lineLinkToken);
			logger.info(lineLinkToken);

			UserProfileResponse userProfile = getUserProfile(event.getSource().getUserId());

			// https://developers.line.biz/ja/docs/messaging-api/linking-accounts/#%E9%80%A3%E6%90%BA%E8%A7%A3%E9%99%A4%E3%81%AB%E3%81%A4%E3%81%84%E3%81%A6
			// ユーザーに連携解除機能を必ず提供すること
			// ユーザーがアカウントを連携するときに、連携解除機能があることを通知すること

			// 自社サービスのユーザー認証画面を表示するボタン付きのメッセージを返す
			List<Action> actions = Arrays
					.asList(new URIAction("連携する",
							URI.create(String.format(YOUR_SERVICE_AUTH_FORM_URL_FORMAT, lineLinkToken)),
							null));

			String botResponseText = "こんにちは！" + userProfile.getDisplayName() + "\n" + "LINEと連携するアカウント情報をフォームに入力してください。\n連携はいつでも解除可能です。";
			TemplateMessage templateMessage = new TemplateMessage("アカウント連携",
					new ButtonsTemplate(null, "アカウント連携", botResponseText, actions));

			ReplyMessage rep = new ReplyMessage(event.getReplyToken(), Arrays.asList(templateMessage));

			return rep;
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * アカウント連携イベント受信時に実行します。<br>
	 * LINEへlinkTokenとnonceを送信したのちに、LINEからイベントが発行される。
	 */
	@Override
	protected ReplyMessage onReceiveAccountLinkEvent(AccountLinkEvent event) {

		LinkContent linkContent = event.getLink();

		if (linkContent.getResult() == Result.OK) {

			String nonce = linkContent.getNonce();

			logger.info("nonce: " + nonce);

			// 自社サービスで生成したノンスでユーザー判定
			Account account = userMapping.get(nonce);

			if (account == null) {
				logger.error("Nonceが一致しない");
				return null;
			}

			account.logInfo();

			// TODO 自社サービスのユーザーIDとラインユーザーIDをセットで保存する

			account.lineUserId = event.getSource().getUserId();

			TextMessage textMessage = new TextMessage("ID: " + account.yourServiceUserId + "のライン連携が完了しました。");

			// https://developers.line.biz/ja/docs/messaging-api/linking-accounts/#%E9%80%A3%E6%90%BA%E8%A7%A3%E9%99%A4%E3%81%AB%E3%81%A4%E3%81%84%E3%81%A6
			// ユーザーに連携解除機能を必ず提供すること
			// ユーザーがアカウントを連携するときに、連携解除機能があることを通知すること

			// リッチメニューとかのほうがいいと思う
			List<Action> actions = Arrays
					.asList(new PostbackAction("解除する", "unlinkAccount"));

			TemplateMessage templateMessage = new TemplateMessage("アカウント連携解除",
					new ButtonsTemplate(null, "アカウント連携解除", "アカウントの連携を解除する場合は、以下ボタンをクリックしてください。", actions));

			ReplyMessage rep = new ReplyMessage(event.getReplyToken(), Arrays.asList(textMessage, templateMessage));

			return rep;

		}

		TextMessage textMessage = new TextMessage("連携失敗");
		ReplyMessage rep = new ReplyMessage(event.getReplyToken(), Arrays.asList(textMessage));
		return rep;
	}

	/**
	 * ポストバックアクション受信時に実行されます。
	 */
	@Override
	protected ReplyMessage onReceivePostbackEvent(PostbackEvent event) {

		if (event.getPostbackContent().getData().equals("unlinkAccount")) {
			// getParam()

			// TODO アカウント連携を解除する

			return new ReplyMessage(event.getReplyToken(), new TextMessage("アカウント連携を解除しました。"));
		}

		return null;
	}

	/**
	 * 自社サービスのユーザー認証実行後、LINEへリダイレクトします。
	 *
	 * @param request
	 * @param response
	 * @param serviceId
	 * @param servicePass
	 * @param token
	 * @throws IOException
	 */
	@POST
	@Path("account/link")
	@Produces(MediaType.APPLICATION_FORM_URLENCODED)
	public void linkAccount(@Context final HttpServletRequest request, @Suspended AsyncResponse response,
			@FormParam("yourServiceUserId") String yourServiceUserId,
			@FormParam("yourServicePassword") String yourServicePassword,
			@FormParam("lineLinkToken") String lineLinkToken) {
		logger.info("linkAccount");

		// TODO サーバーサイドバリデーション
		if (yourServiceUserId.length() > 50 || yourServicePassword.length() > 100) {
			response.resume(Response.status(400).build());
			return;
		}

		if (!lineLinkTokens.contains(lineLinkToken)) {
			response.resume(Response.status(403).build());
			return;
		}

		// TODO 自社サービスユーザー認証処理

		Account account = new Account();
		account.yourServiceUserId = yourServiceUserId;
		account.yourServicePassword = yourServicePassword;
		account.lineLinkToken = lineLinkToken;
		account.yourCreatedNonce = UUID.randomUUID().toString();

		account.logInfo();

		userMapping.put(account.getBase64EncodedYourCreatedNonce(), account);

		// LINEへリダイレクト 連携リクエスト送信
		String url = String.format(LINE_ACCOUNT_LINK_URL_FORMAT,
				account.lineLinkToken, account.getBase64EncodedYourCreatedNonce());

		logger.info(url);

		response.resume(Response.seeOther(URI.create(url)).build());
	}

}
