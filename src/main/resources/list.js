$(function() {
	$("h3 span").click(function () { alert(this.title) });
	var ftsindex = lunr(function () {
		this.field('title', {boost: 10});
		this.field('category', {boost: 8});
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
			description: axo.find('p').text(),
			category: axo.prevAll("h2").first().text()
		};
		store[ref] = doc;
		ftsindex.add(doc);
	});
	$('#s').on("keyup", function() {
		var term = $('#s').val();
		$('#results').empty();
		$('#results').show();
		$('#clears').show();
		console.log(term);
		var list = ftsindex.search(term);
		console.log(list.length,"results");
		$.each(list, function() {
			var result = store[this.ref];
			$('#results').append("<li><a href=\"#"+result.id+"\">"+result.category+"/"+result.title+"</a></li>");
		});
		if (list.length==0) {
			$('#results').append("<li><em>(nothing found)</em></li>");
		}
	});
	$('#clears').click(function() {
		$('#s').val("");
		$('#results').empty();
		$('#results').hide();
		$('#clears').hide();
		return false;
	});
	window.onhashchange = function() { 
		$(window.location.hash).fadeIn(100).fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100).fadeIn(100).fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100);
	}
});