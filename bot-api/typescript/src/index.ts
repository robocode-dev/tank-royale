// Tank Royale Bot API for TypeScript/JavaScript
// Public API exports will be added here as modules are implemented.
export { Constants } from "./Constants.js";
export { GameType } from "./GameType.js";
export type { GameType as GameTypeValue } from "./GameType.js";
export { DefaultEventPriority } from "./DefaultEventPriority.js";
export { InitialPosition } from "./InitialPosition.js";
export { BotInfo, BotInfoBuilder } from "./BotInfo.js";
export { GameSetup } from "./GameSetup.js";
export { BotState } from "./BotState.js";
export { BulletState } from "./BulletState.js";
export { BotResults } from "./BotResults.js";
export { Color } from "./graphics/Color.js";
export { Point } from "./graphics/Point.js";
export type { IGraphics } from "./graphics/IGraphics.js";
export { SvgGraphics } from "./graphics/SvgGraphics.js";
export { ColorUtil } from "./util/ColorUtil.js";
export { MathUtil } from "./util/MathUtil.js";
export type { Droid } from "./Droid.js";
export type { IBaseBot } from "./IBaseBot.js";
export type { IBot } from "./IBot.js";
export { BaseBot } from "./BaseBot.js";
export { Bot } from "./Bot.js";

// Events
export { BotEvent } from "./events/BotEvent.js";
export { Condition } from "./events/Condition.js";
export { CustomEvent } from "./events/CustomEvent.js";
export { ConnectedEvent } from "./events/ConnectedEvent.js";
export { DisconnectedEvent } from "./events/DisconnectedEvent.js";
export { ConnectionErrorEvent } from "./events/ConnectionErrorEvent.js";
export { GameStartedEvent } from "./events/GameStartedEvent.js";
export { GameEndedEvent } from "./events/GameEndedEvent.js";
export { RoundStartedEvent } from "./events/RoundStartedEvent.js";
export { RoundEndedEvent } from "./events/RoundEndedEvent.js";
export { TickEvent } from "./events/TickEvent.js";
export { BotDeathEvent } from "./events/BotDeathEvent.js";
export { DeathEvent } from "./events/DeathEvent.js";
export { HitBotEvent } from "./events/HitBotEvent.js";
export { HitWallEvent } from "./events/HitWallEvent.js";
export { BulletFiredEvent } from "./events/BulletFiredEvent.js";
export { HitByBulletEvent } from "./events/HitByBulletEvent.js";
export { BulletHitBotEvent } from "./events/BulletHitBotEvent.js";
export { BulletHitBulletEvent } from "./events/BulletHitBulletEvent.js";
export { BulletHitWallEvent } from "./events/BulletHitWallEvent.js";
export { ScannedBotEvent } from "./events/ScannedBotEvent.js";
export { SkippedTurnEvent } from "./events/SkippedTurnEvent.js";
export { WonRoundEvent } from "./events/WonRoundEvent.js";
export { TeamMessageEvent } from "./events/TeamMessageEvent.js";
