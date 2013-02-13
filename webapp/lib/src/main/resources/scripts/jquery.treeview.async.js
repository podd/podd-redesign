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

;(function($) {

function hasBeenClicked(id) {
    var data = [];
    var prev_clicked = $.cookie('podd_treeview');
    if (prev_clicked) {
        data = prev_clicked.split(", ");
    }
    var index = $.inArray(id, data);
    return (index > -1);
}

function load(settings, parent, isRoot, child, container) {
	$.getJSON(settings.serviceURL, {parent: parent, isRoot: isRoot}, function(response) {
		function createNode(parent) {
            var current = "";
            var nav_bar = "";
            var hover_txt = "";
            if (this.classes == "file") {
                hover_txt = "Data";
                nav_bar = "<a href=\"" + settings.redirectURL + this.parentPid + "/data/" + this.id + "\" target=\"_blank\">View</a>";
                if (this.canEdit) {
                    nav_bar += " | <a href=\"" + settings.redirectURL + this.parentPid + "/data/" + this.id + "/edit\" target=\"_blank\">Edit</a>";
                }
                if (this.canCopy) {
                    nav_bar += " | <a id=\"" + this.parentPid + ":" + this.id + "_copy\" href=\"" + settings.copyServiceURL + "?type=file" +
                        "&parentPid=" + this.parentPid + "&filename=" + this.id + "\" onclick=\"copyClicked(this.href);  return false;\">Copy</a>";
                }
            }
            if (this.classes == "project" || this.classes == "data") {
                hover_txt = "Project";
                if (this.classes == "data") {
                    hover_txt = "Metadata";
                }
                nav_bar = "<a href=\"" + settings.redirectURL + this.id + "\" target=\"_blank\">View</a>";
                if (this.canEdit) {
                    nav_bar += " | <a href=\"" + settings.redirectURL + this.id + "/edit\" target=\"_blank\">Edit</a>";
                }
                if (this.classes == "data" && this.canCopy) {
                    nav_bar += " | <a id=\"" + this.id + "_copy\" href=\"" + settings.copyServiceURL +
                        "?type=object" + "&pid=" + this.id + "\" onclick=\"copyClicked(this.href);  return false;\">Copy</a>";
                }
                if (this.canEdit) {
                        nav_bar += " | <a href=\"" + settings.pasteURL + "?type=object&target=" + this.id + "\" target=\"_blank\">Paste</a>";
                }
                if (this.canAdd) {
                    nav_bar += " | <a href=\"" + settings.redirectURL + this.id + "/add\" target=\"_blank\">Add Child</a>";
                }
            }
            if (this.classes == "predicate") {
                hover_txt = "Predicate Overview";
                if (this.canCopy) {
                    nav_bar = "<a id=\"" + this.parentPid + ":" + this.id + "_copy\" href=\"" + settings.copyServiceURL + "?type=predicate" +
                        "&predicate=" + this.id + "&parentPid=" + this.parentPid + "\" onclick=\"copyClicked(this.href); return false;\">Copy</a>";
                    if (this.canEdit) {
                        nav_bar += " | ";
                    }
                }
                if (this.canEdit) {
                    nav_bar += "<a href=\"" + settings.pasteURL + "?type=predicate" +
                        "&predicate=" + this.id + "&target=" + this.parentPid + "\" target=\"_blank\">Paste</a>";
                }
                current = $("<li>").attr("id", this.id || "").html("<span></span>" +
                    "<ins class=\"padded\">" + this.name + ", Objects: " + this.activeChildCount + " &nbsp;&nbsp;" + nav_bar + "</ins></li>").appendTo(parent);
            } else {
                if (null == this.obj_state) {
                    current = $("<li>").attr("id", this.id || "").html("<span></span>" +
                        "<ins class=\"padded\">" + this.name + ": Type: " + this.concept + " &nbsp;&nbsp;" + nav_bar + "</ins></li>").appendTo(parent);
                } else {
                    var state = this.obj_state;
                    if ("active" == state) {
                        current = $("<li>").attr("id", this.id || "").html("<span></span>" +
                            "<ins class=\"padded\">" + this.name + ": Type: " + this.concept + " &nbsp;&nbsp;" + nav_bar + "</ins></li>").appendTo(parent);
                    } else if ("deleted" == state) {
                        current = $("<li>").attr("id", this.id || "").html("<span></span>" +
                            "<ins class=\"padded\"><span class=\" descriptive\">" + this.name + ": Type: " + this.concept + " (deleted)</span>" + " &nbsp;&nbsp;" + nav_bar + "</ins></li>").appendTo(parent);
                    } else if ("inactive" == state) {
                        current = $("<li>").attr("id", this.id || "").html("<span></span>" +
                            "<ins class=\"padded\"><span class=\" descriptive\">" + this.name + ": Type: " + this.concept + " (inactive)</span>" + " &nbsp;&nbsp;" + nav_bar + "</ins></li>").appendTo(parent);
                    }
                }
            }

			if (this.classes) {
                current.children("span").attr("title", hover_txt);
                current.children("span").addClass(this.classes);
			}
			if (this.expanded) {
				current.addClass("open");
			}
			if (this.hasChildren || this.children && this.children.length) {
				var branch = $("<ul/>").appendTo(current);
				if (this.hasChildren) {
					current.addClass("hasChildren");
				}
				if (this.children && this.children.length) {
					$.each(this.children, createNode, [branch])
				}
			}
		}
		$.each(response, createNode, [child]);
        $(container).treeview({add: child});
    });
}

var proxied = $.fn.treeview;
$.fn.treeview = function(settings) {
	if (!settings.serviceURL) {
		return proxied.apply(this, arguments);
	}
	var container = this;
	load(settings, settings.root, true, this, container);
	var userToggle = settings.toggle;
	return proxied.call(this, $.extend({}, settings, {
		collapsed: true,
		toggle: function() {
			var $this = $(this);
			if ($this.hasClass("hasChildren")) {
				var childList = $this.removeClass("hasChildren").find("ul");
                load(settings, this.id, false, childList, container);
            }
			if (userToggle) {
				userToggle.apply(this, arguments);
			}
		}
	}));
};

})(jQuery);