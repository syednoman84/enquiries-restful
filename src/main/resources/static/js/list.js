/**
 * 
 */


        $(function() {

          $('input[name="dateRange"]').daterangepicker({
              autoUpdateInput: false,
              locale: {
                  cancelLabel: 'Clear'
              }
          });

          $('input[name="dateRange"]').on('apply.daterangepicker', function(ev, picker) {
              $(this).val(picker.startDate.format('DD/MM/YYYY') + ' - ' + picker.endDate.format('DD/MM/YYYY'));
          });

          $('input[name="dateRange"]').on('cancel.daterangepicker', function(ev, picker) {
              $(this).val('');
          });

        });
   
		$(document).ready(function(){
		    $('[data-toggle="simpleQueryStringPopover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="phraseSlop3Popover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="phraseSlop2Popover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="phraseSlop1Popover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="phraseExactPopover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="keywordWildcardPopover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="keywordFuzzy2Popover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="keywordFuzzy1Popover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="keywordExactPopover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="regularSearchPopover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="fullTextSearchPopover"]').popover(); 
		});
		
		$(document).ready(function(){
		    $('[data-toggle="searchId"]').popover(); 
		});
	
		$(document).ready(function(){
		    $('[data-toggle="enquiry"]').popover();   
		});
		
		$(document).ready(function(){
		    $('[data-toggle="tooltip"]').tooltip();   
		});
		
		
		