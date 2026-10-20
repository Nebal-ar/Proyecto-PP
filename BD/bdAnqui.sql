CREATE DATABASE  IF NOT EXISTS `alquinow` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `alquinow`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: alquinow
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `comprador`
--

DROP TABLE IF EXISTS `comprador`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comprador` (
  `ID_usuario` int NOT NULL,
  `preferencias_busqueda` text,
  `metodo_pago_preferido` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ID_usuario`),
  CONSTRAINT `comprador_ibfk_1` FOREIGN KEY (`ID_usuario`) REFERENCES `usuario` (`ID_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comprador`
--

LOCK TABLES `comprador` WRITE;
/*!40000 ALTER TABLE `comprador` DISABLE KEYS */;
INSERT INTO `comprador` VALUES (1,NULL,NULL),(7,NULL,NULL),(8,NULL,NULL);
/*!40000 ALTER TABLE `comprador` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `disponibilidad`
--

DROP TABLE IF EXISTS `disponibilidad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disponibilidad` (
  `ID_disponibilidad` int NOT NULL AUTO_INCREMENT,
  `ID_propiedad_fk` int DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `estado` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID_disponibilidad`),
  KEY `idx_disponibilidad_fecha` (`fecha`),
  KEY `idx_disponibilidad_propiedad` (`ID_propiedad_fk`),
  CONSTRAINT `disponibilidad_ibfk_1` FOREIGN KEY (`ID_propiedad_fk`) REFERENCES `propiedad` (`ID_propiedad`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `disponibilidad`
--

LOCK TABLES `disponibilidad` WRITE;
/*!40000 ALTER TABLE `disponibilidad` DISABLE KEYS */;
INSERT INTO `disponibilidad` VALUES (1,1,'2026-08-25','Ocupado'),(2,1,'2026-08-26','Ocupado'),(3,1,'2026-08-27','Ocupado'),(4,1,'2026-10-10','Ocupado'),(5,1,'2026-10-11','Ocupado'),(6,1,'2026-10-12','Ocupado'),(7,1,'2026-10-13','Ocupado'),(8,1,'2026-10-14','Ocupado'),(9,1,'2026-11-11','Ocupado'),(10,1,'2026-11-12','Ocupado'),(11,1,'2026-11-13','Ocupado'),(12,1,'2026-11-14','Ocupado'),(13,1,'2026-11-15','Ocupado'),(14,1,'2026-12-01','Ocupado'),(15,1,'2026-12-02','Ocupado'),(16,1,'2026-12-03','Ocupado'),(17,1,'2026-12-04','Ocupado'),(18,2,'2026-12-10','Ocupado'),(19,2,'2026-12-11','Ocupado'),(20,2,'2026-12-12','Ocupado'),(21,2,'2026-12-13','Ocupado'),(22,2,'2026-12-14','Ocupado'),(23,1,'2026-12-12','Ocupado'),(24,1,'2026-12-13','Ocupado'),(25,1,'2026-12-14','Ocupado'),(26,1,'2026-12-15','Ocupado'),(27,1,'2026-12-16','Ocupado'),(28,1,'2026-12-14','Ocupado'),(29,1,'2026-12-15','Ocupado'),(30,1,'2026-12-16','Ocupado'),(31,1,'2026-12-17','Ocupado'),(32,1,'2026-12-14','Ocupado'),(33,1,'2026-12-15','Ocupado'),(34,1,'2026-12-16','Ocupado'),(35,1,'2026-12-17','Ocupado'),(36,1,'2026-09-09','Ocupado'),(37,5,'2026-09-11','Ocupado'),(38,14,'2026-10-20','Ocupado'),(39,14,'2026-10-21','Ocupado'),(40,14,'2026-10-22','Ocupado'),(41,14,'2026-10-23','Ocupado'),(42,14,'2026-10-24','Ocupado'),(43,14,'2026-10-25','Ocupado'),(44,14,'2026-10-26','Ocupado');
/*!40000 ALTER TABLE `disponibilidad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historial_busqueda`
--

DROP TABLE IF EXISTS `historial_busqueda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_busqueda` (
  `ID_historial` int NOT NULL AUTO_INCREMENT,
  `ID_comprador_fk` int DEFAULT NULL,
  `filtros_aplicados` text,
  `fecha_busqueda` date DEFAULT NULL,
  PRIMARY KEY (`ID_historial`),
  KEY `ID_comprador_fk` (`ID_comprador_fk`),
  CONSTRAINT `historial_busqueda_ibfk_1` FOREIGN KEY (`ID_comprador_fk`) REFERENCES `comprador` (`ID_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historial_busqueda`
--

LOCK TABLES `historial_busqueda` WRITE;
/*!40000 ALTER TABLE `historial_busqueda` DISABLE KEYS */;
/*!40000 ALTER TABLE `historial_busqueda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `imagen_propiedad`
--

DROP TABLE IF EXISTS `imagen_propiedad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `imagen_propiedad` (
  `ID_imagen` int NOT NULL AUTO_INCREMENT,
  `ID_propiedad_fk` int NOT NULL,
  `url_imagen` varchar(255) NOT NULL,
  PRIMARY KEY (`ID_imagen`),
  KEY `idx_imagen_propiedad` (`ID_propiedad_fk`),
  CONSTRAINT `imagen_propiedad_ibfk_1` FOREIGN KEY (`ID_propiedad_fk`) REFERENCES `propiedad` (`ID_propiedad`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `imagen_propiedad`
--

LOCK TABLES `imagen_propiedad` WRITE;
/*!40000 ALTER TABLE `imagen_propiedad` DISABLE KEYS */;
/*!40000 ALTER TABLE `imagen_propiedad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lista_deseos`
--

DROP TABLE IF EXISTS `lista_deseos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lista_deseos` (
  `ID_lista` int NOT NULL AUTO_INCREMENT,
  `ID_comprador_fk` int DEFAULT NULL,
  `ID_propiedad_fk` int DEFAULT NULL,
  `fecha_agregado` date DEFAULT NULL,
  PRIMARY KEY (`ID_lista`),
  KEY `ID_comprador_fk` (`ID_comprador_fk`),
  KEY `ID_propiedad_fk` (`ID_propiedad_fk`),
  CONSTRAINT `lista_deseos_ibfk_1` FOREIGN KEY (`ID_comprador_fk`) REFERENCES `comprador` (`ID_usuario`),
  CONSTRAINT `lista_deseos_ibfk_2` FOREIGN KEY (`ID_propiedad_fk`) REFERENCES `propiedad` (`ID_propiedad`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lista_deseos`
--

LOCK TABLES `lista_deseos` WRITE;
/*!40000 ALTER TABLE `lista_deseos` DISABLE KEYS */;
INSERT INTO `lista_deseos` VALUES (27,7,1,'2026-10-14');
/*!40000 ALTER TABLE `lista_deseos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `propiedad`
--

DROP TABLE IF EXISTS `propiedad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `propiedad` (
  `ID_propiedad` int NOT NULL AUTO_INCREMENT,
  `ID_vendedor_fk` int DEFAULT NULL,
  `estadia_minima` int DEFAULT NULL,
  `calle` varchar(100) DEFAULT NULL,
  `altura` int DEFAULT NULL,
  `codigo_postal` varchar(10) DEFAULT NULL,
  `ciudad` varchar(50) DEFAULT NULL,
  `provincia` enum('Buenos Aires','Catamarca','Chaco','Chubut','Córdoba','Corrientes','Entre Ríos','Formosa','Jujuy','La Pampa','La Rioja','Mendoza','Misiones','Neuquén','Río Negro','Salta','San Juan','San Luis','Santa Cruz','Santa Fe','Santiago del Estero','Tierra del Fuego','Tucumán','CABA') NOT NULL,
  `pais` varchar(30) DEFAULT 'Argentina',
  `precio_por_noche` decimal(10,2) DEFAULT NULL,
  `metros_cuadrados` int DEFAULT NULL,
  `cant_personas` int DEFAULT NULL,
  `piso` varchar(50) DEFAULT NULL,
  `descripcion` text,
  `dias_cancelacion_sin_penalizacion` int DEFAULT NULL,
  `porcentaje_sena` int DEFAULT '50',
  `horas_limite_pago` int DEFAULT '24',
  `estado_verificacion` varchar(20) DEFAULT 'pendiente',
  `comprobante_titularidad` varchar(255) DEFAULT NULL,
  `foto_verificacion` varchar(255) DEFAULT NULL,
  `motivo_rechazo` varchar(255) DEFAULT NULL,
  `alias_cbu` varchar(100) DEFAULT 'alquinow.propietario.mp',
  PRIMARY KEY (`ID_propiedad`),
  KEY `ID_vendedor_fk` (`ID_vendedor_fk`),
  KEY `idx_propiedad_precio` (`precio_por_noche`),
  KEY `idx_propiedad_capacidad` (`cant_personas`),
  KEY `idx_propiedad_ubicacion` (`ciudad`,`provincia`),
  CONSTRAINT `propiedad_ibfk_1` FOREIGN KEY (`ID_vendedor_fk`) REFERENCES `vendedor` (`ID_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `propiedad`
--

LOCK TABLES `propiedad` WRITE;
/*!40000 ALTER TABLE `propiedad` DISABLE KEYS */;
INSERT INTO `propiedad` VALUES (1,NULL,NULL,'Av. Alem',1253,NULL,'Bahía Blanca','Buenos Aires','Argentina',25000.00,NULL,4,NULL,'Departamento de prueba para validar el sistema de reservas.',NULL,30,24,'aprobada',NULL,NULL,NULL,'alquinow.propietario.mp'),(2,2,NULL,'Martiniano Rodriguez ',137,NULL,'Bahia Blanca','Buenos Aires','Argentina',50.00,30,3,NULL,'Departamento nuevo en zona centro',NULL,30,24,'aprobada',NULL,NULL,NULL,'alquinow.propietario.mp'),(5,7,NULL,'Vieytes',542,NULL,'Bahia Blanca','Buenos Aires','Argentina',70.00,35,2,NULL,'Departamento amoblado nuevo',NULL,30,24,'aprobada',NULL,NULL,NULL,'alquinow.propietario.mp'),(6,7,NULL,'Sarmiento',422,NULL,'Santa rosa','La Pampa','Argentina',70000.00,50,4,NULL,'',NULL,30,24,'aprobada',NULL,NULL,NULL,'alquinow.propietario.mp'),(8,7,NULL,'Villarino',543,NULL,'Bariloche','Río Negro','Argentina',100000.00,40,3,NULL,'',NULL,30,24,'aprobada',NULL,NULL,NULL,'alquinow.propietario.mp'),(9,7,NULL,'Zelarrayan',2000,NULL,'Bahia Blanca','Buenos Aires','Argentina',300000.00,45,2,'Piso 3 - Dpto B','',NULL,30,24,'aprobada',NULL,NULL,NULL,'alquinow.propietario.mp'),(10,7,NULL,'25 de Mayo',430,NULL,'Cordoba','Córdoba','Argentina',90000.00,40,3,'','Nuevo',NULL,30,24,'aprobada','1789580874221_comp_Notas_sistemas.png','1789580874221_foto_prediccion-mundial-2026-grupos.png',NULL,'alquinow.propietario.mp'),(11,7,NULL,'17 de Agosto',1240,NULL,'Tigre','Buenos Aires','Argentina',100000.00,30,1,'Piso 5 - Dpto C','Facha',5,10,24,'aprobada','1789704914854_comp_Notas_sistemas.png','1789704914879_foto_IMG_20260423_165044993_HDR.jpg',NULL,'alquinow.propietario.mp'),(14,7,NULL,'SixSeven',1320,NULL,'Aura','La Rioja','Argentina',40000.00,30,1,'','Departamento con mucha aura',5,10,24,'aprobada','1790138204993_comp_Captura de pantalla 2024-11-03 164132.png','1790138205006_foto_Captura de pantalla 2024-11-05 184857.png',NULL,'alquinow.propietario.mp');
/*!40000 ALTER TABLE `propiedad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `propiedad_imagen`
--

DROP TABLE IF EXISTS `propiedad_imagen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `propiedad_imagen` (
  `id_imagen` int NOT NULL AUTO_INCREMENT,
  `id_propiedad_fk` int NOT NULL,
  `ruta_archivo` varchar(255) NOT NULL,
  `es_principal` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id_imagen`),
  KEY `id_propiedad_fk` (`id_propiedad_fk`),
  CONSTRAINT `propiedad_imagen_ibfk_1` FOREIGN KEY (`id_propiedad_fk`) REFERENCES `propiedad` (`ID_propiedad`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `propiedad_imagen`
--

LOCK TABLES `propiedad_imagen` WRITE;
/*!40000 ALTER TABLE `propiedad_imagen` DISABLE KEYS */;
INSERT INTO `propiedad_imagen` VALUES (4,14,'1790138205042_gal_1073f831c30904ac519337d8f68d4f24.webp',1),(5,14,'1790138205042_gal_H-UalQ6r-yuvHiGsnP0eGb8W6_gk77j_AjG9YYZmt-A.webp',0);
/*!40000 ALTER TABLE `propiedad_imagen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resena`
--

DROP TABLE IF EXISTS `resena`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resena` (
  `ID_resena` int NOT NULL AUTO_INCREMENT,
  `ID_comprador_fk` int DEFAULT NULL,
  `ID_propiedad_fk` int DEFAULT NULL,
  `puntuacion` int DEFAULT NULL,
  `comentario` text,
  `fecha` date DEFAULT NULL,
  `id_reserva_fk` int DEFAULT NULL,
  PRIMARY KEY (`ID_resena`),
  UNIQUE KEY `id_reserva_fk` (`id_reserva_fk`),
  KEY `ID_comprador_fk` (`ID_comprador_fk`),
  KEY `idx_resena_propiedad` (`ID_propiedad_fk`),
  CONSTRAINT `fk_resena_reserva` FOREIGN KEY (`id_reserva_fk`) REFERENCES `reserva` (`ID_reserva`),
  CONSTRAINT `resena_ibfk_1` FOREIGN KEY (`ID_comprador_fk`) REFERENCES `comprador` (`ID_usuario`),
  CONSTRAINT `resena_ibfk_2` FOREIGN KEY (`ID_propiedad_fk`) REFERENCES `propiedad` (`ID_propiedad`),
  CONSTRAINT `resena_chk_1` CHECK ((`puntuacion` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resena`
--

LOCK TABLES `resena` WRITE;
/*!40000 ALTER TABLE `resena` DISABLE KEYS */;
INSERT INTO `resena` VALUES (1,7,1,5,'Buena atención',NULL,10),(2,7,5,1,'malooooooo',NULL,11);
/*!40000 ALTER TABLE `resena` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reserva`
--

DROP TABLE IF EXISTS `reserva`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reserva` (
  `ID_reserva` int NOT NULL AUTO_INCREMENT,
  `ID_comprador_fk` int DEFAULT NULL,
  `ID_propiedad_fk` int DEFAULT NULL,
  `fecha_inicio` date DEFAULT NULL,
  `fecha_final` date DEFAULT NULL,
  `estado` varchar(20) DEFAULT NULL,
  `monto_total` decimal(10,2) DEFAULT NULL,
  `fecha_reserva` date DEFAULT NULL,
  `dias_cancelacion_aplicados` int DEFAULT NULL,
  `fecha_limite_cancelacion` date DEFAULT NULL,
  `fecha_limite_pago` datetime DEFAULT NULL,
  `monto_pagado` decimal(10,2) DEFAULT '0.00',
  `porcentaje_sena_aplicado` int DEFAULT '0',
  `metodo_pago` varchar(50) DEFAULT 'Transferencia',
  `comprobante_pago` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_reserva`),
  KEY `ID_comprador_fk` (`ID_comprador_fk`),
  KEY `idx_reserva_propiedad` (`ID_propiedad_fk`),
  KEY `idx_reserva_fechas` (`fecha_inicio`,`fecha_final`),
  CONSTRAINT `reserva_ibfk_1` FOREIGN KEY (`ID_comprador_fk`) REFERENCES `comprador` (`ID_usuario`),
  CONSTRAINT `reserva_ibfk_2` FOREIGN KEY (`ID_propiedad_fk`) REFERENCES `propiedad` (`ID_propiedad`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reserva`
--

LOCK TABLES `reserva` WRITE;
/*!40000 ALTER TABLE `reserva` DISABLE KEYS */;
INSERT INTO `reserva` VALUES (4,1,1,'2026-11-11','2026-11-16','pendiente',125000.00,'2026-08-22',NULL,NULL,NULL,0.00,0,'Transferencia',NULL),(5,1,1,'2026-12-01','2026-12-05','pendiente',100000.00,'2026-08-23',NULL,NULL,NULL,0.00,0,'Transferencia',NULL),(6,1,2,'2026-12-10','2026-12-15','pendiente',250.00,'2026-08-26',NULL,NULL,NULL,0.00,0,'Transferencia',NULL),(7,1,1,'2026-12-12','2026-12-17','pendiente',125000.00,'2026-08-26',NULL,NULL,NULL,0.00,0,'Transferencia',NULL),(8,1,1,'2026-12-14','2026-12-18','pendiente',100000.00,'2026-08-26',NULL,NULL,NULL,0.00,0,'Transferencia',NULL),(9,1,1,'2026-12-14','2026-12-18','pendiente',100000.00,'2026-08-26',NULL,NULL,NULL,0.00,0,'Transferencia',NULL),(10,7,1,'2026-09-09','2026-09-10','pendiente',25000.00,'2026-09-09',NULL,NULL,NULL,0.00,0,'Transferencia',NULL),(11,7,5,'2026-09-11','2026-07-10','pendiente',70.00,'2026-09-10',NULL,NULL,'2026-09-11 20:55:32',0.00,0,'Transferencia',NULL),(12,7,14,'2026-10-20','2026-10-27','pendiente_sena',280000.00,'2026-10-19',5,'2026-10-15','2026-10-20 21:06:05',0.00,10,'Transferencia',NULL);
/*!40000 ALTER TABLE `reserva` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sena`
--

DROP TABLE IF EXISTS `sena`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sena` (
  `ID_pago` int NOT NULL AUTO_INCREMENT,
  `ID_reserva_fk` int DEFAULT NULL,
  `monto` decimal(10,2) DEFAULT NULL,
  `fecha_pago` date DEFAULT NULL,
  `metodo_pago` varchar(50) DEFAULT NULL,
  `estado` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID_pago`),
  KEY `ID_reserva_fk` (`ID_reserva_fk`),
  CONSTRAINT `sena_ibfk_1` FOREIGN KEY (`ID_reserva_fk`) REFERENCES `reserva` (`ID_reserva`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sena`
--

LOCK TABLES `sena` WRITE;
/*!40000 ALTER TABLE `sena` DISABLE KEYS */;
/*!40000 ALTER TABLE `sena` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `ID_usuario` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) DEFAULT NULL,
  `contrasena` varchar(100) NOT NULL,
  `dni` varchar(20) DEFAULT NULL,
  `mail` varchar(100) DEFAULT NULL,
  `tel` varchar(20) DEFAULT NULL,
  `es_vendedor` tinyint(1) DEFAULT '0',
  `es_huesped` tinyint(1) DEFAULT '1',
  `cuenta_activa` tinyint(1) DEFAULT '0',
  `codigo_verificacion` varchar(6) DEFAULT NULL,
  PRIMARY KEY (`ID_usuario`),
  UNIQUE KEY `mail` (`mail`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,NULL,'$2a$12$kAwExYfPbRyJ8H4eCk2gNeO51dWm9V1QPV0vmediEzuuG5/RzzkBu','46920939','pepito123@gmail.com','2914146715',1,1,0,NULL),(2,NULL,'$2a$12$DEArvVrsMiYHN81M.GJJ6.cZ2d.gxWQ4lKmv9WvVmeeFh5yhQilre','46939969','vendedor@alquinow.com','2914126513',1,1,0,NULL),(7,'Demian','$2a$12$iO4yT1.mlUa3DR3A/HDwmOs2PP5jTGlxD55nF0uTDEosrqkEmErH.','46939920','demiancastro697@gmail.com','+5492914136815',1,1,1,NULL),(8,NULL,'$2a$12$9.4u07xnvw50BIeEbEdpPOSByQgXPvQMDCTEnHjDpkVTGaI4vyuuO','46939920','salasprueba1@gmail.com','+5492914136815',1,1,1,NULL);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendedor`
--

DROP TABLE IF EXISTS `vendedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendedor` (
  `ID_usuario` int NOT NULL,
  `cbu_cvu` varchar(50) DEFAULT NULL,
  `tipo_vendedor` varchar(50) DEFAULT NULL,
  `verificado` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`ID_usuario`),
  CONSTRAINT `vendedor_ibfk_1` FOREIGN KEY (`ID_usuario`) REFERENCES `usuario` (`ID_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendedor`
--

LOCK TABLES `vendedor` WRITE;
/*!40000 ALTER TABLE `vendedor` DISABLE KEYS */;
INSERT INTO `vendedor` VALUES (2,NULL,NULL,0),(7,NULL,NULL,0),(8,NULL,NULL,0);
/*!40000 ALTER TABLE `vendedor` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-20 11:46:43
