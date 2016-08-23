function a(element) { alert(element.title) }

$(function() {
	var ftsindex = lunr(function () {
		this.field('title', {boost: 10});
		this.field('description');
		this.ref('id');
	});
	var store = {};
	$('.axo').each(function() {
		var axo = $(this);
		var ref = axo.attr('id');
		var doc = {
			id: ref,
			title: axo.find('h3').text(),
			description: axo.find('p').text()
		};
		store[ref] = doc;
		ftsindex.add(doc);
	});
	$('#s').on("keyup", function() {
		var term = $('#s').val();
		$('#results').empty();
		console.log(term);
		var list = ftsindex.search(term);
		console.log(list.length,"results");
		$.each(list, function() {
			var result = store[this.ref];
			console.log(this.ref, result);
			$('#results').append("<li><a href=\"#"+result.id+"\">"+result.title+"</a></li>");
		});
	});
});