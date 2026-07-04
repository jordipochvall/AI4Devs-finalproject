-- =============================================================================
-- V12__recalibrate_seed_configs.sql — HU-31: corrige el RTP fuera de rango (~3000%).
-- Las 3 configs semilla estaban sin calibrar. `game_configs` es append-only (trigger de
-- inmutabilidad V2), así que NO se puede UPDATE: se inserta una **versión 2 calibrada** y se
-- activa. Sólo actúa sobre BBDD ya sembradas (existe una v1); en instalaciones nuevas es no-op
-- (SeedDataLoader inserta directamente la v1 calibrada desde /seed/*.json).
-- =============================================================================

-- --- Egyptian (target RTP 0.95) ---
INSERT INTO game_configs (game_id, version, config, rtp_target, volatility_target, created_by_user_id, notes)
SELECT g.id, 2,
'{"grid":{"cols":5,"rows":3},"symbols":[{"id":"WILD","kind":"WILD"},{"id":"SCATTER","kind":"SCATTER"},{"id":"ANUBIS","kind":"REGULAR"},{"id":"SCARAB","kind":"REGULAR"},{"id":"A","kind":"REGULAR"}],"reels":[["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"],["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"],["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"],["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"],["A","SCARAB","A","ANUBIS","A","SCARAB","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","WILD","A","SCARAB","A","SCATTER","A","SCARAB","ANUBIS","A","SCARAB","A","A","A"]],"paylines":[[1,1,1,1,1],[0,0,0,0,0],[2,2,2,2,2],[0,1,2,1,0],[2,1,0,1,2]],"paytable":[{"symbol":"ANUBIS","payouts":{"3":5,"4":20,"5":80}},{"symbol":"SCARAB","payouts":{"3":2,"4":6,"5":20}},{"symbol":"A","payouts":{"4":1,"5":3}}],"scatterPays":{"SCATTER":{"3":2,"4":8,"5":40}},"bonus":{"wild":{"substitutes":["REGULAR"]},"freeSpins":{"triggerSymbol":"SCATTER","minTriggerCount":3,"award":{"3":5,"4":8,"5":12},"multiplier":2,"retrigger":false}}}'::jsonb,
       0.9500, 8.50, c.created_by_user_id, 'Recalibrated RTP (HU-31)'
FROM   games g JOIN game_configs c ON c.game_id = g.id AND c.version = 1
WHERE  g.code = 'egyptian-5x3';

-- --- Fruits (target RTP 0.92) ---
INSERT INTO game_configs (game_id, version, config, rtp_target, volatility_target, created_by_user_id, notes)
SELECT g.id, 2,
'{"grid":{"cols":3,"rows":3},"symbols":[{"id":"SEVEN","kind":"REGULAR"},{"id":"BAR3","kind":"REGULAR"},{"id":"BAR2","kind":"REGULAR"},{"id":"BAR","kind":"REGULAR"},{"id":"CHERRY","kind":"REGULAR"},{"id":"LEMON","kind":"REGULAR"},{"id":"ORANGE","kind":"REGULAR"},{"id":"PLUM","kind":"REGULAR"}],"reels":[["SEVEN","BAR3","BAR2","CHERRY","BAR","LEMON","ORANGE","PLUM","BAR2","CHERRY","BAR","LEMON","ORANGE","PLUM","BAR2"],["CHERRY","PLUM","LEMON","ORANGE","BAR","BAR2","BAR3","SEVEN","CHERRY","PLUM","LEMON","ORANGE","BAR","BAR2","BAR3"],["LEMON","ORANGE","CHERRY","PLUM","BAR","BAR2","BAR3","SEVEN","LEMON","ORANGE","CHERRY","PLUM","BAR","BAR2","SEVEN"]],"paylines":[[1,1,1],[0,0,0],[2,2,2],[0,1,2],[2,1,0]],"paytable":[{"symbol":"SEVEN","payouts":{"3":280}},{"symbol":"BAR3","payouts":{"3":140}},{"symbol":"BAR2","payouts":{"3":70}},{"symbol":"BAR","payouts":{"2":3,"3":28}},{"symbol":"CHERRY","payouts":{"2":6,"3":14}},{"symbol":"LEMON","payouts":{"2":3,"3":8}},{"symbol":"ORANGE","payouts":{"2":3,"3":8}},{"symbol":"PLUM","payouts":{"2":3,"3":8}}],"bonus":{}}'::jsonb,
       0.9200, 3.00, c.created_by_user_id, 'Recalibrated RTP (HU-31)'
FROM   games g JOIN game_configs c ON c.game_id = g.id AND c.version = 1
WHERE  g.code = 'fruits-3x3';

-- --- Space (target RTP 0.965) ---
INSERT INTO game_configs (game_id, version, config, rtp_target, volatility_target, created_by_user_id, notes)
SELECT g.id, 2,
'{"grid":{"cols":5,"rows":3},"symbols":[{"id":"WILD","kind":"WILD"},{"id":"SCATTER","kind":"SCATTER"},{"id":"PLANET","kind":"REGULAR"},{"id":"COMET","kind":"REGULAR"},{"id":"STAR","kind":"REGULAR"},{"id":"K","kind":"REGULAR"},{"id":"A","kind":"REGULAR"}],"reels":[["A","K","A","STAR","A","K","COMET","A","K","STAR","A","SCATTER","A","K","STAR","COMET","A","K","A","STAR","PLANET","A","K","STAR","A","COMET","A","K","SCATTER","WILD"],["A","K","A","STAR","A","K","COMET","A","K","STAR","A","SCATTER","A","K","STAR","COMET","A","K","A","STAR","PLANET","A","K","STAR","A","COMET","A","K","SCATTER","WILD"],["A","K","A","STAR","A","K","COMET","A","K","STAR","A","SCATTER","A","K","STAR","COMET","A","K","A","STAR","PLANET","A","K","STAR","A","COMET","A","K","SCATTER","WILD"],["A","K","A","STAR","A","K","COMET","A","K","STAR","A","SCATTER","A","K","STAR","COMET","A","K","A","STAR","PLANET","A","K","STAR","A","COMET","A","K","SCATTER","WILD"],["A","K","A","STAR","A","K","COMET","A","K","STAR","A","SCATTER","A","K","STAR","COMET","A","K","A","STAR","PLANET","A","K","STAR","A","COMET","A","K","SCATTER","WILD"]],"paylines":[[1,1,1,1,1],[0,0,0,0,0],[2,2,2,2,2],[0,1,2,1,0],[2,1,0,1,2],[0,0,1,2,2],[2,2,1,0,0],[1,0,0,0,1],[1,2,2,2,1],[0,1,0,1,0]],"paytable":[{"symbol":"PLANET","payouts":{"3":20,"4":62,"5":205}},{"symbol":"COMET","payouts":{"3":10,"4":31,"5":103}},{"symbol":"STAR","payouts":{"3":4,"4":13,"5":41}},{"symbol":"K","payouts":{"3":2,"4":6,"5":20}},{"symbol":"A","payouts":{"4":2,"5":4}}],"scatterPays":{"SCATTER":{"3":4,"4":16,"5":82}},"bonus":{"wild":{"substitutes":["REGULAR"]},"freeSpins":{"triggerSymbol":"SCATTER","minTriggerCount":3,"award":{"3":5,"4":8,"5":12},"multiplier":2,"retrigger":false}}}'::jsonb,
       0.9650, 12.00, c.created_by_user_id, 'Recalibrated RTP (HU-31)'
FROM   games g JOIN game_configs c ON c.game_id = g.id AND c.version = 1
WHERE  g.code = 'space-5x3';

-- Activate the recalibrated version where it was inserted (existing DBs only).
UPDATE games
SET    active_config_id = (SELECT id FROM game_configs WHERE game_id = games.id AND version = 2)
WHERE  code IN ('egyptian-5x3', 'fruits-3x3', 'space-5x3')
  AND  EXISTS (SELECT 1 FROM game_configs WHERE game_id = games.id AND version = 2);
