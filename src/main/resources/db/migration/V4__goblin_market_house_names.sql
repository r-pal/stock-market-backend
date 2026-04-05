-- House names from Christina Rossetti's "Goblin Market" (original seed ids 1–6).
-- Portal passwords are fruits from the poem; set at runtime via GoblinMarketPortalSeedInitializer (BCrypt).

UPDATE house_account SET house_name = 'Laura' WHERE id = 1;
UPDATE house_account SET house_name = 'Lizzie' WHERE id = 2;
UPDATE house_account SET house_name = 'Jeanie' WHERE id = 3;
UPDATE house_account SET house_name = 'Golden Head' WHERE id = 4;
UPDATE house_account SET house_name = 'Brookside' WHERE id = 5;
UPDATE house_account SET house_name = 'Moonlight' WHERE id = 6;
