$(document).ready(function() {
  window.userId = getCookie("userId");
  window.cursor = "";
  let userDetails = getCookie("user_presence");
  console.log(userDetails);

  $(document).on("click", ".feed-comment", function() {
    console.log("comment click");
    $(".feed-comment-input").remove();
    $(this)
      .parent()
      .append(
        '<input class="feed-comment-input" type="text" name="comment" placeholder="Enter your comment">'
      );
    $(".feed-comment-input").focus();
  });

  $(document).on("keypress", ".feed-comment-input", function(event) {
    var self = this;
    if (event.which == 13) {
      var commentObject = {
        userId: userId,
        createdDate: moment().format(),
        feedId: $(".feed-comment-input")
          .parent()
          .attr("id"),
        commentText: $(".feed-comment-input")
          .val()
          .trim()
      };

      let init = {
        method: "POST",
        body: JSON.stringify(commentObject),
        headers: {
          Accept: "application/json,text/plain,*/*",
          "Content-type": "application/json"
        }
      };

      let url = "/feed/comment";

      fetch(url, init)
        .then(
          function(value) {
            return value.json();
          },
          function(reason) {
            console.log(reason);
            return "{'result':'failed'}";
          }
        )
        .then(function(response) {
          console.log(response);
          if(response.status){
          var jsonvalue = response.data;
          var commentTemplateSource = document.getElementById("comments-template").innerHTML;
          var commentTemplate = Handlebars.compile(commentTemplateSource);
          $(".feed-comment-input").parent().remove(".comments-container");
          for(var i = 0; i< jsonvalue.length; i++)
          {
            jsonvalue[i].time  = moment(jsonvalue[i].createdDate).format("MMM Do YYYY,h:mm a");
          }
          //console.log(commentTemplate(jsonvalue));
          // console.log($(".feed-comment-input").parent());
          $(".feed-comment-input").parent().append(commentTemplate(jsonvalue));
        }
        else{
          alert("Something went wrong while commenting the feed...");
        }
          
        });

      $(".feed-comment-input").remove();
    }
  });
  $("#feed_form").submit(function(e) {
    e.preventDefault();
    let feedText = $("#feed_box")
      .val()
      .trim();
    if (feedText != "") {
      var jsonObject = {
        Text: feedText,
        createdDate: moment().format(),
        updatedDate: moment().format()
      };
      let init = {
        method: "POST",
        body: JSON.stringify(jsonObject),
        headers: {
          Accept: "application/json,text/plain,*/*",
          "Content-type": "application/json"
        }
      };
      let url = "/feed/create/" + userId;

      fetch(url, init)
        .then(
          function(value) {
            return value.json();
          },
          function(reason) {
            console.log(reason);
            return "{'result':'failed'}";
          }
        )
        .then(function(data) {
          console.log(data);
          renderFeed(data);
        });
    } else {
      alert("Invalid feed");
    }
    $("#feed_box").val("");
  });

  $(document).on("change", ".feed-card-title", function(e) {
    e.preventDefault();
    var self = this;
    var feedObject = {

      feedId : $(this).parent().attr("id"),
      text :  $(this).val().trim(),
      updatedDate : moment().format()
    }

    console.log(feedObject);

    let init = {
      method: "PUT",
      body: JSON.stringify(feedObject),
      headers: {
        Accept: "application/json,text/plain,*/*",
        "Content-type": "application/json"
      }
    };
    let url = "/feed/update";
    fetch(url, init)
    .then(
      function(value) {
        return value.json();
      },
      function(reason) {
        console.log(reason);
        return "{'result':'failed'}";
      }
    )
    .then(function(data) {
      $(self).text(data.text);
      var avail = $(self).parent().has(".feed-edited").length? true : false;
      if (!avail)
        $("<span class=feed-edited>Edited</span>").insertAfter($(self).siblings(".feed-date"));
    });
  });

  $(document).on("click", ".feed-delete", function(e) {
    var self = this;
    var id = $(this)
      .parent()
      .parent()
      .attr("id");
    $.get("/deletefeed", {
      id: id
    }).done(function(response) {
      console.log(response);
      $(self)
        .parent()
        .parent()
        .remove();
    });
  });

  function renderFeed(data) {
    if (data.status) {
      $(".show-feed-container").prepend(
        '<div class="feed-card" id=' +
          data.id +
          '><div class="feed-action"><button class="feed-delete"></button></div><textarea class="feed-card-title" dir="auto" style="overflow: hidden; overflow-wrap: break-word; height: 34px;">' +
          data.Text +
          '</textarea><span class="like-count">0</span><button class="feed-like">Like</button> <button class="feed-comment">Comment</button><span class="feed-date">' +
          moment(data.createdDate).format("MMMM Do YYYY, h:mm") +
          "</span></div>"
      );
    } else {
      alert("Something went wrong when uploading a feed...");
    }
  }

  
  function renderAllFeed(feedData) {
    var feedObject = feedData;
    var feedTemplateSource = document.getElementById("feeds-template").innerHTML;
    var feedTemplate = Handlebars.compile(feedTemplateSource);

    for (var i = 0; i < feedObject.length; i++) {
      if (feedObject[i].userId === userId) {
        feedObject[i].isOwnFeed = true;
      } else {
        feedObject[i].isOwnFeed = false;
      }
      var createdDate = moment(feedObject[i].createdDate).format(
        "MMMM Do YYYY,h:mm a"
      );
      feedObject[i].createdDate = createdDate;
      if (feedObject[i].likedUsers.includes(null)) {
        feedObject[i].likeCount = 0;
      } else {
        feedObject[i].likeCount = feedObject[i].likedUsers.length;
      }
      if (
        feedObject[i].comments.length > 0 &&
        feedObject[i].comments.length != undefined
      ) {
        for (var j = 0; j < feedObject[i].comments.length; j++) {
          feedObject[i].commentList = true;
          feedObject[i].comments[j].time = moment(
            feedObject[i].comments[j].createdDate
          ).format("MMM Do YYYY,h:mm a");
        }
      } else {
        feedObject[i].commentList = false;
      }
    }

   return feedTemplate(feedObject);
  }

  $("#feed_box").keyup(function(e) {
    let feed_without_tag = $(this)
      .val()
      .replace(/(<([^>]+)>)/gi, "");
    $(this).val(feed_without_tag);
  });

  $(window).on("load", function() {
    readAllFeeds();
    readAllusers();
  });

  function readAllusers() {
    $.get("/user/all").done(function(userData) {
      renderUserList(userData);
    });

    function renderUserList(userData) {
      var userTemplateSource = document.getElementById("peers-template")
        .innerHTML;
      var userTemplate = Handlebars.compile(userTemplateSource);
      if (userData.status) {
        var peersHTML = userTemplate(userData.data);
        $(".peers-list-container").append(peersHTML);
        readFirstUserFeed();
      } else {
        alert("Something went wrong while rending the user list...");
      }
    }
  }
  function readAllFeeds() {
    $.get("/feed/read/all", { cursor: cursor }).done(function(feedObject) {
      console.log(feedObject);
      cursor = feedObject.cursor;
      if (feedObject.status) {
        $(".show-feed-btn .feed-card")
          .first()
          .focus();
        $(".show-feed-btn").css("display", "inline-block");
        $(".show-feed-container").prepend(renderAllFeed(feedObject.data));
      } else {
        $(".show-feed-btn").css("display", "none");
      }
    });

    $(".show-feed-btn").on("click", function() {
      readAllFeeds();
    });
  }

    function readFirstUserFeed() { 
    $.get("/feed/read/"+$(".peers-list .person").first().attr("id")).done(
      function(userData){
        $(".peers-list .person").first().addClass("active");
        renderUserFeedInPeers(userData);
      }
    )};

    function renderUserFeedInPeers(userData) {
      $(".person-feed-container").empty();
      console.log(userData);
      $(".person-feed-container").append(renderAllFeed(userData.data));
      }

      $(document).on("click", ".person",function () {
        var self = this;
        $.get("/feed/read/"+$(this).attr("id")).done(
          function(userData){
            $(".person").removeClass("active");
            $(self).addClass("active");
            console.log(userData);
            renderUserFeedInPeers(userData);
          });
        });
  $(document).on("click", ".feed-like", function() {
    var self = this;
    console.log();
    var jsonObj = {
      feedId: $(this)
        .parent()
        .attr("id"),
      userId: userId
    };
    var params = {
      method: "POST",
      body: JSON.stringify(jsonObj),
      headers: {
        Accept: "application/json,text/plain,*/*",
        "Content-type": "application/json"
      }
    };
    var urlpath = "/feed/like";

    fetch(urlpath, params)
      .then(function(value) {
        return value.json();
      })
      .then(function(likedresponse) {
        console.log(likedresponse.data);
        if (likedresponse.data[0] != null) {
          $(self).css("font-weight", "bolder");
          $(self)
            .siblings(".like-count")
            .text(likedresponse.data.length);
        } else {
          $(self)
            .siblings(".like-count")
            .text("0");
          $(self).css("font-weight", "");
        }
      });
  });

  function getCookie(name) {
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2)
      return parts
        .pop()
        .split(";")
        .shift();
  }
});
