function call(id) {
    jQuery.getScript('/lemonsource/ajax_call?id='+id);
}

function callAction(id,action) {
    jQuery.getScript('/lemonsource/ajax_call?id='+id+'&action='+action);
}

function elemCall(id) {
    jQuery.getScript('/lemonsource/ajax_elem?id='+id+'&value='+($('#'+id).val()));
}

function doSubmit(id) {
    var form = $('#'+id)
    jQuery.getScript('/lemonsource/ajax_submit?id='+id+'&'+form.serialize(), function() {
        form.dialog('close');
    });
}

function doClose(id) {
    $('#'+id).dialog('close');
}

function fieldSubmit(id) {
    jQuery.getScript('/lemonsource/ajax_field?value='+$('#'+id).val());
}

function sessionExpired() {
    showError('Session expired');
}

function multiSelectItem(tagId,idx,value) {
    if($('#'+tagId+"-"+idx).hasClass("multiselect_selected")) {
        $('#'+tagId+"-"+idx).removeClass("multiselect_selected");
        $('#'+tagId+"-formelem").text($('#'+tagId+"-formelem").val().replace("+"+value, ""));
    } else {
        $('#'+tagId+"-"+idx).addClass("multiselect_selected");
        $('#'+tagId+"-formelem").text($('#'+tagId+"-formelem").val() + "+" + value);
    }
}

function twinSelectItemL(tagId,idx) {
    if($('#'+tagId+"-"+idx).hasClass("multiselect_selected")) {
        $('#'+tagId+"-"+idx).removeClass("multiselect_selected");
        $('#'+tagId+"-l2rbutton").attr("disabled","disabled");
    } else {
        if(twinSelect_left(tagId) != null) {
            twinSelect_left(tagId).removeClass("multiselect_selected");
        }
        $('#'+tagId+"-"+idx).addClass("multiselect_selected");
        $('#'+tagId+"-l2rbutton").removeAttr("disabled");
    }
}

function twinSelect_left(tagId) {
    var x = $('#'+tagId+"-left > .multiselect_selected");
    if(x.size() == 0) {
        return null;
    } else {
        return x;
    }
}

function twinSelectItemR(tagId,idx) {
    if($('#'+tagId+"-"+idx).hasClass("multiselect_selected")) {
        $('#'+tagId+"-"+idx).removeClass("multiselect_selected");
        $('#'+tagId+"-r2lbutton").attr("disabled","disabled");
    } else {
        if(twinSelect_left(tagId) != null) {
            twinSelect_left(tagId).removeClass("multiselect_selected");
        }
        $('#'+tagId+"-"+idx).addClass("multiselect_selected");
        $('#'+tagId+"-r2lbutton").removeAttr("disabled");
    }
}

function twinSelect_right(tagId) {
    var x = $('#'+tagId+"-right > .multiselect_selected");
    if(x.size() == 0) {
        return null;
    } else {
        return x;
    }
}

function twinSelectL2R(tagId) {
    var leftSelected = twinSelect_left(tagId)
    if(leftSelected != null) {
        var i = 0;
        for(i=1;i<=$('#'+tagId+"-left > div").size();i++) {
            if($('#'+tagId+"-"+i).hasClass("multiselect_selected")) {
                break;
            }
        }
        leftSelected.removeClass("multiselect_selected");
        $('#'+tagId+"-l2rbutton").attr("disabled","disabled");
        leftSelected.detach();
        leftSelected.attr("onclick","twinSelectItemR('"+tagId+"',"+i+")");
        $('#'+tagId+"-right").append(leftSelected);
        var formElem = $('#'+tagId+"-formelem")
        formElem.attr('value', formElem.val() + "+" + leftSelected.attr("value"));
    }
}

function twinSelectR2L(tagId) {
    var rightSelected = twinSelect_right(tagId)
    if(rightSelected != null) {
        var i = 0;
        for(i=1;i<=$('#'+tagId+"-right > div").size();i++) {
            if($('#'+tagId+"-"+i).hasClass("multiselect_selected")) {
                break;
            }
        }
        rightSelected.removeClass("multiselect_selected");
        $('#'+tagId+"-r2lbutton").attr("disabled","disabled");
        rightSelected.detach();
        rightSelected.attr("onclick","twinSelectItemL('"+tagId+"',"+i+")");
        $('#'+tagId+"-left").append(rightSelected);
        $('#'+tagId+"-formelem").attr("value",$('#'+tagId+"-formelem").attr("value").replace("+" + rightSelected.attr("value"),""));
    }
}

function showPopup(popup) {
    $('#'+popup).removeClass("popup");
    $('#'+popup).slideDown('fast');
}

function hidePopup(popup) {    
    $('#'+popup).slideUp('fast');
    $('#'+popup).addClass("popup");
}

function poll() {
    $.getJSON("/lemonsource/poll",function(poll) {
        $(".tracker_progress > progress").attr("value",poll.value);
        $(".tracker_message").text(poll.message);
        if(poll.value >= 0 && poll.value <= 100) {
            setTimeout("poll();",1000);
        } else {
            $('.tracker_message').html("<a onclick=\"call('generation_report');$('.tracker').fadeOut(5000);setTimeout('$(\\'.tracker\\').remove();',5000);\" href=\"#\">Report</a>  <a href='/lemonsource/Special:LastGeneratedLexicon'>Result</a>");
        }
    });
}

function noop() { }

function showError(msg) {
    var id = Math.floor(Math.random()*100000);
    $('body').append("<div class='error_message' id='err_"+id+"'>" + msg + "</div>");
    setTimeout("$('#err_"+id+"').remove();",10000);
}

$().ready(function(){
    $.ajaxSetup({
        error:function(x,e){
            if(x.status==0){
                showError('Could not connect to server');
            }else if(x.status==404){
                showError('Requested URL not found.');
            }else if(x.status==500){
                showError('Server error.');
            }else if(e=='parsererror'){
                showError('Server Error.');
            }else if(e=='timeout'){
                showError('Request timed out.');
            }else {
                showError('Error:'+x.responseText);
            }
        }
    });
});

function hideShowDumps() {
  var obj = $('#dumps');
  if (obj.is(":hidden")) {
    return obj.slideDown('fast').fadeTo('fast', 1);
  } else {
    return obj.fadeTo('fast',0).slideUp('fast');
  }
};