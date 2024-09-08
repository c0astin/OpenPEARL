## Berechnung des Filamentdurchmessers:
Einflussgrößen:
* Drehzahl Spule
* Fördermenge Extruder
    * Drehzahl Schnecke
    * Viskosität
        * Temperatur
        * Kunststoff --> const. (PLA)
    * Füllgrad Extruder --> Füllmenge ähnlich wie bei Verbraucher Wassertank eine Sinusfunktion o. Ä.
    * Düsendurchmesser / Vordruck ??
* Umfangsgeschwindigkeit Filament
    * Aktueller Wickeldurchmesser
        * Betriebsdauer
        * Spulenbreite --> const.
        * Durchschnittl. Wickelgeschw. bzw. Durchmesser des abgelegten Filaments --> Zusammengefasst zu abgelegter Masse = Fördermenge Extruder




## Sonstige Überlegungen:
Startup:
* Heizelement einschalten und Extruder auf Temp. X +- T °C aufheizen
* Extrudermotor einschalten und warten bis Zieldrehzahl erreicht ist
* Annahme: Führung des Filaments zur Spule + Erste Wicklung ist manueller Prozess und muss nur durch eine Wartezeit von 5s bei Zieldrehzahl und Zieltemperatur berücksichtigt werden.
