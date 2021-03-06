package script.quests.priest_in_peril.tasks;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.task.Task;

import static script.quests.waterfall_quest.data.Quest.PRIEST_IN_PERIL;
import static script.quests.waterfall_quest.data.Quest.WATERFALL;

public class PriestInPeril_7 extends Task {

    private static final String STAMINA_POTION = "Stamina potion(";

    @Override
    public boolean validate() {
        return WATERFALL.getVarpValue() == 10 && PRIEST_IN_PERIL.getVarpValue() == 7;
    }

    @Override
    public int execute() {
        Player local = Players.getLocal();

        if (Dialog.canContinue()) {
            Dialog.processContinue();
        }

        if (!Movement.isRunEnabled()) {
            if (Movement.getRunEnergy() > Random.mid(5, 30)) {
                Movement.toggleRun(true);
            }
        }

        if (!Movement.isStaminaEnhancementActive()) {
            if (Inventory.contains(x -> x.getName().contains(STAMINA_POTION))) {
                Item staminaPotion = Inventory.getFirst(x -> x.getName().contains(STAMINA_POTION));
                if (staminaPotion.interact("Drink")) {
                    Time.sleepUntil(() -> Movement.isStaminaEnhancementActive(), 5000);
                }
            }
        }

        Npc drezel = Npcs.getNearest(3488);
        if (!drezel.isPositionInteractable()) {
            SceneObject jailDoor = SceneObjects.getNearest(3463);
            if (jailDoor != null) {
                if (jailDoor.interact("Open")) {
                    Time.sleepUntil(() -> drezel.isPositionInteractable(), 5000);
                }
            }
        }
        if (drezel.isPositionInteractable()) {
            if (!Dialog.isOpen()) {
                if(drezel.interact("Talk-to")) {
                    Time.sleepUntil(() -> Dialog.isOpen(), 5000);
                }
            }
            if(Dialog.isOpen()){
                if(Dialog.canContinue()){
                    Dialog.processContinue();
                }
            }
        }

        return lowRandom();
    }

    public int lowRandom() {
        return Random.mid(299, 444);
    }

}
