/*
 * Copyright (c) 2009 - 2010. School of Information Technology and Electrical
 * Engineering, The University of Queensland.  This software is being developed
 * for the "Phenomics Ontoogy Driven Data Management Project (PODD)" project.
 * PODD is a National e-Research Architecture Taskforce (NeAT) project
 * co-funded by ANDS and ARCS.
 *
 * PODD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PODD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PODD.  If not, see <http://www.gnu.org/licenses/>.
 */

(function($) {
	$.fn.inputlimiter = function(options) {
		var opts = $.extend({}, $.fn.inputlimiter.defaults, options);
		if ( opts.boxAttach && !$('#'+opts.boxId).length )
		{
			$('<div/>').appendTo("body").attr({id: opts.boxId, 'class': opts.boxClass}).css({'position': 'absolute'}).hide();
			// apply bgiframe if available
			if ( $.fn.bgiframe )
				$('#'+opts.boxId).bgiframe();
		}
		$(this).each(function(i){
			$(this).keyup(function(e){
				if ( $(this).val().length > opts.limit )
					$(this).val($(this).val().substring(0,opts.limit));
				if ( opts.boxAttach )
				{
					$('#'+opts.boxId).css({
						'width': $(this).outerWidth() - ($('#'+opts.boxId).outerWidth() - $('#'+opts.boxId).width()) + 'px',
						'left': $(this).offset().left + 'px',
						'top': ($(this).offset().top + $(this).outerHeight()) - 1 + 'px',
						'z-index': 2000
					});
				}
				var charsRemaining = opts.limit - $(this).val().length;

				var remText = opts.remTextFilter(opts, charsRemaining);
				var limitText = opts.limitTextFilter(opts);

				if ( opts.limitTextShow )
				{
					$('#'+opts.boxId).html(remText + ' ' + limitText);
					// Check to see if the text is wrapping in the box
					// If it is lets break it between the remaining test and the limit test
					var textWidth = $("<span/>").appendTo("body").attr({id: '19cc9195583bfae1fad88e19d443be7a', 'class': opts.boxClass}).html(remText + ' ' + limitText).innerWidth();
					$("#19cc9195583bfae1fad88e19d443be7a").remove();
					if ( textWidth > $('#'+opts.boxId).innerWidth() ) {
						$('#'+opts.boxId).html(remText + '<br />' + limitText);
					}
					// Show the limiter box
					$('#'+opts.boxId).show();
				}
				else
					$('#'+opts.boxId).html(remText).show();
			});
			$(this).keypress(function(e){
				if ( (!e.keyCode || (e.keyCode > 46 && e.keyCode < 90)) && $(this).val().length >= opts.limit )
					return false;
			});
			$(this).blur(function(){
				if ( opts.boxAttach )
				{
					$('#'+opts.boxId).fadeOut('fast');
				}
				else if ( opts.remTextHideOnBlur )
				{
					var limitText = opts.limitText;
					limitText = limitText.replace(/\%n/g, opts.limit);
					limitText = limitText.replace(/\%s/g, ( opts.limit == 1?'':'s' ));
					$('#'+opts.boxId).html(limitText);
				}
			});
		});
	};

	$.fn.inputlimiter.remtextfilter = function(opts, charsRemaining) {
		var remText = opts.remText;
		if ( charsRemaining == 0 && opts.remFullText != null ) {
			remText = opts.remFullText;
		}
		remText = remText.replace(/\%n/g, charsRemaining);
		remText = remText.replace(/\%s/g, ( opts.zeroPlural ? ( charsRemaining == 1?'':'s' ) : ( charsRemaining <= 1?'':'s' ) ) );
		return remText;
	};

	$.fn.inputlimiter.limittextfilter = function(opts) {
		var limitText = opts.limitText;
		limitText = limitText.replace(/\%n/g, opts.limit);
		limitText = limitText.replace(/\%s/g, ( opts.limit <= 1?'':'s' ));
		return limitText;
	};

	$.fn.inputlimiter.defaults = {
		limit: 255,
		boxAttach: true,
		boxId: 'limiterBox',
		boxClass: 'limiterBox',
		remText: '%n character%s remaining.',
		remTextFilter: $.fn.inputlimiter.remtextfilter,
		remTextHideOnBlur: true,
		remFullText: null,
		limitTextShow: true,
		limitText: 'Field limited to %n character%s.',
		limitTextFilter: $.fn.inputlimiter.limittextfilter,
		zeroPlural: true
	};

})(jQuery);