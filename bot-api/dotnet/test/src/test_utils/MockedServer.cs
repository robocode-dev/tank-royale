﻿using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using Fleck;
using Robocode.TankRoyale.BotApi.Internal.Json;
using Robocode.TankRoyale.BotApi.Util;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Tests.Test_utils;

public class MockedServer
{
    public static readonly int Port = FindAvailablePort();
    public static Uri ServerUrl => new($"ws://127.0.0.1:{Port}");

    public static string SessionId = "123abc";
    public static string Name = nameof(MockedServer);
    public static string Version = "1.0.0";
    public static string Variant = "Tank Royale";
    public static ISet<string> GameTypes = new HashSet<string> { "melee", "classic", "1v1" };
    public static int MyId = 1;
    public static string GameType = "classic";
    public static int ArenaWidth = 800;
    public static int ArenaHeight = 600;
    public static int NumberOfRounds = 10;
    public static double GunCoolingRate = 0.1;
    public static int MaxInactivityTurns = 450;
    public static int TurnTimeout = 30_000;
    public static int ReadyTimeout = 1_000_000;

    public static int BotEnemyCount = 7;
    public static double BotEnergy = 99.7;
    public static double BotX = 44.5;
    public static double BotY = 721.34;
    public static double BotDirection = 120.1;
    public static double BotGunDirection = 103.45;
    public static double BotRadarDirection = 253.3;
    public static double BotRadarSweep = 13.5;
    public static double BotSpeed = 8.0;
    public static double BotTurnRate = 5.1;
    public static double BotGunTurnRate = 18.9;
    public static double BotRadarTurnRate = 34.1;
    public static double BotGunHeat = 7.6;

    private int _turnNumber = 1;
    private double _energy = BotEnergy;
    private double _gunHeat = BotGunHeat;
    private double _speed = BotSpeed;
    private double _direction = BotDirection;
    private double _gunDirection = BotGunDirection;
    private double _radarDirection = BotRadarDirection;

    private double _speedIncrement;
    private double _turnIncrement;
    private double _gunTurnIncrement;
    private double _radarTurnIncrement;

    private double? _speedMinLimit;
    private double? _speedMaxLimit;
    private double? _directionMinLimit;
    private double? _directionMaxLimit;
    private double? _gunDirectionMinLimit;
    private double? _gunDirectionMaxLimit;
    private double? _radarDirectionMinLimit;
    private double? _radarDirectionMaxLimit;

    private WebSocketServer _server;
    private IWebSocketConnection _currentConnection;

    private readonly EventWaitHandle _openedEvent = new AutoResetEvent(false);
    private readonly EventWaitHandle _botHandshakeEvent = new AutoResetEvent(false);
    private readonly EventWaitHandle _gameStartedEvent = new AutoResetEvent(false);
    private readonly EventWaitHandle _tickEvent = new AutoResetEvent(false);
    private readonly EventWaitHandle _botIntentEvent = new AutoResetEvent(false);

    private readonly EventWaitHandle _botIntentContinueEvent = new AutoResetEvent(false);

    private BotIntent _botIntent;

    public BotIntent BotIntent => _botIntent;


    public void Start()
    {
        _server = new WebSocketServer(ServerUrl.AbsoluteUri, false);
        _server.Start(conn =>
        {
            conn.OnOpen = () => OnOpen(conn);
            conn.OnClose = () => OnClose(conn);
            conn.OnMessage = message => OnMessage(conn, message);
            conn.OnError = OnError;
        });
    }

    public void Stop()
    {
        _server?.Dispose();
    }

    public void CloseConnections()
    {
        _currentConnection?.Close();
    }

    public void SendRawText(string text)
    {
        _currentConnection?.Send(text);
    }

    public void SetEnergy(double energy)
    {
        _energy = energy;
    }

    public void SetSpeed(double speed)
    {
        _speed = speed;
    }

    public void SetGunHeat(double gunHeat)
    {
        _gunHeat = gunHeat;
    }

    public void SetSpeedIncrement(double increment) {
        _speedIncrement = increment;
    }

    public void SetTurnIncrement(double increment) {
        _turnIncrement = increment;
    }

    public void SetGunTurnIncrement(double increment) {
        _gunTurnIncrement = increment;
    }

    public void SetRadarTurnIncrement(double increment) {
        _radarTurnIncrement = increment;
    }

