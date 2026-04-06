-- ================================================================
-- V1__baseline.sql
-- Fix: hapus orphan constraints yang muncul sebagai WARN di log
-- Constraint ini sudah tidak ada di DB tapi masih direferensikan Hibernate
-- ================================================================

ALTER TABLE IF EXISTS patienten
    DROP CONSTRAINT IF EXISTS uk_oi0md8hnm2gl6lmada4rx0xh6;

ALTER TABLE IF EXISTS users
    DROP CONSTRAINT IF EXISTS uk_6dotkott2kjsp8vw4d0m25fb7;

ALTER TABLE IF EXISTS users
    DROP CONSTRAINT IF EXISTS uk_r43af9ap4edm43mmtq01oddj6;
