using System;
using System.Drawing;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// MyFirstDroid
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// This is a droid bot meaning that is has more energy, but no radar.
// Member of the MyFirstTeam. Follows orders of team leader.
// ------------------------------------------------------------------
public class MyFirstDroid : Bot, Droid
{
    // The main method starts our bot
    static void Main(string[] args)
    {
        new MyFirstDroid().Start();
    }

    // Constructor, which loads the bot config file
    MyFirstDroid() : base(BotInfo.FromFile("MyFirstDroid.json")) { }

    // Called when a new round is started -> just print out that the bot is ready
    public override void Run()
    {
        Console.WriteLine("MyFirstDroid ready");

        while (IsRunning)
        {
            Go(); // execute next turn (OnTeamMessage() takes handles all bot logic based on team messages)
        }
        // terminates when this point is reached
    }

    // Called when a team message is received, which will be send from MyTeamLeader
    public override void OnTeamMessage(TeamMessageEvent evt)
    {
        var message = evt.Message;

        if (message is Point)
        {
            // ------------------------------------------------------
            // Message is a point towards a target
            // ------------------------------------------------------

            // Read the target point
            var target = (Point) message;

            // Turn gun to target
            TurnLeft(BearingTo(target.X, target.Y));

            // Fire hard!
            Fire(3);
        }
        else if (message is RobotColors)
        {
            // ------------------------------------------------------
            // Message is containing new robot colors
            // ------------------------------------------------------

            // Read and set the robot colors
            var colors = (RobotColors) message;

            BodyColor = colors.BodyColor;
            TracksColor = colors.TracksColor;
            TurretColor = colors.TurretColor;
            GunColor = colors.GunColor;
            RadarColor = colors.RadarColor;
            ScanColor = colors.ScanColor;
            BulletColor = colors.BulletColor;
        }
    }
}

// ------------------------------------------------------------------
// Communication objects for team messages
// ------------------------------------------------------------------

// Point (x,y) class
class Point
{
    public double X { get; set; }
    public double Y { get; set; }

    public Point(double x, double y)
    {
        X = x;
        Y = y;
    }
}

// Robot colors
class RobotColors
{
    public Color BodyColor { get; set; }
    public Color TracksColor { get; set; }
    public Color TurretColor { get; set; }
    public Color GunColor { get; set; }
    public Color RadarColor { get; set; }
    public Color ScanColor { get; set; }
    public Color BulletColor { get; set; }
}