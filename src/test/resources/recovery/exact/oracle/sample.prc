CREATE OR REPLACE PROCEDURE sample_proc(p_id IN NUMBER) AS
BEGIN
    UPDATE sample_table
       SET value = value + 1
     WHERE id = p_id;
END;
/
