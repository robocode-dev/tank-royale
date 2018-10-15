export default class GameSetup {
  public static readonly DEFAULT_GAME_TYPE = "melee";
  public static readonly DEFAULT_ARENA_WIDTH = 800;
  public static readonly DEFAULT_ARENA_HEIGHT = 600;
  public static readonly DEFAULT_MIN_NUMBER_OF_PARTICIPANTS = 2;
  public static readonly DEFAULT_MAX_NUMBER_OF_PARTICIPANTS = null;
  public static readonly DEFAULT_NUMBER_OF_ROUNDS = 10;
  public static readonly DEFAULT_GUN_COOLING_RATE = 0.1;
  public static readonly DEFAULT_INACTIVITY_TURNS = 450;
  public static readonly DEFAULT_TURN_TIMEOUT = 100;
  public static readonly DEFAULT_READY_TIMEOUT = 10_000;
  public static readonly DEFAULT_DELAYED_OBSERVER_TURNS = 10;

  public gameType: string = GameSetup.DEFAULT_GAME_TYPE;
  public arenaWidth: number = GameSetup.DEFAULT_ARENA_WIDTH;
  public arenaHeight: number = GameSetup.DEFAULT_ARENA_HEIGHT;
  public minNumberOfParticipants: number =
    GameSetup.DEFAULT_MIN_NUMBER_OF_PARTICIPANTS;
  public maxNumberOfParticipants: number | null =
    GameSetup.DEFAULT_MAX_NUMBER_OF_PARTICIPANTS;
  public numberOfRounds: number = GameSetup.DEFAULT_NUMBER_OF_ROUNDS;
  public gunCoolingRate: number = GameSetup.DEFAULT_GUN_COOLING_RATE;
  public inactivityTurns: number = GameSetup.DEFAULT_INACTIVITY_TURNS;
  public turnTimeout: number = GameSetup.DEFAULT_TURN_TIMEOUT;
  public readyTimeout: number = GameSetup.DEFAULT_READY_TIMEOUT;
  public delayedObserverTurns: number =
    GameSetup.DEFAULT_DELAYED_OBSERVER_TURNS;

  public arenaWidthLocked: boolean = false;
  public arenaHeightLocked: boolean = false;
  public minNumberOfParticipantsLocked: boolean = false;
  public maxNumberOfParticipantsLocked: boolean = false;
  public numberOfRoundsLocked: boolean = false;
  public gunCoolingRateLocked: boolean = false;
  public inactivityTurnsLocked: boolean = false;
  public turnTimeoutLocked: boolean = false;
  public readyTimeoutLocked: boolean = false;
  public delayedObserverTurnsLocked: boolean = false;
}
