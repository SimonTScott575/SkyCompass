CREATE TABLE IF NOT EXISTS TimeZones (
    id TEXT NOT NULL PRIMARY KEY,
    raw_offset INTEGER(8),
    dst_offset INTEGER(8)
);