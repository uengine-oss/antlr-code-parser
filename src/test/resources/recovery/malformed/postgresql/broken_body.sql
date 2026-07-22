CREATE FUNCTION broken_body(value integer)
RETURNS integer
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN value + 1;
$$;
