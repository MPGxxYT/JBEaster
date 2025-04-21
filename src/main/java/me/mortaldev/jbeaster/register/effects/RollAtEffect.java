package me.mortaldev.jbeaster.register.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.mortaldev.jbeaster.modules.miningegg.MiningEgg;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class RollAtEffect extends Effect {

  static {
    Skript.registerEffect(RollAtEffect.class, "jbeaster roll %player% at %location%");
  }

  private Expression<Player> player;
  private Expression<Location> location;

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
    this.player = (Expression<Player>) expressions[0];
    this.location = (Expression<Location>) expressions[1];
    return true;
  }

  @Override
  public String toString(Event event, boolean debug) {
    return "Roll " + player.toString(event, debug) + " at " + location.toString(event, debug);
  }


  @Override
  protected void execute(Event event) {
    Player[] players = player.getArray(event);
    Location[] locations = location.getArray(event);
    if (players[0] == null) {
      return;
    }
    if (locations[0] == null) {
      return;
    }
    MiningEgg.getInstance().rollAt(players[0], locations[0]);
  }
}
