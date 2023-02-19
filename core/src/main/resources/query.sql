-- create teams
CREATE TABLE IF NOT EXISTS teams ( id VARCHAR(20) NOT NULL , display_name VARCHAR(30) NOT NULL , color_code VARCHAR(20) NOT NULL , point INT NOT NULL DEFAULT '0', owner VARCHAR(255) NULL , PRIMARY KEY (id)) ENGINE = InnoDB;

-- ##new_query
-- teams add default team
INSERT INTO teams(id, display_name, color_code, owner) VALUES ('none', 'None', '&e', null) ON DUPLICATE KEY UPDATE id = VALUES(id), display_name = VALUES(display_name), color_code = VALUES(color_code), owner = VALUES(owner);

-- ##new_query
-- teams getAllTeams
CREATE PROCEDURE IF NOT EXISTS getAllTeams() BEGIN SELECT * FROM teams; END

-- ##new_query
-- teams getTeam
CREATE PROCEDURE IF NOT EXISTS getTeam(IN team VARCHAR(20)) BEGIN SELECT id, display_name, color_code FROM teams WHERE teams.id=team; END

-- ##new_query
-- teams getTeamPlayers
CREATE PROCEDURE IF NOT EXISTS getTeamPlayers(IN team VARCHAR(20)) BEGIN SELECT uuid, name FROM players WHERE players.team=team; END

-- ##new_query
-- teams getTeamOwner
CREATE PROCEDURE IF NOT EXISTS getTeamOwner(IN team VARCHAR(20)) BEGIN SELECT id, display_name, color_code FROM teams WHERE teams.id=team; END

-- ##new_query
-- teams deleteTeam
CREATE PROCEDURE IF NOT EXISTS deleteTeam(IN team VARCHAR(20)) BEGIN DELETE FROM teams WHERE teams.id=team; END

-- ##new_query
-- teams addTeam
CREATE PROCEDURE IF NOT EXISTS addTeam(IN id VARCHAR(20), IN name VARCHAR(30), IN color VARCHAR(20)) BEGIN INSERT INTO teams(id, display_name, color_code) VALUES (id,name,color); CALL getTeam(id); END

-- ##new_query
-- teams updateTeam
CREATE PROCEDURE IF NOT EXISTS updateTeam(IN team VARCHAR(20), IN name VARCHAR(30), IN color VARCHAR(20)) BEGIN UPDATE teams SET display_name=name,color_code=color WHERE id=team; CALL getTeam(team); END

-- ##new_query
-- teams getTeamPoint
CREATE PROCEDURE IF NOT EXISTS getTeamPoint(IN team VARCHAR(20)) BEGIN SELECT point FROM teams WHERE teams.id=team; END

-- ##new_query
-- teams updateTeamPoint
CREATE PROCEDURE IF NOT EXISTS updateTeamPoint(IN team VARCHAR(20), IN point INT) BEGIN UPDATE teams SET teams.point=teams.point+point WHERE id=team; INSERT INTO teams_point_history(value, team) VALUES (point,team); CALL getTeamPoint(team); END

