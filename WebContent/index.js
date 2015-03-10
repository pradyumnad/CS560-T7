var base_url = window.location.origin;

function tokenize() {
	var sentence = $("#sentence");
	var url = base_url + "/api/service/tokenize/"+ encodeURI(sentence.val());
	perform(url);
}

function ngrams() {
	var sentence = $("#sentence");
	var url = base_url + "/api/service/ngram/"+ encodeURI(sentence.val());
	perform(url);
}

function synonymns() {
	var sentence = $("#sentence");
	var url = base_url + "/api/service/synon/"+ encodeURI(sentence.val());
	perform(url);
}

function parents() {
	var sentence = $("#sentence");
	var str = sentence.val();
	var res = str.split(",");
	if(res.length > 2) {
		alert("Two words please, separated by a , ");
		return;
	}
	
	var url = base_url + "/api/service/parents/"+ encodeURI(res[0])+"/"+encodeURI(res[1]);
	perform(url);
}

function perform(url) {
	console.log(url);
	var resultDiv = $("#result");
	$.get(url, function(data) {
		console.log(data);
		resultDiv.text(JSON.stringify(data));
	}).fail(function(data) {
		console.log(data);
		alert(data);
	});
}