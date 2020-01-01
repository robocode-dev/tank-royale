//----------------------
// <auto-generated>
//     Generated using the NJsonSchema v10.1.2.0 (Newtonsoft.Json v9.0.0.0) (http://NJsonSchema.org)
// </auto-generated>
//----------------------

namespace Robocode.TankRoyale.Schema
{
    #pragma warning disable // Disable all warnings

    /// <summary>Event occuring when a bullet has hit a wall</summary>
    [System.CodeDom.Compiler.GeneratedCode("NJsonSchema", "10.1.2.0 (Newtonsoft.Json v9.0.0.0)")]
    public class BulletHitWallEvent : Event 
    {
        /// <summary>Bullet that has hit a wall</summary>
        [Newtonsoft.Json.JsonProperty("bullet", Required = Newtonsoft.Json.Required.Always)]
        public BulletState Bullet { get; set; }
    
    
    }
}