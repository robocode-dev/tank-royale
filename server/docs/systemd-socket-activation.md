## Guide to Systemd Socket Activation for the Robocode Server

This guide describes how to set up [Socket Activation] via `systemd` on Linux.
With socket activation, the server will only start when a client accesses the port for the first time.
That is, the Robocode server will be started up automatically when e.g. a UI or bot connects to the port the first time.

### Socket File Setup

Create a socket unit file at `/etc/systemd/system/robocode.socket`:

```toml
[Unit]
Description = "Robocode Tank Royale Socket"

[Socket]
ListenStream = 7654

[Install]
WantedBy = "sockets.target"
```

> [!IMPORTANT]  
> This configuration uses port 7654, which is the default for Robocode, but you can whatever port you prefer.

### Service File Setup

Create a service unit file at `/etc/systemd/system/robocode.service`:

```toml
[Unit]
Description = "Robocode Tank Royale Service"
Requires = "robocode.socket"
After = "network-online.target"
Wants = "network-online.target"

[Service]
ExecStart = "/usr/bin/java -jar /opt/robocode/robocode-tankroyale-server-0.28.0.jar --port=inherit"
WorkingDirectory = "/opt/robocode/"
TimeoutStartSec = 30
StandardInput = "socket"
StandardOutput = "journal"
StandardError = "journal"

[Install]
WantedBy = "multi-user.target"
```

> [!IMPORTANT]  
> Assumes Robocode server jar is located in `/opt/robocode` and using version 0.28.0. Adjust paths and version as
> needed.
>
> The `StandardInput=socket` makes it possible for Java to passing the socket and make it available with the
`java.lang.System.inheritedChannel()` method required by the Server.
>
> You might need to add a `User=<some-user>` and `Group=<some-group>` under `[Service]` to access the `/opt/robocode`
> working directory.

### Socket Management Commands

Enable the socket:

```shell
sudo systemctl enable robocode.socket
```

You can disable the socket later, if you need to like this:

```shell
sudo systemctl disable robocode.socket
```

Start the socket:

```shell
sudo systemctl start robocode.socket
```

You can stop the socket later, if you need to like this:

```shell
sudo systemctl stop robocode.socket
```

### Verification and Monitoring

Check service port availability:

```shell
ss -tuln | grep 7654
```

(Depending on which port number you specified in the `robocode.socket` file)

This is the expected output when service is running:

```
tcp   LISTEN 0      4096                *:7654            *:*
```

You can view service logs with real-time monitoring like this:

```shell
journalctl -u robocode.service -n 50 -f
```

(the `-n 50` tells that you want up the 50 lines, and `-f` means that you want to follow the latest log entries)

### Restarting the Service

If you update the `robocode.socket` or `robocode.service` file, you need to restart the service:

```shell
sudo systemctl daemon-reload
sudo systemctl restart robocode.service
```

### Reading Service Status

You can read out the status of the service like this:

```shell
sudo systemctl status robocode.service
```

This gives output similar to this, if your server is running correctly:

```
● robocode.service - Robocode Tank Royale Server
     Loaded: loaded (/etc/systemd/system/robocode.service; disabled; preset: enabled)
     Active: active (running) since Fri 2024-12-20 22:19:16 CET; 4min 23s ago
TriggeredBy: ● robocode.socket
   Main PID: 4778 (java)
      Tasks: 34 (limit: 19134)
     Memory: 85.7M ()
     CGroup: /system.slice/robocode.service
             └─4778 /usr/bin/java -jar /opt/robocode/robocode-tankroyale-server-0.28.0.jar --port=inherit
```

[Socket Activation]: https://insanity.industries/post/socket-activation-all-the-things/ "Socket Activation"