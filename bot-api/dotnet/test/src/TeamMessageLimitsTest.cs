using System;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Internal;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
[Category("Unit")]
public class TeamMessageLimitsTest
{
    [Test]
    public void CountBoundary()
    {
        Assert.DoesNotThrow(() => IntentValidator.ValidateTeamMessage("hello", Constants.MaxNumberOfTeamMessagesPerTurn - 1));
        Assert.Throws<InvalidOperationException>(() => IntentValidator.ValidateTeamMessage("hello", Constants.MaxNumberOfTeamMessagesPerTurn));
    }

    [Test]
    public void UnicodePayloadBoundary()
    {
        Assert.DoesNotThrow(() => IntentValidator.ValidateTeamMessageSize(new string('é', Constants.TeamMessageMaxSize / 2)));
        Assert.Throws<ArgumentException>(() => IntentValidator.ValidateTeamMessageSize(new string('é', Constants.TeamMessageMaxSize / 2 + 1)));
    }

    [Test]
    public void AggregateBoundary()
    {
        Assert.DoesNotThrow(() => IntentValidator.ValidateTeamMessagesSize(new string('x', Constants.TeamMessagesMaxBytesPerTurn)));
        Assert.Throws<ArgumentException>(() => IntentValidator.ValidateTeamMessagesSize(new string('x', Constants.TeamMessagesMaxBytesPerTurn + 1)));
    }
}
