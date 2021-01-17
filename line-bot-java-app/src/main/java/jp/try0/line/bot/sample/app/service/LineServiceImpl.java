package jp.try0.line.bot.sample.app.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.Broadcast;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.Narrowcast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.group.GroupMemberCountResponse;
import com.linecorp.bot.model.group.GroupSummaryResponse;
import com.linecorp.bot.model.profile.MembersIdsResponse;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.request.SetWebhookEndpointRequest;
import com.linecorp.bot.model.request.TestWebhookEndpointRequest;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.model.response.BotInfoResponse;
import com.linecorp.bot.model.response.GetMessageEventResponse;
import com.linecorp.bot.model.response.GetNumberOfFollowersResponse;
import com.linecorp.bot.model.response.GetNumberOfMessageDeliveriesResponse;
import com.linecorp.bot.model.response.GetWebhookEndpointResponse;
import com.linecorp.bot.model.response.IssueLinkTokenResponse;
import com.linecorp.bot.model.response.MessageQuotaResponse;
import com.linecorp.bot.model.response.NarrowcastProgressResponse;
import com.linecorp.bot.model.response.NumberOfMessagesResponse;
import com.linecorp.bot.model.response.QuotaConsumptionResponse;
import com.linecorp.bot.model.response.SetWebhookEndpointResponse;
import com.linecorp.bot.model.response.TestWebhookEndpointResponse;
import com.linecorp.bot.model.response.demographics.GetFriendsDemographicsResponse;
import com.linecorp.bot.model.richmenu.RichMenu;
import com.linecorp.bot.model.richmenu.RichMenuIdResponse;
import com.linecorp.bot.model.richmenu.RichMenuListResponse;
import com.linecorp.bot.model.richmenu.RichMenuResponse;
import com.linecorp.bot.model.room.RoomMemberCountResponse;
import com.linecorp.bot.parser.LineSignatureValidator;
import com.linecorp.bot.servlet.LineBotCallbackException;
import com.linecorp.bot.servlet.LineBotCallbackRequestParser;

/**
 * {@link LineMessagingClient}ラッパークラス
 *
 * @author Ryo Tsunoda
 *
 */
@Singleton
public class LineServiceImpl implements LineService {

	private static Logger logger = LoggerFactory.getLogger(LineServiceImpl.class);

	private final LineMessagingClient client = LineMessagingClient.builder(getChannelAccessToken())
			.build();

	@Override
	public LineMessagingClient getLineMessagingClient() {
		return client;
	}

