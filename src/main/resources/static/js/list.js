/**Copyright (C) 2018-2019  Piotr Czapik.
 * @author Piotr Czapik
 *
 *  This file is part of EnquirySystem.
 *  EnquirySystem is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  EnquirySystem is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with EnquirySystem.  If not, see <http://www.gnu.org/licenses/>
 *  or write to: latidude99@gmail.com
 */


$(function () {

    $('input[name="dateRange"]').daterangepicker({
        autoUpdateInput: false,
        locale: {
            cancelLabel: 'Clear'
        }
    });

    $('input[name="dateRange"]').on('apply.daterangepicker', function (ev, picker) {
        $(this).val(picker.startDate.format('DD/MM/YYYY') + ' - ' + picker.endDate.format('DD/MM/YYYY'));
    });

    $('input[name="dateRange"]').on('cancel.daterangepicker', function (ev, picker) {
        $(this).val('');
    });

});

$(document).ready(function () {
    $('[data-toggle="simpleQueryStringPopover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="phraseSlop3Popover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="phraseSlop2Popover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="phraseSlop1Popover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="phraseExactPopover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="keywordWildcardPopover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="keywordFuzzy2Popover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="keywordFuzzy1Popover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="keywordExactPopover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="regularSearchPopover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="fullTextSearchPopover"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="searchId"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="enquiry"]').popover();
});

$(document).ready(function () {
    $('[data-toggle="tooltip"]').tooltip();
});
		
		
		