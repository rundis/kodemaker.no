:title Ta kontakt

:lead
<img src="/images/map-marker.png" id="map-marker" style="display:none">

<div class="mod"><div id="map" class="map"></div></div>
<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDi89iBAXS9WK22fa7ua4ruhVssJLpAb9w&sensor=false"></script>
<script>
google.maps.event.addDomListener(window, "load", function () {
    var kmhq = new google.maps.LatLng(59.914432, 10.731476);
    var map = new google.maps.Map(document.getElementById("map"), {
        center: kmhq,
        zoom: 15
    });
    var marker = new google.maps.Marker({
        position: kmhq,
        map: map,
        title: "<address>Munkedamsveien 3, 0161 Oslo</address>",
        icon: document.getElementById("map-marker").src
    });
});
</script>

Vår besøksadresse er Munkedamsveien 3, 0161 Oslo. Henvendelse i resepsjonen.

[Noen andre du vil snakke med?](/folk/)

:aside

<a href="/kolbjorn/"><img src="/photos/people/kolbjorn/side-profile-cropped.jpg"></a>

*Kolbjørn Jetne* <br>
Daglig leder <br>
+47 957 45 096 <br>
[kolbjorn@kodemaker.no](mailto:kolbjorn@kodemaker.no)

<a href="/gry/"><img src="/photos/people/gry/side-profile-cropped.jpg"></a>

*Gry Gautier Dale* <br>
Lederassistent <br>
+47 228 22 080 <br>
[gry@kodemaker.no](mailto:gry@kodemaker.no)


