package script.tasks.training.magic;

import api.API;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.data.Strings;
import script.quests.priest_in_peril.data.Quest;
import script.wrappers.GEWrapper;
import script.wrappers.SleepWrapper;
import script.wrappers.SupplyMapWrapper;


public class TrainTo13 extends Task {

    boolean hasItems = false;
    boolean hasGear = false;
    boolean boughtItems = false;

    private static final String[] ALL_ITEMS_NEEDED_FOR_ACCOUNT_PREPERATION = new String[]{
            "Lumbridge teleport",
            "Staff of air",
            "Staff of fire",
            "Amulet of glory(6)",
            "Ring of wealth (5)",
            "Air rune",
            "Mind rune",
            "Water rune",
            "Earth rune",
            "Monkfish",
            "Stamina potion(4)",
            "Cheese",
            "Leather gloves",
            "Falador teleport",
            "Games necklace(8)",
            "Rope",
            "Adamant scimitar",
            "Ring of recoil",
            "Bucket",
            "Rune essence",
            "Varrock teleport",
            "Silver sickle"
    };

    public static final String Monkfish = "Monkfish";
    public static final String STAFF_OF_AIR = "Staff of air";
    public static final String GLORY = "Amulet of glory(6)";

    private static final Area TRAINING_AREA = Area.rectangular(3194, 3299, 3209, 3285);

    public static final String[] ALL_ITEMS_NEEDED_FOR_MAGIC_TRAINING = {"Ring of wealth (5)", "Stamina potion(4)", "Mind rune", "Water rune", "Earth rune", "Lumbridge teleport", "Monkfish"};

    @Override
    public boolean validate() {
        if(!boughtItems) {
            if(Inventory.containsAll(ALL_ITEMS_NEEDED_FOR_ACCOUNT_PREPERATION)) {
                Log.info("Setting boughtItem to true");
                boughtItems = true;
            }
            else if (Skills.getLevel(Skill.MAGIC) == 1 && Quest.THE_RESTLESS_GHOST.getVarpValue() == 0 && Inventory.getCount(true, Strings.COINS) > 50_000) {
                GEWrapper.setBuySupplies(true, false, SupplyMapWrapper.getStartingItemsMap());
            }
        }
        return boughtItems && Skills.getLevel(Skill.MAGIC) < 13;
    }

