<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>ラインアカウント連携</title>

<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/fomantic-ui/2.8.4/semantic.min.css"
	type="text/css" media="screen" title="Stylesheet">
<script src="https://code.jquery.com/jquery-3.5.1.min.js"
	integrity="sha256-9/aliU8dGd2tb6OSsuzixeV4y/faTqgFtohetphbbj0="
	crossorigin="anonymous"></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/fomantic-ui/2.8.4/semantic.min.js"></script>

<script type="text/javascript">
	$(function() {
		$("#login-form").submit(function() {

			// TODO クライアントバリデーション

			$("#login-button").addClass("loading");
		});
	});
</script>

</head>
<body>

	<div class="ui container aligned" style="padding-top: 4em;">

		<div class="ui blue segment" style="max-width: 500px; margin: auto;">
			<form id="login-form" action="app/account/link" method="post"
				class="ui form">
				<div class="ui error message"></div>
				<input type="hidden" name="lineLinkToken"
					value="<%=request.getParameter("lineLinkToken")%>">
				<div class="field">
					<label>ID</label> <input type="text" name="yourServiceUserId"
						required maxlength="50">
				</div>

				<div class="field">
					<label>PASSWORD</label> <input type="password"
						name="yourServicePassword" required maxlength="100">
				</div>

				<div class="field" style="text-align: center; padding-top: 1em;">
					<button id="login-button" type="submit"
						class="ui fluid primary centered button">ログイン</button>
				</div>
			</form>
		</div>
	</div>


</body>
</html>