# SQL Monitor JDBC Driver

This JDBC Driver wraps an underlying driver and reports any interaction that goes via the JDBC interface to logs.
It uses SLF4J logging interface as it's output.

## Usage

Prefix the jdbc driver name with sql-monitor:

e.g.
instead of
```jdbc:mysql://host:port/db```
use
```jdbc:sql-monitor:mysql://host:port/db```

The logs are reported on the com.wixpress.utils.sqlMonitor.SqlMonitorDriver class

## Notes

Depending on your configuration, you may need to load both driver classes before opening a Jdbc Connection
 e.g.
```this.getClassLoader().loadClass("com.wixpress.utils.sqlMonitor.SqlMonitorDriver");
this.getClassLoader().loadClass("com.mysql.jdbc.Driver");
DriverManager.getConnection("jdbc:sql-monitor:mysql://hostname:port/dbname","username", "password");```
