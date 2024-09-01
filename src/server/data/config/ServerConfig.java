package server.data.config;

import server.StarMadeUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public enum ServerConfig {
	WORLD("World Name", "Sets the name of the world to use. If the specified world doesn't exist, it will be created automatically.", String.class, "world"),
	PROTECT_STARTING_SECTOR("Protect Starting Sector", "If enabled, the starting sector will be protected from combat and damage.", Boolean.class, false),
	ENABLE_SIMULATION("Enable Simulation", "Enables Universe AI simulation.", Boolean.class, true),
	CONCURRENT_SIMULATION("Concurrent Simulation", "How many simulation groups may be in the universe simultaneously. Impacts server performance.", Boolean.class, true),
	ENEMY_SPAWNING("Enemy Spawning", "Enables enemy spawning in the universe.", Boolean.class, true),
	SECTOR_SIZE("Sector Size", "Sets the size of sectors in the universe. Warning: Scaling sectors down in an existing universe may cause issues.", Integer.class, 1000, 100, 10000),
	BLUEPRINT_DEFAULT_PRIVATE("Default Blueprint Privacy", "Sets the default privacy of blueprints created by players.", Boolean.class, true),
	FLOATING_ITEM_LIFETIME_SECS("Floating Item Lifetime", "Sets the lifetime of floating items in seconds.", Integer.class, 240, 1, 604800),
	SIMULATION_SPAWN_DELAY("Simulation Spawn Delay", "How much seconds between simulation spawn ticks.", Integer.class, 420, 1, 604800),
	SIMULATION_TRADING_FILLS_SHOPS("Simulation Trading Fills Shops", "If enabled, the Trading Guild will restock shops.", Boolean.class, true),
	SECTOR_INACTIVE_TIMEOUT("Sector Inactive Timeout", "How many seconds until a sector is considered inactive and unloaded from memory. Set to -1 to disable (not recommended).", Integer.class, 20, -1, 604800),
	USE_STARMADE_AUTHENTICATION("Use StarMade Authentication", "If enabled, players will be able to authenticate with star-made.org to protect their account.", Boolean.class, false),
	REQUIRE_STARMADE_AUTHENTICATION("Require StarMade Authentication", "If enabled, players will be required to authenticate with star-made.org to join the server.", Boolean.class, false),
	PROTECTED_NAMES_BY_ACCOUNT("Protected Names by Account", "How many player names a player may protect with their account. If this count is exceeded, the account with the oldest last login time is overwritten.", Integer.class, 10, 0, Integer.MAX_VALUE),
	DEFAULT_BLUEPRINT_ENEMY_USE("Default Blueprint Enemy Permission", "If enabled, enemies will be able use blueprints uploaded by players by default unless manually toggled off by the uploader.", Boolean.class, true),
	DEFAULT_BLUEPRINT_FACTION_BUY("Default Blueprint Faction Permission", "If enabled, players will be able to buy blueprints uploaded by faction members by default unless manually toggled off by the uploader.", Boolean.class, true),
	DEFAULT_BLUEPRINT_OTHERS_BUY("Default Blueprint Others Permission", "If enabled, players will be able to buy blueprints uploaded by other players by default unless manually toggled off by the uploader.", Boolean.class, true),
	DEFAULT_BLUEPRINT_HOME_BASE_BUY("Default Blueprint Homebase Permission", "If enabled, players will be able to buy blueprints uploaded by home base members by default unless manually toggled off by the uploader.", Boolean.class, true),
	LOCK_FACTION_SHIPS("Lock Faction Ships", "If enabled, players can't enter the ships or stations of other factions.", Boolean.class, true),
	CATALOG_SLOTS_PER_PLAYER("Catalog Slots per Player", "How many slots per player for saved ships (-1 for unlimited).", Integer.class, -1, -1, Integer.MAX_VALUE - 1),
	UNIVERSE_DAY_IN_MS("Universe Day in Milliseconds", "How long is a 'day' (stellar system rotation) in milliseconds (-1 to switch off system rotation).", Integer.class, 20 * 60 * 1000, -1, Integer.MAX_VALUE - 1),
	ASTEROIDS_ENABLE_DYNAMIC_PHYSICS("Asteroid Dynamic Physics", "Enables asteroids to be able to move in space.", Boolean.class, true),
	ENABLE_BREAK_OFF("Enable Break Off", "Debug (don't activate unless you know what you're doing).", Boolean.class, false),
	COLLISION_DAMAGE("Collision Damage", "Colliding into another object does damage.", Boolean.class, false),
	COLLISION_DAMAGE_THRESHOLD("Collision Damage Threshold", "Threshold of Impulse that does damage (the lower, the less force is needed for damage).", Float.class, 2.0f, 0.0f, Float.MAX_VALUE),
	SKIN_ALLOW_UPLOAD("Skin Allow Upload", "If off, custom skin uploading to server is disabled.", Boolean.class, true),
	CATALOG_NAME_COLLISION_HANDLING("Catalog Name Collision Handling", "If off, saving with an existing entry is denied. If on, the name is automatically changed by adding numbers on the end.", Boolean.class, false),
	SECTOR_AUTOSAVE_SEC("Sector Autosave in Seconds", "Time interval in seconds the server will autosave (-1 for never).", Integer.class, 5 * 60, -1, 60 * 60 * 24 * 7),
	PHYSICS_SLOWDOWN_THRESHOLD("Physics Slowdown Threshold", "Milliseconds a collision test may take before anti-slowdown mode is activated.", Integer.class, 40, -1, Integer.MAX_VALUE - 1),
	THRUST_SPEED_LIMIT("Thrust Speed Limit", "How fast ships, etc. may go in m/s. Too high values may induce physics tunneling effects.", Integer.class, 75, 1, 5000),
	MAX_CLIENTS("Max Clients", "Max number of clients allowed on this server.", Integer.class, 32, 1, 1024),
	SUPER_ADMIN_PASSWORD_USE("Enable Super Admin Password", "Enable super admin for this server.", Boolean.class, false),
	SUPER_ADMIN_PASSWORD("Super Admin Password", "Super admin password for this server", String.class, "mypassword"),
	SERVER_LISTEN_IP("Server Listen IP", "Enter specific ip for the server to listen to. use \"all\" to listen on every ip.", String.class, "all"),
	SOCKET_BUFFER_SIZE("Socket Buffer Size", "Buffer size of incoming and outgoing data per socket", Integer.class, 64 * 1024, 0, Integer.MAX_VALUE),
	PHYSICS_LINEAR_DAMPING("Physics Linear Damping", "How much objects slow down naturally over time (must be between 0 and 1): 0 is no slowdown.", Float.class, 0.05f, 0.0f, 1.0f),
	PHYSICS_ROTATIONAL_DAMPING("Physics Rotational Damping", "How much object slow down naturally (must be between 0 and 1): 0 is no slowdown.", Float.class, 0.05f, 0.0f, 1.0f),
	AI_DESTRUCTION_LOOT_COUNT_MULTIPLIER("AI Destruction Loot Count Multiplier", "Multiply amount of items in a loot stack. Use values smaller 1 for less and 0 for none.", Float.class, 0.9f, 0.0f, 100.0f),
	AI_DESTRUCTION_LOOT_STACK_MULTIPLIER("AI Destruction loot Stack Multiplier", "Multiply amount of items spawned after AI destruction. Use values smaller 1 for less and 0 for none.", Float.class, 0.9f, 0.0f, 100.0f),
	CHEST_LOOT_COUNT_MULTIPLIER("Chest Loot Count Multiplier", "Multiply amount of items in a loot stack. Use values smaller 1 for less and 0 for none.", Float.class, 0.9f, 0.0f, 100.0f),
	CHEST_LOOT_STACK_MULTIPLIER("Chest Loot Stack Multiplier", "Multiply amount of items spawned in chests of generated chests. Use values smaller 1 for less and 0 for none.", Float.class, 0.9f, 0.0f, 100.0f),
	USE_WHITELIST("Use Whitelist", "Only names/ips from whitelist.txt are allowed.", Boolean.class, false),
	FILTER_CONNECTION_MESSAGES("Filter Connection Message", "Don't display join/disconnect messages.", Boolean.class, false),
	USE_UDP("Use UDP", "Use 'User Datagram Protocol' (UDP) instead of 'Transmission Control Protocol' (TCP) for connections.", Boolean.class, false),
	AUTO_KICK_MODIFIED_BLUEPRINT_USE("Auto Kick for Modified Blueprint Usage", "Kick players that spawn modified blueprints.", Boolean.class, false),
	AUTO_BAN_ID_MODIFIED_BLUEPRINT_USE("Auto Ban for Modified Blueprint Usage", "Ban player by name that spawn modified blueprints.", Boolean.class, false),
	AUTO_BAN_IP_MODIFIED_BLUEPRINT_USE("Auto IP Ban for Modified Blueprint Usage", "Ban player by IP that spawn modified blueprints.", Boolean.class, false),
	AUTO_BAN_TIME_IN_MINUTES("Auto Ban Time in Minutes", "Time to ban in minutes (-1 for permanently).", Integer.class, 60, 0, Integer.MAX_VALUE),
	REMOVE_MODIFIED_BLUEPRINTS("Remove Modified Blueprints", "Auto-removes a modified blueprint.", Boolean.class, false),
	TCP_NODELAY("TCP No Delay", "Naggles algorithm (Warning: Only change when you know what you're doing).", Boolean.class, true),
	PING_FLUSH("Ping Flush", "Flushes ping/pong immediately (Warning: Only change when you know what you're doing).", Boolean.class, false),
	SHOP_SPAWNING_PROBABILITY("Shop Spawning Probability", "(Must be between 0 and 1): 0 is no shops spawned in asteroid sectors, 1 is shop spawned in everyone (default: 8% -> 0.08).", Float.class, 0.01f, 0.0f, 1.0f),
	DEFAULT_SPAWN_SECTOR_X("Default Spawn Sector X", "DEFAULT Spawn Sector X Coordinate.", Integer.class, 2, Integer.MIN_VALUE, Integer.MAX_VALUE),
	DEFAULT_SPAWN_SECTOR_Y("Default Spawn Sector Y", "DEFAULT Spawn Sector Y Coordinate.", Integer.class, 2, Integer.MIN_VALUE, Integer.MAX_VALUE),
	DEFAULT_SPAWN_SECTOR_Z("Default Spawn Sector Z", "DEFAULT Spawn Sector Z Coordinate.", Integer.class, 2, Integer.MIN_VALUE, Integer.MAX_VALUE),
	MODIFIED_BLUEPRINT_TOLERANCE("Modified Blueprint Tolerance", "Tolerance of modified blueprint trigger (default = 10%).", Float.class, 0.1f, 0.0f, 100.0f),
	DEFAULT_SPAWN_POINT_X_1("Default Spawn Local Position Nr1 X", "First Rotating Spawn: Local Pos X Coordinate.", Float.class, 8.0f, -10000, 10000),
	DEFAULT_SPAWN_POINT_Y_1("Default Spawn Local Position Nr1 Y", "First Rotating Spawn: Local Pos Y Coordinate.", Float.class, -6.5f, -10000, 10000),
	DEFAULT_SPAWN_POINT_Z_1("Default Spawn Local Position Nr1 Z", "First Rotating Spawn: Local Pos Z Coordinate.", Float.class, 0.0f, -10000, 10000),
	DEFAULT_SPAWN_POINT_X_2("Default Spawn Local Position Nr2 X", "Second Rotating Spawn: Local Pos X Coordinate.", Float.class, 15.0f, -10000, 10000),
	DEFAULT_SPAWN_POINT_Y_2("Default Spawn Local Position Nr2 Y", "Second Rotating Spawn: Local Pos Y Coordinate.", Float.class, -6.5f, -10000, 10000),
	DEFAULT_SPAWN_POINT_Z_2("Default Spawn Local Position Nr2 Z", "Second Rotating Spawn: Local Pos Z Coordinate.", Float.class, 8.0f, -10000, 10000),
	DEFAULT_SPAWN_POINT_X_3("Default Spawn Local Position Nr3 X", "Third Rotating Spawn: Local Pos X Coordinate.", Float.class, 8.0f, -10000, 10000),
	DEFAULT_SPAWN_POINT_Y_3("Default Spawn Local Position Nr3 Y", "Third Rotating Spawn: Local Pos Y Coordinate.", Float.class, -6.5f, -10000, 10000),
	DEFAULT_SPAWN_POINT_Z_3("Default Spawn Local Position Nr3 Z", "Third Rotating Spawn: Local Pos Z Coordinate.", Float.class, 15.0f, -10000, 10000),
	DEFAULT_SPAWN_POINT_X_4("Default Spawn Local Position Nr4 X", "Forth Rotating Spawn: Local Pos X Coordinate.", Float.class, 0.0f, -10000, 10000),
	DEFAULT_SPAWN_POINT_Y_4("Default Spawn Local Position Nr4 Y", "Forth Rotating Spawn: Local Pos Y Coordinate.", Float.class, -6.5f, -10000, 10000),
	DEFAULT_SPAWN_POINT_Z_4("Default Spawn Local Position Nr4 Z", "Forth Rotating Spawn: Local Pos Z Coordinate.", Float.class, 8.0f, -10000, 10000),
	PLAYER_DEATH_CREDIT_PUNISHMENT("Player Credit Loss on Death Punishment (Ratio)", "Players credits lost of total on death (must be between 0 and 1): 1 = lose all, 0 = keep all.", Float.class, 0.1f, 0.0f, 1.0f),
	PLAYER_DEATH_CREDIT_DROP("Player Credit Loss on Death", "Drop credits lost on death into space instead.", Boolean.class, false),
	PLAYER_DEATH_BLOCK_PUNISHMENT("Player Block Loss on Death", "Player will drop all his blocks into space on death.", Boolean.class, false),
	PLAYER_DEATH_PUNISHMENT_TIME("Player No Punishment Time after Death", "Time interval in seconds after death of a player in which the player is not punished.", Integer.class, 5 * 60, 0, 60 * 60 * 24 * 7),
	PLAYER_HISTORY_BACKLOG("Player History Backlog", "How many login history objects (with name, IP, account-name, and time) should be saved by player state.", Integer.class, 30, 0, Integer.MAX_VALUE),
	PROJECTILES_ADDITIVE_VELOCITY("Projectiles Additive Velocity", "Initial projectile speed depend on relative linear velocity of object fired from.", Boolean.class, false),
	PROJECTILES_VELOCITY_MULTIPLIER("Projectiles Additive Velocity Multiplier", "Multiplier for projectile velocity.", Float.class, 1.0f, 0.0f, 100000.0f),
	WEAPON_RANGE_REFERENCE("Weapon Range Reference Distance", "Reference distance for weapon ranges. (what blockBehaviorConfig.xml weapon ranges are multiplied with (usually the sector size)). Set to 1 to interpret weapon ranges in the config in meters.", Float.class, 1000.0f, 300.0f, 1000000.0f),
	ALLOW_UPLOAD_FROM_LOCAL_BLUEPRINTS("Allow Upload from Local Blueprints", "Enables clients being able to upload their pre-build-blueprints to the server.", Boolean.class, true),
	SHOP_NPC_STARTING_CREDITS("NPC Shop Starting Credits", "How much credits do shops start with.", Integer.class, 10000000, 0, Integer.MAX_VALUE),
	SHOP_NPC_RECHARGE_CREDITS("NPC Shop Recharge Credit Amount / 10 Minutes", "How much credits do shops gain about every 10 min.", Integer.class, 100000, 0, Integer.MAX_VALUE),
	AI_WEAPON_AIMING_ACCURACY("AI Weapon Accuracy", "How accurate the AI aims (the higher the value the more accurate vs distance. 10 = about 99% accuracy at 10m).", Integer.class, 10, 0, Integer.MAX_VALUE),
	BROADCAST_SHIELD_PERCENTAGE("Broadcast Shield Percentage", "Percent of shields changed for the server to broadcast a shield synch.", Integer.class, 5, 0, Integer.MAX_VALUE),
	BROADCAST_POWER_PERCENTAGE("Broadcast Power Percentage", "Percent of power changed for the server to broadcast a power synch (not that critical).", Integer.class, 20, 0, Integer.MAX_VALUE),
	ADMINS_CIRCUMVENT_STRUCTURE_CONTROL("Admins Circumvent Structure Control", "Admins can enter ships of any faction.", Boolean.class, true),
	STAR_DAMAGE("Heat Damage", "Suns dealing damage to entities.", Boolean.class, true),
	SQL_NIO_FILE_SIZE("SQL NIO File Size", "Megabyte limit of .data file when to use NIO (faster) (must be power of 2).", Integer.class, 512, 256, Integer.MAX_VALUE),
	GALAXY_DENSITY_TRANSITION_INNER_BOUND("Galaxy Density Transition Inner Bound", "Controls how much of the galaxy is considered to be within the core zone of the galaxy.", Float.class, 0.1f, 0.1f, 1.0f),
	GALAXY_DENSITY_TRANSITION_OUTER_BOUND("Galaxy Density Transition Outer Bound", "Controls how much of the galaxy is considered to be within the outer zone of the galaxy.", Float.class, 0.2f, 0.1f, 1.0f),
	GALAXY_DENSITY_RATE_INNER("Galaxy Inner Density Rate", "How dense the galaxy is in the inner regions (must be between 0 and 1): 0 is no stars, 1 is full density.", Float.class, 0.62f, 0.1f, 1.0f),
	GALAXY_DENSITY_RATE_OUTER("Galaxy Outer Density Rate", "How dense the galaxy is in the outer regions (must be between 0 and 1): 0 is no stars, 1 is full density.", Float.class, 0.75f, 0.1f, 1.0f),
	PLANET_SIZE_MEAN_VALUE("Planet Size Mean", "Planet size mean (normal gaussian distribution) (min 300).", Float.class, 350.0f, 300.0f, Float.MAX_VALUE),
	PLANET_SIZE_DEVIATION_VALUE("Planet Size Deviation", "Planet size standard deviation. Note: normal gaussian distribution graph scaled horizontally by 1/3 (min 0).", Float.class, 150.0f, 0, Float.MAX_VALUE),
	ASTEROID_RADIUS_MAX("Asteroids Max Radius", "Asteroid max radius in blocks (from -x to +x).", Integer.class, 64, 1, Integer.MAX_VALUE),
	ASTEROID_RESOURCE_SIZE("Asteroids Resource Veins Diameter", "Average diameter of resource veins in asteroids.", Float.class, 2.5f, 1.0f, 65504.0f),
	ASTEROID_RESOURCE_CHANCE("Asteroids Resource Chance", "Chance per block to place a new resource vein (1.0 = 100%).", Float.class, 0.006f, 0.0f, 1.0f),
	PLAYER_MAX_BUILD_AREA("Player Max Build Area", "Max area a player may add/remove in adv. build mode.", Integer.class, 10, 0, Integer.MAX_VALUE),
	NT_SPAM_PROTECT_TIME_MS("NT Spam Protection Time", "Period of spam protection.", Integer.class, 30000, 0, Integer.MAX_VALUE),
	ASTEROID_SECTOR_REPLENISH_TIME_SEC("Asteroid Sector Replenish Time in Seconds", "Seconds until a sector that is mined down to 0 asteroids is replenished (-1 = never).", Integer.class, -1, -1, Integer.MAX_VALUE),
	NT_SPAM_PROTECT_MAX_ATTEMPTS("NT Spam Protection Max Attempts", "Max attempts before refusing connections in spam protect period (default is 1/sec for 30 sec).", Integer.class, 30, 0, Integer.MAX_VALUE),
	NT_SPAM_PROTECT_EXCEPTIONS("NT Spam Protection IP Exceptions", "IPs excepted from spam control (separate multiple with comma) (default is localhost).", String.class, "127.0.0.1"),
	ANNOUNCE_SERVER_TO_SERVERLIST("Server Announces to Server List", "Announces the server to the starmade server list so clients can find it. Hostname must be provided for HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST!", Boolean.class, false),
	HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST("Server Host Name to Announce to Server List", "This must be a valid hostname (either ip or host, e.g. play.star-made.org).", String.class, ""),
	SERVER_LIST_NAME("Server Name in Server List", "Max length 64 characters.", String.class, ""),
	SERVER_LIST_DESCRIPTION("Server Description in Server List", "Max length 128 characters.", String.class, ""),
	MISSILE_DEFENSE_FRIENDLY_FIRE("Anti Missile Friendly Fire", "Can shoot down own or missiles from own faction.", Boolean.class, true),
	USE_DYNAMIC_RECIPE_PRICES("Dynamic Crafting Recipe Price", "Use recipe based prices (the price is the price of the parts it is made out of in crafting).", Boolean.class, true),
	DYNAMIC_RECIPE_PRICE_MODIFIER("Dynamic Crafting Recipe Modifier", "Modifier to adjust dynamic price.", Float.class, 1.05f, -1.0f, 1000.0f),
	MAKE_HOMBASE_ATTACKABLE_ON_FP_DEFICIT("Make Homebase Destructible on FP Deficit", "Home bases become attackable if a faction's Faction Points are in the minus and the faction doesn't own any territory.", Boolean.class, true),
	PLANET_SPECIAL_REGION_PROBABILITY("Planet Special Region Probability", "One out of thisValue chance of a special region spawning per planet plate (cities, pyramids, etc) (changing this value might change some plates, but won't change any plates that are already modified by a player).", Integer.class, 240, 1, Integer.MAX_VALUE),
	NT_BLOCK_QUEUE_SIZE("NT Block Queue Size", "How many blocks are sent per update. Huge placements will shot faster, but it will consume more bandwidth and is subject to spamming players.", Integer.class, 1024, 1, Integer.MAX_VALUE),
	CHUNK_REQUEST_THREAD_POOL_SIZE_TOTAL("Chunk Request Thread Pool Size Total", "Thread pool size for chunk requests (from disk and generated).", Integer.class, 10, 1, Integer.MAX_VALUE),
	CHUNK_REQUEST_THREAD_POOL_SIZE_CPU("Chunk Request Thread Pool Size CPU", "Available threads of total for CPU generation. WARNING: too high can cause cpu spikes. About the amount of available cores minus one is best.", Integer.class, 2, 1, Integer.MAX_VALUE),
	BUY_BLUEPRINTS_WITH_CREDITS("Buy Blueprints with Credits", "Buy blueprints directly with credits.", Boolean.class, false),
	SHOP_USE_STATIC_SELL_BUY_PRICES("Static Shop Prices", "Shop buy and sell price change depending on stock (shop prices will always stay the same if true).", Boolean.class, false),
	SHOP_SELL_BUY_PRICES_UPPER_LIMIT("Shop Sell/Buy Price Upper Limit", "Maximum of base price a shop will want depending on its stock (e.g. max 120 credits if the normal cost is 100).", Float.class, 1.2f, 0.001f, Float.MAX_VALUE),
	SHOP_SELL_BUY_PRICES_LOWER_LIMIT("Shop Sell/Buy Price Lower Limit", "Minimum of base price a shop will want depending on its stock (e.g. max 80 credits if the normal cost is 100).", Float.class, 1.2f, 0.001f, Float.MAX_VALUE),
	MINING_BONUS("Mining Bonus Multiplier", "General multiplier on all mining.", Integer.class, 1, 1, Integer.MAX_VALUE),
	MAX_LOGIC_SIGNAL_QUEUE_PER_OBJECT("Max Logic Signal Queue per Object", "Max logic trace queue allowed.", Integer.class, 250000, 1, Integer.MAX_VALUE),
	MAX_LOGIC_ACTIVATIONS_AT_ONCE_PER_OBJECT_WARN("Max Logic Activations at Once per Object Warning", "Warn about objects that activate more than x blocks at once.", Integer.class, 10000, 1, Integer.MAX_VALUE),
	MAX_LOGIC_ACTIVATIONS_AT_ONCE_PER_OBJECT_STOP("Max Logic Activations at Once per Object Stop", "Stop logic of objects that activate more than x blocks at once. They will enter a logic cooldown of 10 seconds to prevent servers from overloading.", Integer.class, 50000, 1, Integer.MAX_VALUE),
	MAX_COORDINATE_BOOKMARKS("Max Sector Bookmarks per Player", "Coordinate bookmarks per player allowed.", Integer.class, 20, 0, Integer.MAX_VALUE),
	ALLOWED_STATIONS_PER_SECTOR("Max Stations Allowed per Sector", "How many stations are allowed per sector.", Integer.class, 1, 1, Integer.MAX_VALUE),
	STATION_CREDIT_COST("Station Credit Cost", "How much does a station or station blueprint cost.", Integer.class, 1000000, 0, Integer.MAX_VALUE),
	SKIN_SERVER_UPLOAD_BLOCK_SIZE("Skin Server Upload Block Size", "How fast should skins be transferred from server to clients (too high might cause lag) [default 256 ~ 16kb/s].", Integer.class, 256, 1, Short.MAX_VALUE),
	;
		/*
	SKIN_SERVER_UPLOAD_BLOCK_SIZE(en -> Lng.str("Skin server upload block size"), en -> Lng.str("how fast should skins be transferred from server to clients (too high might cause lag) [default 256 ~ 16kb/s]"),  () -> new SettingStateInt(256, 1, Short.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	WEIGHTED_CENTER_OF_MASS(en -> Lng.str("Weighted Center of Mass"), en -> Lng.str("if on, the center of mass for each structured will be calculated based on block mass. On 'false', the center of mass is always the core position"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	SECURE_UPLINK_ENABLED(en -> Lng.str("Secure uplink enabled"), en -> Lng.str("dedicated servers can be registered on the StarMade registry"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	SECURE_UPLINK_TOKEN(en -> Lng.str("Secure uplink token"), en -> Lng.str("uplink token, provided when registering a dedicated server"), () -> new SettingStateString(""), ServerConfigCategory.NETWORK_SETTING),
	USE_STRUCTURE_HP(en -> Lng.str("Structure HP enabled"), en -> Lng.str("ships and other structures use the hitpoint system. if off, a ship will overheat when the core gets taken out (old)"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	SHOP_REBOOT_COST_PER_SECOND(en -> Lng.str("Ship reboot cost per second at shop"), en -> Lng.str("Cost to reboot a ship at a shop (per second it would take to reboot in space)"), () -> new SettingStateFloat(1000.0f, 0, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	SHOP_ARMOR_REPAIR_COST_PER_HITPOINT(en -> Lng.str("Ship armor repair cost per HP at shop"), en -> Lng.str("Cost to repair a ship's armor at a shop"), () -> new SettingStateFloat(1.0f, 0, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	MAX_SIMULTANEOUS_EXPLOSIONS(en -> Lng.str("Max simultaneous explosions"), en -> Lng.str("the more the faster explosions at the same time are executed (costs in total about 20MB RAM each and of course CPU because it's all threaded) (10 is default for a medium powered singleplayer)"),  () -> new SettingStateInt(10, 0, Integer.MAX_VALUE), ServerConfigCategory.PERFORMANCE_SETTING),
	REMOVE_ENTITIES_WITH_INCONSISTENT_BLOCKS(en -> Lng.str("Remove entities with inconsistent blocks"), en -> Lng.str("This will remove ships that have blocks that are normally disallowed (e.g. space station blocks on ships)"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	OVERRIDE_INVALID_BLUEPRINT_TYPE(en -> Lng.str("Override invalid blueprint types"), en -> Lng.str("If a loaded blueprint is invalid, it's type will be overridden"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	FACTION_FOUNDER_KICKABLE_AFTER_DAYS_INACTIVITY(en -> Lng.str("Days of inactivity after a faction founder becomes kickable"), en -> Lng.str("Days of inactivity after which a founder may kick another founder"),  () -> new SettingStateInt(30, 0, Integer.MAX_VALUE - 1), ServerConfigCategory.NETWORK_SETTING),

	BLUEPRINT_SPAWNABLE_SHIPS(en -> Lng.str("Spawnable ship blueprints"), en -> Lng.str("enables or disables blueprint spawning from item"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	BLUEPRINT_SPAWNABLE_STATIONS(en -> Lng.str("Spawnable station blueprints"), en -> Lng.str("enables or disables blueprint spawning from item"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	USE_OLD_GENERATED_PIRATE_STATIONS(en -> Lng.str("Use old pirate stations"), en -> Lng.str("enables spawning of old style pirate stations"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	CARGO_BLEED_AT_OVER_CAPACITY(en -> Lng.str("Cargo drops when over capacity"), en -> Lng.str("cargo is ejected every minute if storage is at over capacity"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	ALLOW_PERSONAL_INVENTORY_OVER_CAPACITY(en -> Lng.str("Allow over capacity for personal inventory"), en -> Lng.str("Personal Inventory can go over capacity"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	ONLY_ALLOW_FACTION_SHIPS_ADDED_TO_FLEET(en -> Lng.str("Allow only factions ships to be added to fleet"), en -> Lng.str("only allows faction ships to be added to fleet"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	MAX_CHAIN_DOCKING(en -> Lng.str("Max docking chain length"), en -> Lng.str("maximal deepness of docking chains (may cause glitches depending on OS (path and filename length) at high numbers)"),  () -> new SettingStateInt(25, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	SHOP_RAILS_ON_ADV(en -> Lng.str("Rails on advanced shops"), en -> Lng.str("Advanced shops will have 4 rails dockers that can be used like a neutral homebase (anything docked is safe)"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	SHOP_RAILS_ON_NORMAL(en -> Lng.str("Rails on normal shops"), en -> Lng.str("Normal shops will have 4 rails dockers that can be used like a neutral homebase (anything docked is safe)"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	ALLOW_FLEET_FORMATION(en -> Lng.str("Allow fleet formations"), en -> Lng.str("Allows fleet formation"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	BACKUP_WORLD_ON_MIGRATION(en -> Lng.str("Backup world on migration"), en -> Lng.str("Back up world when migrating to a new file format"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	BACKUP_BLUEPRINTS_ON_MIGRATION(en -> Lng.str("Backup blueprints on migration"), en -> Lng.str("Back up blueprints when migrating to a new file format"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),

	SECTORS_TO_EXPLORE_FOR_SYS(en -> Lng.str("Sectors to explore for system information"), en -> Lng.str("How many sectors of a system have to be explored"),  () -> new SettingStateInt(15, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	NPC_FACTION_SPAWN_LIMIT(en -> Lng.str("Max NPC Factions per galaxy"), en -> Lng.str("Maximum npc factions per galaxy (-1 for unlimited (will still be around 2-10))"),  () -> new SettingStateInt(-1, -1, Integer.MAX_VALUE), ServerConfigCategory.NPC_SETTING),
	NPC_DEBUG_MODE(en -> Lng.str("NPC Debug mode"), en -> Lng.str("Sends complete NPC faction package to clients (very bandwith intensive)"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	FLEET_OUT_OF_SECTOR_MOVEMENT(en -> Lng.str("Unloaded fleet sector movement speed in miliseconds"), en -> Lng.str("How long for an unloaded fleet to cross a sector in ms"),  () -> new SettingStateInt(6000, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	NPC_LOADED_SHIP_MAX_SPEED_MULT(en -> Lng.str("NPC max ship speed multiplier"), en -> Lng.str("How fast NPC fleet ships are compared to their max speed when loaded"),  () -> new SettingStateFloat(0.7f, 0.0f, 100000.0f), ServerConfigCategory.GAME_SETTING),
	USE_FOW(en -> Lng.str("Fog of War"), en -> Lng.str("Use 'fog of war'. Turning this off will make everything visible to everyone"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	ALLOW_PASTE_AABB_OVERLAPPING(en -> Lng.str("Allow Paste bounding box overlapping"), en -> Lng.str("Allow Paste bounding box overlapping"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),

	// DEBUG | DEPRECATED
	DEBUG_FSM_STATE("transfer debug FSM state. Turning this on may slow down network", () -> new SettingStateBoolean(false)),
	PHYSICS_SHAPE_CASTING_TUNNELING_PREVENTION("Makes a convex cast for hight speed object to prevent clipping. High Cost. (Bugged right now, so dont turn it on)", () -> new SettingStateBoolean(false)),
	FORCE_DISK_WRITE_COMPLETION("forces writing operations of raw data to disk directly after operation. For some OS this prevents raw data corruption", () -> new SettingStateBoolean(false)),

	DEBUG_SEGMENT_WRITING("Debugs correctness of writing of segments (costs server performance)", () -> new SettingStateBoolean(false)),

	TURNING_DIMENSION_SCALE("Scaling of tuning speed VS ship dimension (default = 1.1)", () -> new SettingStateFloat(1.1f, 0.0f, 100.0f)),


	RECIPE_BLOCK_COST("How much blocks have to be invested to create a recipe (min 0)",  () -> new SettingStateInt(5000, 0, Integer.MAX_VALUE - 1)),
	RECIPE_REFUND_MULT("how much blocks are refunded from selling a recipe (must be between 0 and 1): 0 no refund, 1 full refund", () -> new SettingStateFloat(0.5f, 0.0f, 1.0f)),
	RECIPE_LEVEL_AMOUNT("On how much created blocks will a recipe level up (base value) (min 0)",  () -> new SettingStateInt(4000, 0, Integer.MAX_VALUE - 1)),


	NT_SPAM_PROTECT_ACTIVE("enables connection spawn protection (flooding servers with login attempts)", () -> new SettingStateBoolean(true)),
	USE_PERSONAL_SECTORS("will spawn a player in a locked sector sandbox (warning, don't use unless you know what you do)", () -> new SettingStateBoolean(false)),
	BATTLE_MODE("turn on battlemode (warning, don't use unless you know what you're doing)", () -> new SettingStateBoolean(false)),
	BATTLE_MODE_CONFIG("General config for battlemode", ()-> new SettingStateString("battleSector=0,0,0,Physics.smsec;battleSector=15,15,15,Physics.smsec;countdownRound=300;countdownStart=30;maxMass=-1;maxDim=300;maxMassPerFaction=-1;")),
	BATTLE_MODE_FACTIONS("Faction config for battlemode", ()-> new SettingStateString("[TeamA, fighters, 500,500,500, 0.5,0.1,0.9];[TeamB, fighters, -500,-500,-500, 0.5,0.9,0.2];[TeamFFA,ffa, 0,0,-500, 0.2,0.9,0.9];[Spectators,spectators, 0,500,0,0.8,0.4,0.8]")),
	LEADERBOARD_BACKLOG("time in hours to keep leaderboard backlog (the more time, the more data has to be sent to client)",  () -> new SettingStateInt(24, 0, Integer.MAX_VALUE)),

	DEBUG_BEAM_POWER_CONSUMPTION("server will send notifications on power consumed (not counting power given from supply) on server (costs performance, so only use for debugging)", () -> new SettingStateBoolean(false)),
	DEBUG_BEAM_TICKS_DONE("server will send notifications on ticks done on server (costs performance, so only use for debugging)", () -> new SettingStateBoolean(false)),
	DEBUG_BEAM_POWER_PER_TICK("server will send notifications on beam power per tick on server (costs performance, so only use for debugging)", () -> new SettingStateBoolean(false)),
	DEBUG_MISSILE_POWER_CONSUMPTION("server will send notifications on missiles on server (costs performance, so only use for debugging)", () -> new SettingStateBoolean(false)),

	NPC_LOG_MODE("use 0 for npc file logs [/logs/npc/] and 1 for normal log output",  () -> new SettingStateInt(0, 0, 2)),
	NPC_DEBUG_SHOP_OWNERS("Additional shop owners for npc faction shops (case insensitive, seperate with comma)", () -> new SettingStateString("")),
	SQL_PERMISSION("user name allowed to sql query remotely (direct console always allowed /sql_query, /sql_update, /sql_insert_return_generated_keys) (case insensitive, seperate with comma)", () -> new SettingStateString("")),
	DEBUG_EMPTY_CHUNK_WRITES("Logs empty chunks (debug only)", () -> new SettingStateBoolean(false)),
	ALLOWED_UNPACKED_FILE_UPLOAD_IN_MB("how much mb is an uploaded blueprint/skin allowed to have (unzipped)",  () -> new SettingStateInt(1024, 0, Integer.MAX_VALUE)),
	RESTRICT_BUILDING_SIZE("Restricts Building Size to X times Sector Size (-1 for off) Warning: values set in GameConfig.xml overwrite this", () -> new SettingStateFloat(2.0f, 0.0f, 32.0f)),

	DISPLAY_GROUPING_DEBUG_INFORMATION("Displays grouping calculation information", () -> new SettingStateBoolean(false)),
	MANAGER_CALC_CANCEL_ON("Enables performance increase by prematurely ending calculations when there is a refresh request", () -> new SettingStateBoolean(true)),
	JUMP_DRIVE_ENABLED_BY_DEFAULT("Weather all ships have jump capability or the basic jump chamber is required for jump capability", () -> new SettingStateBoolean(true)),
	SHORT_RANGE_SCAN_DRIVE_ENABLED_BY_DEFAULT("Weather all ships have scanner capability or the basic scan chamber is required for scan capability", () -> new SettingStateBoolean(true)),
	SPAWN_PROTECTION("Spawn protection in seconds (may not yet protect against everything)",  () -> new SettingStateInt(10, 0, Integer.MAX_VALUE)),

	AI_ENGAGEMENT_RANGE_OF_MIN_WEAPON_RANGE("Percentage of minimum weapon range the AI prefers to engage from",  () -> new SettingStateFloat(0.75f, 0.0f, 1.0f)),
	MISSILE_TARGET_PREDICTION_SEC("Since lockstep algorithm is based on recorded target positions, how much should a target chasing missiles predict a target's position based on its velocity (in ticks of 8ms). Change if missiles miss fast targets",  () -> new SettingStateFloat(0.5f, 0.0f, 1000000.0f)),
	AI_WEAPON_SWITCH_DELAY("Delay inbetween an AI can switch weapon in ms",  () -> new SettingStateInt(500, 0, Integer.MAX_VALUE)),
	ALLOW_FACTORY_ON_SHIPS("Factories work on ships", () -> new SettingStateBoolean(false)),
	SHIPYARD_IGNORE_STRUCTURE("Removes necessity to build shipyard structure (just computer and ancor is enough)", () -> new SettingStateBoolean(false)),
	IGNORE_DOCKING_AREA("Ignore size of structure when doing (might lead to overlapping)", () -> new SettingStateBoolean(false)),
	MISSILE_RADIUS_HP_BASE("Missile Damage to Radius relation: MissileRadius = ((3/4π) * (MissileTotalDamage/MISSILE_RADIUS_HP_BASE)) ^ (1/3)", () -> new SettingStateFloat(1.0f, 0.000001f, 9999999999.0f)),
	TEST_PLANET_TYPE("Select planet (0 to 6) ",  () -> new SettingStateInt(1, 0, 6)),
	MAX_EXPLOSION_POOL("Maximum amount of explosions to keep in pool (to reuse in memory, -1 for unlimited)",  () -> new SettingStateInt(-1, -1, 6)),

		 */

	static final String CONFIG_PATH = "server.cfg";
	public final String displayName;
	public final String description;
	public final Class<?> type;
	public final Object defaultValue;
	public final Object min;
	public final Object max;
	public final Object[] options;
	private Object currentValue;

	ServerConfig(String displayName, String description, Class<?> type, Object defaultValue) {
		this.displayName = displayName;
		this.description = description;
		this.type = type;
		this.defaultValue = defaultValue;
		min = null;
		max = null;
		options = null;
		currentValue = defaultValue;
	}

	ServerConfig(String displayName, String description, Class<?> type, Object defaultValue, Object min, Object max) {
		this.displayName = displayName;
		this.description = description;
		this.type = type;
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		options = null;
		currentValue = defaultValue;
	}

	ServerConfig(String displayName, String description, Class<?> type, Object defaultValue, Object... options) {
		this.displayName = displayName;
		this.description = description;
		this.type = type;
		this.defaultValue = defaultValue;
		this.options = options;
		min = null;
		max = null;
		currentValue = defaultValue;
	}

	public Object getValue() {
		return currentValue;
	}

	public void setValue(Object value) {
		if(value.getClass() != type) throw new IllegalArgumentException("Value must be of type " + type.getSimpleName());
		currentValue = value;
		saveValues();
	}

	public static void loadValues() {
		File file = new File(StarMadeUtils.getSMFolder(), CONFIG_PATH);
		if(!file.exists()) saveValues();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
			String line;
			while((line = reader.readLine()) != null) {
				String[] split = line.split(" = ");
				if(split.length != 2) {
					continue;
				}
				String[] split2 = split[1].split("//");
				if(split2.length != 2) {
					continue;
				}
				ServerConfig config = valueOf(split[0]);
				if(config.type == Boolean.class) config.currentValue = Boolean.parseBoolean(split2[0]);
				else if(config.type == Integer.class) config.currentValue = Integer.parseInt(split2[0]);
				else if(config.type == Float.class) config.currentValue = Float.parseFloat(split2[0]);
				else if(config.type == String.class) config.currentValue = split2[0];
			}
			reader.close();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	public static void saveValues() {
		File file = new File(StarMadeUtils.getSMFolder(), CONFIG_PATH);
		try {
			file.delete();
			file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));
			for(ServerConfig config : values()) {
				writer.write(config.name() + " = " + config.currentValue + "//" + config.description + "\n");
			}
			writer.flush();
			writer.close();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
}