-- ##new_query
-- create teams_point_history
CREATE TABLE IF NOT EXISTS `s2_skygamestheapi`.`teams_point_history` ( `date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP , `value` INT NOT NULL , `team` VARCHAR(20) NOT NULL , FOREIGN KEY (team) REFERENCES teams(id)) ENGINE = InnoDB;

-- ##new_query
-- teams_point_history getTeamPointHistory
CREATE PROCEDURE IF NOT EXISTS getTeamPointHistory(IN team VARCHAR(20)) BEGIN SELECT date, value FROM teams_point_history WHERE teams_point_history.team=team; END

-- ##new_query
-- create players
CREATE TABLE IF NOT EXISTS `s2_skygamestheapi`.`players` ( `uuid` VARCHAR(50) NOT NULL , `name` VARCHAR(50) NOT NULL , `first_login` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP , `last_login` DATETIME NULL , `team` VARCHAR(20) NOT NULL DEFAULT 'none', PRIMARY KEY (`uuid`) , FOREIGN KEY (team) REFERENCES teams(id)) ENGINE = InnoDB;

-- ##new_query
-- players getAllPlayers
CREATE PROCEDURE IF NOT EXISTS getAllPlayers() BEGIN SELECT * FROM players; END

-- ##new_query
-- players addPlayer
CREATE PROCEDURE IF NOT EXISTS addPlayer(IN uuid VARCHAR(50), IN name VARCHAR(50)) BEGIN INSERT INTO `players` (`uuid`, `name`, `first_login`, `last_login`) VALUES (uuid, name, current_timestamp(), current_timestamp()) ; CALL getPlayer(uuid); END

-- ##new_query
-- players getPlayer
CREATE PROCEDURE IF NOT EXISTS getPlayer(IN uuid VARCHAR(50)) BEGIN SELECT * FROM players WHERE players.uuid=uuid; END

-- ##new_query
-- players deletePlayer
CREATE PROCEDURE IF NOT EXISTS deletePlayer(IN uuid VARCHAR(50)) BEGIN DELETE FROM players WHERE players.uuid=uuid; END

-- ##new_query
-- players updatePlayer
CREATE PROCEDURE IF NOT EXISTS updatePlayer(IN uuid VARCHAR(50), IN name VARCHAR(50), IN date DATETIME) BEGIN UPDATE players SET players.name=name,last_login=date WHERE players.uuid=uuid; CALL getPlayer(uuid); END

-- ##new_query
-- players getPlayerTeam
CREATE PROCEDURE IF NOT EXISTS getPlayerTeam(IN uuid VARCHAR(50)) BEGIN SELECT team FROM teams JOIN players ON teams.id=players.team WHERE players.uuid=uuid; END

-- ##new_query
-- players updatePlayer
CREATE PROCEDURE IF NOT EXISTS updatePlayerTeam(IN uuid VARCHAR(50), IN team VARCHAR(20)) BEGIN UPDATE players SET players.team=team WHERE players.uuid=uuid; CALL getPlayer(uuid); END

-- ##new_query
-- create discord_tokens
CREATE TABLE IF NOT EXISTS `s2_skygamestheapi`.`discord_tokens` ( `uuid` VARCHAR(50) NOT NULL , `token` VARCHAR(50) NOT NULL, `discord_id` VARCHAR(50) NULL, PRIMARY KEY (`uuid`), FOREIGN KEY (uuid) REFERENCES players(uuid)) ENGINE = InnoDB;

-- ##new_query
-- discord_tokens addToken
CREATE PROCEDURE IF NOT EXISTS addToken(IN uuid VARCHAR(50), IN token VARCHAR(50)) BEGIN INSERT INTO `discord_tokens` (`uuid`, `token`) VALUES (uuid, token) ON DUPLICATE KEY UPDATE token = VALUES(token); END

-- ##new_query
-- discord_tokens getTokenFromUUID
CREATE PROCEDURE IF NOT EXISTS getTokenFromUUID(IN uuid VARCHAR(50)) BEGIN SELECT * FROM discord_tokens WHERE discord_tokens.uuid=uuid; END

-- ##new_query
-- discord_tokens getToken
CREATE PROCEDURE IF NOT EXISTS getToken(IN token VARCHAR(50)) BEGIN SELECT * FROM discord_tokens WHERE discord_tokens.token=token; END

-- ##new_query
-- discord_tokens deleteToken
CREATE PROCEDURE IF NOT EXISTS deleteToken(IN uuid VARCHAR(50)) BEGIN DELETE FROM discord_tokens WHERE discord_tokens.uuid=uuid; END

-- ##new_query
-- discord_tokens addDiscordID
CREATE PROCEDURE IF NOT EXISTS addDiscordID(IN token VARCHAR(50), IN id VARCHAR(50)) BEGIN UPDATE discord_tokens SET discord_tokens.discord_id=id WHERE discord_tokens.token=token; END

-- ##new_query
-- discord_tokens getIDFromUUID
CREATE PROCEDURE IF NOT EXISTS getIDFromUUID(IN uuid VARCHAR(50)) BEGIN SELECT * FROM discord_tokens WHERE discord_tokens.uuid=uuid; END

-- ##new_query
-- discord_tokens setIDFromToken
CREATE PROCEDURE IF NOT EXISTS setIDFromToken(IN token VARCHAR(50), IN id VARCHAR(50)) BEGIN UPDATE discord_tokens SET discord_tokens.discord_id=id WHERE discord_tokens.token=token; CALL getToken(token); END


