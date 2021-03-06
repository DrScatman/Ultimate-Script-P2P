package script.quests.nature_spirit.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
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
import script.tasks.fungus.Fungus;
import script.wrappers.BankWrapper;
import script.wrappers.SleepWrapper;
import script.wrappers.SupplyMapWrapper;
import script.wrappers.WalkingWrapper;

public class NatureSpirit0 extends Task {

    private boolean hasSupplies;

    @Override
    public boolean validate() {
        return Quest.NATURE_SPIRIT.getVarpValue() == 0
                && Skills.getLevel(Skill.PRAYER) < 50
                && Quest.PRIEST_IN_PERIL.getVarpValue() == 61;
    }

    @Override
    public int execute() {
        if (!hasSupplies) {
            if(Inventory.contains("Varrock teleport")){
                if(Inventory.getFirst("Varrock teleport").interact("Break")) {
                    Time.sleepUntil(()-> !Inventory.contains("Varrock teleport"), 5000);
                }
            }
            if (!Inventory.contains("Silver sickle") || (!Inventory.contains("Ghostspeak amulet") && !Equipment.contains("Ghostspeak amulet"))) {
                BankWrapper.doBanking(false, false, SupplyMapWrapper.getNatureSpiritKeepMap());
            } else {
                hasSupplies = true;
                SupplyMapWrapper.setSupplyMap(SupplyMapWrapper.getNatureSpiritKeepMap());
                Bank.close();
                Time.sleepUntil(Bank::isClosed, 5000);
                if (Inventory.contains("Salve graveyard teleport") && WalkingWrapper.GATE_POSITION.distance() > 10 && !Location.DUNGEON_AREA.contains(Players.getLocal())) {
                    Fungus.useTeleportTab("Salve graveyard teleport");
                }
            }
            return SleepWrapper.shortSleep600();
        }

        if (Inventory.contains("Ghostspeak amulet")) {
            Inventory.getFirst("Ghostspeak amulet").interact(a -> true);
            Time.sleepUntil(() -> Equipment.contains("Ghostspeak amulet"), 5000);
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

        if (!Location.DUNGEON_AREA.contains(Players.getLocal()) && Location.DREZEL_POSITION.distance() > 3) {
            WalkingWrapper.walkToPosition(Location.DREZEL_POSITION);
            return NatureSpirit.getLoopReturn();
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
