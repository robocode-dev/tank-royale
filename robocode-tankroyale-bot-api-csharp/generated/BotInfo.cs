//----------------------
// <auto-generated>
//     Generated using the NJsonSchema v10.1.2.0 (Newtonsoft.Json v9.0.0.0) (http://NJsonSchema.org)
// </auto-generated>
//----------------------

namespace Robocode.TankRoyale.Schema
{
    #pragma warning disable // Disable all warnings

    /// <summary>Bot info</summary>
    [System.CodeDom.Compiler.GeneratedCode("NJsonSchema", "10.1.2.0 (Newtonsoft.Json v9.0.0.0)")]
    public class BotInfo : BotHandshake 
    {
        /// <summary>Host name or IP address</summary>
        [Newtonsoft.Json.JsonProperty("host", Required = Newtonsoft.Json.Required.Always)]
        [System.ComponentModel.DataAnnotations.Required(AllowEmptyStrings = true)]
        public string Host { get; set; }
    
        /// <summary>Port number</summary>
        [Newtonsoft.Json.JsonProperty("port", Required = Newtonsoft.Json.Required.Always)]
        public int Port { get; set; }
    
    
    }
}