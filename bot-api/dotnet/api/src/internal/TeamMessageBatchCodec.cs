using System;
using System.Linq;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Robocode.TankRoyale.BotApi.Internal.Json;

namespace Robocode.TankRoyale.BotApi.Internal;

internal static class TeamMessageBatchCodec
{
    internal const string MessageType = "team-message-batch-v1";

    internal static string Encode(TeamMessageBatch batch) => JsonConvert.SerializeObject(new
    {
        messages = batch.Messages.Select(value => new
        {
            messageType = value.GetType().ToString(),
            message = Robocode.TankRoyale.BotApi.Internal.Json.JsonConverter.ToJson(value),
        }),
    });

    internal static JArray DecodeItems(string payload)
    {
        var root = JObject.Parse(payload);
        var messages = root["messages"] as JArray;
        if (messages == null || messages.Count == 0)
            throw new ArgumentException("Invalid team message batch payload");
        foreach (var item in messages)
            if (item["messageType"]?.Type != JTokenType.String || item["message"]?.Type != JTokenType.String)
                throw new ArgumentException("Invalid team message batch item");
        return messages;
    }
}
