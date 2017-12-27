cd ~
wget http://download.geofabrik.de/north-america-latest.osm.pbf
osm2pgsql --slim -d gis -C 4000 --number-processes 4 ~/north-america-latest.osm.pbf
sudo rm -fr /var/lib/mod_tile
sudo mkdir /var/lib/mod_tile
sudo chown -R astump /var/lib/mod_tile
rm north-america-latest.osm.pbf
