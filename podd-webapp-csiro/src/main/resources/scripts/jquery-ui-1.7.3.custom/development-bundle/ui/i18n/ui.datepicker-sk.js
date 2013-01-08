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

/* Slovak initialisation for the jQuery UI date picker plugin. */
/* Written by Vojtech Rinik (vojto@hmm.sk). */
jQuery(function($){
	$.datepicker.regional['sk'] = {
		closeText: 'Zavrieť',
		prevText: '&#x3c;Predchádzajúci',
		nextText: 'Nasledujúci&#x3e;',
		currentText: 'Dnes',
		monthNames: ['Január','Február','Marec','Apríl','Máj','Jún',
		'Júl','August','September','Október','November','December'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Máj','Jún',
		'Júl','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['Nedel\'a','Pondelok','Utorok','Streda','Štvrtok','Piatok','Sobota'],
		dayNamesShort: ['Ned','Pon','Uto','Str','Štv','Pia','Sob'],
		dayNamesMin: ['Ne','Po','Ut','St','Št','Pia','So'],
		dateFormat: 'dd.mm.yy', firstDay: 0,
		isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['sk']);
});
