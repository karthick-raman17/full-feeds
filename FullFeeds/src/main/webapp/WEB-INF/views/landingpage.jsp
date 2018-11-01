<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>

<head>
	<meta http-equiv="content-type" content="application/xhtml+xml; charset=UTF-8" />
	<title>FULL Feeds</title>
	<script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
	<link rel="stylesheet" href="/css/main.css">
</head>


<body>
	<div id="loading-icon" hidden>
		<img src="/image/reload.gif">
	</div>

	<div class="tab">
		<button class="tablinks" onclick="openTab(event, 'feeds')" id="defaultOpen">Feeds</button>
		<button class="tablinks" onclick="openTab(event, 'peers')">Peers</button>

	</div>

	<div id="feeds" class="tabcontent">
		<div class="feed-container">
			<form method="POST" id="feed_form">
				<textarea id="feed_box" name="feed-textarea" rows="10" cols="80" wrap="hard" maxlength="500" placeholder="share your thoughts"></textarea>
				<input type="submit" class="feed-update-btn" id="feed_update" value="Share" />
			</form>
		</div>
		<div class="show-feed-container">

			<!-- <div class="feed-card" id="feed-9a2ef518-ba54-44fd-93d6-a3f0652efd29">
				<div class="feed-action"><button class="feed-delete">Delete</button></div>
				<textarea class="feed-card-title" style="overflow: hidden; overflow-wrap: break-word; height: 34px;">Test Feed</textarea>
				<span class="like-count">99+</span><button class="feed-like">Like</button>
				<button class="feed-comment">Comment</button>
				<span class="feed-date">October 24th 2018, 4:44</span><span class="feed-edited">Edited</span>
				<div class="comments-container">
					<hr>
					<div class="comments">
						<span class="comment-by"><b>Karthick</b></span>
						<span class="comment-text">Comment</span>
						<span class="comment-time">4:44</span>
					</div>
				</div>
			</div>

			<div class="feed-card" id="feed-42bf194c-46cf-4a02-8716-876a60a60f86">
				<div class="feed-action"><button class="feed-delete">Delete</button></div>
				<textarea class="feed-card-title" style="overflow: hidden; overflow-wrap: break-word; height: 34px;">Karthick</textarea>
				<span class="like-count">0</span><button class="feed-like">Like</button>
				<button class="feed-comment">Comment</button>
				<span class="feed-date">October 24th 2018, 4:44</span>
			</div> -->

			<div class="more-feed-cursor"> <button class="show-feed-btn">Show more feeds</button></div>
		</div>
	</div>

	<div id="peers" class="tabcontent peers-cont">

		<div class="peers-container">

			<div class="peers-list-container">
				

			</div>
			<div class="person-feed-container">
				<!-- <div class="feed-card" id="feed-9a2ef518-ba54-44fd-93d6-a3f0652efd29">
					<div class="feed-action"><button class="feed-delete">Delete</button></div>
					<div class="feed-title" style=" overflow-wrap: break-word; height: 34px;">Test Feed</div>
					<span class="like-count">99+</span><button class="feed-like">Like</button>
					<button class="feed-comment">Comment</button>
					<span class="feed-date">October 24th 2018, 4:44</span><span class="feed-edited">Edited</span>
					<div class="comments-container">
						<hr>
						<div class="comments">
							<span class="comment-by"><b>Karthick</b></span>
							<span class="comment-text">Comment</span>
							<span class="comment-time">4:44</span>
						</div>
					</div>
				</div>
				<div class="feed-card" id="feed-9a2ef518-ba54-44fd-93d6-a3f0652efd29">
					<div class="feed-action"><button class="feed-delete">Delete</button></div>
					<div class="feed-title" style=" overflow-wrap: break-word; height: 34px;">Test Feed</div>
					<span class="like-count">99+</span><button class="feed-like">Like</button>
					<button class="feed-comment">Comment</button>
					<span class="feed-date">October 24th 2018, 4:44</span><span class="feed-edited">Edited</span>
				</div> -->

			</div>

		</div>
	</div>
	<script id="feeds-template" type="text/x-handlebars-template">
			  {{#each this }}
			  <div class="feed-card" id={{id}}>
					{{#if isOwnFeed}}
					<div class="feed-action"><button class="feed-delete">Delete</button></div>
					<textarea class="feed-card-title" style="overflow: auto; overflow-wrap: break-word; height: 34px;">{{text}}</textarea>
					{{else}}
					<div class="feed-title" style=" overflow-wrap: break-word; height: 34px;">{{text}}</div>
					{{/if}}
					<span class="like-count">{{likeCount}}</span><button class="feed-like">Like</button>
					<button class="feed-comment">Comment</button>
					<span class="feed-date">{{createdDate}}</span>
					{{#if isEdited}}
					<span class="feed-edited">Edited</span>
					{{/if}}
					{{#if commentList}}
					<div class="comments-container">
						<hr>
						{{#each comments}}
						<div class="comments">
							<span class="comment-by"><b>{{commentBy}}</b></span>
							<span class="comment-text">{{commentText}}</span>
							<span class="comment-time">{{time}}</span>
						</div>
						{{/each}}
					</div>
					{{/if}}
				</div>
			  {{/each}}
  </script>
  <script id="peers-template" type="text/x-handlebars-template">
		<ul class="peers-list">
	  {{#each this}}
	  <li class="person" id={{id}}>{{name}}</li>
			  {{/each}}
		</ul>
	</script>
	<script id="comments-template" type="text/x-handlebars-template">
	<div class="comments-container">
						<hr>
						{{#each this}}
						<div class="comments">
							<span class="comment-by"><b>{{commentBy}}</b></span>
							<span class="comment-text">{{commentText}}</span>
							<span class="comment-time">{{time}}</span>
						</div>
						{{/each}}
					</div>
	</script>

	<script>
		function openTab(evt, tabName) {
			var i, tabcontent, tablinks;
			tabcontent = document.getElementsByClassName("tabcontent");
			for (i = 0; i < tabcontent.length; i++) {
				tabcontent[i].style.display = "none";
			}
			tablinks = document.getElementsByClassName("tablinks");
			for (i = 0; i < tablinks.length; i++) {
				tablinks[i].className = tablinks[i].className.replace(" active", "");
			}
			document.getElementById(tabName).style.display = "block";
			evt.currentTarget.className += " active";
		}
		document.getElementById("defaultOpen").click();
	</script>

	<script src="/js/main.js"></script>
	<script src="https://momentjs.com/downloads/moment.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/handlebars.js/4.0.12/handlebars.min.js"></script>
</body>

</html>