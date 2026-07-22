CREATE OR REPLACE PROCEDURE broken_proc AS
BEGIN
    UPDATE sample_tableSET value = 1;
END;
/
