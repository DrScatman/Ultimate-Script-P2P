package script.wrappers;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;
import script.tasks.fungus.Fungus;

public class WalkingWrapper {

    public static boolean walkToPosition(Position position) {
        return Movement.walkTo(position,
                () -> {
                    if (shouldBreakOnTarget() || shouldEnableRun() || (Fungus.inMortania() && Fungus.inSalveGravyardArea())) {
                        if (Fungus.inMortania() && Fungus.inSalveGravyardArea()) {
                            Log.fine("Handling Gate");
                            handleGate();
                        } else {
                            if (!Movement.isRunEnabled())
                                Movement.toggleRun(true);
                            if (Players.getLocal().getHealthPercent() < 35) {
                                Item food = Inventory.getFirst(f -> f.containsAction("Eat"));
                                if (food != null) {
                                    Log.fine("Eating");
                                    food.interact("Eat");
                                }
                            }
                        }
                    }
                    return false;
                }) || position.distance() < 4;
    }

    public static void walkToNearestBank() {
        Movement.getDaxWalker().walkToBank(() -> {
            if (WalkingWrapper.shouldBreakOnTarget() || WalkingWrapper.shouldEnableRun()) {
                if (!Movement.isRunEnabled())
                    Movement.toggleRun(true);
                if (Players.getLocal().getHealthPercent() < 35) {
                    Item food = Inventory.getFirst(f -> f.containsAction("Eat"));
                    if (food != null) {
                        Log.fine("Eating");
                        food.interact("Eat");
                    }
                }
            }
            return false;
        });
    }

    public static boolean shouldEnableRun() {
        return (Movement.getRunEnergy() > Random.nextInt(5, 15) && !Movement.isRunEnabled());
    }

    public static boolean shouldBreakOnTarget() {
        Npc attacker = Npcs.getNearest(a -> true);
        Player me = Players.getLocal();
        return Movement.getRunEnergy() > 0
                && !Movement.isRunEnabled()
                && me.isHealthBarVisible()
                && attacker != null
                && attacker.getTarget() != null
                && attacker.getTarget().equals(me);
    }

    public static boolean shouldBreakOnRunenergy() {
        return (Movement.getRunEnergy() > Random.nextInt(5, 15) && !Movement.isRunEnabled())
                /*|| Movement.isStaminaEnhancementActive()*/;
    }

    public static void handleGate() {
        Player local = Players.getLocal();
        SceneObject gate = SceneObjects.getNearest("Gate");
        InterfaceComponent enterTheSwamp = Interfaces.getComponent(580, 17);
        InterfaceComponent dontAskMeThisAgain = Interfaces.getComponent(580, 20);
        Log.info("Opening the gate");
        if (gate != null) {
            if (gate.containsAction("Open")) {
                if (gate.interact("Open")) {
                    Time.sleepUntil(() -> !Fungus.AFTER_SALVE_GRAVEYARD_TELEPORT_AREA.contains(local) || enterTheSwamp != null, 30_000);
                    if (dontAskMeThisAgain != null && dontAskMeThisAgain.getMaterialId() == 941) {
                        Log.info("dontAskMeThisAgain is visible");
                        if (dontAskMeThisAgain.interact("Off/On")) {
                            Log.info("Clicked enterTheSwamp");
                            Time.sleepUntil(() -> dontAskMeThisAgain.getMaterialId() == 942, 5000);
                        }
                    }
                    if (enterTheSwamp != null) {
                        if (enterTheSwamp.getMaterialId() == 942) {
                            Log.info("enterTheSwamp is visible and dontAskMeAgain is toggled");
                            if (enterTheSwamp.interact("Yes")) {
                                Log.info("Clicked enterTheSwamp");
                                Time.sleepUntil(() -> !Fungus.AFTER_SALVE_GRAVEYARD_TELEPORT_AREA.contains(local), 5000);
                            }
                        }
                    }
                    if (dontAskMeThisAgain == null && enterTheSwamp != null) {
                        Log.info("enterTheSwamp is visible, but dontAskMeAgain isn't");
                        if (enterTheSwamp.interact("Yes")) {
                            Log.info("Clicked enterTheSwamp");
                            Time.sleepUntil(() -> !Fungus.AFTER_SALVE_GRAVEYARD_TELEPORT_AREA.contains(local), 5000);
                        }
                    }
                }
            }
        }
    }
}
