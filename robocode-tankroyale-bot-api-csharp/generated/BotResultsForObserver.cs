//----------------------
// <auto-generated>
//     Generated using the NJsonSchema v10.1.2.0 (Newtonsoft.Json v9.0.0.0) (http://NJsonSchema.org)
// </auto-generated>
//----------------------

namespace Robocode.TankRoyale.Schema
{
    #pragma warning disable // Disable all warnings

    /// <summary>Individual bot results visible for an observer. Name and version is available.</summary>
    [System.CodeDom.Compiler.GeneratedCode("NJsonSchema", "10.1.2.0 (Newtonsoft.Json v9.0.0.0)")]
    public class BotResultsForObserver : BotResultsForBot 
    {
        /// <summary>Name of bot, e.g. Killer Bee</summary>
        [Newtonsoft.Json.JsonProperty("name", Required = Newtonsoft.Json.Required.Always)]
        [System.ComponentModel.DataAnnotations.Required(AllowEmptyStrings = true)]
        public string Name { get; set; }
    
        /// <summary>Bot version, e.g. 1.0</summary>
        [Newtonsoft.Json.JsonProperty("version", Required = Newtonsoft.Json.Required.Always)]
        [System.ComponentModel.DataAnnotations.Required(AllowEmptyStrings = true)]
        public string Version { get; set; }
    
    
    }
}