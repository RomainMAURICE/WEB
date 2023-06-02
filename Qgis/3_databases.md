# SIG et bases de données

Les SIG et la géo, ça n'existe pas sans données. Savoir requêter, transformer et extraire les infos qu'on veut est quelque chose d'important. Plusieurs manières de faire celà, déjà en utilisant l'interface graphique ou les algorithmes de traitement de QGIS (les différents algos de jointures, attributaires ou géométriques, sont typiquement du requêtage de données rendu user-friendly).

On peut aussi bien sûr visualiser et requêter des données stockées dans des bases de données.

## Requêtes SQL sur les couches présentes dans QGIS via le Database Manager

Il est possible d'exécuter des requêtes SQL sur toutes les couches vecteurs ouvertes dans QGIS, qui considère via une abstraction chaque couche comme une table, avec ses colonnes.

Le dialecte SQL utilisé est propre à QGIS, assez proche de celui de PostGreSQL mais légèrement différent.

Les mots clés classiques du SQL sont présents: select, from, where, join, group by, order by ... De même que les fonctions principales d'aggregation: count, min, max, avg, sum..

Le plus du SQL QGIS réside dans les fonctions spatiales, qui commencent généralement par `ST_` (les mêmes que celles de PostGIS)

A noter que le champ de géométrie d'une couche se nomme `geometry`

-> exécuter la requête suivante en adaptant le nom de la couche à une présente dans votre QGIS:

```sql
select ST_AsText(geometry)
from ma_couche
```

Le résultat montre les géométries au format *WKT*, pour *Well-Known-Text*, un standard qui permet de représenter des géométries en chaines de caractères (généralement les geométries sont stockées en binaire). Ce format est particulièrement utile pour diffuser/transférer des données géo, en CSV notamment. Car il est ensuite possible de reconstruire une géométrie avec la fonction `ST_GeomFromText` (PostGIS) ou `geom_from_wkt` (fonction/expression QGIS). Une amélioration du WKT est le *EWKT*, qui stocke également le code de la projection dans laquelle s'expriment les coordonnées.

Une [partie du tuto QGIS du CNRS](https://tutoqgis.cnrs.fr/06_04_req_sql.php) aborder avec des visuels le SQL dans QGIS, et la manière dont créer des nouvelles couches à partir des requêtes.

Le SQL QGIS a quelques limites à l'heure actuelle :

- pas (encore) possible de modifier des données via `UPDATE`
- les requêtes sur des couches volumineuses peuvent parfois être assez lentes, particulièrement si ça lance des jointures spatiales


## Format Geopackage

Un des formats de fichier géo standard les plus répandus est le [**géopackage**](https://www.geopackage.org/), avec l'extension de fichier *.gpkg*. En réalité il s'agit d'un format qui se base sur celui de [SQLite](https://www.sqlite.org/index.html), une librairie qui permet d'interagir et de requêter des données qui sont stockées *dans un simple fichier et non sur un serveur*.

Ainsi, un fichier geopackage est une base de données en soi, et peut ainsi contenir plusieurs couches/tables à l'intérieur.

Il est possible de lancer des requêtes SQL grâce à la commande `sqlite3`, fournie par le paquet apt qui porte le même nom.

 - télécharger le geopackage nommé *geocoding_nominatim.gpkg*

```bash
sqlite3 geocoding_nominatim.gpkg
```

- lister les différentes tables présentes: `.tables`

- `.mode column` et `.headers on` pour rendre le format des résultats des requêtes plus 'joli' (par défaut c'est un peu ...)

- afficher la structure d'une table: `pragma table_info(gpkg_geometry_columns);`

**Attention**: c'est risqué de modifier les structures et les schémas

NB: à noter qu'il est possible de stocker des données vecteur et des données raster dans un geopackage.

## PostGreSQL/PostGIS

Le standard de SGBD pour stocker des données géo est PostGreSQL, accompagné de l'extension PostGIS (dernière version stable: 3.3.2, v15 pour PostGreSQL). PostGIS est une librairie qui fournit deux choses très utiles pour stocker et requêter des données géo :

- tout un tas de fonctions, [bien documentées dans la doc](https://postgis.net/docs/reference.html#Geometry_Constructors), qui permettent de créer des géométries, de les modifier, de les croiser avec d'autres ...

- quelques [types utiles pour stocker des données géo](https://postgis.net/docs/reference.html#PostGIS_Types), notamment le type `geometry`

A noter que PostGIS gère les données vecteur et raster.

Pour "rendre un BD Postgres spatiale", il faut installer l'extension PostGIS en se connectant à la base et en lançant simplement la commande suivante:

```sql
CREATE EXTENSION postgis;
```

Auparavant il faut avoir installé le paquet apt contenant la lib, `postgresql-XX-postgis-3`. S'il y a des fans de docker, l'image *[kartoza/postgis](https://github.com/kartoza/docker-postgis)* est sympa.

## Exercice

- Donner les commandes SQL permettant de créer les tables suivantes:

    - une table `building`, qui contient un id en clé primaire et une géométry de type Polygon en Lambert 93 (code EPSG: 2154)
    - une table `neighborhood`, qui contient un id en clé primaire, un nom de type texte et une géométry de type Polygon en Lambert 93 (code EPSG: 2154)
    - une table `poi` (point of interest) qui contient un id en clé primaire, un nom de type texte, un "type" de type texte et une géométry de type Point en WGS84 (code EPSG: 4326)

- Insérer quelques lignes dans la table poi, en utilisant les fonctions de PostGIS pour créer des géométries Point

- Créer une nouvelle source de données dans QGIS, de type PostGIS (en renseigant la config de votre base de données), s'y connecter et afficher ajouter à QGIS les données des couches. Afin d'avoir quelques données, il est possible de les dessiner à la main, ou d'importer les données OpenStreetMap utilisées dans la partie processing en faisant des copier-coller entre couches (pour cela il faut activer le mode édition, puis utiliser les boutons sur la droite de celui du mode édition)

- Ecrire et lancer les quelques requêtes:

    - récupérer les géométries des POI au format WKT (well-known-text)

    - afficher les superficies des quartiers triées par ordre decroissant. Que se passe-t-il si les géométries ont une projection en dégrés (par exemple EPSG:4326) ?

    - récupérer en WKT les centroïdes des bâtiments (centre de gravité du polygone)

    - récupérer les id des bâtiments qui chevauchent une limite de quartier

    - compter le nombre de bâtiments dans chaque quartier

    - compter le nombre de POI de type 'parc' dans chaque quartier

    - récupérer la distance de chaque bâtiment au centre de gravité du quartier dans lequel il se situe

    - récupérer les bâtiments qui se trouvent à moins de 100m d'une piscine (càd un poi qui a le type 'piscine')

