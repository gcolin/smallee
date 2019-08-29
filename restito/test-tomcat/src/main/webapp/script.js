window.sendPut = function() {
	$.ajax({
		url : 'post',
		method : 'put',
		data: {value : $("#value").val()}
	}).done(function(msg) {
		console.log(msg)
		alert( msg );
	})
}