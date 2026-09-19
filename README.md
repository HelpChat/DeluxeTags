<p align="center">
  <a href="https://helpch.at/discord">
    <img src="https://helpch.at/banner.png" alt="HelpChat Discord">
  </a>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/HelpChat/DeluxeTags/refs/heads/assets/header.png" alt="DeluxeTags Banner">
</p>

<p align="center">
  <a href="https://modrinth.com/plugin/deluxetags">
    <img src="https://img.shields.io/modrinth/v/wtpLgugo" alt="Modrinth Download">
  </a>
  <a href="https://ci.extendedclip.com/job/DeluxeTags/">
    <img src="http://ci.extendedclip.com/buildStatus/icon?job=DeluxeTags" alt="Dev Builds">
  </a>
  <a href="https://helpch.at/discord">
    <img src="https://img.shields.io/discord/164280494874165248?color=5562e9&logo=discord&logoColor=white" alt="Discord">
  </a>
</p>

---

## About DeluxeTags

**DeluxeTags** gives players the ability to have an extra tag in chat, tab lists, and everywhere else. 

Tags are granted to players via specific permission nodes, allowing for various allocation methods. Players with access to multiple tags can seamlessly switch between them using an intuitive, fully customizable GUI menu.

This serves as an excellent "extra prefix" solution as it does not require a player to belong to a specific primary group. To grant a player access to a specific tag, assign them the permission node: `deluxetags.tag.<identifier>`

<p align="center">
  <img src="https://raw.githubusercontent.com/HelpChat/DeluxeTags/refs/heads/assets/tags.png" alt="Tags Preview">
</p>

## Setup

DeluxeTags supports Spigot and Paper from Minecraft 1.8.8 onward.

To configure DeluxeTags with your preferred chat management plugin, please refer to the documentation.

* [DeluxeTags Setup Guide](https://wiki.helpch.at/helpchat-plugins/deluxetags#setup)

## MySQL player selections

YAML remains the default. To share player selections across servers, create a MySQL 8.0+
database and configure `plugins/DeluxeTags/mysql.yml` on each server with the same database
and table prefix:

```yaml
storage:
  type: mysql
  sync-interval-seconds: 5
  mysql:
    host: localhost
    port: 3306
    database: deluxetags
    username: deluxetags
    password: 'your-password'
    table-prefix: deluxetags_
    ssl-mode: PREFERRED
    connect-timeout-ms: 3000
    socket-timeout-ms: 5000
```

The database user needs `CREATE`, `SELECT`, `INSERT`, and `UPDATE` privileges in that
database. DeluxeTags creates its own InnoDB tables, but does not create the database.
The JDBC driver and connection pool are included in the plugin jar. Java 8 remains supported.
Table prefixes may contain up to 40 letters, digits, or underscores. The polling interval
accepts 1–3600 seconds; connection and socket timeouts accept 250–5000 milliseconds.
TLS modes are `DISABLED`, `PREFERRED`, `REQUIRED`, `VERIFY_CA`, and `VERIFY_IDENTITY`.
`PREFERRED` attempts encryption but allows an unencrypted connection if unavailable;
use `VERIFY_IDENTITY` with a trusted server certificate when verified TLS is required.

All storage settings live in `mysql.yml`, including the YAML/MySQL backend choice.

Restart after changing any storage setting in `mysql.yml`. `/tags reload` reloads tags and messages,
refreshes selections, and reports storage settings that still need a restart.
Tag definitions, categories, and permissions remain local: configure matching tag identifiers
on each server. Players must have the relevant permission on each server.

Selections load asynchronously on join and refresh for online players every five seconds
by default. `load_tag_on_join: false` still loads the saved preference but defers its initial
application until a tag update, such as chat. Changes from another server apply when polled.
Simultaneous writes use database commit order; the last committed selection wins.
Choosing no tag is saved explicitly and overrides default-tag permissions, while enabled
forced tags retain priority. A missing or unauthorized tag uses a local fallback without
deleting the shared preference. Deleting a tag definition in MySQL mode affects only that server.

On the first successful MySQL initialization, each installation automatically imports valid
UUID/string records from `userdata/player_tags.yml`. Existing database records always win,
including previously cleared selections. The original YAML file remains unchanged. Malformed
records are skipped with a count in the log. A failed import rolls back and retries. Completion
is recorded in the database against the installation's `storage-installation-id` file; keep
that file across restarts. Use a distinct installation ID for each server when copying plugin
folders to a new server. Changing the database or table prefix creates a separate import target.

When storage is unavailable, cached tags remain visible and selection changes are refused.
Success messages appear only after a confirmed save. New players wait for their selections
to load. DeluxeTags retries automatically and reconciles database state before resuming changes;
it neither switches to YAML nor replays failed writes. A connection lost during commit may
still have saved the selection, which the next successful refresh will discover.
Storage messages can be customized under `storage` in `messages.yml`. Shutdown allows up to
ten seconds for accepted operations to finish and logs if that limit is exceeded.

Switching `storage.type` back to `yaml` uses the local YAML file; it does **not** export MySQL
selections. In MySQL mode, `getSavedTagIdentifier` is a cache-only getter for loaded players.
Integrations should use `saveTagIdentifierAsync` to observe save results; the legacy void
methods remain available but cannot acknowledge persistence. `getPlayerFile()` remains a
legacy YAML accessor and does not represent MySQL state. Do not modify the tables directly;
cleared selections retain a row revision for synchronization.

### Storage tests

Run `gradlew check` for the legacy/modern API tests and isolated shaded-driver checks.
Real database tests are opt-in: point these environment variables at a disposable test database,
then run `gradlew mysqlIntegrationTest`:

```text
DELUXETAGS_MYSQL_HOST=127.0.0.1
DELUXETAGS_MYSQL_PORT=3306
DELUXETAGS_MYSQL_DATABASE=deluxetags_test
DELUXETAGS_MYSQL_USERNAME=deluxetags_test
DELUXETAGS_MYSQL_PASSWORD=...
```

Integration tests create randomly prefixed tables and remove only those tables afterward.
Their database account additionally needs `DROP` permission. They cover imports, retryable
transactions, Unicode, concurrent writes from two storage instances, and cleared selections.

## Documentation

For comprehensive listings of configuration options, localization, commands, and permission structures, please visit the wiki.

* [Commands and Permissions](https://wiki.helpch.at/helpchat-plugins/deluxetags/commands-and-permissions)
* [Configuration Wiki](https://wiki.helpch.at/helpchat-plugins/deluxetags)

---

### Contributing

If you wish to contribute to the project, please submit a pull request with a clear description of your changes or bug fixes. For significant feature additions, please open an issue first to discuss your ideas.
