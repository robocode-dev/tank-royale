//----------------------
// <auto-generated>
//     Generated using the NJsonSchema v10.1.2.0 (Newtonsoft.Json v9.0.0.0) (http://NJsonSchema.org)
// </auto-generated>
//----------------------

namespace Robocode.TankRoyale.Schema
{
    #pragma warning disable // Disable all warnings

    /// <summary>Event occurring when a bot has been hit by a bullet from another bot</summary>
    [System.CodeDom.Compiler.GeneratedCode("NJsonSchema", "10.1.2.0 (Newtonsoft.Json v9.0.0.0)")]
    public class BulletHitBotEvent : Event 
    {
        /// <summary>ID of the bot that got hit</summary>
        [Newtonsoft.Json.JsonProperty("victimId", Required = Newtonsoft.Json.Required.Always)]
        public int VictimId { get; set; }
    
        /// <summary>Bullet that hit the bot</summary>
        [Newtonsoft.Json.JsonProperty("bullet", Required = Newtonsoft.Json.Required.Always)]
        public BulletState Bullet { get; set; }
    
        /// <summary>Damage inflicted by the bullet</summary>
        [Newtonsoft.Json.JsonProperty("damage", Required = Newtonsoft.Json.Required.Always)]
        public double Damage { get; set; }
    
        /// <summary>Remaining energy level of the bot that got hit</summary>
        [Newtonsoft.Json.JsonProperty("energy", Required = Newtonsoft.Json.Required.Always)]
        public double Energy { get; set; }
    
    
    }
}