    public void SetSpeedMinLimit(double minLimit) {
        _speedMinLimit = minLimit;
    }

    public void SetSpeedMaxLimit(double maxLimit) {
        _speedMaxLimit = maxLimit;
    }

    public void SetDirectionMinLimit(double minLimit) {
        _directionMinLimit = minLimit;
    }

    public void SetDirectionMaxLimit(double maxLimit) {
        _directionMaxLimit = maxLimit;
    }

    public void SetGunDirectionMinLimit(double minLimit) {
        _gunDirectionMinLimit = minLimit;
    }

    public void SetGunDirectionMaxLimit(double maxLimit) {
        _gunDirectionMaxLimit = maxLimit;
    }

    public void SetRadarDirectionMinLimit(double minLimit) {
        _radarDirectionMinLimit = minLimit;
    }

    public void SetRadarDirectionMaxLimit(double maxLimit) {
        _radarDirectionMaxLimit = maxLimit;
    }

    public void ResetBotIntentEvent()
    {
        _botIntentEvent.Reset();
    }

    public bool AwaitConnection(int milliSeconds)
    {
        try
        {
            return _openedEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitConnection: Exception occurred: " + ex);
        }

        return false;
    }

    public bool AwaitBotHandshake(int milliSeconds)
    {
        try
        {
            return _botHandshakeEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitBotHandshake: Exception occurred: " + ex);
        }

        return false;
    }

    public bool AwaitGameStarted(int milliSeconds)
    {
        try
        {
            return _gameStartedEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitGameStarted: Exception occurred: " + ex);
        }

        return false;
    }

    public bool AwaitTick(int milliSeconds)
    {
        try
        {
            return _tickEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitTickEvent: Exception occurred: " + ex);
        }

        return false;
    }

    public bool AwaitBotIntent(int milliSeconds)
    {
        try
        {
            _botIntentContinueEvent.Set();
            return _botIntentEvent.WaitOne(milliSeconds);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("AwaitBotIntentEvent: Exception occurred: " + ex);
        }

        return false;
    }

    public BotHandshake Handshake { get; private set; }


    private void OnOpen(IWebSocketConnection conn)
    {
        _currentConnection = conn;
        _openedEvent.Set();
        SendServerHandshake(conn);
    }

    private void OnClose(IWebSocketConnection conn)
    {
        if (_currentConnection == conn)
        {
            _currentConnection = null;
        }
    }

    private void OnMessage(IWebSocketConnection conn, string messageJson)
    {
        Console.WriteLine("OnMessage: " + messageJson);

        var message = JsonConverter.FromJson<Message>(messageJson);
        if (message == null) return;

        var msgType = (MessageType)Enum.Parse(typeof(MessageType), message.Type);
        switch (msgType)
        {
            case MessageType.BotHandshake:
                Handshake = JsonConverter.FromJson<BotHandshake>(messageJson);
                _botHandshakeEvent.Set();

                SendGameStartedForBot(conn);
                _gameStartedEvent.Set();
                break;

            case MessageType.BotReady:
                SendRoundStarted(conn);
                // Existing tick event logic remains
                SendTickEventForBot(conn, _turnNumber++);
                _tickEvent.Set();
                break;

            case MessageType.BotIntent:
                if (_speedMinLimit != null && _speed < _speedMinLimit) return;
                if (_speedMaxLimit != null && _speed > _speedMaxLimit) return;

                if (_directionMinLimit != null && _direction < _directionMinLimit) return;
                if (_directionMaxLimit != null && _direction > _directionMaxLimit) return;

                if (_gunDirectionMinLimit != null && _gunDirection < _gunDirectionMinLimit) return;
                if (_gunDirectionMaxLimit != null && _gunDirection > _gunDirectionMaxLimit) return;

                if (_radarDirectionMinLimit != null && _radarDirection < _radarDirectionMinLimit) return;
                if (_radarDirectionMaxLimit != null && _radarDirection > _radarDirectionMaxLimit) return;

                _botIntentContinueEvent.WaitOne();

                _botIntent = JsonConverter.FromJson<BotIntent>(messageJson);
                _botIntentEvent.Set();

                SendTickEventForBot(conn, _turnNumber++);
                _tickEvent.Set();

                // Update states
                _speed += _speedIncrement;
                _direction += _turnIncrement;
                _gunDirection += _gunTurnIncrement;
                _radarDirection += _radarTurnIncrement;
                break;
        }
    }

