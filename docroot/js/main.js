function fixDialogPosition() {	
	var $dlg = $(".doPositionDialog.ui-overlay-visible");
	var left = ($(window).width()-$dlg.width())/2;
	var top = ($(window).height()-$dlg.height())/2;
	
	$dlg.css('position', 'fixed').css('left', left+"px").css('top', top+"px");
}