package jp.try0.line.bot.sample.app.resource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.AccountLinkEvent;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.MemberJoinedEvent;
import com.linecorp.bot.model.event.MemberLeftEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.ThingsEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.UnknownEvent;
import com.linecorp.bot.model.event.UnsendEvent;
import com.linecorp.bot.model.event.VideoPlayCompleteEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.FileMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.UnknownMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;

import jp.try0.line.bot.sample.app.service.LineService;

/**
 * Line webhook event リソース
 *
 * @see https://developers.line.biz/ja/docs/messaging-api/
 *
 * @author Ryo Tsunoda
 *
 */
public abstract class AbstractWebhookResource {

	private static Logger logger = LoggerFactory.getLogger(AbstractWebhookResource.class);

	/**
	 *
	 * {@link LineMessagingClient} ラッパー
	 */
	@Inject
	public LineService lineService;

	/**
	 * コンストラクター
	 */
	public AbstractWebhookResource() {
	}

	/**
	 * イベントを処理します。
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public void handleEvent(final HttpServletRequest request, AsyncResponse response)
			throws IOException {
		logger.info("hook");

		// https://developers.line.biz/ja/docs/messaging-api/receiving-messages/

		// イベントを処理する前に200で返却
		// HTTP POSTリクエストの処理が後続のイベントの処理に遅延を与えないよう、イベント処理を非同期化することを推奨します。だそう
		response.resume(Response.status(200).build());

		CallbackRequest callbackRequest = lineService.getCallbackRequest(request);

		List<Event> events = callbackRequest.getEvents();

		for (Event event : events) {

			logger.info(event.getClass().getName());

			try {

				if (event instanceof MessageEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#message-event
					replyMessage(onReceiveMessageEvent((MessageEvent<?>) event));

				} else if (event instanceof UnsendEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#unsend-event
					onReceiveUnsendEvent((UnsendEvent) event);

				} else if (event instanceof FollowEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#follow-event
					replyMessage(onReceiveFollowEvent((FollowEvent) event));

				} else if (event instanceof UnfollowEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#unfollow-event
					onReceiveUnfollowEvent((UnfollowEvent) event);

				} else if (event instanceof JoinEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#join-event
					replyMessage(onReceiveJoinEvent((JoinEvent) event));

				} else if (event instanceof MemberJoinedEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#member-joined-event
					replyMessage(onReceiveMemberJoinedEvent((MemberJoinedEvent) event));

				} else if (event instanceof MemberLeftEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#member-left-event
					onReceiveMemberLeftEvent((MemberLeftEvent) event);

				} else if (event instanceof PostbackEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#postback-event
					replyMessage(onReceivePostbackEvent((PostbackEvent) event));

				} else if (event instanceof VideoPlayCompleteEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#video-viewing-complete
					replyMessage(onReceiveVideoPlayCompleteEvent((VideoPlayCompleteEvent) event));

				} else if (event instanceof BeaconEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#beacon-event
					replyMessage(onReceiveBeaconEvent((BeaconEvent) event));

				} else if (event instanceof AccountLinkEvent) {
					// https://developers.line.biz/ja/reference/messaging-api/#account-link-event
					replyMessage(onReceiveAccountLinkEvent((AccountLinkEvent) event));

				} else if (event instanceof LeaveEvent) {
					onReceiveLeaveEvent((LeaveEvent) event);

				} else if (event instanceof ThingsEvent) {
					replyMessage(onReceiveThingsEvent((ThingsEvent) event));

				} else if (event instanceof UnknownEvent) {
					onReceiveUnknownEvent((UnknownEvent) event);

				}

			} catch (Exception e) {
				logger.error(event.getClass().getName(), e);
				throw new RuntimeException(e);
			}

		}

		logger.info("hooked");

	}

	@SuppressWarnings("unchecked")
	private ReplyMessage onReceiveMessageEvent(MessageEvent<?> messageEvent) {

		MessageContent messageContent = messageEvent.getMessage();
		if (messageContent == null) {
			return null;
		}

		if (messageContent instanceof TextMessageContent) {
			return onReceiveTextMessageEvent((MessageEvent<TextMessageContent>) messageEvent);

		} else if (messageContent instanceof ImageMessageContent) {
			return onReceiveImageMessageEvent((MessageEvent<ImageMessageContent>) messageEvent);

		} else if (messageContent instanceof LocationMessageContent) {
			return onReceiveLocationMessageEvent((MessageEvent<LocationMessageContent>) messageEvent);

		} else if (messageContent instanceof AudioMessageContent) {
			return onReceiveAudioMessageEvent((MessageEvent<AudioMessageContent>) messageEvent);

		} else if (messageContent instanceof VideoMessageContent) {
			return onReceiveVideoMessageEvent((MessageEvent<VideoMessageContent>) messageEvent);

		} else if (messageContent instanceof StickerMessageContent) {
			return onReceiveStickerMessageEvent((MessageEvent<StickerMessageContent>) messageEvent);

		} else if (messageContent instanceof FileMessageContent) {
			return onReceiveFileMessageEvent((MessageEvent<FileMessageContent>) messageEvent);

		} else if (messageContent instanceof UnknownMessageContent) {
			return onReceiveUnknownMessageEvent((MessageEvent<UnknownMessageContent>) messageEvent);

		} else {
			return null;
		}

	}

	protected ReplyMessage onReceiveTextMessageEvent(MessageEvent<TextMessageContent> event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveImageMessageEvent(MessageEvent<ImageMessageContent> event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveFileMessageEvent(MessageEvent<FileMessageContent> event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveUnknownMessageEvent(MessageEvent<UnknownMessageContent> event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveAudioMessageEvent(MessageEvent<AudioMessageContent> event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveVideoMessageEvent(MessageEvent<VideoMessageContent> event) {
		return onReceiveEvent(event);
	}

	protected void onReceiveUnknownEvent(UnknownEvent event) {
		onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveThingsEvent(ThingsEvent event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveVideoPlayCompleteEvent(VideoPlayCompleteEvent event) {
		return onReceiveEvent(event);
	}

	protected void onReceiveUnsendEvent(UnsendEvent event) {
		onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveMemberJoinedEvent(MemberJoinedEvent event) {
		return onReceiveEvent(event);
	}

	protected void onReceiveMemberLeftEvent(MemberLeftEvent event) {
		onReceiveEvent(event);
	}

	protected void onReceiveUnfollowEvent(UnfollowEvent event) {
		onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveFollowEvent(FollowEvent event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveJoinEvent(JoinEvent event) {
		return onReceiveEvent(event);
	}

	protected void onReceiveLeaveEvent(LeaveEvent event) {
		onReceiveEvent(event);
	}

	protected ReplyMessage onReceivePostbackEvent(PostbackEvent event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveBeaconEvent(BeaconEvent event) {
		return onReceiveEvent(event);

	}

	protected ReplyMessage onReceiveAccountLinkEvent(AccountLinkEvent event) {
		return onReceiveEvent(event);
	}

	protected ReplyMessage onReceiveEvent(Event event) {
		return null;
	}

	/**
	 * リプライメッセージを送信します。
	 *
	 * @param replyMessage
	 */
	protected void replyMessage(ReplyMessage replyMessage) {
		try {
			BotApiResponse response = lineService.replyMessage(replyMessage)
					.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("リプライメッセージの送信に失敗", e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * ユーザー情報を取得します。
	 *
	 * @param userId
	 * @return
	 */
	public final UserProfileResponse getUserProfile(String userId) {

		try {
			UserProfileResponse userProfileResponse = lineService.getProfile(userId).get();
			return userProfileResponse;

		} catch (InterruptedException | ExecutionException e) {
			logger.error("UserProfileの取得に失敗", e);
			throw new RuntimeException(e);
		}

	}

}
