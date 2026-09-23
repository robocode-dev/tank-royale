using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;

namespace Robocode.TankRoyale.BotApi;

/// <summary>Immutable ordered, non-null payloads delivered as one team-message event on the next turn.</summary>
public sealed class TeamMessageBatch
{
    /// <summary>Returns the ordered batch payloads.</summary>
    public IReadOnlyList<object> Messages { get; }

    /// <summary>Creates a batch from a non-empty sequence of non-null payloads.</summary>
    /// <param name="messages">Payloads in delivery order.</param>
    /// <exception cref="ArgumentException">The sequence is empty or contains a null payload.</exception>
    public TeamMessageBatch(IEnumerable messages)
    {
        if (messages == null) throw new ArgumentException("A team message batch must contain at least one message");
        var items = messages.Cast<object>().ToArray();
        if (items.Length == 0) throw new ArgumentException("A team message batch must contain at least one message");
        if (items.Any(item => item == null)) throw new ArgumentException("A team message batch cannot contain null messages");
        Messages = Array.AsReadOnly(items);
    }
}