	@Override
	public CallbackRequest getCallbackRequest(HttpServletRequest request) {

		LineSignatureValidator validator = new LineSignatureValidator(
				getChannelSecret().getBytes(StandardCharsets.UTF_8));
		LineBotCallbackRequestParser parser = new LineBotCallbackRequestParser(validator);

		try {
			// X-Line-Signatureヘッダーの検証も行われる
			CallbackRequest callbackRequest = parser.handle(request);
			return callbackRequest;
		} catch (IOException e) {
			logger.error("IOException", e);
			throw new RuntimeException(e);
		} catch (LineBotCallbackException e) {
			logger.error("署名検証エラー", e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public CompletableFuture<BotApiResponse> replyMessage(ReplyMessage replyMessage) {
		return getLineMessagingClient().replyMessage(replyMessage);
	}

	@Override
	public CompletableFuture<BotApiResponse> pushMessage(PushMessage pushMessage) {
		return getLineMessagingClient().pushMessage(pushMessage);
	}

	@Override
	public CompletableFuture<BotApiResponse> multicast(Multicast multicast) {
		return getLineMessagingClient().multicast(multicast);
	}

	@Override
	public CompletableFuture<BotApiResponse> broadcast(Broadcast broadcast) {
		return getLineMessagingClient().broadcast(broadcast);
	}

	@Override
	public CompletableFuture<BotApiResponse> narrowcast(Narrowcast broadcast) {
		return getLineMessagingClient().narrowcast(broadcast);
	}

	@Override
	public CompletableFuture<NarrowcastProgressResponse> getNarrowcastProgress(String requestId) {
		return getLineMessagingClient().getNarrowcastProgress(requestId);
	}

	@Override
	public CompletableFuture<MessageQuotaResponse> getMessageQuota() {
		return getLineMessagingClient().getMessageQuota();
	}

	@Override
	public CompletableFuture<QuotaConsumptionResponse> getMessageQuotaConsumption() {
		return getLineMessagingClient().getMessageQuotaConsumption();
	}

	@Override
	public CompletableFuture<NumberOfMessagesResponse> getNumberOfSentReplyMessages(String date) {
		return getLineMessagingClient().getNumberOfSentReplyMessages(date);
	}

	@Override
	public CompletableFuture<NumberOfMessagesResponse> getNumberOfSentPushMessages(String date) {
		return getLineMessagingClient().getNumberOfSentPushMessages(date);
	}

	@Override
	public CompletableFuture<NumberOfMessagesResponse> getNumberOfSentMulticastMessages(String date) {
		return getLineMessagingClient().getNumberOfSentMulticastMessages(date);
	}

	@Override
	public CompletableFuture<NumberOfMessagesResponse> getNumberOfSentBroadcastMessages(String date) {
		return getLineMessagingClient().getNumberOfSentBroadcastMessages(date);
	}

	@Override
	public CompletableFuture<UserProfileResponse> getProfile(String userId) {
		return getLineMessagingClient().getProfile(userId);
	}

	@Override
	public CompletableFuture<UserProfileResponse> getGroupMemberProfile(String groupId, String userId) {
		return getLineMessagingClient().getGroupMemberProfile(groupId, userId);
	}

	@Override
	public CompletableFuture<UserProfileResponse> getRoomMemberProfile(String roomId, String userId) {
		return getLineMessagingClient().getRoomMemberProfile(roomId, userId);
	}

	@Override
	public CompletableFuture<MembersIdsResponse> getGroupMembersIds(String groupId, String start) {
		return getLineMessagingClient().getGroupMembersIds(groupId, start);
	}

	@Override
	public CompletableFuture<MembersIdsResponse> getRoomMembersIds(String roomId, String start) {
		return getLineMessagingClient().getRoomMembersIds(roomId, start);
	}

	@Override
	public CompletableFuture<BotApiResponse> leaveGroup(String groupId) {
		return getLineMessagingClient().leaveGroup(groupId);
	}

	@Override
	public CompletableFuture<BotApiResponse> leaveRoom(String roomId) {
		return getLineMessagingClient().leaveRoom(roomId);
	}

	@Override
	public CompletableFuture<GroupSummaryResponse> getGroupSummary(String groupId) {
		return getLineMessagingClient().getGroupSummary(groupId);
	}

	@Override
	public CompletableFuture<GroupMemberCountResponse> getGroupMemberCount(String groupId) {
		return getLineMessagingClient().getGroupMemberCount(groupId);
	}

	@Override
	public CompletableFuture<RoomMemberCountResponse> getRoomMemberCount(String roomId) {
		return getLineMessagingClient().getRoomMemberCount(roomId);
	}

	@Override
	public CompletableFuture<RichMenuResponse> getRichMenu(String richMenuId) {
		return getLineMessagingClient().getRichMenu(richMenuId);
	}

	@Override
	public CompletableFuture<RichMenuIdResponse> createRichMenu(RichMenu richMenu) {
		return getLineMessagingClient().createRichMenu(richMenu);
	}

	@Override
	public CompletableFuture<BotApiResponse> deleteRichMenu(String richMenuId) {
		return getLineMessagingClient().deleteRichMenu(richMenuId);
	}

	@Override
	public CompletableFuture<RichMenuIdResponse> getRichMenuIdOfUser(String userId) {
		return getLineMessagingClient().getRichMenuIdOfUser(userId);
	}

	@Override
	public CompletableFuture<BotApiResponse> linkRichMenuIdToUser(String userId, String richMenuId) {
		return getLineMessagingClient().linkRichMenuIdToUser(userId, richMenuId);
	}

	@Override
	public CompletableFuture<BotApiResponse> linkRichMenuIdToUsers(List<String> userIds, String richMenuId) {
		return getLineMessagingClient().linkRichMenuIdToUsers(userIds, richMenuId);
	}

	@Override
	public CompletableFuture<BotApiResponse> unlinkRichMenuIdFromUser(String userId) {
		return getLineMessagingClient().unlinkRichMenuIdFromUser(userId);
	}

	@Override
	public CompletableFuture<BotApiResponse> unlinkRichMenuIdFromUsers(List<String> userIds) {
		return getLineMessagingClient().unlinkRichMenuIdFromUsers(userIds);
	}

	@Override
	public CompletableFuture<RichMenuListResponse> getRichMenuList() {
		return getLineMessagingClient().getRichMenuList();
	}

	@Override
	public CompletableFuture<BotApiResponse> setDefaultRichMenu(String richMenuId) {
		return getLineMessagingClient().setDefaultRichMenu(richMenuId);
	}

	@Override
	public CompletableFuture<RichMenuIdResponse> getDefaultRichMenuId() {
		return getLineMessagingClient().getDefaultRichMenuId();
	}

	@Override
	public CompletableFuture<BotApiResponse> cancelDefaultRichMenu() {
		return getLineMessagingClient().cancelDefaultRichMenu();
	}

	@Override
	public CompletableFuture<IssueLinkTokenResponse> issueLinkToken(String userId) {
		return getLineMessagingClient().issueLinkToken(userId);
	}

	@Override
	public CompletableFuture<GetNumberOfMessageDeliveriesResponse> getNumberOfMessageDeliveries(String date) {
		return getLineMessagingClient().getNumberOfMessageDeliveries(date);
	}

	@Override
	public CompletableFuture<GetNumberOfFollowersResponse> getNumberOfFollowers(String date) {
		return getLineMessagingClient().getNumberOfFollowers(date);
	}

	@Override
	public CompletableFuture<GetMessageEventResponse> getMessageEvent(String requestId) {
		return getLineMessagingClient().getMessageEvent(requestId);
	}

	@Override
	public CompletableFuture<GetFriendsDemographicsResponse> getFriendsDemographics() {
		return getLineMessagingClient().getFriendsDemographics();
	}

	@Override
	public CompletableFuture<BotInfoResponse> getBotInfo() {
		return getLineMessagingClient().getBotInfo();
	}

	@Override
	public CompletableFuture<GetWebhookEndpointResponse> getWebhookEndpoint() {
		return getLineMessagingClient().getWebhookEndpoint();
	}

	@Override
	public CompletableFuture<SetWebhookEndpointResponse> setWebhookEndpoint(SetWebhookEndpointRequest request) {
		return getLineMessagingClient().setWebhookEndpoint(request);
	}

	@Override
	public CompletableFuture<TestWebhookEndpointResponse> testWebhookEndpoint(TestWebhookEndpointRequest request) {
		return getLineMessagingClient().testWebhookEndpoint(request);
	}

}
