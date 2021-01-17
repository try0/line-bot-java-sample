package jp.try0.line.bot.sample.app.resource;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
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
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.UnknownMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;

import jp.try0.line.bot.sample.app.service.LineService;

/**
 * Line webhook event リソーステスト
 *
 * @author Ryo Tsunoda
 *
 */
public class AbstractWebhookResourceTest {

	private static Logger logger = LoggerFactory.getLogger(AbstractWebhookResource.class);

	@Mock
	LineService lineService;

	Set<Class<?>> eventClasses;
	Set<Class<?>> messageContentClasses;

	/**
	 * LineWebhookイベントクラスをロードします。
	 */
	public void loadEventClasses() {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		logger.info("Event classes =======================");
		try {
			eventClasses = ClassPath.from(loader)
					.getTopLevelClassesRecursive(com.linecorp.bot.model.event.Event.class.getPackage().getName())
					.stream()
					.map(info -> info.load())
					.filter(clazz -> com.linecorp.bot.model.event.Event.class.isAssignableFrom(clazz))
					// メッセージイベントは別で
					.filter(clazz -> com.linecorp.bot.model.event.MessageEvent.class != clazz)
					.filter(clazz -> !clazz.isInterface())
					.collect(Collectors.toSet());

			eventClasses.forEach(clazz -> logger.info(clazz.getName()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.info("");
		logger.info("MessageContent classes =======================");
		try {
			messageContentClasses = ClassPath.from(loader)
					.getTopLevelClassesRecursive(
							com.linecorp.bot.model.event.message.MessageContent.class.getPackage().getName())
					.stream()
					.map(info -> info.load())
					.filter(clazz -> com.linecorp.bot.model.event.message.MessageContent.class.isAssignableFrom(clazz))
					.filter(clazz -> !clazz.isInterface())
					.collect(Collectors.toSet());

			messageContentClasses.forEach(clazz -> logger.info(clazz.getName()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.info("");
	}

	/**
	 * com.linecorp.bot.model.eventで定義されているイベントが処理できるか確認する
	 *
	 * @throws IOException
	 */
	@Test
	public void handleEventTest() throws IOException {

		loadEventClasses();

		List<Event> lineWebhookEvents = Lists.newArrayList();
		lineWebhookEvents.add(AccountLinkEvent.builder().build());
		lineWebhookEvents.add(MemberLeftEvent.builder().build());
		lineWebhookEvents.add(PostbackEvent.builder().build());
		lineWebhookEvents.add(JoinEvent.builder().build());
		lineWebhookEvents.add(BeaconEvent.builder().build());
		lineWebhookEvents.add(MemberJoinedEvent.builder().build());
		lineWebhookEvents.add(LeaveEvent.builder().build());
		lineWebhookEvents.add(UnsendEvent.builder().build());
		lineWebhookEvents.add(FollowEvent.builder().build());
		lineWebhookEvents.add(UnfollowEvent.builder().build());
		lineWebhookEvents.add(UnknownEvent.builder().build());
		lineWebhookEvents.add(ThingsEvent.builder().build());
		lineWebhookEvents.add(VideoPlayCompleteEvent.builder().build());

		lineWebhookEvents.add(MessageEvent.builder().message(AudioMessageContent.builder().build()).build());
		lineWebhookEvents.add(MessageEvent.builder().message(FileMessageContent.builder().build()).build());
		lineWebhookEvents.add(MessageEvent.builder().message(LocationMessageContent.builder().build()).build());
		lineWebhookEvents.add(MessageEvent.builder().message(VideoMessageContent.builder().build()).build());
		lineWebhookEvents.add(MessageEvent.builder().message(ImageMessageContent.builder().build()).build());
		lineWebhookEvents.add(MessageEvent.builder().message(UnknownMessageContent.builder().build()).build());
		lineWebhookEvents.add(MessageEvent.builder().message(StickerMessageContent.builder().build()).build());
		lineWebhookEvents.add(MessageEvent.builder().message(TextMessageContent.builder().build()).build());

		Assertions.assertEquals(lineWebhookEvents.size(), eventClasses.size() + messageContentClasses.size(),
				"イベントインスタンスの定義が不足している");

		MockitoAnnotations.openMocks(this);
		when(lineService.getCallbackRequest(any()))
				.thenReturn(CallbackRequest.builder().events(lineWebhookEvents).build());

		List<Event> uncalledEvents = Lists.newArrayList(lineWebhookEvents);
		AbstractWebhookResource res = new AbstractWebhookResource() {

			@Override
			protected ReplyMessage onReceiveDefaultMessageEvent(Event event) {
				// 処理されるイベントを除去していく
				uncalledEvents.remove(event);
				return null;
			}

			@Override
			protected void replyMessage(ReplyMessage replyMessage) {

			}
		};
		res.lineService = lineService;

		// イベントリクエストを受信
		res.handleEvent(mock(HttpServletRequest.class), mock(AsyncResponse.class));

		// イベント処理に漏れがないか確認する
		Assertions.assertEquals(uncalledEvents.size(), 0, "フックされないイベントが存在する");
	}

}
