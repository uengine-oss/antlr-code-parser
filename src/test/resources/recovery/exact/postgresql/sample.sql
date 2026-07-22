CREATE OR REPLACE FUNCTION sample_fn(p_id integer)
RETURNS integer
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN p_id + 1;
END;
$$;
