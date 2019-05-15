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

<!-- data from database s Thymeleaf inline -->
/*<![CDATA[*/
var userId = eval([[${currentUser.id}]]);
/*]]>*/
/*<![CDATA[*/
var enquiryId = eval([[${enquiry.id}]]);
/*]]>*/

document.getElementById('userId').value = userId;
document.getElementById('enquiryId').value = enquiryId;


function initMap() {

    // defines LatLng coordinates for the polygon's path
    /*<![CDATA[*/
    var polygonCoords = eval([[${enquiry.polygon}]]);
    /*]]>*/

    // defines encoded path for the polygon
    /*<![CDATA[*/
    //   	 var polygonEncoded = eval([[${enquiry.polygonEncoded}]]);
    /*]]>*/

    // sets visible map area
    var bounds = new google.maps.LatLngBounds();
    for (var i = 0; i < polygonCoords.length; i++) {
        bounds.extend(new google.maps.LatLng(polygonCoords[i][0], polygonCoords[i][1]));
    }

    // sets the initial centre and zoom level
    var map = new google.maps.Map(document.getElementById('map-out'), {
        zoom: 8,
        center: {
            lat: polygonCoords[0][0],
            lng: polygonCoords[0][1]
        },
        mapTypeId: google.maps.MapTypeId.ROADMAP
    });

    map.setCenter(bounds.getCenter());
    map.fitBounds(bounds);


    var points = [];
    for (var i = 0; i < polygonCoords.length; i++) {
        points.push({
            lat: polygonCoords[i][0],
            lng: polygonCoords[i][1]
        });
    }
    // constructs the polygon.
    var polygonArea = new google.maps.Polygon({
        paths: points,
        strokeColor: 'blue',
        strokeOpacity: 0.7,
        strokeWeight: 2,
        fillColor: 'yellow',
        fillOpacity: 0.15
    });
    polygonArea.setMap(map);
}

google.maps.event.addDomListener(window, "load", initMap);


$(document).ready(function () {
    $('#pdf').click(function () {
        $('#spinner').show();
    });
});

$(document).ready(function () {
    $('#printable').click(function () {
        $('#spinner').show();
    });
});

$(document).ready(function () {
    $('#email').click(function () {
        $('#spinner').show();
    });
});