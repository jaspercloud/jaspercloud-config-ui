/*
 Navicat Premium Data Transfer

 Source Server         : 172.168.1.17_5432
 Source Server Type    : PostgreSQL
 Source Server Version : 90609
 Source Host           : 172.168.1.17:5432
 Source Catalog        : spring_config
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 90609
 File Encoding         : 65001

 Date: 14/11/2018 13:18:28
*/


-- ----------------------------
-- Sequence structure for seq_config_history
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."seq_config_history";
CREATE SEQUENCE "public"."seq_config_history" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for seq_service_config
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."seq_service_config";
CREATE SEQUENCE "public"."seq_service_config" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for config_history
-- ----------------------------
DROP TABLE IF EXISTS "public"."config_history";
CREATE TABLE "public"."config_history" (
  "id" int8 NOT NULL DEFAULT nextval('seq_config_history'::regclass),
  "application" varchar(255) COLLATE "pg_catalog"."default",
  "namespace" varchar(255) COLLATE "pg_catalog"."default",
  "config" text COLLATE "pg_catalog"."default",
  "ver" int4 DEFAULT 0,
  "create_time" timestamp(6) DEFAULT now()
)
;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."config_info";
CREATE TABLE "public"."config_info" (
  "id" int8 NOT NULL DEFAULT nextval('seq_service_config'::regclass),
  "application" varchar(255) COLLATE "pg_catalog"."default",
  "namespace" varchar(255) COLLATE "pg_catalog"."default",
  "config" text COLLATE "pg_catalog"."default",
  "ver" int4 DEFAULT 0
)
;

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."seq_config_history"', 20, true);
SELECT setval('"public"."seq_service_config"', 44, true);

-- ----------------------------
-- Primary Key structure for table config_history
-- ----------------------------
ALTER TABLE "public"."config_history" ADD CONSTRAINT "config_history_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table config_info
-- ----------------------------
ALTER TABLE "public"."config_info" ADD CONSTRAINT "service_config_application_namespace_key" UNIQUE ("application", "namespace");
