-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: book_cycle
-- ------------------------------------------------------
-- Server version	8.0.40

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
-- Table structure for table `order_info`
--

DROP TABLE IF EXISTS `order_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyer_id` int NOT NULL,
  `seller_id` int NOT NULL,
  `book_id` int NOT NULL,
  `book_title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `status` tinyint DEFAULT '0',
  `address_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address_detail` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_status` (`status`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `order_info_ibfk_1` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_info_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_info_ibfk_3` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_info`
--

LOCK TABLES `order_info` WRITE;
/*!40000 ALTER TABLE `order_info` DISABLE KEYS */;
INSERT INTO `order_info` VALUES (1,'ORDER1781417347780b9fb2cd8',1,1,1,'高等数学（第七版）上册 同济大学',18.00,4,'y','18888888888','中南财经政法大学','2026-06-14 14:09:07'),(2,'ORDER17814173477922afcf04d',1,2,2,'高等数学（第七版）下册',15.00,4,'y','18888888888','中南财经政法大学','2026-06-14 14:09:07'),(3,'ORDER17814199157297cbe60f5',1,1,1,'高等数学（第七版）上册 同济大学',18.00,4,'y','18888888888','中南财经政法大学','2026-06-14 14:51:55'),(4,'ORDER178141991573802710f75',1,1,60,'活着 余华',12.00,1,'y','18888888888','中南财经政法大学','2026-06-14 14:51:55'),(5,'ORDER1781419915741ad6d3b25',1,1,82,'三体（全三册）',45.00,1,'y','18888888888','中南财经政法大学','2026-06-14 14:51:55'),(6,'ORDER1781424155018b665ed0e',1,1,1,'高等数学（第七版）上册 同济大学',18.00,1,'y','18888888888','中南财经政法大学','2026-06-14 16:02:35'),(7,'ORDER178142466190001af4a50',6,2,2,'高等数学（第七版）下册',15.00,1,'xiaoy','15555555555','中南财经政法大学','2026-06-14 16:11:01'),(8,'ORDER1781424873509939a4157',1,1,4,'概率论与数理统计（第四版）',14.00,4,'y','18888888888','中南财经政法大学','2026-06-14 16:14:33'),(9,'ORDER1781425148938f8632c0b',1,1,1,'高等数学（第七版）上册 同济大学',18.00,1,'y','18888888888','中南财经政法大学','2026-06-14 16:19:08'),(10,'ORDER17814263541283178085e',1,1,66,'托福官方指南（第6版）',65.00,1,'y','18888888888','中南财经政法大学','2026-06-14 16:39:14'),(11,'ORDER1781426836703ebaa6c7b',1,1,1,'高等数学（第七版）上册 同济大学',18.00,1,'y','18888888888','中南财经政法大学','2026-06-14 16:47:16'),(12,'ORDER1781426857316403077e1',1,1,1,'高等数学（第七版）上册 同济大学',18.00,1,'y','18888888888','中南财经政法大学','2026-06-14 16:47:37'),(13,'ORDER1781426902606f4b7d00c',1,2,2,'高等数学（第七版）下册',15.00,1,'y','18888888888','中南财经政法大学','2026-06-14 16:48:22'),(14,'ORDER17814270360668249882c',1,1,3,'线性代数（第六版）',12.00,1,'y','18888888888','中南财经政法大学','2026-06-14 16:50:36'),(15,'ORDER1781428062903914b47ad',1,1,60,'活着 余华',12.00,1,'y','18888888888','中南财经政法大学','2026-06-14 17:07:42'),(16,'ORDER17814431091962fde950f',1,1,7,'大学语文（第四版）',14.00,1,'y','18888888888','中南财经政法大学','2026-06-14 21:18:29'),(17,'ORDER178144316280909b53393',1,2,2,'高等数学（第七版）下册',15.00,1,'y','18888888888','中南财经政法大学','2026-06-14 21:19:22'),(18,'ORDER17814432043213b5c7690',1,2,2,'高等数学（第七版）下册',15.00,4,'y','18888888888','中南财经政法大学','2026-06-14 21:20:04'),(19,'ORDER1781443217258d3a95ab9',1,2,2,'高等数学（第七版）下册',15.00,1,'y','18888888888','中南财经政法大学','2026-06-14 21:20:17'),(20,'ORDER17814463268638bb1bc3b',1,1,1,'高等数学（第七版）上册 同济大学',18.00,1,'y','18888888888','中南财经政法大学','2026-06-14 22:12:06'),(21,'ORDER178144943312922fadfe6',1,1,85,'高等数学',20.00,1,'y','18888888888','中南财经政法大学','2026-06-14 23:03:53'),(22,'ORDER1781500282611868bd8be',1,1,11,'标准日本语（初级上）',15.00,1,'y','18888888888','中南财经政法大学','2026-06-15 13:11:22'),(23,'ORDER17815003126399e1d7663',1,2,10,'考研英语词汇红宝书',25.00,1,'y','18888888888','中南财经政法大学','2026-06-15 13:11:52'),(24,'ORDER17815004436678ce620db',1,1,6,'大学化学（第二版）',15.00,3,'y','18888888888','中南财经政法大学','2026-06-15 13:14:03'),(25,'ORDER17815144754524b9647b7',1,1,90,'东西',10.00,3,'y','18888888888','中南财经政法大学','2026-06-15 17:07:55'),(26,'ORDER1781514715062353d0f74',1,1,82,'三体（全三册）',45.00,1,'y','18888888888','中南财经政法大学','2026-06-15 17:11:55'),(27,'ORDER178151495549331be13bb',1,1,84,'红楼梦（人民文学版）',28.00,3,'y','18888888888','中南财经政法大学','2026-06-15 17:15:55'),(28,'ORDER1781515108995199bea86',1,1,1020,'10',1.00,1,'y','18888888888','中南财经政法大学','2026-06-15 17:18:29'),(29,'ORDER17815154376410b599cf7',1,1,89,'大学',10.00,3,'y','18888888888','中南财经政法大学','2026-06-15 17:23:57'),(30,'ORDER178151582827565769a86',1,1,1017,'测试',1.00,3,'y','18888888888','中南财经政法大学','2026-06-15 17:30:28'),(31,'ORDER17815162402480ac94de2',1,1,18,'机器学习 周志华',45.00,1,'y','18888888888','中南财经政法大学','2026-06-15 17:37:20'),(32,'ORDER1781517695707fe0c62e1',1,1,1008,'测试',1.00,1,'y','18888888888','中南财经政法大学','2026-06-15 18:01:35'),(33,'ORDER1781525321234e6526db3',6,6,1030,'测试y',1.00,3,'xiaoy','15555555555','中南财经政法大学','2026-06-15 20:08:41');
/*!40000 ALTER TABLE `order_info` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-15 20:10:11
