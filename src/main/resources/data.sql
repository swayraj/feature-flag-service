  INSERT INTO api_keys (key_value, owner_name, active)
  SELECT 'test-key-123', 'dev', true
  WHERE NOT EXISTS (
      SELECT 1 FROM api_keys WHERE key_value = 'test-key-123'
  );