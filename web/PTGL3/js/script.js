/*

Custom JS
============

*/
$(document).ready(function() {

/* Scrollspy */
$('body').scrollspy({ target: '.navbar-scrollspy' })


$(function() {
	
	$('#alertMe').click(function(e) {
		e.preventDefault();
		$('#successAlert').slideDown();
	});
	
	
	$('a.pop').click(function(e) {
		e.preventDefault();
	});
	
	$('a.pop').popover();
	$('[rel="tooltip"]').tooltip();
	
	$('#advancedButton').click( function() {
		$('#advancedSearch').slideToggle();
		$('#arrow').toggleClass('rotateArrow');
	});
	
	
	$('.chainCheckBox').click( function() {
		var selectedProteins = "";
		$('input[type=checkbox]').each(function () {
			if (this.checked) selectedProteins += this.value + " "; })
		
		$('#loadInput').val(selectedProteins);
	});
	
	$('#selectAllBtn').click( function() {
		var selectedProteins = "";
		$('input[type=checkbox]').each(function () {
			selectedProteins += this.value + " ";
			$(this).attr('checked', true);
			console.log(this.id + " is now " + $(this).attr('checked'));
		})
		$('#loadInput').val(selectedProteins);
	});
	
	
	$('#resetBtn').click( function() {
		$('#loadInput').val("");
		$('input[type=checkbox]').each(function () {
			$(this).attr('checked', false);
			console.log(this.id + " is now " + $(this).attr('checked'));
		})
		
	});	
	
	
	
	$('.bxslider').bxSlider({
		minSlides: 1,
		maxSlides: 1,
		slideWidth: 9000
});
	
	
	
});


});



