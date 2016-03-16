-- MySQL dump 10.13  Distrib 5.5.47, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: mediaspot
-- ------------------------------------------------------
-- Server version	5.5.47-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `carro`
--

DROP TABLE IF EXISTS `carro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `carro` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(254) DEFAULT NULL,
  `description` text,
  `img` varchar(254) DEFAULT NULL,
  `keywords` varchar(254) DEFAULT NULL,
  `state` varchar(40) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carro`
--

LOCK TABLES `carro` WRITE;
/*!40000 ALTER TABLE `carro` DISABLE KEYS */;
INSERT INTO `carro` VALUES (1,'Mercedes-Benz E 250','5 portas, grande\r\nnovo 0km','bmwe250.jpeg','bmw, e 250','active'),(3,'BMW M6','bmw m6 thing here','bmwm6-1.jpeg','bmw, m6','active'),(4,'Bugatti Veyron','2 portas \r\n0km\r\n400Km/h','bugatti-veyron-super-sport-.jpg','bugatti veyron, miguel pereira','active'),(10,'Mercedes-Benz E 250','5 portas, grande\r\nnovo 0km','bmwe250.jpeg','bmw, e 250','active'),(11,'Mercedes-Benz E 250','5 portas, grande\r\nnovo 0km','logo.png','bmw, e 250','unactive'),(12,'Mercedes-Benz E 250','5 portas, grande\r\nnovo 0km','huracan-front-876.jpg','bmw, e 250','active'),(13,'Mercedes-Benz E 250','5 portas, grande\r\nnovo 0km','new-huracan-front-876.jpg','bmw, e 250','active'),(14,'Mercedes-Benz E 250','5 portas, grande\r\nnovo 0km','new-huracan-front-876.jpg','bmw, e 250','active'),(15,'Mercedes-Benz E 250','5 portas, grande\r\nnovo 0km','logo.png','lanborgini huracan','active'),(16,'Brasilia 1977','Bom carro','new-volkswagen-brasilia-1977-variant-sp2-buggy-13790-MLB4009032683_032013-F.jpg','brasilia, 1977, 2 portas','active'),(17,'Brasilia 1977','Bom carro, em bom estado e anda bem','logo.png','brasilia, 1977, 2 portas','active'),(18,'Brasilia 1977','Bom carro em bom estado e boa pintura','logo.png','brasilia, 1977, 2 portas','active'),(19,'Brasilia 1977','Bom carro','logo.png','brasilia, 1977, 2 portas','active'),(20,'Brasilia 1977','Bom carro','new-volkswagen-brasilia-1977-variant-sp2-buggy-13790-MLB4009032683_032013-F.jpg','brasilia, 1977, 2 portas','active'),(21,'Brasilia 1977','Bom carro','new-volkswagen-brasilia-1977-variant-sp2-buggy-13790-MLB4009032683_032013-F.jpg','brasilia, 1977, 2 portas','active'),(23,'Brasilia 1977 ULTRA','Bom carro','logo.png','brasilia, 1977, 2 portas','active');
/*!40000 ALTER TABLE `carro` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `media`
--

DROP TABLE IF EXISTS `media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `media` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `filename` varchar(254) DEFAULT NULL,
  `description` varchar(254) DEFAULT NULL,
  `name` varchar(254) DEFAULT NULL,
  `datein` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `media`
--

LOCK TABLES `media` WRITE;
/*!40000 ALTER TABLE `media` DISABLE KEYS */;
/*!40000 ALTER TABLE `media` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-03-15 23:26:57