    private static void OnError(Exception ex)
    {
        throw new InvalidOperationException("MockedServer error", ex);
    }

    private static int FindAvailablePort()
    {
        var listener = new TcpListener(IPAddress.Loopback, 0);
        listener.Start();
        var port = ((IPEndPoint)listener.LocalEndpoint).Port;
        listener.Stop();
        return port;
    }

    private static void SendServerHandshake(IWebSocketConnection conn)
    {
        var serverHandshake = new ServerHandshake
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.ServerHandshake),
            SessionId = SessionId,
            Name = Name,
            Version = Version,
            Variant = Variant,
            GameTypes = GameTypes,
            GameSetup = null
        };
        Send(conn, serverHandshake);
    }

    private static void SendGameStartedForBot(IWebSocketConnection conn)
    {
        var gameStarted = new GameStartedEventForBot
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.GameStartedEventForBot),
            MyId = MyId
        };
        var gameSetup = new Schema.GameSetup
        {
            GameType = GameType,
            ArenaWidth = ArenaWidth,
            ArenaHeight = ArenaHeight,
            NumberOfRounds = NumberOfRounds,
            GunCoolingRate = GunCoolingRate,
            MaxInactivityTurns = MaxInactivityTurns,
            TurnTimeout = TurnTimeout,
            ReadyTimeout = ReadyTimeout
        };
        gameStarted.GameSetup = gameSetup;
        Send(conn, gameStarted);
    }

    private static void SendRoundStarted(IWebSocketConnection conn)
    {
        var roundStarted = new RoundStartedEvent
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.RoundStartedEvent),
            RoundNumber = 1
        };
        Send(conn, roundStarted);
    }

    private void SendTickEventForBot(IWebSocketConnection conn, int turnNumber)
    {
        var tickEvent = new TickEventForBot()
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.TickEventForBot),
            RoundNumber = 1,
            TurnNumber = turnNumber,
        };

        var turnRate = BotTurnRate;
        var gunTurnRate = BotGunTurnRate;
        var radarTurnRate = BotRadarTurnRate;

        if (_botIntent != null)
        {
            turnRate = _botIntent.TurnRate ?? BotTurnRate;
            gunTurnRate = _botIntent.GunTurnRate ?? BotGunTurnRate;
            radarTurnRate = _botIntent.RadarTurnRate ?? BotRadarTurnRate;
        }

        var state = new Schema.BotState
        {
            Energy = _energy,
            X = BotX,
            Y = BotY,
            Direction = _direction,
            GunDirection = _gunDirection,
            RadarDirection = _radarDirection,
            RadarSweep = BotRadarSweep,
            Speed = _speed,
            TurnRate = turnRate,
            GunTurnRate = gunTurnRate,
            RadarTurnRate = radarTurnRate,
            GunHeat = _gunHeat,
            EnemyCount = BotEnemyCount
        };
        tickEvent.BotState = state;

        var bulletState1 = CreateBulletState(1);
        var bulletState2 = CreateBulletState(2);
        tickEvent.BulletStates = new HashSet<Schema.BulletState>
        {
            bulletState1, bulletState2
        };

        var events = new HashSet<Event>();

        var scannedEvent = new ScannedBotEvent()
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.ScannedBotEvent),
            Direction = 45,
            X = 134.56,
            Y = 256.7,
            Energy = 56.9,
            Speed = 9.6,
            TurnNumber = 1,
            ScannedBotId = 2,
            ScannedByBotId = 1
        };
        events.Add(scannedEvent);

        if (_botIntent != null && _botIntent.Firepower != null)
        {
            var bulletEvt = new Schema.BulletFiredEvent
            {
                Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BulletFiredEvent),
                Bullet = CreateBulletState(99)
            };
            events.Add(bulletEvt);
        }

        tickEvent.Events = events;

        Send(conn, tickEvent);
    }

    private static void Send(IWebSocketConnection conn, Object obj)
    {
        conn.Send(JsonConverter.ToJson(obj));
    }

    private static Schema.BulletState CreateBulletState(int id)
    {
        var bulletState = new Schema.BulletState()
        {
            BulletId = id,
            X = 0,
            Y = 0,
            OwnerId = 0,
            Direction = 0,
            Power = 0
        };
        return bulletState;
    }
}
