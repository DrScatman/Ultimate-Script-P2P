package script.quests.nature_spirit.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.quests.nature_spirit.NatureSpirit;
import script.quests.nature_spirit.data.Location;
import script.quests.nature_spirit.data.Quest;
import script.quests.nature_spirit.wrappers.WalkingWrapper;
import script.wrappers.BankWrapper;
import script.wrappers.SleepWrapper;

public class NatureSpirit0 extends Task {

    private boolean hasSupplies;

    @Override
    public boolean validate() {
        return Quest.NATURE_SPIRIT.getVarpValue() == 0
                && Quest.PRIEST_IN_PERIL.getVarpValue() == 61;
    }

    @Override
    public int execute() {
        if (!hasSupplies) {
            if (!Inventory.contains("Silver sickle")) {
                BankWrapper.openAndDepositAll(false, "Silver sickle", "Ghostspeak amulet");
            } else {
                hasSupplies = true;
            }
            return SleepWrapper.shortSleep600();
        }

        if (Dialog.isOpen()) {
            if (Dialog.canContinue())
                Dialog.processContinue();

            Dialog.process("Is there anything else interesting to do around here?",
                    "Well, what is it, I may be able to help?",
                    "Yes, I'll go and look for him.",
                    "Yes, I'm sure.");

            return NatureSpirit.getLoopReturn();
        }

        if (!Location.DUNGEON_AREA.contains(Players.getLocal()) && Location.ENTRANCE.distance() > 3) {
            Movement.walkTo(Location.ENTRANCE, WalkingWrapper::shouldBreakWalkLoop);
            if (Location.ENTRANCE.distance() > 3 && !Movement.isRunEnabled()) {
                Movement.toggleRun(true);
            }
        } else {
            SceneObject trapdoor = SceneObjects.getNearest("Trapdoor");
            if (trapdoor != null && trapdoor.interact(a -> true)) {
                Time.sleepUntil(() -> SceneObjects.getNearest("Trapdoor") == null, 1000, 10_000);
            }
        }

        Npc drezel = Npcs.getNearest("Drezel");
        InterfaceComponent foodComp = Interfaces.getComponent(11, 2);

        if (foodComp != null && foodComp.isVisible() && foodComp.getText().contains("hands you some food.")) {
            Log.info("Handling food component");
            Dialog.processContinue();
            Game.getClient().fireScriptEvent(299, 1, 1);
            Keyboard.pressEnter();
            return SleepWrapper.shortSleep600();
        }

        if (drezel == null || !drezel.isPositionInteractable()) {
            Movement.walkTo(Location.DREZEL_POSITION);
        }
        else if (!Dialog.isOpen()) {
            drezel.interact("Talk-to");
        }

        return NatureSpirit.getLoopReturn();
    }

    private boolean hasPies() {
        return Inventory.getCount("Apple pie") >= 3 && Inventory.getCount("Meat pie") >= 3;
    }
}
