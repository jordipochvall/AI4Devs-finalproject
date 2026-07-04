-- =============================================================================
-- V13__retune_egyptian_rtp.sql — HU-31 (ajuste fino): "Tesoro del Nilo" (egyptian) salía a ~98,5%,
-- por encima de su objetivo (95%). Se baja el pago de SCARAB (4-de-una: 6 -> 4), dejando el RTP
-- empírico en ~95,5%. `game_configs` es append-only: se inserta una **v3 calibrada** y se activa.
-- Sólo actúa donde ya existe la v2 (BBDD sembradas antes); en instalaciones nuevas es no-op
-- (SeedDataLoader inserta la v1 ya calibrada desde /seed/egyptian.json).
-- =============================================================================

INSERT INTO game_configs (game_id, version, config, rtp_target, volatility_target, created_by_user_id, notes)
SELECT g.id, 3,
'{"grid":{"cols":5,"rows":3},"symbols":[{"id":"WILD","kind":"WILD"},{"id":"SCATTER","kind":"SCATTER"},{"id":"ANUBIS","kind":"REGULAR"},{"id":"SCARAB","kind":"REGULAR"},{"id":"A","kind":"REGULAR"}],"reels":[["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"],["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"],["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"],["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"],["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"]],"paylines":[[1,1,1,1,1],[0,0,0,0,0],[2,2,2,2,2],[0,1,2,1,0],[2,1,0,1,2]],"paytable":[{"symbol":"ANUBIS","payouts":{"3":5,"4":20,"5":80}},{"symbol":"SCARAB","payouts":{"3":2,"4":4,"5":20}},{"symbol":"A","payouts":{"4":1,"5":3}}],"scatterPays":{"SCATTER":{"3":2,"4":8,"5":40}},"bonus":{"wild":{"substitutes":["REGULAR"]},"freeSpins":{"triggerSymbol":"SCATTER","minTriggerCount":3,"award":{"3":5,"4":8,"5":12},"multiplier":2,"retrigger":false}}}'::jsonb,
       0.9500, 8.50, c.created_by_user_id, 'Retuned RTP to ~95.5% (HU-31)'
FROM   games g JOIN game_configs c ON c.game_id = g.id AND c.version = 2
WHERE  g.code = 'egyptian-5x3';

UPDATE games
SET    active_config_id = (SELECT id FROM game_configs WHERE game_id = games.id AND version = 3)
WHERE  code = 'egyptian-5x3'
  AND  EXISTS (SELECT 1 FROM game_configs WHERE game_id = games.id AND version = 3);
