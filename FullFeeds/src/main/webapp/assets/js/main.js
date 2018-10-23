$(document).ready(function() {
  //$("#feed_box").keydown(function(event) {
    //if (event.which == 13) {
      //this.form.submit();
      //event.preventDefault();
    //}
  //});
  $("#feed_form").submit(function(e) {
    e.preventDefault();

    var htmlRegex = /(<([^>]+)>)/gi;
    let feedValue = $("#feed_box")
      .val()
      .trim();
    if (feedValue != "") {
      $.post("/newfeed", {
        feedText: feedValue,
        currentDate: moment().format()
      }).done(function(data) {
        console.log(data);
        if (data.isUpdated) {
          $(".show-feed-container").append(
            '<div class="feed-card" id=' +
              data.id +
              '><div class="feed-action"><button class="feed-delete"></button></div><pre contenteditable="true" class="feed-title">' +
              data.feed +
              '</pre><p class="feed-date">' +
              moment(data.createdDate).format("MMMM Do YYYY, h:mm") +
              "</p></div>"
          );
        } else {
          alert("Something went wrong when uploading a feed...");
        }
      });
    } else {
      alert("Invalid feed");
    }
    $("#feed_box").val("");
  });

  $(".show-feed-container").change(function() {
    alert("Handler for .change() called.");
  });

  $("#feed_box").keyup(function(e) {
    let feed_without_tag = $(this)
      .val()
      .replace(/(<([^>]+)>)/gi, "");
    $(this).val(feed_without_tag);
  });

  // $(".feed-delete").on("click", function() {
  //   console.log(
  //     $(this)
  //       .parent()
  //       .parent()
  //       .attr("id")
  //   );
  // });

  // $(".feed-edit").on("click", function() {
  //   console.log(
  //     $(this)
  //       .parent()
  //       .parent()
  //       .attr("id")
  //   );
  // });

  $(document).on("input", ".feed-title", function(e) {
    let feed_without_tag = $(this)
      .val()
      .replace(/(<([^>]+)>)/gi, "");
    $(this).val(feed_without_tag);
  });
  $(document).on("focusout", ".feed-title", function(e) {
    var self = this;
    var id = $(this)
      .parent()
      .attr("id");
    var value = $(this).text();
    $.post("/updatefeed", { id: id, updatedValue: value }).done(function(
      response
    ) {
      var avail = $(self)
        .parent()
        .has(".feed-edited").length
        ? true
        : false;
      if (!avail) $("<p class=feed-edited>Edited</p>").insertAfter(self);
    });
  });

  $(document).on("click", ".feed-delete", function(e) {
    var self = this;
    var id = $(this)
      .parent()
      .parent()
      .attr("id");
    $.get("/deletefeed", { id: id }).done(function(response) {
      console.log(response);
      $(self)
        .parent()
        .parent()
        .remove();
    });
  });
});

$(window).on("load", function() {
  $.get("/readfeeds").done(function(data) {
    var feedObject = JSON.parse(data);
    console.log(feedObject);
    
    
    for (var i = 0; i < feedObject.length; i++) {
      var createdTime = moment(feedObject[i].createdDate);
      
      if (feedObject[i].isEdited) {
        $(".show-feed-container").append(
          '<div class="feed-card" id=' +
            feedObject[i].id +
            '><div class="feed-action"><button class="feed-delete"></button></div><pre contenteditable="true" class="feed-title">' +
            feedObject[i].feed +
            '</pre><p class="feed-edited">Edited</p><p class="feed-date">' +
            createdTime.format("MMMM Do YYYY, h:mm") +
            "</p></div>"
        );
      }else if(feedObject[i].isEdited === false) {
        $(".show-feed-container").append(
          '<div class="feed-card" id=' +
            feedObject[i].id +
            '><div class="feed-action"><button class="feed-delete"></button></div><pre contenteditable="true" class="feed-title">' +
            feedObject[i].feed +
            '</pre><p class="feed-date">' +
            createdTime.format("MMMM Do YYYY, h:mm") +
            "</p></div>"
        );
      }
    }
  });
});
