using System;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.Sample.Bots
{
  /// <summary>
  /// Target - a sample robot by Mathew Nelson.
  /// Modified by Flemming N. Larsen.
  /// 
  /// Sits still. Moves every time energy drops by 20. This Robot demonstrates custom events.
  /// </summary>
  public class Target : Bot
  {
    internal int trigger; // Keeps track of when to move

    // Main method starts our bot
    static void Main_Target(string[] args)
    {
      AppDomain currentDomain = AppDomain.CurrentDomain;
      currentDomain.UnhandledException += new UnhandledExceptionEventHandler(MyHandler);

      new Target().Start();
    }

    // Constructor, which loads the bot settings file
    Target() : base(BotInfo.FromJsonFile("target-settings.json")) { }

    // Run method
    public override void Run()
    {
      // Set colors
      SetBodyColor("#FFF"); // white
      SetTurretColor("#FFF"); // white
      SetRadarColor("#FFF"); // white

      // Initially, we'll move when energy passes 80
      trigger = 80;

      // Add a custom event named "trigger-hit",
      AddCustomEvent(new TriggerHit(this));

      // While loop that prevents bot from exiting the run() method, which would stop the bot
      while (IsRunning)
      {
        Go(); // just call Go() to skip turn doing nothing
      }
    }

    // OnCustomEvent handler
    public override void OnCustomEvent(CustomEvent evt)
    {
      // If our custom event "trigger-hit" went off,
      if (evt.Condition.Name == "trigger-hit")
      {
        // Adjust the trigger value, or else the event will fire again and again and again...
        trigger -= 20;

        // Print out energy level
        Console.WriteLine("Ouch, down to " + (int)(Energy + .5) + " energy.");

        // Move around a bit.
        TurnLeft(65);
        Forward(100);
      }
    }

    static void MyHandler(object sender, UnhandledExceptionEventArgs args)
    {
      Exception e = (Exception)args.ExceptionObject;
      Console.WriteLine("MyHandler caught : " + e.Message);
      Console.WriteLine("Runtime terminating: {0}", args.IsTerminating);
      Console.WriteLine("StackTrace: " + e.StackTrace);
    }
  }

  class TriggerHit : Condition
  {
    Target bot;

    internal TriggerHit(Target bot) : base("trigger-hit") { this.bot = bot; }

    public override bool Test()
    {
      return bot.Energy <= bot.trigger;
    }
  }
}
