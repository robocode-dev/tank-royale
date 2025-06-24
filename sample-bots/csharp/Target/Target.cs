using System;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Graphics;

// ------------------------------------------------------------------
// Target
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// A stationary robot that moves when its energy drops below a certain
// threshold. This robot demonstrates how to use custom events.
// ------------------------------------------------------------------
public class Target : Bot
{
    internal int trigger; // Keeps track of when to move

    // The main method starts our bot
    static void Main(string[] args)
    {
        new Target().Start();
    }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        // Set colors
        BodyColor   = Color.White;
        TurretColor = Color.White;
        RadarColor  = Color.White;

        // Initially, we'll move when energy passes 80
        trigger = 80;

        // Add a custom event named "trigger-hit",
        AddCustomEvent(new TriggerHit(this));
    }

    // A custom event occurred
    public override void OnCustomEvent(CustomEvent evt)
    {
        // Check if our custom event "trigger-hit" went off
        if (evt.Condition.Name == "trigger-hit")
        {
            // Adjust the trigger value, or else the event will fire again and again and again...
            trigger -= 20;

            // Print out energy level
            Console.WriteLine("Ouch, down to " + (int)(Energy + .5) + " energy.");

            // Move around a bit
            TurnLeft(65);
            Forward(100);
        }
    }
}

// Condition used for triggering a custom event named "trigger-hit" when the energy level
// gets below the value of the 'trigger' field.
class TriggerHit : Condition
{
    Target bot;

    internal TriggerHit(Target bot) : base("trigger-hit")
    {
        this.bot = bot;
    }

    public override bool Test()
    {
        return bot.Energy <= bot.trigger;
    }
}