<html>
<head>
<title>Simple Ajax Example</title>
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="viewport" content="user-scalable=no, width=device-width" />
<script src="/resource/jquery-1.6.2.min.js" type="text/javascript"></script>
<script src="/resource/jquery.websocket-0.0.1.js" type="text/javascript"></script>
<script src="/resource/jquery.json-2.3.js" type="text/javascript"></script>
    <script type="text/javascript">
 
        // sends a message to the websocket server
        function sendToServer() {
            ws.send('krakenmsgA', '{ messageTextA: ' + $('#echoText').val() + ' }');
            ws.send('krakenmsgB', '{ messageTextB: ' + $('#echoText').val() + ' }');
        }
 
        // set-up web socket
        var uri = "ws://" + window.location.host + window.location.pathname + "/websocket";
        
        var ws = $.websocket(uri, "quiz-protocol", {
            open: function () {},
            close: function () {alert('Connection has been lost');},
            events: {
                krakenmsgA: function (e) { $('#returnText').append(e.data + "<br/>"); },
                krakenmsgB: function (e) { $('#returnText').append(e.data + "<br/>"); }
            }
        });
 
    </script>
<script language="Javascript">
function update() {
	$.getJSON("/update").
	done(function(json) {
		document.getElementById("question").innerHTML = json.question;
		document.getElementById("answer1").innerHTML = json.answer1;
		document.getElementById("answer2").innerHTML = json.answer2;
		document.getElementById("answer3").innerHTML = json.answer3;
		document.getElementById("answer4").innerHTML = json.answer4;
	})
	.fail(function(jqxhr, textStatus, error) {
		document.getElementById("question").innerHTML = "";
		document.getElementById("answer1").innerHTML = "";
		document.getElementById("answer2").innerHTML = "";
		document.getElementById("answer3").innerHTML = "";
		document.getElementById("answer4").innerHTML = "";
	});
}
function choose(answer) {
	$.getJSON("/choose=answer=" + answer);
}
function BlockMove(event) {
  event.preventDefault() ;
 }
setInterval("update()", 1000);
</script>
<style type="text/css">
	.box {color:white;border-radius: 15px;text-align:center;font-family:Helvetica;font-weight:bold;font-size:30px}
	a {color:white;text-decoration:none}
</style>
</head>
<body ontouchmove="BlockMove(event);" > 
  <table style="width:100%;height:100%" cellpadding="10" cellspacing="20"> 
  	<tr>
  		<td id="question" colspan="2" class="box" style="background:#E44D2E"></td>
  	</tr>
  	<tr>
  		<td class="box" style="background:#447294"><a href="javascript:choose('answer1')" id="answer1"></a></td>
  		<td class="box" style="background:#447294"><a href="javascript:choose('answer2')" id="answer2"></a></td>
  	</tr>
  	<tr>
  		<td class="box" style="background:#447294"><a href="javascript:choose('answer3')" id="answer3"></a></td>
  		<td class="box" style="background:#447294"><a href="javascript:choose('answer4')" id="answer4"></a></td>
  	</tr>
  </table>
</body>
</html>