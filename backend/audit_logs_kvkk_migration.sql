-- KVKK/GDPR Uyumlu Audit Log Migration
-- Remove exact location data (cityName column) and replace with region-level data

-- Add new regionName column for KVKK/GDPR compliance
ALTER TABLE audit_logs ADD COLUMN region_name VARCHAR(100);

-- Update existing city data to region level (anonymization)
UPDATE audit_logs SET region_name = 
    CASE 
        WHEN country_code = 'TR' AND city_name LIKE 'Istanbul%' THEN 'Marmara'
        WHEN country_code = 'TR' AND city_name LIKE 'Ankara%' THEN 'İç Anadolu'
        WHEN country_code = 'TR' AND city_name LIKE 'İzmir%' THEN 'Ege'
        WHEN country_code = 'TR' AND city_name LIKE 'Antalya%' THEN 'Akdeniz'
        WHEN country_code = 'TR' AND city_name LIKE 'Bursa%' THEN 'Marmara'
        WHEN country_code = 'TR' THEN 'Turkey Region'
        WHEN country_code = 'US' THEN 'United States Region'
        WHEN country_code = 'DE' THEN 'Germany Region'
        WHEN country_code = 'GB' THEN 'United Kingdom Region'
        WHEN country_code = 'LOCAL' THEN 'Development Environment'
        ELSE CONCAT(country_code, ' Region')
    END
WHERE city_name IS NOT NULL;

-- Drop the city_name column (contains exact location data)
ALTER TABLE audit_logs DROP COLUMN city_name;

-- Add index for new regionName column
CREATE INDEX idx_audit_region ON audit_logs(region_name);

-- Add compliance comment
COMMENT ON COLUMN audit_logs.region_name IS 'KVKK/GDPR Compliant: Region-level location data only, no exact city coordinates';

-- KVKK/GDPR Uyumlu Audit Log Migration
-- Remove exact location data (cityName column) and replace with region-level data

-- Add new regionName column for KVKK/GDPR compliance
ALTER TABLE audit_logs ADD COLUMN region_name VARCHAR(100);

-- Update existing city data to region level (anonymization)
UPDATE audit_logs SET region_name = 
    CASE 
        WHEN country_code = 'TR' AND city_name LIKE 'Istanbul%' THEN 'Marmara'
        WHEN country_code = 'TR' AND city_name LIKE 'Ankara%' THEN 'İç Anadolu'
        WHEN country_code = 'TR' AND city_name LIKE 'İzmir%' THEN 'Ege'
        WHEN country_code = 'TR' AND city_name LIKE 'Antalya%' THEN 'Akdeniz'
        WHEN country_code = 'TR' AND city_name LIKE 'Bursa%' THEN 'Marmara'
        WHEN country_code = 'TR' THEN 'Turkey Region'
        WHEN country_code = 'US' THEN 'United States Region'
        WHEN country_code = 'DE' THEN 'Germany Region'
        WHEN country_code = 'GB' THEN 'United Kingdom Region'
        WHEN country_code = 'LOCAL' THEN 'Development Environment'
        ELSE CONCAT(country_code, ' Region')
    END
WHERE city_name IS NOT NULL;

-- Drop the city_name column (contains exact location data)
ALTER TABLE audit_logs DROP COLUMN city_name;

-- Add index for new regionName column
CREATE INDEX idx_audit_region ON audit_logs(region_name);

-- Add compliance comment
COMMENT ON COLUMN audit_logs.region_name IS 'KVKK/GDPR Compliant: Region-level location data only, no exact city coordinates';