    @Override
    public int execute() {

        Player local = Players.getLocal();

        if (Dialog.canContinue()) {
            Log.info("I am continuing the dialog");
            Dialog.processContinue();
        }

        if (!Movement.isRunEnabled()) {
            if (Movement.getRunEnergy() > Random.mid(5, 30)) {
                Log.info("I am toggling run");
                Movement.toggleRun(true);
            }
        }

        if (!hasItems) {
            if (Inventory.containsAll(ALL_ITEMS_NEEDED_FOR_MAGIC_TRAINING)) {
                Log.info("Setting hasItems to true");
                hasItems = true;
            }
        }

        if (!hasGear) {
            if(Equipment.contains(x -> x.getName().contains(GLORY)) && Equipment.contains(x -> x.getName().contains(STAFF_OF_AIR))){
                Log.info("Setting hasGear to true");
                hasGear = true;
            }
            if (!Equipment.contains(x -> x.getName().contains(GLORY))) {
                if (!Inventory.contains(x -> x.getName().contains(GLORY))) {
                    Log.info("I don't have a glory");
                    API.withdrawItem(false, GLORY, 1);
                    Log.info("Withdrew the glory");
                }
                if (Inventory.contains(x -> x.getName().contains(GLORY))) {
                    Log.info("Got glory in invent");
                    API.wearItem(GLORY);
                    Log.info("Wielded the glory");
                }
            }
            if (!Equipment.contains(x -> x.getName().contains(STAFF_OF_AIR))) {
                if (!Inventory.contains(STAFF_OF_AIR)) {
                    API.withdrawItem(false, STAFF_OF_AIR, 1);
                }
                if (Inventory.contains(STAFF_OF_AIR)) {
                    API.wearItem(STAFF_OF_AIR);
                }
            }
            if (Equipment.contains(x -> x.getName().contains(STAFF_OF_AIR))
                    && Equipment.contains(x -> x.getName().contains(STAFF_OF_AIR))) {
            }
        }


        if (Inventory.containsAnyExcept(ALL_ITEMS_NEEDED_FOR_MAGIC_TRAINING)) {
            if (!Bank.isOpen()) {
                if (Bank.open()) {
                    Time.sleepUntil(Bank::isOpen, SleepWrapper.longSleep7500());
                }
            }
            if (Bank.isOpen()) {
                if (Bank.depositInventory()) {
                    Time.sleepUntil(Inventory::isEmpty, SleepWrapper.longSleep7500());
                }
                if (Inventory.isEmpty()) {
                    withdrawItem("Ring of wealth (5)", 1,false );
                    withdrawItem("Stamina potion(4)", 1,false);
                    withdrawItem("Mind rune", 1000, true);
                    withdrawItem("Water rune", 200,true);
                    withdrawItem("Earth rune", 200,true );
                    withdrawItem("Lumbridge teleport", 5, true);
                    withdrawItem("Monkfish", 15,false);
                }
            }
        }

        if (hasItems && !hasGear) {
            if (Inventory.contains(362)) {
                if (!Bank.isOpen()) {
                    Bank.open();
                }
                if (Bank.isOpen()) {
                    if (Bank.depositAll(362)) {
                        if (Time.sleepUntil(() -> Bank.contains(361), 5000)) {
                            if (Bank.withdrawAll(361)) {
                                Time.sleepUntil(() -> Inventory.getCount(false, Monkfish) >= 15, 5000);
                            }
                        }
                    }
                }
            }
            if (!Inventory.contains(362)) {
                if (Inventory.contains(GLORY)) {
                    Item glory = Inventory.getFirst(GLORY);
                    glory.interact("Wear");
                }
                if (Inventory.contains(STAFF_OF_AIR)) {
                    Item staffOfAir = Inventory.getFirst(STAFF_OF_AIR);
                    staffOfAir.interact("Wield");
                }
            }
        }
        if (hasItems && hasGear) {
            if (!TRAINING_AREA.contains(local)) {
                Movement.walkToRandomized(TRAINING_AREA.getCenter());
            }
            if (TRAINING_AREA.contains(local)) {
                if (!Magic.Autocast.isEnabled()) {
                    if (Skills.getLevel(Skill.MAGIC) < 5) {
                        if (Magic.Autocast.getSelectedSpell() != Spell.Modern.WIND_STRIKE) {
                            Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, Spell.Modern.WIND_STRIKE);
                        }
                    }
                    if (Skills.getLevel(Skill.MAGIC) >= 5 && Skills.getLevel(Skill.MAGIC) < 9) {
                        if (Magic.Autocast.getSelectedSpell() != Spell.Modern.WATER_STRIKE) {
                            Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, Spell.Modern.WATER_STRIKE);
                        }
                    }
                    if (Skills.getLevel(Skill.MAGIC) >= 9 && Skills.getLevel(Skill.MAGIC) < 13) {
                        if (Magic.Autocast.getSelectedSpell() != Spell.Modern.EARTH_STRIKE) {
                            Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, Spell.Modern.EARTH_STRIKE);
                        }
                    }
                }
                if (Magic.Autocast.isEnabled()) {
                    if (Skills.getLevel(Skill.MAGIC) < 5) {
                        if (Magic.Autocast.getSelectedSpell() != Spell.Modern.WIND_STRIKE) {
                            Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, Spell.Modern.WIND_STRIKE);
                        }
                    }
                    if (Skills.getLevel(Skill.MAGIC) >= 5 && Skills.getLevel(Skill.MAGIC) < 9) {
                        if (Magic.Autocast.getSelectedSpell() != Spell.Modern.WATER_STRIKE) {
                            Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, Spell.Modern.WATER_STRIKE);
                        }
                    }
                    if (Skills.getLevel(Skill.MAGIC) >= 9 && Skills.getLevel(Skill.MAGIC) < 13) {
                        if (Magic.Autocast.getSelectedSpell() != Spell.Modern.EARTH_STRIKE) {
                            Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, Spell.Modern.EARTH_STRIKE);
                        }
                    }
                    if (local.getTargetIndex() == -1) {
                        Npc targetNpc = Npcs.getNearest(x -> x.getName().equals("Cow") && x.getTarget() != null && x.getTarget().equals(local) || x.getName().equals("Cow") && x.getTargetIndex() == -1 && x.getHealthPercent() > 0);
                        if (targetNpc != null
                                && targetNpc.interact("Attack"))
                            Time.sleepUntil(() -> local.getTargetIndex() != -1, 5000);
                    }
                    if (Players.getLocal().getHealthPercent() <= 30) {
                        if (Inventory.contains(Monkfish)) {
                            Inventory.getFirst(Monkfish).interact("Eat");
                            Time.sleepUntil(() -> Players.getLocal().getHealthPercent() > 40, Random.mid(2500, 5850));
                            Time.sleep(449, 740);
                        }
                    }
                }
            }
        }

        return SleepWrapper.shortSleep350();
    }

    public void withdrawItem(String item, int amount, boolean stack) {
        if (Inventory.getCount(item) < amount) {
            if (Bank.withdraw(item, amount)) {
                Time.sleepUntil(() -> Inventory.getCount(stack, item) == amount, SleepWrapper.longSleep7500());
                Time.sleep(600);
            }
        }
    }

}
