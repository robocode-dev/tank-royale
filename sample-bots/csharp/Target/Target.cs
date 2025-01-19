using System;
using System.Drawing;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// Target
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Sits still. Moves every time energy drops by 20.
// This bot demonstrates custom events.
// ------------------------------------------------------------------
public class Target : Bot
{
    internal int trigger; // Keeps track of when to move

    // The main method starts our bot
    static void Main(string[] args)
    {
        new Target().Start();
    }

    // Constructor, which loads the bot config file
    Target() : base(BotInfo.FromFile("Target.json")) { }

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
            TurnRight(65);
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