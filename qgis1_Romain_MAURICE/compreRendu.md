Romain MAURICE INFO1

1_Processings
===

que font les algos suivants ?

1) Calculatrice de champ

Cet algorithme calcule une nouvelle couche vectorielle avec les mêmes entités que la couche en entrée, mais en écrasant un attribut existant ou en ajoutant un attribut supplémentaire. Les valeurs de ce champ sont calculées à partir de chaque entité en utilisant une expression, basée sur les propriétés et les attributs de l'entité. Notez que si 'Nom du champ' est un champ existant dans la couche, alors tous les autres paramètres du champ sont ignorés.

2) Reprojeter une couche

Cet algorithme reprojette une couche vecteur. Il crée une nouvelle couche avec les mêmes éléments que la couche d'entrée, mais pour lesquels les géométries ont été projetées dans le nouveau SCR.

3) Joindre les attributs pas localisation

Cet algorithme prend une couche vectorielle d'entrée et crée une nouvelle couche vectorielle qui est une version étendue de celle d'entrée, avec des attributs supplémentaires.
Les attributs supplémentaires et leurs valeurs sont extraits d'une deuxième couche vectorielle. Un critère spatial est appliqué pour sélectionner les valeurs de la deuxième couche qui sont ajoutées à chaque élément de la première couche de la couche résultante.

4) Extraire les sommets

Cet algorithme prend une couche de lignes ou de polygones et génère une couche de points représentant les sommets des lignes ou des polygones source. Les attributs associés à chaque point sont les mêmes que ceux associés à la ligne ou au polygone dont ils sont issus.
Des champs supplémentaires sont ajoutés au point indiquant l'index du sommet (commençant à 0), la partie du sommet et son index dans la pièce (ainsi que son anneau pour les polygones), la distance le long de la géométrie originale et la bissectrice de la géométrie d'origine.

5) Centroïdes

Cet algorithme créé un nouvelle couche de type point, ou les points représentent le barycentre des entités d'une couche d'entrée.
Les attributs associés aux points dans la couche de sortie sont ceux des points de la couche d'origine.

6) Tampon

Cet algorithme calcule une zone tampon pour toutes les entités d'une couche d'entrée, en utilisant une distance fixe ou dynamique.
Le paramètre de segments contrôle le nombre de segments de ligne à utiliser pour approximer un quart de cercle lors de la création de décalages arrondis.
Le paramètre de style contrôle comment les terminaisons de ligne sont traitées dans le tampon.
Le paramètre de style de jointure spécifie si les joints ronds, à onglets ou biseautés doivent être utilisés lors du décalage des coins dans une ligne. Le paramètre de limite d'onglet n'est applicable que pour les styles de jointure à onglets, et contrôle la distance maximale de la courbe de décalage à utiliser lors de la création d'une jointure à onglets.

7) Translater

Cet algorithme déplace les géométries à l'intérieur d'une couche, en les décalant d'une valeur en X et en Y a spécifier.
Les valeurs Z et M présentes dans la géométrie peuvent également être translatées.

8) Prélèvement des valeurs rasters vers ponctuels

Cet algorithme crée une nouvelle couche vectorielle avec les mêmes attributs que la couche d'entrée et les valeurs rasters correspondant à l'emplacement du point.
Si la couche raster a plus d'une bande, les valeurs de toutes les bandes sont prélevées.


2_python
====

on peut créer une couche Vecteur avec le terminal python en exécutant la commande
scratchLayer = QgsVectorLayer(uri, "Scratch point layer",  "memory")

on peut créer une couche Raster avec le terminal python en exécutant la commande
my_raster_layer = QgsRasterLayer("/path/to/file.tif", "my layer")

une API est nécessaire pour utiliser le geocoding

Il existe le forward geocoding, qui transforme une String en coordonnées spatiales x/y
et le reverse geocoding qui transforme des coordonnées spatiales x/y en chaîne de caractère

3_databases
====

select ST_AsText(geometry)
from sortie
Renvoie les coordonnées de chaque point crée par la couche croisement de l'ex 1

il est possible de stocker des données vecteur et des données raster dans un geopackage

CREATE TABLE building (
  id INTEGER PRIMARY KEY,
  geometry GEOMETRY(Polygon, 2154)
);

CREATE TABLE neighborhood (
  id INTEGER PRIMARY KEY,
  name TEXT,
  geometry GEOMETRY(Polygon, 2154)
);

CREATE TABLE poi (
  id INTEGER PRIMARY KEY,
  name TEXT,
  type TEXT,
  geometry GEOMETRY(Point, 4326)
);

INSERT INTO poi (id, name, type, geometry)
VALUES
  (1, 'data1', 'Type', GEOMETRY(Point, 4326)),
  (2, 'data2', 'Type', GEOMETRY(Point, 4326)),


- récupérer les géométries des POI au format WKT (well-known-text):
SELECT id, name, ST_AsText(geometry)
FROM poi;

- afficher les superficies des quartiers triées par ordre decroissant
  SELECT id, name, ST_Area(geometry)
  FROM neighborhood
  ORDER BY area DESC;

- récupérer en WKT les centroïdes des bâtiments (centre de gravité du polygone)
SELECT id, ST_AsText(ST_Centroid(geometry)) AS centroid_wkt
FROM building;

- récupérer les id des bâtiments qui chevauchent une limite de quartier
SELECT building_id
FROM building, neighborhood
WHERE ST_Intersects(building_geometry, neighborhood_geometry);