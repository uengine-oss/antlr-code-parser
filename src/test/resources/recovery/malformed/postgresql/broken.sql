CREATE FUNCTION broken_fn(value integer
RETURNS integer
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN value + 1;
$$;
