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

/*
 * customer enquiry form page script
 */
function validateForm() {
    var n = document.forms["Form"]["name"].value;
    var e = document.forms["Form"]["email"].value;
    var t = document.forms["Form"]["type"].value;
    var m = document.forms["Form"]["message"].value;
    if (n == null || n == "", e == null || e == "", t == null || t == "", m == null || m == "") {
        alert("Please fill in all the required fields!");
        return false;
    }
}

// sets the fields of multi-part file input form

$('#customFile1').on('change', function () {
    //get the file name
    var fileName = $(this).val().split('\\').pop();
    //replace the "Choose a file" label
    $(this).next('.custom-file-label').addClass("selected").html(fileName);
})

$('#customFile2').on('change', function () {
    //get the file name
    var fileName = $(this).val().split('\\').pop();
    //replace the "Choose a file" label
    $(this).next('.custom-file-label').addClass("selected").html(fileName);
})

$('#customFile3').on('change', function () {
    //get the file name
    var fileName = $(this).val().split('\\').pop();
    //replace the "Choose a file" label
    $(this).next('.custom-file-label').addClass("selected").html(fileName);
})


var drawingManager;
var selectedShape;
var colors = ['#1E90FF', '#FF1493', '#32CD32', '#FF8C00', '#4B0082'];
var selectedColor;
var colorButtons = {};

function clearSelection() {
    if (selectedShape) {
        selectedShape.setEditable(false);
        selectedShape = null;
    }
}

function setSelection(shape) {
    clearSelection();
    selectedShape = shape;
    shape.setEditable(true);
    selectColor(shape.get('fillColor') || shape.get('strokeColor'));
}

function deleteSelectedShape() {
    if (selectedShape) {
        selectedShape.setMap(null);
        // To show:
        drawingManager.setOptions({
            drawingControl: true
        });
    }
}

function selectColor(color) {
    selectedColor = color;
    for (var i = 0; i < colors.length; ++i) {
        var currColor = colors[i];
        colorButtons[currColor].style.border = currColor == color ? '2px solid #789' : '2px solid #fff';
    }

    // Retrieves the current options from the drawing manager and replaces the
    // stroke or fill color as appropriate.
    var polylineOptions = drawingManager.get('polylineOptions');
    polylineOptions.strokeColor = color;
    drawingManager.set('polylineOptions', polylineOptions);

    var rectangleOptions = drawingManager.get('rectangleOptions');
    rectangleOptions.fillColor = color;
    drawingManager.set('rectangleOptions', rectangleOptions);

    var circleOptions = drawingManager.get('circleOptions');
    circleOptions.fillColor = color;
    drawingManager.set('circleOptions', circleOptions);

    var polygonOptions = drawingManager.get('polygonOptions');
    polygonOptions.fillColor = color;
    drawingManager.set('polygonOptions', polygonOptions);
}

function setSelectedShapeColor(color) {
    if (selectedShape) {
        if (selectedShape.type == google.maps.drawing.OverlayType.POLYLINE) {
            selectedShape.set('strokeColor', color);
        } else {
            selectedShape.set('fillColor', color);
        }
    }
}

function makeColorButton(color) {
    var button = document.createElement('span');
    button.className = 'color-button';
    button.style.backgroundColor = color;
    google.maps.event.addDomListener(button, 'click', function () {
        selectColor(color);
        setSelectedShapeColor(color);
    });

    return button;
}

function buildColorPalette() {
    var colorPalette = document.getElementById('color-palette');
    for (var i = 0; i < colors.length; ++i) {
        var currColor = colors[i];
        var colorButton = makeColorButton(currColor);
        colorPalette.appendChild(colorButton);
        colorButtons[currColor] = colorButton;
    }
    selectColor(colors[0]);
}

function initialize() {
    var map = new google.maps.Map(document.getElementById('map'), {
        zoom: 7,
        center: new google.maps.LatLng(52.798, -1.299),
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        disableDefaultUI: true,
        zoomControl: true
    });

    var polyOptions = {
        strokeWeight: 0,
        fillOpacity: 0.45,
        editable: true
    };
    // Creates a drawing manager attached to the map that allows the user to draw
    // markers, lines, and shapes.
    drawingManager = new google.maps.drawing.DrawingManager({
        drawingMode: google.maps.drawing.OverlayType.POLYGON,
        drawingControlOptions: {
            drawingModes: [
                google.maps.drawing.OverlayType.POLYGON
            ]
        },
        markerOptions: {
            draggable: true
        },
        polylineOptions: {
            editable: true
        },
        rectangleOptions: polyOptions,
        circleOptions: polyOptions,
        polygonOptions: polyOptions,
        map: map
    });

    google.maps.event.addListener(drawingManager, 'overlaycomplete', function (event) {
        if (event.type == google.maps.drawing.OverlayType.CIRCLE) {
            var radius = event.overlay.getRadius();
        }
    });
    google.maps.event.addListener(drawingManager, 'overlaycomplete', function (e) {
        if (e.type != google.maps.drawing.OverlayType.MARKER) {
            // Switch back to non-drawing mode after drawing a shape.
            drawingManager.setDrawingMode(null);
            // To hide:
            drawingManager.setOptions({
                drawingControl: false
            });

            // Add an event listener that selects the newly-drawn shape when the user
            // mouses down on it.
            var newShape = e.overlay;
            newShape.type = e.type;
            google.maps.event.addListener(newShape, 'click', function () {
                setSelection(newShape);
            });
            setSelection(newShape);

            //getting polygon coords and setting a value for the form input field (id="polygon") -- my code
            var coordinates = e.overlay.getPath().getArray();
            document.getElementById('polygon').value = coordinates;
//	      console.log(coordinates); 
//		  console.log("polygon original: " + e.overlay.getPath().getArray());

            //constructing a link to GoogleMaps Static API - fitting encoded polygon in -- my code
            var closingPoint = e.overlay.getPath().getAt(0);
//	  	  var polygonClosed = e.overlay.getPath().push(closingPoint);
            e.overlay.getPath().push(closingPoint);
            var pathEncoded = google.maps.geometry.encoding.encodePath(e.overlay.getPath());
            var uriStatic = "https://maps.googleapis.com/maps/api/staticmap?size=900x1500&path=weight:3|color:blue|fillcolor:0xFFFF0033|enc:";
            var key = "&key=AIzaSyCJRlLKY8d05uhSkfmCM-ybiKwZa_RN-AQ"
            var polygonStaticLink = uriStatic + pathEncoded + key;

//   		  console.log("polygon closed: " + e.overlay.getPath().getArray());		  

            document.getElementById('polygonEncoded').value = polygonStaticLink;

        }
    });

    // Clear the current selection when the drawing mode is changed, or when the
    // map is clicked.
    google.maps.event.addListener(drawingManager, 'drawingmode_changed', clearSelection);
    google.maps.event.addListener(map, 'click', clearSelection);
    google.maps.event.addDomListener(document.getElementById('delete-button'), 'click', deleteSelectedShape);

    buildColorPalette();
}

google.maps.event.addDomListener(window, 'load', initialize);


$(document).ready(function () {
    $('#forgot').click(function () {
        $('#spinner').show();
    });
});


$(document).ready(function () {
    $('#form').click(function () {
        $('#spinner').show();
    });
});
	
	
	
	
	
	
	
	
	
	
	